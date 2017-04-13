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
package raw.blockChain.services.thickNode.messages.types;

import raw.blockChain.api.Block;
import raw.blockChain.api.Transaction;
import raw.blockChain.services.thickNode.ThickNode;
import raw.blockChain.services.thickNode.messages.ThickNodeMessages;

/**
 * Implementation of {@link ThickNodeMessages} used to
 * ask a {@link ThickNode} the number of the block
 * containing a {@link Transaction}
 * 
 * @author vic
 *
 */
public class TransactionBlockNumberMessage implements ThickNodeMessages {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = -8756495421385879271L;
	
	private boolean isRequest;
	private Transaction transaction;
	private long blockNumber;
	
	/**
	 * Builds a request to know the block number containing a given 
	 * {@link Transaction}.
	 * 
	 * @param transaction a {@link Transaction}
	 */
	public TransactionBlockNumberMessage(Transaction transaction) {
		isRequest = true;
		this.transaction = transaction;
	}
	
	/**
	 * Builds a reply with the {@link Block} number containing a {@link Transaction}
	 * or -1 if the transaction is not in the block chain.
	 * 
	 * @param blockNumber the number of a {@link Block} containing <code>transaction</code> or -1
	 * @param transaction a {@link Transaction}
	 */
	public TransactionBlockNumberMessage(long blockNumber, Transaction transaction) {
		isRequest = false;
		this.blockNumber = blockNumber;
		this.transaction = transaction;
	}

	/**
	 * @return the isRequest
	 */
	public boolean isRequest() {
		return isRequest;
	}

	/**
	 * @return the transaction
	 */
	public Transaction getTransaction() {
		return transaction;
	}

	/**
	 * @return the blockNumber
	 */
	public long getBlockNumber() {
		return blockNumber;
	}

}
