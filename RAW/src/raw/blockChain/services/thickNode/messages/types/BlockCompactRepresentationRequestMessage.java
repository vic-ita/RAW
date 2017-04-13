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
import raw.blockChain.api.BlockCompactRepresentation;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Transaction;
import raw.blockChain.services.thickNode.ThickNode;
import raw.blockChain.services.thickNode.messages.ThickNodeMessages;
import raw.blockChain.services.thinNode.ThinNode;

/**
 * This message is meant to be used by {@link ThinNode}s to request {@link BlockCompactRepresentation}s
 * and by {@link ThickNode}s to reply to such requests.
 * 
 * @author vic
 *
 */
public class BlockCompactRepresentationRequestMessage implements ThickNodeMessages {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = -3100306097773163431L;
	
	private boolean isRequest;
	private boolean isReject;
	private RequestType typeOfRequest;
	
	private long blockNumber;
	private HashValue hash;
	private BlockHeader header;
	private Transaction transaction;
	private BlockCompactRepresentation block;
	
	
	private BlockCompactRepresentationRequestMessage(Transaction transaction) {
		isRequest = true;
		isReject = false;
		this.transaction = transaction;
	}
	
	/**
	 * Create a request for a {@link BlockCompactRepresentation} by means of
	 * its number 
	 *  
	 * @param blockNumber the {@link Block} number
	 */
	public BlockCompactRepresentationRequestMessage(long blockNumber, Transaction transaction) {
		this(transaction);
		this.blockNumber = blockNumber;
		typeOfRequest = RequestType.BY_BLOCK_NUMBER;
	}
	
	/**
	 * Create a request for a {@link BlockCompactRepresentation} by means of
	 * its {@link HashValue} header hash.
	 * 
	 * @param hash
	 */
	public BlockCompactRepresentationRequestMessage(HashValue hash, Transaction transaction) {
		this(transaction);
		this.hash = hash;
		typeOfRequest = RequestType.BY_HASH;
	}
	
	/**
	 * Create a request for a {@link BlockCompactRepresentation} by means of
	 * its {@link HashValue} header hash.
	 * 
	 * @param header
	 */
	public BlockCompactRepresentationRequestMessage(BlockHeader header, Transaction transaction) {
		this(transaction);
		this.header = header;
		typeOfRequest = RequestType.BY_HEADER;
	}
	
	/**
	 * Create a reply to a {@link BlockCompactRepresentation} request.
	 * 
	 * @param block the {@link BlockCompactRepresentation} requested
	 */
	public BlockCompactRepresentationRequestMessage(BlockCompactRepresentation block) {
		isRequest = false;
		isReject = false;
		this.block = block;
	}
	
	/**
	 * Change a message from a request one to
	 * a rejection.
	 */
	public void rejectMessage(){
		isRequest = false;
		isReject = true;
	}
	
	public boolean isRequestMessage(){
		return isRequest;
	}
	
	public boolean isPositiveReply(){
		return (!isRequest)&&(!isReject);
	}
	
	public boolean isNegativeReply(){
		return (!isRequest)&&isReject;
	}
	
	/**
	 * @return the typeOfRequest
	 */
	public RequestType getTypeOfRequest() {
		return typeOfRequest;
	}

	/**
	 * @return the blockNumber
	 */
	public long getBlockNumber() {
		return blockNumber;
	}

	/**
	 * @return the hash
	 */
	public HashValue getHash() {
		return hash;
	}

	/**
	 * @return the block
	 */
	public BlockCompactRepresentation getBlockCompactRepresentation() {
		return block;
	}

	/**
	 * @return the header
	 */
	public BlockHeader getHeader() {
		return header;
	}

	/**
	 * @return the transaction
	 */
	public Transaction getTransaction() {
		return transaction;
	}

	public enum RequestType{
		BY_BLOCK_NUMBER,
		BY_HASH,
		BY_HEADER;
	}

}
