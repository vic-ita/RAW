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

import java.security.PublicKey;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.dht.DhtAddress;
import raw.dht.DhtID;
import raw.dht.DhtNode;
import raw.dht.implementations.utils.DhtSigningUtils;

/**
 * @author vic
 *
 */
public class DefaultDhtNode implements DhtNode {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = -1892870890390065598L;
	
	private DhtID myId;
	private PublicKey myPublicKey;
	private DhtAddress myAddress;
	
	public DefaultDhtNode(DhtID id, PublicKey publicKey, DhtAddress address) {
		myId = id;
		myPublicKey = publicKey;
		myAddress = address;
	}
	
	public DefaultDhtNode(DhtID id, byte[] publicKeyBytes, DhtAddress address) {
		this(id, DhtSigningUtils.regeneratePublicKey(publicKeyBytes), address);
	}
	
	public DefaultDhtNode(DhtID id, String publicKeyHexString, DhtAddress address) {
		this(id, DhtSigningUtils.regeneratePublicKey(publicKeyHexString), address);
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtNode#getID()
	 */
	@Override
	public DhtID getID() {
		return myId;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtNode#getPublicKey()
	 */
	@Override
	public PublicKey getPublicKey() {
		return myPublicKey;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtNode#getAddress()
	 */
	@Override
	public DhtAddress getAddress() {
		return myAddress;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(myId).append(myPublicKey).append(myAddress);
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DhtNode)){
			return false;
		}
		DhtNode theOther = (DhtNode) obj;
		return (myId.equals(theOther.getID()) && myAddress.equals(theOther.getAddress())) && myPublicKey.equals(theOther.getPublicKey());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return myId.toString()+" # "+myAddress.toString()+" # "+DhtSigningUtils.publicKeyHexRepresentation(myPublicKey);
	}

}
