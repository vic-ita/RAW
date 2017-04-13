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

import raw.logger.Level;

public class LoggerProperties implements Properties {
	
	private String logDir;
	private String logDirJsonKey = "Logger Directory";
	
	private String fileLogLevel;
	private String fileLogLevelJsonKey = "Logging Level (file)";
	
	private String consoleLogLevel;
	private String consoleLogLevelJsonKey = "Logging Level (console)";
	
	private String fileLogType;
	private String fileLogTypeJsonKey = "File log type";
	
	public LoggerProperties() {
		PropertiesManager manager = PropertiesManager.getManager();
		logDir = manager.getGeneralBaseDir()+"log"+manager.getFileSeparator();
		
		fileLogLevel = Level.INFO.getValue();
		consoleLogLevel = Level.INFO.getValue();
		
		defaultFileLogType();
	}
	
	public LoggerProperties(JsonObject json) {
		logDir = json.getString(logDirJsonKey);
		
		fileLogLevel = json.getString(fileLogLevelJsonKey);
		consoleLogLevel = json.getString(consoleLogLevelJsonKey);
		
		boolean updatedSettings = false;
		try {
			fileLogType = json.getString(fileLogTypeJsonKey);
			if(!(fileLogType.equals("JSON") || fileLogType.equals("TXT"))){
				defaultFileLogType();
			}
		} catch (NullPointerException e) {
			defaultFileLogType();
			updatedSettings = true;
		}
		if(updatedSettings){
			notifyChanged();
		}
	}

	@Override
	public JsonObject toJsonObject() {
		JsonObject jsObj = Json.createObjectBuilder().
				add(logDirJsonKey, logDir).
				add(fileLogLevelJsonKey, fileLogLevel).
				add(consoleLogLevelJsonKey, consoleLogLevel).
				add(fileLogTypeJsonKey, fileLogType).build();
		return jsObj;
	}
	
	private void defaultFileLogType(){
		fileLogType = "JSON";
	}

	/**
	 * @return the logDir
	 */
	public String getLogDir() {
		return logDir;
	}

	/**
	 * @param logDir the logDir to set
	 */
	protected void setLogDir(String logDir) {
		this.logDir = logDir;
		notifyChanged();
	}

	/**
	 * @return the fileLogLevel
	 */
	public String getFileLogLevel() {
		return fileLogLevel;
	}

	/**
	 * @param fileLogLevel the fileLogLevel to set
	 */
	public void setFileLogLevel(String logLevel) {
		this.fileLogLevel = logLevel;
		notifyChanged();
	}

	/**
	 * @return the consoleLogLevel
	 */
	public String getConsoleLogLevel() {
		return consoleLogLevel;
	}

	/**
	 * @param consoleLogLevel the consoleLogLevel to set
	 */
	public void setConsoleLogLevel(String consoleLogLevel) {
		this.consoleLogLevel = consoleLogLevel;
		notifyChanged();
	}
	
	private void notifyChanged(){
		PropertiesManager.getManager().notifyPropertiesChanged(ModuleProperty.LOGGER);
		return;
	}

	/**
	 * @return the fileLogType
	 */
	public String getFileLogType() {
		return fileLogType;
	}

	/**
	 * @param fileLogType the fileLogType to set
	 */
	protected void setFileLogType(String fileLogType) {
		this.fileLogType = fileLogType;
		notifyChanged();
	}

}
