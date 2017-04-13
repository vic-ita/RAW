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
package raw.dht.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultTransaction;
import raw.blockChain.transactionGenerators.RandomNodesGenerator;
import raw.dht.DhtConstants;
import raw.dht.DhtNodeExtended;
import raw.dht.implementations.DefaultDhtNodeExtended;
import raw.dht.implementations.utils.DhtSigningUtils;
import raw.dht.messages.DhtMessage.MessageType;
import raw.dht.messages.implementations.udp.FindNodeMessage;
import raw.dht.messages.implementations.udp.PingMessage;

public class UdpDhtMessageMarshallerTest {
	
	PingMessage ping;
	PingMessage pong;
	FindNodeMessage search;
	FindNodeMessage reply;
	
	InetSocketAddress address;
	
	static RandomNodesGenerator gen;
	static ArrayList<DhtNodeExtended> nodes;
	
	static ArrayList<DhtNodeExtended> moreNodes;
	
	@BeforeClass
	public static void startUp() throws Exception{
		gen = new RandomNodesGenerator();
		nodes = gen.getNodes(DhtConstants.K_SIZE);
		
		moreNodes = gen.getNodes(2);
	}

	@Before
	public void setUp() throws Exception {
//		RandomNodesGenerator gen = new RandomNodesGenerator();
//		ArrayList<DhtNodeExtended> nodes = gen.getNodes(DhtConstants.K_SIZE);
//		
//		ArrayList<DhtNodeExtended> moreNodes = gen.getNodes(2);
		KeyPair keyPair = DhtSigningUtils.getSignKeyPair();
		Transaction transaction = new DefaultTransaction(moreNodes.get(0).getTransaction().getDhtID(), moreNodes.get(0).getTransaction().getTransactionNonce(), moreNodes.get(0).getTransaction().getCreationSeedNumber(), keyPair.getPublic());
		DhtNodeExtended pingNode = new DefaultDhtNodeExtended(moreNodes.get(0).getID(), moreNodes.get(0).getAddress(), keyPair.getPublic(), transaction, moreNodes.get(0).getTransactionBlockNumber());
		
		byte[] pingSignature = DhtSigningUtils.signDhtAddress(moreNodes.get(0).getAddress(), keyPair.getPrivate()); 
		ping = new PingMessage(false, pingNode, pingSignature);
		
		keyPair = DhtSigningUtils.getSignKeyPair();
		transaction = new DefaultTransaction(moreNodes.get(1).getTransaction().getDhtID(), moreNodes.get(1).getTransaction().getTransactionNonce(), moreNodes.get(1).getTransaction().getCreationSeedNumber(), keyPair.getPublic());
		DhtNodeExtended pongNode = new DefaultDhtNodeExtended(moreNodes.get(1).getID(), moreNodes.get(1).getAddress(), keyPair.getPublic(), transaction, moreNodes.get(1).getTransactionBlockNumber());
		byte[] pongSignature = DhtSigningUtils.signDhtAddress(moreNodes.get(1).getAddress(), keyPair.getPrivate());
		pong = new PingMessage(true, pongNode, pongSignature);
		
		search = new FindNodeMessage(nodes.get(0), nodes.get(2).getID());
		reply = new FindNodeMessage(nodes.get(0), nodes.get(2).getID(), nodes.subList(0, DhtConstants.ALPHA_SIZE));
		
		address = nodes.get(10).getAddress().getUdpSocketAddress();
	}

	@After
	public void tearDown() throws Exception {
		ping = null;
		pong = null;
		search = null;
		reply = null;
		
		address = null;
	}

	@Test
	public void testConstructor() {
		UdpDhtMessageMarshaller marshaller = UdpDhtMessageMarshaller.getMarshaller();
		
		assertNotNull("Object should not be null", marshaller);
		
		assertNotEquals("Buffer size should be defined!", -1, marshaller.getBufferSize());
		
		System.out.println("Marshaller asserts that buffer size should be == "+marshaller.getBufferSize());
	}
	
	@Test
	public void testPingMessage1() throws Exception {
		UdpDhtMessageMarshaller marshaller = UdpDhtMessageMarshaller.getMarshaller();
		DatagramPacket datagram = marshaller.messageToDatagram(ping);
		
		datagram.setSocketAddress(address);
		
		DhtMessage msg = marshaller.datagramToMessage(datagram);
		
		assertTrue("Wrong deserialized type!", msg instanceof PingMessage);
		
		PingMessage deserialized = (PingMessage) msg;
		
		assertFalse("This should be a PING!", deserialized.isPingReply());
		
		assertEquals("Node differ!!", ping.getSender(), deserialized.getSender());
	}
	
	@Test
	public void testPingMessage2() throws Exception {
		UdpDhtMessageMarshaller marshaller = UdpDhtMessageMarshaller.getMarshaller();
		DatagramPacket datagram = marshaller.messageToDatagram(pong);
		
		datagram.setSocketAddress(address);
		
		DhtMessage msg = marshaller.datagramToMessage(datagram);
		
		assertTrue("Wrong deserialized type!", msg instanceof PingMessage);
		
		PingMessage deserialized = (PingMessage) msg;
		
		assertTrue("This should be a PONG!", deserialized.isPingReply());
		
		assertEquals("Node differ!!", pong.getSender(), deserialized.getSender());
	}
	
	@Test
	public void testFindNode1() throws Exception {
		UdpDhtMessageMarshaller marshaller = UdpDhtMessageMarshaller.getMarshaller();
		DatagramPacket datagram = marshaller.messageToDatagram(search);
		
		datagram.setSocketAddress(address);
		
		DhtMessage msg = marshaller.datagramToMessage(datagram);
		
		assertTrue("Wrong deserialized type!", msg instanceof FindNodeMessage);
		
		FindNodeMessage deserialized = (FindNodeMessage) msg;
		
		assertEquals("Wrong message type!", MessageType.FIND_NODE, deserialized.getMessageType());
		
		assertEquals("Wrong target", search.getTargetId(), deserialized.getTargetId());
	}
	
	@Test
	public void testFindNode2() throws Exception {
		UdpDhtMessageMarshaller marshaller = UdpDhtMessageMarshaller.getMarshaller();
		DatagramPacket datagram = marshaller.messageToDatagram(reply);
		
		datagram.setSocketAddress(address);
		
		DhtMessage msg = marshaller.datagramToMessage(datagram);
		
		assertTrue("Wrong deserialized type!", msg instanceof FindNodeMessage);
		
		FindNodeMessage deserialized = (FindNodeMessage) msg;
		
		assertEquals("Wrong message type!", MessageType.FIND_NODE_REPLY, deserialized.getMessageType());
		
		assertEquals("Wrong nodes set size", reply.getReplyNodes().size(), deserialized.getReplyNodes().size());
		
		assertTrue("First compare of nodes set failing!", reply.getReplyNodes().containsAll(deserialized.getReplyNodes()));
		assertTrue("Second compare of nodes set failing!", deserialized.getReplyNodes().containsAll(reply.getReplyNodes()));		
	}

}
