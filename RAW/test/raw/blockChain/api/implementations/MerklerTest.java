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
package raw.blockChain.api.implementations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import raw.blockChain.api.Block;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Hasher;
import raw.blockChain.api.Merkler;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.Merkler.IntermediateValues;
import raw.blockChain.api.implementations.DefaultBlock;
import raw.blockChain.api.implementations.DefaultHasher;
import raw.blockChain.api.implementations.DefaultMerkleTree;
import raw.blockChain.api.implementations.DefaultMerkler;
import raw.blockChain.api.implementations.DefaultBlockHeader.Builder;
import raw.blockChain.exceptions.TransactionNotPresentException;
import raw.blockChain.transactionGenerators.RandomTransactionsGenerator;

public class MerklerTest {
	
	Merkler merklerUnderTest;
	
	Transaction transaction1;
	Transaction transaction2;
	Transaction transaction3;
	Transaction transaction4;
	Transaction transaction5;
	Transaction transaction6;
	Transaction transaction7;
	Transaction transaction8;
	Transaction transaction9;
	Transaction transaction10;
	Transaction transaction11;
	Transaction transaction12;
	Transaction transaction13;
	Transaction transaction14;
	Transaction transaction15;
	Transaction transaction16;
	
	ArrayList<Transaction> transactions;

	HashValue hash12;
	HashValue hash34;
	HashValue hash56;
	HashValue hash78;
	HashValue hash9_10;
	HashValue hash11_12;
	HashValue hash13_14;
	HashValue hash15_16;
	
	HashValue hash1234;
	HashValue hash5678;
	HashValue hash9_1011_12;
	HashValue hash13_1415_16;
	
	HashValue hash12345678;
	HashValue hash9_1011_1213_1415_16;
	
	HashValue root;
	
	DefaultMerkleTree tree;
	
	Block block;
	
	String blockChainName = "RAW_STD_BLOCKCHAIN";
	
	String signature = "Test signature";

	@Before
	public void setUp() throws Exception {
//		DhtHasher dhtHasher = new DefaultDhtHasher();
//		byte[] firstTrans = {'f','i','r','s','t'};
//		byte[] secondTrans = {'s','e','c','o','n','d'};
//		byte[] thirdTrans = {'t','h','i','r','d'};
//		byte[] fourthTrans = {'f','o','u','r','t','h'};
//		byte[] fifthTrans = {'f','i','f','t','h'};
//		byte[] sixthTrans = {'s','i','x','t','h'};
//		byte[] seventhTrans = {'s','e','v','e','n','t','h'};
//		byte[] eighthTrans = {'e','i','g','h','t','h'};
//		byte[] ninethTrans = {'n','i','n','e','t','h'};
//		byte[] tenthTrans = {'t','e','n','t','h'};
//		byte[] eleventhTrans = {'e','l','e','v','e','n','t','h'};
//		byte[] twelvethTrans = {'t','w','e','l','v','e','t','h'};
//		byte[] thirteenthTrans = {'t','h','i','r','t','e','e','n','t','h'};
//		byte[] fourteenthTrans = {'f','o','u','r','t','e','e','n','t','h'};
//		byte[] fifteenthTrans = {'f','i','f','t','e','e','n','t','h'};
//		byte[] sixteenthTrans = {'s','i','x','t','e','e','n','t','h'};
//		DhtID firstId = dhtHasher.hashBytes(firstTrans);
//		DhtID secondId = dhtHasher.hashBytes(secondTrans);
//		DhtID thirdId = dhtHasher.hashBytes(thirdTrans);
//		DhtID fourthId = dhtHasher.hashBytes(fourthTrans);
//		DhtID fifthId = dhtHasher.hashBytes(fifthTrans);
//		DhtID sixthId = dhtHasher.hashBytes(sixthTrans);
//		DhtID seventhId = dhtHasher.hashBytes(seventhTrans);
//		DhtID eightId = dhtHasher.hashBytes(eighthTrans);
//		DhtID ninethId = dhtHasher.hashBytes(ninethTrans);
//		DhtID tenthId = dhtHasher.hashBytes(tenthTrans);
//		DhtID eleventhId = dhtHasher.hashBytes(eleventhTrans);
//		DhtID twelvethId = dhtHasher.hashBytes(twelvethTrans);
//		DhtID thirteenthId = dhtHasher.hashBytes(thirteenthTrans);
//		DhtID fourteenthId = dhtHasher.hashBytes(fourteenthTrans);
//		DhtID fiftheenthId = dhtHasher.hashBytes(fifteenthTrans);
//		DhtID sixteenthId = dhtHasher.hashBytes(sixteenthTrans);
//		DhtAddress fistrAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.1"), 1024);
//		DhtAddress secondAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.2"), 1025);
//		DhtAddress thirdAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.3"), 1026);
//		DhtAddress fourthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.4"), 1027);
//		DhtAddress fifthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.5"), 1028);
//		DhtAddress sixthAddress  = new DefaultDhtAddress(InetAddress.getByName("10.0.0.6"), 1029);
//		DhtAddress seventhAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.7"), 1030);
//		DhtAddress eighthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.8"), 1031);
//		DhtAddress ninethAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.9"), 1032);
//		DhtAddress tenthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.10"), 1033);
//		DhtAddress eleventhAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.11"), 1034);
//		DhtAddress twelvethAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.12"), 1035);
//		DhtAddress thirteenthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.13"), 1036);
//		DhtAddress fourteenthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.14"), 1037);
//		DhtAddress fifteenthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.15"), 1038);
//		DhtAddress sixteenthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.16"), 1039);
//		
//		 transaction1 = new DefaultTransaction(firstId, fistrAddress);
//		 transaction2 = new DefaultTransaction(secondId, secondAddress);
//		 transaction3 = new DefaultTransaction(thirdId, thirdAddress);
//		 transaction4 = new DefaultTransaction(fourthId, fourthAddress);
//		 transaction5 = new DefaultTransaction(fifthId, fifthAddress);
//		 transaction6 = new DefaultTransaction(sixthId, sixthAddress);
//		 transaction7 = new DefaultTransaction(seventhId, seventhAddress);
//		 transaction8 = new DefaultTransaction(eightId, eighthAddress);
//		 transaction9 = new DefaultTransaction(ninethId, ninethAddress );
//		 transaction10 = new DefaultTransaction(tenthId, tenthAddress);
//		 transaction11 = new DefaultTransaction(eleventhId, eleventhAddress);
//		 transaction12 = new DefaultTransaction(twelvethId, twelvethAddress);
//		 transaction13 = new DefaultTransaction(thirteenthId, thirteenthAddress);
//		 transaction14 =  new DefaultTransaction(fourteenthId, fourteenthAddress);
//		 transaction15 = new DefaultTransaction(fiftheenthId, fifteenthAddress);
//		 transaction16 = new DefaultTransaction(sixteenthId, sixteenthAddress);
//		
//		transactions = new ArrayList<Transaction>();
//		transactions.add(transaction1);
//		transactions.add(transaction2);
//		transactions.add(transaction3);
//		transactions.add(transaction4);
//		transactions.add(transaction5);
//		transactions.add(transaction6);
//		transactions.add(transaction7);
//		transactions.add(transaction8);
//		transactions.add(transaction9);
//		transactions.add(transaction10);
//		transactions.add(transaction11);
//		transactions.add(transaction12);
//		transactions.add(transaction13);
//		transactions.add(transaction14);
//		transactions.add(transaction15);
//		transactions.add(transaction16);
//		
//		Hasher hasher = new DefaultHasher();
//		
//		HashValue hash1 = hasher.hashTransaction(transaction1);
//		HashValue hash2 = hasher.hashTransaction(transaction2);
//		HashValue hash3 = hasher.hashTransaction(transaction3);
//		HashValue hash4 = hasher.hashTransaction(transaction4);
//		HashValue hash5 = hasher.hashTransaction(transaction5);
//		HashValue hash6 = hasher.hashTransaction(transaction6);
//		HashValue hash7 = hasher.hashTransaction(transaction7);
//		HashValue hash8 = hasher.hashTransaction(transaction8);
//		HashValue hash9 = hasher.hashTransaction(transaction9);
//		HashValue hash_10 = hasher.hashTransaction(transaction10);
//		HashValue hash_11 = hasher.hashTransaction(transaction11);
//		HashValue hash_12 = hasher.hashTransaction(transaction12);
//		HashValue hash_13 = hasher.hashTransaction(transaction13);
//		HashValue hash_14 = hasher.hashTransaction(transaction14);
//		HashValue hash_15 = hasher.hashTransaction(transaction15);
//		HashValue hash_16 = hasher.hashTransaction(transaction16);
		
		RandomTransactionsGenerator rand = new RandomTransactionsGenerator();
		transactions = rand.getTransactions(16);

		Hasher hasher = new DefaultHasher();

		HashValue hash1 = hasher.hashTransaction(transactions.get(0));
		HashValue hash2 = hasher.hashTransaction(transactions.get(1));
		HashValue hash3 = hasher.hashTransaction(transactions.get(2));
		HashValue hash4 = hasher.hashTransaction(transactions.get(3));
		HashValue hash5 = hasher.hashTransaction(transactions.get(4));
		HashValue hash6 = hasher.hashTransaction(transactions.get(5));
		HashValue hash7 = hasher.hashTransaction(transactions.get(6));
		HashValue hash8 = hasher.hashTransaction(transactions.get(7));
		HashValue hash9 = hasher.hashTransaction(transactions.get(8));
		HashValue hash_10 = hasher.hashTransaction(transactions.get(9));
		HashValue hash_11 = hasher.hashTransaction(transactions.get(10));
		HashValue hash_12 = hasher.hashTransaction(transactions.get(11));
		HashValue hash_13 = hasher.hashTransaction(transactions.get(12));
		HashValue hash_14 = hasher.hashTransaction(transactions.get(13));
		HashValue hash_15 = hasher.hashTransaction(transactions.get(14));
		HashValue hash_16 = hasher.hashTransaction(transactions.get(15));

		hash12 = hasher.hashHashes(hash1, hash2);
		hash34 = hasher.hashHashes(hash3, hash4);
		hash56 = hasher.hashHashes(hash5, hash6);
		hash78 = hasher.hashHashes(hash7, hash8);
		hash9_10 = hasher.hashHashes(hash9, hash_10);
		hash11_12 = hasher.hashHashes(hash_11, hash_12);
		hash13_14 = hasher.hashHashes(hash_13, hash_14);
		hash15_16 = hasher.hashHashes(hash_15, hash_16);

		hash1234 = hasher.hashHashes(hash12, hash34);
		hash5678 = hasher.hashHashes(hash56, hash78);
		hash9_1011_12 = hasher.hashHashes(hash9_10, hash11_12);
		hash13_1415_16 = hasher.hashHashes(hash13_14, hash15_16);

		hash12345678 = hasher.hashHashes(hash1234, hash5678);
		hash9_1011_1213_1415_16 = hasher.hashHashes(hash9_1011_12, hash13_1415_16);
		
		transaction1 = transactions.get(0);
		transaction2 = transactions.get(1);
		transaction3 = transactions.get(2);
		transaction4 = transactions.get(3);
		transaction5 = transactions.get(4);
		transaction6 = transactions.get(5);
		transaction7 = transactions.get(6);
		transaction8 = transactions.get(7);
		transaction9 = transactions.get(8);
		transaction10 = transactions.get(9);
		transaction11 = transactions.get(10);
		transaction12 = transactions.get(11);
		transaction13 = transactions.get(12);
		transaction14 =  transactions.get(13);
		transaction15 = transactions.get(14);
		transaction16 = transactions.get(15);

		root = hasher.hashHashes(hash12345678, hash9_1011_1213_1415_16);

		tree = new DefaultMerkleTree(transactions);
		
		merklerUnderTest = new DefaultMerkler(5);
		
		Builder blockHeaderBuilder = new Builder();
		blockHeaderBuilder.setBlockChainName(blockChainName)
		.setBlockNumber(42)
		.setDifficulty(new BigDecimal(42))
		.setNonce(42)
		.setPrevBlockHash(hash1)
		.setTimestamp(42L)
		.setMerkleRoot(tree.root()).
		setMinerSignature(signature);
		
		block = new DefaultBlock(blockHeaderBuilder.build(), transactions);
	}

	@After
	public void tearDown() throws Exception {
		transaction1 = null;
		transaction2 = null;
		transaction3 = null;
		transaction4 = null;
		transaction5 = null;
		transaction6 = null;
		transaction7 = null;
		transaction8 = null;
		transaction9 = null;
		transaction10 = null;
		transaction11 = null;
		transaction12 = null;
		transaction13 = null;
		transaction14 = null;
		transaction15 = null;
		transaction16 = null;
		
		transactions = null;

		hash12 = null;
		hash34 = null;
		hash56 = null;
		hash78 = null;
		hash9_10 = null;
		hash11_12 = null;
		hash13_14 = null;
		hash15_16 = null;
		
		hash1234 = null;
		hash5678 = null;
		hash9_1011_12 = null;
		hash13_1415_16 = null;
		
		hash12345678 = null;
		hash9_1011_1213_1415_16 = null;
		
		root = null;
		
		tree = null;
		
		block = null;
		
		merklerUnderTest = null;
	}

	@Test
	public void testGetRoot() {
		HashValue returnedRoot = merklerUnderTest.getMerkleRoot(transactions);
		
		assertEquals("Wrong root!", root, returnedRoot);
	}
	
	@Test
	public void testGetSingleNodeRoot() throws Exception {
		Hasher hasher = new DefaultHasher();
		HashValue hash1 = hasher.hashTransaction(transaction1);
		HashValue expecedRoot = hasher.hashHashes(hash1, hash1);
		
		ArrayList<Transaction> singleTransactionList = new ArrayList<Transaction>();
		singleTransactionList.add(transaction1);
		
		HashValue returnedRoot = merklerUnderTest.getMerkleRoot(singleTransactionList);
		
		assertEquals("Wrong root from sinle transaction list!!!", expecedRoot, returnedRoot);
	}
	
	@Test
	public void testgetIntermediateValues1() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction1, block);
		
		assertEquals("Wrong base transaction!", transaction1, intermediates.getBaseTransaction());
		
		Hasher hasher = new DefaultHasher();
		HashValue nextTransactionHashed = hasher.hashTransaction(transaction2);
		
		assertEquals("Wrong next hash (transaction)!", nextTransactionHashed, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 1)!", hash34, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 2)!", hash5678, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 3)!", hash9_1011_1213_1415_16, intermediates.popNextHash().getValue());
		
		assertNull("There should be no more values here", intermediates.popNextHash());
	}
	
	@Test
	public void testGetIntermediateValuesPairing1() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction1, block);
		
		assertEquals("Wrong base transaction!", transaction1, intermediates.getBaseTransaction());
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
	}
	
	@Test
	public void testgetIntermediateValues2() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction2, block);
		
		assertEquals("Wrong base transaction!", transaction2, intermediates.getBaseTransaction());
		
		Hasher hasher = new DefaultHasher();
		HashValue nextTransactionHashed = hasher.hashTransaction(transaction1);
		
		assertEquals("Wrong next hash (transaction)!", nextTransactionHashed, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 1)!", hash34, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 2)!", hash5678, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 3)!", hash9_1011_1213_1415_16, intermediates.popNextHash().getValue());
		
		assertNull("There should be no more values here", intermediates.popNextHash());
	}
	
	@Test
	public void testGetIntermediateValuesPairing2() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction2, block);
		
		assertEquals("Wrong base transaction!", transaction2, intermediates.getBaseTransaction());
		
		assertTrue(intermediates.popNextHash().leftPaired() == true);
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
	}
	
	@Test
	public void testgetIntermediateValues3() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction8, block);
		
		assertEquals("Wrong base transaction!", transaction8, intermediates.getBaseTransaction());
		
		Hasher hasher = new DefaultHasher();
		HashValue nextTransactionHashed = hasher.hashTransaction(transaction7);
		
		assertEquals("Wrong next hash (transaction)!", nextTransactionHashed, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 1)!", hash56, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 2)!", hash1234, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 3)!", hash9_1011_1213_1415_16, intermediates.popNextHash().getValue());
		
		assertNull("There should be no more values here", intermediates.popNextHash());
	}
	
	@Test
	public void testGetIntermediateValuesPairing3() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction8, block);
		
		assertEquals("Wrong base transaction!", transaction8, intermediates.getBaseTransaction());
		
		assertTrue(intermediates.popNextHash().leftPaired() == true);
		
		assertTrue(intermediates.popNextHash().leftPaired() == true);
		
		assertTrue(intermediates.popNextHash().leftPaired() == true);
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
	}

	@Test
	public void testgetIntermediateValues4() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction9, block);
		
		assertEquals("Wrong base transaction!", transaction9, intermediates.getBaseTransaction());
		
		Hasher hasher = new DefaultHasher();
		HashValue nextTransactionHashed = hasher.hashTransaction(transaction10);
		
		assertEquals("Wrong next hash (transaction)!", nextTransactionHashed, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 1)!", hash11_12, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 2)!", hash13_1415_16, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 3)!", hash12345678, intermediates.popNextHash().getValue());
		
		assertNull("There should be no more values here", intermediates.popNextHash());
	}
	
	@Test
	public void testGetIntermediateValuesPairing4() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction9, block);
		
		assertEquals("Wrong base transaction!", transaction9, intermediates.getBaseTransaction());
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
		
		assertTrue(intermediates.popNextHash().leftPaired() == true);
	}
	
	@Test
	public void testgetIntermediateValues5() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction13, block);
		
		assertEquals("Wrong base transaction!", transaction13, intermediates.getBaseTransaction());
		
		Hasher hasher = new DefaultHasher();
		HashValue nextTransactionHashed = hasher.hashTransaction(transaction14);
		
		assertEquals("Wrong next hash (transaction)!", nextTransactionHashed, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 1)!", hash15_16, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 2)!", hash9_1011_12, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 3)!", hash12345678, intermediates.popNextHash().getValue());
		
		assertNull("There should be no more values here", intermediates.popNextHash());
	}
	
	@Test
	public void testGetIntermediateValuesPairing5() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction13, block);
		
		assertEquals("Wrong base transaction!", transaction13, intermediates.getBaseTransaction());
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
		
		assertTrue(intermediates.popNextHash().leftPaired() == false);
		
		assertTrue(intermediates.popNextHash().leftPaired() == true);
		
		assertTrue(intermediates.popNextHash().leftPaired() == true);
	}
	
	@Test
	public void testgetIntermediateValues6() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction16, block);
		
		assertEquals("Wrong base transaction!", transaction16, intermediates.getBaseTransaction());
		
		Hasher hasher = new DefaultHasher();
		HashValue nextTransactionHashed = hasher.hashTransaction(transaction15);
		
		assertEquals("Wrong next hash (transaction)!", nextTransactionHashed, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 1)!", hash13_14, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 2)!", hash9_1011_12, intermediates.popNextHash().getValue());
		
		assertEquals("Wrong next hash (level 3)!", hash12345678, intermediates.popNextHash().getValue());
		
		assertNull("There should be no more values here", intermediates.popNextHash());
	}
	
	@Test
	public void testGetIntermediateValuesPairing6() throws Exception {
		IntermediateValues intermediates = merklerUnderTest.getIntermediateValues(transaction16, block);
		
		assertEquals("Wrong base transaction!", transaction16, intermediates.getBaseTransaction());
		
		assertTrue(intermediates.popNextHash().leftPaired() == true);
		
		assertTrue(intermediates.popNextHash().leftPaired() == true);
		
		assertTrue(intermediates.popNextHash().leftPaired() == true);
		
		assertTrue(intermediates.popNextHash().leftPaired() == true);
	}
	
	@Test
	public void testGetIntermediateValuesException(){
//		DhtHasher hasher = new DefaultDhtHasher();
//		DhtID dhtId = hasher.hashString("fake");
//		DhtAddress addr = null;
//		try {
//			addr = new DefaultDhtAddress(InetAddress.getByName("192.168.1.100"), 42);
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		}
		
		Transaction fakeTransaction = new RandomTransactionsGenerator().getTransactions(1).get(0);
		
		boolean exception = false;
		try {
			merklerUnderTest.getIntermediateValues(fakeTransaction, block);
		} catch (TransactionNotPresentException e) {
//			System.out.println(e);
			exception = true;
		}
		assertTrue("It should have had an exception....", exception);
	}
	
	@Test
	public void testGetMerkleRootByIntermediates1() throws Exception {
		IntermediateValues interm = merklerUnderTest.getIntermediateValues(transaction1, block);
		
		HashValue reconstructedRoot = merklerUnderTest.getMerkleRootByIntermediate(transaction1, interm);
		
		assertEquals("Wrong reconstruction!", root, reconstructedRoot);
	}
	
	@Test
	public void testGetMerkleRootByIntermediates2() throws Exception {
		IntermediateValues interm = merklerUnderTest.getIntermediateValues(transaction2, block);
		
		HashValue reconstructedRoot = merklerUnderTest.getMerkleRootByIntermediate(transaction2, interm);
		
		assertEquals("Wrong reconstruction!", root, reconstructedRoot);
	}
	
	@Test
	public void testGetMerkleRootByIntermediates3() throws Exception {
		IntermediateValues interm = merklerUnderTest.getIntermediateValues(transaction3, block);
		
		HashValue reconstructedRoot = merklerUnderTest.getMerkleRootByIntermediate(transaction3, interm);
		
		assertEquals("Wrong reconstruction!", root, reconstructedRoot);
	}
	
	@Test
	public void testGetMerkleRootByIntermediates4() throws Exception {
		IntermediateValues interm = merklerUnderTest.getIntermediateValues(transaction6, block);
		
		HashValue reconstructedRoot = merklerUnderTest.getMerkleRootByIntermediate(transaction6, interm);
		
		assertEquals("Wrong reconstruction!", root, reconstructedRoot);
	}
	
	@Test
	public void testGetMerkleRootByIntermediates5() throws Exception {
		IntermediateValues interm = merklerUnderTest.getIntermediateValues(transaction7, block);
		
		HashValue reconstructedRoot = merklerUnderTest.getMerkleRootByIntermediate(transaction7, interm);
		
		assertEquals("Wrong reconstruction!", root, reconstructedRoot);
	}
	
	@Test
	public void testGetMerkleRootByIntermediates6() throws Exception {
		IntermediateValues interm = merklerUnderTest.getIntermediateValues(transaction8, block);
		
		HashValue reconstructedRoot = merklerUnderTest.getMerkleRootByIntermediate(transaction8, interm);
		
		assertEquals("Wrong reconstruction!", root, reconstructedRoot);
	}
	
	@Test
	public void testGetMerkleRootByIntermediates7() throws Exception {
		IntermediateValues interm = merklerUnderTest.getIntermediateValues(transaction9, block);
		
		HashValue reconstructedRoot = merklerUnderTest.getMerkleRootByIntermediate(transaction9, interm);
		
		assertEquals("Wrong reconstruction!", root, reconstructedRoot);
	}
	
	@Test
	public void testGetMerkleRootByIntermediates8() throws Exception {
		IntermediateValues interm = merklerUnderTest.getIntermediateValues(transaction12, block);
		
		HashValue reconstructedRoot = merklerUnderTest.getMerkleRootByIntermediate(transaction12, interm);
		
		assertEquals("Wrong reconstruction!", root, reconstructedRoot);
	}
	
	@Test
	public void testGetMerkleRootByIntermediates9() throws Exception {
		IntermediateValues interm = merklerUnderTest.getIntermediateValues(transaction16, block);
		
		HashValue reconstructedRoot = merklerUnderTest.getMerkleRootByIntermediate(transaction16, interm);
		
		assertEquals("Wrong reconstruction!", root, reconstructedRoot);
	}
}
