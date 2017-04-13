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

import raw.blockChain.api.Block;
import raw.blockChain.services.thickNode.messages.ThickNodeMessages;

/**
 * @author vic
 *
 */
public class UpdatingChainBlockRequestMessage implements ThickNodeMessages {
	
	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = 711246110766267859L;
	
	private Type messageType;
	private Block block;
	private long blockNumber;
	
	/**
	 * Use this constructor build a message
	 * closing the sequence of requests.
	 */
	public UpdatingChainBlockRequestMessage() {
		messageType = Type.DONE_BYE;
	}
	
	/**
	 * Use this constructor to build a request
	 * message.
	 * 
	 * @param blockNumber the number of the {@link Block} needed.
	 */
	public UpdatingChainBlockRequestMessage(long blockNumber) {
		this.blockNumber = blockNumber;
		messageType = Type.BLOCK_REQUEST;
	}
	
	/**
	 * Use this constructor to build a reply
	 * message.
	 * 
	 * @param block the requested {@link Block}
	 */
	public UpdatingChainBlockRequestMessage(Block block) {
		this.block = block;
		messageType = Type.BLOCK_REPLY;
	}
	
	/**
	 * @return the messageType
	 */
	public Type getMessageType() {
		return messageType;
	}

	/**
	 * @return the block
	 */
	public Block getBlock() {
		return block;
	}

	/**
	 * @return the blockNumber
	 */
	public long getBlockNumber() {
		return blockNumber;
	}

	public enum Type {
		BLOCK_REQUEST,
		BLOCK_REPLY,
		DONE_BYE;
	}
}
