/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Michael Scharf (Wind River) - initial implementation
 *     
 *******************************************************************************/
package org.eclipse.tm.terminal.ssh;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.tm.terminal.ISettingsPage;
import org.eclipse.tm.terminal.ISettingsStore;
import org.eclipse.tm.terminal.ITerminalConnector;
import org.eclipse.tm.terminal.ITerminalControl;
import org.eclipse.tm.terminal.Logger;
import org.eclipse.tm.terminal.TerminalState;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;

public class SshConnector implements ITerminalConnector {
	private OutputStream fOutputStream;
	private InputStream fInputStream;
	private ITerminalControl fControl;
	private JSch fJsch;
	private ChannelShell fChannel;

	private final SshSettings fSettings;
	public SshConnector() {
		this(new SshSettings());
		try {
			fJsch=new JSch();
		} catch(NoClassDefFoundError e) {
			// ignore
			e.printStackTrace();
		}
	}
	public SshConnector(SshSettings settings) {
		fSettings=settings;
	}
	public String getId() {
		return getClass().getName();
	}
	public boolean isInstalled() {
		return fJsch!=null;
	}
	public void connect(ITerminalControl control) {
		Logger.log("entered."); //$NON-NLS-1$
		fControl=control;
		SshConnection worker = new SshConnection(this,control);
		worker.start();
	}
	public void disconnect() {
		Logger.log("entered."); //$NON-NLS-1$
	
		if (getInputStream() != null) {
			try {
				getInputStream().close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}
	
		if (getOutputStream() != null) {
			try {
				getOutputStream().close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}
		setState(TerminalState.CLOSED);
	}
	public boolean isLocalEcho() {
		return false;
	}
	public void setTerminalSize(int newWidth, int newHeight) {
		if(fChannel!=null)
			fChannel.setPtySize(newWidth, newHeight, 8, 8);
		
	}
	public InputStream getInputStream() {
		return fInputStream;
	}
	public OutputStream getOutputStream() {
		return fOutputStream;
	}
	void setInputStream(InputStream inputStream) {
		fInputStream = inputStream;
	}
	void setOutputStream(OutputStream outputStream) {
		fOutputStream = outputStream;
	}
	public void writeToTerminal(String txt) {
		fControl.writeToTerminal(txt);
		
	}
	public void setState(TerminalState state) {
		fControl.setState(state);
		
	}
	public ISshSettings getTelnetSettings() {
		return fSettings;
	}
	public ISettingsPage makeSettingsPage() {
		return new SshSettingsPage(fSettings);
	}
	public String getStatusString(String strConnected) {
		return fSettings.getStatusString(strConnected);
	}
	public void load(ISettingsStore store) {
		fSettings.load(store);
		
	}
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
	}
}