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
package raw.blockChain.api;

import java.io.Serializable;

/**
 * Wrapprer interface for a value obtained
 * by hashing something.
 * 
 * @author vic
 *
 */
public interface HashValue extends Serializable {
	
	/**
	 * Return the byte array of this hash value.
	 *  
	 * @return a byte array
	 */
	public byte[] toByteArray();
	
	/**
	 * A human readable hexadecimal representation
	 * of this hash value.
	 * 
	 * @return an hex string
	 */
	public String toHexString();
	
	/**
	 * Create a copy of this {@link HashValue} but with
	 * byte order changed.
	 * 
	 * @return a new {@link HashValue} with changed byte order endianness
	 */
	public HashValue changeEndianness();
	
	/**
	 * Create a new {@link HashValue} by bitwise masking 
	 * this one with the given <tt>mask</tt>. If this {@link HashValue}
	 * and <tt>mask</tt> differ in length, the new {@link HashValue}
	 * will have this {@link HashValue} length.
	 * 
	 * @param mask and {@link HashValue} to be used as bit mask
	 * @return a new {@link HashValue} wich is obtained by a bitwise "this AND mask"
	 */
	public HashValue maskWith(HashValue mask);

}
