/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.connectorservice.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.subsystems.AbstractConnectorService;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.ssh.ISshSessionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.util.Util;
import org.eclipse.team.internal.ccvs.ssh2.CVSSSH2Plugin;
import org.eclipse.team.internal.ccvs.ssh2.ISSHContants;
import org.eclipse.team.internal.ccvs.ui.UserValidationDialog;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SocketFactory;
import com.jcraft.jsch.UserInfo;

/** 
 * Create SSH connections.
 */
public class SshConnectorService extends AbstractConnectorService implements ISshSessionProvider
{
	private static final int SSH_DEFAULT_PORT = 22;
	private static JSch jsch=new JSch();
    private Session session;

	public SshConnectorService(IHost host) {
		//TODO the port parameter doesnt really make sense here since
		//it will be overridden when the subsystem initializes (through
		//setPort() on our base class -- I assume the port is meant to 
		//be a local port.
		super("SSH Connector Service", "SSH Connector Service Description", host, 0);
	}

	//----------------------------------------------------------------------
	// <copied from org.eclipse.team.cvs.ssh2>
	//----------------------------------------------------------------------
	private static String current_ssh_home = null;
	private static String current_pkeys = ""; //$NON-NLS-1$
    protected static int getSshTimeoutInMillis() {
        //return CVSProviderPlugin.getPlugin().getTimeout() * 1000;
    	// TODO Hard-code the timeout for now since Jsch doesn't respect CVS timeout
    	// See bug 92887
    	return 60000;
    }
    

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
	static void loadSshPrefs()
	{
		IPreferenceStore store = CVSSSH2Plugin.getDefault().getPreferenceStore();
		String ssh_home = store.getString(ISSHContants.KEY_SSH2HOME);
		String pkeys = store.getString(ISSHContants.KEY_PRIVATEKEY);

		try {
			if (ssh_home.length() == 0)
				ssh_home = SSH_HOME_DEFAULT;

			if (current_ssh_home == null || !current_ssh_home.equals(ssh_home)) {
				loadKnownHosts(ssh_home);
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

    static void loadKnownHosts(String ssh_home){
		try {
		  java.io.File file;
		  file=new java.io.File(ssh_home, "known_hosts"); //$NON-NLS-1$
		  jsch.setKnownHosts(file.getPath());
		} catch (Exception e) {
		}
	}
    
    static Proxy loadSshProxyPrefs() {
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
			} else {
				proxy = null;
			}
		}
		return proxy;
    }

	public static class SimpleSocketFactory implements SocketFactory {
		InputStream in = null;
		OutputStream out = null;
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			Socket socket = null;
			socket = new Socket(host, port);
			return socket;
		}
		public InputStream getInputStream(Socket socket) throws IOException {
			if (in == null)
				in = socket.getInputStream();
			return in;
		}
		public OutputStream getOutputStream(Socket socket) throws IOException {
			if (out == null)
				out = socket.getOutputStream();
			return out;
		}
	}
	
	public static class ResponsiveSocketFacory extends SimpleSocketFactory {
		private IProgressMonitor monitor;
		public ResponsiveSocketFacory(IProgressMonitor monitor) {
			this.monitor = monitor;
		}
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			Socket socket = null;
			socket = Util.createSocket(host, port, monitor);
			// Null out the monitor so we don't hold onto anything
			// (i.e. the SSH2 session will keep a handle to the socket factory around
			monitor = new NullProgressMonitor();
			// Set the socket timeout
			socket.setSoTimeout(getSshTimeoutInMillis());
			return socket;
		}
	}

	//----------------------------------------------------------------------
	// </copied from org.eclipse.team.cvs.ssh2>
	//----------------------------------------------------------------------

	protected void internalConnect(IProgressMonitor monitor) throws Exception
    {
    	//TODO Set known hosts and identities from Preferences
    	//We could share the preferences from ssh2, or use RSE
    	//ConnectorService Properties / Server Launcher Properties
    	
        //jsch.setKnownHosts("/home/foo/.ssh/known_hosts");
		loadSshPrefs();
        String host = getHostName();
        String user = getUserId();

        Proxy proxy = loadSshProxyPrefs();
        session=jsch.getSession(user, host, SSH_DEFAULT_PORT);
        if (proxy != null) {
            session.setProxy(proxy);
        }
        session.setTimeout(getSshTimeoutInMillis());
        session.setPassword(getPasswordInformation().getPassword());
        session.setUserInfo(new MyUserInfo(user));
        session.setSocketFactory(new ResponsiveSocketFacory(monitor));

        //java.util.Hashtable config=new java.util.Hashtable();
        //config.put("StrictHostKeyChecking", "no");
        //session.setConfig(config);
        try {
        	Activator.trace("connecting..."); //$NON-NLS-1$
            session.connect();
        	Activator.trace("connected"); //$NON-NLS-1$
        } catch (JSchException e) {
        	Activator.trace("connect failed: "+e.toString()); //$NON-NLS-1$
            if (session.isConnected())
                session.disconnect();
            throw e;
        }
    }

	public void internalDisconnect(IProgressMonitor monitor)
	{
		//TODO: Check, Is disconnect being called because the network (connection) went down?
		//TODO: Fire communication event (aboutToDisconnect) -- see DStoreConnectorService.internalDisconnect()
		//TODO: Wrap exception in an InvocationTargetException -- see DStoreConnectorService.internalDisconnect()
		//Will services like the sftp service be disconnected too? Or notified?
    	Activator.trace("disconnect"); //$NON-NLS-1$
		session.disconnect();
	}

	//TODO avoid having jsch type "Session" in the API.
	//Could be done by instanciating SshShellService and SshFileService here,
	//and implementing IShellService getShellService() 
	//and IFileService getFileService().
    public Session getSession() {
    	return session;
    }

    private static Display getStandardDisplay() {
    	Display display = Display.getCurrent();
    	if( display==null ) {
    		display = Display.getDefault();
    	}
    	return display;
    }
    
    private static class MyUserInfo implements UserInfo {
    	String fPassphrase;
		String fPassword;
		final String fUser;

		public MyUserInfo(String user) {
			fUser = user;
		}
		public String getPassword() {
			return fPassword;
		}
		public boolean promptYesNo(final String str) {
			//need to switch to UI thread for prompting
			final boolean[] retval = new boolean[1];
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					retval[0] = MessageDialog.openQuestion(null, "Warning", str); 
				}
			});
			return retval[0]; 
		}
		private String promptString(final String message) {
			final String[] retval = new String[1];
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					//TODO Write our own UserValidationDialog instead of re-using the internal one from team.ssh
					UserValidationDialog uvd = new UserValidationDialog(null, null,
							fUser, message);
					uvd.setUsernameMutable(false);
					if (uvd.open() == uvd.OK) {
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
			fPassphrase = promptString(message);
			return (fPassphrase!=null);
		}
		public boolean promptPassword(final String message) {
			fPassword = promptString(message);
			return (fPassphrase!=null);
		}
		public void showMessage(final String message) {
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(null, "Info", message);
				}
			});
		}
	}
    
	public boolean isConnected() {
		return (session!=null && session.isConnected());
	}

	public boolean hasRemoteServerLauncherProperties() {
		return false;
	}

	public boolean supportsRemoteServerLaunching() {
		return false;
	}

	public boolean supportsServerLaunchProperties() {
		return false;
	}

}
