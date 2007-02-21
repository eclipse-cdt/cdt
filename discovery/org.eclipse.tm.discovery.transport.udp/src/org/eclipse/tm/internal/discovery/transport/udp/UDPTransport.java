/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orús (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.internal.discovery.transport.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.tm.discovery.transport.ITransport;

/**
 * UPD/IP transport implementation
 */

public class UDPTransport implements ITransport {

	// Default multicast DNS port
	public static int MDNS_PORT = 5353;
	
	// DNS-SD multicast address 
	public static String MULTICAST_ADDRESS = "224.0.0.251"; //$NON-NLS-1$
	
	private DatagramSocket socket = null;

	private InetAddress server = null;
	
	private int port = MDNS_PORT;

	/**
	 * Constructor for transport for UDP/IP
	 */
	
	public UDPTransport() {
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.tm.discovery.transport.ITransport#setTargetAddress(java.lang.String)
	 */
	public void setTargetAddress(String address) throws UnknownHostException
	{
		
		Pattern pattern = Pattern.compile("([^:]*)(:(\\d+))?"); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(address);
		if (matcher.matches()) {
			server = InetAddress.getByName(matcher.group(1));
			if (server.isLoopbackAddress()) {
				server = InetAddress.getLocalHost();
			}

			if (matcher.groupCount() == 3 && matcher.group(3) != null)
				port = Integer.parseInt(matcher.group(3));
		}
		
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.tm.discovery.transport.ITransport#setTimeOut(int)
	 */
	public void setTimeOut(int timeOut) throws SocketException
	{
		socket.setSoTimeout(timeOut);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.tm.discovery.transport.ITransport#send(byte[])
	 */
	public void send(byte[] packet) throws IOException {
		DatagramPacket packetOut = new DatagramPacket(packet, packet.length,server, port);
		socket.send(packetOut);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.tm.discovery.transport.ITransport#receive(byte[])
	 */
	public String receive(byte[] packet) throws IOException {
		DatagramPacket packetIn = new DatagramPacket(packet, packet.length);
		socket.receive(packetIn);

		return packetIn.getAddress().getHostAddress();
	}
	
}
