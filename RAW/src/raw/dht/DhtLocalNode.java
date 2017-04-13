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
import java.security.KeyPair;

import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.Transaction;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.exceptions.IncompleteNodeExtendedException;

/**
 * Objects implementing this interface
 * will wrap informations representing
 * a the local DHT node.
 * 
 * @author vic
 *
 */
public interface DhtLocalNode extends Serializable {
	
	/**
	 * @return the {@link DhtNode} of <code>this</code> node
	 */
	public DhtNode getNode();
	
	/**
	 * @return the {@link DhtNodeExtended} of <code>this</code> node or <code>null</code> if no valid {@link DhtNodeExtended} can be created
	 * @throws IncoherentTransactionException 
	 * @throws IncompleteNodeExtendedException 
	 */
	public DhtNodeExtended getNodeExtended() throws IncoherentTransactionException, IncompleteNodeExtendedException;
	
	/**
	 * @return the {@link KeyPair} of <code>this</code> node
	 */
	public KeyPair getKeyPair();
	
	/**
	 * @return the {@link BlockHeader#getBlockNumber()} where 
	 * <code>this</code> node's {@link Transaction} is stored in the block chain 
	 */
	public long getBlockNumber();
	
	/**
	 * @return the {@link Transaction} of this <code>this</code> node
	 */
	public Transaction getTransaction();

}
