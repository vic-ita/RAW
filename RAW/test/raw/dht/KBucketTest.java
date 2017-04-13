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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import raw.blockChain.transactionGenerators.RandomNodesGenerator;
import raw.dht.implementations.DefaultKBucket;

public class KBucketTest {
	
	KBucket underTest;
	
	static ArrayList<DhtNodeExtended> nodes;
	
	static ArrayList<DhtNodeExtended> moreNodes;
	
	@BeforeClass
	public static void startUp() throws Exception{
		RandomNodesGenerator rand = new RandomNodesGenerator();
		
		nodes = rand.getNodes(20);
		
		moreNodes = rand.getNodes(20);
	}

	@Before
	public void setUp() throws Exception {
		underTest = new DefaultKBucket();
	}

	@After
	public void tearDown() throws Exception {
		underTest = null;
//		nodes = null;
//		moreNodes = null;
	}

	@Test
	public void testSizeZero() throws Exception {
		assertEquals("Bucket should be empty", 0, underTest.currentSize());
	}
	
	@Test
	public void testEmpty() throws Exception {
		assertTrue("Bucket should be empty", underTest.isEmpty());
	}
	
	@Test
	public void testInsertSingleNode() throws Exception {
		underTest.insertNode(nodes.get(0));
		
		assertEquals("Wrong size", 1, underTest.currentSize());
	}
	
	public void testInsertMultipleNodes() throws Exception{
		for(DhtNodeExtended node : nodes){
			underTest.insertNode(node);
		}
		
		assertEquals("Wrong size", nodes.size(), underTest.currentSize());
	}
	
	@Test
	public void testDeleteNode1() throws Exception {
		for(DhtNodeExtended node : nodes){
			underTest.insertNode(node);
		}
		
		assertTrue(underTest.deleteNode(nodes.get(0)));
		
		assertEquals("Wrong size", nodes.size()-1, underTest.currentSize());
	}
	
	@Test
	public void testDeleteNode2() throws Exception {
		for(DhtNodeExtended node : nodes){
			underTest.insertNode(node);
		}
		
		assertTrue(underTest.deleteNode(nodes.get(7)));
		
		assertEquals("Wrong size", nodes.size()-1, underTest.currentSize());
	}
	
	@Test
	public void testDeleteOnlyNode() throws Exception {
		underTest.insertNode(nodes.get(0));
		
		underTest.deleteNode(nodes.get(0));
		
		assertEquals("Wrong size", 0, underTest.currentSize());
		assertTrue("Bucket should be empty", underTest.isEmpty());
	}
	
	@Test
	public void testDeleteMissingNode() throws Exception {
		underTest.insertNode(nodes.get(1));
		underTest.insertNode(nodes.get(2));
		underTest.insertNode(nodes.get(3));
		
		assertFalse("Deleting missing node should return false", underTest.deleteNode(nodes.get(0)));
	}
	
	@Test
	public void testGetAll() throws Exception {
		for(DhtNodeExtended node : nodes){
			underTest.insertNode(node);
		}
		
		Collection<DhtNodeExtended> retrieved = underTest.getAllNodes();
		assertTrue("I should get the same colletion", retrieved.containsAll(nodes));
	}
	
	@Test
	public void testGetAllMultipleInsertions() throws Exception {
		for(DhtNodeExtended node : nodes){
			underTest.insertNode(node);
		}
		for(DhtNodeExtended node : nodes){
			underTest.insertNode(node);
		}
		
		Collection<DhtNodeExtended> retrieved = underTest.getAllNodes();
		assertTrue("I should get the same colletion", retrieved.containsAll(nodes));
	}
	
	@Test
	public void testCorrectUpdate1() throws Exception {
		for(DhtNodeExtended node : nodes){
			underTest.insertNode(node);
		}
		for(DhtNodeExtended node : nodes){
			underTest.insertNode(node);
		}
		for(int i=1; i<nodes.size(); i++){
			underTest.insertNode(nodes.get(i));
		}
		
		// now nodes.get(0) should be the least seen and should be excluded from the bucket
		
		underTest.insertNode(moreNodes.get(0));
		
		Collection<DhtNodeExtended> expected = new ArrayList<DhtNodeExtended>();
		for(int i=1; i<nodes.size(); i++){
			expected.add(nodes.get(i));
		}
		expected.add(moreNodes.get(0));
		
		Collection<DhtNodeExtended> retrieved = underTest.getAllNodes();
		assertTrue("I should get the same colletion", retrieved.containsAll(expected));
		
	}
	
	@Test
	public void testCorrectUpdate2() throws Exception {
		for(DhtNodeExtended node : nodes){
			underTest.insertNode(node);
		}
		for(DhtNodeExtended node : nodes){
			underTest.insertNode(node);
		}
		for(int i=1; i<nodes.size(); i++){
			underTest.insertNode(nodes.get(i));
		}
		for(int i=2; i<nodes.size(); i++){
			underTest.insertNode(nodes.get(i));
		}
		
		// now nodes.get(0) and nodes.get(1) should be the least seen nodes and should be excluded from the bucket
		
		underTest.insertNode(moreNodes.get(0));
		underTest.insertNode(moreNodes.get(1));
		
		Collection<DhtNodeExtended> expected = new ArrayList<DhtNodeExtended>();
		for(int i=2; i<nodes.size(); i++){
			expected.add(nodes.get(i));
		}
		expected.add(moreNodes.get(0));
		expected.add(moreNodes.get(1));
		
		Collection<DhtNodeExtended> retrieved = underTest.getAllNodes();
		assertTrue("I should get the same colletion", retrieved.containsAll(expected));
		
	}

}
