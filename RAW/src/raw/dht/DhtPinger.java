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
package raw.dht;

import java.util.concurrent.Callable;

import raw.dht.messages.implementations.udp.PingMessage;

/**
 * This method is used to keep alive
 * this {@link DhtNode} sending {@link PingMessage}s
 * 
 * @author vic
 *
 */
public interface DhtPinger extends Callable<Void> {
	
	/**
	 * Send a {@link PingMessage} to 
	 * a given {@link DhtNodeExtended}. This method
	 * <b>SHOULD</b> also perform an insertion
	 * routine in the {@link RoutingTable} of this node
	 * if pinged node is an old one.
	 * 
	 * @param node a {@link DhtNodeExtended} to be pinged
	 * @return <code>true</code> if <code>node</code> successfuly replied to ping, <code>false</code> otherwise
	 */
	public boolean sendPing(DhtNodeExtended node);
	
	/**
	 * Send a {@link PingMessage} to 
	 * a given {@link DhtNode} in order
	 * of authenticate such a node and
	 * discover if it is "old" or not.
	 * This method should not add a node
	 * to the {@link RoutingTable}.
	 * 
	 * @param node a {@link DhtNodeExtended} to be pinged
	 * @return <code>true</code> if <code>node</code> successfuly replied to ping <b>AND</b> is old, <code>false</code> otherwise
	 */
	public boolean sendAuthenticationPing(DhtNode node);
	
	/**
	 * Halts this {@link DhtPinger}
	 *  
	 * @return <code>true</code> if successfully stopped
	 */
	public boolean stop();
	
	/**
	 * @return <code>true</code> if the node is actually stopped
	 */
	public boolean isStopped();

}
