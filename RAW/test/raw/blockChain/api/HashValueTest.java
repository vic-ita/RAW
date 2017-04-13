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
package raw.blockChain.api;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import raw.blockChain.api.HashValue;
import raw.blockChain.api.implementations.DefaultHashValue;

public class HashValueTest {

	HashValue underTest = null;
	byte[] origin = new byte[5];
	
	@Before
	public void setUp() throws Exception {
		origin = new byte[5];
		for (int i = 0; i < origin.length; i++) {
			origin[i] = (byte) (0x0a+i);
		}
		underTest = new DefaultHashValue(origin);
	}

	@After
	public void tearDown() throws Exception {
		underTest = null;
	}

	@Test
	public void testToByteArray() {
		byte[] returnedArray = underTest.toByteArray();
		System.out.println("Returned array: "+Arrays.toString(returnedArray));
		assertArrayEquals("Original array and returned one differs.", origin, returnedArray);
		
		returnedArray[1] = (byte) (returnedArray[1]+3); // change vale to check that this is a copy and not a reference
		System.out.println("Returned array: "+Arrays.toString(returnedArray));
		
		returnedArray = underTest.toByteArray();
		assertArrayEquals("Original array and returned one differ.", origin, returnedArray);
	}

	@Test
	public void testToHexString() {
		String expected = "0a0b0c0d0e";
		assertEquals("Hex string differs from expected!!", expected, underTest.toHexString());
	}

	@Test
	public void testChangeEndianness() {
		HashValue reverse = underTest.changeEndianness();
		
		byte[] reversed = new byte[5];
		for (int i = 0; i < reversed.length; i++) {
			reversed[i] = (byte) (0x0e-i);
		}		
		assertArrayEquals("Original expected and returned one differs.", reversed, reverse.toByteArray());
		
		String expected = "0e0d0c0b0a";
		assertEquals("Hex string differs from expected!!", expected, reverse.toHexString());
	}

	@Test
	public void testEquals() {
		HashValue anotherOne = new DefaultHashValue(origin);
		
		assertEquals("The two objects should be equal!", underTest, anotherOne);
		
		assertFalse("They should be different!", underTest.equals(anotherOne.changeEndianness()));
	}
	
	@Test
	public void testMaskWith1() {
		byte[] maskArr = {0, 0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
		HashValue mask = new DefaultHashValue(maskArr);
		
		byte[] expectedArr = Arrays.copyOf(origin, origin.length);
		expectedArr[0] = 0;
		expectedArr[1] = 0;
		HashValue expected = new DefaultHashValue(expectedArr);
		
		assertEquals("Masked hash is not what I wanted.", expected, underTest.maskWith(mask));		
		
		expectedArr = Arrays.copyOf(origin, origin.length);
		expectedArr[3] = 0;
		expectedArr[4] = 0;
		expected = new DefaultHashValue(expectedArr);
		
		assertEquals("Masked hash is not what I wanted.", expected, underTest.maskWith(mask.changeEndianness()));
	}
	
	@Test
	public void testMaskWith2() {
		byte[] maskArr = {0, 0, (byte) 0xFF};
		HashValue mask = new DefaultHashValue(maskArr);
		
		byte[] expectedArr = Arrays.copyOf(origin, origin.length);
		expectedArr[0] = 0;
		expectedArr[1] = 0;
		HashValue expected = new DefaultHashValue(expectedArr);
		
		assertEquals("Masked hash is not what I wanted.", expected, underTest.maskWith(mask));		

	}
	
	@Test
	public void testMaskWith3() {
		byte[] maskArr = {(byte) 0xFF, (byte) 0xFF};
		HashValue mask = new DefaultHashValue(maskArr);
		
		byte[] expectedArr = Arrays.copyOf(origin, origin.length);
//		expectedArr[0] = 0;
//		expectedArr[1] = 0;
		HashValue expected = new DefaultHashValue(expectedArr);
		
		assertEquals("Masked hash is not what I wanted.", expected, underTest.maskWith(mask));		

	}
	
	@Test
	public void testMaskWith4() {
		String hashHexString = "000000cb565e5721ac640b4ead7b543890111e894b08a1276e44013d7cfd1f6b5545ddbfd51ce4cfcadd94f74b9a8b09f32eca7a49707a6545f731030946b5cd";
		underTest = new DefaultHashValue(hashHexString);
		
		byte[] maskBytes = new byte[1];
		Arrays.fill(maskBytes, (byte)0x00);
		HashValue mask = new DefaultHashValue(maskBytes);		
		
		assertEquals("Masked hash is not what I wanted.", underTest, underTest.maskWith(mask));		
	}
	
	@Test
	public void testMaskWith5() {
		String hashHexString = "000000cb565e5721ac640b4ead7b543890111e894b08a1276e44013d7cfd1f6b5545ddbfd51ce4cfcadd94f74b9a8b09f32eca7a49707a6545f731030946b5cd";
		underTest = new DefaultHashValue(hashHexString);
		
		byte[] maskBytes = new byte[2];
		Arrays.fill(maskBytes, (byte)0x00);
		HashValue mask = new DefaultHashValue(maskBytes);		
		
		assertEquals("Masked hash is not what I wanted.", underTest, underTest.maskWith(mask));		
	}
	
	@Test
	public void testMaskWith6() {
		String hashHexString = "000000cb565e5721ac640b4ead7b543890111e894b08a1276e44013d7cfd1f6b5545ddbfd51ce4cfcadd94f74b9a8b09f32eca7a49707a6545f731030946b5cd";
		underTest = new DefaultHashValue(hashHexString);
		
		byte[] maskBytes = new byte[3];
		Arrays.fill(maskBytes, (byte)0x00);
		HashValue mask = new DefaultHashValue(maskBytes);		
		
		assertEquals("Masked hash is not what I wanted.", underTest, underTest.maskWith(mask));		
	}
	
	@Test
	public void testMaskWith7() {
		String hashHexString = "00000000565e5721ac640b4ead7b543890111e894b08a1276e44013d7cfd1f6b5545ddbfd51ce4cfcadd94f74b9a8b09f32eca7a49707a6545f731030946b5cd";
		underTest = new DefaultHashValue(hashHexString);
		
		byte[] maskBytes = new byte[4];
		Arrays.fill(maskBytes, (byte)0x00);
		HashValue mask = new DefaultHashValue(maskBytes);		
		
		assertEquals("Masked hash is not what I wanted.", underTest, underTest.maskWith(mask));		
	}
	
	@Test
	public void testMaskWith9() {
		String hashHexString = "00000000005e5721ac640b4ead7b543890111e894b08a1276e44013d7cfd1f6b5545ddbfd51ce4cfcadd94f74b9a8b09f32eca7a49707a6545f731030946b5cd";
		underTest = new DefaultHashValue(hashHexString);
		
		byte[] maskBytes = new byte[5];
		Arrays.fill(maskBytes, (byte)0x00);
		HashValue mask = new DefaultHashValue(maskBytes);		
		
		assertEquals("Masked hash is not what I wanted.", underTest, underTest.maskWith(mask));		
	}
	
	@Test
	public void testMaskWith10() {
		String hashHexString = "0000000000005721ac640b4ead7b543890111e894b08a1276e44013d7cfd1f6b5545ddbfd51ce4cfcadd94f74b9a8b09f32eca7a49707a6545f731030946b5cd";
		underTest = new DefaultHashValue(hashHexString);
		
		byte[] maskBytes = new byte[6];
		Arrays.fill(maskBytes, (byte)0x00);
		HashValue mask = new DefaultHashValue(maskBytes);		
		
		assertEquals("Masked hash is not what I wanted.", underTest, underTest.maskWith(mask));		
	}
	
	@Test
	public void testMaskWith11() {
		String hashHexString = "000000cb565e5721ac640b4ead7b543890111e894b08a1276e44013d7cfd1f6b5545ddbfd51ce4cfcadd94f74b9a8b09f32eca7a49707a6545f731030946b5cd";
		underTest = new DefaultHashValue(hashHexString);
		
		byte[] maskBytes = new byte[4];
		Arrays.fill(maskBytes, (byte)0x00);
		HashValue mask = new DefaultHashValue(maskBytes);		
		
		assertFalse("Masked hash is not what I wanted.", underTest.equals(underTest.maskWith(mask)));		
	}
	
	@Test
	public void testSerializable() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			oos.writeObject(underTest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte [] serialized = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(bais);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HashValue deserialized = null;
		try {
			deserialized = (HashValue) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertEquals("Object deserialized differs from original!", deserialized, underTest);
	}
}
