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
package raw.blockChain.api;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

import raw.blockChain.services.miner.Miner;

/**
 * Abstraction of the header for a given {@link Block}.
 * 
 * @author vic
 *
 */
public interface BlockHeader extends Serializable {
	
	/**
	 * @return an integer representation of the version of this block
	 */
	public int getVersion();
	
	/**
	 * @return the name of the BlockChain to whom this {@link BlockHeader} belongs
	 */
	public String getBlockChainName();
	
	/**
	 * @return the progressive number of this block. Should be {@link BlockHeader#previousBlock()} block number + 1
	 */
	public long getBlockNumber();
	
	/**
	 * This is a convenience method to get the hashed value
	 * of this {@link BlockHeader}
	 * 
	 * @return the hashed value of this header
	 */
	public HashValue hash();
	
	/**
	 * @return the hashed value of previous block
	 */
	public HashValue previousBlock();
	
	/**
	 * @return the {@link HashValue} of the merkle root for the {@link Transaction}s contained
	 * in parent {@link Block}
	 */
	public HashValue merkleRoot();
	
	/**
	 * @return numeber of milliseconds since 1970-01-01T00:00 UTC
	 */
	public long timestamp();
	
	/**
	 * @return integer representation of target difficulty
	 */
	public BigDecimal currentDifficulty();
	
	/**
	 * @return random value allowing this header to match target in its hash
	 */
	public int nonce();
	
	/**
	 * @return a "signature string" of the {@link Miner} who found this node. This "signature" should not exceed 50 chars of length. 
	 */
	public String getMinerSignature();
	
	/**
	 * @return a byte array representation of this {@link BlockHeader}
	 * @throws IOException if a problem arises while serializing this object
	 */
	public byte[] getBytes() throws IOException;
	
	/**
	 * @return the byte representation of the data contained in this {@link BlockHeader} that will hash up to the value returned by {@link BlockHeader#hash()}
	 */
	public byte[] getHashableBytes();

}
