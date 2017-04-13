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
package raw.blockChain.services.thickNode.messages.types;

import java.net.InetSocketAddress;
import java.rmi.server.UID;

import raw.blockChain.api.Block;
import raw.blockChain.services.thickNode.ThickNode;
import raw.blockChain.services.thickNode.messages.ThickNodeMessages;

/**
 * A {@link ThickNode} will send to the other {@link ThickNode}s a
 * {@link LastBlockNotificationMessage} to signal the last {@link Block}
 * added to its block chain copy.
 * 
 * @author vic
 *
 */
public class LastBlockNotificationMessage implements ThickNodeMessages {

	/**
	 * random generated {@link UID}
	 */
	private static final long serialVersionUID = -4286692238868340491L;
	
	private InetSocketAddress thickNodeAddress;
	private Block lastBlock;
	
	public LastBlockNotificationMessage(InetSocketAddress myAddress, Block myLastBlock) {
		thickNodeAddress = myAddress;
		lastBlock = myLastBlock;
	}

	/**
	 * @return the thickNodeAddress
	 */
	public InetSocketAddress getThickNodeAddress() {
		return thickNodeAddress;
	}

	/**
	 * @return the lastBlock
	 */
	public Block getLastBlock() {
		return lastBlock;
	}

}
