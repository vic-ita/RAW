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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.dht.DhtConstants;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.DhtKey;

/**
 * @author vic
 *
 */
public class DefaultDhtKey implements DhtKey {
	
	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = -654865005361839751L;
	
	private String key;
	private DhtID keyId;
	
	/**
	 * Create a new {@link DhtKey} based on a string key
	 * 
	 * @param key the string key
	 * @throws IllegalArgumentException if <tt>key</tt> {@link String#length()} exceeds {@link DhtConstants#MAX_KEY_STRING_SIZE}
	 */
	public DefaultDhtKey(String key) throws IllegalArgumentException{
		if(key.length() > DhtConstants.MAX_KEY_STRING_SIZE){
			throw new IllegalArgumentException("String length exceed maximum size ("+DhtConstants.MAX_KEY_STRING_SIZE+").");
		}
		this.key = key;
		DhtHasher hasher = DefaultDhtCore.getCore().getCurrentHasher();
		keyId = hasher.hashString(this.key);
	}
	
	/**
	 * Create a {@link DhtKey} containing solely its {@link DhtID} representation
	 * 
	 * @param keyId the {@link DhtID} key hash
	 */
	public DefaultDhtKey(DhtID keyId) {
		this.keyId = keyId;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtKey#getKeyString()
	 */
	@Override
	public String getKeyString() {
		return key;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtKey#getKeyId()
	 */
	@Override
	public DhtID getKeyId() {
		return keyId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(keyId);
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DhtKey)){
			return false;
		}
		DhtKey other = (DhtKey) obj;
		boolean result = true;
		if(key != null && other.getKeyString() != null){
			result = result && key.equals(other.getKeyString());
		}
		if(result){
			result = result && keyId.equals(other.getKeyId());
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return key+" <-> "+keyId.toString();
	}

}
