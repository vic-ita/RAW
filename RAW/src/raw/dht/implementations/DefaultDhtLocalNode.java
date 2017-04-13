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
package raw.dht.implementations;

import java.security.KeyPair;

import raw.blockChain.api.Transaction;
import raw.dht.DhtLocalNode;
import raw.dht.DhtNode;
import raw.dht.DhtNodeExtended;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.exceptions.IncompleteNodeExtendedException;

/**
 * Default implementation of {@link DhtLocalNode}
 * 
 * @author vic
 *
 */
public class DefaultDhtLocalNode implements DhtLocalNode {
	
	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = 6038021443958908002L;

	private DhtNode node;
	private Transaction transaction;
	private long transactionBlockNumber;
	private KeyPair keyPair;
	
	public DefaultDhtLocalNode(DhtNodeExtended localNode, KeyPair keyPair) {
		node = localNode.getDhtNode();
		transaction = localNode.getTransaction();
		transactionBlockNumber = localNode.getTransactionBlockNumber();
		this.keyPair = keyPair;
	}
	
	public DefaultDhtLocalNode(DhtNode node, Transaction transaction, KeyPair keyPair, long transactionBlockNumber) {
		this.node = node;
		this.transaction = transaction;
		this.keyPair = keyPair;
		this.transactionBlockNumber = transactionBlockNumber;
	}
	
	/* (non-Javadoc)
	 * @see raw.dht.DhtLocalNode#getNode()
	 */
	@Override
	public DhtNode getNode() {
		return node;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtLocalNode#getNodeExtended()
	 */
	@Override
	public DhtNodeExtended getNodeExtended() throws IncoherentTransactionException, IncompleteNodeExtendedException {
		if(node == null){
			throw new IncompleteNodeExtendedException("Cannot generate a DhtNodeExtended: DhtNode is null");
		}
		if(transaction == null){
			throw new IncompleteNodeExtendedException("Cannot generate a DhtNodeExtended: Transaction is null");
		}
		DhtNodeExtended returnable= new DefaultDhtNodeExtended(node, transaction, transactionBlockNumber);
		return returnable;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtLocalNode#getKeyPair()
	 */
	@Override
	public KeyPair getKeyPair() {
		return keyPair;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtLocalNode#getBlockNumber()
	 */
	@Override
	public long getBlockNumber() {
		return transactionBlockNumber;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtLocalNode#getTransaction()
	 */
	@Override
	public Transaction getTransaction() {
		return transaction;
	}

}
