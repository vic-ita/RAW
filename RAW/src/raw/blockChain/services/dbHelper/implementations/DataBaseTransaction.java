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
package raw.blockChain.services.dbHelper.implementations;

import java.io.IOException;
import java.security.PublicKey;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultTransaction;
import raw.dht.DhtID;
import raw.dht.implementations.DefaultDhtID;
import raw.dht.implementations.utils.DhtSigningUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author vic
 *
 */
@DatabaseTable(tableName = "transactions")
public class DataBaseTransaction implements Transaction {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = -8252678387903125752L;
	
	public static final String ID_FIELD_NAME = "transaction_id";
	public static final String VERSION_FIELD_NAME = "version";
	public static final String DHT_ID_FIELD_NAME = "dht_id";
	public static final String TRANSACTION_NONCE_NAME = "trans_nonce";
	public static final String CREATION_SEED_NUMBER_NAME = "creation_seed_number";
	public static final String PUBLIC_KEY_FIELD_NAME = "public_key";
	public static final String POSITION_FIELD_NAME = "position";
	public static final String HEADER_ID_FIELD_NAME = "header_id";
	
	private static final int PUBKEY_DB_LENGTH = 600;
	
	@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
	private int id;
	@DatabaseField(columnName= VERSION_FIELD_NAME)
	private int version;
	@DatabaseField(columnName = DHT_ID_FIELD_NAME)
	private String dhtID;
	@DatabaseField(columnName=TRANSACTION_NONCE_NAME)
	private long transactionNonce;
	@DatabaseField(columnName=CREATION_SEED_NUMBER_NAME)
	private long creationSeedNumber;
	@DatabaseField(columnName = PUBLIC_KEY_FIELD_NAME, width=PUBKEY_DB_LENGTH)
	private String pubKey;
	
	@DatabaseField(columnName = POSITION_FIELD_NAME)
	private int positionInList;
	
	@DatabaseField(columnName = HEADER_ID_FIELD_NAME)
	private int blockHeaderId;
	
	public DataBaseTransaction() {
		// Empty constructor for ORM 
	}
	
	public DataBaseTransaction(Transaction originalTransaction, int positionInList, DataBaseBlockHeader header) {
		version = originalTransaction.getVersion();
		dhtID = originalTransaction.getDhtID().toHexString();
		transactionNonce = originalTransaction.getTransactionNonce();
		creationSeedNumber = originalTransaction.getCreationSeedNumber();
		pubKey = DhtSigningUtils.publicKeyHexRepresentation(originalTransaction.getPublicKey());
		
		this.positionInList = positionInList;
		
		blockHeaderId = header.getId();
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the positionInList
	 */
	public int getPositionInList() {
		return positionInList;
	}

	/**
	 * @return the blockHeaderId
	 */
	public int getBlockHeaderId() {
		return blockHeaderId;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.Transaction#getVersion()
	 */
	@Override
	public int getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.Transaction#getDhtID()
	 */
	@Override
	public DhtID getDhtID() {
		return new DefaultDhtID(dhtID);
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
		return creationSeedNumber;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.Transaction#getPublicKey()
	 */
	@Override
	public PublicKey getPublicKey() {
		return DhtSigningUtils.regeneratePublicKey(pubKey);
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.Transaction#getBytes()
	 */
	@Override
	public byte[] getBytes() throws IOException {
		return null;
	}
	
	public Transaction getPlainTransaction() {
		return new DefaultTransaction(getDhtID(), getTransactionNonce(), getCreationSeedNumber(), getPublicKey(), version);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id).
		append(version).
		append(dhtID).
		append(transactionNonce).
		append(creationSeedNumber).
		append(pubKey).
		append(positionInList).
		append(blockHeaderId);
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DataBaseTransaction){
			Transaction thisTransaction = getPlainTransaction();
			return thisTransaction.equals(obj);			
		}
		return false;
	}
	
}