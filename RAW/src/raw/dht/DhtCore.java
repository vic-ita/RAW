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

import java.security.KeyPair;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import raw.blockChain.api.Block;
import raw.blockChain.api.Transaction;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.exceptions.IncompleteNodeExtendedException;

/**
 * Objects implementing this class should be
 * the main class to execute all DHT related
 * activities.
 * 
 * @author vic
 *
 */
public interface DhtCore {
	
	/**
	 * Searches in the <b>local</b> {@link RoutingTable} a collection of the 
	 * KNOWN {@link DhtNodeExtended} closest to a given
	 * {@link DhtID}
	 * 
	 * @param id the searched {@link DhtID}
	 * @return a {@link Collection} of {@link DhtNodeExtended} closest to <tt>id</tt>
	 */
	public Collection<DhtNodeExtended> lookupInTable(DhtID id);
	
	/**
	 * Performs a network lookup to find the set of
	 * {@link DhtNodeExtended} closest to a given {@link DhtID}
	 * 
	 * @param id the searched {@link DhtID}
	 * @return a {@link Collection} of {@link DhtNodeExtended} closest to <tt>id</tt>
	 */
	public Collection<DhtNodeExtended> lookup(DhtID id);
	
	/**
	 * This method accesses the {@link RoutingTable} of this 
	 * DHT node.
	 * 
	 * @return a reference to this node's {@link RoutingTable}
	 */
	public RoutingTable getRoutingTable();
	
	/**
	 * Attempt to store in the dht a couple {@link DhtKey} - {@link DhtValue} .
	 * 
	 * @param key a {@link DhtKey}
	 * @param value the {@link DhtValue} associated to <tt>key</tT>
	 * @return <tt>true</tt> if store was successful 
	 */
	public boolean store(DhtKey key, DhtValue value);
	
	/**
	 * Searches the {@link DhtValue}s associated to a given {@link DhtKey}. If
	 * <tt>key</tT> was never stored in the dht <tt>null</tt> is to be returned.
	 * 
	 * @param key the searched {@link DhtKey}
	 * @return the {@link DhtValue}s associated to <tt>key</tt> or <tt>null</tt> if no record is present in the dht
	 */
	public Collection<DhtValue> search(DhtKey key);
	
	/**
	 * @return a reference to the thread pool {@link ExecutorService} of this class
	 */
	public ExecutorService getThreadPool();
	
	/**
	 * @return a simple {@link DhtNode} representation of this node
	 */
	public DhtNode getNode();
	
	/**
	 * @return the {@link DhtNodeExtended} representation of this node
	 * @throws IncoherentTransactionException 
	 * @throws IncompleteNodeExtendedException 
	 */
	public DhtNodeExtended getNodeExtended() throws IncoherentTransactionException, IncompleteNodeExtendedException;
	
	/**
	 * @return the {@link KeyPair} to authenticate this node
	 */
	public KeyPair getKeyPair();
	
	/**
	 * @return the {@link DhtKeyHolder} for this node
	 */
	public DhtKeyHolder getKeyHolder();
	
	/**
	 * @return the current random seed to be used DHT {@link DhtHasher}
	 */
	public byte[] getCurrentSeed();
	
	/**
	 * @return a {@link DhtHasher} set to use {@link DhtCore#getCurrentSeed()} random seed
	 */
	public DhtHasher getCurrentHasher();
	
	/**
	 * Returns a {@link DhtHasher} set up with the seed value
	 * obtained from a specified block. The block number
	 * passed as argument <b>must</b> identify a valid seed,
	 * otherwise an {@link IllegalArgumentException} will be thrown. 
	 * 
	 * @param seedBlockNumber a seed {@link Block} number
	 * @return a configured {@link DhtHasher}
	 * @throws IllegalArgumentException if <code>seedBlockNumber</code> does not identify a "seed" {@link Block}
	 */
	public DhtHasher getHasherFromSeedNumber(long seedBlockNumber) throws IllegalArgumentException;
	
	/**
	 * Checks if a {@link DhtNode} is old enough (but not too much old)
	 * and authenticate it through its {@link Transaction}.
	 * 
	 * @param node a {@link DhtNodeExtended} to check 
	 * @return <code>true</code> if <code>node</code> is to be considered an "old worker", <code>false</code> otherwise
	 */
	public boolean isOldWorker(DhtNodeExtended node);
	
	/**
	 * @return the response of {@link DhtCore#isOld(DhtNode, byte[], long)} applied to <code>this</code> node
	 */
	public boolean isThisNodeOld();
	
	/**
	 * Starts this client of the dht
	 */
	public void start();
	
	/**
	 * Stops this client of the dht
	 */
	public void stop();
	
	/**
	 * @return <tt>true</tt> if dht is running, false otherwise
	 */
	public boolean isStarted();
	
	/**
	 * This method returns the number of the {@link Block} to be used
	 * by other DHT nodes in {@link DhtCore#isOld(DhtNode, long)} to
	 * authenticate this node.
	 * If this DHT node is still not fully initialized this method
	 * will return -1.
	 * 
	 * @return this node {@link Transaction}'s {@link Block} number or <code>-1</code> if this node is not yet initialized.
	 */
	public long getBlockNumber();
	
	/**
	 * Through this method a given {@link DhtNode} could be
	 * pinged without need of accessing internal pinger.
	 * 
	 * @param node the {@link DhtNode} to be pinged
	 * @return <code>true</code> if <code>node</code> answered to ping, <code>false</code> otherwise
	 */
	public boolean pingNode(DhtNodeExtended node);

}
