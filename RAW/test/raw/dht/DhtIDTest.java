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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.implementations.DefaultDhtHasher;

public class DhtIDTest {
	
	DhtID underTest;

	@Before
	public void setUp() throws Exception {
		String lorem = "Lorem ipsum dolor sit amet, consectetur adipisci elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		DhtHasher hasher = new DefaultDhtHasher();
		
		underTest = hasher.hashString(lorem);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testToStringLength() throws Exception {
//		System.out.println(underTest.toHexString());
//		System.out.println(underTest);
		assertEquals("Wrong length!", 20, underTest.toString().length());
	}
}
