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

import java.io.Serializable;
import java.util.Collections;
import java.util.Stack;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.blockChain.api.Merkler;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.Merkler.IntermediateValue;
import raw.blockChain.api.Merkler.IntermediateValues;

/**
 * @author vic
 *
 */
public class DefaultIntermediateValuesBuilder implements Serializable{
	/**
	 * random generated UID 
	 */
	private static final long serialVersionUID = 4858552322690003362L;
	
	private Transaction trans;
	private long number;
	private Stack<IntermediateValue> tmpStack;
	
	public DefaultIntermediateValuesBuilder(Transaction baseTransaction, long blockNumber) {
		this.trans = baseTransaction;
		this.number = blockNumber;
		tmpStack = new Stack<Merkler.IntermediateValue>();
	}
	
	public DefaultIntermediateValuesBuilder pushNextHash(IntermediateValue nextValue){
		tmpStack.push(nextValue);
		return this;
	}
	
	public IntermediateValues build(){
		DefaultIntermediateValues built = new DefaultIntermediateValues();
		built.baseTransaction = trans;
		built.blockNumber = number;
		Collections.reverse(tmpStack);
		built.intermediateValues = tmpStack;
		return built;
	}
	
private class DefaultIntermediateValues implements IntermediateValues{
		
		/**
		 * generated serial version UID 
		 */
		private static final long serialVersionUID = -5935040681519905381L;
		
		private Transaction baseTransaction;
		private long blockNumber;
		private Stack<IntermediateValue> intermediateValues;
		

		@Override
		public Transaction getBaseTransaction() {
			return baseTransaction;
		}

		@Override
		public IntermediateValue popNextHash() {
			if(intermediateValues.isEmpty()){
				return null;
			}
			return intermediateValues.pop();
		}
		
		protected Stack<IntermediateValue> getStack(){
			return intermediateValues;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			HashCodeBuilder builder = new HashCodeBuilder();
			builder.append(baseTransaction).append(intermediateValues);
			return builder.toHashCode();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof DefaultIntermediateValues)){
				return false;
			}
			DefaultIntermediateValues other = (DefaultIntermediateValues) obj;
			if(getBaseTransaction().equals(other.getBaseTransaction())){
				return false;
			}
			boolean stacksEquals = true;
			Stack<IntermediateValue> mystack = getStack();
			Stack<IntermediateValue> otherStack = other.getStack();
			while(stacksEquals){
				if(mystack.isEmpty() && otherStack.isEmpty()){
					break;
				}
				if((mystack.isEmpty() && !otherStack.isEmpty())|| (!mystack.isEmpty() && otherStack.isEmpty())){
					//different lengths
					stacksEquals = false;
				}
				IntermediateValue mine = mystack.pop();
				IntermediateValue his = otherStack.pop();
				if(!mine.equals(his)){
					stacksEquals = false;
				}
			}
			return stacksEquals;
		}

		/* (non-Javadoc)
		 * @see raw.blockChain.api.Merkler.IntermediateValues#getBlockNumber()
		 */
		@Override
		public long getBlockNumber() {
			return blockNumber;
		}
	}
}
