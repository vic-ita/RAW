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
package raw.db;

import raw.db.providers.HSQLServerProvider;

/**
 * This enum will state all supported
 * database engines that can be used
 * by RAW.
 * 
 * @author vic
 *
 */
public enum SupportedDataBases {
	HSQLDB("HyperSQL DataBase", HSQLServerProvider.class);

	private final String dbName;
	private final Class<?> provider;
	
	private SupportedDataBases(String dbName, Class<?> provider) {
		this.dbName = dbName;
		this.provider = provider;
	}
	
	public static SupportedDataBases fromDbName(String dbName){
		for(SupportedDataBases db : SupportedDataBases.values()){
			if(db.getDbName().equals(dbName)){
				return db;
			}
		}
		return null;
	}

	/**
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @return the provider
	 */
	public Class<?> getProvider() {
		return provider;
	}
}
