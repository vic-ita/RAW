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


/**
 * Utility class to compute hashes.
 * The hashes for the BlockChain are 
 * obtained by a double application of SHA-256
 * (like in BitCoin).
 *  
 * @author vic
 *
 */
public interface Hasher {
	
	/**
	 * Digest an {@link Transaction} hashing it in a {@link HashValue}.
	 * 
	 * @param transaction the input {@link Transaction}
	 * @return an {@link HashValue} obtained from {@link Transaction}
	 */
	public HashValue hashTransaction(Transaction transaction);
	
	/**
	 * Hashes (or re-hashes) one or more {@link HashValue}s.
	 * 
	 * @param hashValues one or more input {@link HashValue}
	 * @return the digested {@link HashValue}
	 */
	public HashValue hashHashes(HashValue ... hashValues);
	
	/**
	 * Hashes an header represented ad {@link BlockHeader}.
	 * 
	 * @param header the input {@link BlockHeader}
	 * @return the digested {@link HashValue}
	 */
	public HashValue hashBlockHeader(BlockHeader header);
	
	/**
	 * Hashes a byte array.
	 * 
	 * @param bytes the input byte array
	 * @return the digested {@link HashValue}
	 */
	public HashValue hashBytes(byte[] bytes);
	
	/**
	 * @return the number of bytes of an hash generated by {@link Hasher}
	 */
	public int hashLength();

}
