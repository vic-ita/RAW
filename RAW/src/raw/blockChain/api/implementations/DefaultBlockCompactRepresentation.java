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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.blockChain.api.BlockCompactRepresentation;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.Merkler.IntermediateValues;

/**
 * Default implementation of {@link BlockCompactRepresentation}.
 * 
 * @author vic
 *
 */
public class DefaultBlockCompactRepresentation implements BlockCompactRepresentation {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = 3008881288527062593L;
	
	private BlockHeader header;
	private Transaction transaction;
	private IntermediateValues intermediateValues;
	
	public DefaultBlockCompactRepresentation(BlockHeader header, Transaction transaction, IntermediateValues intermediateHashes) {
		this.header = header;
		this.transaction = transaction;
		intermediateValues = intermediateHashes;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.BlockCompactRepresentation#getHeader()
	 */
	@Override
	public BlockHeader getHeader() {
		return header;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.BlockCompactRepresentation#getTransaction()
	 */
	@Override
	public Transaction getTransaction() {
		return transaction;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.BlockCompactRepresentation#getIntermediateValues()
	 */
	@Override
	public IntermediateValues getIntermediateValues() {
		return intermediateValues;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(header).append(transaction).append(intermediateValues);
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof BlockCompactRepresentation)){
			return false;
		}
		BlockCompactRepresentation other = (BlockCompactRepresentation) obj;
		boolean equals = header.equals(other.getHeader()) && transaction.equals(other.getTransaction()) && intermediateValues.equals(other.getIntermediateValues());
		return equals;
	}

}
