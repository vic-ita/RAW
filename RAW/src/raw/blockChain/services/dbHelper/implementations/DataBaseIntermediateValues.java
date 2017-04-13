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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * @author vic
 *
 */
@DatabaseTable(tableName = "intermediatevalues")
public class DataBaseIntermediateValues {
	
	public static final String ID_FIELD_NAME = "id";
	public static final String HEADER_ID_FIELD_NAME = "header_id";
	public static final String TRANSACTION_ID_FIELD_NAME = "transaction_id";
	
	@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
	private int id;
	@DatabaseField(columnName = HEADER_ID_FIELD_NAME)
	private int headerId;
	@DatabaseField(columnName = TRANSACTION_ID_FIELD_NAME)
	private int transactionId;
	
	public DataBaseIntermediateValues() {
		// Empty constructor for ORM
	}
	
	public DataBaseIntermediateValues(DataBaseBlockHeader header, DataBaseTransaction baseTransaction) {
		headerId = header.getId();
		transactionId = baseTransaction.getId();
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the headerId
	 */
	public int getHeaderId() {
		return headerId;
	}

	/**
	 * @return the transactionId
	 */
	public int getTransactionId() {
		return transactionId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(headerId).append(transactionId);
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DataBaseIntermediateValues){
			DataBaseIntermediateValues other = (DataBaseIntermediateValues) obj;
			return this.headerId == other.getHeaderId() && this.transactionId == other.getTransactionId();
		}
		return false;
	}

}
