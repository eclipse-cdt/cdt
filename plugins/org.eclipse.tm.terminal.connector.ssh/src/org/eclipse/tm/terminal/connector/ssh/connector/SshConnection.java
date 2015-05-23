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
 * Martin Oberhuber (Wind River) - [175686] Adapted to new IJSchService API
 *    - copied code from org.eclipse.team.cvs.ssh2/JSchSession (Copyright IBM)
 * Martin Oberhuber (Wind River) - [198790] make SSH createSession() protected
 * Mikhail Kalugin <fourdman@xored.com> - [201864] Fix Terminal SSH keyboard interactive authentication
 * Martin Oberhuber (Wind River) - [155026] Add keepalives for SSH connection
 * Johnson Ma (Wind River) - [218880] Add UI setting for ssh keepalives
 * Martin Oberhuber (Wind River) - [225792] Rename SshConnector.getTelnetSettings() to getSshSettings()
 * Martin Oberhuber (Wind River) - [168197] Replace JFace MessagDialog by SWT MessageBox
 * Martin Oberhuber (Wind River) - [205674][ssh] Terminal remains "connecting" when authentication is cancelled
 * Michael Scharf (Wind River) - 240420: [terminal][ssh]Channel is not closed when the connection is closed with the close button
 * Martin Oberhuber (Wind River) - [206919] Improve SSH Terminal Error Reporting
 * Andrei Sobolev (Xored) - [250456] Ssh banner message causes IllegalArgumentException popup
 * Anton Leherbauer (Wind River) - [453393] Add support for copying wrapped lines without line break
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.ssh.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.window.Window;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.connector.ssh.activator.UIPlugin;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class SshConnection extends Thread {
	private static int fgNo;
	/* default */ final ITerminalControl fControl;
	private final SshConnector fConn;
	private Session fSession;
	private boolean fDisconnectHasBeenCalled;
	protected SshConnection(SshConnector conn,ITerminalControl control) {
		super("SshConnection-"+fgNo++); //$NON-NLS-1$
		fControl = control;
		fConn = conn;
		fControl.setState(TerminalState.CONNECTING);
	}

	//----------------------------------------------------------------------
	// <copied code from org.eclipse.team.cvs.ssh2/JSchSession (Copyright IBM)>
	//----------------------------------------------------------------------

	/**
	 * Create a Jsch session.
	 * Subclasses can override in order to replace the UserInfo wrapper
	 * (for non-interactive usage, for instance), or in order to change
	 * the Jsch config (for instance, in order to switch off strict
	 * host key checking or in order to add specific ciphers).
	 */
    protected Session createSession(String username, String password, String hostname, int port, UserInfo wrapperUI, IProgressMonitor monitor) throws JSchException {
        IJSchService service = UIPlugin.getDefault().getJSchService();
        if (service == null)
        	return null;
        Session session = service.createSession(hostname, port, username);
        //session.setTimeout(getSshTimeoutInMillis());
        session.setTimeout(0); //never time out on the session
        session.setServerAliveCountMax(6); //give up after 6 tries (remote will be dead after 30 min)
        if (password != null)
			session.setPassword(password);
        session.setUserInfo(wrapperUI);
        return session;
    }

	public static void shutdown() {
		//TODO: Store all Jsch sessions in a pool and disconnect them on shutdown
	}

	//----------------------------------------------------------------------
	// </copied code from org.eclipse.team.cvs.ssh2/JSchSession (Copyright IBM)>
	//----------------------------------------------------------------------

	@Override
    public void run() {
		boolean connectSucceeded = false;
		String host = ""; //$NON-NLS-1$
		int port = ISshSettings.DEFAULT_SSH_PORT;
		try {
			int nTimeout = fConn.getSshSettings().getTimeout() * 1000;
			int nKeepalive = fConn.getSshSettings().getKeepalive() * 1000;
			host = fConn.getSshSettings().getHost();
			String user = fConn.getSshSettings().getUser();
			String password = fConn.getSshSettings().getPassword();
			port = fConn.getSshSettings().getPort();

			UserInfo ui=new MyUserInfo(null, user, password);

            Session session = createSession(user, password, host, port,
            		ui, new NullProgressMonitor());
			synchronized (this) {
				fSession = session;
			}

            //java.util.Hashtable config=new java.util.Hashtable();
            //config.put("StrictHostKeyChecking", "no");
            //session.setConfig(config);
            //ui.aboutToConnect();
            if (nKeepalive > 0) {
                session.setServerAliveInterval(nKeepalive); //default is 5 minutes
            }
            // dont try to connect if disconnect has been requested already
			synchronized (this) {
				if (fDisconnectHasBeenCalled)
					return;
			}

			session.connect(nTimeout);   // making connection with timeout.
			// if we got disconnected, do not continue
			if(!isSessionConnected())
				return;
			ChannelShell channel=(ChannelShell) session.openChannel("shell"); //$NON-NLS-1$
			channel.setPtyType("xterm"); //$NON-NLS-1$
			// TERM=xterm implies VT100 line wrapping mode
			fControl.setVT100LineWrapping(true);
			channel.connect();

			// maybe the terminal was disconnected while we were connecting
			if (isSessionConnected() && channel.isConnected()) {
				connectSucceeded = true;
				fConn.setInputStream(channel.getInputStream());
				fConn.setOutputStream(channel.getOutputStream());
				fConn.setChannel(channel);
				fControl.setState(TerminalState.CONNECTED);
				try {
					// read data until the connection gets terminated
					readDataForever(fConn.getInputStream());
				} catch (InterruptedIOException e) {
					// we got interrupted: we are done...
				}
			}
		} catch (Exception e) {
			Throwable cause = e;
			while (cause.getCause() != null) {
				cause = cause.getCause();
			}
			String origMsg = cause.getMessage();
			String msg = SshMessages.getMessageFor(cause);
			if ((cause instanceof JSchException) && origMsg != null && origMsg.startsWith("Auth")) { //$NON-NLS-1$
				if (origMsg.indexOf("cancel") >= 0) { //$NON-NLS-1$
					msg = SshMessages.SSH_AUTH_CANCEL;
				} else if (origMsg.indexOf("fail") >= 0) { //$NON-NLS-1$
					msg = SshMessages.SSH_AUTH_FAIL;
				}
			}
			if (!connectSucceeded) {
				String hostPort = host;
				if (port != ISshSettings.DEFAULT_SSH_PORT) {
					hostPort = hostPort + ':' + port;
				}
				msg = NLS.bind(SshMessages.ERROR_CONNECTING, hostPort, msg);
			}
			connectFailed(msg, msg);
		} finally {
			// make sure the terminal is disconnected when the thread ends
			try {
				disconnect();
			} finally {
				// when reading is done, we set the state to closed
				fControl.setState(TerminalState.CLOSED);
			}
		}
	}

	/* default */ synchronized boolean isSessionConnected() {
		return !fDisconnectHasBeenCalled && fSession != null && fSession.isConnected();
	}

	/**
	 * disconnect the ssh session
	 */
	void disconnect() {
		interrupt();
		synchronized (this) {
			fDisconnectHasBeenCalled=true;
			if(fSession!=null) {
				try {
					fSession.disconnect();
				} catch (Exception e) {
					// Ignore NPE due to bug in JSch if disconnecting
					// while not yet authenticated
				}
				fSession=null;
			}
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
		// read until the thread gets interrupted....
		while( (n=in.read(bytes))!=-1) {
			fControl.getRemoteToTerminalOutputStream().write(bytes,0,n);
		}
	}

	protected static Display getStandardDisplay() {
    	Display display = Display.getCurrent();
    	if( display==null ) {
    		display = Display.getDefault();
    	}
    	return display;
    }

    private class MyUserInfo implements UserInfo, UIKeyboardInteractive {
    	/* default */ final String fConnectionId;
    	/* default */ final String fUser;
    	private String fPassword;
    	private String fPassphrase;
    	private int fAttemptCount;

		public MyUserInfo(String connectionId, String user, String password) {
			fConnectionId = connectionId;
			fUser = user;
			fPassword = password;
		}
		@Override
        public String getPassword() {
			return fPassword;
		}
		@Override
        public boolean promptYesNo(final String str) {
			//need to switch to UI thread for prompting
			final boolean[] retval = new boolean[1];
			Display.getDefault().syncExec(new Runnable() {
				@Override
                public void run() {
					// [168197] Replace JFace MessagDialog by SWT MessageBox
					//retval[0] = MessageDialog.openQuestion(null, SshMessages.WARNING, str);
					if (isSessionConnected()) {
						MessageBox mb = new MessageBox(fControl.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
						mb.setText(SshMessages.WARNING);
						mb.setMessage(str);
						retval[0] = (mb.open() == SWT.YES);
					} else {
						retval[0] = false;
					}
				}
			});
			return retval[0];
		}
		private String promptSecret(final String message) {
			final String[] retval = new String[1];
			getStandardDisplay().syncExec(new Runnable() {
				@Override
                public void run() {
					if (isSessionConnected()) {
						UserValidationDialog uvd = new UserValidationDialog(null, fConnectionId, fUser, message);
						uvd.setUsernameMutable(false);
						if (uvd.open() == Window.OK) {
							retval[0] = uvd.getPassword();
						} else {
							retval[0] = null;
						}
					} else {
						retval[0] = null;
					}
				}
			});
			return retval[0];
		}
		@Override
        public String getPassphrase() {
			return fPassphrase;
		}
		@Override
        public boolean promptPassphrase(String message) {
			fPassphrase = promptSecret(message);
			return (fPassphrase!=null);
		}
		@Override
        public boolean promptPassword(final String message) {
			String _password = promptSecret(message);
			if (_password!=null) {
				fPassword=_password;
			return true;
		}
			return false;
		}
		@Override
        public void showMessage(final String message) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
                public void run() {
					// [168197] Replace JFace MessagDialog by SWT MessageBox
					// MessageDialog.openInformation(null, SshMessages.INFO, message);
					if (isSessionConnected()) {
						MessageBox mb = new MessageBox(fControl.getShell(), SWT.ICON_INFORMATION | SWT.OK);
						mb.setText(SshMessages.INFO);
						mb.setMessage(message);
						mb.open();
					}
				}
			});
		}
		@Override
        public String[] promptKeyboardInteractive(final String destination,
				final String name, final String instruction,
				final String[] prompt, final boolean[] echo)
		{
		    if (prompt.length == 0) {
		        // No need to prompt, just return an empty String array
		        return new String[0];
		    }
			try{
			    if (fAttemptCount == 0 && fPassword != null && prompt.length == 1 && prompt[0].trim().equalsIgnoreCase("password:")) { //$NON-NLS-1$
			        // Return the provided password the first time but always prompt on subsequent tries
			        fAttemptCount++;
			        return new String[] { fPassword };
			    }
			    final String[][] finResult = new String[1][];
			    getStandardDisplay().syncExec(new Runnable() {
			    	@Override
                    public void run() {
			    		if (isSessionConnected()) {
							KeyboardInteractiveDialog dialog = new KeyboardInteractiveDialog(null, fConnectionId, destination, name, instruction, prompt, echo);
							dialog.open();
							finResult[0] = dialog.getResult();
						} else {
							finResult[0] = null; // indicate cancel to JSch
						}
		    		}
			    });
			    String[] result=finResult[0];
                if (result == null)
                    return null; // cancelled
			    if (result.length == 1 && prompt.length == 1 && prompt[0].trim().equalsIgnoreCase("password:")) { //$NON-NLS-1$
			        fPassword = result[0];
			    }
			    fAttemptCount++;
				return result;
			}
			catch(OperationCanceledException e){
				return null;
			}
		}
    }

    private void connectFailed(String terminalText, String msg) {
		Logger.log(terminalText);
		fControl.displayTextInTerminal(terminalText);
		// fControl.setMsg(msg);
	}

}