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
package raw.blockChain.services.thickNode.implementations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.RandomUtils;

import raw.blockChain.api.Block;
import raw.blockChain.api.BlockChainConstants;
import raw.blockChain.api.BlockCompactRepresentation;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.utils.BlockUtils;
import raw.blockChain.services.dbHelper.BlocksToDataBase;
import raw.blockChain.services.dbHelper.implementations.DefaultBlockToDataBase;
import raw.blockChain.services.implementations.DefaultBlockChainCore;
import raw.blockChain.services.miner.LocalThickNodeListener;
import raw.blockChain.services.miner.messages.MinerMessages;
import raw.blockChain.services.miner.messages.types.MinerAddressNotification;
import raw.blockChain.services.miner.messages.types.SubmitTransactionMessage;
import raw.blockChain.services.thickNode.ThickNode;
import raw.blockChain.services.thickNode.messages.ThickNodeMessages;
import raw.blockChain.services.thickNode.messages.types.BlockCompactRepresentationRequestMessage;
import raw.blockChain.services.thickNode.messages.types.BlockRequestMessage;
import raw.blockChain.services.thickNode.messages.types.CommunicationMessage;
import raw.blockChain.services.thickNode.messages.types.LastBlockHeaderNotificationMessage;
import raw.blockChain.services.thickNode.messages.types.LastBlockNotificationMessage;
import raw.blockChain.services.thickNode.messages.types.MinerNodeAddressMessage;
import raw.blockChain.services.thickNode.messages.types.NodeAddressMessage;
import raw.blockChain.services.thickNode.messages.types.SubmitNewBlockMessage;
import raw.blockChain.services.thickNode.messages.types.ThickNodeAddressMessage;
import raw.blockChain.services.thickNode.messages.types.TransactionBlockNumberMessage;
import raw.blockChain.services.thickNode.messages.types.UpdatingChainBlockRequestMessage;
import raw.blockChain.services.thickNode.messages.types.CommunicationMessage.Type;
import raw.blockChain.services.utils.ThickNodeAddressBookFile;
import raw.logger.Log;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;
import raw.utils.RAWServiceUtils;

import com.google.common.collect.ImmutableList;

/**
 * Default implementation of {@link ThickNode}.
 * 
 * @author vic
 *
 */
public class DefaultThickNode implements ThickNode {
	
	private Log log;
	
	private BlocksToDataBase database;
		
	private InetSocketAddress mySocketAddress;
	private ServerSocket listeningSocket;
	
	private NodeListener myListener;
	private NodePinger myPinger;
	
	private List<InetSocketAddress> otherThickNodes;
	private List<InetSocketAddress> maybeOffline;
	
	private List<InetSocketAddress> minerNodes;
	
	private List<LocalThickNodeListener> registeredListeners;
	
	private ExecutorService pool;
	
	private Queue<Transaction> forwardedTransactions;
	
	private boolean initialized;
	
	private boolean updatingChain;
	
	private BlockingQueue<Boolean> sanityCheckQueue;

	public DefaultThickNode() {
		log = Log.getLogger();
		
		database = new DefaultBlockToDataBase(this);
		
		InetAddress myIP = null;
		while (myIP == null) {
			try {
				myIP = RAWServiceUtils.resolveIP();
			} catch (Exception e) {
				log.debug("Cant resolve my ip. Waiting 2 seconds and trying again.");
				try {
					Thread.sleep(1850);
				} catch (InterruptedException e1) {
					log.exception(e1);
				}
			}
		}
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		try {
			listeningSocket = new ServerSocket(properties.getListeningSocket());
		} catch (IOException e) {
			log.exception(e);
		}
		
		mySocketAddress = new InetSocketAddress(myIP, properties.getListeningSocket());
		
		try {
			listeningSocket.setSoTimeout(BlockChainConstants.SOCKETS_MILLISECONDS_TIMEOUT * 10); // thus if listener is hanging for too long... it just restart accepting.
		} catch (SocketException e) {
			log.exception(e);
		}
		
		otherThickNodes = Collections.synchronizedList(new ArrayList<InetSocketAddress>());
		maybeOffline = Collections.synchronizedList(new ArrayList<InetSocketAddress>());
		minerNodes = Collections.synchronizedList(new ArrayList<InetSocketAddress>());

		registeredListeners = Collections.synchronizedList(new ArrayList<LocalThickNodeListener>());

		initialized = false;
		
		forwardedTransactions = new LinkedList<>();
		
		updatingChain = false;
		
		sanityCheckQueue = new ArrayBlockingQueue<>(1);
		sanityCheckQueue.add(Boolean.TRUE); //pre-loading the blocking queue for the first sanity check
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Void call() throws Exception {
		log.info("Thick Node is starting!");
		
		database.open();
		
		loadOtherNodesFromFile();
		
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		
		boolean chainJustBootstrapped = checkAndBootstrapChain();
		
		if(properties.isShortCheckOnStartup() && !chainJustBootstrapped){
			log.debug("Beginning PARTIAL sanity check.");
			BlockHeader lastHeader = database.getLastBlockHeaderInChain(false);
			if(lastHeader != null){
				long maxStartingPoint = lastHeader.getBlockNumber() - properties.getBlocksCheckedOnShortCheck(); 
				long startingPoint = 0;
				if(maxStartingPoint > 0){
					startingPoint = RandomUtils.nextLong(0, maxStartingPoint+1);
				}
				BlockHeader startingHeader = database.getBlockHeaderByNumber(startingPoint);
				sanityCheckChain(startingHeader, true, properties.getBlocksCheckedOnShortCheck());
			}
			
		} else {			
			sanityCheckEntireChain();
		}
		
		pool = DefaultBlockChainCore.getBlockChainCore().getThreadPool();
		
		myPinger = new NodePinger();
		Future<Void> pingerFuture = pool.submit(myPinger);
		try {
			pingerFuture.get(1, TimeUnit.NANOSECONDS);
			
		} catch (TimeoutException e) {
			log.debug("Pinger is correctly running on its own.");
		}
		
		initialized = true;
		
		myListener = new NodeListener();
		Future<Void> listenerFuture = pool.submit(myListener);
		listenerFuture.get();

		return null;
	}
	
	private boolean checkAndBootstrapChain(){
		boolean chainJustBootstrapped = false;
		if(database.getLastBlockHeaderInChain(false) == null){
			log.verboseDebug("No blocks found. Maybe this node need to boostrap its chain copy.");
			if(otherThickNodes.size() > 0){
				int attempt = 0;
				Random rand = new Random(System.currentTimeMillis());
				while ((attempt < 3) && (database.getLastBlockHeaderInChain(false) == null)) {
					InetSocketAddress nodeToAskUpdate = otherThickNodes.get(rand.nextInt(otherThickNodes.size()));
					log.verboseDebug("Chain prefetch will be asked to: "+nodeToAskUpdate);
					Block lastBlockFromNet = null;
					Socket sock;
					try {
						sock = new Socket(nodeToAskUpdate.getAddress(), nodeToAskUpdate.getPort());
					} catch (IOException e) {
						log.exception(e);
						attempt++;
						continue;
					}
					log.verboseDebug("Socket opened.");
					try {
						sock.setSoTimeout(BlockChainConstants.SOCKETS_MILLISECONDS_TIMEOUT);
					} catch (SocketException e) {
						log.exception(e);
						try {
							sock.close();
						} catch (IOException e1) {
							log.exception(e1);
						}
						attempt++;
						continue;
					}
					ObjectOutputStream oos;
					try {
						oos = new ObjectOutputStream(sock.getOutputStream());
					} catch (IOException e) {
						log.exception(e);
						try {
							sock.close();
						} catch (IOException e1) {
							log.exception(e1);
						}
						attempt++;
						continue;
					}
					log.verboseDebug("Output stream opened.");
					BlockRequestMessage request = new BlockRequestMessage();
					try {
						oos.writeObject(request);
					} catch (IOException e) {
						log.exception(e);
						try {
							sock.close();
						} catch (IOException e1) {
							log.exception(e1);
						}
						attempt++;
						continue;
					}
					log.verboseDebug("Request sent ("+request+")");
					ObjectInputStream ois;
					try {
						ois = new ObjectInputStream(sock.getInputStream());
					} catch (IOException e) {
						log.exception(e);
						try {
							sock.close();
						} catch (IOException e1) {
							log.exception(e1);
						}
						attempt++;
						continue;
					}
					log.verboseDebug("Input stream opened.");
					Object received;
					try {
						received = ois.readObject();
					} catch (ClassNotFoundException | IOException e) {
						log.exception(e);
						try {
							sock.close();
						} catch (IOException e1) {
							log.exception(e1);
						}
						attempt++;
						continue;
					}
					log.verboseDebug("Received object on prefetch socket.");
					try {
						sock.close();
					} catch (IOException e) {
						log.exception(e);
					}
					if(received instanceof BlockRequestMessage){
						BlockRequestMessage reply = (BlockRequestMessage) received;
						if(reply.isPositiveReply()){
							lastBlockFromNet = reply.getBlock();
							log.verboseDebug("Prefetch first request returned block: "+lastBlockFromNet.getHeader());
						}
					}						
					if(lastBlockFromNet != null){
						try{
							updateMyChain(lastBlockFromNet, null, nodeToAskUpdate.getAddress(), nodeToAskUpdate.getPort());							
						} catch (Exception e) {
							log.info("An exception has been raised while bootstrapping the "
									+ "chain. Aborting process, if possible another attemt will be done.");
						}
					}
					attempt++;
				}
				if(database.getLastBlockHeaderInChain(false) == null){
					chainJustBootstrapped = true;
				}
				log.verboseDebug("Block chain bootstrap pre-fetching procedure finished.");
			}
		}
		return chainJustBootstrapped;
	}
	
	private void sanityCheckEntireChain(){
		log.verboseDebug("Starting ENTIRE block chain sanity check.");
		Block current = database.getBlockByNumber(0);
		BlockHeader lastHeader = database.getLastBlockHeaderInChain(false);
		if(current == null){
			if(lastHeader == null){
				//still no blocks. Just exit.
				log.verboseDebug("Chain copy is empty. Aborting sanity check.");
				return;				
			} else {
				log.verboseDebug("Need block ZERO. Asking for it.");
				InetSocketAddress nodeToAsk = null;
				Random rand = new Random(System.currentTimeMillis());
				int otherThickNodesSize = 0;
				synchronized (otherThickNodes) {
					otherThickNodesSize = otherThickNodes.size();
				}
				if(otherThickNodesSize > 0){
					synchronized (otherThickNodes) {						
						nodeToAsk = otherThickNodes.get(rand.nextInt(otherThickNodes.size()));
					}
				} else {
					int maybeOfflineSize;
					synchronized (maybeOffline) {						
						maybeOfflineSize = maybeOffline.size();
					}
					if(maybeOfflineSize > 0){
						synchronized (maybeOffline) {							
							nodeToAsk = maybeOffline.get(rand.nextInt(maybeOffline.size()));
						}
					}
				}
				if(nodeToAsk == null){
					//it is impossible to retrieve a node to correct the chain error.
					log.verboseDebug("Not any node to ask to. Cutting all chain.");
					cutChainFromBlockByNumber(0);
				}
				BlockRequestMessage request = new BlockRequestMessage(0);
				Block retrieved = sendBlockRequest(nodeToAsk, request);
				if(retrieved == null){
					log.verboseDebug("Block received from the net is null. Cutting all chain.");
					cutChainFromBlockByNumber(0);
				}
				boolean valid;
				try {
					valid = BlockUtils.validateConsecutiveBlocks(retrieved, null, false, 0);				
				} catch (Exception e) {
					valid = false;
				}
				if(valid){
					log.verboseDebug("Retrieved block is valid. Fix it.");
					updateBlock(0, retrieved);
					current = database.getBlockByNumber(0);
				} else {
					log.verboseDebug("Retrieved block is INVALID. Cutting all chain.");
					cutChainFromBlockByNumber(0);
				}
			}
		}
		sanityCheckChain(current.getHeader(), false, 0);
	}
	
	private void sanityCheckChain(BlockHeader startingPoing, boolean limitCheck, int blocksToCheck){
		if(startingPoing == null){
			log.verboseDebug("Starting poin is null. Sanity check will not be performed.");
			return;
		}
		log.verboseDebug("Cheching sanity check queue clearance.");
		try {
			sanityCheckQueue.take();
		} catch (InterruptedException e) {
			log.exception(e);
		}
		log.verboseDebug("Starting block sanity check.");
		int blocksToGo = blocksToCheck;
		Block current;
		try {
			current = database.getBlockFromHash(startingPoing.hash());
		} catch (SQLException e1) {
			log.exception(e1);
			log.verboseDebug("Aborting check.");
			return;
		}
		BlockHeader lastHeader = database.getLastBlockHeaderInChain(false);
		
		Block last = null;
		try {
			last = database.getBlockFromHash(lastHeader.hash());
		} catch (SQLException e) {
			log.exception(e);
		}
		if(last == null){
			log.warning("Strangely the last block is null. Aborting sanity check.");
			return;
		}
//		Block previous = current; //trick to get all blocks checked in the loop.
		Block previous; //trick to get all blocks checked in the loop.
		if(startingPoing.getBlockNumber() == 0){
			previous = current; //trick to get all blocks checked in the loop.
		} else {
			previous = database.getBlockByNumber(startingPoing.getBlockNumber() - 1);
		}
		while (!previous.equals(last) && santityCheckConditions(limitCheck, blocksToGo)) {
			if(current == null){
				log.verboseDebug("Looks like we have a \"hole\" in this copy of the chain.");
				InetSocketAddress nodeToAsk = null;
				Random rand = new Random(System.currentTimeMillis());
				int otherThickNodesSize = 0;
				synchronized (otherThickNodes) {
					otherThickNodesSize = otherThickNodes.size();
				}
				if(otherThickNodesSize > 0){
					synchronized (otherThickNodes) {						
						nodeToAsk = otherThickNodes.get(rand.nextInt(otherThickNodes.size()));
					}
				} else {
					int maybeOfflineSize;
					synchronized (maybeOffline) {						
						maybeOfflineSize = maybeOffline.size();
					}
					if(maybeOfflineSize > 0){
						synchronized (maybeOffline) {
							nodeToAsk = maybeOffline.get(rand.nextInt(maybeOffline.size()));							
						}
					}
				}
				if(nodeToAsk == null){
					//it is impossible to retrieve a node to correct the chain error.
					log.verboseDebug("Not any node to ask to. Cutting.");
					cutChainFromBlock(previous);
					break;
				}
				BlockRequestMessage request = new BlockRequestMessage(previous.getHeader().getBlockNumber()+1);
				Block retrieved = sendBlockRequest(nodeToAsk, request);
				if(retrieved == null){
					log.verboseDebug("Block received from the net is null. Cutting.");
					cutChainFromBlock(previous);
					break;
				}
				boolean valid;
				try {
					valid = BlockUtils.validateConsecutiveBlocks(retrieved, previous, false, 0);				
				} catch (Exception e) {
					valid = false;
				}
				if(valid){
					log.verboseDebug("Retrieved block is valid. Fix it.");
					updateBlock(previous.getHeader().getBlockNumber()+1, retrieved);
					current = database.getBlockByNumber(previous.getHeader().getBlockNumber()+1);
				} else {
					log.verboseDebug("Retrieved block is INVALID. Cutting.");
					cutChainFromBlock(previous);
				}
			}
			log.verboseDebug("Checking block "+current.getHeader().getBlockNumber()+"/"+last.getHeader().getBlockNumber());
			boolean valid;
			try {
				valid = BlockUtils.validateConsecutiveBlocks(current, previous, false, 0);				
			} catch (Exception e) {
				valid = false;
			}
			if(!valid){
				log.verboseDebug("This block failed its validation: "+current.getHeader());
				BlockUtils.validateConsecutiveBlocks(current, previous, false, 0);				
				InetSocketAddress nodeToAsk = null;
				Random rand = new Random(System.currentTimeMillis());
				int otherThickNodesSize = 0;
				synchronized (otherThickNodes) {
					otherThickNodesSize = otherThickNodes.size();
				}
				if(otherThickNodesSize > 0){
					synchronized (otherThickNodes) {						
						nodeToAsk = otherThickNodes.get(rand.nextInt(otherThickNodes.size()));
					}
				} else {
					int maybeOfflineSize;
					synchronized (maybeOffline) {						
						maybeOfflineSize = maybeOffline.size();
					}
					if(maybeOfflineSize > 0){
						synchronized (maybeOffline) {							
							nodeToAsk = maybeOffline.get(rand.nextInt(maybeOffline.size()));
						}
					}
				}
				if(nodeToAsk == null){
					//it is impossible to retrieve a node to correct the chain error.
					log.verboseDebug("Not any node to ask to. Cutting.");
					cutChainFromBlock(current);
					break;
				}
				BlockRequestMessage request = new BlockRequestMessage(current.getHeader().getBlockNumber());
				Block retrieved = sendBlockRequest(nodeToAsk, request);
				if(retrieved == null){
					log.verboseDebug("Block received from the net is null. Cutting.");
					cutChainFromBlock(current);
					break;
				}
				try {
					valid = BlockUtils.validateConsecutiveBlocks(retrieved, previous, false, 0);				
				} catch (Exception e) {
					valid = false;
				}
				if(valid){
					log.verboseDebug("Retrieved block is valid. Fix it.");
					updateBlock(current.getHeader().getBlockNumber(), retrieved);
					current = database.getBlockByNumber(current.getHeader().getBlockNumber());
				} else {
					log.verboseDebug("Retrieved block is INVALID. Cutting.");
					cutChainFromBlock(current);
				}
			}
			previous = current;
			current = database.getBlockByNumber(current.getHeader().getBlockNumber()+1);
			blocksToGo = blocksToGo - 1;
		}
		log.verboseDebug("Clearing queue for other checks.");
		try {				
			sanityCheckQueue.add(Boolean.TRUE);
		} catch (IllegalStateException e) {
			log.verboseDebug("Queue is already full. This is odd, but we can cope with it.");
		}
		log.verboseDebug("Sanity check ended.");
	}
	
	private void sanityCheckLastBlocks(int numberOfBlocks){
		log.verboseDebug("Checking last +"+numberOfBlocks+" blocks of the chain");
		BlockHeader last = database.getLastBlockHeaderInChain(false);
		if(last == null){
			log.verboseDebug("Last block not found. Aborting");
			return;
		}
		long startFrom = last.getBlockNumber() - numberOfBlocks;
		if(startFrom < 0){
			startFrom = 0;
		}
		BlockHeader startBlock = database.getBlockHeaderByNumber(startFrom);
		sanityCheckChain(startBlock, true, numberOfBlocks+1);
	}
	
	private boolean santityCheckConditions(boolean limitCheck, int blocksToCheck){
		if(!limitCheck){
			return true;
		}
		if(blocksToCheck > 0){
			return true;
		}
		return false;
	}
	
	private void updateBlock(long blockNumber, Block updatedBlock){
		log.verboseDebug("Updating block.");
		database.deleteBlockByNumber(blockNumber);
		log.verboseDebug("Deleted block #"+blockNumber);
		if(insertPreValidatedBlock(updatedBlock)){
			log.verboseDebug("Exchanged with "+updatedBlock.getHeader());
		}
	}
	
	private void cutChainFromBlock(Block block){
		ArrayList<Block> deleteList = new ArrayList<Block>();
		deleteList.add(block);
		Block cursor = getLastBlockInChain();
		long index = cursor.getHeader().getBlockNumber();
		while (!block.equals(cursor)) {
			deleteList.add(cursor);
			log.verboseDebug("Added to delete list "+cursor.getHeader());
			index = index-1;
			cursor = database.getBlockByNumber(index);
			while (cursor == null) {
				index = index-1;
				cursor = database.getBlockByNumber(index);
			}
		}
		Collections.sort(deleteList, new BlockNumberComparator());
		boolean deleted = database.deleteBlocksBulk(deleteList);
		if(deleted){
			log.verboseDebug("Delete list deleted from my chain copy.");			
		} else {
			log.warning("Chain was not (at least completely) cut.");
		}
	}
	
	private void cutChainFromBlockByNumber(long blockNumber){
		ArrayList<Block> deleteList = new ArrayList<Block>();
		Block cursor = getLastBlockInChain();
		long index = cursor.getHeader().getBlockNumber();
		while (index >= blockNumber) {
			if(cursor != null){				
				deleteList.add(cursor);
				log.verboseDebug("Added to delete list "+cursor.getHeader());
			}
			index = index-1;
			cursor = database.getBlockByNumber(index);
		}
		Collections.sort(deleteList, new BlockNumberComparator());
		boolean deleted = database.deleteBlocksBulk(deleteList);
		if(deleted){
			log.verboseDebug("Delete list deleted from my chain copy.");			
		} else {
			log.warning("Chain was not (at least completely) cut.");
		}
	}
	
	private Block sendBlockRequest(InetSocketAddress nodeToAsk, BlockRequestMessage request){
		log.verboseDebug("Sending Block request to "+nodeToAsk+".");
		try (Socket sock = new Socket(nodeToAsk.getAddress(), nodeToAsk.getPort())) {
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(sock);
				return null;
			}
			try {
				oos.writeObject(request);
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(sock);
				return null;
			}
			try {
				sock.setSoTimeout(BlockChainConstants.SOCKETS_MILLISECONDS_TIMEOUT);
			} catch (SocketException e1) {
				log.exception(e1);
				closeIfNotNull(sock);
				return null;
			}
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(sock.getInputStream());
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(sock);
				return null;
			}
			Object obj = null;
			try {
				obj = ois.readObject();
			} catch (ClassNotFoundException e) {
				log.exception(e);
				closeIfNotNull(sock);
				return null;
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(sock);
				return null;
			}
			try {
				sock.close();
			} catch (IOException e) {
				log.exception(e);
			}
			Block block = null;
			if(obj instanceof BlockRequestMessage){
				BlockRequestMessage reply = (BlockRequestMessage) obj;
				if(reply.isPositiveReply()){
					block = reply.getBlock();
				}
			}
			return block;
		} catch (Exception e) {
			log.exception(e);
			for(Throwable t : e.getSuppressed()){
				log.exception(t);
			}
			return null;
		}
	}
	
	private void loadOtherNodesFromFile(){
		ThickNodeAddressBookFile addressBook = new ThickNodeAddressBookFile();
		otherThickNodes = Collections.synchronizedList(addressBook.getAddressList());
		log.debug("Other nodes list loaded from address book.");
	}
	
	private void saveOtherNodesToFile(){
		ThickNodeAddressBookFile addressBook;
		synchronized (otherThickNodes) {
			addressBook = new ThickNodeAddressBookFile(otherThickNodes);			
		}
		addressBook.writeToFile();
		log.debug("Other nodes list written down in address book.");
	}
	
	private class NodeListener implements Callable<Void>{
		
		private boolean listenerRunning;
		private boolean isStopped;
		
		public NodeListener() {
			listenerRunning = true;
			isStopped = false;
		}

		@Override
		public Void call() throws Exception {
			log.debug("NodeListener has started.");
			while (listenerRunning) {
				acceptAndListen();
			}
			
			isStopped = true;
			
			return null;
		}
		
		private void acceptAndListen(){
			log.debug("Listening on port "+listeningSocket.getLocalPort());
			Socket sock;
			if(listeningSocket.isClosed()){
				log.verboseDebug("ListeningSocket is closed. Cannot accept.");
			}
			try {
				sock = listeningSocket.accept();
			} catch (IOException e) {
				if(e instanceof SocketException){
					log.debug("SocketException has been raised. Probably listeningSocket is already closed.");
					return;
				}
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
			
			Future<?> spawened = pool.submit(spawn);
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
			log.debug("Incoming new connection accepted!");
			try {
				sock.setSoTimeout(BlockChainConstants.SOCKETS_MILLISECONDS_TIMEOUT);
			} catch (SocketException e1) {
				log.exception(e1);
				closeIfNotNull(sock);
				return;
			}
			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(sock.getInputStream());
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(sock);
				return;
			}
			Object received;
			try {
				received = ois.readObject();
			} catch (ClassNotFoundException e) {
				log.exception(e);
				closeIfNotNull(sock);
				return;
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(sock);
				return;
			}
			if(!(received instanceof ThickNodeMessages)){
				if(received instanceof MinerMessages){
					if(received instanceof MinerAddressNotification){
						MinerAddressNotification message = (MinerAddressNotification) received;
						synchronized (minerNodes) {							
							minerNodes.add(message.getAddress());
						}
					} else if (received instanceof SubmitTransactionMessage){
						SubmitTransactionMessage message = (SubmitTransactionMessage) received;
						if(needsForwarding(message.getTransaction())){							
							Runnable disseminator = new Runnable() {
								@Override
								public void run() {
									sendTransactionSubmission(message);
									return;								
								}
							};
							Future<?> disseminatorFuture = pool.submit(disseminator);
							try {
								disseminatorFuture.get(1, TimeUnit.NANOSECONDS);
							} catch (InterruptedException | ExecutionException e) {
								log.exception(e);
							} catch (TimeoutException e) {
								log.verboseDebug("Thread running: forwarding received transaction.");
							}
						}
					}
				} else {						
					//this object is not a valid message. we'll just discard it.
					closeIfNotNull(sock);
					return;
				}
			}
			if(received instanceof SubmitNewBlockMessage){
				SubmitNewBlockMessage newBlockMessage = (SubmitNewBlockMessage) received;
				manageSubmission(newBlockMessage, sock);
			} else if(received instanceof BlockRequestMessage){
				BlockRequestMessage newBlockRequest = (BlockRequestMessage) received;
				manageBlockRequest(newBlockRequest, sock);
			} else if(received instanceof CommunicationMessage){
				CommunicationMessage message = (CommunicationMessage) received;
				if((message.getMessage() == CommunicationMessage.Type.PING_FROM_THICK) || (message.getMessage() == CommunicationMessage.Type.PING_FROM_THIN)){
					log.debug("Received a ping (from "+message.getAttachment()+")!"); 
					BlockChainProperties props = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
					boolean justAdded = false;
					if(message.getChainName().equals(props.getBlockChainName())){
						 if((message.getAttachment() instanceof InetSocketAddress) && (message.getMessage() == CommunicationMessage.Type.PING_FROM_THICK)){
							justAdded = addNewThickNodeIfNotPresent((InetSocketAddress) message.getAttachment());
						}
						sendPingReply(sock);							
					} else { // the node is up to another chain. delete it.
						synchronized (otherThickNodes) {							
							otherThickNodes.remove((InetSocketAddress)message.getAttachment());
						}
						synchronized (maybeOffline) {							
							maybeOffline.remove((InetSocketAddress)message.getAttachment());
						}
						sendRefuse(sock);
					}
					if(justAdded){
						try {
							sock.close();
						} catch (IOException e) {
							log.exception(e);
							return;
						}
						sock = null;
						Block lastBlock = getLastBlockInChain();
						InetSocketAddress newNode = (InetSocketAddress) message.getAttachment();
						sendLastBlockNotification(lastBlock, newNode);
					}
				} else if(message.getMessage() == CommunicationMessage.Type.PING_FROM_MINER){
					log.debug("Received a ping from miner ("+message.getAttachment()+").");
					BlockChainProperties props = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
					if(message.getChainName().equals(props.getBlockChainName())){
						boolean minerNodesContains;
						synchronized (minerNodes) {							
							minerNodesContains = minerNodes.contains(message.getAttachment());
						}
						if((message.getAttachment() instanceof InetSocketAddress) && !(minerNodesContains)){
							synchronized (minerNodes) {								
								minerNodes.add((InetSocketAddress) message.getAttachment());
							}
							log.verboseDebug("Miner address "+message.getAttachment()+" added to my list");
						}
						sendPingReply(sock);
					}
				}
			} else if(received instanceof LastBlockNotificationMessage){
				LastBlockNotificationMessage message = (LastBlockNotificationMessage) received;
				manageLastBlockNotification(message, sock);
			} else if(received instanceof ThickNodeAddressMessage || received instanceof MinerNodeAddressMessage){
				NodeAddressMessage message = (NodeAddressMessage) received;
				manageNodeAddressRequest(message, sock);
			} else if(received instanceof BlockCompactRepresentationRequestMessage){
				BlockCompactRepresentationRequestMessage message = (BlockCompactRepresentationRequestMessage) received;
				manageBlockCompactRepresentationRequest(message, sock);
			} else if(received instanceof UpdatingChainBlockRequestMessage){
				UpdatingChainBlockRequestMessage message = (UpdatingChainBlockRequestMessage) received;
				manageUpdateChainRequest(message, sock, ois);
			} else if(received instanceof TransactionBlockNumberMessage){
				TransactionBlockNumberMessage message = (TransactionBlockNumberMessage) received;
				manageTransactionBlockNumberRequest(message, sock);
			}
			closeIfNotNull(sock);
		}
		
		public void stop(){
			log.verboseDebug("Stop called!");
			listenerRunning = false;
			try {
				listeningSocket.close();
				log.verboseDebug("Listening socket closed!");
			} catch (IOException e) {
				log.exception(e);
			}
		}
		
		public boolean isStopped() {
			return isStopped;
		}
		
	}
	
	private class NodePinger implements Callable<Void>{
		
		private boolean pingerRunning;
		
		private boolean isStopped;
		
		public NodePinger() {
			pingerRunning = true;
			isStopped = false;
		}

		@Override
		public Void call() throws Exception {
			Random rand = new Random(System.currentTimeMillis());
			log.debug("Pinger has started");
			while (pingerRunning) {
				int maybeOfflineSize;
				synchronized (maybeOffline) {						
					maybeOfflineSize = maybeOffline.size();
				}
				if(maybeOfflineSize > 0){
					pingMaybeOfflineNode(rand);
				} else {
					log.verboseDebug("There are no nodes suspected of being offline.");
					int otherThickNodesSize = 0;
					synchronized (otherThickNodes) {
						otherThickNodesSize = otherThickNodes.size();
					}
					if(otherThickNodesSize == 0){
						log.debug("My contact list seems to be empty. Trying to reload defaults or file list.");
						maybeOffline = Collections.synchronizedList(new ThickNodeAddressBookFile().getAddressList());
					}
				}
				int otherThickNodesSize = 0;
				synchronized (otherThickNodes) {
					otherThickNodesSize = otherThickNodes.size();
				}
				if(otherThickNodesSize > 0){
					pingGoodStandingNode(rand);					
				} else {
					log.verboseDebug("There are no nodes to ping.");
				}
				if(rand.nextInt(3) == 1){ // now, with probability 1/3 this node will ask for a new thick node address.
					askNewThickNodeAddress(rand);
				}
				int minerNodesSize;
				synchronized (minerNodes) {					
					minerNodesSize = minerNodes.size();
				}
				if(minerNodesSize > 0){
					pingMinerNode(rand);
				} else {
					log.verboseDebug("There are no miners to ping.");
				}
				if(rand.nextInt(3) == 1){ // with probability 1/3rd ask for a miner contact.
					askNewMinerNodeAddress(rand);
				}
				int sleepTime = PING_MILLISECONDS_INTERTIME + (rand.nextInt(PING_MILLISECONDS_VARIABILITY*2) - PING_MILLISECONDS_VARIABILITY);
				log.debug("Pinger will sleep for "+sleepTime+" ms.");
				Thread.sleep(sleepTime);
			} 
			
			isStopped = true;

			return null;
		}
		
		private void askNewThickNodeAddress(Random rand){
			int otherThickNodesSize = 0;
			synchronized (otherThickNodes) {
				otherThickNodesSize = otherThickNodes.size();
			}
			if(otherThickNodesSize == 0 ){
				return;
			}
			InetSocketAddress nodeToAsk;
			synchronized (otherThickNodes) {				
				nodeToAsk = otherThickNodes.get(rand.nextInt(otherThickNodes.size()));
			}
			if(nodeToAsk.equals(mySocketAddress)){
				log.verboseDebug("No sense in asking a node contact to myself. Skipping for now.");
				return;
			}
			log.verboseDebug("Asking "+nodeToAsk.toString()+" a new node contact");
			ThickNodeAddressMessage requestMessage = new ThickNodeAddressMessage();
			Socket sock = null;
			try {
				sock = new Socket(nodeToAsk.getAddress(), nodeToAsk.getPort());
			} catch (IOException e) {
				return;
			}
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				closeIfNotNull(sock);
				return;
			}
			try {
				oos.writeObject(requestMessage);
			} catch (IOException e) {
				closeIfNotNull(sock);
				return;
			}
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
				closeIfNotNull(sock);
				return;
			}
			Object obj = null;
			try {
				obj = ois.readObject();
			} catch (ClassNotFoundException e) {
				closeIfNotNull(sock);
				return;
			} catch (IOException e) {
				closeIfNotNull(sock);
				return;
			}
			boolean addedNewNode = false;
			InetSocketAddress newNode = null;
			if(obj instanceof ThickNodeAddressMessage){
				ThickNodeAddressMessage reply = (ThickNodeAddressMessage) obj;
				BlockChainProperties props = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
				if(reply.getChainName().equals(props.getBlockChainName())){					
					addedNewNode = addNewThickNodeIfNotPresent(reply.getAddress());
					if(addedNewNode){
						newNode = reply.getAddress();
					}
				}
			}
			closeIfNotNull(sock);
			if(addedNewNode && (newNode != null)){
				Block lastBlock = getLastBlockInChain();
				sendLastBlockNotification(lastBlock, newNode);
			}
		}
		
		private void askNewMinerNodeAddress(Random rand){
			int otherThickNodesSize = 0;
			synchronized (otherThickNodes) {
				otherThickNodesSize = otherThickNodes.size();
			}
			if(otherThickNodesSize == 0 ){
				return;
			}
			InetSocketAddress nodeToAsk;
			synchronized (otherThickNodes) {				
				nodeToAsk = otherThickNodes.get(rand.nextInt(otherThickNodes.size()));
			}
			if(nodeToAsk.equals(mySocketAddress)){
				log.verboseDebug("No sense in asking a node contact to myself. Skipping for now.");
				return;
			}
			log.verboseDebug("Asking "+nodeToAsk.toString()+" a new miner contact");
			MinerNodeAddressMessage requestMessage = new MinerNodeAddressMessage();
			Socket sock = null;
			try {
				sock = new Socket(nodeToAsk.getAddress(), nodeToAsk.getPort());
			} catch (IOException e) {
				return;
			}
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				closeIfNotNull(sock);
				return;
			}
			try {
				oos.writeObject(requestMessage);
			} catch (IOException e) {
				closeIfNotNull(sock);
				return;
			}
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
				closeIfNotNull(sock);
				return;
			}
			Object obj = null;
			try {
				obj = ois.readObject();
			} catch (ClassNotFoundException e) {
				closeIfNotNull(sock);
				return;
			} catch (IOException e) {
				closeIfNotNull(sock);
				return;
			}
			if(obj instanceof MinerNodeAddressMessage){
				MinerNodeAddressMessage reply = (MinerNodeAddressMessage) obj;
				BlockChainProperties props = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
				if(reply.getChainName().equals(props.getBlockChainName())){	
					InetSocketAddress receivedAddress = reply.getAddress();
					boolean minerNodesContains;
					synchronized (minerNodes) {
						minerNodesContains = minerNodes.contains(receivedAddress);
					}
					if(!minerNodesContains){
						synchronized (minerNodes) {							
							minerNodes.add(receivedAddress);
						}
						log.verboseDebug("New miner contact ("+receivedAddress+") added to my list.");
					}
				}
			}
			closeIfNotNull(sock);
			return;
		}
		
		private void pingMaybeOfflineNode(Random rand){
			InetSocketAddress toBePinged;
			synchronized (maybeOffline) {				
				toBePinged = maybeOffline.get(rand.nextInt(maybeOffline.size()));
			}
			if(toBePinged.equals(mySocketAddress)){
				log.verboseDebug("Illogical to ping myself. Removing item from list.");
				synchronized (maybeOffline) {					
					maybeOffline.remove(toBePinged);
				}
				return;
			}
			log.verboseDebug("Try to ping "+toBePinged.toString());
			boolean pingSuccess = sendAPing(toBePinged);
			if(!pingSuccess){
				log.debug("An unresponsive node ("+toBePinged+") failed to respond to ping. Removing it.");
				thickNodeNotRespondedToPing(toBePinged);
			}
		}
		
		private void pingGoodStandingNode(Random rand){
			InetSocketAddress toBePinged;
			synchronized (otherThickNodes) {				
				toBePinged = otherThickNodes.get(rand.nextInt(otherThickNodes.size()));
			}
			if(toBePinged.equals(mySocketAddress)){
				log.verboseDebug("Illogical to ping myself. Removing item from list.");
				synchronized (otherThickNodes) {					
					otherThickNodes.remove(toBePinged);
				}
				return;
			}
			log.verboseDebug("Try to ping "+toBePinged.toString());
			boolean pingSuccess = sendAPing(toBePinged);
			if(!pingSuccess){
				log.debug("A node ("+toBePinged+") failed to respond to ping.");
				thickNodeNotRespondedToPing(toBePinged);
			}
		}
		
		private void pingMinerNode(Random rand){
			InetSocketAddress toBePinged;
			synchronized (minerNodes) {				
				toBePinged = minerNodes.get(rand.nextInt(minerNodes.size()));
			}
			log.verboseDebug("Try to ping miner "+toBePinged.toString());
			boolean pingSuccess = sendAPing(toBePinged);
			if(!pingSuccess){
				log.debug("A miner ("+toBePinged+") failed to respond to ping.");
				synchronized (minerNodes) {					
					minerNodes.remove(toBePinged);
				}
			}
		}
		
		private boolean sendAPing(InetSocketAddress address){
			InetSocketAddress toBePinged = address;
			
			CommunicationMessage ping = new CommunicationMessage(Type.PING_FROM_THICK);
			ping.attachInetSocketAddress(mySocketAddress);
			Socket sock = null;
			try {
				sock = new Socket(toBePinged.getAddress(), toBePinged.getPort());
			} catch (IOException e) {
				return false;
			}
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
		}
		
		public void stop() {
			pingerRunning = false;
		}
		
		public boolean isStopped() {
			return isStopped;
		}
	}
	
	private void closeIfNotNull(Socket sock){
		if(sock != null){
			try {
				sock.close();
			} catch (IOException e) {
				log.exception(e);
			}
		}
	}
	
	private void manageTransactionBlockNumberRequest(TransactionBlockNumberMessage message, Socket sock){
		if(message.isRequest()){
			Transaction transaction = message.getTransaction();
			long blockNumber = transcationLastOccurrence(transaction);
			TransactionBlockNumberMessage reply = new TransactionBlockNumberMessage(blockNumber, transaction);
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				log.exception(e);
				return;
			}
			try {
				oos.writeObject(reply);
			} catch (IOException e) {
				log.exception(e);
				return;
			}
		}
		closeIfNotNull(sock);
	}
	
	private void manageNodeAddressRequest(NodeAddressMessage message, Socket sock){
		if(message.isRequest()){
			List<InetSocketAddress> listOfContacts;
			log.verboseDebug("Received an address request!");
			if(message instanceof ThickNodeAddressMessage){
				log.verboseDebug("The request if for a THICK node address.");
				synchronized (otherThickNodes) {					
					listOfContacts = ImmutableList.copyOf(otherThickNodes);
				}
			} else {
				log.verboseDebug("The request if for a MINER node address.");
				synchronized (minerNodes) {					
					listOfContacts = ImmutableList.copyOf(minerNodes);
				}
			}
			BlockChainProperties props = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
			if(message.getChainName().equals(props.getBlockChainName())){
				if(listOfContacts.size() > 0){
					Random rand = new Random(System.currentTimeMillis());
					InetSocketAddress selectedAddress = listOfContacts.get(rand.nextInt(listOfContacts.size()));
					log.verboseDebug("Replying to address request with"+selectedAddress);
					ThickNodeAddressMessage reply = new ThickNodeAddressMessage(selectedAddress);
					ObjectOutputStream oos;
					try {
						oos = new ObjectOutputStream(sock.getOutputStream());
					} catch (IOException e) {
						log.debug("Cannot open stream to send reply to address request. Aborting.");
						return;
					}
					try {
						oos.writeObject(reply);
					} catch (IOException e) {
						log.debug("Cannot send reply to address request. Abort.");
						return;
					}					
				} else {
					log.verboseDebug("No other nodes of the requested type appears to be online. Sending a refuse message.");
					sendRefuse(sock);
				}
			} else {
				log.verboseDebug("Sending a refuse message! Message chain name: "+message.getChainName()+" ; my chain name: "+props.getBlockChainName());
				sendRefuse(sock);
			}
			closeIfNotNull(sock);
		}
	}
	
	private void sendRefuse(Socket sock){
		CommunicationMessage deny = new CommunicationMessage(Type.REFUSED);
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(sock.getOutputStream());
		} catch (IOException e) {
			log.debug("Cannot open refuse message output stream. Aborting.");
			return;
		}
		try {
			oos.writeObject(deny);
		} catch (IOException e) {
			log.debug("Cannot send refuse message. Aborting.");
			return;
		}
	}
	
	private void manageLastBlockNotification(LastBlockNotificationMessage message, Socket sock) {
		Block myLastBlock = getLastBlockInChain();
		Block receivedLastBlock = message.getLastBlock();
		log.debug("Last block notified from the net. (Block: "+receivedLastBlock.getHeader()+")");
		try {
			sock.close();
		} catch (IOException e2) {
			log.exception(e2);
		}
		sock = null;
		
		if(myLastBlock.getHeader().getBlockNumber() > receivedLastBlock.getHeader().getBlockNumber()){
			//my chain is the "good" one.
			LastBlockNotificationMessage myReply = new LastBlockNotificationMessage(mySocketAddress, myLastBlock);
			Socket replySocket = null;
			try {
				replySocket = new Socket(message.getThickNodeAddress().getAddress(), message.getThickNodeAddress().getPort());
			} catch (IOException e) {
				log.exception(e);
				return;
			}
			ObjectOutputStream replyOos = null;
			try {
				replyOos = new ObjectOutputStream(replySocket.getOutputStream());
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(replySocket);
				return;
			}
			try {
				replyOos.writeObject(myReply);
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(replySocket);
				return;
			}
			try {
				replySocket.close();
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(replySocket);
				return;
			}
			log.debug("The notified block is of a shorter chain. Discarded.");
		} else{
			if(BlockUtils.nextBlockNumber(myLastBlock.getHeader()) == receivedLastBlock.getHeader().getBlockNumber()){
				log.verboseDebug("Received block is a good candidate. Try to insert it.");
				//maybe received block is the next one. just give it a try.
				boolean isGood = insertNewBlock(receivedLastBlock);
				log.verboseDebug("Insertion of candidate block resulted in: "+isGood);
				if(isGood){
					log.debug("Block inserted! Notifying to the other blocks.");
					notifyNewBlockToOtherNodes(receivedLastBlock);
					synchronized (registeredListeners) {						
						for(LocalThickNodeListener listener : registeredListeners){
							listener.notifyNewBlockHeaderFromNet(receivedLastBlock.getHeader());
						}
					}
				}
			} else if(receivedLastBlock.getHeader().getBlockNumber() > myLastBlock.getHeader().getBlockNumber()){
				if(!updatingChain){					
					//maybe my chain copy is out of date.
					log.debug("My block chain looks shorter than the one to which the received block belongs.");
					try {
						updateMyChain(receivedLastBlock, myLastBlock, message.getThickNodeAddress().getAddress(), message.getThickNodeAddress().getPort());
					} catch (Exception e) {
						log.debug("Chain update is failed with an uncatched exception. Aborting update.");
						updatingChain = false;
					}
				} else {
					log.debug("Already updating chain. Skipping for now.");
					updatingChain = false;
				}
			}
			sanityCheckLastBlocks(50);
		}
	}
	
	private void updateMyChain(Block lastBlockReceived, Block myLastBlock, InetAddress queriedThickNodeAddress, int queriedThickNodePort){
		ArrayList<Block> newBranch = new ArrayList<Block>();
		newBranch.add(lastBlockReceived);
		long neededBlock = lastBlockReceived.getHeader().getBlockNumber() - 1;
		long lastNeededBlock = 0;
		if(myLastBlock != null){
			lastNeededBlock = myLastBlock.getHeader().getBlockNumber();
		}
		updatingChain = true;

		Socket updateSocket = null;
		try {
			updateSocket = new Socket(queriedThickNodeAddress, queriedThickNodePort);
		} catch (IOException e1) {
			log.exception(e1);
			updatingChain = false;
			return;
		}
		ObjectOutputStream updateOos = null;
		try {
			updateOos = new ObjectOutputStream(updateSocket.getOutputStream());
		} catch (IOException e1) {
			log.exception(e1);
			closeIfNotNull(updateSocket);
			updatingChain = false;
			return;
		}
		ObjectInputStream updateOis = null;

		while (neededBlock >= lastNeededBlock) {
			//request blocks
			UpdatingChainBlockRequestMessage blockRequest = new UpdatingChainBlockRequestMessage(neededBlock);
			try {
				updateOos.writeObject(blockRequest);
			} catch (IOException e1) {
				log.exception(e1);
			}
			if(updateOis == null){						
				try {
					updateOis = new ObjectInputStream(updateSocket.getInputStream());
				} catch (IOException e1) {
					log.exception(e1);
				}
			}
			UpdatingChainBlockRequestMessage blockReply = null;
			try {
				blockReply = (UpdatingChainBlockRequestMessage) updateOis.readObject();
			} catch (ClassNotFoundException e) {
				log.exception(e);
			} catch (IOException e) {
				log.exception(e);
			}
			if(blockReply.getMessageType() == raw.blockChain.services.thickNode.messages.types.UpdatingChainBlockRequestMessage.Type.BLOCK_REPLY){
				log.verboseDebug("Getting blocks to update my chain. Received: "+blockReply.getBlock().getHeader());
				newBranch.add(blockReply.getBlock());
			}
			try {
				updateOos.flush();
			} catch (IOException e) {
				log.exception(e);
			}

			neededBlock= neededBlock-1;
		}
		UpdatingChainBlockRequestMessage updateDone = new UpdatingChainBlockRequestMessage();
		try {
			updateOos.writeObject(updateDone);
		} catch (IOException e) {
			log.exception(e);
			closeIfNotNull(updateSocket);
			updatingChain = false;
			return;
		}
		try {
			updateSocket.close();
		} catch (IOException e) {
			log.exception(e);
		}

		Collections.sort(newBranch, new BlockNumberComparator());

		boolean branchIsValid = false;
		long deletionStartingPoint = -1;

		Block prevBlock = null;
		if(myLastBlock != null && !newBranch.get(0).getHeader().equals(myLastBlock.getHeader())){
			log.verboseDebug("Chains differs  beyond my last block!");
			//the branch does not start from my my last block. maybe the fork is deeper then here.
			if(newBranch.get(0).getHeader().getBlockNumber() == 0 && myLastBlock.getHeader().getBlockNumber() == 0){
				//in this case I just have an auto-generated genesis block.
				//it can be safely substituted with the one I got from the net.
				log.verboseDebug("My genesis block is wrong. Other chain is longer. Accepting the other chain.");
				deletionStartingPoint = 0;
			} else {
				//I must find the first common block.
				try {
					updateSocket = new Socket(queriedThickNodeAddress, queriedThickNodePort);
				} catch (IOException e1) {
					log.exception(e1);
					updatingChain = false;
					return;
				}
				try {
					updateOos = new ObjectOutputStream(updateSocket.getOutputStream());
				} catch (IOException e) {
					log.exception(e);
					closeIfNotNull(updateSocket);
					updatingChain = false;
					return;
				}
				long insertionPoint = myLastBlock.getHeader().getBlockNumber() - 1;
				UpdatingChainBlockRequestMessage blockRequest = new UpdatingChainBlockRequestMessage(insertionPoint);
				try {
					updateOos.writeObject(blockRequest);
				} catch (IOException e1) {
					log.exception(e1);
				}
				try {
					updateOis = new ObjectInputStream(updateSocket.getInputStream());
				} catch (IOException e1) {
					log.exception(e1);
				}
				UpdatingChainBlockRequestMessage blockReply = null;
				try {
					blockReply = (UpdatingChainBlockRequestMessage) updateOis.readObject();
				} catch (ClassNotFoundException e) {
					log.exception(e);
				} catch (IOException e) {
					log.exception(e);
				}
				Block forkBlock = database.getBlockByNumber(insertionPoint);
				while(blockReply != null && !blockReply.getBlock().equals(forkBlock) && insertionPoint >= 0){
					newBranch.add(blockReply.getBlock());
					log.verboseDebug("This block is not jet the fork point. "+blockReply.getBlock().getHeader());
					insertionPoint = insertionPoint-1;
					if(insertionPoint >= 0){								
						forkBlock = database.getBlockByNumber(insertionPoint);
						blockRequest = new UpdatingChainBlockRequestMessage(insertionPoint);
						try {
							updateOos.writeObject(blockRequest);
						} catch (IOException e1) {
							log.exception(e1);
						}
						try {
							blockReply = (UpdatingChainBlockRequestMessage) updateOis.readObject();
						} catch (ClassNotFoundException e) {
							log.exception(e);
						} catch (IOException e) {
							log.exception(e);
						}
					}
				}
				if(blockReply != null && blockReply.getBlock().equals(forkBlock)){ // in case loop was never entered
					if(!newBranch.contains(blockReply.getBlock())){								
						newBranch.add(blockReply.getBlock());
					}
				}
				updateDone = new UpdatingChainBlockRequestMessage();
				try {
					updateOos.writeObject(updateDone);
				} catch (IOException e) {
					log.exception(e);
					closeIfNotNull(updateSocket);
					updatingChain = false;
					return;
				}
				try {
					updateSocket.close();
				} catch (IOException e) {
					log.exception(e);
				}
				if(newBranch.size() == 0){
					log.warning("Something went wrong retrieving the blocks. Aborting this.");
					updatingChain = false;
					return;
				}
				Collections.sort(newBranch, new BlockNumberComparator());
				deletionStartingPoint = newBranch.get(0).getHeader().getBlockNumber()-1;
				if(deletionStartingPoint > 0){							
					prevBlock = database.getBlockByNumber(deletionStartingPoint);
					log.debug("Update of my chain will start after "+prevBlock.getHeader());
				} else {
					prevBlock = null;
					log.debug("Update of my chain will start from block ZERO");
				}
			}
		} else {
			if(lastNeededBlock != 0){				
				log.verboseDebug("First block of new branch equals mine. It does not need to be added.");
				prevBlock = newBranch.remove(0);
				deletionStartingPoint = prevBlock.getHeader().getBlockNumber()+1;
			} else {
				deletionStartingPoint = 0;
			}
			Collections.sort(newBranch, new BlockNumberComparator());
		}

		branchIsValid = BlockUtils.validateConsecutiveBlocks(newBranch.get(0), prevBlock, false, 0);
		prevBlock = newBranch.get(0);
		if(branchIsValid){				
			for(int i=1; i < newBranch.size(); i++){
				branchIsValid = BlockUtils.validateConsecutiveBlocks(newBranch.get(i), prevBlock, false, 0, newBranch);
				if(!branchIsValid){
					log.verboseDebug("Validation failed!\nvalidating block: "+newBranch.get(i).getHeader()+"\nvs block: "+prevBlock.getHeader());
					break;
				}
				prevBlock = newBranch.get(i);
			}
		}

		if(branchIsValid && deletionStartingPoint >=0){
			//the branch continue from my last block. update my chain.
			if(database.getBlockHeaderByNumber(0) != null){				
				log.verboseDebug("Starting deletion of \"old\" blocks from my chain.");
				ArrayList<Block> originalBlocks = new ArrayList<Block>();
				Block lastBlock = getLastBlockInChain();
				long index = lastBlock.getHeader().getBlockNumber();
				if(deletionStartingPoint > 0){						
					while (lastBlock.getHeader().getBlockNumber() > deletionStartingPoint) {
						originalBlocks.add(lastBlock);
						log.verboseDebug("Putting aside "+lastBlock.getHeader());
						index = index-1;
						lastBlock = database.getBlockByNumber(index);
						while (lastBlock == null) {
							index = index-1;
							lastBlock = database.getBlockByNumber(index);
						}
					}
				} else {
					while(index >= 0){
						if(lastBlock != null){								
							originalBlocks.add(lastBlock);
							log.verboseDebug("Putting aside "+lastBlock.getHeader());
						}
						index = index-1;
						lastBlock = database.getBlockByNumber(index);
					}
				}
				
				boolean deleted = database.deleteBlocksBulk(originalBlocks);
				if(!deleted){
					log.warning("Something went wrong deleting blocks! Aborting...");
					updatingChain = false;
					return;
				}
				log.verboseDebug("My original blocks were deleted.");
			}

			log.debug("Now I'll insert the blocks received in the DB.");
			for(Block block : newBranch){
				if(!insertPreValidatedBlock(block)){
					log.warning("Something went wrong. A block was not inserted: "+block.getHeader());
					log.debug("Aborting this."); //FIXME
					break;
				} else {
					log.verboseDebug("Inserted new block "+block.getHeader());
				}
			}
			BlockHeader lastHeader = getLastBlockHeaderInChain();
			synchronized (registeredListeners) {							
				for(LocalThickNodeListener listener : registeredListeners){
					listener.notifyNewBlockHeaderFromNet(lastHeader);
				}
			}
		} else {
			log.debug("Branch is not valid. Aborting update.");
		}
		updatingChain = false;
	}
	
	private class BlockNumberComparator implements Comparator<Block>{
		@Override
		public int compare(Block o1, Block o2) {
			return (int) (o1.getHeader().getBlockNumber() - o2.getHeader().getBlockNumber());
		}
	}
	
	private void manageUpdateChainRequest(UpdatingChainBlockRequestMessage message, Socket sock, ObjectInputStream ois){
		UpdatingChainBlockRequestMessage receivedMessage = message;
		ObjectOutputStream oos = null;
		while (receivedMessage.getMessageType() != raw.blockChain.services.thickNode.messages.types.UpdatingChainBlockRequestMessage.Type.DONE_BYE) {
			if(receivedMessage.getMessageType() == raw.blockChain.services.thickNode.messages.types.UpdatingChainBlockRequestMessage.Type.BLOCK_REQUEST){
				log.verboseDebug("Got an update request for blok #"+receivedMessage.getBlockNumber());
				BlockHeader header = getBlockHeaderByNumber(receivedMessage.getBlockNumber());
				Block block = null;
				if(header != null){
					block = getBlockFromHeader(header);
				}
				log.verboseDebug("Reply with block "+block.getHeader());
				UpdatingChainBlockRequestMessage reply = new UpdatingChainBlockRequestMessage(block);
				if(oos == null){					
					try {
						oos = new ObjectOutputStream(sock.getOutputStream());
					} catch (IOException e) {
						log.exception(e);
					}
				}
				try {
					oos.writeObject(reply);
					log.verboseDebug("Reply sent.");
				} catch (IOException e) {
					log.exception(e);
				}
				try {
					oos.flush();
				} catch (IOException e1) {
					log.exception(e1);
				}
				try {
					receivedMessage = (UpdatingChainBlockRequestMessage) ois.readObject();
				} catch (ClassNotFoundException e) {
					log.exception(e);
				} catch (IOException e) {
					log.exception(e);
				}
			}
		}
		log.debug("The other node is done. Bye bye.");
	}
	
	/**
	 * Looks up in the list of other {@link ThickNode}s and if
	 * it is not present then add the address to the list.
	 * 
	 * @param address the address of a {@link ThickNode}
	 * @return <tt>true</tt> if the node was not in the goodStanding list
	 */
	private boolean addNewThickNodeIfNotPresent(InetSocketAddress address){
		boolean justAdded = false;
		synchronized (otherThickNodes) {
			if(!otherThickNodes.contains(address)){
				otherThickNodes.add(address);
				justAdded = true;
			}			
		}
		synchronized (maybeOffline) {			
			if(maybeOffline.contains(address)){
				maybeOffline.remove(address);
			}
		}
		return justAdded;
	}
	
	/**
	 * Update the references to another {@link ThickNode} if this
	 * node does not respond to a ping message.
	 * 
	 * @param address the not responding {@link ThickNode} address
	 */
	private void thickNodeNotRespondedToPing(InetSocketAddress address){
		boolean otherThickNodesContains;
		synchronized (otherThickNodes) {
			otherThickNodesContains = otherThickNodes.contains(address);			
		}
		if(otherThickNodesContains){
			moveThickNodeToMyabeOffline(address);
		} else {
			boolean maybeOfflineContains;
			synchronized (maybeOffline) {				
				maybeOfflineContains = maybeOffline.contains(address);
			}
			if(maybeOfflineContains){
				removeThickNodeFromMaybeOffline(address);
			}
		}
	}
	
	private void moveThickNodeToMyabeOffline(InetSocketAddress address){
		synchronized (otherThickNodes) {			
			otherThickNodes.remove(address);
		}
		boolean maybeOfflineContains;
		synchronized (maybeOffline) {				
			maybeOfflineContains = maybeOffline.contains(address);
		}
		if(!maybeOfflineContains){
			synchronized (maybeOffline) {				
				maybeOffline.add(address);
			}
		}
	}
	
	private void removeThickNodeFromMaybeOffline(InetSocketAddress address){
		synchronized (maybeOffline) {			
			maybeOffline.remove(address);
		}
	}
	
	private void manageBlockRequest(BlockRequestMessage message, Socket sock){
		if(message.isRequestMessage()){
			Block found;
			switch (message.getTypeOfRequest()) {
			case BY_BLOCK_NUMBER:
				BlockHeader foundHeader = getBlockHeaderByNumber(message.getBlockNumber());
				if(foundHeader == null){
					found = null;
				} else {
					found = getBlockFromHeader(foundHeader);
				}
				break;
			case BY_HASH:
				found = getBlockFromHash(message.getHash());
				break;
			case LAST_IN_CHAIN:
				found = getLastBlockInChain();
				break;
			default:
				found = null;
				break;
			}
			BlockRequestMessage reply = null;
			if(found != null){
				reply = new BlockRequestMessage(found);
			} else {
				switch (message.getTypeOfRequest()) {
				case BY_BLOCK_NUMBER:
					reply = new BlockRequestMessage(message.getBlockNumber());
					reply.rejectMessage();
					break;
				case BY_HASH:
					reply = new BlockRequestMessage(message.getHash());
					reply.rejectMessage();
					break;
				case LAST_IN_CHAIN:
					reply = new BlockRequestMessage();
					reply.rejectMessage();
					break;
				default:
					break;
				}
			}
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				log.debug("Cannot open stream to reply to block request. Aborting.");
				return;
			}
			try {
				oos.writeObject(reply);
			} catch (IOException e) {
				log.debug("Cannot send reply to block request. Aborting.");
				return;
			}
		}
	}
	
	private void manageBlockCompactRepresentationRequest(BlockCompactRepresentationRequestMessage message, Socket sock){
		if(message.isRequestMessage()){
			BlockCompactRepresentation found = null;
			BlockHeader header = null;
			switch (message.getTypeOfRequest()) {
			case BY_BLOCK_NUMBER:
				header = database.getBlockHeaderByNumber(message.getBlockNumber());
				break;
			case BY_HASH:
				try {
					header = database.getBlockFromHash(message.getHash()).getHeader();
				} catch (SQLException e) {
					log.exception(e);
					return;
				}
				break;
			case BY_HEADER:
				header = message.getHeader();
				break;
			default:
				header = null;
				break;
			}
			if(header != null){
				Transaction transaction = message.getTransaction();
				try {
					found = database.getBlockCompatRepresentation(header, transaction);
				} catch (SQLException e) {
					log.exception(e);
					return;
				}
			}
			BlockCompactRepresentationRequestMessage reply = null;
			if(found != null){
				reply = new BlockCompactRepresentationRequestMessage(found);
			} else {
				switch (message.getTypeOfRequest()) {
				case BY_BLOCK_NUMBER:
					reply = new BlockCompactRepresentationRequestMessage(message.getBlockNumber(), message.getTransaction());
					reply.rejectMessage();
					break;
				case BY_HASH:
					reply = new BlockCompactRepresentationRequestMessage(message.getHash(), message.getTransaction());
					reply.rejectMessage();
					break;
				case BY_HEADER:
					reply = new BlockCompactRepresentationRequestMessage(message.getHeader(), message.getTransaction());
					reply.rejectMessage();
					break;
				default:
					break;
				}
			}
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				log.exception(e);
				return;
			}
			try {
				oos.writeObject(reply);
			} catch (IOException e) {
				log.exception(e);
				return;
			}
		}
	}

	private void sendPingReply(Socket sock) {
		CommunicationMessage reply = new CommunicationMessage(CommunicationMessage.Type.PONG);
		reply.attachInetSocketAddress(mySocketAddress);
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(sock.getOutputStream());
		} catch (IOException e) {
			log.debug("Cannot open stream to send pong.");
			return;
		}
		try {
			oos.writeObject(reply);
		} catch (IOException e) {
			log.debug("Exception sending pong.");
			return;
		}
		log.debug("Replyed with Pong");
	}
	
	private void manageSubmission(SubmitNewBlockMessage message, Socket sock){
		Block receivedBlock = message.getBlock();
		log.verboseDebug("Received a block from "+message.getSenderAddress()+": "+receivedBlock.getHeader());
		boolean isInserted = false;
		try {
			isInserted = insertNewBlock(receivedBlock);			
		} catch (Exception e) {
			log.warning("THIS EXCEPTION IS TOTALLY UNEXPECTED! :-O");
			log.exception(e);
		}
		CommunicationMessage reply;
		if(isInserted){
			log.verboseDebug("Received block is ACCEPTED!");
			reply = new CommunicationMessage(Type.ACCEPTED);
			reply.attachBlockHeader(receivedBlock.getHeader());
		}
		else{
			log.verboseDebug("Received block is REFUSED!");
			reply = new CommunicationMessage(Type.REFUSED);
			reply.attachBlockHeader(receivedBlock.getHeader());
		}
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(sock.getOutputStream());
		} catch (IOException e) {
			log.debug("Cannot open stream to send reply to the new block notification. Try to proceed anyway.");
		}
		if(oos != null){
			try {
				oos.writeObject(reply);
			} catch (IOException e) {
				log.debug("Cannot send reply to the new block notification. Try to proceed anyway.");
			}
		}
		
		if(isInserted){
			synchronized (registeredListeners) {				
				for(LocalThickNodeListener listener : registeredListeners){
					listener.notifyNewBlockHeaderFromNet(receivedBlock.getHeader());
				}
			}
			notifyNewBlockToOtherNodes(receivedBlock);
		}
		boolean minerNodesContains;
		synchronized (minerNodes) {
			minerNodesContains = minerNodes.contains(message.getSenderAddress());			
		}
		if(!minerNodesContains){
			synchronized (minerNodes) {				
				minerNodes.add(message.getSenderAddress());
			}
		}
	}
	
	private void notifyNewBlockToOtherNodes(Block block){
		notifyNewBlockToOtherThickNodes(block);
		notifyNewBlockHeaderToMiners(block.getHeader());
	}
	
	private void notifyNewBlockToOtherThickNodes(Block block){
		ImmutableList<InetSocketAddress> othersCopy;
		synchronized (otherThickNodes) {			
			othersCopy = ImmutableList.copyOf(otherThickNodes);
		}
		ArrayList<InetSocketAddress> nodesRetrieved = new ArrayList<InetSocketAddress>(othersCopy);
		synchronized (maybeOffline) {			
			nodesRetrieved.addAll(maybeOffline);
		}
		
		List<InetSocketAddress> nodes = ImmutableList.copyOf(nodesRetrieved);
		
		for(InetSocketAddress node : nodes){
			sendLastBlockNotification(block, node);
		}
	}
	
	private void sendLastBlockNotification(Block block, InetSocketAddress address){
		LastBlockNotificationMessage message = new LastBlockNotificationMessage(mySocketAddress, block);
		try (Socket notificationSocket = new Socket(address.getAddress(), address.getPort())){			
			ObjectOutputStream notificationOos;
			try {
				notificationOos = new ObjectOutputStream(notificationSocket.getOutputStream());
			} catch (IOException e) {
				log.debug("Cannot open stream to send last block notification. Aborting.");
				closeIfNotNull(notificationSocket);
				return;
			}
			try {
				notificationOos.writeObject(message);
			} catch (IOException e) {
				log.debug("Cannot send last block notification. Aborting.");
			}
			closeIfNotNull(notificationSocket);
		} catch (ConnectException e) {
			log.verboseDebug("Connection refused at "+address+". Aborting.");
			return;
		} catch (Exception e) {
			log.exception(e);
			for(Throwable t : e.getSuppressed()){
				log.exception(t);
			}
		}
	}
	
	private void notifyNewBlockHeaderToMiners(BlockHeader header){
		synchronized (minerNodes) {			
			log.verboseDebug("Must notify a header to the miners. I know "+minerNodes.size()+" miner nodes.");
		}
		LastBlockHeaderNotificationMessage notification = new LastBlockHeaderNotificationMessage(mySocketAddress, header);
		List<InetSocketAddress> miners;
		synchronized (minerNodes) {			
			miners = ImmutableList.copyOf(minerNodes);
		}
		for(InetSocketAddress miner : miners){
			log.verboseDebug("Notify: @ "+miner+" : "+header);
			Socket notificationSocket = null;
			try {
				notificationSocket = new Socket(miner.getAddress(), miner.getPort());
			} catch (IOException e) {
				log.debug("Cannot open socket. Aborting notification.");
				continue;
			}
			ObjectOutputStream notificationOos = null;
			try {
				notificationOos = new ObjectOutputStream(notificationSocket.getOutputStream());
			} catch (IOException e) {
				log.debug("Cannot open stream. Aborting notification.");
				continue;
			}
			try {
				notificationOos.writeObject(notification);
			} catch (IOException e) {
				log.debug("Cannot send message. Aborting notification.");
				continue;
			}
			closeIfNotNull(notificationSocket);					
		}
	}

	/**
	 * Try to insert the block into the blockchain. If the block
	 * number is not valid (or the block itself is not valid) nothing is done
	 * and <tt>false</tT> is returned.
	 * 
	 * @param block the block to be inserted in the chain
	 * @return <tt>true</tT> if the block was valid and inserted in the chain. <tt>false</tt> otherwise
	 */
	private synchronized boolean insertNewBlock(Block block){
		try {			
			if(!BlockUtils.validateBlock(block)){
				return false;
			}
		} catch (Exception e) {
			log.verboseDebug("Validation failed with exception! ("+block.getHeader()+" )");
			return false;
		}
		return insertPreValidatedBlock(block);
	}
	
	/**
	 * Try to insert the block in the blockhain. However the block
	 * is expected to be already validated and no check whatsoever
	 * is performed a part from the constrain integrity ones.
	 * 
	 * @param block the {@link Block} to be inserted in the chain
	 * @return <tt>true</tT> if the block was valid and inserted in the chain. <tt>false</tt> otherwise
	 */
	private synchronized boolean insertPreValidatedBlock(Block block){
		BlockHeader searched = database.getBlockHeaderByHash(block.getHeader().hash());
		if(searched != null){
			return false;
		}
		try {
			database.storeOnDataBase(block);
		} catch (SQLIntegrityConstraintViolationException e) {
			log.verboseDebug("Block seems to be duplicated. Dropping insertion.");
			return false;
		}
		return true;		
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.thickNode.LocalMinerSeriviceListener#notifyNewLocalBlock(raw.blockChain.api.Block)
	 */
	@Override
	public boolean notifyNewLocalBlock(Block block) {
		log.verboseDebug("New local block notified: "+block.getHeader());
		if(insertNewBlock(block)){
			log.verboseDebug(block.getHeader()+" inserted.");
			notifyNewBlockToOtherNodes(block);
			return true;
		}
		log.verboseDebug(block.getHeader()+" refused.");
		return false;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.thickNode.ThickNode#stopService()
	 */
	@Override
	public boolean stopService() {
		log.debug("Stop command issued.");
		database.close();
		log.verboseDebug("DB closed");
		saveOtherNodesToFile();
		myPinger.stop();
		log.verboseDebug("Stopping pinger.");
		while (!myPinger.isStopped()) {
//			log.verboseDebug("Waiting for pinger to stop.");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.exception(e);
			}
		}
		log.verboseDebug("Pinger stopped.");
		myListener.stop();
		log.verboseDebug("Stopping listener.");
		while (!myListener.isStopped()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.exception(e);
			}
		}
		log.verboseDebug("Listener stopped.");

		return true;
	}

	@Override
	public Block getBlockFromHeader(BlockHeader header) {
		return getBlockFromHash(header.hash());
	}

	@Override
	public BlockHeader getLastBlockHeaderInChain() {
		return database.getLastBlockHeaderInChain(true);
	}

	@Override
	public Block getLastBlockInChain() {
		BlockHeader header = getLastBlockHeaderInChain();
		Block found = null;
		try {
			found = database.getBlockFromHash(header.hash());
		} catch (SQLException e) {
			log.exception(e);
		}
		return found;
	}

	@Override
	public BlockHeader getBlockHeaderByNumber(long blockNumber) {
		BlockHeader header = database.getBlockHeaderByNumber(blockNumber);
		return header;
	}

	@Override
	public Block getBlockFromHash(HashValue hash) {
		Block found = null;
		try {
			found = database.getBlockFromHash(hash);
		} catch (SQLException e) {
			log.exception(e);
		}
		return found;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#checkTransactionInBlockByHeader(raw.blockChain.api.BlockHeader, raw.blockChain.api.Transaction)
	 */
	@Override
	public boolean checkTransactionInBlockByHeader(BlockHeader blockHeader,	Transaction transaction) {
		return checkTransactionInBlockByHeaderHash(blockHeader.hash(), transaction);
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#checkTransactionInBlockByHeaderHash(raw.blockChain.api.HashValue, raw.blockChain.api.Transaction)
	 */
	@Override
	public boolean checkTransactionInBlockByHeaderHash(HashValue headerHash, Transaction transaction) {
		log.verboseDebug("Checking if "+transaction+" is in block with hash "+headerHash);
		Block block = getBlockFromHash(headerHash);
		boolean containsTransaction = block.getTransactions().contains(transaction);
		log.verboseDebug("Transaction "+transaction+" is in block "+block+"? "+containsTransaction);
		boolean isValid = BlockUtils.validateBlockNoTimestamp(block);
		log.verboseDebug("Block "+block+" is valid? "+isValid);
		return containsTransaction && isValid;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#checkTransactionInBlockByBlockNumber(long, raw.blockChain.api.Transaction)
	 */
	@Override
	public boolean checkTransactionInBlockByBlockNumber(long blockNumber, Transaction transaction) {
//		log.verboseDebug("Got a check request for transaction: "+transaction+" in block #"+blockNumber);
		BlockHeader header = getBlockHeaderByNumber(blockNumber);
		if(header == null){
			return false;
		}
		return checkTransactionInBlockByHeaderHash(header.hash(), transaction);
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#submitTransaction(raw.blockChain.api.Transaction)
	 */
	@Override
	public void submitTransaction(Transaction transaction) {
		log.verboseDebug("Submitting transaction: "+transaction);
		SubmitTransactionMessage message = new SubmitTransactionMessage(mySocketAddress, transaction);
		if(needsForwarding(transaction)){			
			sendTransactionSubmission(message);
		}
	}
	
	private void sendTransactionSubmission(SubmitTransactionMessage submission){
		List<InetSocketAddress> minerNodesCopy;
		synchronized (minerNodes) {
			minerNodesCopy = ImmutableList.copyOf(minerNodes);
		}
		if(minerNodesCopy.size() > 0){			
			for(InetSocketAddress miner : minerNodesCopy){
				try (Socket sock = new Socket(miner.getAddress(), miner.getPort())){				
					ObjectOutputStream oos = null;
					try {
						oos = new ObjectOutputStream(sock.getOutputStream());
					} catch (IOException e) {
						log.debug("Cannot open stream to miner node. Aborting submission.");
						closeIfNotNull(sock);
						continue;
					}
					try {
						oos.writeObject(submission);
					} catch (IOException e) {
						log.exception(e);
						closeIfNotNull(sock);
						continue;
					}
					log.verboseDebug("Sent transaction "+submission.getTransaction()+" to "+miner);
					closeIfNotNull(sock);
				} catch (Exception e) {
					log.exception(e);
					for(Throwable t : e.getSuppressed()){
						log.exception(t);
					}
				}
			}
		} else {
			ArrayList<InetSocketAddress> allThickNodes = new ArrayList<InetSocketAddress>();
			synchronized (otherThickNodes) {				
				allThickNodes.addAll(otherThickNodes);
			}
			synchronized (maybeOffline) {				
				allThickNodes.addAll(maybeOffline);
			}
			for(InetSocketAddress node : allThickNodes){
				Socket sock = null;
				try {
					sock = new Socket(node.getAddress(), node.getPort());
				} catch (IOException e) {
					log.verboseDebug("Troubles opening socket to "+node+". Aborting.");
					continue;
				}
				ObjectOutputStream oos = null;
				try {
					oos = new ObjectOutputStream(sock.getOutputStream());
				} catch (IOException e) {
					log.verboseDebug("Troubles opening OutputStream to "+node+". Aborting.");
					closeIfNotNull(sock);
					continue;
				}
				try {
					oos.writeObject(submission);
				} catch (IOException e) {
					log.verboseDebug("Troubles writing message in OutputStream to "+node+". Aborting.");
					closeIfNotNull(sock);
					continue;
				}
				try {
					sock.close();
				} catch (IOException e) {
					log.verboseDebug("Troubles closing socket to "+node+". Aborting.");
					continue;
				}
				log.verboseDebug("Forwarded transaction "+submission.getTransaction()+" to thick node "+node);
			}
		}
	}
	
	private boolean needsForwarding(Transaction transaction){
		if(!forwardedTransactions.contains(transaction)){
			if(forwardedTransactions.size()>20){
				forwardedTransactions.remove();
			}
			forwardedTransactions.add(transaction);
			log.verboseDebug("Transaction "+transaction+" is to be forwarded, and is added to already-forwarded list.");
			return true;
		} else {
			log.verboseDebug("Transaction "+transaction+" was already forwarded.");
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.thickNode.ThickNode#reagisterThickNodeListener(raw.blockChain.services.miner.LocalThickNodeListener)
	 */
	@Override
	public boolean reagisterThickNodeListener(LocalThickNodeListener listener) {
		synchronized (registeredListeners) {			
			registeredListeners.add(listener);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#isUp()
	 */
	@Override
	public boolean isUp() {
		return initialized;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.thickNode.LocalMinerSeriviceListener#getListenerAddress()
	 */
	@Override
	public InetSocketAddress getListenerAddress() {
		return mySocketAddress;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#getThickNodesList()
	 */
	@Override
	public ArrayList<InetSocketAddress> getThickNodesList() {
		ArrayList<InetSocketAddress> copy;
		synchronized (otherThickNodes) {			
			copy = new ArrayList<>(otherThickNodes);
		}
		return copy;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#getNodeAddress()
	 */
	@Override
	public InetSocketAddress getNodeAddress() {
		return mySocketAddress;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#transcationLastOccurrence(raw.blockChain.api.Transaction)
	 */
	@Override
	public long transcationLastOccurrence(Transaction transaction) {
		return database.searchTranscationBlockNumber(transaction);
	}

}
