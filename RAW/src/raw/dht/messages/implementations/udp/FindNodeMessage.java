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
package raw.dht.messages.implementations.udp;

import java.util.ArrayList;
import java.util.Collection;

import raw.dht.DhtConstants;
import raw.dht.DhtID;
import raw.dht.DhtNodeExtended;
import raw.dht.messages.DhtMessage;

/**
 * @author vic
 *
 */
public class FindNodeMessage implements DhtMessage {
	
	/**
	 * random generated UID.
	 */
	private static final long serialVersionUID = 3185517275369632798L;
	
	private boolean isReply;
	
	private DhtNodeExtended senderNode;
		
	private DhtID targetId;
	
	private ArrayList<DhtNodeExtended> replyNodes;
	
	/**
	 * A message requesting the set of {@link DhtNodeExtended}
	 * that are "near" to a given {@link DhtID}
	 * 
	 * @param sender the {@link DhtNodeExtended} sending this message
	 * @param targetId the searched {@link DhtID}
	 */
	public FindNodeMessage(DhtNodeExtended sender, DhtID targetId) {
		this.senderNode = sender;
		this.targetId = targetId;
		isReply = false;
	}
	
	/**
	 * A message replying to a search for a {@link DhtID}
	 * with the "nearest" (known) {@link DhtNodeExtended}
	 * 
	 * @param sender the {@link DhtNodeExtended} sending this message
	 * @param replyedNodes the {@link Collection} of {@link DhtNodeExtended} to be replied
	 * @throws IllegalArgumentException if <tt>replyedNodes</tt> size is bigger than {@link DhtConstants#ALPHA_SIZE} .
	 */
	public FindNodeMessage(DhtNodeExtended sender, DhtID targetId, Collection<DhtNodeExtended> replyedNodes) throws IllegalArgumentException {
		this.senderNode = sender;
		isReply = true;
		if(replyedNodes.size() > DhtConstants.ALPHA_SIZE){
			throw new IllegalArgumentException("Provided collection of nodes exceeds maximum allowd nodes ("+DhtConstants.ALPHA_SIZE+").");
		}
		replyNodes = new ArrayList<DhtNodeExtended>(replyedNodes);
		this.targetId = targetId;
	}

	/* (non-Javadoc)
	 * @see raw.dht.messages.DhtMessage#getMessageType()
	 */
	@Override
	public MessageType getMessageType() {
		if(isReply){
			return MessageType.FIND_NODE_REPLY;
		} else {
			return MessageType.FIND_NODE;
		}
	}

	/**
	 * @return the targetId
	 */
	public DhtID getTargetId() {
		return targetId;
	}

	/**
	 * @return the replyNodes
	 */
	public Collection<DhtNodeExtended> getReplyNodes() {
		return replyNodes;
	}

	@Override
	public DhtNodeExtended getSender() {
		return senderNode;
	}

}
