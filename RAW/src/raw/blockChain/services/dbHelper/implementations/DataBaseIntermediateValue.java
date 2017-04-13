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
package raw.blockChain.services.dbHelper.implementations;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.blockChain.api.Merkler.IntermediateValue;
import raw.blockChain.api.implementations.DefaultHashValue;
import raw.blockChain.api.implementations.DefaultIntermediateValue;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="intermediatevalue")
public class DataBaseIntermediateValue {
	
	public static final String ID_FIELD_NAME = "id";
	public static final String HASH_VALUE_FIELD_NAME = "hash";
	public static final String IS_LEFT_PAIRED_FIELD_NAME = "leftPaired";
	public static final String POSITION_IN_STACK_FIELD_NAME = "position";
	public static final String INTERMEDIATE_VALUES_COLLECTION_ID_FIELD_NAME = "intValuesId";
	
	@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
	private int id;
	@DatabaseField(columnName=HASH_VALUE_FIELD_NAME)
	private String hashValue;
	@DatabaseField(columnName=IS_LEFT_PAIRED_FIELD_NAME)
	private boolean leftPaired;
	@DatabaseField(columnName=POSITION_IN_STACK_FIELD_NAME)
	private int positionInStack;
	@DatabaseField(columnName=INTERMEDIATE_VALUES_COLLECTION_ID_FIELD_NAME)
	private int intermediateValuesId;
	
	public DataBaseIntermediateValue() {
		// Empty constructor for ORM 
	}
	
	public DataBaseIntermediateValue(IntermediateValue intermediateValue, int positionInStack, DataBaseIntermediateValues collection) {
		hashValue = intermediateValue.getValue().toHexString();
		leftPaired = intermediateValue.leftPaired();
		this.positionInStack = positionInStack;
		intermediateValuesId = collection.getId();
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the hashValue
	 */
	public String getHashValue() {
		return hashValue;
	}

	/**
	 * @return the leftPaired
	 */
	public boolean isLeftPaired() {
		return leftPaired;
	}

	/**
	 * @return the intermediateValuesId
	 */
	public int getIntermediateValuesId() {
		return intermediateValuesId;
	}
	
	public IntermediateValue getPlainIntermediateValue(){
		DefaultIntermediateValue intValue = new DefaultIntermediateValue(new DefaultHashValue(hashValue), leftPaired);
		return intValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(hashValue)
		.append(leftPaired)
		.append(positionInStack)
		.append(intermediateValuesId);
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DataBaseIntermediateValue){
			DataBaseIntermediateValue other = (DataBaseIntermediateValue) obj; 
			boolean result = this.getPlainIntermediateValue().equals(((DataBaseIntermediateValue) obj).getPlainIntermediateValue());
			result = result && (positionInStack == other.getPositionInStack());
			result = result && (intermediateValuesId == other.getIntermediateValuesId());
			return result;
		}
		return false;
	}

	/**
	 * @return the positionInStack
	 */
	public int getPositionInStack() {
		return positionInStack;
	}

}
