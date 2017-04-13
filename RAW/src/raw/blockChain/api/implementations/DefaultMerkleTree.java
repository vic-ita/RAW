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
import java.util.Iterator;

import raw.blockChain.api.HashValue;
import raw.blockChain.api.Hasher;
import raw.blockChain.api.Merkler;
import raw.blockChain.api.Transaction;

/**
 * This class is designed to be used by {@link DefaultMerkler} implementation of {@link Merkler}.
 * It should not be used for any other purpose...
 * 
 * @author vic
 *
 */
public class DefaultMerkleTree {
	private int treeHeight;
	
	private LeafNode leftmostLeaf;
	private LeafNode rightmostLeaf;
	
	private RootNode root;
	
	protected DefaultMerkleTree(ArrayList<Transaction> leafTransactions) {
		if (leafTransactions.size() == 1) {
			treeHeight = 1;
		} else {
			treeHeight = ceilLogBase2(leafTransactions.size());
		}

		root = iterativeTreeCreation(treeHeight);
		
		Node runningNode = root;
		while(!(runningNode instanceof LeafNode)){
			runningNode = runningNode.getLeftSon();
		}
		leftmostLeaf = (LeafNode) runningNode;
		LeafNode runningLeaf = leftmostLeaf;
		while(runningLeaf.getRightNieghbor() != null){
			runningLeaf = (LeafNode) runningLeaf.getRightNieghbor();
		}// tree is built up and set. now lets fill it.
		rightmostLeaf = runningLeaf;
		runningLeaf = leftmostLeaf;
		Transaction lastTransaction = null;
		for (Iterator<Transaction> iterator = leafTransactions.iterator(); iterator.hasNext();) {
			lastTransaction = iterator.next();
			runningLeaf.setTransaction(lastTransaction);
			runningLeaf = (LeafNode) runningLeaf.getRightNieghbor();
		}
		while(runningLeaf != null){
			runningLeaf.setTransaction(lastTransaction);
			runningLeaf = (LeafNode) runningLeaf.getRightNieghbor();
		}//now all leaves should have they hash. now fill all the other hashes
		runningNode = leftmostLeaf.getFather();
		Node leftmostRunningNode = runningNode;
		Hasher hasher = new DefaultHasher();
		while(!(runningNode instanceof RootNode)){
			HashValue leftHash = runningNode.getLeftSon().getHash();
			HashValue rightHash = runningNode.getRightSon().getHash();
			runningNode.setHash(hasher.hashHashes(leftHash, rightHash));
			if(runningNode.getRightNieghbor() != null){
				runningNode = runningNode.getRightNieghbor();
			}
			else{
				runningNode = leftmostRunningNode.getFather();
				leftmostRunningNode = runningNode;
			}
		}
		HashValue leftHash = root.getLeftSon().getHash();
		HashValue rightHash = root.getRightSon().getHash();
		root.setHash(hasher.hashHashes(leftHash, rightHash));
	}
	
	private RootNode iterativeTreeCreation(int height){
		int numberOfLeaves = (int) Math.pow(2, height);
		LeafNode firstLeaf = null;
		LeafNode prevLeaf = null;
		for (int i = 0; i < numberOfLeaves; i++) {
			LeafNode leaf = new LeafNode();
			if(i == 0){
				firstLeaf = leaf;
			}
			leaf.setLeftNieghbor(prevLeaf);
			if(prevLeaf != null){
				prevLeaf.setRightNieghbor(leaf);
			}
			prevLeaf = leaf;
		} //done with all the leaves
		Node firstNode = null;
		Node prevNode = null;
		Node prevLevelFirstNode = firstLeaf;
		Node prevLevelRunningNode = firstLeaf;
		while( prevLevelFirstNode.getLevel() < height -1){
			while(prevLevelRunningNode != null){
				//level creation
				Node sonL = prevLevelRunningNode;
				Node sonR = prevLevelRunningNode.getRightNieghbor();
				Node node = new Node(sonL, sonR);
				if(firstNode == null){
					firstNode = node;
				}
				node.setLeftNieghbor(prevNode);
				if(prevNode != null){
					prevNode.setRightNieghbor(node);
				}
				prevNode = node;
				prevLevelRunningNode = sonR.getRightNieghbor();
			}
			prevLevelFirstNode = firstNode;
			prevLevelRunningNode = firstNode;
			firstNode = null;
			prevNode = null;			
		}
		RootNode root = new RootNode(prevLevelFirstNode, prevLevelFirstNode.getRightNieghbor());
		return root;
	}
	
	protected HashValue root() {
		return this.root.getHash();
	}
	
	protected RootNode getRootNode() {
		return this.root;
	}
	
	protected int height(){
		return treeHeight;
	}
	
private LeafNode getLeftmostLeaf() {
		return leftmostLeaf;
	}

	private LeafNode getRightmostLeaf() {
		return rightmostLeaf;
	}

//	private int floorLogBase2(int argument){
//		double log = Math.log(argument) / Math.log(2);
//		return (int) Math.floor(log);
//	}
	
	private int ceilLogBase2(int argument){
		double log = Math.log(argument) / Math.log(2);
		return (int) Math.ceil(log);
	}
	
	@Override
	public int hashCode() {
		return root().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(! (obj instanceof DefaultMerkleTree)){
			return false;
		}
		DefaultMerkleTree other = (DefaultMerkleTree) obj;
		return root().equals(other.root());
	}

	private class Node{
		private int level;
		
		private Node father;
		private Node leftSon;
		private Node rightSon;
		
		private Node leftNieghbor;
		private Node rightNieghbor;
		
		private HashValue hash;
		
		public Node() {
			// Auto-generated constructor stub
		}
		
		public Node(Node leftSon, Node rightSon) {
			if(leftSon.level != rightSon.level){
				throw new IllegalArgumentException("Left and right son node should be at the same level!");
			}
			setLeftSon(leftSon);
			setRightSon(rightSon);
			
			setLevel(leftSon.getLevel() + 1);
			
			leftSon.setFather(this);
			rightSon.setFather(this);;
			
//			leftSon.setRightNieghbor(rightSon);
//			rightSon.setLeftNieghbor(leftSon);
		}

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		public Node getFather() {
			return father;
		}

		public void setFather(Node father) {
			this.father = father;
		}

		public Node getLeftSon() {
			return leftSon;
		}

		public void setLeftSon(Node leftSon) {
			this.leftSon = leftSon;
		}

		public Node getRightSon() {
			return rightSon;
		}

		public void setRightSon(Node rightSon) {
			this.rightSon = rightSon;
		}

		public void setLeftNieghbor(Node leftNieghbor) {
			this.leftNieghbor = leftNieghbor;
		}

		public Node getLeftNieghbor() {
			return leftNieghbor;
		}

		public Node getRightNieghbor() {
			return rightNieghbor;
		}

		public void setRightNieghbor(Node rightNieghbor) {
			this.rightNieghbor = rightNieghbor;
		}

		public HashValue getHash() {
			return hash;
		}

		public void setHash(HashValue hash) {
			this.hash = hash;
		}
	}
	
	private class LeafNode extends Node{
	
		private Transaction transaction;
		
		public LeafNode() {
			setLevel(0);
		}

		public void setTransaction(Transaction transaction) {
			this.transaction = transaction;
			Hasher hasher = new DefaultHasher();
			setHash(hasher.hashTransaction(this.transaction));
		}

		public Transaction getTransaction() {
			return transaction;
		}
	}
	
	private class RootNode extends Node{
		public RootNode(Node leftSon, Node rightSon) {
			if(leftSon.getLevel() != rightSon.getLevel()){
				throw new IllegalArgumentException("Left and right son node should be at the same level!");
			}
			setLeftSon(leftSon);
			setRightSon(rightSon);
			
			setLevel(leftSon.getLevel()+1);
			
			leftSon.setFather(this);
			rightSon.setFather(this);
			
			leftSon.setRightNieghbor(rightSon);
			rightSon.setLeftNieghbor(leftSon);
		}
	}
	
	/**
	 * Objects of this class should be used to
	 * traverse the {@link DefaultMerkleTree} objects.
	 * 
	 * @author vic
	 *
	 */
	public static class TreeCursor{
		private DefaultMerkleTree tree;
		private Node current;
		
		/**
		 * Construct a node cursor to traverse a given tree.
		 * An {@link IllegalArgumentException} is raised if <tt>tree</tt> is null.
		 * 
		 * @param tree the {@link DefaultMerkleTree} to be traversed.
		 * @throws IllegalArgumentException if <tt>tree</tt> is <tt>null</tt>.
		 */
		public TreeCursor(DefaultMerkleTree tree) {
			if(tree == null){
				throw new IllegalArgumentException("A valid tree should be provided.");
			}
			this.tree = tree;
			this.current = tree.getLeftmostLeaf();
		}
		
		/**
		 * Set the cursor position at the root node.
		 */
		public void setCursorAtRoot(){
			this.current = tree.getRootNode();
		}
		
		/**
		 * Set the cursor position at the leftmost leaf.
		 */
		public void setCursorAsLeftmostLeaf(){
			this.current = tree.getLeftmostLeaf();
		}
		
		/**
		 * Set the cursor position at the rightmost leaf.
		 */
		public void setCursorAsRightmostLeaf(){
			this.current = tree.getRightmostLeaf();
		}
		
		/**
		 * @return <tt> true</tt> if this {@link TreeCursor} is on a leaf node
		 */
		public boolean isLeaf(){
			return (current instanceof LeafNode);
		}
		
		/**
		 * @return <tt> true</tt> if this {@link TreeCursor} is on a the root node
		 */
		public boolean isRoot(){
			return (current instanceof RootNode);
		}
		
		/**
		 * @return <tt> true</tt> if this {@link TreeCursor} is on a node that has a node to its right 
		 */
		public boolean hasRightNeighbor(){
			return (current.getRightNieghbor() != null);
		}
		
		/**
		 * @return <tt> true</tt> if this {@link TreeCursor} is on a node that has a node to its left
		 */
		public boolean hasLeftNeighbor(){
			return (current.getLeftNieghbor() != null);
		}
		
		/**
		 * @return <tt>true</tt> if this {@link TreeCursor} was "moved" to the father node. <tt>false</tt> otherwise.
		 */
		public boolean moveToFather(){
			if(isRoot()){
				return false;
			}
			current = current.getFather();
			return true;
		}
		
		/**
		 * @return <tt>true</tt> if this {@link TreeCursor} was "moved" to the left son node. <tt>false</tt> otherwise.
		 */
		public boolean moveToLeftSon(){
			if(isLeaf()){
				return false;
			}
			current = current.getLeftSon();
			return true;
		}
		
		/**
		 * @return <tt>true</tt> if this {@link TreeCursor} was "moved" to the right son node. <tt>false</tt> otherwise.
		 */
		public boolean moveToRightSon(){
			if(isLeaf()){
				return false;
			}
			current = current.getRightSon();
			return true;
		}
		
		/**
		 * @return <tt>true</tt> if this {@link TreeCursor} was "moved" to the left brother node. <tt>false</tt> otherwise.
		 */
		public boolean moveToLeft(){
			if(current.getLeftNieghbor() == null){
				return false;
			}
			current = current.getLeftNieghbor();
			return true;
		}
		
		/**
		 * @return <tt>true</tt> if this {@link TreeCursor} was "moved" to the right brother node. <tt>false</tt> otherwise.
		 */
		public boolean moveToRight(){
			if(current.getRightNieghbor() == null){
				return false;
			}
			current = current.getRightNieghbor();
			return true;
		}
		
		/**
		 * If this {@link TreeCursor} references a leaf this method will return the
		 * {@link Transaction} associated to that leaf. Otherwise it will return null.
		 * 
		 * @return the {@link Transaction} if {@link TreeCursor} is a leaf. <tt>null</tt> otherwise.
		 */
		public Transaction getTransaction(){
			if(isLeaf()){
				return ((LeafNode) current).getTransaction();
			}
			return null;
		}
		
		/**
		 * If this {@link TreeCursor} references an internal node this method will return the
		 * {@link HashValue} associated to that node. Otherwise it will return null.
		 * 
		 * @return the {@link HashValue} if {@link TreeCursor} is an internal node. <tt>null</tt> otherwise.
		 */
		public HashValue getHashValue(){
			if(isLeaf()){
				return null;
			}
			return current.getHash();
		}
		
		/**
		 * @return the {@link HashValue} of this node father if it does exist. If this node is the root returns <tt>null</tt>
		 */
		public HashValue getFatherHash(){
			if(isRoot()){
				return null;
			}
			return current.getFather().getHash();
		}
	}

}
