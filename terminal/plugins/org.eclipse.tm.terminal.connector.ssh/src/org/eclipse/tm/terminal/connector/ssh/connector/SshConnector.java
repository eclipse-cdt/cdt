/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [225792] Rename SshConnector.getTelnetSettings() to getSshSettings()
 * Martin Oberhuber (Wind River) - [225853][api] Provide more default functionality in TerminalConnectorImpl
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.ssh.connector;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.NullSettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;

public class SshConnector extends TerminalConnectorImpl {
	private OutputStream fOutputStream;
	private InputStream fInputStream;
	private JSch fJsch;
	private ChannelShell fChannel;
	private SshConnection fConnection;
	private final SshSettings fSettings;
	private int fWidth;
	private int fHeight;

	public SshConnector() {
		this(new SshSettings());
	}

	public SshConnector(SshSettings settings) {
		fSettings = settings;
	}

	@Override
	public void initialize() throws Exception {
		fJsch = new JSch();
	}

	@Override
	public void connect(ITerminalControl control) {
		super.connect(control);
		fConnection = new SshConnection(this, control);
		fConnection.start();
	}

	@Override
	synchronized public void doDisconnect() {
		fConnection.disconnect();
		if (getInputStream() != null) {
			try {
				getInputStream().close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}

		if (getTerminalToRemoteStream() != null) {
			try {
				getTerminalToRemoteStream().close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}
	}

	@Override
	public void setTerminalSize(int newWidth, int newHeight) {
		if (fChannel != null && (newWidth != fWidth || newHeight != fHeight)) {
			//avoid excessive communications due to change size requests by caching previous size
			fChannel.setPtySize(newWidth, newHeight, 8 * newWidth, 8 * newHeight);
			fWidth = newWidth;
			fHeight = newHeight;
		}
	}

	public InputStream getInputStream() {
		return fInputStream;
	}

	@Override
	public OutputStream getTerminalToRemoteStream() {
		return fOutputStream;
	}

	void setInputStream(InputStream inputStream) {
		fInputStream = inputStream;
	}

	void setOutputStream(OutputStream outputStream) {
		fOutputStream = outputStream;
	}

	/**
	 * Return the SSH Settings.
	 *
	 * @return the settings for a concrete connection.
	 * @since org.eclipse.tm.terminal.ssh 2.0 renamed from getTelnetSettings()
	 */
	public ISshSettings getSshSettings() {
		return fSettings;
	}

	@Override
	public void setDefaultSettings() {
		fSettings.load(new NullSettingsStore());
	}

	@Override
	public String getSettingsSummary() {
		return fSettings.getSummary();
	}

	@Override
	public void load(ISettingsStore store) {
		fSettings.load(store);
	}

	@Override
	public void save(ISettingsStore store) {
		fSettings.save(store);
	}

	protected JSch getJsch() {
		return fJsch;
	}

	ChannelShell getChannel() {
		return fChannel;
	}

	void setChannel(ChannelShell channel) {
		fChannel = channel;
		fWidth = -1;
		fHeight = -1;
	}
}
