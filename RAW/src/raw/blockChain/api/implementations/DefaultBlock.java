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

import java.util.ArrayList;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.blockChain.api.Block;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.Transaction;

/**
 * Implementation of {@link Block} interface.
 * 
 * @author vic
 *
 */
public class DefaultBlock implements Block {

	/**
	 * random generated serial UID
	 */
	private static final long serialVersionUID = -993361530552542067L;
	
	private BlockHeader myHeader;
	
	private ArrayList<Transaction> transactionsList;
	
	public DefaultBlock(BlockHeader header, ArrayList<Transaction> transactionList) {
		this.myHeader = header;
		this.transactionsList = transactionList;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.interfaces.Block#getVersion()
	 */
	@Override
	public int getVersion() {
		return myHeader.getVersion();
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.interfaces.Block#getHeader()
	 */
	@Override
	public BlockHeader getHeader() {
		return myHeader;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.interfaces.Block#getTransactions()
	 */
	@Override
	public ArrayList<Transaction> getTransactions() {
		return transactionsList;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(getHeader()).
		append(getTransactions());
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Block)){
			return false;
		}
		Block other = (Block) obj;
		return getHeader().equals(other.getHeader());
	}

}
