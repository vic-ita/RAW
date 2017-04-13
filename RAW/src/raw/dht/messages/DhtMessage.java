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
package raw.dht.messages;

import java.io.Serializable;

import raw.dht.DhtNodeExtended;

/**
 * A generic wrapper for all messages to be used in the DHT
 * 
 * @author vic
 *
 */
public interface DhtMessage extends Serializable {
	
	/**
	 * @return the {@link MessageType} of this message
	 */
	public MessageType getMessageType();
	
	/**
	 * Returns the {@link DhtNodeExtended} representation of 
	 * the node sending this message.
	 * 
	 * @return a {@link DhtNodeExtended} representing the sender node
	 */
	public DhtNodeExtended getSender();
	
	public enum MessageType{
		PING,
		PONG,
		STORE,
		STORE_REPLY,
		FIND_NODE,
		FIND_NODE_REPLY,
		FIND_VALUE,
		FIND_VALUE_REPLY,
		FIND_VALUE_EMPTY_REPLY;
	}

}
