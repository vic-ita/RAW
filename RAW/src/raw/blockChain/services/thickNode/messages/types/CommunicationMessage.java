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
import raw.blockChain.api.BlockHeader;
import raw.blockChain.services.miner.Miner;
import raw.blockChain.services.thickNode.ThickNode;
import raw.blockChain.services.thickNode.messages.ThickNodeMessages;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

/**
 * Message used as mean of communication between {@link ThickNode}s
 * and with {@link ThickNode}s (e.g.: in communications {@link Miner}<-->{@link ThickNode}).
 * 
 * @author vic
 *
 */
public class CommunicationMessage implements ThickNodeMessages {
	
	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = 2410322699908869254L;
	
	private Type message;
	private final String chainName;
	
	private Object attachment;
	private Class<?> attachmentType;
	
	public CommunicationMessage(Type message) {
		this.message = message;
		BlockChainProperties props = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		chainName = props.getBlockChainName();
	}
	
	public void attachBlock(Block block){
		attachment = block;
		attachmentType = Block.class;
	}
	
	public void attachBlockHeader(BlockHeader blockHeader){
		attachment = blockHeader;
		attachmentType = BlockHeader.class;
	}
	
	public void attachInetSocketAddress(InetSocketAddress address){
		attachment = address;
		attachmentType = InetSocketAddress.class;
	}
	
	public Type getMessage(){
		return message;
	}
	
	public Object getAttachment(){
		return attachment;
	}
	
	public Class<?> getAttachmentType(){
		return attachmentType;
	}
	
	public enum Type{
		PING_FROM_THICK,
		PING_FROM_THIN,
		PING_FROM_MINER,
		PONG,
		ACCEPTED,
		REFUSED ;
	}

	/**
	 * @return the chainName
	 */
	public String getChainName() {
		return chainName;
	}

}
