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

import raw.blockChain.api.HashValue;
import raw.blockChain.api.Merkler.IntermediateValue;

/**
 * @author vic
 *
 */
public class DefaultIntermediateValue implements IntermediateValue {

	/**
	 * generated serial version UID 
	 */
	private static final long serialVersionUID = 5823810308072039651L;
	
	private HashValue value;
	private boolean leftPaired;
	
	public DefaultIntermediateValue(HashValue value, boolean isLeftPaired) {
		this.value = value;
		this.leftPaired = isLeftPaired;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(value).append(leftPaired);
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof IntermediateValue)){
			return false;
		}
		IntermediateValue other = (IntermediateValue) obj;
		boolean equals = true;
		equals = equals && getValue().equals(other.getValue());
		equals = equals && ((leftPaired() && other.leftPaired()) || (!leftPaired() && !other.leftPaired()) );
		return equals;
	}
	
	/* (non-Javadoc)
	 * @see raw.blockChain.api.Merkler.IntermediateValue#getValue()
	 */
	@Override
	public HashValue getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.api.Merkler.IntermediateValue#leftPaired()
	 */
	@Override
	public boolean leftPaired() {
		return leftPaired;
	}

}
