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
package raw.blockChain.api.implementations;

import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import raw.blockChain.api.HashValue;

/**
 * Plain implementation of {@link HashValue}
 * 
 * @author vic
 *
 */
public class DefaultHashValue implements HashValue {

	/**
	 * auto-generated serial
	 */
	private static final long serialVersionUID = -393722621636372264L;
	
	private byte[] myHash;
	
	/**
	 * Create a new {@link HashValue} instance storing
	 * the hash represented by a byte array.
	 * 
	 * @param hashByteArray a byte array represented hash
	 */
	public DefaultHashValue(byte[] hashByteArray) {
		myHash = hashByteArray;
	}
	
	/**
	 * Create a new {@link HashValue} instance storing
	 * the hash by its hexadecimal
	 * string representation.
	 *
	 * @param hashHexString a string hexadecimal representation
	 * @throws IllegalArgumentException if the string contains illegal hexadecimal characters or is of odd length
	 */
	public DefaultHashValue(String hashHexString) throws IllegalArgumentException{
		try {
			myHash = Hex.decodeHex(hashHexString.toCharArray());
		} catch (DecoderException e) {
			throw new IllegalArgumentException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public byte[] toByteArray() {
		return myHash;
	}

	@Override
	public String toHexString() {
		return Hex.encodeHexString(myHash);
	}

	@Override
	public HashValue changeEndianness() {
		byte[] tmpBytes = Arrays.copyOf(myHash, myHash.length);
		ArrayUtils.reverse(tmpBytes);
		return new DefaultHashValue(tmpBytes);
	}

	@Override
	public HashValue maskWith(HashValue mask) {
		byte[] masked = new byte[myHash.length];
		byte[] maskArray = mask.toByteArray();
		
		int length = myHash.length;
		
		if(maskArray.length < length){
			length = maskArray.length;
		}
		
		for(int i = 0; i < length; i++){
			masked[i] = (byte) (myHash[i] & maskArray[i]);
		}
		
		if(length < myHash.length){
			for(int i = length; i < myHash.length; i++){
				masked[i] = myHash[i];
			}
		}
		
		return new DefaultHashValue(masked);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HashValue){
			return Arrays.equals(myHash, ((HashValue)obj).toByteArray());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(myHash);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toHexString();
	}

}
