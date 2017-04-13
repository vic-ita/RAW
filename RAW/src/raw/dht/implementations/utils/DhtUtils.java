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
package raw.dht.implementations.utils;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;

import raw.blockChain.BlockChainCore;
import raw.blockChain.api.Block;
import raw.blockChain.api.HashValue;
import raw.dht.DhtConstants;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.implementations.DefaultDhtHasher;

/**
 * Utils for DHT usage.
 * 
 * @author vic
 *
 */
public class DhtUtils {
	
	/**
	 * Compute logical XOR between two {@link DhtID} objects.
	 * 
	 * @param first one {@link DhtID}
	 * @param second another {@link DhtID}
	 * @return the logical XOR between <tt>first</tt> and <tt>second</tt>
	 */
	public static BigInteger xor(DhtID first, DhtID second){
		byte[] firstBytes = first.toByteArray();
		byte[] secondBytes = second.toByteArray();
		
		int len = firstBytes.length;
		
		byte[] xord = new byte[len + 1];
		xord[0] = 0; //to avoid change in BigInteger sign.
		
		for(int i = 0; i < len; i++){
			xord[i+1] = (byte) ((int)firstBytes[i] ^ (int)secondBytes[i]);
		}
		
		return new BigInteger(xord);
	}
	
	/**
	 * Generate a String containing only "0"s and "1"s given a {@link DhtID}.
	 * 
	 * @param id the {@link DhtID} to be converted
	 * @return a {@link String} with a binary representation of <tt>id</tt>
	 */
	public static String convertIdToBinaryString(DhtID id){
		byte[] bytes = id.toByteArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String value = Integer.toBinaryString(bytes[i] & 0xFF); // & 0xFF for the sign.
			value = StringUtils.leftPad(value, 8, '0');
			sb.append(value);
		}
		return sb.toString();
	}
	
	/**
	 * Checks if a certain block identified 
	 * by its number may be considered
	 * (for DHT purposes) a seed block.
	 * 
	 * @param blockNumber a block number
	 * @return <code>true</code> if the block identified by <code>blockNumber</code> is a seed block
	 */
	public static boolean isSeedBlockNumber(long blockNumber){
		return (blockNumber % DhtConstants.SEED_BLOCK_NUMBER_MODULE == 0);
	}
	
	/**
	 * Identify the first DHT-acceptable block number
	 * when providing the last available blockchain block number.
	 * 
	 * @param lastAvailableBlockNumbler a block number
	 * @return the first acceptable block number starting from <code>lastAvailableBlockNumbler</code>
	 */
	public static long getLastBlockNumber(long lastAvailableBlockNumbler){
		if(lastAvailableBlockNumbler <= DhtConstants.MINIMUM_ACCEPTABLE_CHAIN_DEPTH + 1){
			return 1;
		}
		return lastAvailableBlockNumbler - DhtConstants.MINIMUM_ACCEPTABLE_CHAIN_DEPTH;
	}
	
	/**
	 * Starting from a provided {@link Block} number
	 * this utility method find the first
	 * seed {@link Block} number.
	 * 
	 * @param startingPoint a {@link Block} number
	 * @return the first valid seed {@link Block} number
	 */
	public static long findFirstSeedNumberFromBlockNumber(long startingPoint){
		while(!(DhtUtils.isSeedBlockNumber(startingPoint))){
			startingPoint -= 1;
		}
		return startingPoint;
	}
	
	/**
	 * Starting from a provided {@link Block} number (excluded)
	 * this utility method finds the seed block number
	 * "eta epochs" in the past.
	 * 
	 * @param startingPoint a {@link Block} number
	 * @return the seed {@link Block} number eta blocks away
	 */
	public static long decreaseBlockNumberByEtaSeeds(long startingPoint){
		long returnable = startingPoint;
		int seedsPassed = 0;
		boolean found = false;
		while(!found){
			returnable -= 1;
			if(returnable < 0){
				returnable = 0;
				found = true;
			}
			if(isSeedBlockNumber(returnable)){
				seedsPassed += 1;
			}
			if(seedsPassed >= DhtConstants.TRANSACTION_VALIDITY_NUMBER_OF_SEEDS){
				found = true;
			}
		}
		return returnable;
	}
	
	/**
	 * Utility method building a {@link DhtHasher}
	 * based on a seed {@link Block} number
	 * 
	 * @param seedBlockNumber
	 * @param chainCore
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static DhtHasher getHasherFromSeedNumber(long seedBlockNumber, BlockChainCore chainCore) throws IllegalArgumentException{
		if(!DhtUtils.isSeedBlockNumber(seedBlockNumber)){
			throw new IllegalArgumentException("Provided block number ("+seedBlockNumber+") does not identify a SEED block.");
		}
		Block retrieved = chainCore.getBlockByNumber(seedBlockNumber);
		if(retrieved == null){
			return null;
		}
		HashValue hash = retrieved.getHeader().hash();
		return new DefaultDhtHasher(hash.toByteArray());
	}

}
