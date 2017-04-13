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

import java.util.Collection;

import raw.dht.utils.nodesInfoServer.NodesInfoServer;

public interface RoutingTable {
	
	/**
	 * Find a {@link Collection} of (known) {@link DhtNodeExtended} whose {@link DhtID} is closest
	 * to the a given {@link DhtID}.
	 * 
	 * @param searchedID the {@link DhtID} searched
	 * @return a {@link Collection} of the the {@link DhtNodeExtended} closest to <tt>searchedID</tT>
	 */
	public Collection<DhtNodeExtended> findClosest(DhtID searchedID);
	
	/**
	 * This method is used to select a known {@link DhtNodeExtended}
	 * to whom a ping should be sent. This method <b>MAY</b>
	 * fail to find a {@link DhtNodeExtended} reference and return 
	 * <tt>null</tt> instead.
	 * 
	 * @return a random {@link DhtNodeExtended} from the known ones (or <tt>null</tT> if unable to resolve one)
	 */
	public DhtNodeExtended randomNode();
	
	/**
	 * Insert in the routing table a new {@link DhtNodeExtended}
	 * or refresh its entry if already present. 
	 * 
	 * @param node a new {@link DhtNodeExtended}
	 * @return <tt>true</tT> if {@link DhtNodeExtended} was successfully inserted
	 */
	public boolean insertNode(DhtNodeExtended node);
	
	/**
	 * Delete from this routing table a {@link DhtNodeExtended}.
	 * Should be used in case of a failed ping reply.
	 * 
	 * @param node the {@link DhtNodeExtended} to be deleted
	 * @return <tt>true</tT> if {@link DhtNodeExtended} was successfully deleted
	 */
	public boolean removeNode(DhtNodeExtended node);
	
	/**
	 * This method will return all known {@link DhtNodeExtended}s
	 * referred by this  {@link RoutingTable}. If askNewNodes
	 * is set to <code>true</code> and this {@link RoutingTable} does
	 * not refer enough {@link DhtNodeExtended}s, this node will try to contact
	 * a {@link NodesInfoServer} and will ask to it a new
	 * set of {@link DhtNodeExtended}s
	 * 
	 * @param askNewNodes if <code>true</code> try to ask nodes to {@link NodesInfoServer}s if local {@link RoutingTable} is empty.
	 * @return a complete {@link Collection} of all known {@link DhtNodeExtended} or <code>null</code> if no node is known.
	 */
	public Collection<DhtNodeExtended> getFullSetOfNodes(boolean askNewNodes);
	
	/**
	 * Checks if a given {@link DhtNodeExtended} is already
	 * present in this {@link RoutingTable}.
	 * 
	 * @param node the {@link DhtNodeExtended} to be checked
	 * @return <code>true</code> if <code>node</code> is present in this {@link RoutingTable}, <code>false</code> otherwise
	 */
	public boolean isPresentInTable(DhtNodeExtended node);

}
