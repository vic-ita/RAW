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
package raw.blockChain.services.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import raw.blockChain.services.miner.Miner;
import raw.logger.Log;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

/**
 * This is a helper class to persist the {@link Miner}s addresses
 * known to a node.
 * 
 * @author vic
 *
 */
public class MinerNodeAddressBookFile {
	
private ArrayList<InetSocketAddress> addressList;
	
	private final String ipJsonKey = "ip";
	private final String portJsonKey = "port";
	
	private Log log;

	public MinerNodeAddressBookFile(ArrayList<InetSocketAddress> addresses) {
		addressList = addresses;
		log = Log.getLogger();
	}
	
	public MinerNodeAddressBookFile() {
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);

		String fileName = properties.getBlockChainDir()	+ properties.getMinerNodesAddressesFileName()+ ".json";
		Path filePath = FileSystems.getDefault().getPath(fileName);
		Path prent = filePath.getParent();
		if (!Files.exists(prent)) {
			try {
				Files.createDirectories(prent);
			} catch (IOException e) {
				log.exception(e);
			}
		}
		if(!Files.exists(filePath)){
			defaultList();
		} else {
			loadFromFile(filePath);
		}
	}
	
	private void defaultList(){
		addressList = createDefaultList();
	}
	
	private ArrayList<InetSocketAddress> createDefaultList(){
		ArrayList<InetSocketAddress> list = new ArrayList<InetSocketAddress>();
		list.add(new InetSocketAddress("10.0.0.1", 4011)); //XXX here you can hard-code addresses for your "stable" nodes
		return list;
	}
	
	protected int defaultListSize() {
		return createDefaultList().size();
	}
	
	private void loadFromFile(Path filePath){
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filePath.toFile());
		} catch (FileNotFoundException e) {
			log.exception(e);
		}
		JsonReader reader = Json.createReader(fis);
		JsonArray addressesArray = reader.readArray();
		addressList = new ArrayList<InetSocketAddress>();
		for(int i = 0; i < addressesArray.size(); i++){
			JsonObject entry = addressesArray.getJsonObject(i);
			String ipString = entry.getString(ipJsonKey);
			InetAddress ip = null;
			try {
				ip = InetAddress.getByName(ipString);
			} catch (UnknownHostException e) {
				log.exception(e);
			}
			int port = entry.getInt(portJsonKey);
			
			InetSocketAddress address = new InetSocketAddress(ip, port);
			addressList.add(address);
		}
		reader.close();
	}

	/**
	 * @return the addressList
	 */
	public ArrayList<InetSocketAddress> getAddressList() {
		return addressList;
	}
	
	public void writeToFile(){
		if(addressList == null || addressList.size() == 0){
			return;
		}
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for(InetSocketAddress address : addressList){
			String ipString = address.getAddress().toString().substring(1);
			JsonObjectBuilder entry = Json.createObjectBuilder().add(ipJsonKey, ipString).add(portJsonKey, address.getPort());
			arrayBuilder.add(entry);
		}
		
		JsonArray addressesArray = arrayBuilder.build();
		
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		
		String fileName = properties.getBlockChainDir()+properties.getMinerNodesAddressesFileName()+".json";
		Path filePath = FileSystems.getDefault().getPath(fileName);
		Path prent = filePath.getParent();
		if(!Files.exists(prent)){
			try {
				Files.createDirectories(prent);
			} catch (IOException e) {
				log.exception(e);
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filePath.toFile());
		} catch (FileNotFoundException e) {
			log.exception(e);
		}
		Map<String, Object> props = new HashMap<String, Object>(1);
		props.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonWriterFactory writerFactory = Json.createWriterFactory(props);
		JsonWriter writer = writerFactory.createWriter(fos);
		writer.writeArray(addressesArray);
		writer.close();
	}
}
