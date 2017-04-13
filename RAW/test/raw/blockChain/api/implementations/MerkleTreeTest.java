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

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import raw.blockChain.api.HashValue;
import raw.blockChain.api.Hasher;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultHasher;
import raw.blockChain.api.implementations.DefaultMerkleTree;
import raw.blockChain.transactionGenerators.RandomTransactionsGenerator;

public class MerkleTreeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConstructorTwoLeaves() throws IllegalArgumentException, UnknownHostException {
//		DhtHasher dhtHasher = new DefaultDhtHasher();
//		byte[] firstTrans = {'f','i','r','s','t'};
//		byte[] secondTrans = {'s','e','c','o','n','d'};
//		DhtID firstId = dhtHasher.hashBytes(firstTrans);
//		DhtID secondId = dhtHasher.hashBytes(secondTrans);
//		DhtAddress fistrAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.1"), 1024);
//		DhtAddress secondAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.2"), 1025);
//		
//		Transaction transaction1 = new DefaultTransaction(firstId, fistrAddress);
//		Transaction transaction2 = new DefaultTransaction(secondId, secondAddress);
//		
//		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
//		transactions.add(transaction1);
//		transactions.add(transaction2);
		
		RandomTransactionsGenerator rand = new RandomTransactionsGenerator();
		ArrayList<Transaction> transactions = rand.getTransactions(2);
		
		Hasher hasher = new DefaultHasher();
		
		HashValue hash1 = hasher.hashTransaction(transactions.get(0));
		HashValue hash2 = hasher.hashTransaction(transactions.get(1));
		
		HashValue expectedRoot = hasher.hashHashes(hash1, hash2);
		
		DefaultMerkleTree tree = new DefaultMerkleTree(transactions);
		
		assertEquals("Wrong Root hash", expectedRoot, tree.root());
	}
	
	@Test
	public void testConstructorFourLeaves() throws IllegalArgumentException, UnknownHostException {
//		DhtHasher dhtHasher = new DefaultDhtHasher();
//		byte[] firstTrans = {'f','i','r','s','t'};
//		byte[] secondTrans = {'s','e','c','o','n','d'};
//		byte[] thirdTrans = {'t','h','i','r','d'};
//		byte[] fourthTrans = {'f','o','u','r','t','h'};
//		DhtID firstId = dhtHasher.hashBytes(firstTrans);
//		DhtID secondId = dhtHasher.hashBytes(secondTrans);
//		DhtID thirdId = dhtHasher.hashBytes(thirdTrans);
//		DhtID fourthId = dhtHasher.hashBytes(fourthTrans);
//		DhtAddress fistrAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.1"), 1024);
//		DhtAddress secondAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.2"), 1025);
//		DhtAddress thirdAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.3"), 1026);
//		DhtAddress fourthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.4"), 1027);
//		
//		Transaction transaction1 = new DefaultTransaction(firstId, fistrAddress);
//		Transaction transaction2 = new DefaultTransaction(secondId, secondAddress);
//		Transaction transaction3 = new DefaultTransaction(thirdId, thirdAddress);
//		Transaction transaction4 = new DefaultTransaction(fourthId, fourthAddress);
//		
//		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
//		transactions.add(transaction1);
//		transactions.add(transaction2);
//		transactions.add(transaction3);
//		transactions.add(transaction4);
//		
		RandomTransactionsGenerator rand = new RandomTransactionsGenerator();
		ArrayList<Transaction> transactions = rand.getTransactions(4);
		
		Hasher hasher = new DefaultHasher();
		
		HashValue hash1 = hasher.hashTransaction(transactions.get(0));
		HashValue hash2 = hasher.hashTransaction(transactions.get(1));
		HashValue hash3 = hasher.hashTransaction(transactions.get(2));
		HashValue hash4 = hasher.hashTransaction(transactions.get(3));
		
		HashValue hash12 = hasher.hashHashes(hash1, hash2);
		HashValue hash34 = hasher.hashHashes(hash3, hash4);
		
		HashValue expectedRoot = hasher.hashHashes(hash12, hash34);
		
		DefaultMerkleTree tree = new DefaultMerkleTree(transactions);
		
		assertEquals("Wrong Root hash", expectedRoot, tree.root());
	}
	
	@Test
	public void testConstructorThreeLeaves() throws IllegalArgumentException, UnknownHostException {
//		DhtHasher dhtHasher = new DefaultDhtHasher();
//		byte[] firstTrans = {'f','i','r','s','t'};
//		byte[] secondTrans = {'s','e','c','o','n','d'};
//		byte[] thirdTrans = {'t','h','i','r','d'};
//
//		DhtID firstId = dhtHasher.hashBytes(firstTrans);
//		DhtID secondId = dhtHasher.hashBytes(secondTrans);
//		DhtID thirdId = dhtHasher.hashBytes(thirdTrans);
//
//		DhtAddress fistrAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.1"), 1024);
//		DhtAddress secondAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.2"), 1025);
//		DhtAddress thirdAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.3"), 1026);
//		
//		Transaction transaction1 = new DefaultTransaction(firstId, fistrAddress);
//		Transaction transaction2 = new DefaultTransaction(secondId, secondAddress);
//		Transaction transaction3 = new DefaultTransaction(thirdId, thirdAddress);
//		
//		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
//		transactions.add(transaction1);
//		transactions.add(transaction2);
//		transactions.add(transaction3);
		
		RandomTransactionsGenerator rand = new RandomTransactionsGenerator();
		ArrayList<Transaction> transactions = rand.getTransactions(3);
		
		Hasher hasher = new DefaultHasher();
		
		HashValue hash1 = hasher.hashTransaction(transactions.get(0));
		HashValue hash2 = hasher.hashTransaction(transactions.get(1));
		HashValue hash3 = hasher.hashTransaction(transactions.get(2));
		
		HashValue hash12 = hasher.hashHashes(hash1, hash2);
		HashValue hash33 = hasher.hashHashes(hash3, hash3);
		
		HashValue expectedRoot = hasher.hashHashes(hash12, hash33);
		
		DefaultMerkleTree tree = new DefaultMerkleTree(transactions);
		
		assertEquals("Wrong Root hash", expectedRoot, tree.root());
	}
	
	@Test
	public void testConstructorEightLeaves() throws IllegalArgumentException, UnknownHostException {
//		DhtHasher dhtHasher = new DefaultDhtHasher();
//		byte[] firstTrans = {'f','i','r','s','t'};
//		byte[] secondTrans = {'s','e','c','o','n','d'};
//		byte[] thirdTrans = {'t','h','i','r','d'};
//		byte[] fourthTrans = {'f','o','u','r','t','h'};
//		byte[] fifthTrans = {'f','i','f','t','h'};
//		byte[] sixthTrans = {'s','i','x','t','h'};
//		byte[] seventhTrans = {'s','e','v','e','n','t','h'};
//		byte[] eighthTrans = {'e','i','g','h','t','h'};
//		DhtID firstId = dhtHasher.hashBytes(firstTrans);
//		DhtID secondId = dhtHasher.hashBytes(secondTrans);
//		DhtID thirdId = dhtHasher.hashBytes(thirdTrans);
//		DhtID fourthId = dhtHasher.hashBytes(fourthTrans);
//		DhtID fifthId = dhtHasher.hashBytes(fifthTrans);
//		DhtID sixthId = dhtHasher.hashBytes(sixthTrans);
//		DhtID seventhId = dhtHasher.hashBytes(seventhTrans);
//		DhtID eightId = dhtHasher.hashBytes(eighthTrans);
//		DhtAddress fistrAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.1"), 1024);
//		DhtAddress secondAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.2"), 1025);
//		DhtAddress thirdAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.3"), 1026);
//		DhtAddress fourthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.4"), 1027);
//		DhtAddress fifthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.5"), 1028);
//		DhtAddress sixthAddress  = new DefaultDhtAddress(InetAddress.getByName("10.0.0.6"), 1029);
//		DhtAddress seventhAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.7"), 1030);
//		DhtAddress eighthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.8"), 1031);
//		
//		Transaction transaction1 = new DefaultTransaction(firstId, fistrAddress);
//		Transaction transaction2 = new DefaultTransaction(secondId, secondAddress);
//		Transaction transaction3 = new DefaultTransaction(thirdId, thirdAddress);
//		Transaction transaction4 = new DefaultTransaction(fourthId, fourthAddress);
//		Transaction transaction5 = new DefaultTransaction(fifthId, fifthAddress);
//		Transaction transaction6 = new DefaultTransaction(sixthId, sixthAddress);
//		Transaction transaction7 = new DefaultTransaction(seventhId, seventhAddress);
//		Transaction transaction8 = new DefaultTransaction(eightId, eighthAddress);
//		
//		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
//		transactions.add(transaction1);
//		transactions.add(transaction2);
//		transactions.add(transaction3);
//		transactions.add(transaction4);
//		transactions.add(transaction5);
//		transactions.add(transaction6);
//		transactions.add(transaction7);
//		transactions.add(transaction8);
		
		RandomTransactionsGenerator rand = new RandomTransactionsGenerator();
		ArrayList<Transaction> transactions = rand.getTransactions(8);
		
		Hasher hasher = new DefaultHasher();
		
		HashValue hash1 = hasher.hashTransaction(transactions.get(0));
		HashValue hash2 = hasher.hashTransaction(transactions.get(1));
		HashValue hash3 = hasher.hashTransaction(transactions.get(2));
		HashValue hash4 = hasher.hashTransaction(transactions.get(3));
		HashValue hash5 = hasher.hashTransaction(transactions.get(4));
		HashValue hash6 = hasher.hashTransaction(transactions.get(5));
		HashValue hash7 = hasher.hashTransaction(transactions.get(6));
		HashValue hash8 = hasher.hashTransaction(transactions.get(7));
		
		
		HashValue hash12 = hasher.hashHashes(hash1, hash2);
		HashValue hash34 = hasher.hashHashes(hash3, hash4);
		HashValue hash56 = hasher.hashHashes(hash5, hash6);
		HashValue hash78 = hasher.hashHashes(hash7, hash8);
		
		HashValue hash1234 = hasher.hashHashes(hash12, hash34);
		HashValue hash5678 = hasher.hashHashes(hash56, hash78);
		
		HashValue expectedRoot = hasher.hashHashes(hash1234, hash5678);
		
		DefaultMerkleTree tree = new DefaultMerkleTree(transactions);
		
		assertEquals("Wrong Root hash", expectedRoot, tree.root());
	}
	
	@Test
	public void testConstructorSevenLeaves() throws IllegalArgumentException, UnknownHostException {
//		DhtHasher dhtHasher = new DefaultDhtHasher();
//		byte[] firstTrans = {'f','i','r','s','t'};
//		byte[] secondTrans = {'s','e','c','o','n','d'};
//		byte[] thirdTrans = {'t','h','i','r','d'};
//		byte[] fourthTrans = {'f','o','u','r','t','h'};
//		byte[] fifthTrans = {'f','i','f','t','h'};
//		byte[] sixthTrans = {'s','i','x','t','h'};
//		byte[] seventhTrans = {'s','e','v','e','n','t','h'};
//		DhtID firstId = dhtHasher.hashBytes(firstTrans);
//		DhtID secondId = dhtHasher.hashBytes(secondTrans);
//		DhtID thirdId = dhtHasher.hashBytes(thirdTrans);
//		DhtID fourthId = dhtHasher.hashBytes(fourthTrans);
//		DhtID fifthId = dhtHasher.hashBytes(fifthTrans);
//		DhtID sixthId = dhtHasher.hashBytes(sixthTrans);
//		DhtID seventhId = dhtHasher.hashBytes(seventhTrans);
//		DhtAddress fistrAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.1"), 1024);
//		DhtAddress secondAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.2"), 1025);
//		DhtAddress thirdAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.3"), 1026);
//		DhtAddress fourthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.4"), 1027);
//		DhtAddress fifthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.5"), 1028);
//		DhtAddress sixthAddress  = new DefaultDhtAddress(InetAddress.getByName("10.0.0.6"), 1029);
//		DhtAddress seventhAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.7"), 1030);
//		
//		Transaction transaction1 = new DefaultTransaction(firstId, fistrAddress);
//		Transaction transaction2 = new DefaultTransaction(secondId, secondAddress);
//		Transaction transaction3 = new DefaultTransaction(thirdId, thirdAddress);
//		Transaction transaction4 = new DefaultTransaction(fourthId, fourthAddress);
//		Transaction transaction5 = new DefaultTransaction(fifthId, fifthAddress);
//		Transaction transaction6 = new DefaultTransaction(sixthId, sixthAddress);
//		Transaction transaction7 = new DefaultTransaction(seventhId, seventhAddress);
//		
//		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
//		transactions.add(transaction1);
//		transactions.add(transaction2);
//		transactions.add(transaction3);
//		transactions.add(transaction4);
//		transactions.add(transaction5);
//		transactions.add(transaction6);
//		transactions.add(transaction7);
		
		RandomTransactionsGenerator rand = new RandomTransactionsGenerator();
		ArrayList<Transaction> transactions = rand.getTransactions(7);
		
		Hasher hasher = new DefaultHasher();
		
		HashValue hash1 = hasher.hashTransaction(transactions.get(0));
		HashValue hash2 = hasher.hashTransaction(transactions.get(1));
		HashValue hash3 = hasher.hashTransaction(transactions.get(2));
		HashValue hash4 = hasher.hashTransaction(transactions.get(3));
		HashValue hash5 = hasher.hashTransaction(transactions.get(4));
		HashValue hash6 = hasher.hashTransaction(transactions.get(5));
		HashValue hash7 = hasher.hashTransaction(transactions.get(6));
		
		
		HashValue hash12 = hasher.hashHashes(hash1, hash2);
		HashValue hash34 = hasher.hashHashes(hash3, hash4);
		HashValue hash56 = hasher.hashHashes(hash5, hash6);
		HashValue hash77 = hasher.hashHashes(hash7, hash7);
		
		HashValue hash1234 = hasher.hashHashes(hash12, hash34);
		HashValue hash5677 = hasher.hashHashes(hash56, hash77);
		
		HashValue expectedRoot = hasher.hashHashes(hash1234, hash5677);
		
		DefaultMerkleTree tree = new DefaultMerkleTree(transactions);
		
		assertEquals("Wrong Root hash", expectedRoot, tree.root());
	}
	
	@Test
	public void testConstructorSixLeaves() throws IllegalArgumentException, UnknownHostException {
//		DhtHasher dhtHasher = new DefaultDhtHasher();
//		byte[] firstTrans = {'f','i','r','s','t'};
//		byte[] secondTrans = {'s','e','c','o','n','d'};
//		byte[] thirdTrans = {'t','h','i','r','d'};
//		byte[] fourthTrans = {'f','o','u','r','t','h'};
//		byte[] fifthTrans = {'f','i','f','t','h'};
//		byte[] sixthTrans = {'s','i','x','t','h'};
//		DhtID firstId = dhtHasher.hashBytes(firstTrans);
//		DhtID secondId = dhtHasher.hashBytes(secondTrans);
//		DhtID thirdId = dhtHasher.hashBytes(thirdTrans);
//		DhtID fourthId = dhtHasher.hashBytes(fourthTrans);
//		DhtID fifthId = dhtHasher.hashBytes(fifthTrans);
//		DhtID sixthId = dhtHasher.hashBytes(sixthTrans);
//		DhtAddress fistrAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.1"), 1024);
//		DhtAddress secondAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.2"), 1025);
//		DhtAddress thirdAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.3"), 1026);
//		DhtAddress fourthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.4"), 1027);
//		DhtAddress fifthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.5"), 1028);
//		DhtAddress sixthAddress  = new DefaultDhtAddress(InetAddress.getByName("10.0.0.6"), 1029);
//		
//		Transaction transaction1 = new DefaultTransaction(firstId, fistrAddress);
//		Transaction transaction2 = new DefaultTransaction(secondId, secondAddress);
//		Transaction transaction3 = new DefaultTransaction(thirdId, thirdAddress);
//		Transaction transaction4 = new DefaultTransaction(fourthId, fourthAddress);
//		Transaction transaction5 = new DefaultTransaction(fifthId, fifthAddress);
//		Transaction transaction6 = new DefaultTransaction(sixthId, sixthAddress);
//		
//		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
//		transactions.add(transaction1);
//		transactions.add(transaction2);
//		transactions.add(transaction3);
//		transactions.add(transaction4);
//		transactions.add(transaction5);
//		transactions.add(transaction6);
		
		RandomTransactionsGenerator rand = new RandomTransactionsGenerator();
		ArrayList<Transaction> transactions = rand.getTransactions(6);
		
		Hasher hasher = new DefaultHasher();
		
		HashValue hash1 = hasher.hashTransaction(transactions.get(0));
		HashValue hash2 = hasher.hashTransaction(transactions.get(1));
		HashValue hash3 = hasher.hashTransaction(transactions.get(2));
		HashValue hash4 = hasher.hashTransaction(transactions.get(3));
		HashValue hash5 = hasher.hashTransaction(transactions.get(4));
		HashValue hash6 = hasher.hashTransaction(transactions.get(5));
		
		HashValue hash12 = hasher.hashHashes(hash1, hash2);
		HashValue hash34 = hasher.hashHashes(hash3, hash4);
		HashValue hash56 = hasher.hashHashes(hash5, hash6);
		HashValue hash66 = hasher.hashHashes(hash6, hash6);
		
		HashValue hash1234 = hasher.hashHashes(hash12, hash34);
		HashValue hash5666 = hasher.hashHashes(hash56, hash66);
		
		HashValue expectedRoot = hasher.hashHashes(hash1234, hash5666);
		
		DefaultMerkleTree tree = new DefaultMerkleTree(transactions);
		
		assertEquals("Wrong Root hash", expectedRoot, tree.root());
	}
	
	@Test
	public void testConstructorFiveLeaves() throws IllegalArgumentException, UnknownHostException {
//		DhtHasher dhtHasher = new DefaultDhtHasher();
//		byte[] firstTrans = {'f','i','r','s','t'};
//		byte[] secondTrans = {'s','e','c','o','n','d'};
//		byte[] thirdTrans = {'t','h','i','r','d'};
//		byte[] fourthTrans = {'f','o','u','r','t','h'};
//		byte[] fifthTrans = {'f','i','f','t','h'};
//		DhtID firstId = dhtHasher.hashBytes(firstTrans);
//		DhtID secondId = dhtHasher.hashBytes(secondTrans);
//		DhtID thirdId = dhtHasher.hashBytes(thirdTrans);
//		DhtID fourthId = dhtHasher.hashBytes(fourthTrans);
//		DhtID fifthId = dhtHasher.hashBytes(fifthTrans);
//		DhtAddress fistrAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.1"), 1024);
//		DhtAddress secondAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.2"), 1025);
//		DhtAddress thirdAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.3"), 1026);
//		DhtAddress fourthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.4"), 1027);
//		DhtAddress fifthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.5"), 1028);
//		
//		Transaction transaction1 = new DefaultTransaction(firstId, fistrAddress);
//		Transaction transaction2 = new DefaultTransaction(secondId, secondAddress);
//		Transaction transaction3 = new DefaultTransaction(thirdId, thirdAddress);
//		Transaction transaction4 = new DefaultTransaction(fourthId, fourthAddress);
//		Transaction transaction5 = new DefaultTransaction(fifthId, fifthAddress);
//		
//		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
//		transactions.add(transaction1);
//		transactions.add(transaction2);
//		transactions.add(transaction3);
//		transactions.add(transaction4);
//		transactions.add(transaction5);
		
		RandomTransactionsGenerator rand = new RandomTransactionsGenerator();
		ArrayList<Transaction> transactions = rand.getTransactions(5);
		
		Hasher hasher = new DefaultHasher();
		
		HashValue hash1 = hasher.hashTransaction(transactions.get(0));
		HashValue hash2 = hasher.hashTransaction(transactions.get(1));
		HashValue hash3 = hasher.hashTransaction(transactions.get(2));
		HashValue hash4 = hasher.hashTransaction(transactions.get(3));
		HashValue hash5 = hasher.hashTransaction(transactions.get(4));
		
		HashValue hash12 = hasher.hashHashes(hash1, hash2);
		HashValue hash34 = hasher.hashHashes(hash3, hash4);
		HashValue hash55 = hasher.hashHashes(hash5, hash5);
		
		HashValue hash1234 = hasher.hashHashes(hash12, hash34);
		HashValue hash5555 = hasher.hashHashes(hash55, hash55);
		
		HashValue expectedRoot = hasher.hashHashes(hash1234, hash5555);
		
		DefaultMerkleTree tree = new DefaultMerkleTree(transactions);
		
		assertEquals("Wrong Root hash", expectedRoot, tree.root());
	}
	
	@Test
	public void testConstructorSixteenLeaves() throws IllegalArgumentException, UnknownHostException {
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
//		Transaction transaction1 = new DefaultTransaction(firstId, fistrAddress);
//		Transaction transaction2 = new DefaultTransaction(secondId, secondAddress);
//		Transaction transaction3 = new DefaultTransaction(thirdId, thirdAddress);
//		Transaction transaction4 = new DefaultTransaction(fourthId, fourthAddress);
//		Transaction transaction5 = new DefaultTransaction(fifthId, fifthAddress);
//		Transaction transaction6 = new DefaultTransaction(sixthId, sixthAddress);
//		Transaction transaction7 = new DefaultTransaction(seventhId, seventhAddress);
//		Transaction transaction8 = new DefaultTransaction(eightId, eighthAddress);
//		Transaction transaction9 = new DefaultTransaction(ninethId, ninethAddress );
//		Transaction transaction10 = new DefaultTransaction(tenthId, tenthAddress);
//		Transaction transaction11 = new DefaultTransaction(eleventhId, eleventhAddress);
//		Transaction transaction12 = new DefaultTransaction(twelvethId, twelvethAddress);
//		Transaction transaction13 = new DefaultTransaction(thirteenthId, thirteenthAddress);
//		Transaction transaction14 =  new DefaultTransaction(fourteenthId, fourteenthAddress);
//		Transaction transaction15 = new DefaultTransaction(fiftheenthId, fifteenthAddress);
//		Transaction transaction16 = new DefaultTransaction(sixteenthId, sixteenthAddress);
//		
//		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
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
		
		RandomTransactionsGenerator rand = new RandomTransactionsGenerator();
		ArrayList<Transaction> transactions = rand.getTransactions(16);
		
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
		
		HashValue hash12 = hasher.hashHashes(hash1, hash2);
		HashValue hash34 = hasher.hashHashes(hash3, hash4);
		HashValue hash56 = hasher.hashHashes(hash5, hash6);
		HashValue hash78 = hasher.hashHashes(hash7, hash8);
		HashValue hash9_10 = hasher.hashHashes(hash9, hash_10);
		HashValue hash11_12 = hasher.hashHashes(hash_11, hash_12);
		HashValue hash13_14 = hasher.hashHashes(hash_13, hash_14);
		HashValue hash15_16 = hasher.hashHashes(hash_15, hash_16);
		
		HashValue hash1234 = hasher.hashHashes(hash12, hash34);
		HashValue hash5678 = hasher.hashHashes(hash56, hash78);
		HashValue hash9_1011_12 = hasher.hashHashes(hash9_10, hash11_12);
		HashValue hash13_1415_16 = hasher.hashHashes(hash13_14, hash15_16);
		
		HashValue hash12345678 = hasher.hashHashes(hash1234, hash5678);
		HashValue hash9_1011_1213_1415_16 = hasher.hashHashes(hash9_1011_12, hash13_1415_16);
		
		HashValue expectedRoot = hasher.hashHashes(hash12345678, hash9_1011_1213_1415_16);
		
		DefaultMerkleTree tree = new DefaultMerkleTree(transactions);
		
		assertEquals("Wrong Root hash", expectedRoot, tree.root());
	}

}
