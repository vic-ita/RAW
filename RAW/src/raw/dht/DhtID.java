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
 * Objects from this interface should represent
 * an ID mapped onto the  ID space of the DHT
 * 
 * @author vic
 *
 */
public interface DhtID extends Serializable{
	
	/**
	 * Return the byte array of this ID value.
	 *  
	 * @return a byte array
	 */
	public byte[] toByteArray();
	
	/**
	 * A human readable hexadecimal representation
	 * of this ID value.
	 * 
	 * @return an hex string
	 */
	public String toHexString();

	/**
	 * Create a new {@link DhtID} by bitwise masking 
	 * this one with the given <tt>mask</tt>. If this {@link DhtID}
	 * and <tt>mask</tt> differ in length, the new {@link DhtID}
	 * will have this {@link DhtID} length.
	 * 
	 * @param mask an {@link DhtID} to be used as bit mask
	 * @return a new {@link DhtID} wich is obtained by a bitwise "this AND mask"
	 */
	public DhtID maskWith(DhtID mask);
	
}
