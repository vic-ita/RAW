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

import java.util.ArrayList;
import java.util.Random;

import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultTransaction;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.implementations.DefaultDhtHasher;
import raw.dht.implementations.utils.DhtSigningUtils;
import raw.logger.Log;

public class RandomTransactionsGenerator {
	
	private Log log;
	
	private Random rand;
	private DhtHasher dhtHasher;
	
	public RandomTransactionsGenerator() {
		log = Log.getLogger();
		
		rand = new Random(System.currentTimeMillis());
		dhtHasher = new DefaultDhtHasher();
	}
	
	public ArrayList<Transaction> getTransactions(int howManyTransactions){
		log.debug("Starting generation of "+howManyTransactions+" transactions.");
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		for(int i=0; i<howManyTransactions; i++){			
			byte[] plainID = new byte[64];
			rand.nextBytes(plainID);
			
			DhtID id = dhtHasher.hashBytes(plainID);
			
			Transaction transaction = new DefaultTransaction(id, 0L, 0L, DhtSigningUtils.getSignKeyPair().getPublic());
			
			transactions.add(transaction);
		}
		log.debug("Transactions are genetated.");
		return transactions;
	}

}
