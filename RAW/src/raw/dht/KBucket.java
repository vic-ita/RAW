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

/**
 * Utility class to be used as 
 * element for the {@link RoutingTable}.
 * Elements ({@link DhtNode}s) are ordered
 * in a <i>least seen</i> fashion.  
 * 
 * @author vic
 *
 */
public interface KBucket extends Bucket{
	
	/**
	 * @return the number of {@link DhtNodeExtended}s
	 * stored in this bucket
	 */
	public int currentSize();
	
	/**
	 * @return <tt>true</tt> if {@link KBucket#currentSize()} == 0
	 */
	public boolean isEmpty();
	
	/**
	 * Insert in the {@link KBucket} a {@link DhtNodeExtended} and
	 * updates / refresh its position in the bucket.
	 *  
	 * @param node a {@link DhtNodeExtended}
	 */
	public void insertNode(DhtNodeExtended node);
	
	/**
	 * Remove the reference in this {@link KBucket} to a
	 * given {@link DhtNodeExtended}.
	 *  
	 * @param node the {@link DhtNodeExtended} to be deleted
	 * @return <tt>true</tt> if successfully deleted, <tt>false</tt> otherwise
	 */
	public boolean deleteNode(DhtNodeExtended node);
	
	/**
	 * Return <tt>numberOfNodes</tt> {@link DhtNodeExtended}s stored
	 * in this {@link KBucket}. If <tt>numberOfNodes</tt> is greater then {@link KBucket#currentSize()}
	 * then all nodes are returned as with {@link KBucket#getAllNodes()}.
	 * 
	 * @param numberOfNodes the desired number of {@link DhtNodeExtended}s
	 * @return a {@link Collection} of {@link DhtNodeExtended}s
	 */
	public Collection<DhtNodeExtended> getNodes(int numberOfNodes);
	
	/**
	 * @return a {@link Collection} containing all the {@link DhtNodeExtended}s stored in this {@link KBucket}
	 */
	public Collection<DhtNodeExtended> getAllNodes();

	/**
	 * Checks if this {@link KBucket} contains a given {@link DhtNodeExtended}
	 * 
	 * @param node the searched {@link DhtNodeExtended}
	 * @return <code>true</code> if this {@link KBucket} contains <code>node</code>, <code>false</code> otherwise
	 */
	public boolean contains(DhtNodeExtended node);
}
