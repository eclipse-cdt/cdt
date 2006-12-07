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

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ssh2.CVSSSH2Plugin;
import org.eclipse.team.internal.ccvs.ssh2.ISSHContants;
import org.eclipse.team.internal.ccvs.ui.KeyboardInteractiveDialog;
import org.eclipse.team.internal.ccvs.ui.UserValidationDialog;
import org.eclipse.tm.terminal.ITerminalControl;
import org.eclipse.tm.terminal.Logger;
import org.eclipse.tm.terminal.TerminalState;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS5;
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

	//----------------------------------------------------------------------
	// <copied from org.eclipse.team.cvs.ssh2>
	//----------------------------------------------------------------------
	private static String current_ssh_home = null;
	private static String current_pkeys = ""; //$NON-NLS-1$
	static String SSH_HOME_DEFAULT = null;
	static {
		String ssh_dir_name = ".ssh"; //$NON-NLS-1$
		
		// Windows doesn't like files or directories starting with a dot.
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			ssh_dir_name = "ssh"; //$NON-NLS-1$
		}
		SSH_HOME_DEFAULT = System.getProperty("user.home"); //$NON-NLS-1$
		if (SSH_HOME_DEFAULT != null) {
		    SSH_HOME_DEFAULT = SSH_HOME_DEFAULT + java.io.File.separator + ssh_dir_name;
		}
	}

	// Load ssh prefs from Team/CVS for now.
	// TODO do our own preference page.
	static void loadSshPrefs(JSch jsch)
	{
		IPreferenceStore store = CVSSSH2Plugin.getDefault().getPreferenceStore();
		String ssh_home = store.getString(ISSHContants.KEY_SSH2HOME);
		String pkeys = store.getString(ISSHContants.KEY_PRIVATEKEY);

		try {
			if (ssh_home.length() == 0)
				ssh_home = SSH_HOME_DEFAULT;

			if (current_ssh_home == null || !current_ssh_home.equals(ssh_home)) {
				loadKnownHosts(jsch, ssh_home);
				current_ssh_home = ssh_home;
			}

			if (!current_pkeys.equals(pkeys)) {
				java.io.File file;
				String[] pkey = pkeys.split(","); //$NON-NLS-1$
				String[] _pkey = current_pkeys.split(","); //$NON-NLS-1$
				current_pkeys = ""; //$NON-NLS-1$
				for (int i = 0; i < pkey.length; i++) {
					file = new java.io.File(pkey[i]);
					if (!file.isAbsolute()) {
						file = new java.io.File(ssh_home, pkey[i]);
					}
					if (file.exists()) {
						boolean notyet = true;
						for (int j = 0; j < _pkey.length; j++) {
							if (pkey[i].equals(_pkey[j])) {
								notyet = false;
								break;
							}
						}
						if (notyet)
							jsch.addIdentity(file.getPath());
						if (current_pkeys.length() == 0) {
							current_pkeys = pkey[i];
						} else {
							current_pkeys += ("," + pkey[i]); //$NON-NLS-1$
						}
					}
				}
			}
		} catch (Exception e) {
		}

	}

    static void loadKnownHosts(JSch jsch, String ssh_home){
		try {
		  java.io.File file;
		  file=new java.io.File(ssh_home, "known_hosts"); //$NON-NLS-1$
		  jsch.setKnownHosts(file.getPath());
		} catch (Exception e) {
		}
	}
    
    static Proxy loadSshProxyPrefs() {
    	//TODO Get rid of discouraged access when bug 154100 is fixed
		boolean useProxy = CVSProviderPlugin.getPlugin().isUseProxy();
        Proxy proxy = null;
		if (useProxy) {
			String _type = CVSProviderPlugin.getPlugin().getProxyType();
			String _host = CVSProviderPlugin.getPlugin().getProxyHost();
			String _port = CVSProviderPlugin.getPlugin().getProxyPort();

			boolean useAuth = CVSProviderPlugin.getPlugin().isUseProxyAuth();
			String _user = ""; //$NON-NLS-1$
			String _pass = ""; //$NON-NLS-1$
			
			// Retrieve username and password from keyring.
			if(useAuth){
				_user=CVSProviderPlugin.getPlugin().getProxyUser();
				_pass=CVSProviderPlugin.getPlugin().getProxyPassword();
			}

			String proxyhost = _host + ":" + _port; //$NON-NLS-1$
			if (_type.equals(CVSProviderPlugin.PROXY_TYPE_HTTP)) {
				proxy = new ProxyHTTP(proxyhost);
				if (useAuth) {
					((ProxyHTTP) proxy).setUserPasswd(_user, _pass);
				}
			} else if (_type.equals(CVSProviderPlugin.PROXY_TYPE_SOCKS5)) {
				proxy = new ProxySOCKS5(proxyhost);
				if (useAuth) {
					((ProxySOCKS5) proxy).setUserPasswd(_user, _pass);
				}
			}
		}
		return proxy;
    }

	//----------------------------------------------------------------------
	// </copied from org.eclipse.team.cvs.ssh2>
	//----------------------------------------------------------------------

	public void run() {
		try {
			int nTimeout = fConn.getTelnetSettings().getTimeout() * 1000;
			String host = fConn.getTelnetSettings().getHost();
			String user = fConn.getTelnetSettings().getUser();
			String password = fConn.getTelnetSettings().getPassword();
			int port=fConn.getTelnetSettings().getPort();

			loadSshPrefs(fConn.getJsch());
	        Proxy proxy = loadSshProxyPrefs();
			Session session=fConn.getJsch().getSession(user, host, port);
	        if (proxy != null) {
	            session.setProxy(proxy);
	        }
	        session.setTimeout(0); //never time out once connected
			
			session.setPassword(password);
			UserInfo ui=new MyUserInfo(user, password);
			session.setUserInfo(ui);

//			java.util.Hashtable config=new java.util.Hashtable();
//			config.put("StrictHostKeyChecking", "no");
//			session.setConfig(config);

			//session.connect();
			session.connect(nTimeout);   // making connection with timeout.

			ChannelShell channel=(ChannelShell) session.openChannel("shell"); //$NON-NLS-1$
			channel.connect();
			fConn.setInputStream(channel.getInputStream());
			fConn.setOutputStream(channel.getOutputStream());
			fConn.setChannel(channel);
			fControl.setState(TerminalState.CONNECTED);
			// read data until the connection gets terminated
			readDataForever(fConn.getInputStream());
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

	protected static Display getStandardDisplay() {
    	Display display = Display.getCurrent();
    	if( display==null ) {
    		display = Display.getDefault();
    	}
    	return display;
    }
    
    private static class MyUserInfo implements UserInfo {
    	private String fPassword;
    	private String fPassphrase;
    	private int fAttemptCount;
    	private final String fUser;

		public MyUserInfo(String user, String password) {
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
			final String finUser = fUser;
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					//TODO discouraged access: Write our own UserValidationDialog
					UserValidationDialog uvd = new UserValidationDialog(null, null,
							finUser, message);
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
			    		//TODO discouraged access: write our own KeyboardInteractiveDialog
			    		KeyboardInteractiveDialog dialog = new KeyboardInteractiveDialog(null, 
			    			null, destination, name, instruction, prompt, echo);
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