/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Mikhail Kalugin <fourdman@xored.com> - [201867] Improve Terminal SSH connection summary string
 * Johnson Ma (Wind River) - [218880] Add UI setting for ssh keepalives
 *  Bryan Hunt - [313991] cannot programatically pass password to SshConnector
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.ssh.connector;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

public class SshSettings implements ISshSettings {
    protected String fHost;
    protected String fUser;
    protected String fPassword;
    protected String fPort;
    protected String fTimeout;
    protected String fKeepalive;
	@Override
    public String getHost() {
		return fHost;
	}

	public void setHost(String strHost) {
		fHost = strHost;
	}

	@Override
    public String getSummary() {
		String settings = getUser()+'@'+getHost();
		if(getPort()!=22) {
			settings += ':' + getPort();
		}
		return settings;
	}

	@Override
    public void load(ISettingsStore store) {
		fHost = store.get("Host", "");//$NON-NLS-1$ //$NON-NLS-2$
		fUser = store.get("User", "");//$NON-NLS-1$ //$NON-NLS-2$
		// ISettingsStore providers have to make sure that
		// the password is not saved in some as plain text
		// on disk. [bug 313991]
		fPassword = store.get("Password", "");//$NON-NLS-1$ //$NON-NLS-2$
		fPort = store.get("Port", "22");//$NON-NLS-1$ //$NON-NLS-2$
		fTimeout = store.get("Timeout", "0");//$NON-NLS-1$ //$NON-NLS-2$
		fKeepalive = store.get("Keepalive", "300");//$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
    public void save(ISettingsStore store) {
		store.put("Host", fHost);//$NON-NLS-1$
		store.put("User", fUser);//$NON-NLS-1$
		store.put("Port", fPort);//$NON-NLS-1$
		// We do *not* store the password in the settings because
		// this can cause the password to be stored as plain text
		// in some settings file
		store.put("Timeout", fTimeout);//$NON-NLS-1$
		store.put("Keepalive", fKeepalive);//$NON-NLS-1$
	}


	@Override
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

	@Override
    public int getKeepalive() {
		try {
			return Integer.parseInt(fKeepalive);
		} catch (NumberFormatException numberFormatException) {
			return 300;
		}
	}
	public String getKeepaliveString() {
		return fKeepalive;
	}

	public void setKeepalive(String keepalive) {
		fKeepalive = keepalive;
	}

	@Override
    public String getUser() {
		return fUser;
	}

	public void setUser(String user) {
		fUser = user;
	}
	@Override
    public int getPort() {
		try {
			return Integer.parseInt(fPort);
		} catch (NumberFormatException numberFormatException) {
			return 22;
		}
	}

	public String getPortString() {
		return fPort;
	}

	public void setPort(String port) {
		fPort = port;
	}

	@Override
    public String getPassword() {
		return fPassword;
	}

	public void setPassword(String password) {
		fPassword = password;
	}
}
