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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import raw.blockChain.api.HashValue;
import raw.blockChain.api.Hasher;
import raw.blockChain.api.implementations.DefaultHasher;

public class HasherTest {
	
	HashFunction testerDigester;
	Hasher hasherUnderTest;
	
	byte[] sourceBytes = {'a', 'b', 'c', 'd', 'e', 1, 2, 3, 4, 5};

	@Before
	public void setUp() throws Exception {
		testerDigester = Hashing.sha512();
		
		hasherUnderTest = new DefaultHasher();
	}

	@After
	public void tearDown() throws Exception {
		testerDigester = null;
		
		hasherUnderTest = null;
	}

	@Test
	public void testHashBytes() {
		byte[] expected = testerDigester.hashBytes(sourceBytes).asBytes();
		expected = testerDigester.hashBytes(expected).asBytes();
		
		HashValue result = hasherUnderTest.hashBytes(sourceBytes);
		
		assertArrayEquals(expected, result.toByteArray());
	}
	
	@Test
	public void testHashBytesTwoTimes() {
		byte[] expected = testerDigester.hashBytes(sourceBytes).asBytes();
		expected = testerDigester.hashBytes(expected).asBytes();
		
		HashValue result = hasherUnderTest.hashBytes(sourceBytes);
		
		result = null;
		
		result = hasherUnderTest.hashBytes(sourceBytes);
		
		assertArrayEquals(expected, result.toByteArray());
	}
	
	@Test
	public void testHashHashes(){
		byte[] firstPass = testerDigester.hashBytes(sourceBytes).asBytes();
		firstPass = testerDigester.hashBytes(firstPass).asBytes();
		byte[] expected = testerDigester.hashBytes(firstPass).asBytes();
		expected = testerDigester.hashBytes(expected).asBytes();
		
		HashValue firstHash = hasherUnderTest.hashBytes(sourceBytes);
		
		HashValue result = hasherUnderTest.hashHashes(firstHash);
		
		assertArrayEquals(expected, result.toByteArray());
	}
	
	@Test
	public void testHashHashes2(){
		byte[] firstPass = testerDigester.hashBytes(sourceBytes).asBytes();
		firstPass = testerDigester.hashBytes(firstPass).asBytes();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write(firstPass);
			baos.write(firstPass);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] concatenation = baos.toByteArray();
		byte[] expected = testerDigester.hashBytes(concatenation).asBytes();
		expected = testerDigester.hashBytes(expected).asBytes();
		
		HashValue firstHash = hasherUnderTest.hashBytes(sourceBytes);
		
		assertArrayEquals("Indermediate result is not ok.", firstPass, firstHash.toByteArray());
		
		HashValue result = hasherUnderTest.hashHashes(firstHash, firstHash);
		
		assertArrayEquals("The result is not the desired one", expected, result.toByteArray());
	}
	
	@Test
	public void testHashHashes3(){
		byte[] firstPass = testerDigester.hashBytes(sourceBytes).asBytes();
		firstPass = testerDigester.hashBytes(firstPass).asBytes();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write(firstPass);
			baos.write(firstPass);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] concatenation = baos.toByteArray();
		byte[] secondPass = testerDigester.hashBytes(concatenation).asBytes();
		secondPass = testerDigester.hashBytes(secondPass).asBytes();
		
		baos.reset();
		
		try {
			baos.write(secondPass);
			baos.write(firstPass);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		concatenation = baos.toByteArray();
		byte[] expected = testerDigester.hashBytes(concatenation).asBytes();
		expected = testerDigester.hashBytes(expected).asBytes();
		
		HashValue firstHash = hasherUnderTest.hashBytes(sourceBytes);
		
		assertArrayEquals("Indermediate result is not ok.", firstPass, firstHash.toByteArray());
		
		HashValue result = hasherUnderTest.hashHashes(firstHash, firstHash, firstHash);
		
		assertArrayEquals("The result is not the desired one", expected, result.toByteArray());
	}
	
	@Test
	public void testHashLength(){
		int lenght = hasherUnderTest.hashLength();
		
		assertEquals(64, lenght);
	}

}
