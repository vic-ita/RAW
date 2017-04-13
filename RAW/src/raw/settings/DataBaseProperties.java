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
package raw.settings;

import javax.json.Json;
import javax.json.JsonObject;

import raw.db.SupportedDataBases;

public class DataBaseProperties implements Properties {
	
	private SupportedDataBases dbEngine;
	private String dbEngineJsonKey = "DataBase Engine";
	
	private String dbDir;
	private String dbDirJsonKey = "DataBase Directory";
	
	private String fileSeparator;
	
	private int port;
	private String portJsonKey = "DataBase port";
	
	public DataBaseProperties() {
		PropertiesManager manager = PropertiesManager.getManager();
		
		dbEngine = SupportedDataBases.HSQLDB;
		
		dbDir = manager.getGeneralBaseDir()+dbEngine+manager.getFileSeparator();
		
		fileSeparator = manager.getFileSeparator();
		
		defaultPort();
	}
	
	public DataBaseProperties(JsonObject json) {
		dbEngine = SupportedDataBases.fromDbName(json.getString(dbEngineJsonKey));
		
		dbDir = json.getString(dbDirJsonKey);
		
		boolean updatedSettings = false;
		
		try {
			port = json.getInt(portJsonKey);
		} catch (NullPointerException e) {
			defaultPort();
			updatedSettings = true;
		}
		
		if(updatedSettings){
			notifyChanged();
		}
	}

	@Override
	public JsonObject toJsonObject() {
		JsonObject json = Json.createObjectBuilder().
				add(dbEngineJsonKey, dbEngine.getDbName()).
				add(dbDirJsonKey, dbDir).
				add(portJsonKey, port).
				build();
		return json;
	}
	
	private void defaultPort(){
		port = 15000;
	}

	/**
	 * @return the dbEngine
	 */
	public SupportedDataBases getDbEngine() {
		return dbEngine;
	}

	/**
	 * @param dbEngine the dbEngine to set
	 */
	protected void setDbEngine(SupportedDataBases dbEngine) {
		this.dbEngine = dbEngine;
		notifyChanged();
	}

	/**
	 * @return the dbDir
	 */
	public String getDbDir() {
		return dbDir;
	}

	/**
	 * @param dbDir the dbDir to set
	 */
	protected void setDbDir(String dbDir) {
		this.dbDir = dbDir;
		notifyChanged();
	}

	/**
	 * @return the fileSeparator
	 */
	public String getFileSeparator() {
		return fileSeparator;
	}
	
	private void notifyChanged(){
		PropertiesManager.getManager().notifyPropertiesChanged(ModuleProperty.DATABASE);
		return;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
		notifyChanged();
	}

}
