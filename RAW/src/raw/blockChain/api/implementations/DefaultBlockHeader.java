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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Hasher;
import raw.blockChain.exceptions.IllegalBlockHeaderBytesRepresentation;
import raw.blockChain.exceptions.IncompleteBuilderSettingsException;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * Implementation of {@link BlockHeader} interface. You can instantiate new
 * {@link DefaultBlockHeader} objects {@link DefaultBlockHeader#hash()} using its
 * {@link DefaultIntermediateValuesBuilder}.
 * 
 * @author vic
 *
 */
public class DefaultBlockHeader implements BlockHeader {

	/**
	 * random generated serial
	 */
	private static final long serialVersionUID = -3675472816391063218L;

	private final int headerVersion;
	
	private final String blockChainName;

	/**
	 * this will be computated the first time {@link DefaultBlockHeader#hash()} is
	 * called
	 */
	private transient HashValue blockHash;
	private HashValue prevBlockHash;
	private HashValue merkleRoot;

	private long blockNumber;
	private long timestamp;

	private BigDecimal difficulty;
	private int nonce;
	
	private String minerSignature;

	private DefaultBlockHeader(Builder builder) throws IllegalBlockHeaderBytesRepresentation {
		this.headerVersion = builder.headerVersion;
		
		this.blockChainName = builder.blockChainName;

		this.blockHash = null;

		this.prevBlockHash = builder.prevBlockHash;
		this.merkleRoot = builder.merkleRoot;

		this.blockNumber = builder.blockNumber;
		this.timestamp = builder.timestamp;

		this.difficulty = builder.difficulty;
		this.nonce = builder.nonce;
		
		this.minerSignature = builder.minerSignature;
		
		if(builder.hash != null){
			hash();
			if(! this.blockHash.equals(builder.hash)){
				throw new IllegalBlockHeaderBytesRepresentation("The constructed hash does not match with a computed one.");
			}
		}
	}

	/**
	 * This class can be used to set up a {@link DefaultBlockHeader} object and will
	 * validate its parameters before instantiating one.
	 * 
	 * @author vic
	 *
	 */
	public static class Builder {
		private boolean buildFromBytes;

		private byte[] bytes;

		private int headerVersion = 1;
		
		private String blockChainName;
		private boolean blockChainNameSet;
		
		private HashValue hash;

		private HashValue prevBlockHash;
		private boolean prevBlockSet;
		private HashValue merkleRoot;
		private boolean merkleRootSet;

		private long blockNumber;
		private boolean blockNumberSet;
		private long timestamp;
		private boolean timestampSet;

		private BigDecimal difficulty;
		private boolean difficultySet;
		private int nonce;
		private boolean nonceSet;
		
		private String minerSignature;
		private boolean minerSignatureSet;

		public Builder() {
			// Auto-generated constructor stub
		}

		/**
		 * Convenience constructor. Calling this is equivalent to call the
		 * standard constructor and {@link DefaultIntermediateValuesBuilder#setBytes(byte[])}
		 * 
		 * @param bytes
		 *            a byte array representation of this {@link DefaultBlockHeader}
		 */
		public Builder(byte[] bytes) {
			setBytes(bytes);
		}

		public Builder setBytes(byte[] bytes) {
			buildFromBytes = true;
			this.bytes = bytes;
			return this;
		}

		public Builder setHeaderVersion(int headerVersion) {
			buildFromBytes = false;
			this.headerVersion = headerVersion;
			return this;
		}
		
		public Builder setBlockChainName(String blockChainName){
			buildFromBytes = false;
			this.blockChainName = blockChainName;
			blockChainNameSet = true;
			return this;
		}

		public Builder setPrevBlockHash(HashValue prevBlockHash) {
			buildFromBytes = false;
			this.prevBlockHash = prevBlockHash;
			prevBlockSet = true;
			return this;
		}

		public Builder setMerkleRoot(HashValue merkleRoot) {
			buildFromBytes = false;
			this.merkleRoot = merkleRoot;
			merkleRootSet = true;
			return this;
		}

		public Builder setBlockNumber(long blockNumber) {
			buildFromBytes = false;
			this.blockNumber = blockNumber;
			blockNumberSet = true;
			return this;
		}

		public Builder setTimestamp(long timestamp) {
			buildFromBytes = false;
			this.timestamp = timestamp;
			timestampSet = true;
			return this;
		}

		public Builder setDifficulty(BigDecimal difficulty) {
			buildFromBytes = false;
			this.difficulty = difficulty;
			difficultySet = true;
			return this;
		}

		public Builder setNonce(int nonce) {
			buildFromBytes = false;
			this.nonce = nonce;
			nonceSet = true;
			return this;
		}
		
		public Builder setMinerSignature(String minerSignature){
			buildFromBytes = false;
			this.minerSignature = minerSignature;
			if(this.minerSignature.length() > 50){
				this.minerSignature = this.minerSignature.substring(0, 50);
			}
			minerSignatureSet = true;
			return this;
		}

		public BlockHeader build() throws IncompleteBuilderSettingsException,
				IOException, ClassNotFoundException, IllegalBlockHeaderBytesRepresentation {
			if (buildFromBytes) {
				
				ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
				ObjectInputStream ois = new ObjectInputStream(bais);
				
				byte[] hash;
				
				hash = (byte[]) ois.readObject();
				this.hash = new DefaultHashValue(hash);
				this.headerVersion = (Integer) ois.readObject();
				this.blockChainName = (String) ois.readObject();
				hash = (byte[]) ois.readObject();
				this.prevBlockHash = new DefaultHashValue(hash);
				hash = (byte[]) ois.readObject();
				this.merkleRoot = new DefaultHashValue(hash);
				this.blockNumber = (Long) ois.readObject();
				this.timestamp = (Long) ois.readObject();
				this.difficulty = (BigDecimal) ois.readObject();
				this.nonce = (Integer) ois.readObject();
				this.minerSignature = (String) ois.readObject();
				
				ois.close();
				bais.close();
				
				DefaultBlockHeader header = new DefaultBlockHeader(this);
				return header;
			}

			boolean builderProperlySet = true;
			StringBuilder errorMsg = new StringBuilder(
					"Some fields are not initializated: ");
			if (!blockChainNameSet){
				builderProperlySet = false;
				errorMsg.append("(BlockChain Name) ");
			}
			if (!prevBlockSet) {
				builderProperlySet = false;
				errorMsg.append("(Previous DefaultBlock Hash) ");
			}
			if (!merkleRootSet) {
				builderProperlySet = false;
				errorMsg.append("(Merkle Root) ");
			}
			if (!blockNumberSet) {
				builderProperlySet = false;
				errorMsg.append("(DefaultBlock Number) ");
			}
			if (!timestampSet) {
				builderProperlySet = false;
				errorMsg.append("(Timestamp) ");
			}
			if (!difficultySet) {
				builderProperlySet = false;
				errorMsg.append("(Difficulty) ");
			}
			if (!nonceSet) {
				builderProperlySet = false;
				errorMsg.append("(Nonce) ");
			}
			
			if(!minerSignatureSet){
				builderProperlySet = false;
				errorMsg.append("(Miner Signature) ");
			}

			if (!builderProperlySet) {
				throw new IncompleteBuilderSettingsException(
						errorMsg.toString());
			}

			return new DefaultBlockHeader(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see raw.blockChain.interfaces.BlockHeader#getVersion()
	 */
	@Override
	public int getVersion() {
		return headerVersion;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.BlockHeader#getBlockChainName()
	 */
	@Override
	public String getBlockChainName() {
		return blockChainName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see raw.blockChain.interfaces.BlockHeader#getBlockNumber()
	 */
	@Override
	public long getBlockNumber() {
		return this.blockNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see raw.blockChain.interfaces.BlockHeader#hash()
	 */
	@Override
	public HashValue hash() {
		if (blockHash != null) {
			return blockHash;
		}
		Hasher hasher = new DefaultHasher();
		blockHash = hasher.hashBlockHeader(this);
		return blockHash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see raw.blockChain.interfaces.BlockHeader#previousBlock()
	 */
	@Override
	public HashValue previousBlock() {
		return this.prevBlockHash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see raw.blockChain.interfaces.BlockHeader#merkleRoot()
	 */
	@Override
	public HashValue merkleRoot() {
		return this.merkleRoot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see raw.blockChain.interfaces.BlockHeader#timestamp()
	 */
	@Override
	public long timestamp() {
		return this.timestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see raw.blockChain.interfaces.BlockHeader#currentDifficulty()
	 */
	@Override
	public BigDecimal currentDifficulty() {
		return this.difficulty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see raw.blockChain.interfaces.BlockHeader#nonce()
	 */
	@Override
	public int nonce() {
		return this.nonce;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.BlockHeader#getMinerSignature()
	 */
	@Override
	public String getMinerSignature() {
		return minerSignature;
	}

	@Override
	public byte[] getBytes() throws IOException {
//		ByteArrayDataOutput bado = ByteStreams.newDataOutput();
//
//		bado.write(hash().toByteArray());
//		bado.write(getHashableBytes());
//
//		return bado.toByteArray();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		
		oos.writeObject(hash().toByteArray());
		oos.writeObject(headerVersion);
		oos.writeObject(blockChainName);
		oos.writeObject(prevBlockHash.toByteArray());
		oos.writeObject(merkleRoot.toByteArray());
		oos.writeObject(blockNumber);
		oos.writeObject(timestamp);
		oos.writeObject(difficulty);
		oos.writeObject(nonce);
		oos.writeObject(minerSignature);
		
		byte[] retVal = baos.toByteArray();
		
		oos.close();
		baos.close();
		
		return retVal;
	}

	@Override
	public byte[] getHashableBytes() {
		ByteArrayDataOutput bado = ByteStreams.newDataOutput();

		bado.writeInt(headerVersion);
		bado.writeUTF(blockChainName);
		bado.write(prevBlockHash.toByteArray());
		bado.write(merkleRoot.toByteArray());
		bado.writeLong(blockNumber);
		bado.writeLong(timestamp);
		bado.writeChars(difficulty.toString());
		bado.writeInt(nonce);
		bado.writeChars(minerSignature);

		return bado.toByteArray();
	}

	@Override
	public boolean equals(Object obj) {
		if ((!(obj instanceof BlockHeader)) || obj == null ){
			return false;
		}
		BlockHeader compared = (BlockHeader) obj;
		return hash().equals(compared.hash());
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(headerVersion).
		append(blockChainName).
		append(hash()).
		append(prevBlockHash).
		append(merkleRoot).
		append(blockNumber).
		append(timestamp).
		append(difficulty).
		append(nonce).
		append(minerSignature);
		return builder.toHashCode();
	}

	@Override
	public String toString() {
		return "Block number #"+blockNumber+" / hash: "+hash().toHexString();
	}
	
}
