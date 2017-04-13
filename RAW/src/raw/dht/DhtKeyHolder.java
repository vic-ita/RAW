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
 * Objects implementing this interface
 * are responsible to hold {@link DhtKey} - {@link DhtValue}
 * references.
 * 
 * @author vic
 *
 */
public interface DhtKeyHolder {
	
	/**
	 * Attempts to store a couple of 
	 * {@link DhtKey} - {@link DhtValue}
	 * to be held by this node.
	 * 
	 * @param key a {@link DhtKey}
	 * @param value the {@link DhtValue} relative to <tt>key</tt>
	 * @return <tt>true</tt> if successfully stored, <tt>false</tt> otherwise (e.g.: if couple is refused)
	 */
	public boolean store(DhtKey key, DhtValue value);
	
	/**
	 * Search for a {@link DhtKey} in this {@link DhtKeyHolder}
	 * and return the associated {@link DhtValue} if present or
	 * <tt>null</tt> otherwise.
	 * 
	 * @param key a searched {@link DhtKey}
	 * @return the associated collection of {@link DhtValue}s if present or <tt>null</tt> otherwise
	 */
	public Collection<DhtValue> get(DhtKey key);
	
	/**
	 * Delete a couple {@link DhtKey} - {@link DhtValue}.
	 * 
	 * @param key a {@link DhtKey}
	 * @param value the associated {@link DhtValue}
	 * @return <tt>true</tT> if successfully deleted, <tt>false</tt> otherwise
	 */
	public boolean delete(DhtKey key, DhtValue value);

}
