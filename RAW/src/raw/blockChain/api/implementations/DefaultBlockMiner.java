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
package raw.blockChain.api.implementations;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import raw.blockChain.api.Block;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.BlockMiner;
import raw.blockChain.api.BlockMinerListener;
import raw.blockChain.api.BlockMinerTask;
import raw.blockChain.api.Transaction;
import raw.concurrent.RAWExecutors;
import raw.logger.Log;

/**
 * Default implementation of {@link BlockMiner} interface.
 * 
 * @author vic
 *
 */
public class DefaultBlockMiner implements BlockMiner {
	
	private ExecutorService pool;
	private Future<Block> miningTaskFuture;
	
	private boolean isMining;
	
	private BlockHeader miningPrevHeader;
	
	private BlockMinerTask miningTask;
	
	private ArrayList<BlockMinerListener> listeners;
	
	private ArrayList<Transaction> transactions;
	
	private String mySignature;
	
	private Log log;
	
	public DefaultBlockMiner(String minerSignature) {
		pool = RAWExecutors.newCachedThreadPool();
		listeners = new ArrayList<BlockMinerListener>();
		transactions = new ArrayList<Transaction>();
		mySignature = minerSignature;
		log = Log.getLogger();
	}
	
	public DefaultBlockMiner(ExecutorService pool, String minerSignature) {
		this.pool = pool;
		listeners = new ArrayList<BlockMinerListener>();
		transactions = new ArrayList<Transaction>();
		mySignature = minerSignature;
		log = Log.getLogger();
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockMiner#findNextBlock(raw.blockChain.BlockHeader)
	 */
	@Override
	public void findNextBlock(BlockHeader previousBlock) {
		if(isMining){
			if(miningPrevHeader != null && previousBlock.getBlockNumber() > miningPrevHeader.getBlockNumber()){
				//task to be sumbitted is better than the one going on.
				log.verboseDebug("Miner already running on block "+miningPrevHeader.getBlockNumber()+" and received task for previous block "+previousBlock.getBlockNumber());
				haltBlockMining();
			} else {
				//the task requested is already ongoing or older than the one going on. abort this.
				log.verboseDebug("Miner appears to be mining. Received request to mine after "+previousBlock.getBlockNumber()+". My prev header is: "+miningPrevHeader);
				return;
			}
		}
		BlockMinerTask newBlockSearch = new DefaultBlockMinerTask(previousBlock, transactions, this, mySignature);
		log.debug("Submitting a new mining task for previous block "+previousBlock);
		miningPrevHeader = previousBlock;
		miningTaskFuture = pool.submit(newBlockSearch);
		try {
			miningTaskFuture.get(1, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			log.exception(e);
		} catch (ExecutionException e) {
			log.exception(e);
		} catch (TimeoutException e) {
			log.debug("Task has started and is on its own.");
		}
		miningTask = newBlockSearch;
		log.debug("Mining has begun!");
		isMining = true;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockMiner#registerListener(raw.blockChain.BlockMinerListener)
	 */
	@Override
	public void registerListener(BlockMinerListener listener) {
		synchronized (listeners) {	
			if(!listeners.contains(listener)){				
				listeners.add(listener);
				log.debug("New listener registered.");
			} else {
				log.debug("Listener already registered.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockMiner#pushTransaction(raw.blockChain.Transaction)
	 */
	@Override
	public void pushTransaction(Transaction newTransaction) {
		if(!transactions.contains(newTransaction)){
			transactions.add(newTransaction);
			log.debug("Added new transaction to list: "+newTransaction);
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockMiner#haltBlockMining()
	 */
	@Override
	public void haltBlockMining() {
		if(miningTask != null){
		synchronized (miningTask) {			
				log.debug("Signalling to task: halt requested.");
				if(miningTask != null){					
					miningTask.stop();
				}
				miningTask = null;
				miningTaskFuture = null;
				miningPrevHeader = null;
				isMining = false;
			}
		} else {
			log.verboseDebug("No task is ongoing. Nothing to do.");
		}
	}
	
	protected void signalBlockIsFound(Block block){
		synchronized (listeners) {			
			for(BlockMinerListener listener : listeners){
				listener.notifyNewBlock(block);
			}
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.BlockMiner#clearTransactions()
	 */
	@Override
	public void clearTransactions() {
		transactions.clear();		
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.BlockMiner#isMining()
	 */
	@Override
	public boolean isMining() {
		return isMining;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.BlockMiner#prevHeaderMining()
	 */
	@Override
	public BlockHeader prevHeaderMining() {
		return miningPrevHeader;
	}

}
