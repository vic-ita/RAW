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
package raw.dht.implementations.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

import raw.blockChain.api.implementations.DefaultTransaction;
import raw.dht.DhtAddress;
import raw.dht.DhtConstants;
import raw.dht.DhtID;
import raw.dht.DhtNodeExtended;
import raw.dht.implementations.DefaultDhtAddress;
import raw.dht.implementations.DefaultDhtCore;
import raw.dht.implementations.DefaultDhtID;
import raw.dht.implementations.DefaultDhtNodeExtended;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.utils.nodesInfoServer.NodesInfoRequestMessage;
import raw.logger.Log;
import raw.settings.DhtProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

public class DhtNodeAddressBookFile {
	
	private Collection<DhtNodeExtended> addresses;
	
	private final String dhtIdJSonKey = "dht_id";
	private final String dhtInetAddresJSonKey = "ip";
	private final String udpPortJSonKey = "udp";
	private final String tcpPortJSonKey = "tcp";
	private final String pubKeyJSonKey = "publicKeyHex";
	private final String transactionNonceJSonKey = "transactionNonce";
	private final String transactionSeedNumberJSonKey = "transactionSeedBlock";
	private final String transactionBlockNumberJSonKey = "transactionBlockNumber";
	
	
	private Log log;
	
	public DhtNodeAddressBookFile() {
		this(true);
	}
	
	public DhtNodeAddressBookFile(Collection<DhtNodeExtended> nodes) {
		addresses = nodes;
		log = Log.getLogger();
	}
	
	public DhtNodeAddressBookFile(boolean retrieveNodesFromInfoServers) {
		log = Log.getLogger();
		addresses = loadFromFile();
		if(retrieveNodesFromInfoServers && (addresses == null || addresses.size() == 0)){
			addresses = retrieveFromNodesInfoServers();
		}
	}
	
	private void createDefaultNodesInfoServerFile(File file){
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		JsonObjectBuilder entry;
		
		entry = Json.createObjectBuilder().add(dhtInetAddresJSonKey, "10.0.0.1").add(tcpPortJSonKey, 5011); //XXX here you can hard-code addresses for your "stable" nodes
		arrayBuilder.add(entry);
		
		JsonArray addressesArray = arrayBuilder.build();
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			log.exception(e);
		}
		Map<String, Object> jsonProps = new HashMap<String, Object>(1);
		jsonProps.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonWriterFactory writerFactory = Json.createWriterFactory(jsonProps);
		JsonWriter writer = writerFactory.createWriter(fos);
		writer.writeArray(addressesArray);
		writer.close();
	}
	
	public File getDefaultFile(){
		DhtProperties props = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
		String path = props.getDhtBaseDir()+props.getNodesInfoServerFileName();
		File nodesFile = new File(path);
		if(!nodesFile.exists()){
			nodesFile.getParentFile().mkdirs();
			createDefaultNodesInfoServerFile(nodesFile);
		}
		return nodesFile;
	}
	
	public Collection<DhtNodeExtended> retrieveFromNodesInfoServers(){
		File nodesFile = getDefaultFile();
		FileInputStream fis;
		try {
			fis = new FileInputStream(nodesFile);
		} catch (FileNotFoundException e) {
			return null;
		}
		JsonReader reader = Json.createReader(fis);
		JsonArray addressesArray = reader.readArray();
		ArrayList<DhtNodeExtended> retAddresses = new ArrayList<>();
		DhtProperties props = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
		int localServerPort = props.getNodesInfoServerPort();
		InetSocketAddress localAddress = new InetSocketAddress(DefaultDhtCore.getCore().getNode().getAddress().getAddress(), localServerPort); 
		for(int i = 0; i < addressesArray.size(); i++){
			JsonObject entry = addressesArray.getJsonObject(i);
			InetSocketAddress address = new InetSocketAddress(entry.getString(dhtInetAddresJSonKey), entry.getInt(tcpPortJSonKey));
//			if(DefaultDhtCore.getCore().getNode().getAddress().getAddress().equals(address.getAddress())){
			if(localAddress.equals(address.getAddress())){
				log.verboseDebug("Unreasonable to contact myself.");
				continue;
			}
			Collection<DhtNodeExtended> collected = contactNodesInfoServer(address);
			if(collected != null){
				retAddresses.addAll(collected);				
			}
		}
		if(retAddresses.size()> 0){
			HashSet<DhtNodeExtended> set = new HashSet<>(retAddresses);
			retAddresses = new ArrayList<>(set);			
		}
		
		log.verboseDebug("Got a return list ("+retAddresses+") consisting of "+retAddresses.size()+" nodes.");
		
		return retAddresses;
	}
	
	private Collection<DhtNodeExtended> contactNodesInfoServer(InetSocketAddress address){
		log.verboseDebug("Try to contact "+address);
//		Collection<DhtNode> receivedNodes = null;
		Collection<DhtNodeExtended> receivedNodes = new ArrayList<DhtNodeExtended>();
		try(Socket sock = new Socket(address.getAddress(), address.getPort())){
			sock.setSoTimeout(DhtConstants.TIMEOUT_MILLISECONDS);
			ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
			NodesInfoRequestMessage request = new NodesInfoRequestMessage(true);
			log.verboseDebug("Sending request to "+address);
			oos.writeObject(request);
			oos.flush();
			log.verboseDebug("Request sent to "+address);
//			receivedNodes = new ArrayList<DhtNode>();
			ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
			log.verboseDebug("Ready to receive contact informations.");
			Object obj = ois.readObject();
			while (true) {
				if(obj instanceof DhtNodeExtended){
					DhtNodeExtended received = (DhtNodeExtended) obj;
					if(!receivedNodes.contains(received)){
						receivedNodes.add(received);
						log.verboseDebug("Added to received nodes "+received);
					}
				} else if(obj instanceof NodesInfoRequestMessage){
					NodesInfoRequestMessage message = (NodesInfoRequestMessage) obj;
					if(!message.isRequest()){
						log.verboseDebug("All nodes received.");
						break;
					}
				}
				obj = ois.readObject();
				log.verboseDebug("Received obj = "+obj);
			}
		}catch (ConnectException e) {
			log.verboseDebug("Connection refused by "+address+". Aborting.");			
		}catch (Exception e) {
			log.exception(e);
			for(Throwable t : e.getSuppressed()){
				log.exception(t);
			}
		}
		log.verboseDebug("Gonna return "+receivedNodes+" containing "+receivedNodes.size()+" nodes.");
		return receivedNodes;
	}
	
	private Collection<DhtNodeExtended> loadFromFile(){
		DhtProperties props = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
		String path = props.getDhtBaseDir()+props.getDhtNodesFileName();
		File nodesFile = new File(path);
		FileInputStream fis;
		try {
			fis = new FileInputStream(nodesFile);
		} catch (FileNotFoundException e) {
			return null;
		}
		JsonReader reader = Json.createReader(fis);
		JsonArray addressesArray = reader.readArray();
		ArrayList<DhtNodeExtended> retAddresses = new ArrayList<>();
		for(int i = 0; i < addressesArray.size(); i++){
			JsonObject entry = addressesArray.getJsonObject(i);
			String idString = entry.getString(dhtIdJSonKey);
			String ipString = entry.getString(dhtInetAddresJSonKey);
			int udp = entry.getInt(udpPortJSonKey);
			int tcp = entry.getInt(tcpPortJSonKey);
			long transactionNonce = entry.getJsonNumber(transactionNonceJSonKey).longValue();
			long transactionCreationSeed = entry.getJsonNumber(transactionSeedNumberJSonKey).longValue();
			long transactionBlockNumber = entry.getJsonNumber(transactionBlockNumberJSonKey).longValue();
			InetAddress ip; 
			try {
				ip = InetAddress.getByName(ipString);
			} catch (UnknownHostException e) {
				log.exception(e);
				continue;
			}
			DhtID id = new DefaultDhtID(idString);
			DhtAddress addres = new DefaultDhtAddress(ip, udp, tcp);
			String pubKeyHex = entry.getString(pubKeyJSonKey);
			PublicKey pubKey = DhtSigningUtils.regeneratePublicKey(pubKeyHex);
			if(pubKey == null){
				continue;
			}
//			retAddresses.add(new DefaultDhtNode(id, pubKeyHex, addres));
			DefaultDhtNodeExtended reconstructedNode;
			try {
				reconstructedNode = new DefaultDhtNodeExtended(id, addres, pubKey, new DefaultTransaction(id, transactionNonce, transactionCreationSeed, pubKey), transactionBlockNumber);
			} catch (IncoherentTransactionException e) {
				log.exception(e);
				continue;
			}
			retAddresses.add(reconstructedNode);
		}
		return retAddresses;
	}
	
	public void saveInFile(){
		if(addresses == null || addresses.size() == 0){
			return;
		}
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for(DhtNodeExtended node : addresses){
			JsonObjectBuilder entry = Json.createObjectBuilder().
					add(dhtIdJSonKey, node.getID().toHexString()).
					add(dhtInetAddresJSonKey, node.getAddress().getAddress().getHostAddress()).
					add(udpPortJSonKey, node.getAddress().getUdpPort()).
					add(tcpPortJSonKey, node.getAddress().getTcpPort()).
					add(pubKeyJSonKey, DhtSigningUtils.publicKeyHexRepresentation(node.getPublicKey())).
					add(transactionNonceJSonKey, node.getTransaction().getTransactionNonce()).
					add(transactionSeedNumberJSonKey, node.getTransaction().getCreationSeedNumber()).
					add(transactionBlockNumberJSonKey, node.getTransactionBlockNumber());
			arrayBuilder.add(entry);
		}
		
		JsonArray addressesArray = arrayBuilder.build();
		
		DhtProperties props = (DhtProperties) PropertiesManager.getManager().getProperties(ModuleProperty.DHT);
		String path = props.getDhtBaseDir()+props.getDhtNodesFileName();
		Path filePath = FileSystems.getDefault().getPath(path);
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
		Map<String, Object> jsonProps = new HashMap<String, Object>(1);
		jsonProps.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonWriterFactory writerFactory = Json.createWriterFactory(jsonProps);
		JsonWriter writer = writerFactory.createWriter(fos);
		writer.writeArray(addressesArray);
		writer.close();
	}
	
	public Collection<DhtNodeExtended> getNodes() {
		return addresses;
	}

}
