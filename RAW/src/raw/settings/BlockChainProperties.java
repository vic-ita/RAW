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

public class BlockChainProperties implements Properties {
	
	private String blockChainName;
	private String blockChainNameJsonKey = "Block Chain Name";
	
	private String blockChainDir;
	private String blockChainDirJsonKey = "Block Chain Directory";
	
	private int listeningSocket;
	private String listeningSocketJsonKey = "Listening Socket";
	
	private int merkleCacheSize;
	private String merkleCacheSizeJsonKey = "Merkle Calculator Cache Size";
	
	private boolean minerIsOn;
	private String minerIsOnJsonKey = "Mininer is ON";
	
	private boolean thickClientIsOn;
	private String thickClientIsOnJsonKey = "Thick Client is ON";
	
	private String blockChainDBName;
	private String blockChainDBNameJsonKey = "BlockChain DataBase Name";
	
	private String thickNodesAddressesFileName;
	private String thickNodesAddressesFileNameJsonKey = "Thick Nodes Address Book File Name";
	
	private String minerNodesAddressesFileName;
	private String minerNodesAddressesFileNameJsonKey = "Miner Nodes Address Book File Name";
	
	private int minerListeningSocket;
	private String minerListeningSocketJsonKey = "Miner Listening Socket";
	
	private boolean shortCheckOnStartup;
	private String shortCheckOnStartupJsonKey = "Perform only short chain check on startup";
	
	private int blocksCheckedOnShortCheck;
	private String blocksCheckedOnShortCheckJsonKey = "Number of blocks checked during short startup check";
	
	public BlockChainProperties() {
		blockChainName = "RAW_STD_BLOCKCHAIN";
		
		PropertiesManager manager = PropertiesManager.getManager();
		blockChainDir = manager.getGeneralBaseDir()+"blockChain"+manager.getFileSeparator();
		
		listeningSocket = 4010;
		
		merkleCacheSize = 32;
		
		minerIsOn = false;
		thickClientIsOn = true;
		
		defaultBlocksDBName();
		
		defaultThickNodesAddressBookName();
		
		defaultMinerNodesAddressBookName();
		
		defaultMinerListeningSocket();
		
		defaultShortCheckOnStartup();
		
		defaultBlocksCheckedOnShortCheck();
	}
	
	public BlockChainProperties(JsonObject json) {
		blockChainName = json.getString(blockChainNameJsonKey);
		
		blockChainDir = json.getString(blockChainDirJsonKey);
		
		listeningSocket = json.getInt(listeningSocketJsonKey);
		
		merkleCacheSize = json.getInt(merkleCacheSizeJsonKey);
		
		minerIsOn = json.getBoolean(minerIsOnJsonKey);
		thickClientIsOn = json.getBoolean(thickClientIsOnJsonKey);

		boolean updatedSettings = false;
		
		try {
			blockChainDBName = json.getString(blockChainDBNameJsonKey);		
		} catch (NullPointerException e) {
			defaultBlocksDBName();
			updatedSettings = true;
		}
		
		try {
			thickNodesAddressesFileName = json.getString(thickNodesAddressesFileNameJsonKey);
		} catch (NullPointerException e) {
			defaultThickNodesAddressBookName();
			updatedSettings = true;
		}
		
		try {
			minerNodesAddressesFileName = json.getString(minerNodesAddressesFileNameJsonKey);
		} catch (NullPointerException e) {
			defaultMinerNodesAddressBookName();
			updatedSettings = true;
		}
		
		try {
			minerListeningSocket = json.getInt(minerListeningSocketJsonKey);
		} catch (NullPointerException e) {
			defaultMinerListeningSocket();
			updatedSettings = true;
		}
		
		try {
			shortCheckOnStartup = json.getBoolean(shortCheckOnStartupJsonKey);
		} catch (NullPointerException e) {
			defaultShortCheckOnStartup();
			updatedSettings = true;
		}
		
		try {
			blocksCheckedOnShortCheck = json.getInt(blocksCheckedOnShortCheckJsonKey);
		} catch (NullPointerException e) {
			defaultBlocksCheckedOnShortCheck();
			updatedSettings = true;
		}
		
		if(updatedSettings){
			notifyChanged();
		}
	}	

	@Override
	public JsonObject toJsonObject() {
		JsonObject jsObj = Json.createObjectBuilder().
				add(blockChainNameJsonKey, blockChainName).
				add(blockChainDirJsonKey, blockChainDir).
				add(listeningSocketJsonKey, listeningSocket).
				add(merkleCacheSizeJsonKey, merkleCacheSize).
				add(minerIsOnJsonKey, minerIsOn).
				add(thickClientIsOnJsonKey, thickClientIsOn).
				add(blockChainDBNameJsonKey, blockChainDBName).
				add(thickNodesAddressesFileNameJsonKey, thickNodesAddressesFileName).
				add(minerNodesAddressesFileNameJsonKey, minerNodesAddressesFileName).
				add(minerListeningSocketJsonKey, minerListeningSocket).
				add(shortCheckOnStartupJsonKey, shortCheckOnStartup).
				add(blocksCheckedOnShortCheckJsonKey, blocksCheckedOnShortCheck).
				build();
		return jsObj;
	}
	
	private void defaultBlocksDBName(){
		blockChainDBName = "blocksDB";
	}
	
	private void defaultThickNodesAddressBookName(){
		thickNodesAddressesFileName = "thickNodesAddresses";
	}
	
	private void defaultMinerNodesAddressBookName(){
		minerNodesAddressesFileName = "minerNodesAddresses";
	}
	
	private void defaultMinerListeningSocket(){
		minerListeningSocket = 4011;
	}
	
	private void defaultShortCheckOnStartup(){
		shortCheckOnStartup = true;
	}
	
	private void defaultBlocksCheckedOnShortCheck(){
		blocksCheckedOnShortCheck = 200;
	}

	/**
	 * @return the blockChainName
	 */
	public String getBlockChainName() {
		return blockChainName;
	}

	/**
	 * @param blockChainName the blockChainName to set
	 */
	protected void setBlockChainName(String blockChainName) {
		this.blockChainName = blockChainName;
		notifyChanged();
	}

	/**
	 * @return the listeningSocket
	 */
	public int getListeningSocket() {
		return listeningSocket;
	}

	/**
	 * @param listeningSocket the listeningSocket to set
	 */
	public void setListeningSocket(int listeningSocket) {
		this.listeningSocket = listeningSocket;
		notifyChanged();
	}

	/**
	 * @return the blockChainDir
	 */
	public String getBlockChainDir() {
		return blockChainDir;
	}

	/**
	 * @param blockChainDir the blockChainDir to set
	 */
	protected void setBlockChainDir(String blockChainDir) {
		this.blockChainDir = blockChainDir;
		notifyChanged();
	}

	/**
	 * @return the merkleCacheSize
	 */
	public int getMerkleCacheSize() {
		return merkleCacheSize;
	}

	/**
	 * @param merkleCacheSize the merkleCacheSize to set
	 */
	protected void setMerkleCacheSize(int merkleCacheSize) {
		this.merkleCacheSize = merkleCacheSize;
		notifyChanged();
	}

	/**
	 * @return the minerIsOn
	 */
	public boolean isMinerIsOn() {
		return minerIsOn;
	}

	/**
	 * @param minerIsOn the minerIsOn to set
	 */
	public void setMinerIsOn(boolean minerIsOn) {
		this.minerIsOn = minerIsOn;
		notifyChanged();
	}

	/**
	 * @return the thickClientIsOn
	 */
	public boolean isThickClientIsOn() {
		return thickClientIsOn;
	}

	/**
	 * @param thickClientIsOn the thickClientIsOn to set
	 */
	public void setThickClientIsOn(boolean thickClientIsOn) {
		this.thickClientIsOn = thickClientIsOn;
		notifyChanged();
	}

	/**
	 * @return the blockChainDBName
	 */
	public String getBlockChainDBName() {
		return blockChainDBName;
	}

	/**
	 * @param blockChainDBName the blockChainDBName to set
	 */
	protected void setBlockChainDBName(String blockChainDBName) {
		this.blockChainDBName = blockChainDBName;
		notifyChanged();
	}
	
	private void notifyChanged(){
		PropertiesManager.getManager().notifyPropertiesChanged(ModuleProperty.BLOCK_CHAIN);
		return;
	}

	/**
	 * @return the thickNodesAddressesFileName
	 */
	public String getThickNodesAddressesFileName() {
		return thickNodesAddressesFileName;
	}

	/**
	 * @param thickNodesAddressesFileName the thickNodesAddressesFileName to set
	 */
	protected void setThickNodesAddressesFileName(String thickNodesAddressesFileName) {
		this.thickNodesAddressesFileName = thickNodesAddressesFileName;
		notifyChanged();
	}

	/**
	 * @return the minerNodesAddressesFileName
	 */
	public String getMinerNodesAddressesFileName() {
		return minerNodesAddressesFileName;
	}

	/**
	 * @param minerNodesAddressesFileName the minerNodesAddressesFileName to set
	 */
	protected void setMinerNodesAddressesFileName(String minerNodesAddressesFileName) {
		this.minerNodesAddressesFileName = minerNodesAddressesFileName;
		notifyChanged();
	}

	/**
	 * @return the minerListeningSocket
	 */
	public int getMinerListeningSocket() {
		return minerListeningSocket;
	}

	/**
	 * @param minerListeningSocket the minerListeningSocket to set
	 */
	public void setMinerListeningSocket(int minerListeningSocket) {
		this.minerListeningSocket = minerListeningSocket;
		notifyChanged();
	}

	/**
	 * @return the shortCheckOnStartup
	 */
	public boolean isShortCheckOnStartup() {
		return shortCheckOnStartup;
	}

	/**
	 * @param shortCheckOnStartup the shortCheckOnStartup to set
	 */
	protected void setShortCheckOnStartup(boolean shortCheckOnStartup) {
		this.shortCheckOnStartup = shortCheckOnStartup;
		notifyChanged();
	}

	/**
	 * @return the blocksCheckedOnShortCheck
	 */
	public int getBlocksCheckedOnShortCheck() {
		return blocksCheckedOnShortCheck;
	}

	/**
	 * @param blocksCheckedOnShortCheck the blocksCheckedOnShortCheck to set
	 */
	protected void setBlocksCheckedOnShortCheck(int blocksCheckedOnShortCheck) {
		this.blocksCheckedOnShortCheck = blocksCheckedOnShortCheck;
		notifyChanged();
	}

}
