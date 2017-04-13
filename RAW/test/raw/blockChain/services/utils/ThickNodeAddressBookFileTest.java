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
package raw.blockChain.services.utils;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import raw.blockChain.services.utils.ThickNodeAddressBookFile;
import raw.logger.Log;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

public class ThickNodeAddressBookFileTest {
	
	ThickNodeAddressBookFile underTest;
	
	ArrayList<InetSocketAddress> addressList;
	
	@AfterClass
	public static void tearDownClass(){
		Log.getLogger().flushLogger();
	}

	@Before
	public void setUp() throws Exception {
		addressList = new ArrayList<InetSocketAddress>();
		addressList.add(new InetSocketAddress("10.10.10.10", 100));
		addressList.add(new InetSocketAddress("147.162.96.72", 4010));
		addressList.add(new InetSocketAddress("95.10.12.42", 101));
		addressList.add(new InetSocketAddress("101.12.13.14", 3000));
	}

	@After
	public void tearDown() throws Exception {
		underTest = null;
		addressList = null;
	}

	@Test
	public void testBuildWithArray() {
		underTest = new ThickNodeAddressBookFile(addressList);
		
		assertNotNull("Constructed object should not be null!", underTest);
	}
	
	@Test
	public void testGetArrayList() throws Exception {
		underTest = new ThickNodeAddressBookFile(addressList);
		assertEquals("Wrong list!", addressList, underTest.getAddressList());
	}
	
	@Test
	public void testCreateFile() throws Exception {
		underTest = new ThickNodeAddressBookFile(addressList);
		
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		String fileName = properties.getBlockChainDir()+properties.getThickNodesAddressesFileName()+".json";
		Path filePath = FileSystems.getDefault().getPath(fileName);
		if(Files.exists(filePath)){
			Files.delete(filePath);
		}
		
		underTest.writeToFile();
		
		assertTrue("File has not been created!", Files.exists(filePath));
	}
	
	@Test
	public void testDefaultListCreation() throws Exception {
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		String fileName = properties.getBlockChainDir()+properties.getThickNodesAddressesFileName()+".json";
		Path filePath = FileSystems.getDefault().getPath(fileName);
		if(Files.exists(filePath)){
			Files.delete(filePath);
		}
		
		underTest = new ThickNodeAddressBookFile();
		
		assertEquals("List is of wrong size!", underTest.defaultListSize(), underTest.getAddressList().size());
	}
	
	@Test
	public void testLoadFromFile() throws Exception {
		underTest = new ThickNodeAddressBookFile(addressList);
		underTest.writeToFile();
		
		underTest = null;
		underTest = new ThickNodeAddressBookFile();
		
		assertEquals("Wrong list!", addressList, underTest.getAddressList());
	}
	
	@Test
	public void testFileUpdate() throws Exception {
		underTest = new ThickNodeAddressBookFile(addressList);
		underTest.writeToFile();
		
		addressList.add(new InetSocketAddress("42.42.42.42", 42));
		underTest = new ThickNodeAddressBookFile(addressList);
		underTest.writeToFile();
		
		underTest = null;
		underTest = new ThickNodeAddressBookFile();
		assertEquals("Wrong list!", addressList, underTest.getAddressList());
	}

}
