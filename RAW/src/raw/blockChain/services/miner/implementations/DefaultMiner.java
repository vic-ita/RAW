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
package raw.blockChain.services.miner.implementations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import raw.blockChain.BlockChainCore;
import raw.blockChain.api.Block;
import raw.blockChain.api.BlockChainConstants;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.BlockMiner;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultBlockMiner;
import raw.blockChain.api.implementations.DefaultTransaction;
import raw.blockChain.services.implementations.DefaultBlockChainCore;
import raw.blockChain.services.miner.Miner;
import raw.blockChain.services.miner.messages.MinerMessages;
import raw.blockChain.services.miner.messages.types.MinerAddressNotification;
import raw.blockChain.services.miner.messages.types.SubmitTransactionMessage;
import raw.blockChain.services.thickNode.LocalMinerSeriviceListener;
import raw.blockChain.services.thickNode.messages.ThickNodeMessages;
import raw.blockChain.services.thickNode.messages.types.CommunicationMessage;
import raw.blockChain.services.thickNode.messages.types.LastBlockHeaderNotificationMessage;
import raw.blockChain.services.thickNode.messages.types.SubmitNewBlockMessage;
import raw.blockChain.services.thickNode.messages.types.ThickNodeAddressMessage;
import raw.blockChain.services.thickNode.messages.types.CommunicationMessage.Type;
import raw.logger.Log;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;
import raw.utils.RAWServiceUtils;

import com.google.common.collect.ImmutableList;

/**
 * Default implementation of {@link Miner} interface.
 * 
 * @author vic
 *
 */
public class DefaultMiner implements Miner {
	
	private Log log;
	
	private ArrayList<LocalMinerSeriviceListener> localListeners;
	
	private ArrayList<Transaction> transactionsPool;
	
	private boolean running;
	
	private InetSocketAddress myAddress;

	private ThickNodeFinder myFinder;
	
	private BlockChainCore core;
	
	private BlockMiner myMiner;
	private MinerNullSearchMonitor minerNullTransactionMonitor;
	
	public DefaultMiner() {
		log = Log.getLogger();
		
		localListeners = new ArrayList<LocalMinerSeriviceListener>();
		
		transactionsPool = new ArrayList<Transaction>();
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Void call() throws Exception {
		log.info("Starting miner.");
		running = true;
		
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		InetAddress myIP = null;
		while (myIP == null) {
			try {
				myIP = RAWServiceUtils.resolveIP();
			} catch (Exception e) {
				log.debug("Cant resolve my ip. Waiting 2 seconds and trying again.");
				Thread.sleep(1850);
			}
		}
		myAddress = new InetSocketAddress(myIP, properties.getMinerListeningSocket());
		
		core = DefaultBlockChainCore.getBlockChainCore();
		
		while (!core.isNodeUp()) {
			log.verboseDebug("Node is not up. Waiting a while...");
			Thread.sleep(1000);
		}
		
		notifyExistenceToAllKnownThicks();
		
		myFinder = new ThickNodeFinder();
		Future<?> finderFuture = core.getThreadPool().submit(myFinder);
		try {
			finderFuture.get(1, TimeUnit.NANOSECONDS);
		} catch (TimeoutException e) {
			log.debug("Thick Nodes finder is running on its own.");
		}
		
		
		myMiner = new DefaultBlockMiner(core.getThreadPool(), myAddress.toString());
		startNewBlockSearch();
		
		ServerSocket listeningSocket = null;
		try {
			listeningSocket = new ServerSocket(myAddress.getPort());
		} catch (IOException e) {
			log.exception(e);
		}
		while (running) {
			listendAndAccept(listeningSocket);
		}
		listeningSocket.close();
		return null;
	}
	
	private void listendAndAccept(ServerSocket listeningSocket){
		Socket sock;
		try {
			sock = listeningSocket.accept();
		} catch (IOException e) {
			log.exception(e);
			return;
		}
		spawnSocketRunnable(sock);
	}
	
	private void spawnSocketRunnable(Socket sock){
		final Socket receivedSocket = sock;
		Runnable spawn = new Runnable() {
			@Override
			public void run() {
				try (final Socket internalSocket = receivedSocket){						
					listen(receivedSocket);
				} catch (Exception e) {
					log.exception(e);
					for(Throwable t : e.getSuppressed()){
						log.exception(t);
					}
				}
			}
		};
		
		Future<?> spawened = core.getThreadPool().submit(spawn);
		
		try {
			spawened.get(1, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			log.exception(e);
		} catch (ExecutionException e) {
			log.exception(e);
		} catch (TimeoutException e) {
			log.debug("Ready to manage incoming connection.");
		}
	}
	
	private void listen(Socket sock){
		log.debug("Incoming connection accepted!");
		try {
			sock.setSoTimeout(BlockChainConstants.SOCKETS_MILLISECONDS_TIMEOUT);
		} catch (SocketException e1) {
			log.exception(e1);
			closeIfNotNull(sock);
			return;
		}
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(sock.getInputStream());
		} catch (IOException e) {
			log.exception(e);
		}
		Object obj = null;
		try {
			obj = ois.readObject();
		} catch (ClassNotFoundException e) {
			log.exception(e);
		} catch (IOException e) {
			log.exception(e);
			closeIfNotNull(sock);
			return;
		}
		if(obj instanceof MinerMessages || obj instanceof ThickNodeMessages){
			if(obj instanceof SubmitTransactionMessage){
				SubmitTransactionMessage message = (SubmitTransactionMessage) obj;
				submitTransaction(message.getTransaction());
			} else if (obj instanceof LastBlockHeaderNotificationMessage){
				LastBlockHeaderNotificationMessage message = (LastBlockHeaderNotificationMessage) obj;
				log.verboseDebug("Received a LastBlockNotificationMessage!");
				newChainHeaderNotified(message.getLastBlockHeader());
			} else if (obj instanceof CommunicationMessage){
				CommunicationMessage message = (CommunicationMessage) obj;
				replyToPing(message, sock);
			}
		}
		try {
			sock.close();
		} catch (IOException e) {
			log.exception(e);
		}
	}
	
	private void startNewBlockSearch(){
		BlockHeader lastHeader = core.getLastBlockHeaderInChain();
		log.verboseDebug("Last header from core: "+lastHeader);
		if(lastHeader != null){				
			ArrayList<Transaction> usedTransactions = new ArrayList<Transaction>(transactionsPool);
			boolean runningOnNullTransaction;
			if(usedTransactions.size()!= 0){
				for(Transaction transaction : usedTransactions){
					myMiner.pushTransaction(transaction);
				}
				runningOnNullTransaction = false;
			} else {
				myMiner.pushTransaction(DefaultTransaction.getNullTransaction());
				runningOnNullTransaction = true;
			}
			log.verboseDebug("Transactions list set up.");
			
			myMiner.registerListener(this);
			myMiner.findNextBlock(lastHeader);
			log.verboseDebug("Next block search started.");
			
			if(runningOnNullTransaction){
				minerNullTransactionMonitor = new MinerNullSearchMonitor(lastHeader);
				Future<?> monitorFuture = core.getThreadPool().submit(minerNullTransactionMonitor);
				try {
					monitorFuture.get(1, TimeUnit.NANOSECONDS);
				} catch (InterruptedException e) {
					log.exception(e);
				} catch (ExecutionException e) {
					log.exception(e);
				} catch (TimeoutException e) {
					log.verboseDebug("Monitor set up for miner working on null transaction.");
				}
			}
		} else {
			// the node running on this machine is not able to resovle a block header.
			// so... should we wait for a bit?
			int sleepTime = 5000;
			log.verboseDebug("Waiting "+sleepTime+" ms for a header to become available.");
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				log.exception(e);
			}
			startNewBlockSearch();
		}
	}
	
	private class MinerNullSearchMonitor implements Runnable{
		
		private boolean running;
		private BlockHeader headerMonitoring;
		
		public MinerNullSearchMonitor(BlockHeader headerMonitored) {
			running = true;
			headerMonitoring = headerMonitored;
		}

		@Override
		public void run() {
			boolean haltedPreviousTaskOnNullTransactions = false;
			while (running) {
				if(transactionsPool.size()>0){
					 if(myMiner != null && myMiner.isMining() && myMiner.prevHeaderMining().equals(headerMonitoring)){
//						 stopBlockSearch();
						 myMiner.haltBlockMining();
						 haltedPreviousTaskOnNullTransactions = true;
						 running = false;
					 }
				}
				try {
					Thread.sleep(60*1000);
				} catch (InterruptedException e) {
					log.exception(e);
				}
			}
			if(haltedPreviousTaskOnNullTransactions){
				startNewBlockSearch();
			}
		}
		
		public boolean isRunning(){
			return running;
		}
		
		public void stop(){
			running = false;
		}
	}
	
	private void notifyExistenceToAllKnownThicks(){
		MinerAddressNotification notification = new MinerAddressNotification(myAddress);
		for(InetSocketAddress node : core.getThickNodesList()){
			Socket sock = null;
			try {
				sock = new Socket(node.getAddress(), node.getPort());
			} catch (IOException e) {
				log.verboseDebug("Cannot open connection to "+node+".");
				return;
			}
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(sock);
				return;
			}
			try {
				oos.writeObject(notification);
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(sock);
				return;
			}
			try {
				sock.close();
			} catch (IOException e) {
				log.exception(e);
			}
		}
	}
	
	private void closeIfNotNull(Socket sock) {
		if(sock != null){
			try {
				sock.close();
			} catch (IOException e) {
				log.exception(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.miner.Miner#stopService()
	 */
	@Override
	public boolean stopService() {
		running = false;
		if(myFinder != null){			
			myFinder.stop();
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.miner.Miner#registerListener(raw.blockChain.services.thickNode.LocalMinerSeriviceListener)
	 */
	@Override
	public boolean registerListener(LocalMinerSeriviceListener listener) {
		if(!localListeners.contains(listener)){
			localListeners.add(listener);
			log.verboseDebug("New LocalMinerServiceListener registered.");
			return true;
		} else {
			log.verboseDebug("LocalMinerServiceListener is already in the list.");
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.miner.Miner#submitTransaction(raw.blockChain.api.Transaction)
	 */
	@Override
	public void submitTransaction(Transaction transaction) {
		if(!transactionsPool.contains(transaction)){
			transactionsPool.add(transaction);
			log.verboseDebug("Added to my pool transaction: "+transaction);
		}
	}
	
	/* (non-Javadoc)
	 * @see raw.blockChain.services.miner.LocalThickNodeListener#notifyNewBlockHeaderFromNet(raw.blockChain.api.BlockHeader)
	 */
	@Override
	public boolean notifyNewBlockHeaderFromNet(BlockHeader header) {
		newChainHeaderNotified(header);
		return true;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.BlockMinerListener#notifyNewBlock(raw.blockChain.api.Block)
	 */
	@SuppressWarnings("resource")
	@Override
	public synchronized void notifyNewBlock(Block newBlock) {
		List<LocalMinerSeriviceListener> listeners = ImmutableList.copyOf(localListeners);
		for(LocalMinerSeriviceListener listener : listeners){
			listener.notifyNewLocalBlock(newBlock);
		}
		List<InetSocketAddress> remoteNodes = ImmutableList.copyOf(core.getThickNodesList());

		boolean isBlockAccepted = false;
		
		for(InetSocketAddress remoteNode : remoteNodes){
			Socket sock = null;
			try {
				sock = new Socket(remoteNode.getAddress(), remoteNode.getPort());
			} catch (IOException e4) {
				log.verboseDebug("Troubles opening a socket to "+remoteNode+". Aborting.");
				closeIfNotNull(sock);
				continue;
			}
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e3) {
				log.verboseDebug("Troubles opening an OtputStream to "+remoteNode+". Aborting.");
				closeIfNotNull(sock);
				continue;
			}
			try {
				oos.writeObject(new SubmitNewBlockMessage(newBlock, myAddress));
			} catch (IOException e2) {
				log.verboseDebug("Troubles sending a SubmitNewBlockMessage to "+remoteNode+". Aborting.");
				closeIfNotNull(sock);
				continue;
			} catch (ConcurrentModificationException e){
				log.exception(e);
				closeIfNotNull(sock);
				continue;
			}
			try {
				sock.setSoTimeout(BlockChainConstants.SOCKETS_MILLISECONDS_TIMEOUT);
			} catch (SocketException e1) {
				log.exception(e1);
				closeIfNotNull(sock);
				continue;
			}
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(sock.getInputStream());
			} catch (IOException e1) {
				log.verboseDebug("Troubles opening an InputStream to "+remoteNode+". Aborting.");
				closeIfNotNull(sock);
				continue;
			}
			Object obj = null;
			try {
				obj = ois.readObject();
			} catch (ClassNotFoundException e) {
				log.exception(e);
				closeIfNotNull(sock);
				continue;
			} catch (IOException e) {
				log.verboseDebug("Troubles reading messages from "+remoteNode+" InputStream. Aborting.");
				closeIfNotNull(sock);
				continue;
			}
			if(obj instanceof CommunicationMessage){
				CommunicationMessage reply = (CommunicationMessage) obj;
				if(reply.getMessage() == CommunicationMessage.Type.ACCEPTED){
					isBlockAccepted = true;
					log.debug("YAY! A remote thick node accepted the new message"+newBlock.getHeader().toString());
				} else if (reply.getMessage() == CommunicationMessage.Type.REFUSED){
					isBlockAccepted = false;
					log.debug("BOO! A remote thick node refused the new message"+newBlock.getHeader().toString());
				}
			}

		}
		if(isBlockAccepted){			
			transactionsPool.removeAll(newBlock.getTransactions());
		}
		if(myMiner != null && myMiner.isMining()){
			if(myMiner.prevHeaderMining().getBlockNumber() <= newBlock.getHeader().getBlockNumber()){
				myMiner.haltBlockMining();
			}
		}
		startNewBlockSearch();
	}

	private void newChainHeaderNotified(BlockHeader header){
		if(myMiner != null && myMiner.isMining()){
			if(header.getBlockNumber() >= myMiner.prevHeaderMining().getBlockNumber()+1){
				log.verboseDebug("Stopping my miner.");
				if(minerNullTransactionMonitor != null){
					minerNullTransactionMonitor.stop();
					while(minerNullTransactionMonitor.isRunning()){
						log.verboseDebug("Waiting for null monitor to be halted.");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							log.exception(e);
						}
					}
					minerNullTransactionMonitor = null;
				}
				myMiner.haltBlockMining();
				log.verboseDebug("Miner halt requested");
				startNewBlockSearch();
			}
		}
	}
	
	private void replyToPing(CommunicationMessage message, Socket sock){
		if((message.getMessage() == CommunicationMessage.Type.PING_FROM_THICK) ||(message.getMessage() == CommunicationMessage.Type.PING_FROM_THIN)){
			CommunicationMessage reply = new CommunicationMessage(CommunicationMessage.Type.PONG);
			reply.attachInetSocketAddress(myAddress);
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				log.debug("Cannot open a stream to send PONG message. Aborting reply.");
				return;
			}
			try {
				oos.writeObject(reply);
			} catch (IOException e) {
				log.debug("Cannot sent PONG to pinging node. Aborting reply.");
				return;
			}			
			log.verboseDebug("Pong sent to "+(InetSocketAddress)message.getAttachment());
		}
	}
	
	private class ThickNodeFinder implements Runnable{

		boolean running;
		
		@Override
		public void run() {
			log.verboseDebug("ThickNode finder has started");
			running = true;
			Random rand = new Random(System.currentTimeMillis());
			while (running) {
				ThickNodeAddressMessage request = new ThickNodeAddressMessage();
				ArrayList<InetSocketAddress> thickNodesAddress = core.getThickNodesList();
				if(thickNodesAddress != null && thickNodesAddress.size() > 0){
					InetSocketAddress nodeToAsk = thickNodesAddress .get(rand.nextInt(thickNodesAddress.size()));
					boolean communicationWithNodeWentGood = true;
					boolean foundNewThickAddress = false;
					InetSocketAddress newAddress = null;
					Socket sock = null;
					try {
						sock = new Socket(nodeToAsk.getAddress(), nodeToAsk.getPort());
					} catch (IOException e1) {
						communicationWithNodeWentGood = false;
						log.verboseDebug("Cannot establish connection to "+nodeToAsk+".");
					}
					if(sock != null){		
						ObjectOutputStream oos = null;
						try {
							oos = new ObjectOutputStream(sock.getOutputStream());
						} catch (IOException e1) {
							communicationWithNodeWentGood = false;
							log.exception(e1);
						} 
						if( oos != null){
							try {
								oos.writeObject(request);
							} catch (IOException e1) {
								communicationWithNodeWentGood = false;
								log.exception(e1);
							}
							try {
								sock.setSoTimeout(BlockChainConstants.SOCKETS_MILLISECONDS_TIMEOUT);
							} catch (SocketException e1) {
								log.exception(e1);
							}
							ObjectInputStream ois = null;
							try {
								ois = new ObjectInputStream(sock.getInputStream());
							} catch (IOException e1) {
								communicationWithNodeWentGood = false;
								log.verboseDebug("Troubles opening input stream. Skipping.");
							}
							if( ois != null){								
								Object obj = null;
								try {
									obj = ois.readObject();
								} catch (ClassNotFoundException e1) {
									communicationWithNodeWentGood = false;
									log.exception(e1);
								} catch (IOException e1) {
									communicationWithNodeWentGood = false;
									log.verboseDebug("Troubles reading message from socket. Skipping.");
								}
								if((obj != null) && (obj instanceof ThickNodeAddressMessage)){
									ThickNodeAddressMessage reply = (ThickNodeAddressMessage) obj;
									if(!reply.isRequest()){
										InetSocketAddress obtained = reply.getAddress();
										log.verboseDebug("Recevied from a thick node this address: "+obtained);
										if(!thickNodesAddress.contains(obtained)){
											boolean addressOfAListener = false;
											for(LocalMinerSeriviceListener listener : localListeners){
												if(obtained.equals(listener.getListenerAddress())){
													addressOfAListener = true;
													break;
												}
											}
											if(!addressOfAListener){									
												thickNodesAddress.add(obtained);
												foundNewThickAddress = true;
												newAddress = obtained;
											}
										}
									}
								}
							}
						}
						try {
							sock.close();
						} catch (IOException e1) {
							log.exception(e1);
						}
					}
					if(!communicationWithNodeWentGood){
						thickNodesAddress.remove(nodeToAsk); //XXX maybe is a little drastic... but for now just try this way. 
					} else {
						if(foundNewThickAddress && newAddress != null){ //say hello to this new node.
							if(sendAPing(newAddress)){
								log.verboseDebug(newAddress+" seems to be up and running");
							} else {
								log.verboseDebug("There was problems pinging "+newAddress);
							}
						}
					}
				}
				int sleepTime = NODE_SEARCH_MILLISECONDS_INTERTIME+ (rand.nextInt(NODE_SEARCH_MILLISECONDS_VARIABILITY*2) - NODE_SEARCH_MILLISECONDS_VARIABILITY);
				log.debug("Node Finder will sleep for "+sleepTime+" ms.");
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					log.exception(e);
				}
			}			
		}
		
		public boolean stop(){
			running = false;
			return true;
		}
		
		private boolean sendAPing(InetSocketAddress address){
			InetSocketAddress toBePinged = address;
			
			CommunicationMessage ping = new CommunicationMessage(Type.PING_FROM_MINER);
			ping.attachInetSocketAddress(myAddress);

			try (Socket sock = new Socket(toBePinged.getAddress(), toBePinged.getPort())){
				try {
					ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
					oos.writeObject(ping);						
				} catch (IOException e) {
					closeIfNotNull(sock);
					return false;
				}

				//wait for a pong.
				CommunicationMessage reply = null;
				try {
					sock.setSoTimeout(BlockChainConstants.SOCKETS_MILLISECONDS_TIMEOUT);
				} catch (SocketException e1) {
					log.exception(e1);
				}
				try {
					ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
					try {
						reply = (CommunicationMessage) ois.readObject();
					} catch (ClassNotFoundException e) {
						log.exception(e);
					}							
				} catch (IOException e) {
					closeIfNotNull(sock);
					return false;
				}
				if(reply == null || reply.getMessage() != Type.PONG){
					closeIfNotNull(sock);
					return false;
				} else {
					InetSocketAddress otherNodeAddress = (InetSocketAddress) reply.getAttachment();
					if(!otherNodeAddress.equals(toBePinged)){
						closeIfNotNull(sock);
						return false;
					}
				}

				try {
					sock.close();
				} catch (IOException e) {
					log.exception(e);
				}
				return true;
			} catch (ConnectException e) {
				log.verboseDebug("Connection refused by "+address+". Aborting.");
				return false;
			} catch (Exception e) {
				log.exception(e);
				for(Throwable t : e.getSuppressed()){
					log.exception(t);
				}
				return false;
			}
		}
		
	}

}
