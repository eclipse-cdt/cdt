/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/

package org.eclipse.tm.terminal.internal.telnet;



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
