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
package raw.blockChain.services.thinNode.implementations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import raw.blockChain.api.Block;
import raw.blockChain.api.BlockChainConstants;
import raw.blockChain.api.BlockCompactRepresentation;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Merkler;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultMerkler;
import raw.blockChain.exceptions.TransactionNotPresentException;
import raw.blockChain.services.dbHelper.BlocksToDataBase;
import raw.blockChain.services.dbHelper.implementations.DefaultBlockToDataBase;
import raw.blockChain.services.miner.messages.types.SubmitTransactionMessage;
import raw.blockChain.services.thickNode.messages.types.BlockCompactRepresentationRequestMessage;
import raw.blockChain.services.thickNode.messages.types.BlockRequestMessage;
import raw.blockChain.services.thickNode.messages.types.CommunicationMessage;
import raw.blockChain.services.thickNode.messages.types.ThickNodeAddressMessage;
import raw.blockChain.services.thickNode.messages.types.TransactionBlockNumberMessage;
import raw.blockChain.services.thickNode.messages.types.CommunicationMessage.Type;
import raw.blockChain.services.thinNode.ThinNode;
import raw.blockChain.services.utils.ThickNodeAddressBookFile;
import raw.logger.Log;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;
import raw.utils.RAWServiceUtils;

/**
 * Default implementation of {@link ThinNode} interface.
 * 
 * @author vic
 *
 */
public class DefaultThinNode implements ThinNode {
	
	private Log log;
	
	private List<InetSocketAddress> thickNodes;
	private List<InetSocketAddress> unresponsiveThickNodes;
	
	private BlocksToDataBase database;
	
	private boolean running;
	private boolean initialized;
	
	private InetSocketAddress myAddress;
	
	public DefaultThinNode() {
		log = Log.getLogger();
		
		thickNodes = Collections.synchronizedList(new ArrayList<InetSocketAddress>());
		unresponsiveThickNodes = Collections.synchronizedList(new ArrayList<InetSocketAddress>());

		database = new DefaultBlockToDataBase(this);
		initialized = false;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Void call() throws Exception {
		log.info("Starting Thin Node");
		running = true;
		database.open();

		BlockChainProperties props = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		
		ThickNodeAddressBookFile addressBook = new ThickNodeAddressBookFile();
		thickNodes = Collections.synchronizedList(addressBook.getAddressList());
		
		InetAddress myIP = null;
		while (myIP == null) {
			try {
				myIP = RAWServiceUtils.resolveIP();
			} catch (Exception e) {
				log.debug("Cant resolve my ip. Waiting 2 seconds and trying again.");
				Thread.sleep(1850);
			}
		}
		myAddress = new InetSocketAddress(myIP, props.getListeningSocket());
		
		Random rand = new Random(System.currentTimeMillis());
		while (running) {
			int thickNodesSize;
			synchronized (thickNodes) {
				thickNodesSize = thickNodes.size();
			}
			if(thickNodesSize>0){
				InetSocketAddress toBeAsked;
				synchronized (thickNodes) {					
					toBeAsked = thickNodes.get(rand.nextInt(thickNodes.size()));
				}
				log.debug("Asking a new contact to "+toBeAsked);
				askNodeContactToAnotherNode(toBeAsked);
			} else {
				log.verboseDebug("No nodes to ask new contacts. Skipping.");
			}
			if(initialized){				
				int sleeptime = PING_MILLISECONDS_INTERTIME + (rand.nextInt(PING_MILLISECONDS_VARIABILITY*2) - PING_MILLISECONDS_VARIABILITY);
				log.verboseDebug("Gonna sleep for "+sleeptime+" ms.");
				Thread.sleep(sleeptime);
				log.verboseDebug("I woke up!");
			}
			InetSocketAddress lastPinged = null;
			synchronized (thickNodes) {
				thickNodesSize = thickNodes.size();
			}
			if(thickNodesSize>0){
				InetSocketAddress toPing;
				synchronized (thickNodes) {					
					toPing = thickNodes.get(rand.nextInt(thickNodes.size()));
				}
				log.debug("Try to ping "+toPing);
				boolean success = sendAPing(toPing);
				if(!success){
					moveThickNodeToUnresponsive(toPing);
					lastPinged = toPing;
				}
			} else {
				log.verboseDebug("No nodes to ping. Skipping.");
			}
			int unresponsiveThickNodesSize;
			synchronized (unresponsiveThickNodes) {
				unresponsiveThickNodesSize = unresponsiveThickNodes.size();
			}
			if(unresponsiveThickNodesSize > 0){
				InetSocketAddress toPing;
				synchronized (unresponsiveThickNodes) {
					toPing = unresponsiveThickNodes.get(rand.nextInt(unresponsiveThickNodes.size()));					
				}
				if(lastPinged != null){
					int counter = 0;
					while(lastPinged.equals(toPing)){
						toPing = null;
						if(counter > (unresponsiveThickNodesSize/2)){
							log.verboseDebug("Cant find a suitable unresponsive node to ping.");
							break;
						}
						synchronized (unresponsiveThickNodes) {							
							toPing = unresponsiveThickNodes.get(rand.nextInt(unresponsiveThickNodes.size()));
						}
						counter++;
					}
				}
				if(toPing != null){					
					log.debug("Try to ping unresponsive node "+toPing);
					boolean success = sendAPing(toPing);
					if(success){
						addNodeAddressIfNotPresent(toPing);
					} else {
						log.verboseDebug(toPing + " is dead. Deleting contact.");
						synchronized (unresponsiveThickNodes) {
							unresponsiveThickNodes.remove(toPing);							
						}
					}
				} else {
					log.verboseDebug("I do not have a \"good\" unresponsive node to ping. Skipping.");
				}
			} else {
				log.verboseDebug("No unresponsive nodes to ping. Skipping.");
				synchronized (thickNodes) {					
					thickNodesSize = thickNodes.size();
				}
				if(thickNodesSize == 0){
					log.debug("My contact list is empty. Try to reload contacts from file or defaults.");
					unresponsiveThickNodes = Collections.synchronizedList(new ThickNodeAddressBookFile().getAddressList());
				}
			}
			initialized = true;
		}

		log.verboseDebug("Thin node is done.");
		return null;
	}
	
	private void askNodeContactToAnotherNode(InetSocketAddress nodeToBeAsked){
		ThickNodeAddressMessage request = new ThickNodeAddressMessage();
		Socket sock = null;
		try {
			sock = new Socket(nodeToBeAsked.getAddress(), nodeToBeAsked.getPort());
		} catch (IOException e5) {
//			log.exception(e5);
			log.verboseDebug("IOException creating socket to "+nodeToBeAsked+". Aborting contact.");
			return;
		}
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(sock.getOutputStream());
		} catch (IOException e4) {
			log.verboseDebug("Cannot establish connection output stream. Aborting request.");
			closeIfNotNull(sock);
			return;
		}
		try {
			oos.writeObject(request);
		} catch (IOException e3) {
			log.verboseDebug("Cannot Send request. Aborting.");
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
		} catch (IOException e2) {
			if(!(e2 instanceof SocketTimeoutException)){				
				log.verboseDebug("Cannot establish connection input stream. Aborting request.");
			} else {
				log.verboseDebug("Socket timeout opening input stream. Aborting request.");;
			}
			closeIfNotNull(sock);
			return;
		}
		Object obj = null;
		try {
			obj = ois.readObject();
		} catch (ClassNotFoundException e1) {
			log.exception(e1);
			closeIfNotNull(sock);
			return;
		} catch (IOException e1) {
			if (!(e1 instanceof SocketTimeoutException)) {
				log.verboseDebug("Cannot read reply. Aborting request.");
			} else {
				log.verboseDebug("Read from socket timeout. Aborting request.");;
			}
			closeIfNotNull(sock);
			return;
		}
		try {
			sock.close();
		} catch (IOException e) {
			log.exception(e);
		}
		if(obj instanceof ThickNodeAddressMessage){
			ThickNodeAddressMessage reply = (ThickNodeAddressMessage) obj;
			BlockChainProperties props = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
			if((!reply.isRequest())&&reply.getChainName().equals(props.getBlockChainName())){
				addNodeAddressIfNotPresent(reply.getAddress());
			}
		}
	}
	
	private void moveThickNodeToUnresponsive(InetSocketAddress address){
		synchronized (thickNodes) {			
			thickNodes.remove(address);
		}
		boolean unresponsiveThickNodesContains;
		synchronized (unresponsiveThickNodes) {
			unresponsiveThickNodesContains = unresponsiveThickNodes.contains(address);
		}
		if(!unresponsiveThickNodesContains){
			synchronized (unresponsiveThickNodes) {				
				unresponsiveThickNodes.add(address);
			}
		}
	}
	
	private boolean sendAPing(InetSocketAddress address){
		CommunicationMessage ping = new CommunicationMessage(Type.PING_FROM_THIN);
		ping.attachInetSocketAddress(myAddress);
		try (Socket sock = new Socket(address.getAddress(), address.getPort())){
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				closeIfNotNull(sock);
				return false;
			}
			try {
				oos.writeObject(ping);
			} catch (IOException e) {
				closeIfNotNull(sock);
				return false;
			}
			try {
				sock.setSoTimeout(BlockChainConstants.SOCKETS_MILLISECONDS_TIMEOUT);
			} catch (SocketException e1) {
				log.exception(e1);
				closeIfNotNull(sock);
				return false;
			}
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(sock.getInputStream());
			} catch (IOException e) {
				closeIfNotNull(sock);
				return false;
			}
			Object obj = null;
			try {
				obj = ois.readObject();
			} catch (ClassNotFoundException e1) {
				closeIfNotNull(sock);
				return false;
			} catch (IOException e1) {
				closeIfNotNull(sock);
				return false;
			}
			try {
				sock.close();
			} catch (IOException e) {
				log.exception(e);
			}
			if(obj instanceof CommunicationMessage){
				CommunicationMessage reply = (CommunicationMessage) obj;
				BlockChainProperties props = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
				if((!(reply.getMessage() == Type.PONG) || !reply.getChainName().equals(props.getBlockChainName()))){
					return false;
				}
			} else {
				return false;
			}
			return true;
		} catch (Exception e) {
			log.exception(e);
			for(Throwable t : e.getSuppressed()){
				log.exception(t);
			}
			return false;
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
	
	private void addNodeAddressIfNotPresent(InetSocketAddress address){
		boolean thickNodesContains;
		synchronized (thickNodes) {
			thickNodesContains = thickNodes.contains(address);
		}
		if(!thickNodesContains){
			boolean unresponsiveThickNodesContains;
			synchronized (unresponsiveThickNodes) {
				unresponsiveThickNodesContains = unresponsiveThickNodes.contains(address);
			}
			if(unresponsiveThickNodesContains){
				synchronized (unresponsiveThickNodes) {					
					unresponsiveThickNodes.remove(address);
				}
			}
			synchronized (thickNodes) {				
				thickNodes.add(address);
			}
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#getBlockFromHeader(raw.blockChain.api.BlockHeader)
	 */
	@Override
	public Block getBlockFromHeader(BlockHeader header) {
		return getBlockFromHash(header.hash());
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#getBlockFromHash(raw.blockChain.api.HashValue)
	 */
	@Override
	public Block getBlockFromHash(HashValue hash) {
		Block block = localGetBlockFromHash(hash);
		if(block == null){
			// in the database the block is not found.
			// BUT. we could ask to thick nodes.
			ArrayList<InetSocketAddress> nodesToAsk = getNodesToAsk();
			ArrayList<Block> blocks = new ArrayList<Block>();
			for(InetSocketAddress node : nodesToAsk){
				Block received = askBlockByHashToThickNode(node, hash);
				if(received != null){
					blocks.add(received);
				}
			}
			block = selectTheGoodBlock(blocks);
			if(block != null){
				try {
					database.storeOnDataBase(block);
				} catch (SQLIntegrityConstraintViolationException e) {
					log.verboseDebug("Retrieved block is possibly duplicated.");
					return null;
				}
			}
		}
		return block;
	}
	
	private Block selectTheGoodBlock(ArrayList<Block> blocks){
		if(blocks.size() == 0){
			return null;
		}
		boolean strongConsensus = true;
		Block check = blocks.get(0);
		for(Block bl : blocks){
			if(!bl.equals(check)){
				strongConsensus = false;
				break;
			}
		}
		if(strongConsensus){
			return check;
		}
		Block block = moreAttendibleBlock(blocks);
		return block;
	}
	
	private ArrayList<InetSocketAddress> getNodesToAsk(){
		int numberOfNodesToAsk;
		synchronized (thickNodes) {			
			numberOfNodesToAsk = Math.min(THICK_NODE_CONSENSUS, thickNodes.size());
		}
		Random rand = new Random(System.currentTimeMillis());
		ArrayList<InetSocketAddress> nodesToAsk = new ArrayList<InetSocketAddress>();
		while (numberOfNodesToAsk > 0) {
			InetSocketAddress address;
			synchronized (thickNodes) {				
				address = thickNodes.get(rand.nextInt(thickNodes.size()));
			}
			if(!nodesToAsk.contains(address)){
				nodesToAsk.add(address);
				numberOfNodesToAsk-=1;
			}
		}
		return nodesToAsk;
	}
	
	private Block localGetBlockFromHash(HashValue hash){
		Block block = null;
		try {
			block = database.getBlockFromHash(hash);
		} catch (SQLException e) {
			log.exception(e);
		}
		return block;
	}
	
	/**
	 * Given a set of blocks try to find the "best" one.
	 * @param blocks
	 * @return
	 */
	private Block moreAttendibleBlock(ArrayList<Block> blocks){
		HashMap<Block, Integer> count = new HashMap<Block, Integer>();
		for(Block block : blocks){
			int freq = Collections.frequency(blocks, block);
			if(!count.containsKey(block)){
				count.put(block, freq);
			}
		}
		ArrayList<Map.Entry<Block, Integer>> countToList = new ArrayList<Map.Entry<Block,Integer>>(count.entrySet());
		Collections.sort(countToList, new Comparator<Map.Entry<Block, Integer>>() {
			@Override
			public int compare(Map.Entry<Block, Integer> o1, Map.Entry<Block, Integer> o2) {
				return o1.getValue().intValue() - o2.getValue().intValue();
			}
		});
		int maxValue = -1;
		ArrayList<Block> papabili = new ArrayList<Block>();
		for(Map.Entry<Block, Integer> entry : countToList){
			if(entry.getValue().intValue() >= maxValue){
				papabili.add(entry.getKey());
				maxValue = entry.getValue().intValue();
			} else {
				break;
			}
		}
		if(papabili.size() == 1){
			return papabili.get(0);
		}
		Block chosen = null;
		for(Block block : papabili){
			BlockHeader myPrevious = localGetBlockHeaderByNumber(block.getHeader().getBlockNumber() - 1 );
			if(myPrevious != null){
				if(block.getHeader().previousBlock().equals(myPrevious.hash())){
					chosen = block;
					break;
				}
			}
		}
		if(chosen == null){
			Random rand = new Random(System.currentTimeMillis());
			chosen = papabili.get(rand.nextInt(papabili.size()));
		}
		return chosen;
	}
	
	private BlockHeader localGetBlockHeaderByNumber(long blockNumber){
		return database.getBlockHeaderByNumber(blockNumber);
	}
	
	private Block askBlockByHashToThickNode(InetSocketAddress nodeToAsk, HashValue hash){
		BlockRequestMessage request = new BlockRequestMessage(hash);
		return sendBlockRequest(nodeToAsk, request);
	}
	
	private Block sendBlockRequest(InetSocketAddress nodeToAsk, BlockRequestMessage request){
		log.verboseDebug("Sending Block request to "+nodeToAsk+".");
		try (Socket sock = new Socket(nodeToAsk.getAddress(), nodeToAsk.getPort())){
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				log.verboseDebug("Cannot establish connection output stream. Returning.");
				closeIfNotNull(sock);
				return null;
			}
			try {
				oos.writeObject(request);
			} catch (IOException e) {
				log.verboseDebug("Cannot send request. Returning.");
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
				log.verboseDebug("Cannot establish connection input stream. Returning.");
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
				log.verboseDebug("Cannot receive reply. Returning.");
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

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#getLastBlockHeaderInChain()
	 */
	@Override
	public BlockHeader getLastBlockHeaderInChain() {
		// we should ask it to the thick nodes
		Block lastBlock = retrieveLastBlockInChain();
		if(lastBlock == null){
			return null;
		}
		return lastBlock.getHeader();
	}
	
	private Block retrieveLastBlockInChain(){
		ArrayList<InetSocketAddress> nodesToAsk = getNodesToAsk();
		ArrayList<Block> blocks = new ArrayList<Block>();
		for(InetSocketAddress node : nodesToAsk){
			Block received = askLastBlockInChain(node);
			if(received != null){
				blocks.add(received);
			}
		}
		Block block = selectTheGoodBlock(blocks);
		if(block != null){
			try {
				database.storeOnDataBase(block);
			} catch (SQLIntegrityConstraintViolationException e) {
				log.verboseDebug("Retrieved block is possibly duplicated.");
				BlockHeader header = database.getLastBlockHeaderInChain(false);
				if(!block.getHeader().equals(header)){					
					return null;
				}
			}
		}
		return block;
	}
	
	private Block askLastBlockInChain(InetSocketAddress nodeToAsk){
		BlockRequestMessage request = new BlockRequestMessage();
		return sendBlockRequest(nodeToAsk, request);
	}
	
	private BlockHeader localGetLastBlockHeaderInChain(){
		return database.getLastBlockHeaderInChain(false);
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#getLastBlockInChain()
	 */
	@Override
	public Block getLastBlockInChain() {
		BlockHeader lastHeader = localGetLastBlockHeaderInChain();
		Block lastBlock = null;
		if(lastHeader != null){
			lastBlock = localGetBlockFromHash(lastHeader.hash());
		}
		if(lastBlock == null){
			lastBlock = retrieveLastBlockInChain();
		}
		return lastBlock;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#getBlockHeaderByNumber(long)
	 */
	@Override
	public BlockHeader getBlockHeaderByNumber(long blockNumber) {
		BlockHeader header = localGetBlockHeaderByNumber(blockNumber);
		if(header == null){
			ArrayList<InetSocketAddress> nodesToAsk = getNodesToAsk();
			ArrayList<Block> blocks = new ArrayList<Block>();
			for(InetSocketAddress node : nodesToAsk){
				Block received = askBlockByBumberToThickNode(node, blockNumber);
				if(received != null){
					blocks.add(received);
				}
			}
			Block block = selectTheGoodBlock(blocks);
			if(block != null){
				try {
					database.storeOnDataBase(block);
				} catch (SQLIntegrityConstraintViolationException e) {
					log.verboseDebug("Retrieved block is possibly duplicated.");
					return null;
				}
				header = block.getHeader();
			}
		}
		return header;
	}
	
	private Block askBlockByBumberToThickNode(InetSocketAddress nodeToAsk, long blockNumber){
		BlockRequestMessage request = new BlockRequestMessage(blockNumber);
		return sendBlockRequest(nodeToAsk, request);
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.thinNode.ThinNode#stopService()
	 */
	@Override
	public boolean stopService() {
		log.verboseDebug("Stop issued.");
		database.close();
		running = false;
		
		int thickNodesSize;
		synchronized (thickNodes) {
			thickNodesSize = thickNodes.size();			
		}
		if(thickNodesSize > 0){
			log.verboseDebug("Saving thick nodes addresses");
			ThickNodeAddressBookFile addressBook;
			synchronized (thickNodes) {				
				addressBook = new ThickNodeAddressBookFile(thickNodes);
			}
			addressBook.writeToFile();
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#checkTransactionInBlockByHeader(raw.blockChain.api.BlockHeader, raw.blockChain.api.Transaction)
	 */
	@Override
	public boolean checkTransactionInBlockByHeader(BlockHeader blockHeader,	Transaction transaction) {
		BlockCompactRepresentation compact = null;
		try {
			compact = database.getBlockCompatRepresentation(blockHeader, transaction);
		} catch (SQLException e) {
			log.exception(e);
		}
		if(compact == null){
			BlockCompactRepresentationRequestMessage request = new BlockCompactRepresentationRequestMessage(blockHeader, transaction);
			ArrayList<InetSocketAddress> nodes = getNodesToAsk();
			ArrayList<BlockCompactRepresentation> gotCompacts = new ArrayList<BlockCompactRepresentation>();
			for(InetSocketAddress node : nodes){
				BlockCompactRepresentation retrieved = askBlockCompactRepresentation(node, request);
				if(retrieved != null){
					gotCompacts.add(retrieved);
				}
			}
			if(gotCompacts.size() == 0){
				return false;
			}
			compact = selectGoodBlockCompactRepresentation(gotCompacts);
		}
		Merkler merkler = new DefaultMerkler(1);
		HashValue root = null;
		try {
			root = merkler.getMerkleRootByIntermediate(transaction, compact.getIntermediateValues());
		} catch (TransactionNotPresentException e) {
			log.exception(e);
		}
		return compact.getHeader().merkleRoot().equals(root);
	}
	
	private BlockCompactRepresentation selectGoodBlockCompactRepresentation(ArrayList<BlockCompactRepresentation> blocks){
		HashMap<BlockCompactRepresentation, Integer> freqs = new HashMap<BlockCompactRepresentation, Integer>();
		for(BlockCompactRepresentation compact : blocks){
			if(!freqs.containsKey(compact)){
				int freq = Collections.frequency(blocks, compact);
				freqs.put(compact, new Integer(freq));
			}
		}
		ArrayList<Map.Entry<BlockCompactRepresentation, Integer>> freqsList = new ArrayList<Map.Entry<BlockCompactRepresentation, Integer>>(freqs.entrySet());
		Collections.sort(freqsList, new Comparator<Map.Entry<BlockCompactRepresentation, Integer>>() {
			@Override
			public int compare(Entry<BlockCompactRepresentation, Integer> o1,
					Entry<BlockCompactRepresentation, Integer> o2) {
				int a = o1.getValue().intValue();
				int b = o2.getValue().intValue();
				return a - b;
			}
		});
		ArrayList<BlockCompactRepresentation> papabili = new ArrayList<BlockCompactRepresentation>();
		int maxValue = -1;
		for(Map.Entry<BlockCompactRepresentation, Integer> item : freqsList){
			if(item.getValue().intValue() >= maxValue){
				maxValue = item.getValue().intValue();
				papabili.add(item.getKey());
			} else {
				break;
			}
		}
		if(papabili.size() == 1){
			return papabili.get(0);
		}
		Random rand = new Random(System.currentTimeMillis());
		return papabili.get(rand.nextInt(papabili.size()));
	}
	
	private BlockCompactRepresentation askBlockCompactRepresentation(InetSocketAddress nodeAddress, BlockCompactRepresentationRequestMessage requestMessage){
		Object obj = null;
		log.verboseDebug("Ready to open socket.");
		try (Socket sock = new Socket(nodeAddress.getAddress(), nodeAddress.getPort())){
			log.verboseDebug("Sending request to "+nodeAddress);
			ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
			oos.writeObject(requestMessage);
			sock.setSoTimeout(BlockChainConstants.SOCKETS_MILLISECONDS_TIMEOUT);
			ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
			obj = ois.readObject();
			log.verboseDebug("Read object reply");
		} catch (Exception e) {
			log.exception(e);
			for(Throwable t : e.getSuppressed()){
				log.exception(t);
			}
		}

		BlockCompactRepresentation retrieved = null;
		if(obj instanceof BlockCompactRepresentationRequestMessage){
			log.verboseDebug("Replyed object is a BlockCompactRepresentationRequestMessage.");
			BlockCompactRepresentationRequestMessage reply = (BlockCompactRepresentationRequestMessage) obj;
			if(reply.isPositiveReply()){
				retrieved = reply.getBlockCompactRepresentation();
				log.verboseDebug("Compact representation retrieved.");
			}
		}
		return retrieved;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#checkTransactionInBlockByHeaderHash(raw.blockChain.api.HashValue, raw.blockChain.api.Transaction)
	 */
	@Override
	public boolean checkTransactionInBlockByHeaderHash(HashValue headerHash, Transaction transaction) {
		BlockHeader header = database.getBlockHeaderByHash(headerHash);
		BlockCompactRepresentation compact = null;
		if(header != null){
			try {
				database.getBlockCompatRepresentation(header, transaction);
			} catch (SQLException e) {
				log.exception(e);
			}
		}
		if(compact == null){
			BlockCompactRepresentationRequestMessage request = new BlockCompactRepresentationRequestMessage(headerHash, transaction);
			ArrayList<InetSocketAddress> nodes = getNodesToAsk();
			ArrayList<BlockCompactRepresentation> gotCompacts = new ArrayList<BlockCompactRepresentation>();
			for(InetSocketAddress node : nodes){
				BlockCompactRepresentation retrieved = askBlockCompactRepresentation(node, request);
				if(retrieved != null){					
					gotCompacts.add(retrieved);
				}
			}
			if(gotCompacts.size() == 0){
				return false;
			}
			compact = selectGoodBlockCompactRepresentation(gotCompacts);
		}
		Merkler merkler = new DefaultMerkler(1);
		HashValue root = null;
		try {
			root = merkler.getMerkleRootByIntermediate(transaction, compact.getIntermediateValues());
		} catch (TransactionNotPresentException e) {
			log.exception(e);
		}
		return compact.getHeader().merkleRoot().equals(root);
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#checkTransactionInBlockByBlockNumber(long, raw.blockChain.api.Transaction)
	 */
	@Override
	public boolean checkTransactionInBlockByBlockNumber(long blockNumber, Transaction transaction) {
		log.verboseDebug("Checking: "+transaction+" is in block #"+blockNumber+"?");
//		BlockHeader header = localGetBlockHeaderByNumber(blockNumber);
		BlockHeader header = getBlockHeaderByNumber(blockNumber);
		log.verboseDebug("Header: "+header);
		BlockCompactRepresentation compact = null;
		if(header != null){
			try {
				database.getBlockCompatRepresentation(header, transaction);
			} catch (SQLException e) {
				log.exception(e);
			}
		}
		if(compact == null){
			log.verboseDebug("No db-stored compact representation for "+transaction+" in "+header);
			BlockCompactRepresentationRequestMessage request = new BlockCompactRepresentationRequestMessage(blockNumber, transaction);
			ArrayList<InetSocketAddress> nodes = getNodesToAsk();
			ArrayList<BlockCompactRepresentation> gotCompacts = new ArrayList<BlockCompactRepresentation>();
			for(InetSocketAddress node : nodes){
				BlockCompactRepresentation retrieved = askBlockCompactRepresentation(node, request);
				log.verboseDebug("Completed request to "+node);
				if(retrieved != null){
					log.verboseDebug("Compact representation retrieved from "+node);
					gotCompacts.add(retrieved);
				}
			}
			if(gotCompacts.size() == 0){
				return false;
			}
			compact = selectGoodBlockCompactRepresentation(gotCompacts);
			log.verboseDebug("Selected final compact representation!");
		}
		Merkler merkler = new DefaultMerkler(1);
		HashValue root = null;
		try {
			root = merkler.getMerkleRootByIntermediate(transaction, compact.getIntermediateValues());
		} catch (TransactionNotPresentException e) {
			log.exception(e);
		}
		boolean result = compact.getHeader().merkleRoot().equals(root);
//		return compact.getHeader().merkleRoot().equals(root);
		log.verboseDebug("Check result = "+result+" . (Computated root:"+root+" VS actual root:"+compact.getHeader().merkleRoot()+")");
		return result;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#submitTransaction(raw.blockChain.api.Transaction)
	 */
	@Override
	public void submitTransaction(Transaction transaction) {
		ArrayList<InetSocketAddress> allThickNodes = new ArrayList<InetSocketAddress>();
		synchronized (thickNodes) {			
			allThickNodes.addAll(thickNodes);
		}
		synchronized (unresponsiveThickNodes) {			
			allThickNodes.addAll(unresponsiveThickNodes);
		}

		SubmitTransactionMessage submission = new SubmitTransactionMessage(myAddress, transaction);
		for(InetSocketAddress node : allThickNodes){
			Socket sock = null;
			try {
				sock = new Socket(node.getAddress(), node.getPort());
			} catch (IOException e) {
				log.exception(e);
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
				oos.writeObject(submission);
			} catch (IOException e) {
				log.exception(e);
				closeIfNotNull(sock);
				return;
			}
			try {
				sock.close();
			} catch (IOException e) {
				log.exception(e);
				return;
			}
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#isUp()
	 */
	@Override
	public boolean isUp() {
		return initialized;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#getThickNodesList()
	 */
	@Override
	public ArrayList<InetSocketAddress> getThickNodesList() {
		ArrayList<InetSocketAddress> copy;
		synchronized (thickNodes) {			
			copy = new ArrayList<>(thickNodes);
		}
		return copy;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#getNodeAddress()
	 */
	@Override
	public InetSocketAddress getNodeAddress() {
		return myAddress;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.CommonNode#transcationLastOccurrence(raw.blockChain.api.Transaction)
	 */
	@Override
	public long transcationLastOccurrence(Transaction transaction) {
		ArrayList<Long> numbers = new ArrayList<Long>();
		List<InetSocketAddress> thickNodesCopy;
		synchronized (thickNodes) {
			thickNodesCopy = ImmutableList.copyOf(thickNodes);
		}
		for(InetSocketAddress node : thickNodesCopy){
			numbers.add(askTransactionLastOccurrence(node, transaction));
		}
		Collections.sort(numbers);
		
		long current = -1;
		int count = 0;
		long max = -1;
		int maxCount = 0;
		
		for(Long number : numbers){
			if(number != current){
				count = 1;
				current = number;
			} else {
				count += 1;
			}
			if(count > maxCount){
				max = current;
				maxCount = count;
			}
		}
		return max;
	}
	
	private long askTransactionLastOccurrence(InetSocketAddress node, Transaction transaction){
		TransactionBlockNumberMessage reply = null;
		try (Socket sock = new Socket(node.getAddress(), node.getPort());){
			sock.setSoTimeout(BlockChainConstants.SOCKETS_MILLISECONDS_TIMEOUT);
			ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
			TransactionBlockNumberMessage request = new TransactionBlockNumberMessage(transaction);
			oos.writeObject(request);
			ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
			Object received = ois.readObject();
			if(received instanceof TransactionBlockNumberMessage){
				TransactionBlockNumberMessage converted = (TransactionBlockNumberMessage) received;
				if(!converted.isRequest()){
					reply = converted;
				}
			}
		} catch (Exception e) {
			log.exception(e);
			for(Throwable t : e.getSuppressed()){
				log.exception(t);
			}
		} 

		if(reply == null){
			return -1;
		}
		return reply.getBlockNumber();
	}

}
