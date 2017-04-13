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
package raw.dht.messages.implementations.udp;

import raw.blockChain.api.Block;
import raw.dht.DhtNodeExtended;
import raw.dht.messages.DhtMessage;

/**
 * @author vic
 *
 */
public class PingMessage implements DhtMessage {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = -3007782694716245833L;
	
	private boolean pingReply;
	
	private DhtNodeExtended senderNode;
	
	private byte[] signature;
	
	public PingMessage(boolean pingReply, DhtNodeExtended senderNode, byte[] signature) {
		this.pingReply = pingReply;
		this.senderNode = senderNode;
		this.signature = signature;
	}

	/* (non-Javadoc)
	 * @see raw.dht.messages.DhtMessage#getMessageType()
	 */
	@Override
	public MessageType getMessageType() {
		if(pingReply){
			return MessageType.PONG;
		} else {
			return MessageType.PING;
		}
	}

	/**
	 * @return the pingReply
	 */
	public boolean isPingReply() {
		return pingReply;
	}

	/**
	 * @return the transaction {@link Block} number
	 */
	public long getTransactionBlockNumber() {
		return senderNode.getTransactionBlockNumber();
	}

	/**
	 * @return the signature
	 */
	public byte[] getSignature() {
		return signature;
	}

	@Override
	public DhtNodeExtended getSender() {
		return senderNode;
	}

}
