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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Hasher;
import raw.blockChain.api.implementations.DefaultBlockHeader;
import raw.blockChain.api.implementations.DefaultHasher;
import raw.blockChain.exceptions.IllegalBlockHeaderBytesRepresentation;
import raw.blockChain.exceptions.IncompleteBuilderSettingsException;

public class BlockHeaderTest {
	
	BlockHeader headerUnderTest;
	
	private int headerVersion;
	private String blockChainName;
	
	private HashValue prevBlockHash;
	private HashValue merkleRoot;

	private long blockNumber;
	private long timestamp;

	private BigDecimal difficulty;
	private int nonce;
	
	private String signature;
	
	Hasher hasher;
	
	@Before
	public void setUp() throws Exception {
		hasher = new DefaultHasher();
		
		headerVersion = 1;
		
		blockChainName = "RAW_STD_BLOCKCHAIN";
		
		byte[] bytearr1 = {1, 2, 3, 4, 5, 'a', 'b', 'c', 'd', 'e'};
		prevBlockHash = hasher.hashBytes(bytearr1);
		
		byte[] bytearr2 = {'a', 'b', 'c', 'd', 'e', 1, 2, 3, 4, 5};
		merkleRoot = hasher.hashBytes(bytearr2);
		
		blockNumber = 42L;
		
		Date date = new Date();
		
		timestamp = date.getTime();
		
		difficulty = new BigDecimal(424242);
		
		nonce = 666;
		
		signature = "Signature for tests";
		
		DefaultBlockHeader.Builder builder = new DefaultBlockHeader.Builder();
		builder.setHeaderVersion(headerVersion).
		setBlockChainName(blockChainName).
		setPrevBlockHash(prevBlockHash).
		setMerkleRoot(merkleRoot).
		setBlockNumber(blockNumber).
		setTimestamp(timestamp).
		setDifficulty(difficulty).
		setNonce(nonce).
		setMinerSignature(signature);
		
		headerUnderTest = builder.build();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBuilder() throws ClassNotFoundException, IncompleteBuilderSettingsException, IOException, IllegalBlockHeaderBytesRepresentation {
		headerUnderTest = null;
		
		DefaultBlockHeader.Builder builder = new DefaultBlockHeader.Builder();
		builder.setHeaderVersion(headerVersion).
		setBlockChainName(blockChainName).
		setPrevBlockHash(prevBlockHash).
		setMerkleRoot(merkleRoot).
		setBlockNumber(blockNumber).
		setTimestamp(timestamp).
		setDifficulty(difficulty).
		setNonce(nonce).
		setMinerSignature(signature);
		
		headerUnderTest = builder.build();
			
		assertEquals("Wrong version", headerVersion, headerUnderTest.getVersion());
		assertEquals("Wrong previous block hash", prevBlockHash, headerUnderTest.previousBlock());
		assertEquals("Wrong merkle hash", merkleRoot, headerUnderTest.merkleRoot());
		assertEquals("Wrong block number", blockNumber, headerUnderTest.getBlockNumber());
		assertEquals("Wrong timestamp", timestamp, headerUnderTest.timestamp());
		assertEquals("Wrong difficulty", difficulty, headerUnderTest.currentDifficulty());
		assertEquals("Wrong nonce", nonce, headerUnderTest.nonce());
	}

	@Test
	public void testHash(){
		byte[] bytes = headerUnderTest.getHashableBytes();
		
		HashValue expected = hasher.hashBytes(bytes);
		
		assertEquals("Wrong hash", expected, headerUnderTest.hash());
	}
	
	@Test
	public void testSerializableness() throws IOException, ClassNotFoundException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		
		oos.writeObject(headerUnderTest);
		oos.close();
		
		byte[] serialization = baos.toByteArray();
		
		baos.close();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(serialization);
		ObjectInputStream ois = new ObjectInputStream(bais);
		
		BlockHeader deserialized = (BlockHeader) ois.readObject();
		
		ois.close();
		bais.close();
		
		assertEquals("Deserialized object do not equals to itself!", headerUnderTest, deserialized);
	}
	
	@Test
	public void testEquals() throws ClassNotFoundException, IncompleteBuilderSettingsException, IOException, IllegalBlockHeaderBytesRepresentation{
		DefaultBlockHeader.Builder builder = new DefaultBlockHeader.Builder();
		builder.setHeaderVersion(headerVersion).
		setBlockChainName(blockChainName).
		setPrevBlockHash(prevBlockHash).
		setMerkleRoot(merkleRoot).
		setBlockNumber(blockNumber).
		setTimestamp(timestamp).
		setDifficulty(difficulty).
		setNonce(nonce).
		setMinerSignature(signature);
		
		BlockHeader newHeader = builder.build();
		
		assertEquals("This two objects should be equal...", headerUnderTest, newHeader);
	}
	
	@Test
	public void testBuildFromBytes() throws IOException, ClassNotFoundException, IncompleteBuilderSettingsException, IllegalBlockHeaderBytesRepresentation{
		byte[] gotBytes = headerUnderTest.getBytes();
		
		DefaultBlockHeader.Builder builder = new DefaultBlockHeader.Builder(gotBytes);
		
		BlockHeader built = builder.build();
		
		assertEquals("wrong block number", headerUnderTest.getBlockNumber(), built.getBlockNumber());
		assertEquals("wrong difficulty", headerUnderTest.currentDifficulty(), built.currentDifficulty());
		assertEquals("wrong timestamp", headerUnderTest.timestamp(), built.timestamp());
		assertEquals("wrong nonce", headerUnderTest.nonce(), built.nonce());
		
		assertEquals("wrong previous block", headerUnderTest.previousBlock(), built.previousBlock());
		
		assertEquals("wrong merkle", headerUnderTest.merkleRoot(), built.merkleRoot());
		
		assertEquals("This two objects should be equal...", headerUnderTest, built);
	}
	
	@Test
	public void testHashCode() throws Exception {
		BlockHeader anotherHeader;
		DefaultBlockHeader.Builder builder = new DefaultBlockHeader.Builder();
		builder.setHeaderVersion(headerVersion).
		setBlockChainName("A new name!"). //changed here!
		setPrevBlockHash(prevBlockHash).
		setMerkleRoot(merkleRoot).
		setBlockNumber(blockNumber).
		setTimestamp(timestamp).
		setDifficulty(difficulty).
		setNonce(nonce).
		setMinerSignature(signature);
		
		anotherHeader = builder.build();
		
		assertNotEquals("The hash codes should be different!!!", headerUnderTest.hashCode(), anotherHeader.hashCode());
		
		builder = new DefaultBlockHeader.Builder();
		builder.setHeaderVersion(headerVersion).
		setBlockChainName(blockChainName).
		setPrevBlockHash(prevBlockHash).
		setMerkleRoot(merkleRoot).
		setBlockNumber(101). //changed here!
		setTimestamp(timestamp).
		setDifficulty(difficulty).
		setNonce(nonce).
		setMinerSignature(signature);
		
		anotherHeader = builder.build();
		
		assertNotEquals("The hash codes should be different!!!", headerUnderTest.hashCode(), anotherHeader.hashCode());
		
		builder = new DefaultBlockHeader.Builder();
		builder.setHeaderVersion(headerVersion).
		setBlockChainName(blockChainName).
		setPrevBlockHash(prevBlockHash).
		setMerkleRoot(merkleRoot).
		setBlockNumber(blockNumber). 
		setTimestamp(timestamp+1).//changed here!
		setDifficulty(difficulty).
		setNonce(nonce).
		setMinerSignature(signature);
		
		anotherHeader = builder.build();
		
		assertNotEquals("The hash codes should be different!!!", headerUnderTest.hashCode(), anotherHeader.hashCode());
		
		builder = new DefaultBlockHeader.Builder();
		builder.setHeaderVersion(headerVersion).
		setBlockChainName(blockChainName).
		setPrevBlockHash(prevBlockHash).
		setMerkleRoot(merkleRoot).
		setBlockNumber(blockNumber). 
		setTimestamp(timestamp).
		setDifficulty(difficulty).
		setNonce(nonce).
		setMinerSignature(signature);
		
		anotherHeader = builder.build(); //this should be identical.
		
		assertEquals("The hash codes should be different!!!", headerUnderTest.hashCode(), anotherHeader.hashCode());
	}
}
