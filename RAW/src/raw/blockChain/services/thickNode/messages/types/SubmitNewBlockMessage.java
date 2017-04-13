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

import raw.blockChain.api.Block;
import raw.blockChain.services.miner.Miner;
import raw.blockChain.services.thickNode.ThickNode;
import raw.blockChain.services.thickNode.messages.ThickNodeMessages;

/**
 * This message is meant to be used by {@link Miner} services to
 * notify to {@link ThickNode}s the discovery of a new {@link Block}
 * 
 * @author vic
 *
 */
public class SubmitNewBlockMessage implements ThickNodeMessages {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = -1175087181303343402L;
	
	private Block block;
	private InetSocketAddress myAddress;
	
	public SubmitNewBlockMessage(Block block, InetSocketAddress myAddress) {
		this.block = block;
		this.myAddress = myAddress;
	}
	
	public Block getBlock(){
		return block;
	}

	public InetSocketAddress getSenderAddress() {
		return myAddress;
	}

}
