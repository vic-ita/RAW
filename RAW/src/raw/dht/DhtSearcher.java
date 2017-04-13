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
 * Objects implementing this class are used to run the search of a {@link DhtKey}
 * or a {@link DhtID}
 * 
 * @author vic
 *
 */
public interface DhtSearcher {
	
	/**
	 * Lookup the set of {@link DhtNodeExtended}
	 * near to a given {@link DhtID}
	 * 
	 * @param id a searched {@link DhtID}
	 * @return a collection of {@link DhtNodeExtended} "close" to {@link DhtID}
	 */
	public Collection<DhtNodeExtended> lookup(DhtID id);
	
	/**
	 * Lookup the set of {@link DhtNodeExtended}
	 * near to a given {@link DhtKey} 
	 * 
	 * @param key a searched {@link DhtKey}
	 * @return a collection of {@link DhtNodeExtended} "close" to {@link DhtKey}
	 */
	public Collection<DhtNodeExtended> lookup(DhtKey key);
	
	/**
	 * Try to retrieve {@link DhtValue}s associated to
	 * a {@link DhtKey}. If no {@link DhtValue} is retrieved,
	 * <tt>null</tt> is returned.
	 * 
	 * @param key a searched {@link DhtKey}
	 * @return a {@link Collection} o {@link DhtValue}s or <tt>null</tt> if no {@link DhtValue} is found or associated to <tt>key</tT>
	 */
	public Collection<DhtValue> findValues(DhtKey key);
	
	/**
	 * Starts a periodic job checking if on the DHT network
	 * is present a more suitable {@link DhtNode} to host
	 * some of the {@link DhtKey}/{@link DhtValue} couples
	 * currently hosted on this node.
	 */
	public void startKeysMigrator();
	
	/**
	 * Stops the job started with {@link DhtSearcher#startKeysMigrator()}.
	 * 
	 * @return <code>true</code> if the {@link DhtSearcher#startKeysMigrator()} stopped correctly, <code>false</code> otherwise
	 */
	public boolean stopKeysMigrator();

}
