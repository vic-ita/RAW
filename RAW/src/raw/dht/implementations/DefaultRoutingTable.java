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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import raw.dht.Bucket;
import raw.dht.DhtConstants;
import raw.dht.DhtID;
import raw.dht.DhtNode;
import raw.dht.DhtNodeExtended;
import raw.dht.KBucket;
import raw.dht.RoutingTable;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.exceptions.IncompleteNodeExtendedException;
import raw.dht.implementations.utils.DhtNodeAddressBookFile;
import raw.dht.implementations.utils.DhtUtils;
import raw.logger.Log;

/**
 * Default implementation of {@link RoutingTable}.
 * 
 * @author vic
 *
 */
public class DefaultRoutingTable implements RoutingTable {
	
	private DhtNode myNode;
	private InternalNode routingTreeRoot;
	
	private DefaultDhtCore myCore;
	
	private Log log;
	
	/**
	 * Constructs a new {@link RoutingTable} given the
	 * the {@link DhtNode} identifier of the node owning such
	 * a table.
	 * 
	 * @param myNode the {@link DhtNode} of this node
	 * @param owner a {@link DefaultDhtCore} owning this {@link RoutingTable}
	 */
	public DefaultRoutingTable(DhtNode myNode, DefaultDhtCore owner) {
		this.myNode = myNode;
		routingTreeRoot = new InternalNode(this.myNode.getID());
		
		myCore = owner;
		
		log = Log.getLogger();
	}

	/* (non-Javadoc)
	 * @see raw.dht.RoutingTable#findClosest(raw.dht.DhtID)
	 */
	@Override
	public Collection<DhtNodeExtended> findClosest(DhtID searchedID) {
		log.verboseDebug("Searching for "+searchedID);
		String id = DhtUtils.convertIdToBinaryString(searchedID);
		int neededNodes = DhtConstants.ALPHA_SIZE;
		Collection<DhtNodeExtended> foundNodes = recursivelyGatherNodes(routingTreeRoot, id, neededNodes);
		log.verboseDebug("Found a list of "+foundNodes.size()+" nodes.");
		return foundNodes;
	}
	
	private Collection<DhtNodeExtended> recursivelyGatherNodes(Bucket routingTableNode, String id, int neededNodes){
		if(routingTableNode instanceof KBucket){
			Collection<DhtNodeExtended> returnable = ((KBucket)routingTableNode).getNodes(neededNodes);
			HashMap<DhtNodeExtended, Boolean> stillValid = myCore.areCorrectlyOld(returnable);
			returnable = new ArrayList<DhtNodeExtended>();
			for(Entry<DhtNodeExtended, Boolean> entry: stillValid.entrySet()){
				if(entry.getValue()){
					returnable.add(entry.getKey());
				} else {
					removeNode(entry.getKey()); // update the routing table removing now invalid nodes
				}
			}
//			return ((KBucket)routingTableNode).getNodes(neededNodes);
			return returnable;
		}
		if(routingTableNode instanceof MyNode){
			List<DhtNodeExtended> list = new ArrayList<DhtNodeExtended>();
//			list.add(myNode);
			DhtNodeExtended updatedMyNode;
			try {
				updatedMyNode = myCore.getNodeExtended();
				list.add(updatedMyNode);
			} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
				log.exception(e);
			}
			return list;
		}
		InternalNode currentNode = (InternalNode) routingTableNode;
		Bucket primaryNode;
		Bucket fallbackNode;
		if(id.charAt(0) == '0'){
			primaryNode = currentNode.getZeroChild();
			fallbackNode = currentNode.getOneChild();
		} else {
			primaryNode = currentNode.getOneChild();
			fallbackNode = currentNode.getZeroChild();
		}
		String remainingId = id.substring(1);
		HashSet<DhtNodeExtended> nodes = new HashSet<>(recursivelyGatherNodes(primaryNode, remainingId, neededNodes));
		if(nodes.size() == neededNodes){
			return nodes;
		}
		int stillNeeded = neededNodes - nodes.size();
		HashSet<DhtNodeExtended> moreNodes = new HashSet<>(recursivelyGatherNodes(fallbackNode, remainingId, stillNeeded));
		
		nodes.addAll(moreNodes);
		
		return nodes;
	}

	/* (non-Javadoc)
	 * @see raw.dht.RoutingTable#randomNode()
	 */
	@Override
	public DhtNodeExtended randomNode() {
		Collection<DhtNodeExtended> nodes = getFullSetOfNodes(true);
		if(nodes == null || nodes.size() == 0){
			return null;
		}
		int i = (new Random()).nextInt(nodes.size());
		return (DhtNodeExtended) nodes.toArray()[i];

	}

	/* (non-Javadoc)
	 * @see raw.dht.RoutingTable#insertNode(raw.dht.DhtNode)
	 */
	@Override
	public boolean insertNode(DhtNodeExtended node) {
		KBucket bucket = findExactBucket(node);
		if(bucket == null){
			return false;
		}
		bucket.insertNode(node);
		return true;
	}
	
	private KBucket findExactBucket(DhtNode node){
		String id = DhtUtils.convertIdToBinaryString(node.getID());
		Bucket current = routingTreeRoot;
		while (current instanceof InternalNode && !(current instanceof MyNode)) {
			if(id.charAt(0) == '0'){
				current = ((InternalNode)current).getZeroChild();
			} else {
				current = ((InternalNode)current).getOneChild();
			}
			id = id.substring(1);
		}
		if(current instanceof MyNode){
			return null;
		}
		return (KBucket) current;
	}

	/* (non-Javadoc)
	 * @see raw.dht.RoutingTable#removeNode(raw.dht.DhtNode)
	 */
	@Override
	public boolean removeNode(DhtNodeExtended node) {
		KBucket bucket = findExactBucket(node);
		if(bucket == null){
			return false;
		}
		return bucket.deleteNode(node);
	}
	
	private class InternalNode implements Bucket{
		
		private Bucket zeroChild;
		private Bucket oneChild;
		
		public InternalNode() {
			// void constructor
		}
		
		public InternalNode(DhtID id) {
			String myId = DhtUtils.convertIdToBinaryString(id);
			init(myId);
		}
		
		public InternalNode(String remainingId) {
			init(remainingId);
		}
		
		private void init(String remainingId){
			if(remainingId.length() == 1){
				if(remainingId.charAt(0) == '0'){
					zeroChild = new MyNode();
					oneChild = new DefaultKBucket();
				} else {
					zeroChild = new DefaultKBucket();
					oneChild = new MyNode();
				}
			} else {
				if(remainingId.charAt(0) == '0'){
					zeroChild = new InternalNode(remainingId.substring(1));
					oneChild = new DefaultKBucket();
				} else {
					zeroChild = new DefaultKBucket();
					oneChild = new InternalNode(remainingId.substring(1));
				}
			}
		}
		
		public Bucket getZeroChild() {
			return zeroChild;
		}
		
		public Bucket getOneChild() {
			return oneChild;
		}

		
		
	}
	
	private class MyNode extends InternalNode{
		
	}

	/* (non-Javadoc)
	 * @see raw.dht.RoutingTable#getFullSetOfNodes()
	 */
	@Override
	public Collection<DhtNodeExtended> getFullSetOfNodes(boolean askNewNodes) {
		log.verboseDebug("Beginning collection of all nodes.");
		Collection<DhtNodeExtended> nodes = recursiveGetAllNodes(routingTreeRoot);
		log.verboseDebug("Recursion done. Returning. ("+nodes+")");
		if(((nodes == null || nodes.size() < 5) && askNewNodes)){ //FIXME 5 is a rather arbitrary choice...
			log.verboseDebug("No node retrieved. Getting a new set of nodes.");
			DhtNodeAddressBookFile addressBook = new DhtNodeAddressBookFile();
			Collection<DhtNodeExtended> newNodes = addressBook.retrieveFromNodesInfoServers();
			for(DhtNodeExtended node : newNodes){
				insertNode(node);
			}
			log.verboseDebug("Re-Beginning collection of all nodes.");
			nodes = recursiveGetAllNodes(routingTreeRoot);
			log.verboseDebug("Recursion done. Returning. ("+nodes+")");
		}
		return nodes;
//		return recursiveGetAllNodes(routingTreeRoot);
	}
	
	private Collection<DhtNodeExtended> recursiveGetAllNodes(Bucket tableNode){
		if(tableNode instanceof KBucket){
			Collection<DhtNodeExtended> allNodes = ((KBucket)tableNode).getAllNodes();
			HashMap<DhtNodeExtended, Boolean> stillValid = myCore.areCorrectlyOld(allNodes);
			allNodes = new ArrayList<DhtNodeExtended>();
			for(Entry<DhtNodeExtended, Boolean> entry: stillValid.entrySet()){
				if(entry.getValue()){
					allNodes.add(entry.getKey());
				} else {
					removeNode(entry.getKey()); // update the routing table removing now invalid nodes
				}
			}
			return ((KBucket)tableNode).getAllNodes();
		}
		if(tableNode instanceof MyNode){
			return null;			
		}
		if(tableNode instanceof InternalNode){
			InternalNode thisNode = (InternalNode) tableNode;
			Collection<DhtNodeExtended> nodes0 = recursiveGetAllNodes(thisNode.getZeroChild());
			Collection<DhtNodeExtended> nodes1 = recursiveGetAllNodes(thisNode.getOneChild());
			HashSet<DhtNodeExtended> fullSet = new HashSet<>();
			if(nodes0 != null && nodes0.size() > 0){
				fullSet.addAll(nodes0);
			}
			if(nodes1 != null && nodes1.size() > 0){
				fullSet.addAll(nodes1);
			}
			if(fullSet.size() > 0){
				return fullSet;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see raw.dht.RoutingTable#isPresentInTable(raw.dht.DhtNode)
	 */
	@Override
	public boolean isPresentInTable(DhtNodeExtended node) {
		KBucket bucket = findExactBucket(node);
		if(bucket == null){
			return true;
		}
		return bucket.contains(node);
	}

}
