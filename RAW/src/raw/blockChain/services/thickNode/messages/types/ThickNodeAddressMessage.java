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
package raw.blockChain.services.thickNode.messages.types;

import java.net.InetSocketAddress;

import raw.blockChain.services.thickNode.ThickNode;

/**
 * Use this message to ask a new {@link ThickNode} address.
 * 
 * @author vic
 *
 */
public class ThickNodeAddressMessage extends NodeAddressMessage {

	/**
	 * Random generated UID
	 */
	private static final long serialVersionUID = 6624163869377801974L;
	
	/**
	 * Builds an address request message.
	 */
	public ThickNodeAddressMessage() {
		super();
	}

	/**
	 * Builds an address reply message.
	 */
	public ThickNodeAddressMessage(InetSocketAddress address) {
		super(address);
	}
	
}
