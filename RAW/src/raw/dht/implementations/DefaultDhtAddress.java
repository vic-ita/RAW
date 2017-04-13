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
package raw.dht.implementations;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.dht.DhtAddress;

/**
 * Objects of this class implement {@link DhtAddress}
 * wrapper for physical addresses used in the DHT.
 * 
 * @author vic
 *
 */
public class DefaultDhtAddress implements DhtAddress {

	private InetAddress address;
	private int udpPort;
	private int tcpPort;
	
	
	/**
	 * Generated random serial
	 */
	private static final long serialVersionUID = 7914143027648273175L;
	
	/**
	 * Build an instance of a {@link DhtAddress} object.
	 * 
	 * @param address the {@link InetAddress} physical address
	 * @param port the port (both TCP and UPD) used by this address
	 * @throws IllegalArgumentException if <tt>port</tt> is not in range [0 - 65535]
	 */
	public DefaultDhtAddress(InetAddress address, int port) throws IllegalArgumentException{
		this(address, port, port);
	}
	
	/**
	 * Build an instance of a {@link DhtAddress} object.
	 * 
	 * @param address the {@link InetAddress} physical address
	 * @param udpPort the UDP port used by this address
	 * @param tcpPort the TCP port used by this address
	 * @throws IllegalArgumentException if <tt>tcpPort</tt> or <tt>udpPort</tt> are not in range [0 - 65535]
	 */
	public DefaultDhtAddress(InetAddress address, int udpPort, int tcpPort) throws IllegalArgumentException{
		checkPort(udpPort);
		checkPort(tcpPort);
		this.address = address;
		this.udpPort = udpPort;
		this.tcpPort = tcpPort;
	}
	
	/**
	 * Build an instance of a {@link DhtAddress} object.
	 * 
	 * @param address the raw IP address for this physical address
	 * @param port the port (both TCP and UPD) used by this address
	 * @throws UnknownHostException if IP address is of illegal length
	 * @throws IllegalArgumentException if <tt>port</tt> is not in range [0 - 65535]
	 */
	public DefaultDhtAddress(byte[] address, int port) throws UnknownHostException, IllegalArgumentException {
		this(address, port, port);
	}
	
	/**
	 * Build an instance of a {@link DhtAddress} object.
	 * 
	 * @param address the raw IP address for this physical address
	 * @param udpPort the UDP port used by this address
	 * @param tcpPort the TCP port used by this address
	 * @throws UnknownHostException if IP address is of illegal length
	 * @throws IllegalArgumentException if <tt>port</tt> is not in range [0 - 65535]
	 */
	public DefaultDhtAddress(byte[] address, int udpPort, int tcpPort) throws UnknownHostException, IllegalArgumentException {
		InetAddress addr = InetAddress.getByAddress(address);
		checkPort(udpPort);
		checkPort(tcpPort);
		this.address = addr;
		this.udpPort = udpPort;
		this.tcpPort = tcpPort;
	}
	
	/**
	 * Build an instance of a {@link DhtAddress} object.
	 * 
	 * @param address the canonical IP address string representation for this physical address
	 * @param port the port (both TCP and UPD) used by this address
	 * @throws UnknownHostException if IP address is of illegal length
	 * @throws IllegalArgumentException if <tt>udpPort</tt> is not in range [0 - 65535]
	 */
	public DefaultDhtAddress(String address, int port) throws UnknownHostException, IllegalArgumentException {
		this(address, port, port);
	}
	
	/**
	 * Build an instance of a {@link DhtAddress} object.
	 * 
	 * @param address the canonical IP address string representation for this physical address
	 * @param udpPort the UDP port used by this address
	 * @param tcpPort the TCP port used by this address
	 * @throws UnknownHostException if IP address is of illegal length
	 * @throws IllegalArgumentException if <tt>udpPort</tt> is not in range [0 - 65535]
	 */
	public DefaultDhtAddress(String address, int udpPort, int tcpPort) throws UnknownHostException, IllegalArgumentException {
		InetAddress addr = InetAddress.getByName(address);
		checkPort(udpPort);
		checkPort(tcpPort);
		this.address = addr;
		this.udpPort = udpPort;
		this.tcpPort = tcpPort;
	}
	
	/**
	 * Rise exception if udpPort is invalid. To be used in constructors.
	 * 
	 * @throws IllegalArgumentException if udpPort is less than 0 or more than 65535 
	 */
	private void checkPort(int port) throws IllegalArgumentException{
		if(port<0 || port >65535){
			throw new IllegalArgumentException("Port number specified is not valid (got "+port+").");
		}
	}

	/* (non-Javadoc)
	 * @see raw.dht.interfaces.DhtAddress#getAddress()
	 */
	@Override
	public InetAddress getAddress() {
		return address;
	}

	/* (non-Javadoc)
	 * @see raw.dht.interfaces.DhtAddress#getPort()
	 */
	@Override
	public int getUdpPort() {
		return udpPort;
	}

	/* (non-Javadoc)
	 * @see raw.dht.interfaces.DhtAddress#getSocketAddress()
	 */
	@Override
	public InetSocketAddress getUdpSocketAddress() {
		return new InetSocketAddress(address, udpPort);
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtAddress#getTcpPort()
	 */
	@Override
	public int getTcpPort() {
		return tcpPort;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtAddress#getTcpSocketAddress()
	 */
	@Override
	public InetSocketAddress getTcpSocketAddress() {
		return new InetSocketAddress(address, tcpPort);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DhtAddress)){
			return false;
		}
		
		DhtAddress other = (DhtAddress) obj;
		
		if(!Arrays.equals(address.getAddress(), other.getAddress().getAddress())){
			return false;
		}
		
		if(tcpPort != other.getTcpPort()){
			return false;
		}

		return (udpPort == other.getUdpPort());
	}

	@Override
	public String toString() {
		return getUdpSocketAddress().getHostString()+":"+getUdpPort()+"+"+getTcpPort();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(address).
		append(udpPort).
		append(tcpPort);
		return builder.toHashCode();
	}
	
}
