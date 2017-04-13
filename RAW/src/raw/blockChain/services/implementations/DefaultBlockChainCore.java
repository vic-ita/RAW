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
package raw.blockChain.services.implementations;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import raw.blockChain.BlockChainCore;
import raw.blockChain.api.Block;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.Transaction;
import raw.blockChain.services.miner.Miner;
import raw.blockChain.services.miner.implementations.DefaultMiner;
import raw.blockChain.services.thickNode.ThickNode;
import raw.blockChain.services.thickNode.implementations.DefaultThickNode;
import raw.blockChain.services.thinNode.ThinNode;
import raw.blockChain.services.thinNode.implementations.DefaultThinNode;
import raw.concurrent.RAWExecutors;
import raw.logger.Log;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;


/**
 * This class instantiate and execute a BlockChain
 * client, from a minimal one to a full fledged one.
 * 
 * @author vic
 *
 */
public class DefaultBlockChainCore implements BlockChainCore {
	
	private static DefaultBlockChainCore thisCore = null;
	
	private Log log;
	
	private BlockChainProperties properties;
	
	private Miner miner;
	private ThickNode thickNode;
	private ThinNode thinNode;
	
	private ExecutorService pool;
	
	private DefaultBlockChainCore() {
		log = Log.getLogger();
		
		properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		
//		pool = Executors.newCachedThreadPool();
		pool = RAWExecutors.newCachedThreadPool();
		
		if(properties.isThickClientIsOn()){
			thickNode = new DefaultThickNode();
		} else{
			thinNode = new DefaultThinNode();
		}
		
		if(properties.isMinerIsOn()){
			miner = new DefaultMiner();
		}
		
		log.debug("BlockChainCore constructed.");
	}
	
	/**
	 * Static method to get a singleton {@link BlockChainCore} instance.
	 * 
	 * @return a singleton object implementing {@link BlockChainCore}
	 */
	public static DefaultBlockChainCore getBlockChainCore(){
		if(thisCore == null){
			thisCore = new DefaultBlockChainCore();
		}
		return thisCore;
	}
	
	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#start()
	 */
	@Override
	public boolean start(){
		boolean status = true;
		
		if(properties.isThickClientIsOn()){
			Future<Void> thickNodeFuture = pool.submit(thickNode);
			try {
				thickNodeFuture.get(1, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				log.exception(e);
				status = false;
			} catch (ExecutionException e) {
				log.exception(e);
				status = false;
			} catch (TimeoutException e) {
				log.debug("thickNodeFuture is running and on its own.");
			}
		}
		else{
			Future<Void> thinNodeFuture = pool.submit(thinNode);
			try {
				thinNodeFuture.get(1, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				log.exception(e);
				status = false;
			} catch (ExecutionException e) {
				log.exception(e);
				status = false;
			} catch (TimeoutException e) {
				log.debug("thinNodeFuture is running and on its own.");
			}
		}
		
		if(properties.isMinerIsOn()){
			if (properties.isThickClientIsOn()) {
				if(miner.registerListener(thickNode)){
					log.debug("On this machine also a ThickNode is running. Local listener registered on Miner.");
				}
				else{
					log.debug("Register local listener failed on Miner!");
				}
				if(thickNode.reagisterThickNodeListener(miner)){
					log.debug("On this machine also a ThickNode is running. Local listener registered on ThickNode.");
				} else {
					log.debug("Register local listener failed on ThickNode!");
				}
			}
			Future<Void> minerFuture = pool.submit(miner);
			try {
				minerFuture.get(1, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				log.exception(e);
				status = false;
			} catch (ExecutionException e) {
				log.exception(e);
				status = false;
			} catch (TimeoutException e) {
				log.debug("minerFuture is running and on its own.");
			}
		}
		
		log.info("BlockChain thread(s) started.");
				
		return status;
	}
	
	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#stop()
	 */
	@Override
	public boolean stop(){
		boolean status = true;
		
		if(properties.isMinerIsOn()){
			status = status && miner.stopService();
		}
		if(properties.isThickClientIsOn()){			
			status = status && thickNode.stopService();
		} else {
			status = status && thinNode.stopService();
		}
		
		return status;
	}
	
	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#getThreadPool()
	 */
	@Override
	public ExecutorService getThreadPool(){
		return this.pool;
	}
	
	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#getLastBlockInChain()
	 */
	@Override
	public Block getLastBlockInChain(){
		Block lastBlock;
		if(properties.isThickClientIsOn()){
			lastBlock = thickNode.getLastBlockInChain();
		} else {
			lastBlock = thinNode.getLastBlockInChain();
		}
		return lastBlock;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#getLastBlockHeaderInChain()
	 */
	@Override
	public BlockHeader getLastBlockHeaderInChain() {
		BlockHeader lastHeader;
		if(properties.isThickClientIsOn()){
			lastHeader = thickNode.getLastBlockHeaderInChain();
		} else {
			lastHeader = thinNode.getLastBlockHeaderInChain();
		}
		return lastHeader;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#getBlockHeaderByNumber()
	 */
	@Override
	public BlockHeader getBlockHeaderByNumber(long blockNumber) {
		BlockHeader header;
		if(properties.isThickClientIsOn()){
			header = thickNode.getBlockHeaderByNumber(blockNumber);
		} else {
			header = thinNode.getBlockHeaderByNumber(blockNumber);
		}
		return header;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#getBlockByNumber(long)
	 */
	@Override
	public Block getBlockByNumber(long blockNumber) {
		BlockHeader header = getBlockHeaderByNumber(blockNumber);
		if(header == null){
			return null;
		}
		Block block;
		if(properties.isThickClientIsOn()){
			block = thickNode.getBlockFromHeader(header);
		} else {
			block = thinNode.getBlockFromHeader(header);
		}
		return block;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#isNodeUp()
	 */
	@Override
	public boolean isNodeUp() {
		if(properties.isThickClientIsOn()){
			return thickNode.isUp();
		} else {
			return thinNode.isUp();
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#getThickNodesList()
	 */
	@Override
	public ArrayList<InetSocketAddress> getThickNodesList() {
		if(properties.isThickClientIsOn()){
			return thickNode.getThickNodesList();
		} else {
			return thinNode.getThickNodesList();
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#submitTransaction(raw.blockChain.api.Transaction)
	 */
	@Override
	public void submitTransaction(Transaction transaction) {
		if(properties.isMinerIsOn()){
			miner.submitTransaction(transaction);
		}
		if(properties.isThickClientIsOn()){
			thickNode.submitTransaction(transaction);
		} else {
			thinNode.submitTransaction(transaction);
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#transcationLastOccurrence(raw.blockChain.api.Transaction)
	 */
	@Override
	public long transcationLastOccurrence(Transaction transaction) {
		if(properties.isThickClientIsOn()){
			return thickNode.transcationLastOccurrence(transaction);
		} else {
			return thinNode.transcationLastOccurrence(transaction);
		}
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.BlockChainCore#checkTransactionIsInBlock(long, raw.blockChain.api.Transaction)
	 */
	@Override
	public boolean checkTransactionIsInBlock(long blockNumber, Transaction transaction) {
//		log.verboseDebug("Gonna check "+transaction+" in block #"+blockNumber);
		if(properties.isThickClientIsOn()){
//			log.verboseDebug("Checking through thick node.");
			return thickNode.checkTransactionInBlockByBlockNumber(blockNumber, transaction);
		} else {
//			log.verboseDebug("Checking through thin node.");
			return thinNode.checkTransactionInBlockByBlockNumber(blockNumber, transaction);
		}
	}

}
