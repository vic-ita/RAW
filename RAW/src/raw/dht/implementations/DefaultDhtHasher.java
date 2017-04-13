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

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import raw.dht.DhtHasher;
import raw.dht.DhtID;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Implements {@link DhtHasher}. Generating
 * valid {@link DhtID} objects from a given input.
 * 
 * @author vic
 *
 */
public class DefaultDhtHasher implements DhtHasher {
	
	private HashFunction digester;
	private byte[] salt;
	
	/**
	 * Build a {@link DhtHasher} without any salt
	 */
	public DefaultDhtHasher() {		
		this(null);
	}
	
	/**
	 * Build a {@link DhtHasher} without any salt
	 * using a set of bytes as salt.
	 * 
	 * @param salt the bytes salt
	 */
	public DefaultDhtHasher(byte[] salt) {
		digester = Hashing.sha512();
		this.salt = salt;
	}

	/* (non-Javadoc)
	 * @see raw.dht.interfaces.DhtHasher#hashBytes(byte[])
	 */
	@Override
	public DhtID hashBytes(byte[] bytes) {
		return new DefaultDhtID(digestBytes(bytes));
	}

	/* (non-Javadoc)
	 * @see raw.dht.interfaces.DhtHasher#hashString(java.lang.String)
	 */
	@Override
	public DhtID hashString(String string) {
		return new DefaultDhtID(digestString(string));
	}

	/* (non-Javadoc)
	 * @see raw.dht.interfaces.DhtHasher#hashLength()
	 */
	@Override
	public int hashLength() {
		int length = digester.bits() / 8;
		return length;
	}
	
	@Override
	public DhtID hashHexString(String hex) throws DecoderException {
		byte[] hexBytes = Hex.decodeHex(hex.toCharArray());
		return new DefaultDhtID(digestBytes(hexBytes));
	}

	private byte[] digestBytes(byte[] bytes){
		byte[] retVal;
//		byte[] retVal = digester.hashBytes(bytes).asBytes();
		if(salt == null){
			retVal = digester.hashBytes(bytes).asBytes();
		} else {
			retVal = digester.newHasher().putBytes(bytes).putBytes(salt).hash().asBytes();
		}
		return retVal;
	}
	
	private byte[] digestString(String string){
		return digestBytes(string.getBytes());
	}
	
	protected byte[] getSalt(){
		return salt;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DefaultDhtHasher)){
			return false;
		}
		DefaultDhtHasher other = (DefaultDhtHasher) obj;
		return Arrays.equals(salt, other.getSalt());
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtHasher#hashDhtIDwithLongNonce(raw.dht.DhtID, long)
	 */
	@Override
	public DhtID hashDhtIDwithLongNonce(DhtID id, long nonce) {
		byte[] nonceBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(nonce).array();
		byte[] toBeDigested = ArrayUtils.addAll(id.toByteArray(), nonceBytes);
		return new DefaultDhtID(digestBytes(toBeDigested));
	}

}
