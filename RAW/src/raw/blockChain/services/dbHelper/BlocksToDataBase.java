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
package raw.blockChain.services.dbHelper;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

import raw.blockChain.api.Block;
import raw.blockChain.api.BlockCompactRepresentation;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Transaction;

/**
 * Objects implementing this interface
 * will marshall the {@link Block} objects to
 * and from the database running on this machine.
 * 
 * @author vic
 */
public interface BlocksToDataBase {
	
	/**
	 * Open a connection to the database hosting the {@link Block}s.
	 */
	public void open();
	
	/**
	 * Close connection to the underlying database.
	 */
	public void close();
	
	/**
	 * Store a {@link Block} in the database.
	 * 
	 * @param block the {@link Block} to be stored
	 * @throws SQLIntegrityConstraintViolationException if an integrity constraint is violated
	 */
	public void storeOnDataBase(Block block) throws SQLIntegrityConstraintViolationException;
	
	/**
	 * Store only a {@link BlockHeader} in the database.
	 * 
	 * @param header the {@link BlockHeader} to be stored
	 * @throws SQLIntegrityConstraintViolationException if an integrity constraint is violated
	 * @throws SQLException 
	 */
	public void storeHeaderOnDataBase(BlockHeader header) throws SQLIntegrityConstraintViolationException, SQLException;
	
	/**
	 * Stores a compact representation for a block.
	 * 
	 * @param compactBlock the {@link BlockCompactRepresentation}
	 * @throws SQLException 
	 */
	public void storeBlockCompactRepresentation(BlockCompactRepresentation compactBlock) throws SQLException;
	
	/**
	 * Retrieves (if available) a {@link BlockCompactRepresentation} given a {@link BlockHeader}
	 * and a base {@link Transaction}.
	 * 
	 * @param header the key {@link BlockHeader}
	 * @param transaction the base {@link Transaction}
	 * @return a {@link BlockCompactRepresentation} if available in the databse or <tt>null</tt> otherwise
	 * @throws SQLException 
	 */
	public BlockCompactRepresentation getBlockCompatRepresentation(BlockHeader header, Transaction transaction) throws SQLException;
	
	/**
	 * This method will try to retrieve the header of the last block in this BlockChain.
	 * 
	 * @param generateGenesis if <tt>true</tt> this method will generate a genesis block in the case there is no block in the database
	 * @return the last {@link BlockHeader} in this block chain, or <code>null</code> if there are no blocks in this database. 
	 */
	public BlockHeader getLastBlockHeaderInChain(boolean generateGenesis);
	
	/**
	 * This method retrieves {@link Block} given its
	 * {@link HashValue} hash.
	 * 
	 * @param hash a {@link HashValue} representig a {@link BlockHeader} hash
	 * @return the {@link Block} if it exists, or <tt>null</tt> otherwise.
	 * @throws SQLException 
	 */
	public Block getBlockFromHash(HashValue hash) throws SQLException;
	
	/**
	 * This method will try to find a {@link BlockHeader} given its block number in BlockChain.
	 * 
	 * @param blockNumber the number of the desired {@link BlockHeader}
	 * @return the {@link BlockHeader} if existent or <tt>null</tt> otherwise
	 */
	public BlockHeader getBlockHeaderByNumber(long blockNumber);
	
	/**
	 * This method will try to find a {@link Block} given its block number in BlockChain.
	 * 
	 * @param blockNumber the number of the desired {@link Block}
	 * @return the {@link Block} if existent or <tt>null</tt> otherwise
	 */
	public Block getBlockByNumber(long blockNumber);
	
	/**
	 * This method will try to find a {@link BlockHeader} given its {@link HashValue}
	 * header's hash in  the BlockChain.
	 * 
	 * @param hash the header's {@link HashValue}
	 * @return the {@link BlockHeader} if existent or <tt>null</tt> otherwise
	 */
	public BlockHeader getBlockHeaderByHash(HashValue hash);
	
	/**
	 * Delete the {@link Block} identified by this 
	 * {@link BlockHeader}.
	 * 
	 * @param header the {@link BlockHeader} of the {@link Block} to be deleted
	 */
	public void deleteBlockByHeader(BlockHeader header);
	
	/**
	 * Delete the {@link Block} identified by its
	 * block number. 
	 * 
	 * @param blockNumber the number of the {@link Block} to be deleted
	 */
	public void deleteBlockByNumber(long blockNumber);
	
	/**
	 * Delete a whole {@link ArrayList} of {@link Block}s from
	 * the database.
	 * 
	 * @param blocksBulk an {@link ArrayList} of {@link Block}s to be deleted
	 * @return <tt>true</tt> if the set of {@link Block}s is deleted
	 */
	public boolean deleteBlocksBulk(ArrayList<Block> blocksBulk);
	
	/**
	 * Search the number of last block containing
	 * a given {@link Transaction}.
	 * 
	 * @param transaction a {@link Transaction}
	 * @return the {@link BlockHeader#getBlockNumber()} number or -1 if <code>transaction</code> is not in the block chain
	 */
	public long searchTranscationBlockNumber(Transaction transaction);

}
