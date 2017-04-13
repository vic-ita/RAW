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
package raw.blockChain.services;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import raw.blockChain.api.Block;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Transaction;
import raw.blockChain.services.miner.Miner;
import raw.blockChain.services.thickNode.ThickNode;
import raw.blockChain.services.thinNode.ThinNode;

/**
 * This interface defines basic operations
 * on the block chain common both to
 * {@link ThickNode}s and {@link ThinNode}s.
 * 
 * @author vic
 *
 */
public interface CommonNode {
	
	/**
	 * Retrieves a full {@link Block} representation starting
	 * from its {@link BlockHeader}.
	 * 
	 * @param header the header of the desired {@link Block}
	 * @return the {@link Block} if existent or <tt>null</tt> otherwise
	 */
	public Block getBlockFromHeader(BlockHeader header);
	
	/**
	 * Retrieves a full {@link Block} representation starting
	 * from its {@link HashValue} header's hash.
	 * 
	 * @param hash {@link HashValue} header's hash
	 * @return the {@link Block} if existent or <tt>null</tt> otherwise
	 */
	public Block getBlockFromHash(HashValue hash);
	
	/**
	 * This method will try to retrieve the header of the last block in this BlockChain.
	 * 
	 * @return the last {@link BlockHeader} in this block chain. 
	 */
	public BlockHeader getLastBlockHeaderInChain();
	
	/**
	 * This method will try to retrieve the last {@link Block} in this BlockChain.
	 * 
	 * @return the last {@link Block} in this block chain. 
	 */
	public Block getLastBlockInChain();
	
	/**
	 * This method will try to find a {@link BlockHeader} given its block number in BlockChain.
	 * 
	 * @param blockNumber the number of the desired {@link BlockHeader}
	 * @return the {@link BlockHeader} if existent or <tt>null</tt> otherwise
	 */
	public BlockHeader getBlockHeaderByNumber(long blockNumber);
	
	/**
	 * Check if a given {@link Transaction} is contained in the block identified 
	 * by a given {@link BlockHeader}.
	 * 
	 * @param blockHeader the {@link BlockHeader}
	 * @param transaction the {@link Transaction} to be checked
	 * @return <tt>true</tt> if {@link Transaction} is in the list of transactions of the {@link Block} to whom {@link BlockHeader} belong
	 */
	public boolean checkTransactionInBlockByHeader(BlockHeader blockHeader, Transaction transaction);
	
	/**
	 * Check if a given {@link Transaction} is contained in the block identified 
	 * by a given {@link HashValue} header's hash.
	 * 
	 * @param headerHash the header's hash {@link HashValue}
	 * @param transaction the {@link Transaction} to be checked
	 * @return <tt>true</tt> if {@link Transaction} is in the list of transactions of the {@link Block} to whom {@link BlockHeader} belong
	 */
	public boolean checkTransactionInBlockByHeaderHash(HashValue headerHash, Transaction transaction);
	
	/**
	 * Check if a given {@link Transaction} is contained in the block identified 
	 * by a given block number.
	 * 
	 * @param blockNumber the block number in chain
	 * @param transaction the {@link Transaction} to be checked
	 * @return <tt>true</tt> if {@link Transaction} is in the list of transactions of the {@link Block} to whom {@link BlockHeader} belong
	 */
	public boolean checkTransactionInBlockByBlockNumber(long blockNumber, Transaction transaction);
	
	/**
	 * Since {@link ThickNode}s and {@link ThinNode}s are not able to mine new
	 * {@link Block}s this method will take in charge a {@link Transaction} to
	 * be incorporated in the block chain and forward it to a {@link Miner}.
	 * 
	 * @param transaction the {@link Transaction} to include in the block chain
	 */
	public void submitTransaction(Transaction transaction);
	
	/**
	 * @return <tt>true</tt> if this node is up and running.
	 */
	public boolean isUp();
	
	/**
	 * @return an {@link ArrayList} of {@link InetSocketAddress} of "good staning" {@link ThickNode}s
	 */
	public ArrayList<InetSocketAddress> getThickNodesList();
	
	/**
	 * @return an {@link InetSocketAddress} representing this node
	 */
	public InetSocketAddress getNodeAddress();
	
	/**
	 * This method searches the block chain for the number of the last
	 * block where a given {@link Transaction} appears.
	 * 
	 * @param transaction a {@link Transaction}
	 * @return the {@link BlockHeader#getBlockNumber()} or -1 if no {@link Block} contains <code>transaction</code>
	 */
	public long transcationLastOccurrence(Transaction transaction);
}
