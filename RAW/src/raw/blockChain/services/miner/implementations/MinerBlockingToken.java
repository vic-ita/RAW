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
package raw.blockChain.services.miner.implementations;

import raw.blockChain.api.BlockHeader;

public class MinerBlockingToken {
	
	private BlockHeader header;
	private boolean foundLocally;
	
	public MinerBlockingToken(BlockHeader header, boolean isFoundLocally) {
		this.header = header;
		foundLocally = isFoundLocally;
	}

	/**
	 * @return the header
	 */
	public BlockHeader getHeader() {
		return header;
	}

	/**
	 * @return <tt>true</tt> id {@link MinerBlockingToken#getHeader()} is considered to have been found locally
	 */
	public boolean isFoundLocally() {
		return foundLocally;
	}

}
