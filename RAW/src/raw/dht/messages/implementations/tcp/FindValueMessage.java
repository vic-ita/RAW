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

import java.util.ArrayList;
import java.util.Collection;

import raw.dht.DhtKey;
import raw.dht.DhtNode;
import raw.dht.DhtNodeExtended;
import raw.dht.DhtValue;
import raw.dht.messages.DhtMessage;

/**
 * Message used in the DHT to ask a {@link DhtNode}
 * if it knows at least one {@link DhtValue} for a given {@link DhtKey}.
 * 
 * @author vic
 *
 */
public class FindValueMessage implements DhtMessage {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = -9213069353190100808L;
	
	private DhtNodeExtended sender;
	
	private DhtKey key;
	private Collection<DhtValue> value;
		
	/**
	 * Builds a message request for the {@link DhtValue}s associated to 
	 * a given {@link DhtKey}.
	 * 
	 * @param sender the message sender {@link DhtNodeExtended} representation
	 * @param key the {@link DhtKey} searched
	 */
	public FindValueMessage(DhtNodeExtended sender, DhtKey key) {
		this(sender, key, null);
	}
	
	/**
	 * Builds a message with a collection of {@link DhtValue} response to a 
	 * certain  {@link DhtKey} sarch.
	 * 
	 * @param sender the message sender {@link DhtNodeExtended} representation
	 * @param value a {@link DhtValue} response
	 */
	public FindValueMessage(DhtNodeExtended sender, Collection<DhtValue> value) {
		this(sender, null, value);
	}
	
	/**
	 * Build a message empty response to a {@link DhtKey}
	 * search. This should be used if a node does not know any {@link DhtValue}
	 * associated to a certain {@link DhtKey}.
	 * 
	 * @param sender the message sender {@link DhtNodeExtended} representation
	 */
	public FindValueMessage(DhtNodeExtended sender) {
		this(sender, null, null);
	}
	
	private FindValueMessage(DhtNodeExtended sender, DhtKey key, Collection<DhtValue> value) {
		this.sender = sender;
		this.key = key;
		if(value == null){
			this.value = null;
		} else {			
			this.value = new ArrayList<>(value);
		}
	}

	/* (non-Javadoc)
	 * @see raw.dht.messages.DhtMessage#getMessageType()
	 */
	@Override
	public MessageType getMessageType() {
		if(key == null && value == null){
			return MessageType.FIND_VALUE_EMPTY_REPLY;
		}
		if(key == null){
			return MessageType.FIND_VALUE_REPLY;
		}
		return MessageType.FIND_VALUE;
	}

	public DhtKey getKey() {
		return key;
	}

	public Collection<DhtValue> getValue() {
		return value;
	}

	@Override
	public DhtNodeExtended getSender() {
		return sender;
	}

}
