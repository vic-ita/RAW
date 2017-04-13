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
package raw.blockChain.transactionGenerators;

import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;

import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultTransaction;
import raw.blockChain.api.implementations.utils.TransactionUtils;
import raw.dht.DhtAddress;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.DhtNode;
import raw.dht.DhtNodeExtended;
import raw.dht.implementations.DefaultDhtAddress;
import raw.dht.implementations.DefaultDhtHasher;
import raw.dht.implementations.DefaultDhtNode;
import raw.dht.implementations.DefaultDhtNodeExtended;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.utils.DhtSigningUtils;
import raw.logger.Log;

public class RandomNodesGenerator {
	
private Log log;
	
	private Random rand;
	private DhtHasher dhtHasher;
	
	public RandomNodesGenerator() {
		log = Log.getLogger();
		
		rand = new Random(System.currentTimeMillis());
		dhtHasher = new DefaultDhtHasher();
	}
	
	public ArrayList<DhtNodeExtended> getNodes(int howManyNodes){
		log.debug("Starting generation of "+howManyNodes+" nodes.");
		ArrayList<DhtNodeExtended> nodes = new ArrayList<DhtNodeExtended>();
		for(int i=0; i<howManyNodes; i++){
			byte[] ipArray = new byte[4];
			rand.nextBytes(ipArray);
		
			int port = rand.nextInt(65535);
			
			DhtAddress address = null;
			try {
				address = new DefaultDhtAddress(ipArray, port);
			} catch (UnknownHostException e) {
				log.exception(e);
			} catch (IllegalArgumentException e) {
				log.exception(e);
			}
			
			byte[] plainID = new byte[64];
			rand.nextBytes(plainID);
			
			DhtID id = dhtHasher.hashBytes(plainID);
			
			PublicKey pubKey = DhtSigningUtils.getSignKeyPair().getPublic();
			
			DhtNode node = new DefaultDhtNode(id, pubKey, address);
			
			long nonce = Long.MIN_VALUE;
			
			while (!TransactionUtils.isValid(id, nonce, dhtHasher)) {
				if(nonce == Long.MAX_VALUE){
					i--;
					continue;
				}
				nonce++;
			}
			
			Transaction transaction = new DefaultTransaction(id, nonce, 42, pubKey);
			
			try {
				nodes.add(new DefaultDhtNodeExtended(node, transaction, 42));
			} catch (IncoherentTransactionException e) {
				i--;
				continue;
			}
		}
		log.debug("Nodes are genetated.");
		return nodes;
	}

}
