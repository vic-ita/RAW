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
package raw.settings;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * @author vic
 *
 */
public class DhtProperties implements Properties {
	
	private int udpListeningSocket;
	private String udpListeningSocketJsonKey = "UDP socket listening port";
	
	private int tcpListeningSocket;
	private String tcpListeningSocketJsonKey = "TCP socket listening port";
	
	private String dhtBaseDir;
	private String dhtBaseDirJsonKey = "DHT base directory";
	
	private String dhtNodesFileName;
	private String dhtNodesFileNameJsonKey = "Nodes list file name";
	
	private boolean nodesInfoServerRunning;
	private String nodesInfoServerRunningJsonKey = "Nodes Info Server is running";
	
	private int nodesInfoServerPort;
	private String nodesInfoServerPortJsonKey = "Nodes Info Server network port";
	
	private String nodesInfoServerFileName;
	private String nodesInfoServerFileNameJsonKey = "Nodes Info Server addresses file";
	
	private boolean webGuiIsOn;
	private String webGuiIsOnJsonKey = "Web GUI is on (WARNING: possible security breach)";
	
	private int webGuiPort;
	private String webGuiPortJsonKey = "Web GUI network port";
	
	public DhtProperties() {
		udpListeningSocket = 5010;
		tcpListeningSocket = 5010;
		
		PropertiesManager manager = PropertiesManager.getManager();
		dhtBaseDir = manager.getGeneralBaseDir()+"dht"+manager.getFileSeparator();
		
		dhtNodesFileName = "nodes.json";
		
		nodesInfoServerRunning = false;
		nodesInfoServerPort = 5011;
		
		nodesInfoServerFileName = "nodesInfoServer.json";
		
		defaultWebGuiIsOn();
		
		defaultWebGuiPort();
	}
	
	private void defaultWebGuiIsOn(){
		webGuiIsOn = false;
	}
	
	private void defaultWebGuiPort(){
		webGuiPort = 5020;
	}
	
	public DhtProperties(JsonObject json) {
		udpListeningSocket = json.getInt(udpListeningSocketJsonKey);
		
		tcpListeningSocket = json.getInt(tcpListeningSocketJsonKey);
		
		dhtBaseDir = json.getString(dhtBaseDirJsonKey);
		
		dhtNodesFileName = json.getString(dhtNodesFileNameJsonKey);
		
		nodesInfoServerRunning = json.getBoolean(nodesInfoServerRunningJsonKey);
		
		nodesInfoServerPort = json.getInt(nodesInfoServerPortJsonKey);
		
		nodesInfoServerFileName = json.getString(nodesInfoServerFileNameJsonKey);
		
		boolean  updatedSettings = false;
		
		try {
			webGuiIsOn = json.getBoolean(webGuiIsOnJsonKey);
		} catch (NullPointerException e) {
			defaultWebGuiIsOn();
			updatedSettings = true;
		}
		
		try {
			webGuiPort = json.getInt(webGuiPortJsonKey);
		} catch (NullPointerException e) {
			defaultWebGuiPort();
			updatedSettings = true;
		}
		
		if(updatedSettings){
			notifyChanged();
		}
	}

	/* (non-Javadoc)
	 * @see raw.settings.Properties#toJsonObject()
	 */
	@Override
	public JsonObject toJsonObject() {
		JsonObject jsObj = Json.createObjectBuilder().
				add(udpListeningSocketJsonKey, udpListeningSocket).
				add(tcpListeningSocketJsonKey, tcpListeningSocket).
				add(dhtBaseDirJsonKey, dhtBaseDir).
				add(dhtNodesFileNameJsonKey, dhtNodesFileName).
				add(nodesInfoServerRunningJsonKey, nodesInfoServerRunning).
				add(nodesInfoServerPortJsonKey, nodesInfoServerPort).
				add(nodesInfoServerFileNameJsonKey, nodesInfoServerFileName).
				add(webGuiIsOnJsonKey, webGuiIsOn).
				add(webGuiPortJsonKey, webGuiPort).
				build();
		return jsObj;
	}
	
	private void notifyChanged(){
		PropertiesManager.getManager().notifyPropertiesChanged(ModuleProperty.DHT);
		return;
	}

	/**
	 * @return the udpListeningSocket
	 */
	public int getUdpListeningSocket() {
		return udpListeningSocket;
	}

	/**
	 * @param udpListeningSocket the udpListeningSocket to set
	 */
	public void setUdpListeningSocket(int udpListeningSocket) {
		this.udpListeningSocket = udpListeningSocket;
		notifyChanged();
	}

	/**
	 * @return the tcpListeningSocket
	 */
	public int getTcpListeningSocket() {
		return tcpListeningSocket;
	}

	/**
	 * @param tcpListeningSocket the tcpListeningSocket to set
	 */
	public void setTcpListeningSocket(int tcpListeningSocket) {
		this.tcpListeningSocket = tcpListeningSocket;
		notifyChanged();
	}

	/**
	 * @return the dhtBaseDir
	 */
	public String getDhtBaseDir() {
		return dhtBaseDir;
	}

	/**
	 * @param dhtBaseDir the dhtBaseDir to set
	 */
	protected void setDhtBaseDir(String dhtBaseDir) {
		this.dhtBaseDir = dhtBaseDir;
		notifyChanged();
	}

	/**
	 * @return the dhtNodesFileName
	 */
	public String getDhtNodesFileName() {
		return dhtNodesFileName;
	}

	/**
	 * @param dhtNodesFileName the dhtNodesFileName to set
	 */
	protected void setDhtNodesFileName(String dhtNodesFileName) {
		this.dhtNodesFileName = dhtNodesFileName;
		notifyChanged();
	}

	/**
	 * @return the nodesInfoServerRunning
	 */
	public boolean isNodesInfoServerRunning() {
		return nodesInfoServerRunning;
	}

	/**
	 * @param nodesInfoServerRunning the nodesInfoServerRunning to set
	 */
	public void setNodesInfoServerRunning(boolean nodesInfoServerRunning) {
		this.nodesInfoServerRunning = nodesInfoServerRunning;
		notifyChanged();
	}

	/**
	 * @return the nodesInfoServerPort
	 */
	public int getNodesInfoServerPort() {
		return nodesInfoServerPort;
	}

	/**
	 * @param nodesInfoServerPort the nodesInfoServerPort to set
	 */
	public void setNodesInfoServerPort(int nodesInfoServerPort) {
		this.nodesInfoServerPort = nodesInfoServerPort;
		notifyChanged();
	}

	/**
	 * @return the nodesInfoServerFileName
	 */
	public String getNodesInfoServerFileName() {
		return nodesInfoServerFileName;
	}

	/**
	 * @param nodesInfoServerFileName the nodesInfoServerFileName to set
	 */
	protected void setNodesInfoServerFileName(String nodesInfoServerFileName) {
		this.nodesInfoServerFileName = nodesInfoServerFileName;
		notifyChanged();
	}

	/**
	 * @return the webGuiIsOn
	 */
	public boolean isWebGuiIsOn() {
		return webGuiIsOn;
	}

	/**
	 * @param webGuiIsOn the webGuiIsOn to set
	 */
	public void setWebGuiIsOn(boolean webGuiIsOn) {
		this.webGuiIsOn = webGuiIsOn;
		notifyChanged();
	}

	/**
	 * @return the webGuiPort
	 */
	public int getWebGuiPort() {
		return webGuiPort;
	}

	/**
	 * @param webGuiPort the webGuiPort to set
	 */
	public void setWebGuiPort(int webGuiPort) {
		this.webGuiPort = webGuiPort;
		notifyChanged();
	}

}
