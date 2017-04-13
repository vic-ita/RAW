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
package raw.blockChain;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import raw.blockChain.api.Block;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.Transaction;
import raw.blockChain.services.miner.Miner;
import raw.blockChain.services.thickNode.ThickNode;
import raw.blockChain.services.thinNode.ThinNode;

public interface BlockChainCore {

	/**
	 * Calling this method will start all
	 * needed threads depending on the properties settings.
	 * 
	 * @return <tt>true</tt> if all thread (one or more) was started regularly
	 */
	public boolean start();

	/**
	 * This method will halt all BlockChain related
	 * threads running.
	 * 
	 * @return <tt>true</tt> if shutdown went regularly
	 */
	public boolean stop();

	/**
	 * @return a reference to the thread pool {@link ExecutorService} of this class
	 */
	public ExecutorService getThreadPool();

	/**
	 * @return a reference to the last {@link Block} in this block chain
	 */
	public Block getLastBlockInChain();
	
	/**
	 * @return a reference to the last {@link BlockHeader} in this block chain
	 */
	public BlockHeader getLastBlockHeaderInChain();
	
	/**
	 * This method will try to find a {@link BlockHeader} given its block number in BlockChain.
	 * 
	 * @param blockNumber the number of the desired {@link BlockHeader}
	 * @return the {@link BlockHeader} if existent or <tt>null</tt> otherwise
	 */
	public BlockHeader getBlockHeaderByNumber(long blockNumber);
	
	/**
	 * This method will try to find a complete {@link Block} given its block number in BlockChain.
	 * 
	 * @param blockNumberthe number of the desired {@link BlockHeader}
	 * @return the {@link Block} if existent or <tt>null</tt> otherwise
	 */
	public Block getBlockByNumber(long blockNumber);
	
	/**
	 * Checks the status of this node, regardless if it hosts a {@link ThickNode} or a {@link ThinNode}.
	 * 
	 * @return <tT>true</tT> if on this node a {@link ThickNode} or {@link ThinNode} is up and running.
	 */
	public boolean isNodeUp();
	
	/**
	 * @return an {@link ArrayList} of {@link InetSocketAddress} of "good staning" {@link ThickNode}s
	 */
	public ArrayList<InetSocketAddress> getThickNodesList();
	
	/**
	 * This method submits a {@link Transaction} to the {@link Miner}s network in order to
	 * get such {@link Transaction} incorporated in the block chain.
	 * 
	 * @param transaction the {@link Transaction} to be sent to {@link Miner}s
	 */
	public void submitTransaction(Transaction transaction);
	
	/**
	 * This method searches the block chain for the number of the last
	 * block where a given {@link Transaction} appears.
	 * 
	 * @param transaction a {@link Transaction}
	 * @return the {@link BlockHeader#getBlockNumber()} or -1 if no {@link Block} contains <code>transaction</code>
	 */
	public long transcationLastOccurrence(Transaction transaction);
	
	/**
	 * This method checks if a given {@link Transaction} is accounted for
	 * in the {@link Block} identified by <tt>blockNumber</tt>.
	 *
	 * @param blockNumber the number of the {@link Block} that should hold a reference to <tt>transaction</tt>
	 * @param transaction a given {@link Transaction}
	 * @return <tt>true</tt> if <tt>transaction</tt> is contained in the {@link Block} identified by <tt>blockNumber</tt>, <tt>false</tt> otherwise
	 */
	public boolean checkTransactionIsInBlock(long blockNumber, Transaction transaction);

}