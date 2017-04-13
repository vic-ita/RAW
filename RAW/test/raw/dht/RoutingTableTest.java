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
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import raw.blockChain.transactionGenerators.RandomNodesGenerator;
import raw.dht.implementations.DefaultDhtCore;
import raw.dht.implementations.DefaultRoutingTable;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.exceptions.IncompleteNodeExtendedException;
import raw.dht.implementations.utils.DhtUtils;

public class RoutingTableTest {
	
	static ArrayList<DhtNodeExtended> nodes;
	static ArrayList<DhtNodeExtended> moreNodes;
	
	static DhtNodeExtended baseNode;
	
	DefaultDhtCore coreMock;
	
	RoutingTable underTest;
	
	@BeforeClass
	public static void startUp() throws Exception{
		RandomNodesGenerator rand = new RandomNodesGenerator();
		nodes = rand.getNodes(20);
		
		moreNodes = rand.getNodes(20);
		
		baseNode = rand.getNodes(1).get(0);
	}

	@Before
	public void setUp() throws Exception {
//		nodes = new ArrayList<DhtNode>();
		
//		coreMock = EasyMock.createMock(DefaultDhtCore.class);
//		
//		EasyMock.expect(coreMock.areCorrectlyOld(EasyMock.anyObject())).andAnswer(new IAnswer<HashMap<DhtNodeExtended,Boolean>>() {
//			@Override
//			public HashMap<DhtNodeExtended, Boolean> answer() throws Throwable {
//				@SuppressWarnings("unchecked")
//				Collection<DhtNodeExtended> params = (Collection<DhtNodeExtended>) EasyMock.getCurrentArguments()[0];
//				HashMap<DhtNodeExtended, Boolean> reply = new HashMap<>();
//				for(DhtNodeExtended node : params){
//					reply.put(node, true);
//				}
//				return reply;
//			}
//		}).anyTimes();//andReturn(true).anyTimes();
//		EasyMock.expect(coreMock.getNodeExtended()).andReturn(baseNode).anyTimes();
//		
//		EasyMock.replay(coreMock);
		
		coreMock = mockCore(baseNode);

		underTest = new DefaultRoutingTable(baseNode, coreMock);
	}
	
	private DefaultDhtCore mockCore(DhtNodeExtended baseNode) throws IncoherentTransactionException, IncompleteNodeExtendedException{
		DefaultDhtCore mocked = EasyMock.createMock(DefaultDhtCore.class);
		
		EasyMock.expect(mocked.areCorrectlyOld(EasyMock.anyObject())).andAnswer(new IAnswer<HashMap<DhtNodeExtended,Boolean>>() {
			@Override
			public HashMap<DhtNodeExtended, Boolean> answer() throws Throwable {
				@SuppressWarnings("unchecked")
				Collection<DhtNodeExtended> params = (Collection<DhtNodeExtended>) EasyMock.getCurrentArguments()[0];
				HashMap<DhtNodeExtended, Boolean> reply = new HashMap<>();
				for(DhtNodeExtended node : params){
					reply.put(node, true);
				}
				return reply;
			}
		}).anyTimes();//andReturn(true).anyTimes();
		EasyMock.expect(mocked.getNodeExtended()).andReturn(baseNode).anyTimes();
		
		EasyMock.replay(mocked);
		
		return mocked;
	}

	@After
	public void tearDown() throws Exception {
//		nodes = null;
//		moreNodes = null;
		underTest = null;
	}

	@Test
	public void testConstructor() throws IncoherentTransactionException, IncompleteNodeExtendedException {
		DhtNodeExtended myNode = nodes.get(0);
		@SuppressWarnings("unused")
		RoutingTable table = new DefaultRoutingTable(myNode, mockCore(myNode));
		assertTrue("Something went wrong", true);
	}
	
//	@Test
//	public void testConstructorRealScenario() throws Exception {
//		DhtAddress addr = new DefaultDhtAddress("147.162.96.137", 5010, 5010);
//		DhtID id = new DefaultDhtID("49995c3ba6300ad58800c1be4fc48d5ae0e28d2cbfe4c3902280af7ad66e2fe2486d3d2c7fca5857b7e677f4c35403e40e089a876b6dd65265eaa9a80f818a02");
//		PublicKey pubKey = DhtSigningUtils.getSignKeyPair().getPublic();
//		DhtNode node = new DefaultDhtNode(id, pubKey, addr);
//		RoutingTable table = new DefaultRoutingTable(node);
//		table.insertNode(nodes.get(0)) ;
//		assertEquals("Wrong nodes size!", 1, table.getFullSetOfNodes(false).size());
//	}
//	
//	@Test
//	public void testConstructorRealScenario2() throws Exception {
//		DhtAddress addr = new DefaultDhtAddress("147.162.96.137", 5010, 5010);
//		DhtID id = new DefaultDhtID("49995c3ba6300ad58800c1be4fc48d5ae0e28d2cbfe4c3902280af7ad66e2fe2486d3d2c7fca5857b7e677f4c35403e40e089a876b6dd65265eaa9a80f818a02");
//		PublicKey pubKey = DhtSigningUtils.getSignKeyPair().getPublic();
//		DhtNode node = new DefaultDhtNode(id, pubKey, addr);
//		RoutingTable table = new DefaultRoutingTable(node);
//		table.insertNode(node) ;
//	}
	
	@Test
	public void testSingleInsert() throws Exception {
		underTest.insertNode(nodes.get(0));
		assertTrue("Something went wrong", true);
	}
	
	@Test
	public void testMultipleInsertions() throws Exception {
		for(DhtNodeExtended node : moreNodes){
			underTest.insertNode(node);
		}
		assertTrue("Something went wrong", true);
	}
	
	@Test
	public void testSearchForSingleNode() throws Exception {
		underTest.insertNode(nodes.get(0));
		
		Collection<DhtNodeExtended> result = underTest.findClosest(nodes.get(0).getID());
		
		Collection<DhtNodeExtended> expected = new ArrayList<DhtNodeExtended>();
		expected.add(baseNode);
		expected.add(nodes.get(0));
		
		assertTrue("Wrong lookup result!", expected.containsAll(result));
	}
	
	@Test
	public void testSearchInUnderPopulatedTable1() throws Exception {
		underTest.insertNode(nodes.get(1));
		underTest.insertNode(nodes.get(2));
		underTest.insertNode(nodes.get(3));
		underTest.insertNode(nodes.get(4));
		underTest.insertNode(nodes.get(5));
		
		Collection<DhtNodeExtended> result = underTest.findClosest(nodes.get(1).getID());
		
		assertTrue("Missing searched node!", result.contains(nodes.get(1)));
		
		assertEquals("Wrong result size!", 6, result.size()); //inserted 5 nodes + "my node"
	}
	
	@Test
	public void testSearchInUnderPopulatedTable2() throws Exception {
		underTest.insertNode(nodes.get(1));
		underTest.insertNode(nodes.get(2));
		underTest.insertNode(nodes.get(3));
		underTest.insertNode(nodes.get(4));
		underTest.insertNode(nodes.get(5));
		
		Collection<DhtNodeExtended> result = underTest.findClosest(nodes.get(0).getID());
		
		assertTrue("Missing searched node!", !result.contains(nodes.get(0)));
		
		assertEquals("Wrong result size!", 6, result.size()); //inserted 5 nodes + "my node"
	}
	
	@Test
	public void testSearchForNodeAmongOthers() throws Exception {
		for(DhtNodeExtended node : moreNodes){
			underTest.insertNode(node);
		}
		
		Collection<DhtNodeExtended> result = underTest.findClosest(moreNodes.get(0).getID());
		
		assertEquals("Less nodes than desired!", DhtConstants.ALPHA_SIZE, result.size());
		
		assertTrue("Desired node is missing!", result.contains(moreNodes.get(0)));		
	}
	
	@Test
	public void testXorMetricOfResults1() throws Exception {
		for(DhtNodeExtended node : moreNodes){
			underTest.insertNode(node);
		}
		
		ArrayList<DhtNodeExtended> result = new ArrayList<DhtNodeExtended>(underTest.findClosest(nodes.get(0).getID())); //searching an ID that is probably not in the table.
		
		BigInteger xorInEntireSet = DhtUtils.xor(moreNodes.get(0).getID(), nodes.get(0).getID());
		for(int i = 1; i < moreNodes.size(); i++){
			BigInteger xor = DhtUtils.xor(moreNodes.get(i).getID(), nodes.get(0).getID());
			if(xor.compareTo(xorInEntireSet)<0){
				xorInEntireSet = xor;
			}
		}
		
		BigInteger xorInResultSet = DhtUtils.xor(result.get(0).getID(), nodes.get(0).getID());
		for(int i = 1; i < result.size(); i++){
			BigInteger xor = DhtUtils.xor(result.get(i).getID(), nodes.get(0).getID());
			if(xor.compareTo(xorInResultSet)<0){
				xorInResultSet = xor;
			}
		}
		
		System.out.println("XOR in result set: "+xorInResultSet);
		System.out.println("XOR in entire set: "+xorInEntireSet);
		
		assertTrue("Xor is bigger than expected!", xorInResultSet.compareTo(xorInEntireSet) <= 0);
		
	}
	
	@Test
	public void testXorMetricOfResults2() throws Exception {
		for(DhtNodeExtended node : moreNodes){
			underTest.insertNode(node);
		}
		underTest.insertNode(nodes.get(0));
		
		ArrayList<DhtNodeExtended> result = new ArrayList<DhtNodeExtended>(underTest.findClosest(nodes.get(0).getID())); 
		
		BigInteger xorInResultSet = DhtUtils.xor(result.get(0).getID(), nodes.get(0).getID());
		for(int i = 1; i < result.size(); i++){
			BigInteger xor = DhtUtils.xor(result.get(i).getID(), nodes.get(0).getID());
			if(xor.compareTo(xorInResultSet)<0){
				xorInResultSet = xor;
			}
		}
		
		assertTrue("Xor is not the one expected!", xorInResultSet.compareTo(new BigInteger("0")) == 0);
		
		assertTrue("Missing right node", result.contains(nodes.get(0)));
		
	}
	
	@Test
	public void testRemoveNode() throws Exception {
		for(DhtNodeExtended node : moreNodes){
			underTest.insertNode(node);
		}
		
		underTest.removeNode(moreNodes.get(0));
		
		Collection<DhtNodeExtended> result = underTest.findClosest(moreNodes.get(0).getID());
		
		assertTrue("Searched node should not be in the result collection!", !result.contains(moreNodes.get(0)));
	}
	
	@Test
	public void testSearchMyNode() throws Exception {
		for(DhtNodeExtended node : nodes){
			underTest.insertNode(node);
		}
		
		for(DhtNodeExtended node : moreNodes){
			underTest.insertNode(node);
		}
		
		Collection<DhtNodeExtended> result = underTest.findClosest(baseNode.getID());
		
		assertTrue("Searched node should be in the result collection!", result.contains(baseNode));
		
		assertEquals("Less nodes than desired!", DhtConstants.ALPHA_SIZE, result.size());		
	}

}
