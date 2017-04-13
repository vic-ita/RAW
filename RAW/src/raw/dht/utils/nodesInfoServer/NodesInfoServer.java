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
package raw.dht.utils.nodesInfoServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import raw.concurrent.RAWExecutors;
import raw.dht.DhtConstants;
import raw.dht.DhtCore;
import raw.dht.DhtNode;
import raw.dht.DhtNodeExtended;
import raw.dht.RoutingTable;
import raw.dht.implementations.DefaultDhtCore;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.exceptions.IncompleteNodeExtendedException;
import raw.logger.Log;
import raw.settings.DhtProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

/**
 * This service will reply known {@link DhtNode}s informations
 * in response to a network-query.
 * 
 * @author vic
 *
 */
public class NodesInfoServer implements Callable<Void> {
	
	private ServerSocket listeningSocket;
	
	private Log log;
	
	private DhtCore coreReference;
	
	private boolean running;
	
	public NodesInfoServer() {
		log = Log.getLogger();
		
		DhtProperties props = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
		
		try {
			listeningSocket = new ServerSocket(props.getNodesInfoServerPort());
		} catch (IOException e) {
			log.exception(e);
		}
		
		coreReference = DefaultDhtCore.getCore();
	}

	@Override
	public Void call() throws Exception {
		log.verboseDebug("Nodes' info server starting.");
		running = true;
		listeningSocket.setSoTimeout(DhtConstants.TIMEOUT_MILLISECONDS);
		while (running) {
			listen();
		}
		log.debug("Server loop ended. Closing this service.");
		return null;
	}
	
	private void listen() {
		log.verboseDebug("Listening on socket.");
				
		try {
			spawnSocketThread(listeningSocket.accept());
		} catch (IOException e) {
			if(e instanceof SocketTimeoutException){
				log.verboseDebug("Socket timeout.");
				return;
			}
			if(e instanceof SocketException){
				log.debug("Socket exception while listening. Cuse: "+e.getMessage());
			}
			log.exception(e);
			return;
		}		
	}
	
	private void spawnSocketThread(Socket receivedSock){
		
		Spawned managingThread = new Spawned(receivedSock);
//		Future<?> spawned = coreReference.getThreadPool().submit(new Spawned(receivedSock));
//		Future<Void> spawned = coreReference.getThreadPool().submit(managingThread);
		ExecutorService executor = RAWExecutors.newSingleThreadExecutor();
		Future<Void> spawned = executor.submit(managingThread);
		log.verboseDebug("Managing callable submitted.");
		try {
			spawned.get(1, TimeUnit.NANOSECONDS);
		} catch (InterruptedException | ExecutionException e) {
			log.exception(e);
		} catch (TimeoutException e) {
			log.verboseDebug("Started thread to manage incoming communication.");
		}
		
	}
	
	private class Spawned implements Callable<Void>{
		private Socket receivedSock;
		
		public Spawned(Socket receivedSock) {
			this.receivedSock = receivedSock;
			log.verboseDebug("Callable constructed.");
		}

		@Override
		public Void call() throws Exception {
			log.verboseDebug("Beginning callable.");
			manageCommunication(receivedSock);
			return null;
		}
	}
	
	private void manageCommunication(Socket sock){
		log.verboseDebug("Socket opened.");
		try {
			sock.setSoTimeout(DhtConstants.TIMEOUT_MILLISECONDS);
		} catch (SocketException e) {
			log.exception(e);
			return;
		}
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(sock.getInputStream());
		} catch (IOException e) {
			log.exception(e);
			return;
		}
		log.verboseDebug("Reading object.");
		Object obj;
		try {
			obj = ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			log.exception(e);
			return;
		}
		log.verboseDebug("Object read. (obj = "+obj+").");
		if(obj instanceof NodesInfoRequestMessage){
			log.verboseDebug("Received a NodesInfoRequestMessage");
			if(!((NodesInfoRequestMessage)obj).isRequest()){
				log.verboseDebug("Received message is not a request. Returning.");
				return;
			}
			log.verboseDebug("NodesInfoRequestMessage is a request. Proceed to serve a reply.");
			RoutingTable routingTable = coreReference.getRoutingTable();
			log.verboseDebug("Retrieved routing table: "+routingTable);
//			Collection<DhtNode> nodesCollection = coreReference.getRoutingTable().getFullSetOfNodes();
			Collection<DhtNodeExtended> nodesCollection = routingTable.getFullSetOfNodes(false);
			if(nodesCollection == null){
				log.verboseDebug("Routing table returned no nodes.");
				nodesCollection = new ArrayList<DhtNodeExtended>();
			} else {				
				log.verboseDebug("From routing table i got "+nodesCollection.size()+" nodes.");
			}
			List<DhtNodeExtended> allNodes; // = new ArrayList<>(coreReference.getRoutingTable().getFullSetOfNodes());
			if(nodesCollection.size() <= DhtConstants.NUMBER_OF_NODES_INFO){
				log.verboseDebug("My routing table has less then "+DhtConstants.NUMBER_OF_NODES_INFO+" so we'll use them all.");
				allNodes = new ArrayList<DhtNodeExtended>(nodesCollection);
			} else {
				List<DhtNodeExtended> tmp = new ArrayList<DhtNodeExtended>(nodesCollection);				
				Collections.shuffle(tmp);
				
				allNodes = tmp.subList(0, DhtConstants.NUMBER_OF_NODES_INFO);
				log.verboseDebug("Randomly selected "+DhtConstants.NUMBER_OF_NODES_INFO+" nodes from routing table.");
			}
			//note that DefaultRoutingTable does not include the owner node's info. Adding them manually.
//			DhtNode myNode = coreReference.getNode();
			try {
				DhtNodeExtended myNode = coreReference.getNodeExtended();
				if(!allNodes.contains(myNode)){
					allNodes.add(myNode);
					
				}				
			} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
				log.exception(e);
			}
			
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				log.exception(e);
				return;
			}
			log.verboseDebug("Starting to feed the nodes info to requesting node.");
			for(DhtNodeExtended node : allNodes){
				try {
					oos.writeObject(node);
					oos.flush();
					log.verboseDebug("Sent info: "+node);
				} catch (IOException e) {
					log.exception(e);
					return; 
				}
			} 
			log.verboseDebug("All nodes info sent.");
			try {
				oos.writeObject(new NodesInfoRequestMessage(false));
			} catch (IOException e1) {
				log.exception(e1);
			}
			log.verboseDebug("End message sent.");
			try {
				ois.close();
			} catch (IOException e) {
				log.exception(e);
			}
			try {
				oos.close();
			} catch (IOException e) {
				log.exception(e);
			}
			try {
				sock.close();
			} catch (IOException e) {
				log.exception(e);
			}
		} else {
			log.verboseDebug("What I received was not a correct Request Message.");
		}
		return;		
	}
	
	public void stop(){
		try {
			listeningSocket.close();
		} catch (IOException e) {
			log.exception(e);
		}
		running = false;
		log.verboseDebug("Received halt signal.");
	}
}
