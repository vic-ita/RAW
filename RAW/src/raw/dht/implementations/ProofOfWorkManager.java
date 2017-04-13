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
package raw.dht.implementations;

import java.rmi.UnexpectedException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.blockChain.BlockChainCore;
import raw.blockChain.api.BlockChainConstants;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultTransaction;
import raw.blockChain.api.implementations.utils.TransactionUtils;
import raw.blockChain.services.implementations.DefaultBlockChainCore;
import raw.dht.DhtCore;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.implementations.utils.DhtUtils;
import raw.logger.Log;


public class ProofOfWorkManager {
	
	private Queue<TokenPair> tokens;
	private List<TokenPair> researching;
	
	private DhtID baseId;
	private PublicKey basePublicKey;
	
	private SeedsMonitor monitor;
	
	private DhtCore dhtCore;
	
	private Log log;
	
	public ProofOfWorkManager(DhtID baseId, PublicKey basePublicKey, DhtCore dhtCore) {
		tokens = new ConcurrentLinkedQueue<>();
		researching = Collections.synchronizedList(new ArrayList<TokenPair>());
		
		this.baseId = baseId;
		this.basePublicKey = basePublicKey;
		
		monitor = null;
		
		this.dhtCore = dhtCore;
		
		log = Log.getLogger();
		
		log.verboseDebug("ProofOfWorkManager constructed for id: "+baseId.toHexString());
	}
	
	/**
	 * Return if present a {@link Transaction} valid with a certain seed identified
	 * by a seed block number or {@link NoSuchElementException} otherwise
	 * @param seedBlockNumber a seed block number
	 * @return the requested {@link Transaction} <b>if existent</b> in this collection
	 */
	public Transaction getToken(long seedBlockNumber) throws NoSuchElementException{
		
		if(!DhtUtils.isSeedBlockNumber(seedBlockNumber)){
			throw new NoSuchElementException(seedBlockNumber+" is not a valid seed block number!");
		}
//		log.verboseDebug("Requested token for seed: "+(new DefaultHashValue(seed)).toHexString());
		log.verboseDebug("Requested token");
		TokenPair searched = new TokenPair(seedBlockNumber, null);
		synchronized (tokens) {			
			if(tokens.contains(searched)){
				for(TokenPair token : tokens){
					if(seedBlockNumber == token.getSeedBlockNumber() && token.getTransaction() != null){
						synchronized (researching) {
							if(researching.contains(searched)){
								researching.remove(searched);
							}
						}
						log.verboseDebug("Transaction found: "+token.getTransaction());
						return token.getTransaction();
					}
				}
			}
		}
		log.verboseDebug("Cannot find any valid transaction.");
		throw new NoSuchElementException("Transaction for requested seed block number is not present.");			
	}
	
	/**
	 * Attempts to generate a proof of work transaction
	 * using the pair of the base {@link DhtID} for
	 * <code>this</code> object and a provided random seed block number.
	 * If such a token is discovered it will be retrievable 
	 * through {@link ProofOfWorkManager#getToken(long)}
	 *
	 * @param seedBlockNumber
	 * @throws UnexpectedException 
	 */
	public void requestTokenGeneration(long seedBlockNumber) throws UnexpectedException{
		if(!DhtUtils.isSeedBlockNumber(seedBlockNumber)){
			log.verboseDebug("Skipping request: seed block number provided is invalid.");
			return;
		}
		TokenPair checking = new TokenPair(seedBlockNumber, null);
		synchronized (tokens) {
			if(tokens.contains(checking)) {
				log.verboseDebug("Transaction for seed block number "+seedBlockNumber+" is ALREADY KNOWN.");
				return;
			}
		}
		synchronized (researching) {
			if(researching.contains(checking)){
				log.verboseDebug("Transaction for seed block number "+seedBlockNumber+" is being searched.");
				return;
			}
		}
		
		try {
			generateToken(seedBlockNumber, false);
			log.verboseDebug("A transaction generation has been issued for seed block number "+seedBlockNumber);
		} catch (InterruptedException | ExecutionException e) {
			log.exception(e);
		}
	}
	
	/**
	 * Starts the thread computing a valid {@link Transaction}
	 * for a particular "seed epoch".
	 * Can be made a blocking or a non-blocking call.
	 * 
	 * @param seedBlockNumber
	 * @param blocking
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws UnexpectedException 
	 */
	private Transaction generateToken(long seedBlockNumber, boolean blocking) throws InterruptedException, ExecutionException, UnexpectedException{
		TokenPair checking = new TokenPair(seedBlockNumber, null);
		synchronized (tokens) {
			if(tokens.contains(checking)) {
				return getToken(seedBlockNumber);
			}
		}
//		ExecutorService pool = DefaultDhtCore.getCore().getThreadPool();
		ExecutorService pool = dhtCore.getThreadPool();
		Future<TokenPair> task = pool.submit(new MinerTask(baseId, seedBlockNumber, basePublicKey, dhtCore));
		
		if(blocking){
			log.verboseDebug("Starting a blocking token generation.");
			TokenPair returned = task.get();
			if(returned == null){
				throw new UnexpectedException("Requested token appears to be unexistent.");
			}
			return returned.getTransaction();			
		} else {			
			try {
				task.get(1, TimeUnit.NANOSECONDS);
				return null;
			} catch (TimeoutException e) {
				log.verboseDebug("Token search task is running on its own.");
				return null;
			}
		}
		
	}
	
	/**
	 * This method return the requested {@link Transaction} 
	 * possibly blocking until such a {@link Transaction} is
	 * generated 
	 * 
	 * @param seedBlockNumber the seed block number coupled with requested {@link Transaction}
	 * @return the requested {@link Transaction}
	 * @throws UnexpectedException 
	 * @throws NoSuchElementException
	 * 
	 */
	public Transaction blockingGetToken(long seedBlockNumber) throws UnexpectedException, NoSuchElementException{
		if(!DhtUtils.isSeedBlockNumber(seedBlockNumber)){
			throw new NoSuchElementException("Provided seed block number is not valid");
		}
		try {
			log.verboseDebug("Received blocking request for transaction.");
			return getToken(seedBlockNumber);			
		} catch (NoSuchElementException e) {
			try {
				return generateToken(seedBlockNumber, true);
			} catch (InterruptedException | ExecutionException e1) {
				throw new UnexpectedException("Transaction generation ended unexpectedly.");
			}
		}
	}
	
	public void startSeedsMonitor(){
		if(monitor == null){
			monitor = new SeedsMonitor();
			ExecutorService pool = DefaultDhtCore.getCore().getThreadPool();
			Future<?> monitorTask = pool.submit(monitor);
			try {
				monitorTask.get(1, TimeUnit.NANOSECONDS);
			} catch (InterruptedException | ExecutionException e) {
				log.exception(e);
			} catch (TimeoutException e) {
				log.verboseDebug("Seeds monitor is running on its thread");
			}
		}
	}
	
	public void stopSeedsMonitor(){
		if(monitor != null){
			log.verboseDebug("Issuing stop request to seeds monitor.");
			monitor.stop();
			monitor = null;
		}
	}
	
	private class TokenPair{
		private long seedBlockNumber;
		private Transaction trasnaction;
		
		public TokenPair(long seedBlockNumber, Transaction transaction) {
			this.seedBlockNumber = seedBlockNumber;
			this.trasnaction = transaction;
		}

		/**
		 * @return the seed block number
		 */
		public long getSeedBlockNumber() {
			return seedBlockNumber;
		}

		/**
		 * @return the token
		 */
		public Transaction getTransaction() {
			return trasnaction;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			HashCodeBuilder builder = new HashCodeBuilder();
			builder.append(seedBlockNumber);
			if(trasnaction != null){
				builder.append(trasnaction);
			}
			return builder.toHashCode();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof TokenPair)){
				return false;
			}
			return (seedBlockNumber == ((TokenPair) obj).getSeedBlockNumber());
		}
	}
	
	private class MinerTask implements Callable<TokenPair>{
		
		private DhtID id;
		private long seedBlockNumber;
		private PublicKey pubKey;
		
		private DhtCore dhtCore;
		
		public MinerTask(DhtID id, long seedBlockNumber, PublicKey pubKey, DhtCore dhtCore) {
			this.id = id;
			this.seedBlockNumber = seedBlockNumber;
			this.pubKey = pubKey;
			
			this.dhtCore = dhtCore;
		}

		@Override
		public TokenPair call() throws Exception {
			log.verboseDebug("A search for a new proof of work transaction is beginning. (Seed block number is: "+seedBlockNumber+")");
			synchronized (researching) {
				researching.add(new TokenPair(seedBlockNumber, null));
			}
			long nonce = Long.MIN_VALUE;
			boolean found = false;
			
			log.verboseDebug("Target for this search will be: "+TransactionUtils.getTarget().toHexString());

			DhtHasher hasher = dhtCore.getHasherFromSeedNumber(seedBlockNumber);
			if(hasher == null){
				log.verboseDebug("Cannot retrieve a valid hasher. Abort search.");
				return null;
			}
			
			log.verboseDebug("Beginning search loop.");
			if(TransactionUtils.isValid(id, nonce, hasher)){
				found = true;
			} else {				
				nonce++;
			}
			while((nonce != Long.MIN_VALUE) && !found){
//				log.verboseDebug("Nonce: "+nonce);
				if(TransactionUtils.isValid(id, nonce, hasher)){
					found = true;
				} else {					
					nonce++;
				}
			}
			log.verboseDebug("Search loop ended.");
			
			if(found){
				Transaction transaction = new DefaultTransaction(id, nonce, seedBlockNumber, pubKey);
				TokenPair foundToken = new TokenPair(seedBlockNumber, transaction);
				synchronized (tokens) {
					if(tokens.size() > 10){
						tokens.poll();
					}
					tokens.add(foundToken);
				}
				synchronized (researching) {
					researching.remove(foundToken);
				}
				log.verboseDebug("A token was found: "+foundToken.getTransaction());
				return foundToken;
			}
			return null;
		}
		
	}
	
	private class SeedsMonitor implements Runnable{
		
		private boolean running;
		private DefaultDhtCore core;
		private BlockChainCore blockChain;
		
		private HashSet<Transaction> submitted;
		private HashMap<Transaction, Long> stored;
		
		public SeedsMonitor() {
			core = (DefaultDhtCore) DefaultDhtCore.getCore();
			blockChain = DefaultBlockChainCore.getBlockChainCore();
			
			submitted = new HashSet<>();
			stored = new HashMap<>();
			
			running = true;
		}

		@Override
		public void run() {
//			int increment = (BlockChainConstants.EXPECTED_SECONDS_PER_BLOCK / 4)*3;
			int increment = BlockChainConstants.EXPECTED_SECONDS_PER_BLOCK / 2;
//			long timeForNextBlock = time() + increment;
			long timeForNextBlock = time() + 20;
			log.verboseDebug("Starting seeds monitor loop.");
			while(running){
				if(time() >= timeForNextBlock){
					timeForNextBlock += increment;
					
					checkSubmittedTransactions();
					
					chooseSuitableStoredTransaction();
					
					newSeedTransaction();
					
				}
				try {
					Thread.sleep(45000);
				} catch (InterruptedException e) {
					log.exception(e);
				}
			}
			return;			
		}
		
		/**
		 * generate (if necessary) a transaction
		 * valid for a future seed
		 */
		private void newSeedTransaction(){
			log.verboseDebug("Asking core if future seed is known");
			byte[] futureSeed = core.getFutureSeed();
			if(futureSeed != null){
				log.verboseDebug("Future seed is known to core.");
				long nextSeedBlockNumber = core.getFutureSeedBlockNumber();
				try {
					Transaction generated = getToken(nextSeedBlockNumber);
					log.verboseDebug("A transaction for seed #"+nextSeedBlockNumber+" has already been found: "+generated);
					blockChain.submitTransaction(generated);
					log.verboseDebug("Submitting it again for good measure.");
					submitted.add(generated);
				} catch (NoSuchElementException e) {
					try {
						log.verboseDebug("The transaction was not available, generating it.");
						try {
							Transaction generated = generateToken(nextSeedBlockNumber, true);
							log.verboseDebug("Transaction for seed #"+nextSeedBlockNumber+" has been computed: "+generated);
							blockChain.submitTransaction(generated);
							log.verboseDebug("Transaction submitted ("+generated+").");
							submitted.add(generated);

						} catch (InterruptedException | ExecutionException e1) {
							log.verboseDebug("Unable to compute a transaction.");
						}
					} catch (UnexpectedException e1) {
						log.exception(e1);
					}
				}						
			}
			else{
				log.verboseDebug("Future seed is not yet known.");
			}
		}
		
		/**
		 * checks if submitted transactions
		 * have been incorporated in the main
		 * chain
		 */
		private void checkSubmittedTransactions(){
			if(submitted.size() > 0){
				log.verboseDebug("I have transactions to check.");
				ArrayList<Transaction> toBeRemoved = new ArrayList<>();
				for(Transaction transaction : submitted){
					log.verboseDebug("Checking transaction: "+transaction);
					long occurrence = blockChain.transcationLastOccurrence(transaction);
					if(occurrence != -1){
						log.verboseDebug("The transaction is stored in block #"+occurrence);
						stored.put(transaction, occurrence);
						toBeRemoved.add(transaction);
					} else {
						log.verboseDebug("This transaction is not into any block ("+transaction+").");
						blockChain.submitTransaction(transaction);
						log.verboseDebug("Transaction has been re-submitted.");
					}
				}
				submitted.removeAll(toBeRemoved);
			} else {
				log.verboseDebug("There are no unmanaged transactions submitted.");
			}
		}
		
		/**
		 * checks if a stored transaction is more suitable
		 * then the current core's.
		 */
		private void chooseSuitableStoredTransaction(){
			ArrayList<Transaction> purged = new ArrayList<>();
			ArrayList<Entry<Transaction, Long>> goodOnes = new ArrayList<>();
			for(Entry<Transaction, Long> entry : stored.entrySet()){
				if(core.isTooOld(entry.getValue())){
					purged.add(entry.getKey());
					log.verboseDebug("A transaction is too old, purging it ("+entry.getKey()+").");
				} else {
					if(!core.isTooYoung(entry.getValue())){
						goodOnes.add(entry);
					}
				}
			}
			for(Transaction entry: purged){
				stored.remove(entry);
			}
			if(goodOnes.size() == 0){
				log.verboseDebug("No transaction appears to be suitable to replace the current one.");
				return;
			}
			Entry<Transaction, Long> chosenOne;
			if(goodOnes.size() > 1){
				log.verboseDebug("The candidate transactions are "+goodOnes.size()+". Sorting them.");
				Comparator<Entry<Transaction, Long>> comp = (a, b) -> (int)Math.signum(a.getValue() - b.getValue());
				Collections.sort(goodOnes, Collections.reverseOrder(comp));
			}
			chosenOne = goodOnes.get(0);
			log.verboseDebug("The best transaction chosen is stored in block #"+chosenOne.getValue()+" and is: "+chosenOne.getKey());
			core.updateTransaction(chosenOne.getKey(), chosenOne.getValue());
		}
		
		/**
		 * @return timestamp in seconds
		 */
		public long time(){
			return System.currentTimeMillis() / 1000;
		}
		
		public void stop(){
			running = false;
		}
		
	}

}
