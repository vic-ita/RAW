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

import raw.blockChain.api.BlockHeader;
import raw.blockChain.services.miner.Miner;
import raw.blockChain.services.thickNode.ThickNode;
import raw.blockChain.services.thickNode.messages.ThickNodeMessages;

/**
 * A {@link ThickNode} will send a {@link LastBlockHeaderNotificationMessage}
 * to {@link Miner}s  to signal that a new block was found to continue the
 * block chain.
 * 
 * @author vic
 *
 */
public class LastBlockHeaderNotificationMessage implements ThickNodeMessages {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = 6068719971983407596L;
	
	private InetSocketAddress thickNodeAddress;
	private BlockHeader lastBlockHeader;
	
	public LastBlockHeaderNotificationMessage(InetSocketAddress myAddress, BlockHeader myLastBlockHeader) {
		thickNodeAddress = myAddress;
		lastBlockHeader = myLastBlockHeader;
	}

	/**
	 * @return the thickNodeAddress
	 */
	public InetSocketAddress getThickNodeAddress() {
		return thickNodeAddress;
	}

	/**
	 * @return the last {@link BlockHeader}
	 */
	public BlockHeader getLastBlockHeader() {
		return lastBlockHeader;
	}

}
