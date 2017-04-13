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
package raw.blockChain.api.implementations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.util.Arrays;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.blockChain.api.Block;
import raw.blockChain.api.Transaction;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.implementations.DefaultDhtHasher;
import raw.dht.implementations.DefaultDhtID;
import raw.dht.implementations.utils.DhtSigningUtils;

/**
 * Implementation of {@link Transaction} interface.
 * 
 * @author vic
 *
 */
public class DefaultTransaction implements Transaction {

	/**
	 * random generated serial
	 */
	private static final long serialVersionUID = 126672759222254325L;
	
	private DhtID id;
	private long transactionNonce;
	private long seedBlockNumber;
	private PublicKey pubKey;
	
	private final int version;
	
	public DefaultTransaction(DhtID id, long transactionNonce, long seedBlockNumber, PublicKey publicKey) {
		this(id, transactionNonce, seedBlockNumber, publicKey, 1);
	}
	
	public DefaultTransaction(DhtID id, long transactionNonce, long seedBlockNumber, byte[] publicKeyBytes) {
		this(id, transactionNonce, seedBlockNumber, publicKeyBytes, 1);
	}
	
	public DefaultTransaction(DhtID id, long transactionNonce, long seedBlockNumber, String publicKeyHex) {
		this(id, transactionNonce, seedBlockNumber, publicKeyHex, 1);
	}
	
	public DefaultTransaction(DhtID id, long transactionNonce, long seedBlockNumber, byte[] publicKeyBytes, int version) {
		this(id, transactionNonce, seedBlockNumber, DhtSigningUtils.regeneratePublicKey(publicKeyBytes), version);
	}
	
	public DefaultTransaction(DhtID id, long transactionNonce, long seedBlockNumber, String publicKeyHex, int version) {
		this(id, transactionNonce, seedBlockNumber, DhtSigningUtils.regeneratePublicKey(publicKeyHex), version);
	}
	public DefaultTransaction(DhtID id, long transactionNonce, long seedBlockNumber, PublicKey publicKey, int version) {
		this.id = id;
		this.transactionNonce = transactionNonce;
		this.seedBlockNumber = seedBlockNumber;
		this.pubKey = publicKey;
		this.version = version;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.interfaces.Transaction#getVersion()
	 */
	@Override
	public int getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.interfaces.Transaction#getDhtID()
	 */
	@Override
	public DhtID getDhtID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.Transaction#getTransactionNonce()
	 */
	@Override
	public long getTransactionNonce() {
		return transactionNonce;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.Transaction#getCreationSeedNumber()
	 */
	@Override
	public long getCreationSeedNumber() {
		return seedBlockNumber;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.Transaction#getPublicKey()
	 */
	@Override
	public PublicKey getPublicKey() {
		return pubKey;
	}

	@Override
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		
		byte[] retVal = baos.toByteArray();
		
		oos.close();
		baos.close();
		
		return retVal;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Transaction)){
			return false;
		}
		Transaction compared = (Transaction) obj;
		if(!this.id.equals(compared.getDhtID())){
			return false;
		}
		if(this.transactionNonce != compared.getTransactionNonce()){
			return false;
		}
		if(this.seedBlockNumber != compared.getCreationSeedNumber()){
			return false;
		}
		if(pubKey == null){
			if(compared.getPublicKey() == null){
				return true;
			} else {
				return false; 
			}
		} else {			
			if(!this.pubKey.equals(compared.getPublicKey())){
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id).
		append(transactionNonce).
		append(seedBlockNumber).
		append(pubKey).
		append(version);

		return builder.toHashCode();
	}

	@Override
	public String toString() {
		return id+"§"+transactionNonce+"/"+seedBlockNumber+"@"+DhtSigningUtils.publicKeyHexRepresentation(pubKey);
	}
	
	/**
	 * This convenience method will return a {@link Transaction} with an invalid
	 * id and address. This transaction can be used when no {@link Transaction} is
	 * available to mine a new {@link Block}.
	 * 
	 * @return a special invalid {@link Transaction}
	 */
	public static Transaction getNullTransaction() {
		DhtHasher hasher = new DefaultDhtHasher();
		byte[] zeros = new byte[hasher.hashLength()];
		Arrays.fill(zeros, (byte)0);
		DhtID id = new DefaultDhtID(zeros);
		
		zeros = new byte[DhtSigningUtils.getPublicKeyByteLength()];
		Arrays.fill(zeros, (byte)0);
		PublicKey publicKey = DhtSigningUtils.regeneratePublicKey(zeros);
		
		Transaction returned = new DefaultTransaction(id, 0L, 0L, publicKey);
		
		return returned;
	}

}
