/*******************************************************************************
 *  Copyright 2017 Vincenzo-Maria Cappelleri <vincenzo.cappelleri@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package raw.dht.implementations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import raw.dht.DhtConstants;
import raw.dht.DhtCore;
import raw.dht.DhtID;
import raw.dht.DhtKey;
import raw.dht.DhtNodeExtended;
import raw.dht.DhtSearcher;
import raw.dht.DhtValue;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.exceptions.IncompleteNodeExtendedException;
import raw.dht.implementations.utils.DhtUtils;
import raw.dht.messages.DhtMessage;
import raw.dht.messages.DhtMessage.MessageType;
import raw.dht.messages.UdpDhtMessageMarshaller;
import raw.dht.messages.implementations.tcp.FindValueMessage;
import raw.dht.messages.implementations.udp.FindNodeMessage;
import raw.logger.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * default implementation of {@link DhtSearcher}
 * 
 * @author vic
 *
 */
public class DefaultDhtSearcher implements DhtSearcher {
	
	private DhtCore myOwner;
	
	private List<DhtNodeExtended> unaskedNodes;
	private List<DhtNodeExtended> askedNodes;
	private List<DhtNodeExtended> bestNodes;
	
	private Log log;
	
	private KeysMigrator migrator;
	
	public DefaultDhtSearcher(DhtCore owner) {
		myOwner = owner;
		unaskedNodes = Collections.synchronizedList(new ArrayList<DhtNodeExtended>());
		askedNodes = Collections.synchronizedList(new ArrayList<DhtNodeExtended>());
		bestNodes = Collections.synchronizedList(new ArrayList<DhtNodeExtended>());
		
		log = Log.getLogger();
		log.verboseDebug("Searcher constructed and linked to: "+myOwner);
		
		migrator = null;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtSearcher#lookup(raw.dht.DhtID)
	 */
	@Override
	public synchronized Collection<DhtNodeExtended> lookup(DhtID id) {
		log.verboseDebug("Starting lookup for: "+id);
		synchronized (unaskedNodes) {			
			unaskedNodes.clear();
		}
		synchronized (askedNodes) {
			askedNodes.clear();
		}
		synchronized (bestNodes) {
			bestNodes.clear();
		}			

		Collection<DhtNodeExtended> myKnownNodes = myOwner.lookupInTable(id);
		log.verboseDebug("Lookup in my own table returned "+myKnownNodes.size()+" nodes.");
		try {
			if(myKnownNodes.contains(myOwner.getNodeExtended())){
				log.verboseDebug("My own node is in the list. Removing it from nodes to be asked and adding it to best nodes list.");
				myKnownNodes.remove(myOwner.getNodeExtended());
				if(myOwner.isThisNodeOld()){
					synchronized (bestNodes) {
						bestNodes.add(myOwner.getNodeExtended());
					}
					log.verboseDebug("My own node is old, adding it to bestNodes candidates.");
				}
			}
		} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
			log.exception(e);
		}
		
		synchronized (askedNodes) {
			try {
				askedNodes.add(myOwner.getNodeExtended());
			} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
				log.exception(e);
			} //avoid to ask myself! 			
		}
		
		synchronized (unaskedNodes) {			
			unaskedNodes.addAll(myKnownNodes);
		}
		synchronized (bestNodes) {			
			bestNodes.addAll(myKnownNodes);
		}
		
		recursiveFindNodes(id);
		log.verboseDebug("Recursive lookup terminated.");
		
		pingIfNotKnown(bestNodes);
		
		return bestNodes;
	}
	
	/**
	 * If a node in the given list is NOT
	 * known to the routing table of this node
	 * it will be pinged.
	 * 
	 * @param nodesList
	 */
	private void pingIfNotKnown(List<DhtNodeExtended> nodesList){
		ImmutableList<DhtNodeExtended> nodes = ImmutableList.copyOf(nodesList);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for(DhtNodeExtended node : nodes){
					if(!myOwner.getRoutingTable().isPresentInTable(node)){
						log.verboseDebug(node+" appears to be unknown. Try and ping it.");
						myOwner.pingNode(node);
					} else {
						log.verboseDebug(node+" appears to BE known. Don't need to ping it.");
					}
				}
			}
		};
		Future<?> pingerFuture = myOwner.getThreadPool().submit(runnable);
		try {
			pingerFuture.get(1, TimeUnit.NANOSECONDS);
		} catch (InterruptedException | ExecutionException e) {
			log.exception(e);
		} catch (TimeoutException e) {
			log.verboseDebug("Started thread checking new nodes resulting from lookup.");
		}
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtSearcher#lookup(raw.dht.DhtKey)
	 */
	@Override
	public synchronized Collection<DhtNodeExtended> lookup(DhtKey key) {
		return lookup(key.getKeyId());
	}
	
	private synchronized void recursiveFindNodes(DhtID id){
		List<DhtNodeExtended> toBeAsked;
		synchronized (unaskedNodes) {			
			int numberOfNodes = Math.min(DhtConstants.ALPHA_SIZE, unaskedNodes.size());
			toBeAsked = new CopyOnWriteArrayList<DhtNodeExtended>(unaskedNodes.subList(0, numberOfNodes));
			unaskedNodes.removeAll(toBeAsked);
		}
		synchronized (askedNodes) {			
			askedNodes.addAll(toBeAsked);
		}
		
		List<DhtNodeExtended> received = askNodes(toBeAsked, id);
		
		for(DhtNodeExtended node : received){
			synchronized (askedNodes) {				
				if(!askedNodes.contains(node)){
					synchronized (unaskedNodes) {
						unaskedNodes.add(node);						
					}
				}
			}
		}
		
		synchronized (askedNodes) {			
			sortNodesList(askedNodes, id);
		}
		synchronized (unaskedNodes) {			
			sortNodesList(unaskedNodes, id);
		}
		synchronized (bestNodes) {			
			sortNodesList(bestNodes, id);
		}

		int unaskedSize;
		synchronized (unaskedNodes) {
			unaskedSize = unaskedNodes.size();
		}
		if(unaskedSize > 0 && updatedBestNodesList(received, id)){ 
			recursiveFindNodes(id);
		} else {
			return;
		}
	}
	
	/**
	 * Asks to other nodes a set of {@link DhtNodeExtended}
	 * close to a given {@link DhtID}.
	 * 
	 * @param nodesToBeAsked
	 * @param id
	 * @return
	 */
	private List<DhtNodeExtended> askNodes(List<DhtNodeExtended> nodesToBeAsked, DhtID id){
		UdpDhtMessageMarshaller marshaller = UdpDhtMessageMarshaller.getMarshaller(); 
		
		FindNodeMessage request;
		try {
			request = new FindNodeMessage(myOwner.getNodeExtended(), id);
		} catch (IncoherentTransactionException | IncompleteNodeExtendedException e2) {
			log.exception(e2);
			return new ArrayList<DhtNodeExtended>();
		}
		DatagramPacket requestDatagram = marshaller.messageToDatagram(request);
		DatagramSocket socket;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			log.exception(e);
			return new ArrayList<DhtNodeExtended>();
		}
		
		try {
			socket.setSoTimeout(DhtConstants.TIMEOUT_MILLISECONDS);
		} catch (SocketException e1) {
			log.exception(e1);
		}
		
		for(DhtNodeExtended node : nodesToBeAsked){
			requestDatagram.setAddress(node.getAddress().getAddress());
			requestDatagram.setPort(node.getAddress().getUdpPort());
			try {
				socket.send(requestDatagram);
				log.verboseDebug("Sent request for "+id+" to node "+node);
			} catch (IOException e) {
				log.exception(e);
			}
		}
		
		HashSet<DhtNodeExtended> nodes = new HashSet<DhtNodeExtended>();
		for (int i = 0; i < nodesToBeAsked.size(); i++) {
			byte[] buf = new byte[marshaller.getBufferSize()];
			DatagramPacket received = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(received);
			} catch (IOException e) {
				log.exception(e);
				continue;
			}
			DhtMessage converted = marshaller.datagramToMessage(received);
			if(converted.getMessageType() == MessageType.FIND_NODE_REPLY){
				FindNodeMessage findNodeReply = (FindNodeMessage) converted;
				if(myOwner.isOldWorker(findNodeReply.getSender())){
					nodes.addAll(findNodeReply.getReplyNodes());
				}
			}
		}
		socket.close();
		
		ArrayList<DhtNodeExtended> returnable = new ArrayList<DhtNodeExtended>();
		for(DhtNodeExtended node : nodes){
			if(myOwner.isOldWorker(node)){
				returnable.add(node);
			}
		}
		return returnable;
	}
	
	/**
	 * Use this method to update <tT>bestNodes</tt>.
	 * 
	 * @param newNodes
	 * @param baseId
	 * @return <tt>true</tT> if nodes from <tt>newNodes</tT> are added to <tt>newNodes<tt>, <tt>false</tt> otherwise
	 */
	private boolean updatedBestNodesList(List<DhtNodeExtended> newNodes, DhtID baseId){
		log.verboseDebug("Update best nodes list.");
		Set<DhtNodeExtended> updated = new HashSet<DhtNodeExtended>(bestNodes);
		updated.addAll(newNodes);
		List<DhtNodeExtended> updatedList = new ArrayList<DhtNodeExtended>(updated);
		sortNodesList(bestNodes, baseId);
		DhtNodeExtended currentBestNode = bestNodes.get(0);
		log.verboseDebug("Gonna filter my original "+updatedList.size()+" nodes.");
		filterNodesList(updatedList, baseId, currentBestNode.getID());
		log.verboseDebug("After filtering the nodes are just "+updatedList.size()+".");
		Collections.shuffle(updatedList);
		if(updatedList.size() > DhtConstants.ALPHA_SIZE){
			updatedList = updatedList.subList(0, DhtConstants.ALPHA_SIZE);
		}
		boolean containsAll;
		synchronized (bestNodes) {
			containsAll = bestNodes.containsAll(updatedList);
		}
		if(containsAll){
			return false;
		} else {
			bestNodes = updatedList;
			return true;
		}
	}
	
	/**
	 * sort <tt>listToSort</tt> based on a XOR metric with respect to
	 * <tt>baseId</tt>.
	 * 
	 * @param listToSort
	 * @param baseId
	 */
	private void sortNodesList(List<DhtNodeExtended> listToSort, final DhtID baseId){
		synchronized (listToSort) {
			Collections.sort(listToSort, new Comparator<DhtNodeExtended>() {
				@Override
				public int compare(DhtNodeExtended o1, DhtNodeExtended o2) {
					BigInteger first = DhtUtils.xor(o1.getID(), baseId);
					BigInteger second = DhtUtils.xor(o2.getID(), baseId);
					return first.compareTo(second);
				}
			});
		}
	}
	
	/**
	 * Removes from <code>listToFilter</code> the nodes that are NOT 
	 * closer to targetId.
	 * 
	 * @param listToFilter
	 * @param targetId
	 * @param bestIdSoFar
	 */
	private void filterNodesList(List<DhtNodeExtended> listToFilter, DhtID targetId, DhtID bestIdSoFar){
		Predicate<DhtNodeExtended> removeNodes = new Predicate<DhtNodeExtended>() {
			BigInteger threshold = DhtUtils.xor(bestIdSoFar, targetId);
			@Override
			public boolean test(DhtNodeExtended t) {
				BigInteger testedInteger = DhtUtils.xor(t.getID(), targetId);
				return testedInteger.compareTo(threshold) > 0;
			}
		};
		listToFilter.removeIf(removeNodes);
	}
	
	

	/* (non-Javadoc)
	 * @see raw.dht.DhtSearcher#findValues(raw.dht.DhtKey)
	 */
	@Override
	public Collection<DhtValue> findValues(DhtKey key) {
		Collection<DhtValue> results = null;
		Collection<DhtNodeExtended> nodes = lookup(key);
		HashSet<DhtValue> values = new HashSet<DhtValue>(); 
		if(nodes.size() > 0){
			try {
				if(nodes.contains(myOwner.getNodeExtended())){
					Collection<DhtValue> myVals = myOwner.getKeyHolder().get(key);
					if(myVals != null){
						values.addAll(myVals);
					}

					nodes.remove(myOwner.getNodeExtended());
				}
			} catch (IncoherentTransactionException | IncompleteNodeExtendedException e2) {
				log.exception(e2);
			}
			Collection<RetrieveValue> futures = new ArrayList<DefaultDhtSearcher.RetrieveValue>();
			for(DhtNodeExtended node : nodes){
				futures.add(new RetrieveValue(node, key));
			}
			
			List<Future<Collection<DhtValue>>> futuresResults = null;			
			try {
				futuresResults = myOwner.getThreadPool().invokeAll(futures);
			} catch (InterruptedException e1) {
				log.exception(e1);
			}
			if(futuresResults != null){				
				for(Future<Collection<DhtValue>> futureResult : futuresResults){
					Collection<DhtValue> valuesFromNode;
					try {
						valuesFromNode = futureResult.get();
					} catch (InterruptedException | ExecutionException e) {
						log.exception(e);
						continue;
					}
					if(valuesFromNode != null && valuesFromNode.size() > 0){						
						values.addAll(valuesFromNode);
					}
				}
			}
			
			if(values.size() > 0){
				results = values;
				log.verboseDebug("Found "+values.size()+" values for "+key);
			} else {
				log.verboseDebug("No values found for "+key);
			}
		}
		return results;
	}
	
	private class RetrieveValue implements Callable<Collection<DhtValue>>{
		
		private DhtKey key;
		private DhtNodeExtended nodeToAsk;
		
		public RetrieveValue(DhtNodeExtended nodeToAsk, DhtKey key) {
			this.nodeToAsk = nodeToAsk;
			this.key = key;
		}

		@Override
		public Collection<DhtValue> call() throws Exception {
			return askValue();
		}
		
		private Collection<DhtValue> askValue(){
			InetSocketAddress address = nodeToAsk.getAddress().getTcpSocketAddress();
			Collection<DhtValue> values = null;
			FindValueMessage request;
			try {
				request = new FindValueMessage(myOwner.getNodeExtended(), key);
			} catch (IncoherentTransactionException | IncompleteNodeExtendedException e1) {
				log.exception(e1);
				return null;
			}
			try (Socket sock = new Socket(address.getAddress(), address.getPort())){
				sock.setSoTimeout(DhtConstants.TIMEOUT_MILLISECONDS);
				ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
				oos.writeObject(request);
				log.verboseDebug("Sent request for value of key "+key+"to "+nodeToAsk);
				ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
				Object received = ois.readObject();
				if(received instanceof FindValueMessage){
					FindValueMessage reply = (FindValueMessage) received;
					if(reply.getMessageType() == MessageType.FIND_VALUE_REPLY){
						if(myOwner.isOldWorker(reply.getSender())){
							if(reply.getValue().size() > 0){
								values = reply.getValue();
								log.verboseDebug("Got a reply with "+values.size()+" values.");
							}
						} else {
							log.verboseDebug(reply.getSender()+" did not provide a valid isOldWorker proof. Rejecting answer.");
						}
					}
				}
			} catch (Exception e) {
				log.exception(e);
				for(Throwable t : e.getSuppressed()){
					log.exception(t);
				}
			} 
			return values;
		}
		
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtSearcher#startKeysMigrator()
	 */
	@Override
	public void startKeysMigrator() {
		if(migrator == null){
			migrator = new KeysMigrator();
			Future<?> migratorFuture = myOwner.getThreadPool().submit(migrator);
			try {
				migratorFuture.get(1, TimeUnit.NANOSECONDS);
			} catch (InterruptedException | ExecutionException e) {
				log.exception(e);
			} catch (TimeoutException e) {
				log.verboseDebug("Keys migrator is created and running on its own thread.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtSearcher#stopKeysMigrator()
	 */
	@Override
	public boolean stopKeysMigrator() {
		if(migrator == null){
			return true;
		}
		return migrator.stop();
	}
	
	private class KeysMigrator implements Runnable{
		
		private boolean running;
		private boolean hasStopped;
		
		public KeysMigrator() {
			running = true;
			hasStopped = false;
		}

		@Override
		public void run() {
			log.verboseDebug("Starting keys migrator job.");
			while (running) {
				log.verboseDebug("Wait a cycle before checking migration.");
				try {
					Thread.sleep(DhtConstants.KEYS_MIGRATION_CYCLE_MILLISECONDS);
				} catch (InterruptedException e) {
					log.exception(e);
				}
				if(myOwner.getKeyHolder() instanceof DefaultDhtKeyHolder){
					DefaultDhtKeyHolder h = (DefaultDhtKeyHolder) myOwner.getKeyHolder();
					ImmutableMap<DhtKey, Collection<DhtValue>> map = h.getCurrentTable().asMap();
					for(DhtKey key : map.keySet()){
						Collection<DhtNodeExtended> nodes = lookup(key);
						try {
							if(!nodes.contains(myOwner.getNodeExtended())){
								log.verboseDebug("Looks like there is another node closer then mine to "+key);
								Collection<DhtValue> values = map.get(key);
								for(DhtValue vaue : values){
									myOwner.store(key, vaue);
								}
								log.verboseDebug("Finished migration of "+key);
							}
						} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
							log.exception(e);
						}
					}
					log.verboseDebug("Done with migration operations for this cycle.");
				} else {
					log.verboseDebug("Impossible to migrate keys. Aborting migrator.");
					running = false;
				}				
			}
			hasStopped = true;
		}
		
		public boolean stop(){
			log.verboseDebug("Stopping keys migrator.");
			running = false;
			while (!hasStopped) {
				log.verboseDebug("Waiting for migrator to stop...");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					log.exception(e);
				}
			}
			log.verboseDebug("Keys migrator stopped.");
			return true;
		}
		
	}

}
