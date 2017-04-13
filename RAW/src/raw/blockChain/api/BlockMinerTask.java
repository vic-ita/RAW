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

import java.util.ArrayList;
import java.util.concurrent.Callable;

import raw.blockChain.api.implementations.DefaultTransaction;

/**
 * Objects implementing this class should be used to compute a new {@link Block}
 * 
 * @author vic
 *
 */
public abstract class BlockMinerTask implements Callable<Block> {

	private BlockHeader previousBlockHeader;
	private ArrayList<Transaction> candidateTransactions;
	
	/**
	 * This object will try to compute a {@link BlockHeader} that would
	 * legally follow <tt>previousBlockHeader</tt> in the block chain.
	 * 
	 * @param previousBlockHeader the header of previous {@link Block}
	 * @param candidateTransactions the set of transactions to be used for the new {@link Block}
	 * @throws IllegalArgumentException if parameters are <tt>null</tt>.
	 */
	public BlockMinerTask(BlockHeader previousBlockHeader, ArrayList<Transaction> candidateTransactions) throws IllegalArgumentException {
		if(previousBlockHeader == null || candidateTransactions == null){
			throw new IllegalArgumentException();
		}
		this.previousBlockHeader = previousBlockHeader;
		this.candidateTransactions = candidateTransactions;
		if(candidateTransactions.size()>1){
			if(candidateTransactions.contains(DefaultTransaction.getNullTransaction())){
				candidateTransactions.remove(DefaultTransaction.getNullTransaction());
			}
		}
	}
	
	/**
	 * Calling this method will halt this task.
	 */
	public abstract void stop();

	/**
	 * @return the previousBlockHeader
	 */
	public BlockHeader getPreviousBlockHeader() {
		return previousBlockHeader;
	}

	/**
	 * @return the candidateTransactions
	 */
	public ArrayList<Transaction> getCandidateTransactions() {
		return candidateTransactions;
	}
	
}
