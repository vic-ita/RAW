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

import raw.blockChain.services.thickNode.messages.ThickNodeMessages;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

/**
 * Use this message to ask a new address.
 * 
 * @author vic
 *
 */
public class NodeAddressMessage implements ThickNodeMessages {

	/**
	 * Random generated UID
	 */
	private static final long serialVersionUID = 253123974025987602L;
	
	private final String chainName;
	private InetSocketAddress address;
	private boolean isRequest;
	
	/**
	 * Builds an address request message.
	 */
	public NodeAddressMessage() {
		isRequest = true;
		BlockChainProperties props = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		chainName = props.getBlockChainName();
	}
	
	/**
	 * Builds an address reply message.
	 */
	public NodeAddressMessage(InetSocketAddress address) {
		this.address = address;
		isRequest = false;
		BlockChainProperties props = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		chainName = props.getBlockChainName();
	}

	/**
	 * @return the address
	 */
	public InetSocketAddress getAddress() {
		return address;
	}

	/**
	 * @return the isRequest
	 */
	public boolean isRequest() {
		return isRequest;
	}

	/**
	 * @return the chainName
	 */
	public String getChainName() {
		return chainName;
	}
	
}
