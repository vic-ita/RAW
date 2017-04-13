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
package raw.blockChain.services.dbHelper.implementations;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.implementations.DefaultBlockHeader;
import raw.blockChain.api.implementations.DefaultHashValue;
import raw.blockChain.api.implementations.DefaultBlockHeader.Builder;
import raw.blockChain.exceptions.IllegalBlockHeaderBytesRepresentation;
import raw.blockChain.exceptions.IncompleteBuilderSettingsException;
import raw.logger.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * This {@link BlockHeader} implementation is to be used
 * <b>SPECIFICALLY</b> to store {@link BlockHeader}s into
 * the database.
 * 
 * @author vic
 *
 */
@DatabaseTable(tableName = "blockheaders")
public class DataBaseBlockHeader implements BlockHeader {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = 4061441112024782245L;
	
	public static final String ID_FIELD_NAME = "header_id";
	public static final String VERSION_FIELD_NAME = "version";
	public static final String BLOCKCHAIN_NAME_FIELD_NAME = "name";
	public static final String BLOCK_NUMBER_FIELD_NAME = "number";
	public static final String HASH_FIELD_NAME = "hash";
	public static final String PREVIOUS_HASH_FIELD_NAME = "prevhash";
	public static final String MERKLE_ROOT_FIELD_NAME = "merkle";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String DIFFICULTY_FIELD_NAME = "difficulty";
	public static final String NONCE_FIELD_NAME = "nonce";
	public static final String MINER_SIGNATURE_NAME = "miner_signature";
	
	@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
	private int id;
	@DatabaseField(columnName = VERSION_FIELD_NAME)
	private int version;	
	@DatabaseField(columnName = BLOCKCHAIN_NAME_FIELD_NAME)
	private String blockChainName;
	@DatabaseField(columnName = BLOCK_NUMBER_FIELD_NAME, unique=true)
	private long blockNumber;
	@DatabaseField(columnName = HASH_FIELD_NAME)
	private String hash;
	@DatabaseField(columnName = PREVIOUS_HASH_FIELD_NAME, unique=true)
//	@DatabaseField(columnName = PREVIOUS_HASH_FIELD_NAME)
	private String previosuBlockHash;
	@DatabaseField(columnName = MERKLE_ROOT_FIELD_NAME)
	private String merkleRoot;
	@DatabaseField(columnName = TIMESTAMP_FIELD_NAME)
	private long timestamp;
	@DatabaseField(columnName = DIFFICULTY_FIELD_NAME)
	private String currentDifficulty;
	@DatabaseField(columnName = NONCE_FIELD_NAME)
	private int nonce;
	@DatabaseField(columnName = MINER_SIGNATURE_NAME)
	private String minerSignature;
	
	public DataBaseBlockHeader() {
		// Empty constructor for ORM 
	}
	
	public DataBaseBlockHeader(BlockHeader originalHeader) {
		version = originalHeader.getVersion();
		blockChainName = originalHeader.getBlockChainName();
		blockNumber = originalHeader.getBlockNumber();
		hash = originalHeader.hash().toHexString();
		previosuBlockHash = originalHeader.previousBlock().toHexString();
		merkleRoot = originalHeader.merkleRoot().toHexString();
		timestamp = originalHeader.timestamp();
		currentDifficulty = originalHeader.currentDifficulty().toPlainString();
		nonce = originalHeader.nonce();
		minerSignature = originalHeader.getMinerSignature();
	}
	
	public int getId(){
		return id;
	}

	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public String getBlockChainName() {
		return blockChainName;
	}

	@Override
	public long getBlockNumber() {
		return blockNumber;
	}

	@Override
	public HashValue hash() {
		return new DefaultHashValue(hash);
	}

	@Override
	public HashValue previousBlock() {
		return new DefaultHashValue(previosuBlockHash);
	}

	@Override
	public HashValue merkleRoot() {
		return new DefaultHashValue(merkleRoot);
	}

	@Override
	public long timestamp() {
		return timestamp;
	}

	@Override
	public BigDecimal currentDifficulty() {
		return new BigDecimal(currentDifficulty);
	}

	@Override
	public int nonce() {
		return nonce;
	}

	@Override
	public String getMinerSignature() {
		return minerSignature;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return null;
	}

	@Override
	public byte[] getHashableBytes() {
		return null;
	}
	
	/**
	 * @return a plain version of this {@link BlockHeader} (not the
	 * database-adapted one)
	 */
	public BlockHeader getPlainBlockHeader() {
		DefaultBlockHeader.Builder builder = new Builder();
		builder.setHeaderVersion(version).
		setBlockChainName(blockChainName).
		setBlockNumber(blockNumber).
		setPrevBlockHash(new DefaultHashValue(previosuBlockHash)).
		setMerkleRoot(new DefaultHashValue(merkleRoot)).
		setTimestamp(timestamp).
		setDifficulty(new BigDecimal(currentDifficulty)).
		setNonce(nonce).
		setMinerSignature(minerSignature); 
		
		Log tmpLogger = Log.getLogger();
		
		BlockHeader header = null;
		try {
			header = builder.build();
		} catch (ClassNotFoundException e) {
			tmpLogger.exception(e);
		} catch (IncompleteBuilderSettingsException e) {
			tmpLogger.exception(e);
		} catch (IOException e) {
			tmpLogger.exception(e);
		} catch (IllegalBlockHeaderBytesRepresentation e) {
			tmpLogger.exception(e);
		}
		
		return header;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id).
		append(version).
		append(blockChainName).
		append(blockNumber).
		append(hash).
		append(previosuBlockHash).
		append(merkleRoot).
		append(timestamp).
		append(currentDifficulty).
		append(nonce).
		append(minerSignature);
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		BlockHeader thisBlock = getPlainBlockHeader();
		return thisBlock.equals(obj);
	}

}
