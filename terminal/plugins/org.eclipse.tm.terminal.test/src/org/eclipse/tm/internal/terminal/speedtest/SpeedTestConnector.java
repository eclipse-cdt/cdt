/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [225853][api] Provide more default functionality in TerminalConnectorImpl
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.speedtest;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.NullSettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

public class SpeedTestConnector extends TerminalConnectorImpl {
	final SpeedTestSettings fSettings = new SpeedTestSettings();
	InputStream fInputStream;
	OutputStream fOutputStream;
	SpeedTestConnection fConnection;

	public SpeedTestConnector() {
	}

	@Override
	synchronized public void connect(ITerminalControl control) {
		super.connect(control);
		fControl.setState(TerminalState.CONNECTING);
		String file = fSettings.getInputFile();
		try {
			fInputStream = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			disconnect();
			fControl.setMsg(file + ": " + e.getLocalizedMessage());
			return;
		}
		fOutputStream = System.out;
		fControl.setTerminalTitle(fSettings.getInputFile());
		fConnection = new SpeedTestConnection(fInputStream, fSettings, fControl);
		fConnection.start();
	}

	@Override
	synchronized public void doDisconnect() {
		if (fConnection != null) {
			fConnection.interrupt();
			fConnection = null;
		}
		if (fInputStream != null) {
			try {
				fInputStream.close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}
		fInputStream = null;
		if (fOutputStream != null) {
			try {
				fOutputStream.close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}
		fOutputStream = null;
	}

	synchronized public InputStream getInputStream() {
		return fInputStream;
	}

	@Override
	synchronized public OutputStream getTerminalToRemoteStream() {
		return fOutputStream;
	}

	@Override
	public String getSettingsSummary() {
		return fSettings.getInputFile();
	}

	@Override
	public void setDefaultSettings() {
		fSettings.load(new NullSettingsStore());
	}

	@Override
	public void load(ISettingsStore store) {
		fSettings.load(store);
	}

	@Override
	public void save(ISettingsStore store) {
		fSettings.save(store);
	}

	@Override
	public void setTerminalSize(int newWidth, int newHeight) {
	}

}
