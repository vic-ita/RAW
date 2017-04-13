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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.implementations.DefaultDhtHasher;

public class DhtHasherTest {
	
	private DhtHasher underTest;
	
	private String trololol = "trololol";
//	private String hashTrololol = "8ba93d6a4f5f76d68fb2e7e6c5099b8c7f2bedfb9dee9b891762ba11eaa38bb5";
	private String hashTrololol = "8927dd0eeb9b65500d148bdf7a144b598fc161c974d86e1ec18174ca7813ee8483f56035e28779aae83e11017b29fe08208b09d515cafb214e5defdbace07f36";
	
	private String lorem = "Lorem ipsum dolor sit amet, consectetur adipisci elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
//	private String hashLorem = "2fccbf31b36ca8723dc065938c7e6fadf2b2579ab9598ebbbc9a1f5b89c02ce4";
	private String hashLorem = "220c81c4c832fa1e209ff502b9aaa69f2b46a1b10abe1d9f9a57b861414953c711728efeefdbb353a603c8ae45c8530e1df7b0cf851f73835d0b9ddd1ca9db08";

	@Before
	public void setUp() throws Exception {
		underTest = new DefaultDhtHasher();
	}

	@After
	public void tearDown() throws Exception {
		underTest = null;
	}

	@Test
	public void testHashString() {
		DhtID hash = underTest.hashString(trololol);
		byte[] expected = null;
		try {
			expected = Hex.decodeHex(hashTrololol.toCharArray());
		} catch (DecoderException e) {
			e.printStackTrace();
		}
		assertArrayEquals("Returned hash is not what I expected (trololol)", expected, hash.toByteArray());
		
		hash = underTest.hashString(lorem);
		expected = null;
		try {
			expected = Hex.decodeHex(hashLorem.toCharArray());
		} catch (DecoderException e) {
			e.printStackTrace();
		}
		assertArrayEquals("Returned hash is not what I expected (lorem ipsum)", expected, hash.toByteArray());
	}
	
	@Test
	public void testLength() {
		assertEquals("Wrong length", 64, underTest.hashLength());
	}

}
