/*******************************************************************************
 * Copyright (c) 2003, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - extracted from TerminalProperties 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.connector;

public class TelnetProperties {
	private final NetworkPortMap fNetworkPortMap;
	private final String fDefaultHost;
	private final String fDefaultNetworkPort;

	public TelnetProperties() {
		fNetworkPortMap = new NetworkPortMap();
		fDefaultNetworkPort = fNetworkPortMap.getDefaultNetworkPort();
		fDefaultHost = ""; //$NON-NLS-1$
	}

	public String getDefaultHost() {
		return fDefaultHost;
	}

	public String getDefaultNetworkPort() {
		return fDefaultNetworkPort;
	}

	public NetworkPortMap getNetworkPortMap() {
		// TODO Auto-generated method stub
		return fNetworkPortMap;
	}
}
