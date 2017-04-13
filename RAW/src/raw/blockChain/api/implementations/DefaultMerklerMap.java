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
import java.util.Collections;
import java.util.HashMap;

import raw.blockChain.api.Transaction;

/**
 * This class implements a map to be used as chache by {@link DefaultMerkler}.
 * 
 * It should not be used for any other purpose.
 * 
 * @author vic
 *
 */
public class DefaultMerklerMap {
	
	private int size;
	
	private HashMap<ArrayList<Transaction>, DefaultMerkleTree> records;
	private HashMap<DefaultMerkleTree, ArrayList<Transaction>> reverseRecords;
	private ArrayList<Counter> recordsCount;
	
	
	/**
	 * Build a {@link DefaultMerklerMap} with a capacity of <tt>size</tt> elements.
	 * A {@link DefaultMerklerMap} contains pre-computed {@link DefaultMerkleTree} elements.
	 * 
	 * @param size the numebr of records that can be hold by this object.
	 */
	protected DefaultMerklerMap(int size) {
		this.size = size;
		
		records = new HashMap<ArrayList<Transaction>, DefaultMerkleTree>();
		reverseRecords = new HashMap<DefaultMerkleTree, ArrayList<Transaction>>();
		
		recordsCount = new ArrayList<DefaultMerklerMap.Counter>();
	}
	
	/**
	 * Search the {@link DefaultMerklerMap} for a {@link DefaultMerkleTree} that
	 * contains the ordered transations of <tT>transactionsList</tt>.
	 * 
	 * @param trasnactionsList a list of {@link Transaction}s
	 * @return the {@link DefaultMerkleTree} that was built on the same transactions specified by <tt>transactionsList</tt> or <tt>null</tt> if it is not present
	 */
	protected DefaultMerkleTree search(ArrayList<Transaction> trasnactionsList){
		if(isContained(trasnactionsList)){
			DefaultMerkleTree returnableTree = records.get(trasnactionsList);
			for(int i = 0; i <recordsCount.size(); i++){
				if(recordsCount.get(i).compareTree(returnableTree)){
					recordsCount.get(i).increment();
				}
				else{
					recordsCount.get(i).decrement();
				}
			}
			return returnableTree;			
		} 
		return null;
	}
	
	/**
	 * Add a {@link DefaultMerkleTree} as a record in this {@link DefaultMerklerMap}. If the
	 * tree is already present this operation has no effect.
	 * 
	 * @param the list of transactions that created <tt>tree</tt>
	 * @param tree a {@link DefaultMerkleTree} record
	 */
	protected void put(ArrayList<Transaction> transactionsList, DefaultMerkleTree tree){
		if(recordsCount.size() == size && !isContained(transactionsList)){
			/* maximum size reached.
			 * purge less used record.
			 */
			Collections.sort(recordsCount);
			DefaultMerkleTree lessUsedTree = recordsCount.get(0).tree;
			ArrayList<Transaction> lessUsedList = reverseRecords.get(lessUsedTree);
			records.remove(lessUsedList, lessUsedTree);
			reverseRecords.remove(lessUsedTree, lessUsedList);
			recordsCount.remove(0);
		}
		if(!isContained(transactionsList)){
			Counter counter = new Counter(1, tree);
			records.put(transactionsList, tree);
			reverseRecords.put(tree, transactionsList);
			recordsCount.add(counter);		
		}
	}
	
	private boolean isContained(ArrayList<Transaction> transactions){
		return records.containsKey(transactions);
	}
	
	protected int size(){
		return records.size();
	}
	
	/**
	 * Helper class to be used to keep track of the frequencies
	 * of searches about a given record.
	 * 
	 * @author vic
	 *
	 */
	private class Counter implements Comparable<Counter>{
		private double count;
		private DefaultMerkleTree tree;
		
		private final double decrement = 1/3;
		
		public Counter(int count, DefaultMerkleTree tree) {
			this.count = count;
			this.tree = tree;
		}
		
		public void increment() {
			this.count = this.count + 1f;
		}
		
		public void decrement(){
			if(this.count > decrement){
				this.count = this.count - decrement;
			}
			else{
				this.count = 0.0;
			}
		}
		
		public boolean compareTree(DefaultMerkleTree tree){
			return this.tree.equals(tree);
		}

		@Override
		public int compareTo(Counter o) {
			if(this.count - o.count > 0){
				return 1;
			}
			if(this.count - o.count < 0){
				return -1;
			}
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == null){
				return false;
			}
			if(!(obj instanceof Counter)){
				return false;
			}
			return compareTree((((Counter)obj).tree));
		}
	}
}
