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
package raw.blockChain.services.thickNode;

import java.util.concurrent.Callable;

import raw.blockChain.services.CommonNode;
import raw.blockChain.services.miner.LocalThickNodeListener;

/**
 * Objects implementing this interface will
 * grant a BlockChain "thick node" service
 * that will maintain a full BlockChain copy
 * with the other thick nodes of the network. 
 * 
 * @author vic
 *
 */
public interface ThickNode extends Callable<Void>, LocalMinerSeriviceListener, CommonNode {
	
	public static final int PING_MILLISECONDS_INTERTIME = 30000;
	public static final int PING_MILLISECONDS_VARIABILITY = 5000;
	
	/**
	 * Calling this method will halt this service.
	 * 
	 * @return <tt>true</tt> if service has regularly halted
	 */
	public boolean stopService();
	
	/**
	 * Register a {@link LocalThickNodeListener}.
	 * 
	 * @return <tt>true</tt> if registration went well
	 */
	public boolean reagisterThickNodeListener(LocalThickNodeListener listener);
}
