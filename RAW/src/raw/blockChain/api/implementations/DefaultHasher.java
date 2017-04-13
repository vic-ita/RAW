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
package raw.blockChain.api.implementations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Hasher;
import raw.blockChain.api.Transaction;
import raw.logger.Log;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Implementation of {@link Hasher} interface.
 * 
 * @author vic
 *
 */
public class DefaultHasher implements Hasher {
	
	private HashFunction digester;
	
	private Log log;
	
	public DefaultHasher() {
		log = Log.getLogger();
		
		digester = Hashing.sha512();
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.interfaces.Hasher#hashTransaction(raw.blockChain.interfaces.Transaction)
	 */
	@Override
	public HashValue hashTransaction(Transaction transaction) {
		byte[] bytesEquivalent = null;
		try {
			bytesEquivalent = transaction.getBytes();
		} catch (IOException e) {
			log.exception(e);
		}
		return hashBytes(bytesEquivalent);
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.interfaces.Hasher#hashHashes(raw.blockChain.interfaces.HashValue[])
	 */
	@Override
	public HashValue hashHashes(HashValue... hashValues) {
		if( hashValues.length == 1 ){
			return hashBytes(hashValues[0].toByteArray());
		}
		byte[] previous = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (HashValue hashValue : hashValues) {
			if( previous == null){
				previous = hashValue.toByteArray();
			}
			else{
				baos.reset();
				try {
					baos.write(previous);
					baos.write(hashValue.toByteArray());
				} catch (IOException e) {
					log.exception(e);
				}
				previous = digestBytes(baos.toByteArray());
			}
		}
		
		return new DefaultHashValue(previous);
	}

	@Override
	public int hashLength() {
		return digester.bits() / 8;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.interfaces.Hasher#hashBlockHeader(raw.blockChain.interfaces.BlockHeader)
	 */
	@Override
	public HashValue hashBlockHeader(BlockHeader header) {
		byte[] bytesEquivalent = null;
		bytesEquivalent = header.getHashableBytes();
		
		return hashBytes(bytesEquivalent);
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.interfaces.Hasher#hashBytes(byte[])
	 */
	@Override
	public HashValue hashBytes(byte[] bytes) {
		return new DefaultHashValue(digestBytes(bytes));
	}
	
	private byte[] digestBytes(byte[] bytes){
		byte[] retVal = digester.hashBytes(bytes).asBytes();
		retVal = digester.hashBytes(retVal).asBytes();
		return retVal;
	}

}
