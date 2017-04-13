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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import raw.dht.DhtConstants;
import raw.dht.DhtNodeExtended;
import raw.dht.KBucket;

/**
 * Default implementation of {@link KBucket}
 * 
 * @author vic
 *
 */
public class DefaultKBucket implements KBucket {
	
	private ConcurrentLinkedQueue<DhtNodeExtended> bucket;
	private int maxSize;
	
	public DefaultKBucket() {
		bucket = new ConcurrentLinkedQueue<DhtNodeExtended>();
		maxSize = DhtConstants.K_SIZE;
	}

	/* (non-Javadoc)
	 * @see raw.dht.implementations.KBucket#currentSize()
	 */
	@Override
	public int currentSize() {
		return bucket.size();
	}

	/* (non-Javadoc)
	 * @see raw.dht.implementations.KBucket#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return (bucket.size() == 0);
	}

	/* (non-Javadoc)
	 * @see raw.dht.implementations.KBucket#insertNode(raw.dht.DhtNode)
	 */
	@Override
	public void insertNode(DhtNodeExtended node) {
		if(currentSize() < maxSize && !(bucket.contains(node))){
			bucket.add(node);
			return;
		}
		
		if(!bucket.remove(node)){
			//node was not present in the bucket. 
			bucket.remove(); //remove the least seen.
		}
		bucket.add(node); //put as last seen the node.
		return;
	}

	/* (non-Javadoc)
	 * @see raw.dht.implementations.KBucket#getNodes(int)
	 */
	@Override
	public Collection<DhtNodeExtended> getNodes(int numberOfNodes) {
		if(numberOfNodes >= currentSize()){
			return getAllNodes();
		}
		List<DhtNodeExtended> tmp = Collections.list(Collections.enumeration(bucket));
		return tmp.subList(0, numberOfNodes);
	}

	/* (non-Javadoc)
	 * @see raw.dht.implementations.KBucket#getAllNodes()
	 */
	@Override
	public Collection<DhtNodeExtended> getAllNodes() {
		return bucket;
	}

	/* (non-Javadoc)
	 * @see raw.dht.KBucket#deleteNode(raw.dht.DhtNode)
	 */
	@Override
	public boolean deleteNode(DhtNodeExtended node) {
		return bucket.remove(node);
	}

	/* (non-Javadoc)
	 * @see raw.dht.KBucket#contains(raw.dht.DhtNode)
	 */
	@Override
	public boolean contains(DhtNodeExtended node) {
		return bucket.contains(node);
	}

}
