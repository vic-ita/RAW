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
package raw.blockChain.services.miner;

import raw.blockChain.api.BlockHeader;
import raw.blockChain.services.thickNode.ThickNode;

public interface LocalThickNodeListener {
	
	/**
	 * {@link ThickNode}s will call this method to inform a local
	 * {@link Miner} that a new block was found by the network.
	 * 
	 * @param header a new {@link BlockHeader}
	 * @return <tt>true</tt> if notification went well, <tt>false</tt> otherwise
	 */
	public boolean notifyNewBlockHeaderFromNet(BlockHeader header);

}
