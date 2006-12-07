/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.terminal.ITerminalControl;
import org.eclipse.tm.terminal.Logger;
import org.eclipse.tm.terminal.TerminalState;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

class SshConnection extends Thread {
	private final ITerminalControl fControl;
	private final SshConnector fConn;
	protected SshConnection(SshConnector conn,ITerminalControl control) {
		fControl = control;
		fConn = conn;
		fControl.setState(TerminalState.CONNECTING);
	}
	public void run() {
		try {
			int nTimeout = fConn.getTelnetSettings().getTimeout() * 1000;
			String host = fConn.getTelnetSettings().getHost();
			String user = fConn.getTelnetSettings().getUser();
			int port=fConn.getTelnetSettings().getPort();
			Session session=fConn.getJsch().getSession(user, host, port);
			//session.setPassword("your password");

			// username and password will be given via UserInfo interface.
			UserInfo ui=new MyUserInfo(fConn.getTelnetSettings().getPassword());
			session.setUserInfo(ui);

//			java.util.Hashtable config=new java.util.Hashtable();
//			config.put("StrictHostKeyChecking", "no");
//			session.setConfig(config);

			//session.connect();
			session.connect(nTimeout);   // making connection with timeout.

			ChannelShell channel=(ChannelShell) session.openChannel("shell"); //$NON-NLS-1$
			
			//hmm, now it gets a bit complicated
			// Input and output streams are somehow confusing
			PipedInputStream pin = new PipedInputStream();
			PipedOutputStream out = new PipedOutputStream(pin);

			PipedOutputStream pout = new PipedOutputStream();
			PipedInputStream in = new PipedInputStream(pout);

			
			channel.setInputStream(pin);
			channel.setOutputStream(pout);
			channel.connect();

			fConn.setInputStream(in);
			fConn.setOutputStream(out);
			fConn.setChannel(channel);
			fControl.setState(TerminalState.CONNECTED);
			// read data until the connection gets terminated
			readDataForever(in);
		} catch (JSchException e) {
			connectFailed(e.getMessage(),e.getMessage());
		} catch (IOException e) {
			connectFailed(e.getMessage(),e.getMessage());
		} finally {
			
		}
	}

	/**
	 * Read the data from the ssh connection and display it in the terminal.
	 * @param in
	 * @throws IOException
	 */
	private void readDataForever(InputStream in) throws IOException {
		// read the data
		byte bytes[]=new byte[32*1024];
		int n;
		while((n=in.read(bytes))!=-1) {
			fControl.writeToTerminal(new String(bytes,0,n));
		}
		fControl.setState(TerminalState.CLOSED);
	}

    private static class MyUserInfo implements UserInfo {
    	private String fPassword;

		public MyUserInfo(String password) {
			fPassword = password;
		}
		public String getPassword() {
			return fPassword;
		}
		public boolean promptYesNo(final String str) {
			//need to switch to UI thread for prompting
			final boolean[] retval = new boolean[1];
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					retval[0] = MessageDialog.openQuestion(null, SshMessages.WARNING, str); 
				}
			});
			return retval[0]; 
		}
		public String getPassphrase() {
			return null;
		}
		public boolean promptPassphrase(String message) {
			return true;
		}
		public boolean promptPassword(final String message) {
			return true;
		}
		public void showMessage(final String message) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(null, SshMessages.INFO, message);
				}
			});
		}
    }

    private void connectFailed(String terminalText, String msg) {
		Logger.log(terminalText);
		fControl.displayTextInTerminal(terminalText);
		fControl.setState(TerminalState.CLOSED);
		fControl.setMsg(msg);
	}
}