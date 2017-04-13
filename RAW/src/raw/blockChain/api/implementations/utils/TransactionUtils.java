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
package raw.blockChain.api.implementations.utils;

import java.util.Arrays;

import raw.blockChain.BlockChainCore;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultHashValue;
import raw.blockChain.api.implementations.DefaultHasher;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.implementations.utils.DhtUtils;
import raw.logger.Log;

public class TransactionUtils {
	
	/**
	 * This method checks if a {@link Transaction}
	 * is valid within a certain seed epoch. A reference
	 * to a {@link BlockChainCore} is needed to handle 
	 * the blockchain information retrieval. 
	 * I.E.: the nonce returned by {@link Transaction#getTransactionNonce()}
	 * hashed with {@link Transaction#getDhtID()} and the seed 
	 * epoch's hasher identified by {@link Transaction#getCreationSeedNumber()}
	 * outputs a valid value.
	 * 
	 * @param transaction the {@link Transaction} to be evaluated
	 * @param chainCore a {@link BlockChainCore} reference
	 * @return <code>true</code> if the {@link Transaction} is valid
	 */
	public static boolean isValid(Transaction transaction, BlockChainCore chainCore){
		long seedNumber = transaction.getCreationSeedNumber();
		DhtHasher hasher = null;
		try {
			hasher = DhtUtils.getHasherFromSeedNumber(seedNumber, chainCore);			
		} catch (IllegalArgumentException e) {
			Log.getLogger().debug("Block number does not identify a seed block.");
			return false;
		}
		return isValid(transaction, hasher);
	}
	
	/**
	 * As {@link TransactionUtils#isValid(Transaction, BlockChainCore)} but,
	 * instead of requesting a reference to a {@link BlockChainCore} to reconstruct
	 * a valid {@link DhtHasher}, an explicit hasher instance is required.
	 * 
	 * @param transaction the {@link Transaction} to be evaluated
	 * @param hasher a {@link DhtHasher} instance
	 * @return <code>true</code> if the {@link Transaction} is valid
	 */
	public static boolean isValid(Transaction transaction, DhtHasher hasher){
		if(transaction == null){
			return false;
		}
		return isValid(transaction.getDhtID(), transaction.getTransactionNonce(), hasher);
	}
	
	public static boolean isValid(DhtID id, long nonce, DhtHasher hasher){
		DhtID hashed = hasher.hashDhtIDwithLongNonce(id, nonce);
		HashValue converted = new DefaultHashValue(hashed.toByteArray());
		HashValue target = getTarget();
		HashValue masked = converted.maskWith(target);
		return converted.equals(masked);
	}
	
	private static byte[] fixedTargetBytes(){
		byte[] target = new byte[(new DefaultHasher()).hashLength()];
		Arrays.fill(target, (byte) 0xff);
		target[0] = (byte) 0x00;
		target[1] = (byte) 0x00;
		target[2] = (byte) 0x00;
		return target;
	}
	
	/**
	 * @return an {@link HashValue} target for the {@link Transaction}'s proof of work
	 */
	public static HashValue getTarget(){
		return new DefaultHashValue(fixedTargetBytes());
	}

}
