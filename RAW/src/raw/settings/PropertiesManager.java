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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

/**
 * This class is meant to retrieve 
 * the various settings to be used
 * by this application.
 * 
 * @author vic
 *
 */
public class PropertiesManager {
	
	private static PropertiesManager thePropertiesManager = null;
	
	private String homeDir;
	private String fileSeparator;
	
	private String generalBaseDir;
	
	private static GeneralProperties general = null;
	private static BlockChainProperties blockChain = null;	
	private static LoggerProperties logger = null;
	private static DataBaseProperties db = null;
	private static DhtProperties dht = null;
	
	private ModuleProperty changedProperties = null;
	
	/**
	 * Instantiate an object which can be used to access
	 * and retrieve properties for the various raw modules.
	 * The base directory is the default one. 
	 */
	private PropertiesManager() {
		this(null);
	}
	
	/**
	 * Instantiate an object which can be used to access
	 * and retrieve properties for the various raw modules.
	 * This will set up a custom base directory.
	 * 
	 *  @param dir the base directory for raw.
	 */
	private PropertiesManager(String dir) {
		homeDir = System.getProperty("user.home");
		fileSeparator = System.getProperty("file.separator"); 
		
		if(dir == null){
			generalBaseDir = homeDir+fileSeparator+"RAW_distributor"+fileSeparator;
		} else {
			if(dir.lastIndexOf(fileSeparator) != dir.length()-1){
				dir = dir+fileSeparator;
			}
			generalBaseDir = dir;
		}
		if(!Files.exists(FileSystems.getDefault().getPath(generalBaseDir))){
			try {
				Files.createDirectories(FileSystems.getDefault().getPath(generalBaseDir));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * {@link PropertiesManager} can be used to access
	 * and retrieve properties for the various raw modules.
	 * This object is intended as a singleton.
	 * 
	 * @return the {@link PropertiesManager}
	 */
	public static PropertiesManager getManager(){
		if(thePropertiesManager == null){
			thePropertiesManager = new PropertiesManager();
		}
		return thePropertiesManager;
	}
	
	
	/**
	 * {@link PropertiesManager} can be used to access
	 * and retrieve properties for the various raw modules.
	 * This object is intended as a singleton. Calling this method
	 * will attempt to retrieve a {@link PropertiesManager} whose
	 * base directory is <code>baseDirectory</code>. If this
	 * method calling causes the instantiation of {@link PropertiesManager}
	 * singleton then the base directory will be set, otherwise
	 * this will only return the already set-up {@link PropertiesManager}
	 * (without setting <code>baseDirectory</code>).
	 * 
	 * @param baseDirectory the proposed raw base directory
	 * 
	 * @return the {@link PropertiesManager}
	 */
	public static PropertiesManager getManager(String baseDirectory){
		if(thePropertiesManager == null){
			thePropertiesManager = new PropertiesManager(baseDirectory);
		}
		return thePropertiesManager;
	}
	
	/**
	 * Get a {@link Properties} object containing the settings for
	 * a given raw module.
	 * 
	 * @param module specifies the module for which the settings will be retrieved
	 * @return a {@link Properties} object
	 */
	public Properties getProperties(ModuleProperty module){
		if(!isThereAPropertyFile(module)){
			generateDefaults(module);
		}
		else{
			if(needsToBeLoadedFromFile(module)){
				loadFromFile(module);				
			}
		}
		return returnAppropriate(module);
	}
	
	private String getPropertyFileName(ModuleProperty module){
		String jsonFileName = null;
		if(module == ModuleProperty.GENERAL){
			jsonFileName = generalBaseDir+"generalSettings.json";
		}
		else{
			GeneralProperties gen = (GeneralProperties) getProperties(ModuleProperty.GENERAL);
			switch (module) {
			case BLOCK_CHAIN:
				jsonFileName = gen.getBlockChainPropertiesFileName();
				break;
			case LOGGER:
				jsonFileName = gen.getLoggerPropertiesFileName();
				break;
			case DATABASE:
				jsonFileName = gen.getDbPropertiesFileName();
				break;
			case DHT:
				jsonFileName = gen.getDhtPropertiesFileName();
				break;
			default:
				break;
			}
		}
		return jsonFileName;
	}
	
	private boolean isThereAPropertyFile(ModuleProperty module){
		String fileName = getPropertyFileName(module);
		return Files.exists(FileSystems.getDefault().getPath(fileName));		
	}
	
	private void generateDefaults(ModuleProperty module){
		switch (module) {
		case GENERAL:
			general = new GeneralProperties();
			writePropertyFile(general, getPropertyFileName(module));
			break;
		case BLOCK_CHAIN:
			blockChain = new BlockChainProperties();
			writePropertyFile(blockChain, getPropertyFileName(module));
			break;
		case LOGGER:
			logger = new LoggerProperties();
			writePropertyFile(logger, getPropertyFileName(module));
			break;
		case DATABASE:
			db = new DataBaseProperties();
			writePropertyFile(db, getPropertyFileName(module));
			break;
		case DHT:
			dht = new DhtProperties();
			writePropertyFile(dht, getPropertyFileName(module));
			break;
		default:
			break;
		}
	}
	
	private void loadFromFile(ModuleProperty module){
		String fileName = getPropertyFileName(module);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		JsonReader reader = Json.createReader(fis);
		JsonObject jsonObject = reader.readObject();
		reader.close();
		switch (module) {
		case GENERAL:
			general = new GeneralProperties(jsonObject);
			break;
		case BLOCK_CHAIN:
			blockChain = new BlockChainProperties(jsonObject);
			break;
		case LOGGER:
			logger = new LoggerProperties(jsonObject);
			break;
		case DATABASE:
			db = new DataBaseProperties(jsonObject);
			break;
		case DHT:
			dht = new DhtProperties(jsonObject);
			break;
		default:
			break;
		}
		checkForChanges();
	}
	
	private Properties returnAppropriate(ModuleProperty module){
		switch (module) {
		case GENERAL:
			return general;
		case BLOCK_CHAIN:
			return blockChain;
		case LOGGER:
			return logger;
		case DATABASE:
			return db;
		case DHT:
			return dht;
		default:
			return null;
		}
	}
	
	private boolean needsToBeLoadedFromFile(ModuleProperty module){
		switch (module) {
		case GENERAL:
			return (general == null);
		case BLOCK_CHAIN:
			return (blockChain == null);
		case LOGGER:
			return (logger == null);
		case DATABASE:
			return (db == null);
		case DHT:
			return (dht == null);
		default:
			break;
		}
		return false;
	}
	
	private void writePropertyFile(Properties property, String fileName){
		JsonObject jsonObject = property.toJsonObject();
		Path path = FileSystems.getDefault().getPath(fileName);
		Path parent = path.getParent();
		if(!Files.exists(parent)){
			try {
				Files.createDirectories(parent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path.toFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Map<String, Object> props = new HashMap<String, Object>(1);
		props.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonWriterFactory writerFactory = Json.createWriterFactory(props);
		JsonWriter writer = writerFactory.createWriter(fos);
		writer.writeObject(jsonObject);
		writer.close();
	}

	/**
	 * @return the generalBaseDir <b>INCLUDING</b> trailing file separator.
	 */
	protected String getGeneralBaseDir() {
		return generalBaseDir;
	}

	/**
	 * @return the fileSeparator
	 */
	protected String getFileSeparator() {
		return fileSeparator;
	}
	
	/**
	 * This method is intended as a mean of signaling that a 
	 * module settings has been programmatically changed and need to
	 * be written down to the file.
	 *  
	 * @param module the module whose properties has changed
	 */
	protected void notifyPropertiesChanged(ModuleProperty module){
		changedProperties = module;
		checkForChanges();
	}
	
	/**
	 * To persist what has been signaled by {@link PropertiesManager#notifyPropertiesChanged(ModuleProperty)}
	 */
	private void checkForChanges(){
		if(changedProperties != null){
			switch (changedProperties) {
			case GENERAL:
				writePropertyFile(general, getPropertyFileName(changedProperties));
				break;
			case BLOCK_CHAIN:
				writePropertyFile(blockChain, getPropertyFileName(changedProperties));
				break;
			case DATABASE:
				writePropertyFile(db, getPropertyFileName(changedProperties));
				break;
			case LOGGER:
				writePropertyFile(logger, getPropertyFileName(changedProperties));
				break;
			case DHT:
				writePropertyFile(dht, getPropertyFileName(changedProperties));
				break;
			default:
				//no changes at all. no action required.
				break;
			}
			changedProperties = null;
		}
	}
	
}
