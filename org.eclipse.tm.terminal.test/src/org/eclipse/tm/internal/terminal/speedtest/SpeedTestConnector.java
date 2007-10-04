/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.speedtest;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public class SpeedTestConnector implements ITerminalConnector {
	final SpeedTestSettings fSettings=new SpeedTestSettings();
	InputStream fInputStream;
	OutputStream fOutputStream;
	ITerminalControl fControl;
	SpeedTestConnection fConnection;
	public SpeedTestConnector() {
	}
	synchronized public void connect(ITerminalControl control) {
		fControl=control;
		fControl.setState(TerminalState.CONNECTING);
		String file=fSettings.getInputFile();
		try {
			fInputStream=new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			disconnect();
			fControl.setMsg(file+": "+e.getLocalizedMessage());
			return;
		}
		fOutputStream=System.out;
		fControl.setTerminalTitle(fSettings.getInputFile());
		fConnection=new SpeedTestConnection(fInputStream,fSettings,fControl);
		fConnection.start();
	}

	synchronized public void disconnect() {
		if(fConnection!=null){
			fConnection.interrupt();
			fConnection=null;
		}
		if (fInputStream != null) {
			try {
				fInputStream.close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}
		fInputStream=null;
		if (fOutputStream != null) {
			try {
				fOutputStream.close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}
		fOutputStream=null;
		fControl.setState(TerminalState.CLOSED);
	}
	synchronized public InputStream getInputStream() {
		return fInputStream;
	}

	synchronized public OutputStream getOutputStream() {
		return fOutputStream;
	}

	public String getSettingsSummary() {
		return fSettings.getInputFile();
	}

	public void initialize() {
		//throw new RuntimeException("XXX problems\nSpeedTest\nXXX!");
	}

	public boolean isLocalEcho() {
		return false;
	}

	public void load(ISettingsStore store) {
		 fSettings.load(store);
		
	}

	public ISettingsPage makeSettingsPage() {
		return new SpeedTestSettingsPage(fSettings);
	}

	public void save(ISettingsStore store) {
		fSettings.save(store);
		
	}

	public void setTerminalSize(int newWidth, int newHeight) {
	}

}
