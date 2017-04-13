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
package raw.blockChain.api;

/**
 * Objects extending this class will work
 * to find the next viable {@link Block} to be submitted
 * in the network blockchain.
 * 
 * @author vic
 *
 */
public interface BlockMiner {
	
	/**
	 * This method will search for a valid {@link Block}
	 * that should follow the block identified by <tt>previousBlock</tt>
	 * {@link BlockHeader} in the block chain.
	 * The transactions that will be used are chosen among the one submitted
	 * to this object via {@link BlockMiner#pushTransaction(Transaction)}.
	 * This computation started calling this method could potentially never end.<br><br>
	 * If a new {@link Block} is found by this method it will be notified to all listening
	 * objects registered by means of {@link BlockMiner#registerListener(BlockMinerListener)}.  
	 * 
	 * @param previousBlock the {@link BlockHeader} of the previous block
	 */
	public void findNextBlock(BlockHeader previousBlock);
	
	/**
	 * Register a new {@link BlockMinerListener} to whom a newly found
	 * {@link Block} will be notified.
	 * 
	 * @param listener the listening object
	 */
	public void registerListener(BlockMinerListener listener);
	
	/**
	 * Add a new {@link Transaction} to the list of
	 * those that could be used by {@link BlockMiner#findNextBlock(BlockHeader)}
	 * 
	 * @param newTransaction a new {@link Transaction}
	 */
	public void pushTransaction(Transaction newTransaction);
	
	/**
	 * Calling this method will clear the transactions added
	 * by {@link BlockMiner#pushTransaction(Transaction)}.
	 */
	public void clearTransactions();
	
	/**
	 * Stops the computation started by invocation of {@link BlockMiner#findNextBlock(BlockHeader)}
	 */
	public void haltBlockMining();
	
	/**
	 * @return <tt>true</tt> if there is a block mining task ongoing
	 */
	public boolean isMining();
	
	/**
	 * @return the {@link BlockHeader} that will be the previous {@link BlockHeader} of the {@link Block} that is going to be mined
	 */
	public BlockHeader prevHeaderMining();

}
