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

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Objects from this interface wrap
 * the physical address to a dht node.
 * 
 * @author vic
 *
 */
public interface DhtAddress extends Serializable{

	/**
	 * @return the {@link InetAddress} of this node
	 */
	public InetAddress getAddress();
	
	/**
	 * @return an integer representation of the UDP port number [0 - 65535] of this node.
	 */
	public int getUdpPort();
	
	/**
	 * @return {@link InetSocketAddress} complete of address and UDP port number
	 */
	public InetSocketAddress getUdpSocketAddress();
	
	/**
	 * @return an integer representation of the TCP port number [0 - 65535] of this node.
	 */
	public int getTcpPort();
	
	/**
	 * @return {@link InetSocketAddress} complete of address and TCP port number
	 */
	public InetSocketAddress getTcpSocketAddress();
	
}
