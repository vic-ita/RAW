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
package raw.blockChain.services.miner.messages.types;

import java.net.InetSocketAddress;

import raw.blockChain.services.miner.Miner;
import raw.blockChain.services.miner.messages.MinerMessages;
import raw.blockChain.services.thickNode.ThickNode;

/**
 * This message can be used by a {@link Miner} to notify
 * it's public address to {@link ThickNode}s.
 * 
 * @author vic
 *
 */
public class MinerAddressNotification implements MinerMessages {
	
	/**
	 * random generated UID 
	 */
	private static final long serialVersionUID = -5304293245051378028L;
	
	private InetSocketAddress myAddress;
	
	public MinerAddressNotification(InetSocketAddress myAddress) {
		this.myAddress = myAddress;
	}

	/**
	 * @return the myAddress
	 */
	public InetSocketAddress getAddress() {
		return myAddress;
	}

}
