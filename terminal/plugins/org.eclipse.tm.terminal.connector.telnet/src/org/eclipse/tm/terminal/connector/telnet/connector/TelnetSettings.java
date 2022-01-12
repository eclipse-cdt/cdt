/*******************************************************************************
 * Copyright (c) 2003, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - extracted from TerminalSettings
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.connector;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

/**
 * @noreference This class is not intended to be referenced by clients.
 */
public class TelnetSettings implements ITelnetSettings {
	protected String fHost;
	protected String fNetworkPort;
	protected String fTimeout;
	protected String fEndOfLine = EOL_CRNUL;
	private final TelnetProperties fProperties = new TelnetProperties();

	@Override
	public String getHost() {
		return fHost;
	}

	public void setHost(String strHost) {
		fHost = strHost;
	}

	public String getNetworkPortString() {
		return fNetworkPort;
	}

	@Override
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

	@Override
	public String getSummary() {
		return getHost() + ":" + getNetworkPortString(); //$NON-NLS-1$
	}

	@Override
	public void load(ISettingsStore store) {
		fHost = store.get("Host", fProperties.getDefaultHost());//$NON-NLS-1$
		fNetworkPort = store.get("NetworkPort", fProperties.getDefaultNetworkPort());//$NON-NLS-1$
		fTimeout = store.get("Timeout", "10");//$NON-NLS-1$ //$NON-NLS-2$
		fEndOfLine = store.get("EndOfLine", EOL_CRNUL);//$NON-NLS-1$
	}

	@Override
	public void save(ISettingsStore store) {
		store.put("Host", fHost);//$NON-NLS-1$
		store.put("NetworkPort", fNetworkPort);//$NON-NLS-1$
		store.put("Timeout", fTimeout);//$NON-NLS-1$
		store.put("EndOfLine", fEndOfLine);//$NON-NLS-1$
	}

	public TelnetProperties getProperties() {
		return fProperties;
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

	public void setEndOfLine(String eol) {
		fEndOfLine = EOL_CRLF.equals(eol) ? EOL_CRLF : EOL_CRNUL;
	}

	@Override
	public String getEndOfLine() {
		return fEndOfLine;
	}
}
