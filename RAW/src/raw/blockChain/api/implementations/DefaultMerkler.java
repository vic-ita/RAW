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
package raw.blockChain.api.implementations;

import java.util.ArrayList;

import raw.blockChain.api.Block;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Hasher;
import raw.blockChain.api.Merkler;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultMerkleTree.TreeCursor;
import raw.blockChain.exceptions.TransactionNotPresentException;

/**
 * Implementation of {@link Merkler}
 * 
 * @author vic
 *
 */
public class DefaultMerkler implements Merkler {
	
	private int cacheSize;
	private DefaultMerklerMap merkleTreesCache;
	
	/**
	 * Build an instance of {@link Merkler}.
	 * This hasher keeps track of the last <tt>cacheSize</tt> most
	 * requested Merkle trees avoiding recomputation.
	 * 
	 * @param cacheSize the number of most asked Merkle trees kept in cache 
	 */
	public DefaultMerkler(int cacheSize) {
		this.cacheSize = cacheSize;
		merkleTreesCache = new DefaultMerklerMap(this.cacheSize);
	}	

	/* (non-Javadoc)
	 * @see raw.blockChain.interfaces.Merkler#getMerkleRoot(java.util.ArrayList)
	 */
	@Override
	public HashValue getMerkleRoot(ArrayList<Transaction> transactions) {
		DefaultMerkleTree tree = merkleTreesCache.search(transactions);
		if(tree == null){
			tree = new DefaultMerkleTree(transactions);
			merkleTreesCache.put(transactions, tree);
		}
		return tree.root();
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.interfaces.Merkler#getMerkleRootByIntermediate(raw.blockChain.interfaces.Transaction, raw.blockChain.interfaces.Merkler.IIntermediateValues)
	 */
	@Override
	public HashValue getMerkleRootByIntermediate(Transaction transaction,
			IntermediateValues compactRepresentation) throws TransactionNotPresentException {
		if(!transaction.equals(compactRepresentation.getBaseTransaction())){
			String msg = "Transaciton "+transaction+" is not the base transaction for this compact representation";
			throw new TransactionNotPresentException(msg);
		}
		Hasher hasher = new DefaultHasher();
		HashValue prevHash = hasher.hashTransaction(transaction);
		IntermediateValue nextValue = compactRepresentation.popNextHash();
		while(nextValue != null){
			if(nextValue.leftPaired()){
				prevHash = hasher.hashHashes(nextValue.getValue(), prevHash);
			}
			else{
				prevHash = hasher.hashHashes(prevHash, nextValue.getValue());
			}
			nextValue = compactRepresentation.popNextHash();
		}
		return prevHash;
	}

	@Override
	public IntermediateValues getIntermediateValues(Transaction transaction,
			Block containerBlock) throws TransactionNotPresentException {
		ArrayList<Transaction> tansactions = containerBlock.getTransactions();
		DefaultMerkleTree tree = merkleTreesCache.search(tansactions);
		if(tree == null){
			tree = new DefaultMerkleTree(tansactions);
		}
		DefaultIntermediateValuesBuilder intermediateBuilder = new DefaultIntermediateValuesBuilder(transaction, containerBlock.getHeader().getBlockNumber());
		TreeCursor cursor = new TreeCursor(tree);
		while(!(cursor.getTransaction().equals(transaction))){
			boolean moved = cursor.moveToRight();
			if(!moved){
				//the transactions are over and we did not find what we were searching for.
				throw new TransactionNotPresentException("DefaultTransaction "+transaction+" is not in block #"+containerBlock.getHeader().getBlockNumber());
			}
		}//now cursor is on "transaction" node.
		IntermediateValue nextValue = nextValue(cursor);
		while(nextValue != null){
			intermediateBuilder.pushNextHash(nextValue);
			nextValue = nextValue(cursor);
		}
		return intermediateBuilder.build();
	}
	
	/**
	 * Return the next {@link IntermediateValue} to be put in the {@link IntermediateValues} list.
	 * If <tt>cursor</tt> refers to the root node and no more {@link IntermediateValue} are needed
	 * this method returns null.<br>
	 * <b>NOTE:</b> if an {@link IntermediateValue} is returned <tt>cursor</tt> is moved to the father node's position.
	 * 
	 * @param cursor a {@link TreeCursor} traversing a {@link DefaultMerkleTree}
	 * @return the next {@link IntermediateValue} from the current position.
	 */
	private IntermediateValue nextValue(TreeCursor cursor){
		IntermediateValue desiredValue = null;
		if(!cursor.isRoot()){
			boolean isLeftPaired;
			HashValue nextValue;
			if(!cursor.hasLeftNeighbor()){
				/*
				 * This node has no neighbors to its left.
				 * Thus the desired brother node is to its right. 
				 */
				isLeftPaired = false;
				cursor.moveToRight();
				if(!cursor.isLeaf()){
					nextValue = cursor.getHashValue();
				}
				else{
					Hasher hasher = new DefaultHasher();
					nextValue = hasher.hashTransaction(cursor.getTransaction());
				}
			}
			else{
				if(!cursor.hasRightNeighbor()){
					/*
					 * This node has no neighbors to its right.
					 * Thus the desired brother node is to its left. 
					 */
					isLeftPaired = true;
					cursor.moveToLeft();
					if(!cursor.isLeaf()){
						nextValue = cursor.getHashValue();
					}
					else{
						Hasher hasher = new DefaultHasher();
						nextValue = hasher.hashTransaction(cursor.getTransaction());
					}
				}
				else{					
					/*
					 * So we are not on an "edge node".
					 * We will guess that the desired node is the one to the right
					 * and we check that. If not we'll just revert to the left one.
					 */
					HashValue startingNodeFatherHash = cursor.getFatherHash();
					cursor.moveToRight();
					if(startingNodeFatherHash.equals(cursor.getFatherHash())){
						/*
						 * the guess was correct. cursor is on the 
						 * desired node. 
						 */
						isLeftPaired = false;
						if(!cursor.isLeaf()){
							nextValue = cursor.getHashValue();
						}
						else{
							Hasher hasher = new DefaultHasher();
							nextValue = hasher.hashTransaction(cursor.getTransaction());
						}
					}
					else{
						/*
						 * The guess was wrong.
						 * Go back to the left brother of the original node.
						 */
						isLeftPaired = true;
						cursor.moveToLeft();//back to the "starting node"
						cursor.moveToLeft();//hopping to the desired one
						if(!cursor.isLeaf()){
							nextValue = cursor.getHashValue();
						}
						else{
							Hasher hasher = new DefaultHasher();
							nextValue = hasher.hashTransaction(cursor.getTransaction());
						}
					}
				}
			}
			desiredValue = new DefaultIntermediateValue(nextValue, isLeftPaired); //here is the value to be returned
			cursor.moveToFather(); // up one step
		}
		return desiredValue;
	}

}
