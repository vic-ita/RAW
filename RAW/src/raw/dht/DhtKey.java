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

import java.io.Serializable;

/**
 * This class wraps a key to be used in the dht.
 * 
 * @author vic
 *
 */
public interface DhtKey extends Serializable {
	
	/**
	 * Return the query string used in a dht search.
	 * This string length must be at most {@link DhtConstants#MAX_KEY_STRING_SIZE}.
	 * 
	 * @return the query string
	 */
	public String getKeyString();
	
	/**
	 * @return the {@link DhtID} associated with {@link DhtKey#getKeyString()}
	 */
	public DhtID getKeyId();

}
