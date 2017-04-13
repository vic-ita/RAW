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

import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.dht.DhtID;
import raw.logger.Log;

/**
 * @author vic
 *
 */
public class DefaultDhtID implements DhtID {

	/**
	 * random generated id
	 */
	private static final long serialVersionUID = 6948497396919273160L;
	
	private byte[] myID;
	
	/**
	 * Create a new instance of a {@link DhtID}. The provided
	 * raw byte id must be of legal length, an {@link IllegalArgumentException}
	 * will be rised otherwise.
	 *  
	 * @param idBytes the raw bytes ID representation.
	 * @throws IllegalArgumentException if idBytes is null or its length is not valid
	 */
	public DefaultDhtID(byte[] idBytes) throws IllegalArgumentException{
		setId(idBytes);
	}
	
	/**
	 * private method used by constructors
	 * 
	 * @param idBytes the byte array representation of the id
	 */
	private void setId(byte[] idBytes){		
		if(idBytes == null || (idBytes.length != new DefaultDhtHasher().hashLength())){
			throw new IllegalArgumentException("Provided raw bites were null or had invalid length.");
		}
		myID = idBytes;
	}
	
	/**
	 * Create a new instance of a {@link DhtID}. The
	 * id is provided as an hexadecimal string.
	 * 
	 * @param hexIdString the hexadecimal representation of the id
	 */
	public DefaultDhtID(String hexIdString) {
		byte[] id = null;
		try {
			id = Hex.decodeHex(hexIdString.toCharArray());
		} catch (DecoderException e) {
			Log.getLogger().exception(e);
		}
		setId(id);
	}

	/* (non-Javadoc)
	 * @see raw.dht.interfaces.DhtID#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		return myID;
	}

	/* (non-Javadoc)
	 * @see raw.dht.interfaces.DhtID#toHexString()
	 */
	@Override
	public String toHexString() {
		return Hex.encodeHexString(myID);
	}

	/* (non-Javadoc)
	 * @see raw.dht.interfaces.DhtID#maskWith(raw.dht.interfaces.DhtID)
	 */
	@Override
	public DhtID maskWith(DhtID mask) {
		byte[] masked = new byte[myID.length];
		byte[] maskArray = mask.toByteArray();
		
		int length = myID.length;
		
		if(maskArray.length < length){
			length = maskArray.length;
		}
		
		for(int i = 0; i < length; i++){
			masked[i] = (byte) (myID[i] & maskArray[i]);
		}
		
		if(length < myID.length){
			for(int i = length; i < myID.length; i++){
				masked[i] = myID[i];
			}
		}
		return new DefaultDhtID(masked);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DhtID)){
			return false;
		}
		DhtID compared = (DhtID) obj;
		return Arrays.equals(this.myID, compared.toByteArray());
	}

	@Override
	public String toString() {
		String hex = toHexString();
		return hex.substring(0, 7)+"[....]"+hex.substring(hex.length()-7);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(myID);
		return builder.toHashCode();
	}

}
