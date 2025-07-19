/*******************************************************************************
 * Copyright (c) 2015, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.remote.internal;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.terminal.connector.ISettingsStore;
import org.eclipse.terminal.connector.ITerminalControl;
import org.eclipse.terminal.connector.NullSettingsStore;
import org.eclipse.terminal.connector.TerminalState;
import org.eclipse.terminal.connector.provider.AbstractTerminalConnector;
import org.eclipse.tm.terminal.connector.remote.IRemoteSettings;

public class RemoteConnector extends AbstractTerminalConnector {
	private OutputStream fOutputStream;
	private InputStream fInputStream;
	private RemoteConnectionManager fConnection;
	private int fWidth;
	private int fHeight;
	private final RemoteSettings fSettings;

	public RemoteConnector() {
		this(new RemoteSettings());
	}

	public RemoteConnector(RemoteSettings settings) {
		fSettings = settings;
	}

	@Override
	public void connect(ITerminalControl control) {
		super.connect(control);
		fControl.setState(TerminalState.CONNECTING);
		fConnection = new RemoteConnectionManager(this, control);
		fConnection.schedule();
	}

	@Override
	public synchronized void doDisconnect() {
		fConnection.cancel();
	}

	public InputStream getInputStream() {
		return fInputStream;
	}

	@Override
	public String getSettingsSummary() {
		return fSettings.getSummary();
	}

	/**
	 * Return the Remote Settings.
	 *
	 * @return the settings for a concrete connection.
	 */
	public IRemoteSettings getRemoteSettings() {
		return fSettings;
	}

	@Override
	public OutputStream getTerminalToRemoteStream() {
		return fOutputStream;
	}

	@Override
	public void load(ISettingsStore store) {
		fSettings.load(store);
	}

	@Override
	public void setDefaultSettings() {
		fSettings.load(new NullSettingsStore());
	}

	@Override
	public void save(ISettingsStore store) {
		fSettings.save(store);
	}

	public void setInputStream(InputStream inputStream) {
		fInputStream = inputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		fOutputStream = outputStream;
	}

	@Override
	public void setTerminalSize(int newWidth, int newHeight) {
		if (fConnection != null && (newWidth != fWidth || newHeight != fHeight)) {
			// avoid excessive communications due to change size requests by caching previous size
			fConnection.setTerminalSize(newWidth, newHeight, 8 * newWidth, 8 * newHeight);
			fWidth = newWidth;
			fHeight = newHeight;
		}
	}
}
