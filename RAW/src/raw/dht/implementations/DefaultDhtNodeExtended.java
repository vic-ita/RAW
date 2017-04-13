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

import java.security.PublicKey;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.blockChain.api.Transaction;
import raw.dht.DhtAddress;
import raw.dht.DhtID;
import raw.dht.DhtNode;
import raw.dht.DhtNodeExtended;
import raw.dht.implementations.exceptions.IncoherentTransactionException;

/**
 * Default {@link DhtNodeExtended} implementation.
 * 
 * @author vic
 *
 */
public class DefaultDhtNodeExtended implements DhtNodeExtended {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = -651211096038601232L;
	
	private DhtNode internalNode;
	private Transaction transaction;
	private long transactionBlockNumber;
	
	public DefaultDhtNodeExtended(DhtNode node, Transaction transaction, long transactionBlockNumber) throws IncoherentTransactionException{
		this.internalNode = node;
		if((!internalNode.getID().equals(transaction.getDhtID())) || (!internalNode.getPublicKey().equals(transaction.getPublicKey()))){
			throw new IncoherentTransactionException("Provided transaction is not consistend with provided DhtNode data!");
		}
		this.transaction = transaction;
		this.transactionBlockNumber = transactionBlockNumber;
	}
	
	public DefaultDhtNodeExtended(DhtID nodeId, DhtAddress nodeAddress, PublicKey nodePublicKey, Transaction transaction, long transactionBlockNumber) throws IncoherentTransactionException {
		this(new DefaultDhtNode(nodeId, nodePublicKey, nodeAddress), transaction, transactionBlockNumber);
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtNode#getID()
	 */
	@Override
	public DhtID getID() {
		return internalNode.getID();
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtNode#getPublicKey()
	 */
	@Override
	public PublicKey getPublicKey() {
		return internalNode.getPublicKey();
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtNode#getAddress()
	 */
	@Override
	public DhtAddress getAddress() {
		return internalNode.getAddress();
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtNodeExtended#getTransaction()
	 */
	@Override
	public Transaction getTransaction() {
		return transaction;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtNodeExtended#getTransactionBlockNumber()
	 */
	@Override
	public long getTransactionBlockNumber() {
		return transactionBlockNumber;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtNodeExtended#updateTransaction(raw.blockChain.api.Transaction, long)
	 */
	@Override
	public void updateTransaction(Transaction transaction, long transactionBlockNumber) throws IllegalArgumentException{
		if(!internalNode.getID().equals(transaction.getDhtID()) || !internalNode.getPublicKey().equals(transaction.getPublicKey())){
			throw new IllegalArgumentException("Provided transaction is not consistend with provided DhtNode data!");
		}
		this.transaction = transaction;
		this.transactionBlockNumber = transactionBlockNumber;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtNodeExtended#getDhtNode()
	 */
	@Override
	public DhtNode getDhtNode() {
		return internalNode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(internalNode.hashCode())
		.append(transaction.hashCode())
		.append(transactionBlockNumber);
		
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		DhtNode otherNode;
		if(obj instanceof DhtNodeExtended){
			otherNode = ((DhtNodeExtended) obj).getDhtNode();
		} else if (obj instanceof DhtNode){
			otherNode = (DhtNode) obj;
		} else {
			return false;
		}
		return internalNode.equals(otherNode);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Node: "+internalNode.toString()+" --- Transaction block number: "+transactionBlockNumber+" --- Transaction: "+transaction.toString();
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtNodeExtended#equalsWithTransaction(raw.dht.DhtNodeExtended)
	 */
	@Override
	public boolean equalsWithTransaction(DhtNodeExtended otherNode) {
		if(!transaction.equals(otherNode.getTransaction())){
			return false;
		}
		if(transactionBlockNumber != otherNode.getTransactionBlockNumber()){
			return false;
		}
		return equals(otherNode);
	}

}
