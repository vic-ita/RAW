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
package raw.dht;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import raw.dht.DhtAddress;
import raw.dht.implementations.DefaultDhtAddress;

public class DhtAddressTest {

	private DhtAddress underTest;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		underTest = null;
	}
	
	@Test
	public void testInetAddressConstructor(){
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName("192.168.1.105");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int port = 12345;
		underTest = new DefaultDhtAddress(addr, port);
		
		assertArrayEquals("Wrong address", addr.getAddress(), underTest.getAddress().getAddress());
		assertEquals("Wrong port", port, underTest.getUdpPort());
	}
	
	@Test
	public void testByteAddressConstructor(){
		byte[] addr = {(byte) 192, (byte) 168, (byte)1, (byte)105};
		int port = 12345;
		try {
			underTest = new DefaultDhtAddress(addr, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		assertArrayEquals("Wrong address", addr, underTest.getAddress().getAddress());
		assertEquals("Wrong port", port, underTest.getUdpPort());
	}
	
	@Test
	public void testInetAddressException1(){
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName("192.168.1.105");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int port = -1;
		try {
			underTest = new DefaultDhtAddress(addr, port);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Wrong exception");
		}		
	}
	
	@Test
	public void testInetAddressException2(){
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName("192.168.1.105");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int port = 65536;
		try {
			underTest = new DefaultDhtAddress(addr, port);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Wrong exception");
		}		
	}
	
	@Test
	public void testByteAddressException1(){
		byte[] addr = {(byte) 192, (byte) 168, (byte)1};
		int port = 12345;
		try {
			underTest = new DefaultDhtAddress(addr, port);
		} catch (UnknownHostException e) {
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail("Wrong exception");
		}
	}
	
	@Test
	public void testByteAddressException2(){
		byte[] addr = {(byte) 192, (byte) 168, (byte)1, (byte)42, (byte)18};
		int port = 12345;
		try {
			underTest = new DefaultDhtAddress(addr, port);
		} catch (UnknownHostException e) {
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail("Wrong exception");
		}
	}
	
	@Test
	public void testEquals(){
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName("192.168.1.105");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int port = 12345;
		underTest = new DefaultDhtAddress(addr, port);
		
		byte[] addr2 = {(byte) 192, (byte) 168, (byte)1, (byte)105};

		DhtAddress anotherOne = null;
		try {
			anotherOne = new DefaultDhtAddress(addr2, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		assertEquals("Objects do not equals!", underTest, anotherOne);
	}
}
