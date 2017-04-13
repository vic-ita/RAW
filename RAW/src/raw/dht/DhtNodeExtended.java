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
package raw.dht;

import raw.blockChain.api.Block;
import raw.blockChain.api.Transaction;

/**
 * Extends {@link DhtNode} adding references to
 * the {@link Transaction} authenticating a node
 * and to the number of the {@link Block} containing
 * such transaction.
 * 
 * @author vic
 *
 */
public interface DhtNodeExtended extends DhtNode {
	
	public Transaction getTransaction();
	
	public long getTransactionBlockNumber();
	
	/**
	 * Updates the informations of this {@link DhtNodeExtended},
	 * setting up a new {@link Transaction} together with
	 * the number of the {@link Block} where {@link Transaction} is
	 * stored.
	 * 
	 * @param transaction a new {@link Transaction}
	 * @param transactionBlockNumber number of the {@link Block} storing <code>transaction</code>
	 * @throws IllegalArgumentException if data contained in {@link Transaction} is not consistent with the internal {@link DhtNode} data
	 */
	public void updateTransaction(Transaction transaction, long transactionBlockNumber) throws IllegalArgumentException;
	
	/**
	 * Shrinks down this object to a 
	 * "simple" {@link DhtNode}.
	 * 
	 * @return a {@link DhtNode}
	 */
	public DhtNode getDhtNode();
	
	/**
	 * Compares the data of this {@link DhtNodeExtended} with
	 * another {@link DhtNodeExtended} considering 
	 * {@link Transaction} and transaction block number.
	 * 
	 * @param otherNode a {@link DhtNode} or {@link DhtNodeExtended}
	 * @return <code>true</code> if the two objects equals also on transaction data
	 */
	public boolean equalsWithTransaction(DhtNodeExtended otherNode);

}
