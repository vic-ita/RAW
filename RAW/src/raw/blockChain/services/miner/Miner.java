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
package raw.blockChain.services.miner;

import java.util.concurrent.Callable;

import raw.blockChain.api.BlockMinerListener;
import raw.blockChain.api.Transaction;
import raw.blockChain.services.thickNode.LocalMinerSeriviceListener;
import raw.blockChain.services.thickNode.ThickNode;

/**
 * Objects implementing this interface will
 * grant a BlockChain miner service.
 * 
 * @author vic
 *
 */
public interface Miner extends Callable<Void>, LocalThickNodeListener, BlockMinerListener {
	
	public static final int NODE_SEARCH_MILLISECONDS_INTERTIME = 10000;
	public static final int NODE_SEARCH_MILLISECONDS_VARIABILITY = 3000;
	
	public static final int MINE_ON_NULL_TRANSACTION_MILLISECONDS = 8000;

	/**
	 * Calling this method will halt this service.
	 * 
	 * @return <tt>true</tt> if service has regularly halted
	 */
	public boolean stopService();
	
	/**
	 * Register a {@link LocalMinerSeriviceListener} for a local
	 * {@link ThickNode}.
	 * 
	 * @param listener the listener
	 * @return <tt>true</tt> if listener was registered
	 */
	public boolean registerListener(LocalMinerSeriviceListener listener);
	
	/**
	 * Submit a {@link Transaction} to this {@link Miner} so that
	 * it will be incorporated in the block chain.
	 * 
	 * @param transaction the {@link Transaction} submitted
	 */
	public void submitTransaction(Transaction transaction);
	
}
