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

import raw.blockChain.api.Block;
import raw.blockChain.api.Transaction;
import raw.dht.messages.implementations.udp.PingMessage;
import raw.dht.utils.nodesInfoServer.NodesInfoServer;

/**
 * Collection of constants to be used by
 * the DHT.
 * 
 * @author vic
 *
 */
public interface DhtConstants {

	/**
	 * size of the K-buckets
	 */
	public int K_SIZE = 20;
//	public int K_SIZE = 50;
	
	/**
	 * number of nodes to be returned upon a query
	 */
	public int ALPHA_SIZE = 3;
//	public int ALPHA_SIZE = 10;
	
	/**
	 * The maximum number of {@link DhtValue} to be answered upon a query
	 */
	public int NUMBER_OF_VALUES_IN_REPLY = 300;
	
	/**
	 * milliseconds to wait in DHT sockets 
	 */
	public int TIMEOUT_MILLISECONDS = 30 * 1000;
	
	/**
	 * milliseconds between sending out of two {@link PingMessage}s
	 */
	public int PING_INTERTIME = 30 * 1000;
	
	/**
	 * maximum lenght of {@link DhtKey#getKeyString()}
	 */
	public int MAX_KEY_STRING_SIZE = 150;
	
	/**
	 * The minimum depth a {@link Block} must be in the
	 * chain to be acceptable for the DHT.
	 */
	public long MINIMUM_ACCEPTABLE_CHAIN_DEPTH = 5;
	
	/**
	 * A seed {@link Block} number should be an integer
	 * multiple of this constant.
	 */
	public long SEED_BLOCK_NUMBER_MODULE = 7;
	
	/**
	 * The number of seeds defining the "life span"
	 * of a {@link Transaction}. (greek "eta")
	 */
	public long TRANSACTION_VALIDITY_NUMBER_OF_SEEDS = 3;
	
	/**
	 * The number of {@link DhtNode}s returned as response
	 * by {@link NodesInfoServer}.
	 */
	public int NUMBER_OF_NODES_INFO = 20;
	
	/**
	 * The number of milliseconds that
	 * {@link DhtSearcher#startKeysMigrator()} job will wait
	 * before checking for more suitable {@link DhtNode}s
	 */
	public int KEYS_MIGRATION_CYCLE_MILLISECONDS = 2 * 60 * 1000;
	
	/**
	 * the hardness of the proof of work is eased by this quantity.
	 */
	public int PROOF_OF_WORK_SIMPLIFICATION = 5;
	
	/**
	 * time between two consecutive seed-retrieval access 
	 * to the block chain (thus caching the accesses)
	 */
	public int SEED_REQUESTS_REFRESH_MILLISECONDS = 10 * 1000;
	
}
