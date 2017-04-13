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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import raw.blockChain.BlockChainCore;
import raw.blockChain.api.implementations.utils.TransactionUtils;
import raw.dht.DhtConstants;
import raw.dht.DhtCore;
import raw.dht.DhtListener;
import raw.dht.DhtNodeExtended;
import raw.dht.DhtValue;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.exceptions.IncompleteNodeExtendedException;
import raw.dht.implementations.utils.DhtSigningUtils;
import raw.dht.messages.DhtMessage;
import raw.dht.messages.DhtMessage.MessageType;
import raw.dht.messages.UdpDhtMessageMarshaller;
import raw.dht.messages.implementations.tcp.FindValueMessage;
import raw.dht.messages.implementations.tcp.StoreMessage;
import raw.dht.messages.implementations.udp.FindNodeMessage;
import raw.dht.messages.implementations.udp.PingMessage;
import raw.logger.Log;
import raw.settings.DhtProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

/**
 * Default implementation of {@link DhtListener}
 * 
 * @author vic
 *
 */
public class DefaultDhtListener implements DhtListener {
	
	private DhtCore myCore;
	private DhtProperties props;
	
	private BlockChainCore myChainCore;
	
	private Log log;
	
	private UdpListener udpListener;
	private TcpListener tcpListener;
	
	public DefaultDhtListener(DhtCore core, BlockChainCore chainCore) {
		myCore = core;
		props = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
		
		myChainCore = chainCore;
		
		log = Log.getLogger();
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Void call() throws Exception {
		udpListener = new UdpListener();
		tcpListener = new TcpListener();
		
		Future<Void> udpFuture = myCore.getThreadPool().submit(udpListener);
		
		Future<Void> tcpfFuture = myCore.getThreadPool().submit(tcpListener);
		
		try {			
			tcpfFuture.get(1, TimeUnit.NANOSECONDS);
		} catch (TimeoutException e) {
			log.verboseDebug("TCP listener thread is running.");
		}
		
		log.verboseDebug("Launching UDP thread.");
		udpFuture.get();
		return null;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtListener#stop()
	 */
	@Override
	public boolean stop() {
		if(udpListener != null){
			udpListener.stop();
		}
		
		while (!udpListener.isStopped()) {
			log.verboseDebug("Waiting for UDP listener to stop.");
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				log.exception(e);
			}
		}
		
		if(tcpListener != null){
			tcpListener.stop();
		}
		
		while (!tcpListener.isStopped()) {
			log.verboseDebug("Waiting for TCP listener to stop.");
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				log.exception(e);
			}
		}
		
		return true;
	}
	
	private class UdpListener implements Callable<Void>{
		
		private boolean running;
		private boolean halted;
		private UdpDhtMessageMarshaller marshaller;
		private DatagramSocket socket;
		
		UdpListener() {
			running = true;
			halted = false;
			marshaller = UdpDhtMessageMarshaller.getMarshaller();
		}

		@Override
		public Void call() throws Exception {
			socket = new DatagramSocket(props.getUdpListeningSocket());
			socket.setSoTimeout(4*DhtConstants.TIMEOUT_MILLISECONDS);
			while (running) {
				listen();
			}
			if(socket != null && !socket.isClosed()){
				socket.close();				
			}
			halted = true;
			log.info("Udp Listener: exiting.");
			return null;
		}
		
		private void listen(){
			log.debug("Listening on UDP port "+socket.getLocalPort());
			byte[] buf = new byte[marshaller.getBufferSize()];
			DatagramPacket received = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(received);
			} catch (IOException e) {
				if(e instanceof SocketTimeoutException){
					log.debug("Socket timed out. Abort listening.");
					return;
				}
				if(e instanceof SocketException){
					log.debug("Socket is closed. Abort listening");
					return;
				}
				log.exception(e);
				return;
			}
			log.verboseDebug("Datagram received.");
			final DhtMessage decoded = marshaller.datagramToMessage(received);
			log.verboseDebug("Datagram decoded: "+decoded);
			if(decoded instanceof PingMessage){
				log.verboseDebug("Ping message received.");
				Future<?> pongFuture = myCore.getThreadPool().submit(new Runnable() {
					@Override
					public void run() {
						log.verboseDebug("Pong thread started. (decoded = "+decoded+")");
						PingMessage ping = (PingMessage) decoded;
						if(ping.isPingReply()){
							//not a ping.
							log.verboseDebug("Message is not a ping. Aborting.");
							return;
						}
						try {
							if(ping.getSender().equals(myCore.getNodeExtended()) || ping.getSender().getAddress().equals(myCore.getNodeExtended().getAddress())){
								//wrong address
								log.verboseDebug("Looks like ping message is coming from my own node. Aborting.");
								return;
							}
						} catch (IncoherentTransactionException | IncompleteNodeExtendedException e1) {
							log.exception(e1);
							log.verboseDebug("Exception retrieving local node data. Aborting.");
							return;
						}

						if(!TransactionUtils.isValid(ping.getSender().getTransaction(), myChainCore)){
							log.verboseDebug("Sender node for this ping message didn't sent a walid proof of work token. Still sending a pong reply.");
						}
						log.verboseDebug("PingMessage is a valid ping. (from "+received.getSocketAddress()+").");
						byte[] signature;
						try {
							signature = DhtSigningUtils.signDhtAddress(myCore.getNodeExtended().getAddress(), myCore.getKeyPair().getPrivate());
						} catch (IncoherentTransactionException | IncompleteNodeExtendedException e1) {
							log.exception(e1);
							log.verboseDebug("Exception retrieving local node data. Aborting.");
							return;
						}
						
						PingMessage pong;
						try {
							pong = new PingMessage(true, myCore.getNodeExtended(), signature);
						} catch (IncoherentTransactionException | IncompleteNodeExtendedException e1) {
							log.exception(e1);
							log.verboseDebug("Exception retrieving local node data. Aborting.");
							return;
						}
						DatagramPacket datagramPong = marshaller.messageToDatagram(pong);
						datagramPong.setSocketAddress(received.getSocketAddress());
						log.verboseDebug("Pong datagram ready to be sent.");						
						try {
							socket.send(datagramPong);
						} catch (IOException e) {
							log.exception(e);
						}
						log.debug("Pong sent!");
//						DhtUtils.authenticateNodeAndInsert(ping);
					}
				});
				try {
					pongFuture.get(1, TimeUnit.NANOSECONDS);
				} catch (InterruptedException | ExecutionException e) {
					log.exception(e);
				} catch (TimeoutException e) {
					log.verboseDebug("Pong thread is running.");
				}
			} else if (decoded instanceof FindNodeMessage) {
				log.verboseDebug("Received a find node message.");
				Future<?> finNodeReplyFuture = myCore.getThreadPool().submit(new Runnable() {
					@Override
					public void run() {
						FindNodeMessage request = (FindNodeMessage) decoded;
						if(request.getMessageType() != MessageType.FIND_NODE){
							log.verboseDebug("Received FindNodeMessage is not a request. Aborting...");
							return;
						}
						log.verboseDebug("Received FindNodeMessage targeting "+request.getTargetId());
						Collection<DhtNodeExtended> nodes;
						boolean pingSender = false;
						if(!TransactionUtils.isValid(request.getSender().getTransaction(), myChainCore)){
							log.verboseDebug("Request came from a node who did not provide correct proof of continuous work. Replying anyway.");
						} else {
							log.verboseDebug("The node who made the request provided the proof of continuous work");
							pingSender = true;
						}
						nodes = myCore.lookupInTable(request.getTargetId());
						log.verboseDebug("My lookup returned "+nodes.size()+" nodes.");
						
						FindNodeMessage reply;
						try {
							reply = new FindNodeMessage(myCore.getNodeExtended(), request.getTargetId(), nodes);
						} catch (IllegalArgumentException e1) {
							log.exception(e1);
							return;
						} catch (IncoherentTransactionException | IncompleteNodeExtendedException e1) {
							log.exception(e1);
							log.verboseDebug("Exception retrieving local node data. Aborting.");
							return;
						}
						DatagramPacket datagramReply = marshaller.messageToDatagram(reply);
						
						datagramReply.setSocketAddress(received.getSocketAddress());
						
						try {
							socket.send(datagramReply);
						} catch (IOException e) {
							log.exception(e);
						}
						log.debug("Find node reply sent!");
						
						String nodesToString = "{";
						for(DhtNodeExtended node : nodes){
							nodesToString = nodesToString+node+"; ";
						}
						nodesToString = nodesToString+"}";
						log.verboseDebug("Reply contained this nodes:"+nodesToString);
						
						if(pingSender){
							myCore.pingNode(request.getSender());
						}
					}
				});
				try {
					finNodeReplyFuture.get(1, TimeUnit.NANOSECONDS);
				} catch (InterruptedException | ExecutionException e) {
					log.exception(e);
				} catch (TimeoutException e) {
					log.verboseDebug("Find node reply thread is running.");
				}
			} else {
				log.debug("Received message could not be correctly decoded. Dropping it.");
			}
		}
		
		protected boolean stop() {
			log.verboseDebug("Stopping UDP listener.");
			running = false;
			if(socket != null){
				socket.close();			
			}
			return true;
		}
		
		protected boolean isStopped(){
			return halted;
		}
		
	}
	
	private class TcpListener implements Callable<Void>{
		
		private boolean running;
		private boolean halted;
		private ServerSocket listeningSock;
		
		TcpListener() {
			running = true;
			halted = false;
		}

		@Override
		public Void call() throws Exception {
			listeningSock = new ServerSocket(props.getTcpListeningSocket());
			listeningSock.setSoTimeout(DhtConstants.TIMEOUT_MILLISECONDS*4);
			while (running) {
				acceptAndListen();
			}
			if(listeningSock != null && !listeningSock.isClosed()){				
				listeningSock.close();
			}
			halted = true;
			log.debug("TCP listener: exiting.");
			return null;
		}
		
		private void acceptAndListen(){
			log.debug("Listening on port "+listeningSock.getLocalPort());
			Socket socket;
			if(listeningSock.isClosed()){
				log.verboseDebug("Listening socket is closed. Impossible to accept. Returning.");
				return;
			}
			try {
				socket = listeningSock.accept();
			} catch (IOException e) {
				if(e instanceof SocketException){
					log.debug("SocketException raised. Probably listeningSocket has been closed.");
					return;
				}
				if( e instanceof SocketTimeoutException){
					log.verboseDebug("Listeing timeout expired. Ending this round of listening.");
					return;
				}
				log.exception(e);
				return;
			}
			spawnListenThread(socket);
		}
		
		private void spawnListenThread(final Socket socket){
			Future<?> spawned = myCore.getThreadPool().submit(new Runnable() {
				@Override
				public void run() {
					try (Socket internalSocket = socket){
						listen(internalSocket);
					} catch (Exception e) {
						log.exception(e);
						for(Throwable t : e.getSuppressed()){
							log.exception(t);
						}
					}
					
				}
			});
			
			try {
				spawned.get(1, TimeUnit.NANOSECONDS);
			} catch (InterruptedException | ExecutionException e) {
				log.exception(e);
			} catch (TimeoutException e) {
				log.debug("Listening thread is running.");
			}
		}
		
		private void listen(Socket socket){
			log.debug("Incoming connection accepted.");
			try {
				socket.setSoTimeout(DhtConstants.TIMEOUT_MILLISECONDS);
			} catch (SocketException e) {
				log.exception(e);
				return;
			}
			
			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				log.exception(e);
				return;
			}
			Object received;
			try {
				received = ois.readObject();
			} catch (ClassNotFoundException e) {
				log.exception(e);
				return;
			} catch (IOException e) {
				log.exception(e);
				return;
			}
			if(!(received instanceof DhtMessage)){
				log.debug("Received message is not a DHT message. Returning.");
				return;
			} else {
				if(!TransactionUtils.isValid(((DhtMessage)received).getSender().getTransaction(), myChainCore)){
					log.verboseDebug("Received message sender did not provide a valid transaction. Aborting.");
					return;
				}
				if(received instanceof StoreMessage){
					log.verboseDebug("Received a StoreMessage.");
					manageStoreMessage(socket, (StoreMessage) received);
				} else if (received instanceof FindValueMessage){
					log.verboseDebug("Received a FindValueMessage.");
					manageFindValueMessage(socket, (FindValueMessage) received);
				}
			}
			try {
				ois.close();
			} catch (IOException e) {
				log.exception(e);
			}
			try {
				socket.close();
			} catch (IOException e) {
				log.exception(e);
			}
			return;
		}
		
		private void manageStoreMessage(Socket sock, StoreMessage message){
			if(message.getMessageType() == MessageType.STORE_REPLY){
				log.debug("A store reply sent to the node listener... aborting.");
				return;
			}
			log.verboseDebug("Got a store request for key: "+message.getKey()+" and "+message.getValue());
			
			boolean result = myCore.getKeyHolder().store(message.getKey(), message.getValue());
			log.verboseDebug("Request is accepted? "+result);
			StoreMessage reply;
			try {
				reply = new StoreMessage(myCore.getNodeExtended(), result);
			} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
				log.exception(e);
				log.verboseDebug("Exception retrieving local node data. Aborting.");
				return;
			}
			sendDhtMessage(sock, reply);
			return;
		}
		
		private void manageFindValueMessage(Socket sock, FindValueMessage message) {
			if(message.getMessageType() == MessageType.FIND_VALUE_EMPTY_REPLY || message.getMessageType() == MessageType.FIND_VALUE_REPLY){
				log.debug("A find value reply sent to the node listener... aborting.");
				return;
			}
			log.verboseDebug("Lookup request received for: "+message.getKey());
			Collection<DhtValue> value = myCore.getKeyHolder().get(message.getKey());
			
			FindValueMessage reply;
			if(value == null){
				log.verboseDebug("No results found.");
				try {
					reply = new FindValueMessage(myCore.getNodeExtended());
				} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
					log.exception(e);
					log.verboseDebug("Exception retrieving local node data. Aborting.");
					return;
				}
			} else {
				log.verboseDebug("Lookup returned some value(s).");
				try {
					reply = new FindValueMessage(myCore.getNodeExtended(), value);
				} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
					log.exception(e);
					log.verboseDebug("Exception retrieving local node data. Aborting.");
					return;
				}
			}
			sendDhtMessage(sock, reply);
			return;
		}
		
		private void sendDhtMessage(Socket sock, DhtMessage message){
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				log.exception(e);
				return;
			}
			try {
				oos.writeObject(message);
				log.verboseDebug("DhtMessage sent.");
			} catch (IOException e) {
				log.exception(e);
			}
			try {
				oos.close();
			} catch (IOException e) {
				log.exception(e);
			}
			return;
		}
		
		protected boolean stop() {
			log.verboseDebug("Stopping TCP listener.");
			running = false;
			if(listeningSock != null){				
				try {
					listeningSock.close();
				} catch (IOException e) {
					log.exception(e);
					return false;
				}
			}
			return true;
		}
		
		protected boolean isStopped() {
			return halted;
		}
		
	}

}
