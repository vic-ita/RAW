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

import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;

import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultTransaction;
import raw.dht.DhtAddress;
import raw.dht.DhtConstants;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.DhtNode;
import raw.dht.DhtNodeExtended;
import raw.dht.implementations.DefaultDhtAddress;
import raw.dht.implementations.DefaultDhtHasher;
import raw.dht.implementations.DefaultDhtID;
import raw.dht.implementations.DefaultDhtNode;
import raw.dht.implementations.DefaultDhtNodeExtended;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.utils.DhtSigningUtils;
import raw.dht.messages.DhtMessage.MessageType;
import raw.dht.messages.implementations.udp.FindNodeMessage;
import raw.dht.messages.implementations.udp.PingMessage;
import raw.logger.Log;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * A specialized "serializer"/"deserializer" for
 * those dht messages sent via UDP protocol.
 * 
 * @author vic
 *
 */
public class UdpDhtMessageMarshaller {
	
	private static UdpDhtMessageMarshaller singleton = new UdpDhtMessageMarshaller();
	
	private static int bufferSize;
	
	private static int hashSize;
	
	private static int pubKeySize;
	
	private static int signatureSize;
	
	private static Log log;

	private UdpDhtMessageMarshaller() {
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		DhtHasher hasher = new DefaultDhtHasher();
		DhtID probeId = hasher.hashString("probe");
		byte[] ipBytes = {10, 0, 0, 1};
		DhtAddress probeAddress = null;
		try {
			probeAddress = new DefaultDhtAddress(ipBytes, 1024);
		} catch (UnknownHostException | IllegalArgumentException e) {
			log.exception(e);
		}
		KeyPair probeKeyPair = DhtSigningUtils.getSignKeyPair();
		DhtNode probeNode = new DefaultDhtNode(probeId, probeKeyPair.getPublic(), probeAddress);
		Transaction probeTransaction = new DefaultTransaction(probeId, 42L, 0L, probeKeyPair.getPublic());
		DhtNodeExtended probeNodeExtended = null;
		try {
			probeNodeExtended = new DefaultDhtNodeExtended(probeNode, probeTransaction, 42L);
		} catch (IncoherentTransactionException e) {
			log.exception(e);
		}
		byte[] probeSignature = DhtSigningUtils.signDhtAddress(probeAddress, probeKeyPair.getPrivate());
		
		hashSize = new DefaultDhtHasher().hashLength();
		pubKeySize = probeKeyPair.getPublic().getEncoded().length;
		signatureSize = probeSignature.length;
		
		DhtMessage message = new PingMessage(false, probeNodeExtended, probeSignature);
		byte[] converted = convertMessageToByteArray(message);
		sizes.add(new Integer(converted.length));
		
		message = new PingMessage(true, probeNodeExtended, probeSignature);
		converted = convertMessageToByteArray(message);
		sizes.add(new Integer(converted.length));
		
		message = new FindNodeMessage(probeNodeExtended, probeId);
		converted = convertMessageToByteArray(message);
		sizes.add(new Integer(converted.length));
		
		ArrayList<DhtNodeExtended> replyNodes = new ArrayList<DhtNodeExtended>();
		for (int i = 0; i < DhtConstants.ALPHA_SIZE; i++) {
			replyNodes.add(probeNodeExtended);
		}
		message = new FindNodeMessage(probeNodeExtended, probeId, replyNodes);
		converted = convertMessageToByteArray(message);
		sizes.add(new Integer(converted.length));
		
		bufferSize = -1;
		for(Integer size : sizes){
			bufferSize = Math.max(bufferSize, size.intValue());
		}
		
		log = Log.getLogger();
	}
	
	public static UdpDhtMessageMarshaller getMarshaller(){
		if(singleton == null){
			singleton = new UdpDhtMessageMarshaller();
		}
		return singleton;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public DatagramPacket messageToDatagram(DhtMessage message) {
		byte[] bytes = singleton.convertMessageToByteArray(message);
		return new DatagramPacket(bytes, bytes.length);
	}
	
	public DhtMessage datagramToMessage(DatagramPacket packet) { 
		ByteArrayDataInput badi = ByteStreams.newDataInput(packet.getData());
		byte dhtProtocolVersion = badi.readByte();
		if(dhtProtocolVersion == 1){
			int ordinal = badi.readInt();
			MessageType type = MessageType.values()[ordinal];
			
			DhtMessage message = null;
			
			if(type == MessageType.PING || type == MessageType.PONG){
				try {
					message = singleton.decodePingMessage(type, badi);
				} catch (UnknownHostException | IllegalArgumentException e) {
					log.exception(e);
				}
			} else if (type == MessageType.FIND_NODE || type == MessageType.FIND_NODE_REPLY) {
				try {
					message = singleton.decodeFindNodeMessa(type, badi);
				} catch (IllegalArgumentException | UnknownHostException e) {
					log.exception(e);
				}
			}
			
			return message;
		} else {
			// for future backward compatibility
		}
		return null;		
	}
	
	private byte[] convertMessageToByteArray(DhtMessage message) {
		ByteArrayDataOutput bado = ByteStreams.newDataOutput();
		byte dhtProtocolVersion = 1;
		bado.write(dhtProtocolVersion);
		if(message instanceof PingMessage){
			bado = writePing(bado, (PingMessage) message);
		} else {
			if(message instanceof FindNodeMessage){
				bado = writeFindNode(bado, (FindNodeMessage) message);
			}
		}
		return bado.toByteArray();		
	}
	
	private ByteArrayDataOutput writePing(ByteArrayDataOutput bado, PingMessage message){
		MessageType type = message.getMessageType();
		bado.writeInt(type.ordinal());
		byte[] idBytes = message.getSender().getID().toByteArray();
		bado.write(idBytes);
		byte[] ipBytes = message.getSender().getAddress().getAddress().getAddress();
		bado.write(ipBytes);
		bado.writeInt(message.getSender().getAddress().getUdpPort());
		bado.writeInt(message.getSender().getAddress().getTcpPort());
		byte[] pubKeyBytes = message.getSender().getPublicKey().getEncoded();
		bado.write(pubKeyBytes);
		bado.writeLong(message.getTransactionBlockNumber());
		Transaction transaction = message.getSender().getTransaction();
		bado.writeLong(transaction.getCreationSeedNumber());
		bado.writeLong(transaction.getTransactionNonce());
		byte[] signature = message.getSignature();
		bado.write(signature);
		return bado;
	}
	
	private PingMessage decodePingMessage(MessageType type, ByteArrayDataInput badi) throws UnknownHostException, IllegalArgumentException{
		if(!(type == MessageType.PING || type == MessageType.PONG)){
			throw new IllegalArgumentException();
		}
		byte[] idBytes = new byte[hashSize];
		badi.readFully(idBytes);
		byte[] ipBytes = new byte[4];
		badi.readFully(ipBytes);
		int udpPort = badi.readInt();
		int tcpPort = badi.readInt();
		byte[] pubKeyBytes = new byte[pubKeySize];
		badi.readFully(pubKeyBytes);
		long transactionNumber = badi.readLong();
		long creationSeedNumber = badi.readLong();
		long transactionNonce = badi.readLong();
		byte[] signature = new byte[signatureSize];
		badi.readFully(signature);
		
		DhtID id = new DefaultDhtID(idBytes);
		DhtAddress addr = null;

		addr = new DefaultDhtAddress(ipBytes, udpPort, tcpPort);
				
		DhtNode node = new DefaultDhtNode(id, pubKeyBytes, addr);
		Transaction transaction = new DefaultTransaction(id, transactionNonce, creationSeedNumber, pubKeyBytes);
		
		DhtNodeExtended nodeExtended = null;
		try {
			nodeExtended = new DefaultDhtNodeExtended(node, transaction, transactionNumber);
		} catch (IncoherentTransactionException e) {
			log.exception(e);
		}
		
		PingMessage message;
		
		if(type == MessageType.PING){
			message = new PingMessage(false, nodeExtended, signature);
		} else {
			message = new PingMessage(true, nodeExtended, signature);
		}
		
		return message;
	}
	
	private ByteArrayDataOutput writeFindNode(ByteArrayDataOutput bado, FindNodeMessage message){
		MessageType type = message.getMessageType();
		bado.writeInt(type.ordinal());
		byte[] senderID = message.getSender().getID().toByteArray();
		bado.write(senderID);
		int addressLength = message.getSender().getAddress().getAddress().getAddress().length;
		bado.writeInt(addressLength);
		byte[] senderAddress = message.getSender().getAddress().getAddress().getAddress();
		bado.write(senderAddress);
		bado.writeInt(message.getSender().getAddress().getTcpPort());
		bado.writeInt(message.getSender().getAddress().getUdpPort());
		bado.writeLong(message.getSender().getTransactionBlockNumber());
		bado.writeLong(message.getSender().getTransaction().getCreationSeedNumber());
		bado.writeLong(message.getSender().getTransaction().getTransactionNonce());
		byte[] senderPubKey = message.getSender().getPublicKey().getEncoded();
		bado.write(senderPubKey);
		byte[] idBytes = message.getTargetId().toByteArray();
		bado.write(idBytes);
		if(!(type == MessageType.FIND_NODE)){//this is a reply.
			Collection<DhtNodeExtended> nodes = message.getReplyNodes();
			int size = nodes.size();
			bado.writeInt(size);
			
			for(DhtNodeExtended node : nodes){
				byte[] nodeId = node.getID().toByteArray();
				byte[] nodeAddress = node.getAddress().getAddress().getAddress();
				int udpPort = node.getAddress().getUdpPort();
				int tcpPort = node.getAddress().getTcpPort();
				byte[] nodePubKey = node.getPublicKey().getEncoded();
				long nodeTransactionBlockNumber = node.getTransactionBlockNumber();
				long nodeTransactionCreationSeedNumber = node.getTransaction().getCreationSeedNumber();
				long nodeTransactionNonce = node.getTransaction().getTransactionNonce();
				
				bado.write(nodeId);
				bado.write(nodeAddress);
				bado.writeInt(udpPort);
				bado.writeInt(tcpPort);
				bado.write(nodePubKey);
				bado.writeLong(nodeTransactionBlockNumber);
				bado.writeLong(nodeTransactionCreationSeedNumber);
				bado.writeLong(nodeTransactionNonce);
			}			
		}
		return bado;
	}

	private FindNodeMessage decodeFindNodeMessa(MessageType type, ByteArrayDataInput badi) throws IllegalArgumentException, UnknownHostException {
		FindNodeMessage message;
		byte[] senderId = new byte[hashSize];
		badi.readFully(senderId);
		int addressLenght = badi.readInt();
		byte[] senderAddress = new byte[addressLenght];
		badi.readFully(senderAddress);
		int tcpSenderPort = badi.readInt();
		int udpSenderPort = badi.readInt();
		long transactionBlockNumber = badi.readLong();
		long transactionCreationSeedNumber = badi.readLong();
		long transactionNonce = badi.readLong();
		byte[] senderPubKey = new byte[pubKeySize];
		badi.readFully(senderPubKey);
		DhtID senderDhtId = new DefaultDhtID(senderId);
		DhtNode senderNode = new DefaultDhtNode(senderDhtId, senderPubKey, new DefaultDhtAddress(senderAddress, udpSenderPort, tcpSenderPort));
		Transaction senderTransaction = new DefaultTransaction(senderDhtId, transactionNonce, transactionCreationSeedNumber, senderPubKey);
		DhtNodeExtended senderNodeExtended = null;
		try {
			senderNodeExtended = new DefaultDhtNodeExtended(senderNode, senderTransaction, transactionBlockNumber);
		} catch (IncoherentTransactionException e) {
			log.exception(e);
		}
		byte[] targetId = new byte[hashSize];
		badi.readFully(targetId);
		if(type == MessageType.FIND_NODE){
			message = new FindNodeMessage(senderNodeExtended, new DefaultDhtID(targetId));
		} else if (type == MessageType.FIND_NODE_REPLY){
			int size = badi.readInt();
			ArrayList<DhtNodeExtended> replyNodes = new ArrayList<DhtNodeExtended>();
			for(int i = 0; i < size; i++){
				byte[] nodeId = new byte[hashSize];
				badi.readFully(nodeId);
				byte[] nodeIP = new byte[4];
				badi.readFully(nodeIP);
				int udpPort = badi.readInt();
				int tcpPort = badi.readInt();
				byte[] nodePubKey = new byte[pubKeySize];
				badi.readFully(nodePubKey);
				long nodeTransactionBlockNumber = badi.readLong();
				long nodeTransactionCreationSeedNumber = badi.readLong();
				long nodeTransactionNonce = badi.readLong();
				
				DhtID nodeDhtId = new DefaultDhtID(nodeId);
				DhtNode node = new DefaultDhtNode(nodeDhtId, nodePubKey, new DefaultDhtAddress(nodeIP, udpPort, tcpPort));
				Transaction nodeTransaction = new DefaultTransaction(nodeDhtId, nodeTransactionNonce, nodeTransactionCreationSeedNumber, nodePubKey);
				DhtNodeExtended nodeExtended = null;
				try {
					nodeExtended = new DefaultDhtNodeExtended(node, nodeTransaction, nodeTransactionBlockNumber);
				} catch (IncoherentTransactionException e) {
					log.exception(e);
				}
				
				replyNodes.add(nodeExtended);
			}
			message = new FindNodeMessage(senderNodeExtended, new DefaultDhtID(targetId), replyNodes);
		} else {
			throw new IllegalArgumentException();
		}
		return message;
	}
	
}
