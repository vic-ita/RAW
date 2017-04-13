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
package raw.dht.messages.implementations.tcp;

import raw.dht.DhtKey;
import raw.dht.DhtNodeExtended;
import raw.dht.DhtValue;
import raw.dht.messages.DhtMessage;

/**
 * Message used to store a couple
 * {@link DhtKey} - {@link DhtValue} on 
 * the DHT.
 * 
 * @author vic
 *
 */
public class StoreMessage implements DhtMessage {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = 3152544210933153637L;
	
	private DhtNodeExtended sender;
	
	private DhtKey key;
	private DhtValue value;
	
	private boolean accepted;
	
	/**
	 * Constructs a store request for a couple
	 * {@link DhtKey} - {@link DhtValue}
	 * 
	 * @param sender the message sender {@link DhtNodeExtended} representation
	 * @param key a {@link DhtKey} to be stored
	 * @param value a {@link DhtValue} to be stored
	 */
	public StoreMessage(DhtNodeExtended sender, DhtKey key, DhtValue value) {
		this.sender = sender;
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Constructs a reply to a store request.
	 * 
	 * @param sender the message sender {@link DhtNodeExtended} representation
	 * @param accepted <tt>true</tT> if store is accepted, <tt>false</tt> if refused
	 */
	public StoreMessage(DhtNodeExtended sender, boolean accepted) {
		this.sender = sender;
		this.accepted = accepted;
		key = null;
		value = null;
	}

	/* (non-Javadoc)
	 * @see raw.dht.messages.DhtMessage#getMessageType()
	 */
	@Override
	public MessageType getMessageType() {
		if(key == null && value == null){
			return MessageType.STORE_REPLY;
		}
		return MessageType.STORE;
	}

	public DhtKey getKey() {
		return key;
	}

	public DhtValue getValue() {
		return value;
	}

	public boolean isAccepted() {
		return accepted;
	}

	@Override
	public DhtNodeExtended getSender() {
		return sender;
	}

}
