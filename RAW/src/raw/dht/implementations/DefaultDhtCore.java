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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.UnexpectedException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import raw.blockChain.BlockChainCore;
import raw.blockChain.api.Block;
import raw.blockChain.api.BlockChainConstants;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.utils.TransactionUtils;
import raw.blockChain.services.implementations.DefaultBlockChainCore;
import raw.concurrent.RAWExecutors;
import raw.dht.DhtAddress;
import raw.dht.DhtConstants;
import raw.dht.DhtCore;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.DhtKey;
import raw.dht.DhtKeyHolder;
import raw.dht.DhtListener;
import raw.dht.DhtLocalNode;
import raw.dht.DhtNode;
import raw.dht.DhtNodeExtended;
import raw.dht.DhtPinger;
import raw.dht.DhtSearcher;
import raw.dht.DhtValue;
import raw.dht.RoutingTable;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.exceptions.IncompleteNodeExtendedException;
import raw.dht.implementations.utils.DhtNodeAddressBookFile;
import raw.dht.implementations.utils.DhtSigningUtils;
import raw.dht.implementations.utils.DhtUtils;
import raw.dht.messages.DhtMessage.MessageType;
import raw.dht.messages.implementations.tcp.StoreMessage;
import raw.dht.utils.nodesInfoServer.NodesInfoServer;
import raw.logger.Log;
import raw.settings.DhtProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;
import raw.utils.RAWServiceUtils;

/**
 * Default implementation of {@link DhtCore}
 * 
 * @author vic
 *
 */
public class DefaultDhtCore implements DhtCore {
	
	private static DefaultDhtCore core = null;
	
	private BlockChainCore chainCore;
	
	private ExecutorService pool;
	
	private DhtKeyHolder keyHolder;
	private DhtListener listener;
	private DhtPinger pinger;
	
	private DhtProperties props;
	
	private DhtLocalNode myLocalNode;
	
	private RoutingTable routingTable;
	
	private DhtSearcher searcher;
	
	private boolean started;
	
	private long lastBlockNumberInChain;
	private long lastBlockNumberInChainAccess;
	
	private byte[] currentSeed;
	private long currentSeedAccessedAtBlockNumber;

	private long currentSeedBlockNumber;
	private long currentSeedBlockNumberAccess;
	
	private long lastSeedBlockNumber;
	private long lastSeedBlockNumberAccess;
	
	private byte[] futureSeed;
	private long futureSeedAccess;
	
	private ProofOfWorkManager proofOfWorkManager;
	
	private TransactionMonitor transactionMonitor;
	
	private NodesInfoServer nodesInfoServer;
	
	private Log log;
	
	private DefaultDhtCore() {
		this.chainCore = DefaultBlockChainCore.getBlockChainCore();
		pool = RAWExecutors.newCachedThreadPool();
		
		props = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
		
		log = Log.getLogger();
		
		setUpMyNodeReference();
		DhtNode myNode;
		try {
			myNode = myLocalNode.getNodeExtended();
		} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
			myNode = myLocalNode.getNode();
		}
		log.verboseDebug("Node set up. Id = "+myNode.getID().toHexString()+" ; address = "+myNode.getAddress());
		
		keyHolder = new DefaultDhtKeyHolder(this);
		listener = new DefaultDhtListener(this, chainCore);
		pinger = new DefaultDhtPinger(this);
		
		routingTable = new DefaultRoutingTable(myNode, this);
		log.debug("Routing table set up.");
		
		searcher = new DefaultDhtSearcher(this);
		log.verboseDebug("Searcher set up.");
		
		lastBlockNumberInChain = -1;
		lastBlockNumberInChainAccess = -1;
		
		currentSeed = null;
		currentSeedAccessedAtBlockNumber = -1;
		
		currentSeedBlockNumber = -1;
		currentSeedBlockNumberAccess = -1;

		lastSeedBlockNumber = -1;
		lastSeedBlockNumberAccess = -1;
		
		futureSeed = null;
		futureSeedAccess = -1;
		
		proofOfWorkManager = new ProofOfWorkManager(myNode.getID(), myNode.getPublicKey(), this);
		
		boolean transactionNeeded = false;
		if(myNode instanceof DhtNodeExtended){
			long advertisedBlockNumber = ((DhtNodeExtended)myNode).getTransactionBlockNumber();
			Transaction advertisedTransaction = ((DhtNodeExtended)myNode).getTransaction();
			if(advertisedBlockNumber == -1){
				advertisedBlockNumber = chainCore.transcationLastOccurrence(advertisedTransaction);
				KeyPair keyPair = myLocalNode.getKeyPair();
				myLocalNode = new DefaultDhtLocalNode(((DhtNodeExtended) myNode).getDhtNode(), advertisedTransaction, keyPair, advertisedBlockNumber);
			}
			if(isTooOld(advertisedBlockNumber)){
				transactionNeeded = true;
			} else {
				transactionNeeded = !chainCore.checkTransactionIsInBlock(advertisedBlockNumber, advertisedTransaction);
			}
		} else {
			transactionNeeded = true;
		}
		
		if(transactionNeeded){			
			resolveMyNodeTransaction(myNode.getAddress(), getKeyPair());
		}
		
		transactionMonitor = null;
		
		nodesInfoServer = null;
			
		started = false;
		
		log.verboseDebug("DHT Core constructed.");
	}
	
	private void setUpMyNodeReference(){
		DhtLocalNode loadedNode = loadMyNodeFromFile();
		DhtAddress address = resolveMyAddress();
		if(loadedNode == null){
			log.verboseDebug("Loaded node infos: NULL.");
			KeyPair keyPair = DhtSigningUtils.getSignKeyPair();
			DhtNode createdNode = createDhtNode(address, keyPair);
			myLocalNode = new DefaultDhtLocalNode(createdNode, null, keyPair, -1);
			saveMyNodeToFile();
			return;
		}
		log.verboseDebug("Loaded node infos: "+loadedNode);
		if(loadedNode.getNode().getAddress().equals(address)){
			log.verboseDebug("Address ("+address+") is still valid.");
			myLocalNode = loadedNode;
			return;
		} else {
			log.verboseDebug("Address changed. Updating my node.");
			DhtNode newNode = new DefaultDhtNode(loadedNode.getNode().getID(), loadedNode.getKeyPair().getPublic(), address);
			myLocalNode = new DefaultDhtLocalNode(newNode, loadedNode.getTransaction(), loadedNode.getKeyPair(), loadedNode.getBlockNumber());
			saveMyNodeToFile();
			return;
		}
	}
	
	private DhtLocalNode loadMyNodeFromFile(){
		String path = props.getDhtBaseDir()+"myNode.ser";
		File nodeFile = new File(path);
		Object read = null;
		try (	FileInputStream fis = new FileInputStream(nodeFile);
				ObjectInputStream ois = new ObjectInputStream(fis)){
			read = ois.readObject();
		} catch (Exception e) {
			if(!(e instanceof FileNotFoundException)){				
				log.exception(e);
				for(Throwable t : e.getSuppressed()){
					log.exception(t);
				}
			} else {
				log.debug("File containing my node specs do not exists.");
			}
			return null;
		}
		if(read != null){
			if(read instanceof DhtLocalNode){
				DhtLocalNode node = (DhtLocalNode) read;
				log.verboseDebug("DhtLocalNode infos read from file.");
				return node;
			}
		}
		return null;
	}
	
	private void saveMyNodeToFile(){
		String path = props.getDhtBaseDir()+"myNode.ser";
		File nodeFile = new File(path);
		File parent = nodeFile.getParentFile();
		if(!parent.exists()){
			parent.mkdirs();
			log.verboseDebug("Built dirs for node info");
		}
		try (   FileOutputStream fos = new FileOutputStream(nodeFile);
				ObjectOutputStream oos = new ObjectOutputStream(fos)){
			oos.writeObject(myLocalNode);			
		} catch (Exception e) {
			log.exception(e);
			for(Throwable t : e.getSuppressed()){
				log.exception(t);
			}
		}
		log.verboseDebug("My node infos saved to file.");
	}
	
	private DhtAddress resolveMyAddress(){
		InetAddress myIp = null;
		int maximumAttempts = 4;
		while(maximumAttempts > 0 && myIp == null){
			log.debug("Try to resolve my ip address...");
			try {
				myIp = RAWServiceUtils.resolveIP();
			} catch (Exception e) {
				log.exception(e);
			}
			maximumAttempts--;
		}
		DhtAddress myAddress = new DefaultDhtAddress(myIp, props.getUdpListeningSocket(), props.getTcpListeningSocket());
		return myAddress;	
	}
	
	private DhtNode createDhtNode(DhtAddress address, KeyPair keyPair){
		DhtHasher hasher = new DefaultDhtHasher();
		byte[] base = new byte[42];
		Random rand = new Random(System.currentTimeMillis());
		rand.nextBytes(base);
		DhtID id = hasher.hashBytes(base);
		DhtNode node = new DefaultDhtNode(id, keyPair.getPublic(), address);
		return node;
	}
	
	private void resolveMyNodeTransaction(DhtAddress address, KeyPair keyPair){
//		DhtNode myNode = createDhtNode(address, keyPair);
		Transaction transaction = null;
		try {
			transaction = proofOfWorkManager.blockingGetToken(currentSeedBlockNumber());
		} catch (UnexpectedException | NoSuchElementException e) {
			log.exception(e);
		}
		long myBlockNumber = -1;
		if(transaction!= null){
			myBlockNumber = chainCore.transcationLastOccurrence(transaction);
			if(myBlockNumber == -1){
				chainCore.submitTransaction(transaction);
			}			
		}
		DhtNode myNode = getNode();
		myLocalNode = new DefaultDhtLocalNode(myNode, transaction, keyPair, myBlockNumber);
	}
	
	private void startListener(){
		Future<Void> listenerFuture = pool.submit(listener);
		try {
			listenerFuture.get(1, TimeUnit.NANOSECONDS);
		} catch (InterruptedException | ExecutionException e) {
			log.exception(e);
		} catch (TimeoutException e) {
			log.debug("Listener started on its own thread.");
		}
	}
	
	public static DhtCore getCore() {
		if(core == null){
			core = new DefaultDhtCore();
		}
		return core;
	}
	
	protected synchronized void updateTransaction(Transaction newTransaction, long newTransactionBlockNumber){
		DhtNodeExtended nodeExtended;
		try {
			nodeExtended = myLocalNode.getNodeExtended();
		} catch (IncoherentTransactionException	| IncompleteNodeExtendedException e) {
			log.exception(e);
			return;
		}
		nodeExtended.updateTransaction(newTransaction, newTransactionBlockNumber);
		KeyPair keys = myLocalNode.getKeyPair();
		myLocalNode = new DefaultDhtLocalNode(nodeExtended, keys);
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#lookupInTable(raw.dht.DhtID)
	 */
	@Override
	public Collection<DhtNodeExtended> lookupInTable(DhtID id) {
		log.verboseDebug("Lookup for: "+id);
		return routingTable.findClosest(id);
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#store(raw.dht.DhtKey, raw.dht.DhtValue)
	 */
	@Override
	public boolean store(final DhtKey key, final DhtValue value) {
		log.verboseDebug("Started storing procedures...");
		checkIfRunningOrWait();
		log.verboseDebug("...ready to store "+key+" / "+value);
		Collection<DhtNodeExtended> nodes = searcher.lookup(key);
		boolean stored = false;
		DhtNodeExtended myNode;
		try {
			myNode = myLocalNode.getNodeExtended();
			if(myNode != null && nodes.contains(myNode)){
				log.verboseDebug("Store is also gonna be on my own node!");
				if(isOldWorker(myNode)){
					log.verboseDebug("My node is old enought to accept a key-value store.");
					stored = (stored || keyHolder.store(key, value));
				}
				nodes.remove(myNode);
			}
		} catch (IncoherentTransactionException | IncompleteNodeExtendedException e1) {
			log.exception(e1);
		}
		ArrayList<Callable<Boolean>> storeCallables = new ArrayList<Callable<Boolean>>();
		for(final DhtNodeExtended node : nodes){
			storeCallables.add(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					log.verboseDebug("Sending store request to: "+node);
					InetSocketAddress address = node.getAddress().getTcpSocketAddress();
					try (Socket sock = new Socket(address.getAddress(), address.getPort());){
						sock.setSoTimeout(DhtConstants.TIMEOUT_MILLISECONDS);
						StoreMessage storeRequest = new StoreMessage(getNodeExtended(), key, value);
						ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
						oos.writeObject(storeRequest);
						ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
						Object received = ois.readObject();
						if(!(received instanceof StoreMessage)){
							log.verboseDebug("Received message is not of the right type. Aborting.");
							return Boolean.FALSE;
						}
						StoreMessage reply = (StoreMessage) received;
						if(reply.getMessageType() != MessageType.STORE_REPLY){
							log.verboseDebug("Received message is not a store reply. Aborting.");
							return Boolean.FALSE;
						}
//						if(!DhtProofOfWorkUtils.checkTokenValidity(node.getID(), reply.getSeedToken())){
//							log.verboseDebug("Node "+node+" did not provide a valid proof of work. Rejecting store reply status.");
//							return Boolean.FALSE;
//						}
						return reply.isAccepted();
					} catch (Exception e) {
						log.exception(e);
						for(Throwable t : e.getSuppressed()){
							log.exception(t);
						}
						return Boolean.FALSE;
					}
				}
			});
		}
		
		List<Future<Boolean>> storeReplies = null;
		try {
			storeReplies = pool.invokeAll(storeCallables);
		} catch (InterruptedException e) {
			log.exception(e);
		}
		
		if(storeReplies != null){			
			for(Future<Boolean> reply : storeReplies){
				try {
					stored = stored || reply.get().booleanValue();
				} catch (InterruptedException | ExecutionException e) {
					log.exception(e);
				}
			}
		}
		return stored;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#search(raw.dht.DhtKey)
	 */
	@Override
	public Collection<DhtValue> search(DhtKey key) {
		checkIfRunningOrWait();
		return searcher.findValues(key);
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#getThreadPool()
	 */
	@Override
	public ExecutorService getThreadPool() {
		return pool;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#getNode()
	 */
	@Override
	public DhtNode getNode() {
		return myLocalNode.getNode();
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#getNode()
	 */
	@Override
	public DhtNodeExtended getNodeExtended() throws IncoherentTransactionException, IncompleteNodeExtendedException {
		return myLocalNode.getNodeExtended();
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#getKeyHolder()
	 */
	@Override
	public DhtKeyHolder getKeyHolder() {
		return keyHolder;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#getCurrentSeed()
	 */
	@Override
	public byte[] getCurrentSeed() {
		log.verboseDebug("Requested current seed.");
		
//		long seedNumberBeforeUpdate = currentSeedBlockNumber;
		if(currentSeedAccessedAtBlockNumber != currentSeedBlockNumber() || currentSeed == null){ //if block number changes here it will show up
			HashValue hash = chainCore.getBlockByNumber(currentSeedBlockNumber()).getHeader().hash();
			currentSeedAccessedAtBlockNumber = currentSeedBlockNumber();
			currentSeed = hash.toByteArray();
			log.verboseDebug("New seed is: "+hash.toHexString());
		}

		log.verboseDebug("Seed block number is #"+currentSeedAccessedAtBlockNumber);
		return currentSeed;
	}
	
	private long currentSeedBlockNumber(){
		long now = System.currentTimeMillis();
		if((now - currentSeedBlockNumberAccess > DhtConstants.SEED_REQUESTS_REFRESH_MILLISECONDS) || currentSeedBlockNumber == -1){
			currentSeedBlockNumber = DhtUtils.findFirstSeedNumberFromBlockNumber(getLastBlockNumber());
			currentSeedBlockNumberAccess = System.currentTimeMillis();
		}
		return currentSeedBlockNumber;
	}

	/**
	 * This method will search for a new
	 * upcoming seed. Thiss will be used to
	 * precompute a proof of work token.
	 * 
	 * @return the next seed coming up or <code>null</code> if next seed block is not yet available
	 */
	protected byte[] getFutureSeed(){
		long now = System.currentTimeMillis();
		if((now - futureSeedAccess > DhtConstants.SEED_REQUESTS_REFRESH_MILLISECONDS) || futureSeed == null){
			BlockHeader nextSeedBlockHeader = chainCore.getBlockHeaderByNumber(getFutureSeedBlockNumber());
			if(nextSeedBlockHeader == null){
				futureSeed = null;
			} else {
				futureSeed = nextSeedBlockHeader.hash().toByteArray();
			}
			futureSeedAccess = System.currentTimeMillis();
		}
		return futureSeed;
	}
	
	protected long getFutureSeedBlockNumber(){
		long nextSeedNumber = currentSeedBlockNumber() + DhtConstants.SEED_BLOCK_NUMBER_MODULE;
		return nextSeedNumber;
	}
	
	private long lastSeedBlockNumber(){
		long now = System.currentTimeMillis();
		if((now - lastSeedBlockNumberAccess > (DhtConstants.SEED_REQUESTS_REFRESH_MILLISECONDS)/3) || lastSeedBlockNumber == -1){
			lastSeedBlockNumber = DhtUtils.findFirstSeedNumberFromBlockNumber(currentSeedBlockNumber() - 1);
			lastSeedBlockNumberAccess = System.currentTimeMillis();
		}
		return lastSeedBlockNumber;
	}
	
	/**
	 * This method return the last (for dht purposes)
	 * {@link Block} number.
	 *  
	 * @return the last {@link Block} number for dht purposes
	 */
	private long getLastBlockNumber(){
		return DhtUtils.getLastBlockNumber(getLastBlockNumberInchain());
	}
	
	/**
	 * @return the last {@link Block} number
	 */
	private long getLastBlockNumberInchain(){
		long now = System.currentTimeMillis();
		if((now - lastBlockNumberInChainAccess > DhtConstants.SEED_REQUESTS_REFRESH_MILLISECONDS) || lastBlockNumberInChain == -1 ){
			lastBlockNumberInChain = chainCore.getLastBlockHeaderInChain().getBlockNumber();
			lastBlockNumberInChainAccess = System.currentTimeMillis();
		}
		return lastBlockNumberInChain;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#getCurrentHasher()
	 */
	@Override
	public DhtHasher getCurrentHasher() {
//		checkIfRunningOrWait();
		return new DefaultDhtHasher(getCurrentSeed());
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#getHasherFromSeedNumber(long)
	 */
	@Override
	public DhtHasher getHasherFromSeedNumber(long seedBlockNumber) throws IllegalArgumentException {
//		checkIfRunningOrWait();
		return DhtUtils.getHasherFromSeedNumber(seedBlockNumber, chainCore);
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#isOldWorker(raw.dht.DhtNode, raw.blockChain.api.Transaction)
	 */
	@Override
	public boolean isOldWorker(DhtNodeExtended node) {
		if(!isCorrectlyOld(node)){
			log.verboseDebug("Node "+node+" did not provide a good block number.");
			return false;
		}
		if(!chainCore.checkTransactionIsInBlock(node.getTransactionBlockNumber(), node.getTransaction())){
			log.verboseDebug("Node "+node+" advertised transaction block didn't check out.");
			return false;
		}
		if(!TransactionUtils.isValid(node.getTransaction(), chainCore)){
			log.verboseDebug("Provided transaction is not a valid one.");
			return false;
		}
		return true;
	}
	
	/**
	 * Only checks if a referenced block number
	 * would make a {@link Transaction} "old enough"
	 * but not "too much old".
	 *   
	 * @param blockNumber
	 * @return
	 */
	public boolean isCorrectlyOld(DhtNodeExtended node){
		ArrayList<DhtNodeExtended> list = new ArrayList<>();
		list.add(node);
		HashMap<DhtNodeExtended, Boolean> set = areCorrectlyOld(list);
		if(set != null && set.containsKey(node)){
			return set.get(node).booleanValue();
		}
		return false;
	}
	
	/**
	 * Checks block numbers provided by multiple {@link DhtNodeExtended}
	 * and makes sure that associated {@link Transaction}s are "old enough"
	 * but not "too much old".
	 *   
	 * @param nodes a set of {@link DhtNodeExtended}
	 * @return an {@link HashMap} where each element of <code>nodes</code> is coupled with a boolean value
	 */
	public HashMap<DhtNodeExtended, Boolean> areCorrectlyOld(Collection<DhtNodeExtended> nodes){
//		long lastSeed = lastSeedBlockNumber();
//		long olderAcceptableBlockNumber = DhtUtils.decreaseBlockNumberByEtaSeeds(lastSeed);
		
		HashMap<DhtNodeExtended, Boolean> returnable = new HashMap<>();
		
		long retrievedCurrentSeedBlockNumber = currentSeedBlockNumber();
		long retrievedLastSeedBlockNumber = lastSeedBlockNumber();
		
		for(DhtNodeExtended node : nodes){
			long blockNumber = node.getTransactionBlockNumber();
			if(blockNumber <1 ){
				log.verboseDebug("Node "+node+" is autenticated with an invalid block number ("+blockNumber+").");
				returnable.put(node, false);
				continue;
			}
//			if(blockNumber >= retrievedLastSeedBlockNumber){
			if(isTooYoung(blockNumber)){
//				log.verboseDebug("Node "+node+" is autenticated with block #"+blockNumber+" while seed is "+currentSeedBlockNumber()+" and last seed was "+lastSeedBlockNumber());
				log.verboseDebug("Node "+node+" is autenticated with block #"+blockNumber+" while seed is "+retrievedCurrentSeedBlockNumber+" and last seed was "+retrievedLastSeedBlockNumber);
				returnable.put(node, false);
				continue;
			}
//			if(blockNumber < olderAcceptableBlockNumber){
			if(isTooOld(blockNumber)){
				log.verboseDebug("Node "+node+" has grown too old!");
				returnable.put(node, false);
				continue;
			}
			returnable.put(node, true);
		}
		
		return returnable;
	}
	
	protected boolean isTooYoung(long blockNumber){
		long lastSeed = lastSeedBlockNumber();
		return (blockNumber >= lastSeed);
	}
	
	protected boolean isTooOld(long blockNumber){
		long lastSeed = lastSeedBlockNumber();
		long olderAcceptableBlockNumber = DhtUtils.decreaseBlockNumberByEtaSeeds(lastSeed);
		return (blockNumber < olderAcceptableBlockNumber);
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#isThisNodeOld()
	 */
	@Override
	public boolean isThisNodeOld() {
		try {
//			return isOldWithoutSignature(getNodeExtended(), getBlockNumber());
			DhtNodeExtended myNode = getNodeExtended();
			if(myNode == null){				
				return false;
			}
			return isOldWorker(myNode);
		} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
			log.exception(e);
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#start()
	 */
	@Override
	public void start() {
		while (!this.chainCore.isNodeUp()) {
			log.debug("Waiting for block chain client to be up.");
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
				log.exception(e);
			}
		}
		
		log.info("Starting DHT core.");
		
		loadDhtNodesFromFile();
		
		startNodesInfoServer();
		
		startTransactionMonitoringThread();
		
		startListener();
		
		started = true;
		
		log.verboseDebug("DHT core is running with the exception of ProofOfWorkManager");
		
		startProofOfWorkManagerThread(); //note: this is a blocking call
		
		log.verboseDebug("DHT core is fully started!");
	}
	
	private void startNodesInfoServer(){
		if(props.isNodesInfoServerRunning()){
			log.verboseDebug("Starting nodes' info server.");
			nodesInfoServer = new NodesInfoServer();
			Future<Void> serverFuture = pool.submit(nodesInfoServer);
			try {
				serverFuture.get(1, TimeUnit.NANOSECONDS);
			} catch (InterruptedException | ExecutionException e) {
				log.exception(e);
			} catch (TimeoutException e){
				log.verboseDebug("Nodes' info server is running in its own thread.");
			}
		} else {
			log.verboseDebug("No need to start nodes' info server. ");
		}
	}
	
	private class TransactionMonitor implements Runnable{
		
		private Transaction myTransaction;
		private boolean running;
		private boolean done;
		
		public TransactionMonitor(Transaction transaction) {
			myTransaction = transaction;
			running = true;
			done = false;
		}
		
		@Override
		public void run() {
			log.debug("First: quick check for my transaction.");
			long transactionBlockNumber = transactionAlreadySetUp();
			if(transactionBlockNumber == -1){
				log.verboseDebug("The block number was not retrieved by my local node informations.");
				transactionBlockNumber = chainCore.transcationLastOccurrence(myTransaction);
			}
			if(transactionBlockNumber == -1){
				log.verboseDebug("Quick check resulted in -1.");
				chainCore.submitTransaction(myTransaction);
				try {
					Thread.sleep(BlockChainConstants.EXPECTED_SECONDS_PER_BLOCK * 1000);
				} catch (InterruptedException e) {
					log.exception(e);
				}
				if(!running){
					done = true;
					return;
				}
				log.debug("Starting to monitor for my transaction");
				transactionBlockNumber = chainCore.transcationLastOccurrence(myTransaction);
				while (transactionBlockNumber == -1 && running) {
					log.verboseDebug("My transaction block number is: "+transactionBlockNumber);
					try {
						Thread.sleep(BlockChainConstants.EXPECTED_SECONDS_PER_BLOCK * 1000 / 3);
					} catch (InterruptedException e) {
						log.exception(e);
					}
					if(!running){
						log.verboseDebug("Running flag is off. Exiting loop.");
						done = true;
						return;
					}
					transactionBlockNumber = chainCore.transcationLastOccurrence(myTransaction);
				}
				if(transactionBlockNumber == -1 || !running){
					log.verboseDebug("My transaction still -1 or running flag has been turned to false. Returning.");
					done = true;
					return;
				}
			}
			DhtNode myNode = myLocalNode.getNode();
			KeyPair myKeyPair = myLocalNode.getKeyPair();
//			long myBlockNumber = transactionBlockNumber;
//			myLocalNode = new DefaultDhtLocalNode(myNode, myKeyPair, myBlockNumber);
			DhtNodeExtended myNodeExtended;
			try {
				myNodeExtended = new DefaultDhtNodeExtended(myNode, myTransaction, transactionBlockNumber);
			} catch (IncoherentTransactionException e) {
				log.exception(e);
				done = true;
				return;
			}
			myLocalNode = new DefaultDhtLocalNode(myNodeExtended, myKeyPair);
//			log.verboseDebug("My transaction block number set to: "+myBlockNumber);
			log.verboseDebug("My transaction block number set to: "+myLocalNode.getBlockNumber());
			DhtID id = myLocalNode.getNode().getID();
			log.verboseDebug("Asking searcher ("+searcher+") a lookup for: "+id);
			Collection<DhtNodeExtended> nodes = searcher.lookup(id);
			log.verboseDebug("Lookup returned "+nodes.size()+" nodes.");
			for(DhtNodeExtended node : nodes){
				if(!node.equals(myNode)){					
					log.verboseDebug("Checking in with node "+node);
					if(pinger.sendPing(node)){
						log.verboseDebug(node+" replyed to ping. Inserting it in routing table.");
//						routingTable.insertNode(node); //insertion should have been performed by sendPing method
					} else {
						log.verboseDebug(node+" DID NOT reply to ping. Removing it from routing table.");
						routingTable.removeNode(node);
					}
				}
			}
			startPinger();
			log.verboseDebug("Starting keys migrator");
			searcher.startKeysMigrator();
			done = true;
		}	
		
		private long transactionAlreadySetUp(){
			if(myLocalNode.getBlockNumber() == -1){
				log.verboseDebug("My local node block number is unknown.");
				return -1;
			}
			log.verboseDebug("My local node block number is KNOWN.");
			if(chainCore.checkTransactionIsInBlock(myLocalNode.getBlockNumber(), myTransaction)){
				log.verboseDebug("My already known block number is still valid with my transaction");
				return myLocalNode.getBlockNumber();
			}
			log.verboseDebug("My already known block number is no more valid with my transaction");
			return -1;
		}
		
		public void stop() {
			running = false;
		}
		
		public boolean isDone(){
			return done;
		}
	}
	
	private void startTransactionMonitoringThread(){
//		Transaction myTransaction = DhtUtils.dhtNodeToTransaction(myLocalNode.getNode());
		Transaction myTransaction;
		try {
			myTransaction = myLocalNode.getNodeExtended().getTransaction();
		} catch (IncoherentTransactionException	| IncompleteNodeExtendedException | NullPointerException e1) {
			log.exception(e1);
			resolveMyNodeTransaction(myLocalNode.getNode().getAddress(), myLocalNode.getKeyPair());
			try {
				myTransaction = myLocalNode.getNodeExtended().getTransaction();
			} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
				log.exception(e);
				return;
			}
		}
//		chainCore.submitTransaction(myTransaction); //moved in transaction monitor thread.
		transactionMonitor = new TransactionMonitor(myTransaction);
		Future<?> monitor = pool.submit(transactionMonitor);
		try {
			monitor.get(1, TimeUnit.NANOSECONDS);
		} catch (InterruptedException | ExecutionException e) {
			log.exception(e);
		} catch (TimeoutException e) {
			log.debug("Transaction monitor is running on its own.");
		}
	}
	
	private void startProofOfWorkManagerThread(){
		Future<?> starterTask = pool.submit(new Runnable() {
			
			@Override
			public void run() {
				if(transactionMonitor != null){
					while(!transactionMonitor.isDone()){
						log.verboseDebug("Waiting for TransactionMonitor to complete. (Pause 20 seconds)");
						try {
							Thread.sleep(20*1000);
						} catch (InterruptedException e) {
							log.exception(e);
						}
					}
				}
				log.verboseDebug("Beginning starting procedures for core's ProofOfWorkManager.");
				try {
					log.verboseDebug("Asking transaction for current seed");
					proofOfWorkManager.blockingGetToken(currentSeedBlockNumber());
				} catch (UnexpectedException e) {
					log.exception(e);
				}		
				try {
					log.verboseDebug("Asking transaction for future seed");
					proofOfWorkManager.blockingGetToken(getFutureSeedBlockNumber());
				} catch (UnexpectedException e) {
					log.exception(e);
				} catch (NoSuchElementException e) {
					log.verboseDebug("Future seed was null. Skipping blocking token generation.");
				}
				log.verboseDebug("Starting ProofOfWorkManager autonomous thread.");
				proofOfWorkManager.startSeedsMonitor();				
			}
		});
		
		try {
			starterTask.get(1, TimeUnit.NANOSECONDS);
		} catch (InterruptedException | ExecutionException e) {
			log.exception(e);
		} catch (TimeoutException e) {
			log.verboseDebug("A thread starting ProofOfWOrkManager is running on its own.");
		}
	}
	
	private void startPinger() {
		Future<Void> pingerFuture = pool.submit(pinger);
		try {
			pingerFuture.get(1, TimeUnit.NANOSECONDS);
		} catch (InterruptedException | ExecutionException e) {
			log.exception(e);
		} catch (TimeoutException e) {
			log.debug("Pinger is running on its own thread.");
		}
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#stop()
	 */
	@Override
	public void stop() {
		if(transactionMonitor != null){
			transactionMonitor.stop();
		}
		if(nodesInfoServer != null){
			nodesInfoServer.stop();
		}
		listener.stop();
		pinger.stop();
		searcher.stopKeysMigrator();
		saveMyNodeToFile();
		saveDhtNodesToFile();
		started = false;
		log.verboseDebug("Dht Core has been asked to stop.");
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#isStarted()
	 */
	@Override
	public boolean isStarted() {
		return started;
	}
	
	private void checkIfRunningOrWait(){
		while (!isStarted()) {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				log.exception(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#getBlockNumber()
	 */
	@Override
	public long getBlockNumber() {
		return myLocalNode.getBlockNumber();
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#getRoutingTable()
	 */
	@Override
	public RoutingTable getRoutingTable() {
		return routingTable;
	}

	private void loadDhtNodesFromFile() {
		DhtNodeAddressBookFile addressBook = new DhtNodeAddressBookFile();
		Collection<DhtNodeExtended> nodes = addressBook.getNodes();
		for(DhtNodeExtended node : nodes){
			if(isOldWorker(node)){				
				log.verboseDebug("Old worker "+node + " retrieved from address file. Adding it to routing table.");
				boolean inserted = routingTable.insertNode(node);
				log.verboseDebug("Insertion in routing table of "+node+" result = "+inserted);
			}
		}
	}
	
	private void saveDhtNodesToFile(){
		Collection<DhtNodeExtended> nodes = routingTable.getFullSetOfNodes(false);
		DhtNodeAddressBookFile addressBook = new DhtNodeAddressBookFile(nodes);
		addressBook.saveInFile();
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#lookup(raw.dht.DhtID)
	 */
	@Override
	public Collection<DhtNodeExtended> lookup(DhtID id) {
		checkIfRunningOrWait();
		return searcher.lookup(id);
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#pingNode(raw.dht.DhtNode)
	 */
	@Override
	public boolean pingNode(DhtNodeExtended node) {
		if(node.equals(myLocalNode.getNode())){
			log.verboseDebug("No sense to send a ping to my own node.");
			return false;
		}
		checkIfRunningOrWait();
		return pinger.sendPing(node);
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtCore#getKeyPair()
	 */
	@Override
	public KeyPair getKeyPair() {
		return myLocalNode.getKeyPair();
	}

}
