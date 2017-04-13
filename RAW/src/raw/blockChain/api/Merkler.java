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

import java.io.Serializable;
import java.util.ArrayList;

import raw.blockChain.exceptions.TransactionNotPresentException;

/**
 * An utility class to compute root value for a Merkle Tree of {@link Transaction}s.
 *  It do not require the binary tree to be complete.<br>
 * <br>
 * From bitcoin documentation:
 * <pre>
 *        ABCDEEEE .......Merkle root
 *       /        \
 *     ABCD        EEEE
 *    /    \      /
 *   AB    CD    EE .......E is paired with itself
 *  /  \  /  \  /
 *  A  B  C  D  E .........Transactions
 *  </pre>
 * @author vic
 *
 */
public interface Merkler {

	/**
	 * Using the given list of {@link Transaction}s as leaves it compute the
	 * hash for the root node.
	 * 
	 * @param transactions the <b>COMPLETE</b> list of leaves transactions
	 * @return the hash representing the Merkle root of the tree
	 */
	public HashValue getMerkleRoot(ArrayList<Transaction> transactions);
	
	/**
	 * Compute the Merkle root hash valuer without needing all the leaves.
	 * It should be used to verify the presence of a given {@link Transaction}
	 * inside a given {@link Block}. For example in this tree:<br>
	 * <pre>
	 *        ABCDEEEE 
	 *       /        \
	 *     ABCD        EEEE
	 *    /    \      /
	 *   AB    CD    EE
	 *  /  \  /  \  /
	 *  A  B  C  D  E
	 *  </pre>
	 *  
	 *  to verify transaction D only a copy of C, AB, and EEEE are needed. 
	 *  
	 * @param transaction the transaction to be verified
	 * @param compactRepresentation the data required to rebuild Merkle root
	 * @return the hash representing the Merkle root of the tree
	 * @throws TransactionNotPresentException if <tt>transaction</tt> and {@link IntermediateValues#getBaseTransaction()} of <tt>compactRepresentation</tT> are not matching
	 */
	public HashValue getMerkleRootByIntermediate(Transaction transaction, IntermediateValues compactRepresentation) throws TransactionNotPresentException;
	
	/**
	 * This method computes the compact representation of the necessary and sufficient
	 * intermediate hashes to re-compute a given tree verifying the presence of
	 * <tt>transaction</tt> amidst its leaves. The returned value is meant to be used
	 * by {@link Merkler#getMerkleRootByIntermediate(Transaction, IntermediateValues)}.
	 * A {@link TransactionNotPresentException} is risen if <tt>transaction</tt> is not
	 * present amidst the transactions contained in the <tt>containerBlock</tt> 
	 * 
	 * @param transaction the transaction to be verified
	 * @param containerBlock the block containing the given transaction
	 * @return an {@link IntermediateValues} compact representation.
	 * 
	 * @throws TransactionNotPresentException if <tt>transaction</tt> is not contained in <tt>containerBlock</tt>
	 */
	public IntermediateValues getIntermediateValues(Transaction transaction, Block containerBlock) throws TransactionNotPresentException;
	
	/**
	 * Object of this class are used to give a compact representation of
	 * data nedded to rebuild the Merkle tree by intermediate values
	 * without knowing the full tree representation. It is used in {@link Merkler#getMerkleRootByIntermediate(IntermediateValues)}
	 * 
	 * For example in this tree:<br>
	 * <pre>
	 *        ABCDEEEE 
	 *       /        \
	 *     ABCD        EEEE
	 *    /    \      /
	 *   AB    CD    EE
	 *  /  \  /  \  /
	 *  A  B  C  D  E
	 *  </pre>
	 *  
	 *  to verify transaction D only a copy of C, AB, and EEEE are needed. 
	 * 
	 * @author vic
	 *
	 */
	public interface IntermediateValues extends Serializable{
		
		/**
		 * Return the transaction on which this compact representation of the merkle tree us based.
		 * 
		 * @return the transaction under test.
		 */
		public Transaction getBaseTransaction();
		
		/**
		 * Return the number of the {@link Block} hosting the base transaction
		 * returned by {@link IntermediateValues#getBaseTransaction()}
		 * 
		 * @return the number of the {@link Block} hosting the base {@link Transaction}
		 */
		public long getBlockNumber();
		
		/**
		 * Return the next hash value to be hashed coming from the correct subtree.
		 * 
		 * @return the next hash value to be hashed coming from the correct subtree or null if no more hashes are left in the stack.
		 */
		public IntermediateValue popNextHash();
		
	}
	
	/**
	 * Objects used by {@link IntermediateValues} to store the items
	 * used to recompute a merkele root using the least possible amount
	 * of information.
	 * 
	 * @author vic
	 *
	 */
	public interface IntermediateValue extends Serializable{
		
		/**
		 * A value to be hashed with the ongoing merkle root
		 * reconstruction.
		 * 
		 * @return {@link HashValue} to be hashed with the ongoing merkle root reconstruction.
		 */
		public HashValue getValue();
		
		/**
		 * If the hash returned by {@link IntermediateValue#getValue()} is to be
		 * concatenated to the left of the ongoing merkle root subresult.
		 * 
		 * @return <tt>true</tt> if {@link IntermediateValue#getValue()} is to be concatenated on the left of the subresult
		 */
		public boolean leftPaired();
		
	}
	
}
