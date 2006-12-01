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

import org.eclipse.tm.terminal.ISettingsStore;
import org.eclipse.tm.terminal.ITerminalConnector;

public class TelnetSettings implements ITelnetSettings {
    protected String fHost;
    protected String fNetworkPort;
    protected String fTimeout;
    private final TelnetProperties fProperties=new TelnetProperties();
	public String getHost() {
		return fHost;
	}

	public void setHost(String strHost) {
		fHost = strHost;
	}

	public String getNetworkPortString() {
		return fNetworkPort;
	}

	public int getNetworkPort() {
		try {
			return Integer.parseInt(fNetworkPort);
		} catch (NumberFormatException numberFormatException) {
			return 1313;
		}
	}

	public void setNetworkPort(String strNetworkPort) {
		fNetworkPort = strNetworkPort;
	}

	public String getStatusString(String strConnected) {
		return " (" + //$NON-NLS-1$
			getHost() + ":" + //$NON-NLS-1$
			getNetworkPortString() + " - " + //$NON-NLS-1$
			strConnected + ")"; //$NON-NLS-1$
	}


	public ITerminalConnector makeConnector() {
		return new TelnetConnector(this);
	}


	public void load(ISettingsStore store) {
		fHost = store.get("Host", fProperties.getDefaultHost());//$NON-NLS-1$
		fNetworkPort = store.get("NetworkPort", fProperties.getDefaultNetworkPort());//$NON-NLS-1$
	}


	public void save(ISettingsStore store) {
		store.put("Host", fHost);//$NON-NLS-1$
		store.put("NetworkPort", fNetworkPort);//$NON-NLS-1$
	}


	public TelnetProperties getProperties() {
		return fProperties;
	}
	public int getTimeout() {
		try {
			return Integer.parseInt(fTimeout);
		} catch (NumberFormatException numberFormatException) {
			return 10;
		}
	}
	public String getTimeoutString() {
		return fTimeout;
	}

	public void setTimeout(String timeout) {
		fTimeout = timeout;
	}
}
