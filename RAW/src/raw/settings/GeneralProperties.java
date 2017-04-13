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

public class GeneralProperties implements Properties {
	
	private String loggerPropertiesFileName;
	private String loggerPropertiesFileJsonKey = "Logger Settings File Path";
	
	private String blockChainPropertiesFileName;
	private String blockChainPropertiesFileJsonKey = "BlockChain Settings File Path";
	
	private String dbPropertiesFileName;
	private String dbPropertiesFileNameJsonKey= "DataBase Settings File Path";
	
	private String dhtPropertiesFileName;
	private String dhtPropertiesFileNameJsonKey= "DHT Settings File Path";
	
	private boolean dhtIsOff;
	private String dhtIsOffJsonKey = "DHT is OFF";

	public GeneralProperties() {
		PropertiesManager manager = PropertiesManager.getManager();
		loggerPropertiesFileName = manager.getGeneralBaseDir()+"logSettings.json";
		
		blockChainPropertiesFileName = manager.getGeneralBaseDir()+"blockChainSettings.json";
		
		dbPropertiesFileName = manager.getGeneralBaseDir()+"dbSettings.json";
		
		dhtPropertiesFileName = manager.getGeneralBaseDir()+"dhtSettings.json";
		
		defaultDhtIsOff();
	}
	
	public GeneralProperties(JsonObject json) {
		loggerPropertiesFileName = json.getString(loggerPropertiesFileJsonKey);
		
		blockChainPropertiesFileName = json.getString(blockChainPropertiesFileJsonKey);
		
		dbPropertiesFileName = json.getString(dbPropertiesFileNameJsonKey);
		
		dhtPropertiesFileName = json.getString(dhtPropertiesFileNameJsonKey);
		
		try {
			dhtIsOff = json.getBoolean(dhtIsOffJsonKey);			
		} catch (Exception e) {
			defaultDhtIsOff();
			notifyChanged();
		}
	}
	
	public JsonObject toJsonObject() {
		JsonObject jsObj = Json.createObjectBuilder().
				add(loggerPropertiesFileJsonKey, loggerPropertiesFileName).
				add(blockChainPropertiesFileJsonKey, blockChainPropertiesFileName).
				add(dbPropertiesFileNameJsonKey, dbPropertiesFileName).
				add(dhtPropertiesFileNameJsonKey, dhtPropertiesFileName).
				add(dhtIsOffJsonKey, dhtIsOff).build();
		return jsObj;
	}

	/**
	 * @return the loggerPropertiesFileName
	 */
	public String getLoggerPropertiesFileName() {
		return loggerPropertiesFileName;
	}

	/**
	 * @param loggerPropertiesFileName the loggerPropertiesFileName to set
	 */
	protected void setLoggerPropertiesFile(String loggerPropertiesFile) {
		this.loggerPropertiesFileName = loggerPropertiesFile;
		notifyChanged();
	}

	/**
	 * @return the blockChainPropertiesFileName
	 */
	public String getBlockChainPropertiesFileName() {
		return blockChainPropertiesFileName;
	}

	/**
	 * @param blockChainPropertiesFileName the blockChainPropertiesFileName to set
	 */
	protected void setBlockChainPropertiesFile(String blockChainPropertiesFile) {
		this.blockChainPropertiesFileName = blockChainPropertiesFile;
		notifyChanged();
	}

	/**
	 * @return the dbPropertiesFileName
	 */
	public String getDbPropertiesFileName() {
		return dbPropertiesFileName;
	}

	/**
	 * @param dbPropertiesFileName the dbPropertiesFileName to set
	 */
	protected void setDbPropertiesFileName(String dbPropertiesFileName) {
		this.dbPropertiesFileName = dbPropertiesFileName;
		notifyChanged();
	}
	
	/**
	 * @return the dhtPropertiesFileName
	 */
	public String getDhtPropertiesFileName() {
		return dhtPropertiesFileName;
	}

	/**
	 * @param dhtPropertiesFileName the dhtPropertiesFileName to set
	 */
	protected void setDhtPropertiesFileName(String dhtPropertiesFileName) {
		this.dhtPropertiesFileName = dhtPropertiesFileName;
		notifyChanged();
	}

	private void notifyChanged(){
		PropertiesManager.getManager().notifyPropertiesChanged(ModuleProperty.GENERAL);
		return;
	}
	
	private void defaultDhtIsOff(){
		dhtIsOff = false;
	}

	/**
	 * @return the dhtIsOff
	 */
	public boolean isDhtIsOff() {
		return dhtIsOff;
	}

	/**
	 * @param dhtIsOff the dhtIsOff to set
	 */
	protected void setDhtIsOff(boolean dhtIsOff) {
		this.dhtIsOff = dhtIsOff;
	}

}
