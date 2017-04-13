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
/**
 * 
 */
package raw.blockChain.services.thinNode;

import java.util.concurrent.Callable;

import raw.blockChain.services.CommonNode;
import raw.blockChain.services.thickNode.ThickNode;

/**
 * If a node is not running a {@link ThickNode} then
 * it should run a {@link ThinNode} to be able to
 * execute basic operations with the block chain.
 * 
 * @author vic
 *
 */
public interface ThinNode extends Callable<Void>, CommonNode {
	
	public static final int PING_MILLISECONDS_INTERTIME = 10000;
	public static final int PING_MILLISECONDS_VARIABILITY = 3000;
	
	public static final int THICK_NODE_CONSENSUS = 3;
	
	/**
	 * Calling this method will halt this service.
	 * 
	 * @return <tt>true</tt> if service has regularly halted
	 */
	public boolean stopService();

}
