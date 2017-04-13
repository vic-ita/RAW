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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import raw.blockChain.api.HashValue;
import raw.blockChain.api.Hasher;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.DefaultHasher;
import raw.blockChain.api.implementations.DefaultTransaction;
import raw.dht.DhtAddress;
import raw.dht.DhtHasher;
import raw.dht.DhtID;
import raw.dht.implementations.DefaultDhtAddress;
import raw.dht.implementations.DefaultDhtHasher;

public class TransactionsGenerator {

	private Transaction transaction1;
	private Transaction transaction2;
	private Transaction transaction3;
	private Transaction transaction4;
	private Transaction transaction5;
	private Transaction transaction6;
	private Transaction transaction7;
	private Transaction transaction8;
	private Transaction transaction9;
	private Transaction transaction10;
	private Transaction transaction11;
	private Transaction transaction12;
	private Transaction transaction13;
	private Transaction transaction14;
	private Transaction transaction15;
	private Transaction transaction16;
	
	private ArrayList<Transaction> transactions;

//	private HashValue hash12;
//	private HashValue hash34;
//	private HashValue hash56;
//	private HashValue hash78;
//	private HashValue hash9_10;
//	private HashValue hash11_12;
//	private HashValue hash13_14;
//	private HashValue hash15_16;
//	
//	private HashValue hash1234;
//	private HashValue hash5678;
//	private HashValue hash9_1011_12;
//	private HashValue hash13_1415_16;
//	
//	private HashValue hash12345678;
//	private HashValue hash9_1011_1213_1415_16;
	
	private Hasher hasher;
	
	public TransactionsGenerator() {
		hasher = new DefaultHasher();
		
		DhtHasher dhtHasher = new DefaultDhtHasher();
		
		byte[] firstTrans = {'f','i','r','s','t'};
		byte[] secondTrans = {'s','e','c','o','n','d'};
		byte[] thirdTrans = {'t','h','i','r','d'};
		byte[] fourthTrans = {'f','o','u','r','t','h'};
		byte[] fifthTrans = {'f','i','f','t','h'};
		byte[] sixthTrans = {'s','i','x','t','h'};
		byte[] seventhTrans = {'s','e','v','e','n','t','h'};
		byte[] eighthTrans = {'e','i','g','h','t','h'};
		byte[] ninethTrans = {'n','i','n','e','t','h'};
		byte[] tenthTrans = {'t','e','n','t','h'};
		byte[] eleventhTrans = {'e','l','e','v','e','n','t','h'};
		byte[] twelvethTrans = {'t','w','e','l','v','e','t','h'};
		byte[] thirteenthTrans = {'t','h','i','r','t','e','e','n','t','h'};
		byte[] fourteenthTrans = {'f','o','u','r','t','e','e','n','t','h'};
		byte[] fifteenthTrans = {'f','i','f','t','e','e','n','t','h'};
		byte[] sixteenthTrans = {'s','i','x','t','e','e','n','t','h'};
		DhtID firstId = dhtHasher.hashBytes(firstTrans);
		DhtID secondId = dhtHasher.hashBytes(secondTrans);
		DhtID thirdId = dhtHasher.hashBytes(thirdTrans);
		DhtID fourthId = dhtHasher.hashBytes(fourthTrans);
		DhtID fifthId = dhtHasher.hashBytes(fifthTrans);
		DhtID sixthId = dhtHasher.hashBytes(sixthTrans);
		DhtID seventhId = dhtHasher.hashBytes(seventhTrans);
		DhtID eightId = dhtHasher.hashBytes(eighthTrans);
		DhtID ninethId = dhtHasher.hashBytes(ninethTrans);
		DhtID tenthId = dhtHasher.hashBytes(tenthTrans);
		DhtID eleventhId = dhtHasher.hashBytes(eleventhTrans);
		DhtID twelvethId = dhtHasher.hashBytes(twelvethTrans);
		DhtID thirteenthId = dhtHasher.hashBytes(thirteenthTrans);
		DhtID fourteenthId = dhtHasher.hashBytes(fourteenthTrans);
		DhtID fiftheenthId = dhtHasher.hashBytes(fifteenthTrans);
		DhtID sixteenthId = dhtHasher.hashBytes(sixteenthTrans);
//		DhtAddress fistrAddress = null;
//		DhtAddress secondAddress = null;
//		DhtAddress thirdAddress = null;
//		DhtAddress fourthAddress = null;
//		DhtAddress fifthAddress = null;
//		DhtAddress sixthAddress = null;
//		DhtAddress seventhAddress = null;
//		DhtAddress eighthAddress = null;
//		DhtAddress ninethAddress = null;
//		DhtAddress tenthAddress = null;
//		DhtAddress eleventhAddress = null;
//		DhtAddress twelvethAddress = null;
//		DhtAddress thirteenthAddress = null;
//		DhtAddress fourteenthAddress = null;
//		DhtAddress fifteenthAddress = null;
//		DhtAddress sixteenthAddress = null;
//		try {
//			fistrAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.1"), 1024);
//			secondAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.2"), 1025);
//			thirdAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.3"), 1026);
//			fourthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.4"), 1027);
//			fifthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.5"), 1028);
//			sixthAddress  = new DefaultDhtAddress(InetAddress.getByName("10.0.0.6"), 1029);
//			seventhAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.7"), 1030);
//			eighthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.8"), 1031);
//			ninethAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.9"), 1032);
//			tenthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.10"), 1033);
//			eleventhAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.11"), 1034);
//			twelvethAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.12"), 1035);
//			thirteenthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.13"), 1036);
//			fourteenthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.14"), 1037);
//			fifteenthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.15"), 1038);
//			sixteenthAddress = new DefaultDhtAddress(InetAddress.getByName("10.0.0.16"), 1039);
//		} catch (IllegalArgumentException e) {
//			// Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnknownHostException e) {
//			// Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		transaction1 = new DefaultTransaction(firstId, fistrAddress);
//		transaction2 = new DefaultTransaction(secondId, secondAddress);
//		transaction3 = new DefaultTransaction(thirdId, thirdAddress);
//		transaction4 = new DefaultTransaction(fourthId, fourthAddress);
//		transaction5 = new DefaultTransaction(fifthId, fifthAddress);
//		transaction6 = new DefaultTransaction(sixthId, sixthAddress);
//		transaction7 = new DefaultTransaction(seventhId, seventhAddress);
//		transaction8 = new DefaultTransaction(eightId, eighthAddress);
//		transaction9 = new DefaultTransaction(ninethId, ninethAddress );
//		transaction10 = new DefaultTransaction(tenthId, tenthAddress);
//		transaction11 = new DefaultTransaction(eleventhId, eleventhAddress);
//		transaction12 = new DefaultTransaction(twelvethId, twelvethAddress);
//		transaction13 = new DefaultTransaction(thirteenthId, thirteenthAddress);
//		transaction14 =  new DefaultTransaction(fourteenthId, fourteenthAddress);
//		transaction15 = new DefaultTransaction(fiftheenthId, fifteenthAddress);
//		transaction16 = new DefaultTransaction(sixteenthId, sixteenthAddress);
		transaction1 = new DefaultTransaction(firstId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction2 = new DefaultTransaction(secondId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction3 = new DefaultTransaction(thirdId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction4 = new DefaultTransaction(fourthId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction5 = new DefaultTransaction(fifthId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction6 = new DefaultTransaction(sixthId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction7 = new DefaultTransaction(seventhId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction8 = new DefaultTransaction(eightId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction9 = new DefaultTransaction(ninethId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey() );
		transaction10 = new DefaultTransaction(tenthId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction11 = new DefaultTransaction(eleventhId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction12 = new DefaultTransaction(twelvethId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction13 = new DefaultTransaction(thirteenthId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction14 =  new DefaultTransaction(fourteenthId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction15 = new DefaultTransaction(fiftheenthId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		transaction16 = new DefaultTransaction(sixteenthId, 0L, 0L, DefaultTransaction.getNullTransaction().getPublicKey());
		
		transactions = new ArrayList<Transaction>();
		transactions.add(transaction1);
		transactions.add(transaction2);
		transactions.add(transaction3);
		transactions.add(transaction4);
		transactions.add(transaction5);
		transactions.add(transaction6);
		transactions.add(transaction7);
		transactions.add(transaction8);
		transactions.add(transaction9);
		transactions.add(transaction10);
		transactions.add(transaction11);
		transactions.add(transaction12);
		transactions.add(transaction13);
		transactions.add(transaction14);
		transactions.add(transaction15);
		transactions.add(transaction16);
	}

	public Transaction getTransaction1() {
		return transaction1;
	}
	
	public HashValue getTransaction1Hash() {
		return hasher.hashTransaction(transaction1);
	}

	public Transaction getTransaction2() {
		return transaction2;
	}
	
	public HashValue getTransaction2Hash() {
		return hasher.hashTransaction(transaction2);
	}

	public Transaction getTransaction3() {
		return transaction3;
	}
	
	public HashValue getTransaction3Hash() {
		return hasher.hashTransaction(transaction3);
	}

	public Transaction getTransaction4() {
		return transaction4;
	}
	
	public HashValue getTransaction4Hash() {
		return hasher.hashTransaction(transaction4);
	}

	public Transaction getTransaction5() {
		return transaction5;
	}
	
	public HashValue getTransaction5Hash() {
		return hasher.hashTransaction(transaction5);
	}

	public Transaction getTransaction6() {
		return transaction6;
	}

	public Transaction getTransaction7() {
		return transaction7;
	}
	
	public HashValue getTransaction7Hash() {
		return hasher.hashTransaction(transaction7);
	}

	public Transaction getTransaction8() {
		return transaction8;
	}
	
	public HashValue getTransaction8Hash() {
		return hasher.hashTransaction(transaction8);
	}

	public Transaction getTransaction9() {
		return transaction9;
	}
	
	public HashValue getTransaction9Hash() {
		return hasher.hashTransaction(transaction9);
	}

	public Transaction getTransaction10() {
		return transaction10;
	}
	
	public HashValue getTransaction10Hash() {
		return hasher.hashTransaction(transaction10);
	}

	public Transaction getTransaction11() {
		return transaction11;
	}
	
	public HashValue getTransaction11Hash() {
		return hasher.hashTransaction(transaction11);
	}

	public Transaction getTransaction12() {
		return transaction12;
	}
	
	public HashValue getTransaction12Hash() {
		return hasher.hashTransaction(transaction12);
	}

	public Transaction getTransaction13() {
		return transaction13;
	}
	
	public HashValue getTransaction13Hash() {
		return hasher.hashTransaction(transaction13);
	}

	public Transaction getTransaction14() {
		return transaction14;
	}
	
	public HashValue getTransaction14Hash() {
		return hasher.hashTransaction(transaction14);
	}

	public Transaction getTransaction15() {
		return transaction15;
	}
	
	public HashValue getTransaction15Hash() {
		return hasher.hashTransaction(transaction15);
	}

	public Transaction getTransaction16() {
		return transaction16;
	}
	
	public HashValue getTransaction16Hash() {
		return hasher.hashTransaction(transaction16);
	}

	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}

	public Hasher getHasher() {
		return hasher;
	}
}
