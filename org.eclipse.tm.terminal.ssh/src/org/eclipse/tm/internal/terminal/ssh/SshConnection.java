/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

class SshConnection extends Thread {
	private static int fgNo;
	private final ITerminalControl fControl;
	private final SshConnector fConn;
	private Channel fChannel;
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
        IJSchService service = Activator.getDefault().getJSchService();
        if (service == null)
        	return null;
        Session session = service.createSession(hostname, port, username);
        //session.setTimeout(getSshTimeoutInMillis());
        session.setTimeout(0); //never time out on the session
        if (password != null)
			session.setPassword(password);
        session.setUserInfo(wrapperUI);
        return session;
    }

	static void shutdown() {
		//TODO: Store all Jsch sessions in a pool and disconnect them on shutdown
	}

	//----------------------------------------------------------------------
	// </copied code from org.eclipse.team.cvs.ssh2/JSchSession (Copyright IBM)>
	//----------------------------------------------------------------------

	public void run() {
		try {
			int nTimeout = fConn.getTelnetSettings().getTimeout() * 1000;
			String host = fConn.getTelnetSettings().getHost();
			String user = fConn.getTelnetSettings().getUser();
			String password = fConn.getTelnetSettings().getPassword();
			int port=fConn.getTelnetSettings().getPort();

			////Giving a connectionId could be the index into a local
			////Store where passwords are stored
			//String connectionId = host;
			//if (port!=22) {
			//	connectionId += ':' + port;
			//}
			//UserInfo ui=new MyUserInfo(connectionId, user, password);
			UserInfo ui=new MyUserInfo(null, user, password);

            Session session = createSession(user, password, host, port, 
            		ui, new NullProgressMonitor());

            //java.util.Hashtable config=new java.util.Hashtable();
            //config.put("StrictHostKeyChecking", "no");
            //session.setConfig(config);
            //ui.aboutToConnect();
			session.connect(nTimeout);   // making connection with timeout.

			ChannelShell channel=(ChannelShell) session.openChannel("shell"); //$NON-NLS-1$
			channel.setPtyType("ansi"); //$NON-NLS-1$
			channel.connect();
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
			// when reading is done, we set the state to closed
			fControl.setState(TerminalState.CLOSED);
		} catch (JSchException e) {
			if(e.toString().indexOf("Auth cancel")<0) {  //$NON-NLS-1$
				//no error if user pressed cancel
				connectFailed(e.getMessage(),e.getMessage());
			}
		} catch (IOException e) {
			connectFailed(e.getMessage(),e.getMessage());
		} finally {
			
		}
	}
	synchronized void setChannel(Channel channel) {
		fChannel = channel;
	}
	/**
	 * disconnect the ssh session
	 */
	void disconnect() {
		interrupt();
		synchronized (this) {
			if(fChannel!=null) {
				fChannel.disconnect();
				fChannel=null;
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
    
    private static class MyUserInfo implements UserInfo {
    	private final String fConnectionId;
    	private final String fUser;
    	private String fPassword;
    	private String fPassphrase;
    	private int fAttemptCount;

		public MyUserInfo(String connectionId, String user, String password) {
			fConnectionId = connectionId;
			fUser = user;
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
		private String promptSecret(final String message) {
			final String[] retval = new String[1];
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					UserValidationDialog uvd = new UserValidationDialog(null, fConnectionId, fUser, message);
					uvd.setUsernameMutable(false);
					if (uvd.open() == Window.OK) {
						retval[0] = uvd.getPassword();
					} else {
						retval[0] = null;
					}
				}
			});
			return retval[0];
		}
		public String getPassphrase() {
			return fPassphrase;
		}
		public boolean promptPassphrase(String message) {
			fPassphrase = promptSecret(message);
			return (fPassphrase!=null);
		}
		public boolean promptPassword(final String message) {
			String _password = promptSecret(message);
			if (_password!=null) {
				fPassword=_password;
			return true;
		}
			return false;
		}
		public void showMessage(final String message) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(null, SshMessages.INFO, message);
				}
			});
		}
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
			    	public void run() {
			    		KeyboardInteractiveDialog dialog = new KeyboardInteractiveDialog(null, 
			    			fConnectionId, destination, name, instruction, prompt, echo);
			    		dialog.open();
			    		finResult[0]=dialog.getResult();
		    		}
			    });
			    String[] result=finResult[0];
                if (result == null) 
                    return null; // canceled
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
        /**
         * Callback to indicate that a connection is about to be attempted
         */
        public void aboutToConnect() {
            fAttemptCount = 0;
        }
        /**
         * Callback to indicate that a connection was made
         */
        public void connectionMade() {
            fAttemptCount = 0;
        }
    }

    private void connectFailed(String terminalText, String msg) {
		Logger.log(terminalText);
		fControl.displayTextInTerminal(terminalText);
		fControl.setState(TerminalState.CLOSED);
		fControl.setMsg(msg);
	}
}