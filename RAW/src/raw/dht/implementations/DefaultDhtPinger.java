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
package raw.dht.implementations;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import raw.dht.DhtConstants;
import raw.dht.DhtCore;
import raw.dht.DhtNode;
import raw.dht.DhtNodeExtended;
import raw.dht.DhtPinger;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.exceptions.IncompleteNodeExtendedException;
import raw.dht.implementations.utils.DhtSigningUtils;
import raw.dht.messages.UdpDhtMessageMarshaller;
import raw.dht.messages.implementations.udp.PingMessage;
import raw.logger.Log;

/**
 * @author vic
 *
 */
public class DefaultDhtPinger implements DhtPinger {
	
	private DhtCore myCore;
	
	private boolean running;
	private boolean isStopped;
	
	private UdpDhtMessageMarshaller marshaller;
	
	private Log log;
	
	public DefaultDhtPinger(DhtCore owner) {
		myCore = owner;
		running = true;
		isStopped = false;
		
		marshaller = UdpDhtMessageMarshaller.getMarshaller();
		
		log = Log.getLogger();
		log.verboseDebug("Pinger constructed.");
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Void call() throws Exception {
		while (running) {
			log.verboseDebug("Asking a random node to ping...");
			DhtNodeExtended node = myCore.getRoutingTable().randomNode();
			if(node != null){
				log.debug("Going to ping: "+node);
				if(!sendPing(node)){
					log.debug(node+" failed to respond to ping. Deleting it.");
					myCore.getRoutingTable().removeNode(node);
				} else {
					log.debug("Pong received from "+node);
				}
			} else {
				log.verboseDebug("Random node is null. Skipping it.");				
			}
			Thread.sleep(DhtConstants.PING_INTERTIME);
		}
		isStopped = true;
		return null;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtPinger#sendPing(raw.dht.DhtNode)
	 */
	@Override
	public boolean sendPing(DhtNodeExtended node) {		
		PingMessage pong = sendPingAndReturnPong(node);
		if(pong == null){
			myCore.getRoutingTable().removeNode(node);
			return false;
		}
		if(!pong.isPingReply()){
			log.verboseDebug("PongMessage is NOT a reply.");
			myCore.getRoutingTable().removeNode(node);
			return false;
		}
		if(pong.getSender().equalsWithTransaction(node)){
			log.verboseDebug("PongMessage is valid!");
			myCore.getRoutingTable().insertNode(pong.getSender());
			return true;
		} else 	if(pong.getSender().equals(node)){
			log.verboseDebug("Discrepancy in PongMessage Transaction data!");
			if(myCore.isOldWorker(pong.getSender())){
				log.verboseDebug("PongMessage provided transaction data proved to be valid!");
				myCore.getRoutingTable().insertNode(pong.getSender());
				return true;
			} else{
				log.verboseDebug("PongMessage sender is no more a valid old worker!");
				myCore.getRoutingTable().removeNode(node);
				return false;
			}
		} else {
			log.verboseDebug("PongMessage is from wrong node!");
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see raw.dht.DhtPinger#sendAuthenticationPing(raw.dht.DhtNode)
	 */
	@Override
	public boolean sendAuthenticationPing(DhtNode node) {
		PingMessage pong = sendPingAndReturnPong(node);
		if(pong == null){
			return false;
		}
		if(!pong.isPingReply()){
			log.verboseDebug("PongMessage is NOT a reply.");
			return false;
		}
		if(pong.getSender().equals(node)){
			log.verboseDebug("PongMessage is valid!");
			return myCore.isOldWorker(pong.getSender());
		} else {
			log.verboseDebug("PongMessage is from wrong node!");
			return false;
		}
	}

	/**
	 * Just send the ping and return the <b>pong</b>
	 * message or <code>null</code> otherwise.
	 * 
	 * @param node
	 * @return
	 */
	private PingMessage sendPingAndReturnPong(DhtNode node){
		try {
			if(node.equals(myCore.getNodeExtended())){
				log.verboseDebug("No sense in pinging my own node.");
				return null;
			}
		} catch (IncoherentTransactionException | IncompleteNodeExtendedException e1) {
			log.exception(e1);
			log.verboseDebug("Exception retrieving local node data. Aborting.");
			return null;
		}
		log.verboseDebug("Preparing to ping "+node);
		PingMessage ping;
		byte[] signature;
		try {
			signature = DhtSigningUtils.signDhtAddress(myCore.getNodeExtended().getAddress(), myCore.getKeyPair().getPrivate());
		} catch (IncoherentTransactionException | IncompleteNodeExtendedException e1) {
			log.exception(e1);
			log.verboseDebug("Exception retrieving local node data. Aborting.");
			return null;
		}
		
		DhtNodeExtended myNode;
		try {
			myNode = myCore.getNodeExtended();
		} catch (IncoherentTransactionException | IncompleteNodeExtendedException e1) {
			log.exception(e1);
			log.verboseDebug("Exception retrieving local node data. Aborting.");
			return null;
		}
		ping = new PingMessage(false, myNode, signature);
		log.verboseDebug("Built ping (My node: "+myNode+").");
		
		try (DatagramSocket sock = new DatagramSocket()){
			sock.setSoTimeout(DhtConstants.TIMEOUT_MILLISECONDS);
			DatagramPacket datagram = marshaller.messageToDatagram(ping);
			datagram.setSocketAddress(node.getAddress().getUdpSocketAddress());
			log.verboseDebug("Preparing to send ping datagram "+datagram+" to "+datagram.getSocketAddress());
			sock.send(datagram);
			log.verboseDebug("Ping datagram sent.");
			byte[] buff = new byte[marshaller.getBufferSize()];
			DatagramPacket received = new DatagramPacket(buff, buff.length);
			log.verboseDebug("Ready to receive pong.");
			sock.receive(received);
			PingMessage pong = (PingMessage) marshaller.datagramToMessage(received);
			log.verboseDebug("Datagram de-marshalled.");
			return pong;			
		} catch (Exception e) {
			log.exception(e);
			for(Throwable t : e.getSuppressed()){
				log.exception(t);
			}
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtPinger#stop()
	 */
	@Override
	public boolean stop() {
		running = false;
		return true;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtPinger#isStopped()
	 */
	@Override
	public boolean isStopped() {
		return isStopped;
	}

}
