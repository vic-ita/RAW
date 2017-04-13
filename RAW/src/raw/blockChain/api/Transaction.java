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

import java.io.IOException;
import java.io.Serializable;
import java.security.PublicKey;

import raw.dht.DhtID;

/**
 * Objects of this interface represents a transaction
 * to be contained into a block of the block-chain.
 * {@link Transaction}s to be valid <b>must</b> include
 * a nonce to validate the effort spent in creating it.
 * 
 * @author vic
 *
 */
public interface Transaction extends Serializable {
	
	/**
	 * @return the version of this {@link Transaction}
	 */
	public int getVersion();
	
	/**
	 * @return the {@link DhtID} stored in this {@link Transaction}
	 */
	public DhtID getDhtID();
	
	/**
	 * @return a long integer that, hashed with the value returned by {@link Transaction#getDhtID()}, validate the transaction
	 */
	public long getTransactionNonce();
	
	/**
	 * @return the long identifying a "seed" {@link Block}
	 */
	public long getCreationSeedNumber();
	
	/**
	 * @return the {@link PublicKey} coupled with the {@link DhtID} stored in this {@link Transaction}
	 */
	public PublicKey getPublicKey();
	
	/**
	 * @return a byte array representation of this object
	 * @throws IOException  if a problem arises while serializing this object
	 */
	public byte[] getBytes() throws IOException;

}
