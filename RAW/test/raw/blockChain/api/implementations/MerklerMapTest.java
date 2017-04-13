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
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultMerkleTree;
import raw.blockChain.api.implementations.DefaultMerklerMap;
import raw.blockChain.transactionGenerators.RandomTransactionsGenerator;

public class MerklerMapTest {
	int size;
	DefaultMerklerMap mapUnderTest;

	@Before
	public void setUp() throws Exception {
		size = 5;
		mapUnderTest = new DefaultMerklerMap(size);
	}

	@After
	public void tearDown() throws Exception {
		mapUnderTest = null;
	}
	
	private ArrayList<ArrayList<Transaction>> twoTransactionsLists() throws IllegalArgumentException, UnknownHostException{
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
		
		RandomTransactionsGenerator rand = new RandomTransactionsGenerator();
		ArrayList<Transaction> transactions = rand.getTransactions(16);
		
		Transaction transaction1 = transactions.get(0);
		Transaction transaction2 = transactions.get(1);
		Transaction transaction3 = transactions.get(2);
		Transaction transaction4 = transactions.get(3);
		Transaction transaction5 = transactions.get(4);
		Transaction transaction6 = transactions.get(5);
		Transaction transaction7 = transactions.get(6);
		Transaction transaction8 = transactions.get(7);
		Transaction transaction9 = transactions.get(8);
		Transaction transaction10 = transactions.get(9);
		Transaction transaction11 = transactions.get(10);
		Transaction transaction12 = transactions.get(11);
		Transaction transaction13 = transactions.get(12);
		Transaction transaction14 =  transactions.get(13);
		Transaction transaction15 = transactions.get(14);
		Transaction transaction16 = transactions.get(15);
		
		ArrayList<ArrayList<Transaction>> transactionsLists = new ArrayList<ArrayList<Transaction>>();
		
		ArrayList<Transaction> list1 = new ArrayList<Transaction>();
		list1.add(transaction1);
		list1.add(transaction2);
		
		ArrayList<Transaction> list2 = new ArrayList<Transaction>();
		list2.add(transaction3);
		list2.add(transaction4);
		
		ArrayList<Transaction> list3 = new ArrayList<Transaction>();
		list3.add(transaction5);
		list3.add(transaction6);
		
		ArrayList<Transaction> list4 = new ArrayList<Transaction>();
		list4.add(transaction7);
		list4.add(transaction8);
		
		ArrayList<Transaction> list5 = new ArrayList<Transaction>();
		list5.add(transaction9);
		list5.add(transaction10);
		
		ArrayList<Transaction> list6 = new ArrayList<Transaction>();
		list6.add(transaction11);
		list6.add(transaction12);
		
		ArrayList<Transaction> list7 = new ArrayList<Transaction>();
		list7.add(transaction13);
		list7.add(transaction14);
		
		ArrayList<Transaction> list8 = new ArrayList<Transaction>();
		list8.add(transaction15);
		list8.add(transaction16);
		
		transactionsLists.add(list1);
		transactionsLists.add(list2);
		transactionsLists.add(list3);
		transactionsLists.add(list4);
		transactionsLists.add(list5);
		transactionsLists.add(list6);
		transactionsLists.add(list7);
		transactionsLists.add(list8);
		
		return transactionsLists;		
	}

	@Test
	public void testPutTwoTrees() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(1);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		assertEquals("Size wrong", 2, mapUnderTest.size());
	}
	
	@Test
	public void testPutTwoTreesDoublePut1() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree0;
		DefaultMerkleTree tree1;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree0 = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree0);
		
		transactions = lists.get(1);
		tree1 = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree1);
		
		mapUnderTest.put(transactions, tree0);
		
		assertEquals("Size wrong", 2, mapUnderTest.size());
		assertTrue(mapUnderTest.search(lists.get(0)).equals(tree0));
		assertTrue(mapUnderTest.search(lists.get(1)).equals(tree1));
	}
	
	@Test
	public void testPutTwoTreesDoublePut2() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree0;
		DefaultMerkleTree tree1;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree0 = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree0);
		
		transactions = lists.get(1);
		tree1 = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree1);
		
		mapUnderTest.put(transactions, tree1);
		
		assertEquals("Size wrong", 2, mapUnderTest.size());
		assertTrue(mapUnderTest.search(lists.get(0)).equals(tree0));
		assertTrue(mapUnderTest.search(lists.get(1)).equals(tree1));
	}
	
	@Test
	public void testPutTwoTreesDoublePut3() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree0;
		DefaultMerkleTree tree1;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree0 = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree0);
		
		transactions = lists.get(1);
		tree1 = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree1);
		
		mapUnderTest.put(transactions, tree0);
		mapUnderTest.put(transactions, tree1);
		
		assertEquals("Size wrong", 2, mapUnderTest.size());
		assertTrue(mapUnderTest.search(lists.get(0)).equals(tree0));
		assertTrue(mapUnderTest.search(lists.get(1)).equals(tree1));
	}
	
	@Test
	public void testPutTwoTreesDoublePut4() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree0;
		DefaultMerkleTree tree1;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree0 = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree0);
		
		transactions = lists.get(1);
		tree1 = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree1);
		
		mapUnderTest.put(transactions, tree0);
		mapUnderTest.put(transactions, tree1);
		mapUnderTest.put(transactions, tree1);
		mapUnderTest.put(transactions, tree0);
		mapUnderTest.put(transactions, tree0);
		mapUnderTest.put(transactions, tree0);
		mapUnderTest.put(transactions, tree1);
		mapUnderTest.put(transactions, tree1);
		mapUnderTest.put(transactions, tree1);
		mapUnderTest.put(transactions, tree0);
		mapUnderTest.put(transactions, tree0);
		mapUnderTest.put(transactions, tree1);
		
		assertEquals("Size wrong", 2, mapUnderTest.size());
		assertTrue(mapUnderTest.search(lists.get(0)).equals(tree0));
		assertTrue(mapUnderTest.search(lists.get(1)).equals(tree1));
	}
	
	@Test
	public void testPutThreeTrees() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(1);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(2);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		assertEquals("Size wrong", 3, mapUnderTest.size());
	}
	
	@Test
	public void testPutFourTrees() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(1);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(2);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(3);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		assertEquals("Size wrong", 4, mapUnderTest.size());
	}
	
	@Test
	public void testPutFiveTrees() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(1);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(2);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(3);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(4);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);

		assertEquals("Size wrong", 5, mapUnderTest.size());
	}
	
	@Test
	public void testPutEightTrees() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(1);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(2);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(3);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(4);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(5);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(6);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(7);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);

		assertEquals("Size wrong", 5, mapUnderTest.size());
	}
	
	@Test
	public void testPutSixTreesAndChekPrunedOne1() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(1);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(2);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(3);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(4);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(3));
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		transactions = lists.get(5);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);

		assertEquals("Size wrong", 5, mapUnderTest.size());
		
		assertTrue("Tist transaction should NOT be here!", mapUnderTest.search(lists.get(4))==null);
		
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(0))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(1))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(2))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(3))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(5))!=null);
	}
	
	@Test
	public void testPutSixTreesAndChekPrunedOne2() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(1);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(2);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(3);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(4);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(3));
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		transactions = lists.get(5);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(3));
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		transactions = lists.get(6);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);

		assertEquals("Size wrong", 5, mapUnderTest.size());
		
		assertTrue("Tist transaction should NOT be here!", mapUnderTest.search(lists.get(4))==null);
		assertTrue("Tist transaction should NOT be here!", mapUnderTest.search(lists.get(5))==null);
		
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(0))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(1))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(2))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(3))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(6))!=null);
	}
	
	@Test
	public void testPutSixTreesAndChekContained() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = twoTransactionsLists();
		DefaultMerkleTree tree;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(1);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(2);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(3);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(4);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(3));
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		transactions = lists.get(5);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(3));
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		transactions = lists.get(6);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);

		assertEquals("Size wrong", 5, mapUnderTest.size());
		
		assertTrue("Tist transaction should NOT be here!", mapUnderTest.search(lists.get(4))==null);
		assertTrue("Tist transaction should NOT be here!", mapUnderTest.search(lists.get(5))==null);
		
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(0))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(1))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(2))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(3))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(6))!=null);
		
		transactions = lists.get(0);
		tree = new DefaultMerkleTree(transactions);
		assertEquals("Retrieved wrong tree!", tree, mapUnderTest.search(transactions));
		
		transactions = lists.get(1);
		tree = new DefaultMerkleTree(transactions);
		assertEquals("Retrieved wrong tree!", tree, mapUnderTest.search(transactions));
		
		transactions = lists.get(2);
		tree = new DefaultMerkleTree(transactions);
		assertEquals("Retrieved wrong tree!", tree, mapUnderTest.search(transactions));
		
		transactions = lists.get(3);
		tree = new DefaultMerkleTree(transactions);
		assertEquals("Retrieved wrong tree!", tree, mapUnderTest.search(transactions));
		
		transactions = lists.get(6);
		tree = new DefaultMerkleTree(transactions);
		assertEquals("Retrieved wrong tree!", tree, mapUnderTest.search(transactions));
	}
	
	private ArrayList<ArrayList<Transaction>> fiveTransactionsLists() throws IllegalArgumentException, UnknownHostException{
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
		
		RandomTransactionsGenerator rand = new RandomTransactionsGenerator();
		ArrayList<Transaction> transactions = rand.getTransactions(16);
		
		Transaction transaction1 = transactions.get(0);
		Transaction transaction2 = transactions.get(1);
		Transaction transaction3 = transactions.get(2);
		Transaction transaction4 = transactions.get(3);
		Transaction transaction5 = transactions.get(4);
		Transaction transaction6 = transactions.get(5);
		Transaction transaction7 = transactions.get(6);
		Transaction transaction8 = transactions.get(7);
		Transaction transaction9 = transactions.get(8);
		Transaction transaction10 = transactions.get(9);
		Transaction transaction11 = transactions.get(10);
		Transaction transaction12 = transactions.get(11);
		Transaction transaction13 = transactions.get(12);
		Transaction transaction14 =  transactions.get(13);
		Transaction transaction15 = transactions.get(14);
		Transaction transaction16 = transactions.get(15);
		
		ArrayList<ArrayList<Transaction>> transactionsLists = new ArrayList<ArrayList<Transaction>>();
		
		ArrayList<Transaction> list1 = new ArrayList<Transaction>();
		list1.add(transaction1);
		list1.add(transaction2);
		list1.add(transaction3);
		list1.add(transaction4);
		list1.add(transaction5);
		
		ArrayList<Transaction> list2 = new ArrayList<Transaction>();
		list2.add(transaction3);
		list2.add(transaction4);
		list2.add(transaction1);
		list2.add(transaction2);
		list2.add(transaction3);
		
		ArrayList<Transaction> list3 = new ArrayList<Transaction>();
		list3.add(transaction5);
		list3.add(transaction6);
		list3.add(transaction1);
		list3.add(transaction2);
		list3.add(transaction7);
		
		ArrayList<Transaction> list4 = new ArrayList<Transaction>();
		list4.add(transaction7);
		list4.add(transaction8);
		list4.add(transaction9);
		list4.add(transaction10);
		list4.add(transaction11);
		
		ArrayList<Transaction> list5 = new ArrayList<Transaction>();
		list5.add(transaction9);
		list5.add(transaction10);
		list5.add(transaction8);
		list5.add(transaction11);
		list5.add(transaction7);
		
		ArrayList<Transaction> list6 = new ArrayList<Transaction>();
		list6.add(transaction11);
		list6.add(transaction12);
		list6.add(transaction13);
		list6.add(transaction14);
		list6.add(transaction10);
		
		ArrayList<Transaction> list7 = new ArrayList<Transaction>();
		list7.add(transaction14);
		list7.add(transaction13);
		list7.add(transaction12);
		list7.add(transaction11);
		list7.add(transaction10);
		
		ArrayList<Transaction> list8 = new ArrayList<Transaction>();
		list8.add(transaction15);
		list8.add(transaction16);
		list8.add(transaction10);
		list8.add(transaction11);
		list8.add(transaction12);
		
		transactionsLists.add(list1);
		transactionsLists.add(list2);
		transactionsLists.add(list3);
		transactionsLists.add(list4);
		transactionsLists.add(list5);
		transactionsLists.add(list6);
		transactionsLists.add(list7);
		transactionsLists.add(list8);
		
		return transactionsLists;		
	}
	
	@Test
	public void testPutSixBIGTreesAndChekContained() throws IllegalArgumentException, UnknownHostException {
		ArrayList<ArrayList<Transaction>> lists = fiveTransactionsLists();
		DefaultMerkleTree tree;
		ArrayList<Transaction> transactions;
		
		transactions = lists.get(0);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(1);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(2);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(3);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		transactions = lists.get(4);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(3));
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		transactions = lists.get(5);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);
		
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		mapUnderTest.search(lists.get(3));
		mapUnderTest.search(lists.get(2));
		mapUnderTest.search(lists.get(1));
		mapUnderTest.search(lists.get(0));
		
		transactions = lists.get(6);
		tree = new DefaultMerkleTree(transactions);
		mapUnderTest.put(transactions, tree);

		assertEquals("Size wrong", 5, mapUnderTest.size());
		
		assertTrue("Tist transaction should NOT be here!", mapUnderTest.search(lists.get(4))==null);
		assertTrue("Tist transaction should NOT be here!", mapUnderTest.search(lists.get(5))==null);
		
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(0))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(1))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(2))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(3))!=null);
		assertTrue("Tist transaction should BE here!", mapUnderTest.search(lists.get(6))!=null);
		
		transactions = lists.get(0);
		tree = new DefaultMerkleTree(transactions);
		assertEquals("Retrieved wrong tree!", tree, mapUnderTest.search(transactions));
		
		transactions = lists.get(1);
		tree = new DefaultMerkleTree(transactions);
		assertEquals("Retrieved wrong tree!", tree, mapUnderTest.search(transactions));
		
		transactions = lists.get(2);
		tree = new DefaultMerkleTree(transactions);
		assertEquals("Retrieved wrong tree!", tree, mapUnderTest.search(transactions));
		
		transactions = lists.get(3);
		tree = new DefaultMerkleTree(transactions);
		assertEquals("Retrieved wrong tree!", tree, mapUnderTest.search(transactions));
		
		transactions = lists.get(6);
		tree = new DefaultMerkleTree(transactions);
		assertEquals("Retrieved wrong tree!", tree, mapUnderTest.search(transactions));
	}

}
