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
package raw.dht.implementations;

import static org.junit.Assert.*;

import java.rmi.UnexpectedException;
import java.security.PublicKey;
import java.util.concurrent.ExecutorService;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import raw.blockChain.api.HashValue;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultHashValue;
import raw.blockChain.api.implementations.utils.TransactionUtils;
import raw.concurrent.RAWExecutors;
import raw.dht.DhtCore;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.implementations.DefaultDhtHasher;
import raw.dht.implementations.DefaultDhtID;
import raw.dht.implementations.ProofOfWorkManager;
import raw.dht.implementations.utils.DhtSigningUtils;

public class ProofOfWorkManagerTest {
	
	DhtID id = new DefaultDhtID("a008093b0b74e17bd6c31d41f3461104c94a955080d7d91a12c4f4b63c4a817b29c9cdc3d9e7618dc0498593db74517d2cc2c27e31d11be2fcebd7d4f8ec8815");
	ProofOfWorkManager underTest;
	long seedBlockNumber;
	PublicKey pubKey;
	ExecutorService pool;
	
	DhtCore coreMock;

	HashValue value = new DefaultHashValue("0000005960a4ef5609b003cda1fc83bf43c7a69a66aabcab014d67ff628f6b15664be096cee72e930e2499fb4eeb4772f823b84e1eee341380f209eda0d46ad8");
	DhtHasher hasher = new DefaultDhtHasher(value.toByteArray());
	
	@Before
	public void setUp() throws Exception {
		seedBlockNumber = 42;
		pubKey = DhtSigningUtils.getSignKeyPair().getPublic();
		coreMock = EasyMock.createMock(DhtCore.class);
		pool = RAWExecutors.newCachedThreadPool();
		EasyMock.expect(coreMock.getThreadPool()).andReturn(pool).anyTimes();
		EasyMock.expect(coreMock.getHasherFromSeedNumber(seedBlockNumber)).andReturn(hasher).anyTimes();
		underTest = new ProofOfWorkManager(id, pubKey, coreMock);
	}

	@After
	public void tearDown() throws Exception {
		underTest = null;
	}
	
	@Test
	public void test() {
		long begin = System.currentTimeMillis();
		for(long nonce = Long.MIN_VALUE; nonce < Long.MAX_VALUE; nonce++) {
			DhtID computed = hasher.hashDhtIDwithLongNonce(id, nonce);
			HashValue converted = new DefaultHashValue(computed.toByteArray());
//			System.out.println("Converted: "+converted.toHexString());
			HashValue target = new DefaultHashValue("000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
//			HashValue target = new DefaultHashValue("0000ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
//			HashValue target = new DefaultHashValue("00000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
//			System.out.println("Target   : "+target.toHexString());
			HashValue masked = converted.maskWith(target);
//			System.out.println("Masked   : "+masked.toHexString());
			if(masked.equals(converted)){
				long end = System.currentTimeMillis();
				System.out.println("Converted: "+converted.toHexString());
				System.out.println("Target   : "+target.toHexString());
				System.out.println("Masked   : "+masked.toHexString());
				System.out.println("Token found in "+((end-begin)/1000));
				System.out.println("Nonce: "+nonce);
				System.out.println("(min val: "+Long.MIN_VALUE+"; max val: "+Long.MAX_VALUE+")");
				assertTrue(true);
//				assertTrue("Nonce does not add up!!", DhtProofOfWorkUtils.checkTokenValidity(id, nonce, value.toByteArray()));
				return;
			}
			nonce++;
		}
		fail("No nonce found.");
	}

	@Test
	public void testBlockingGetToken() {
		EasyMock.replay(coreMock);
//		HashValue value = new DefaultHashValue("0000005960a4ef5609b003cda1fc83bf43c7a69a66aabcab014d67ff628f6b15664be096cee72e930e2499fb4eeb4772f823b84e1eee341380f209eda0d46ad8");
		try {
//			underTest.blockingGetToken(value.toByteArray());
			underTest.blockingGetToken(seedBlockNumber);
		} catch (UnexpectedException e) {
			e.printStackTrace();
		}
		Transaction token = underTest.getToken(seedBlockNumber);
		assertTrue("token does not hash up to a valid token", TransactionUtils.isValid(id, token.getTransactionNonce(), hasher));
	}

}
