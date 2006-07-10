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
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.util.Util;
import org.eclipse.team.internal.ccvs.ssh2.CVSSSH2Plugin;
import org.eclipse.team.internal.ccvs.ssh2.ISSHContants;
import org.eclipse.team.internal.ccvs.ui.KeyboardInteractiveDialog;
import org.eclipse.team.internal.ccvs.ui.UserValidationDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SocketFactory;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.AbstractConnectorService;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.SystemSignonInformation;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.ssh.ISshSessionProvider;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;

/** 
 * Create SSH connections.
 */
public class SshConnectorService extends AbstractConnectorService implements ISshSessionProvider
{
	private static final int SSH_DEFAULT_PORT = 22;
	private static JSch jsch=new JSch();
    private Session session;
    private SessionLostHandler fSessionLostHandler;

	public SshConnectorService(IHost host) {
		//TODO the port parameter doesnt really make sense here since
		//it will be overridden when the subsystem initializes (through
		//setPort() on our base class -- I assume the port is meant to 
		//be a local port.
		super(SshConnectorResources.SshConnectorService_Name, SshConnectorResources.SshConnectorService_Description, host, 0);
		fSessionLostHandler = null;
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
			//Allows to cancel the socket creation operation if necessary.
			//Waits for the timeout specified in CVS Preferences, maximum.
			socket = Util.createSocket(host, port, monitor);
			// Null out the monitor so we don't hold onto anything
			// (i.e. the SSH2 session will keep a handle to the socket factory around
			monitor = new NullProgressMonitor();
			// Set the socket timeout
			//socket.setSoTimeout(getSshTimeoutInMillis());
			// We want blocking read() calls in ssh to never terminate
			socket.setSoTimeout(0);
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
        //session.setTimeout(getSshTimeoutInMillis());
        session.setTimeout(0); //never time out on the session
        String password=""; //$NON-NLS-1$
        SystemSignonInformation ssi = getPasswordInformation();
        if (ssi!=null) {
        	password = getPasswordInformation().getPassword();
        }
        session.setPassword(password);
        MyUserInfo userInfo = new MyUserInfo(user, password);
        session.setUserInfo(userInfo);
        session.setSocketFactory(new ResponsiveSocketFacory(monitor));

        //java.util.Hashtable config=new java.util.Hashtable();
        //config.put("StrictHostKeyChecking", "no");
        //session.setConfig(config);
        userInfo.aboutToConnect();
        try {
        	Activator.trace("SshConnectorService.connecting..."); //$NON-NLS-1$
        	//wait for 60 sec maximum during connect
            session.connect(60000);
        	Activator.trace("SshConnectorService.connected"); //$NON-NLS-1$
        } catch (JSchException e) {
        	Activator.trace("SshConnectorService.connect failed: "+e.toString()); //$NON-NLS-1$
            if (session.isConnected())
                session.disconnect();
            throw e;
        }
        userInfo.connectionMade();
        fSessionLostHandler = new SessionLostHandler(this);
        notifyConnection();
    }

	public void internalDisconnect(IProgressMonitor monitor) throws Exception
	{
		//TODO Will services like the sftp service be disconnected too? Or notified?
    	Activator.trace("SshConnectorService.disconnect"); //$NON-NLS-1$
		try
		{
			if (session != null) {
				// Is disconnect being called because the network (connection) went down?
				boolean sessionLost = (fSessionLostHandler!=null && fSessionLostHandler.isSessionLost());
				// no more interested in handling session-lost, since we are disconnecting anyway
				fSessionLostHandler = null;
				// handle events
				if (sessionLost) {
					notifyError();
				} 
				else {
					// Fire comm event to signal state about to change
					fireCommunicationsEvent(CommunicationsEvent.BEFORE_DISCONNECT);
				}

				if (session.isConnected()) {
					session.disconnect();
				}
				
				// Fire comm event to signal state changed
				notifyDisconnection();
				//TODO MOB - keep the session to avoid NPEs in services (disables gc for the session!)
				// session = null;
				// DKM - no need to clear uid cache
				clearPasswordCache(false); // clear in-memory password
				//clearUserIdCache(); // Clear any cached local user IDs
			}
		}
		catch (Exception exc)
		{
			throw new java.lang.reflect.InvocationTargetException(exc);
		}
	}

	//TODO avoid having jsch type "Session" in the API.
	//Could be done by instanciating SshShellService and SshFileService here,
	//and implementing IShellService getShellService() 
	//and IFileService getFileService().
    public Session getSession() {
    	return session;
    }

    /**
     * Handle session-lost events.
     * This is generic for any sort of connector service.
     * Most of this is extracted from dstore's ConnectionStatusListener.
     * 
     * TODO should be refactored to make it generally available, and allow
     * dstore to derive from it.
     */
	public static class SessionLostHandler implements Runnable, IRunnableWithProgress
	{
		private IConnectorService _connection;
		private boolean fSessionLost;
		
		public SessionLostHandler(IConnectorService cs)
		{
			_connection = cs;
			fSessionLost = false;
		}
		
		/** 
		 * Notify that the connection has been lost. This may be called 
		 * multiple times from multiple subsystems. The SessionLostHandler
		 * ensures that actual user feedback and disconnect actions are
		 * done only once, on the first invocation.
		 */
		public void sessionLost()
		{
			//avoid duplicate execution of sessionLost
			boolean showSessionLostDlg=false;
			synchronized(this) {
				if (!fSessionLost) {
					fSessionLost = true;
					showSessionLostDlg=true;
				}
			}
			if (showSessionLostDlg) {
				//invokes this.run() on dispatch thread
				Display.getDefault().asyncExec(this);
			}
		}
		
		public synchronized boolean isSessionLost() {
			return fSessionLost;
		}
		
		public void run()
		{
			Shell shell = getShell();
			//TODO need a more correct message for "session lost"
			//TODO allow users to reconnect from this dialog
			//SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_UNKNOWNHOST);
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_CANCELLED);
			msg.makeSubstitution(_connection.getPrimarySubSystem().getHost().getAliasName());
			SystemMessageDialog dialog = new SystemMessageDialog(getShell(), msg);
			dialog.open();
			try
			{
				//TODO I think we should better use a Job for disconnecting?
				//But what about error messages?
				IRunnableContext runnableContext = getRunnableContext(getShell());
				// will do this.run(IProgressMonitor mon)
		    	//runnableContext.run(false,true,this); // inthread, cancellable, IRunnableWithProgress
		    	runnableContext.run(true,true,this); // fork, cancellable, IRunnableWithProgress
		    	_connection.reset();
				ISystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry();    	    
	            sr.connectedStatusChange(_connection.getPrimarySubSystem(), false, true, true);
			}
	    	catch (InterruptedException exc) // user cancelled
	    	{
	    	  if (shell != null)    		
	            showDisconnectCancelledMessage(shell, _connection.getHostName(), _connection.getPort());
	    	}    	
	    	catch (java.lang.reflect.InvocationTargetException invokeExc) // unexpected error
	    	{
	    	  Exception exc = (Exception)invokeExc.getTargetException();
	    	  if (shell != null)    		
	    	    showDisconnectErrorMessage(shell, _connection.getHostName(), _connection.getPort(), exc);    	    	
	    	}
			catch (Exception e)
			{
				SystemBasePlugin.logError(SshConnectorResources.SshConnectorService_ErrorDisconnecting, e);
			}
		}

		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException
		{
			String message = null;
			message = SubSystemConfiguration.getDisconnectingMessage(
					_connection.getHostName(), _connection.getPort());
			monitor.beginTask(message, IProgressMonitor.UNKNOWN);
			try {
				_connection.disconnect(monitor);
			} catch (Exception exc) {
				if (exc instanceof java.lang.reflect.InvocationTargetException)
					throw (java.lang.reflect.InvocationTargetException) exc;
				if (exc instanceof java.lang.InterruptedException)
					throw (java.lang.InterruptedException) exc;
				throw new java.lang.reflect.InvocationTargetException(exc);
			} finally {
				monitor.done();
			}
		}

		public Shell getShell() {
			Shell activeShell = SystemBasePlugin.getActiveWorkbenchShell();
			if (activeShell != null) {
				return activeShell;
			}

			IWorkbenchWindow window = null;
			try {
				window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			} catch (Exception e) {
				return null;
			}
			if (window == null) {
				IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
						.getWorkbenchWindows();
				if (windows != null && windows.length > 0) {
					return windows[0].getShell();
				}
			} else {
				return window.getShell();
			}

			return null;
		}

	    /**
		 * Get the progress monitor dialog for this operation. We try to use one
		 * for all phases of a single operation, such as connecting and
		 * resolving.
		 */
		protected IRunnableContext getRunnableContext(Shell rshell) {
			Shell shell = getShell();
			// for other cases, use statusbar
			IWorkbenchWindow win = SystemBasePlugin.getActiveWorkbenchWindow();
			if (win != null) {
				Shell winShell = RSEUIPlugin.getDefault().getWorkbench()
						.getActiveWorkbenchWindow().getShell();
				if (winShell != null && !winShell.isDisposed()
						&& winShell.isVisible()) {
					SystemBasePlugin
							.logInfo("Using active workbench window as runnable context"); //$NON-NLS-1$
					shell = winShell;
					return win;
				} else {
					win = null;
				}
			}
			if (shell == null || shell.isDisposed() || !shell.isVisible()) {
				SystemBasePlugin
						.logInfo("Using progress monitor dialog with given shell as parent"); //$NON-NLS-1$
				shell = rshell;
			}
			IRunnableContext dlg = new ProgressMonitorDialog(rshell);
			return dlg;
		}

	    /**
		 * Show an error message when the disconnection fails. Shows a common
		 * message by default. Overridable.
		 */
	    protected void showDisconnectErrorMessage(Shell shell, String hostName, int port, Exception exc)
	    {
	         //SystemMessage.displayMessage(SystemMessage.MSGTYPE_ERROR,shell,RSEUIPlugin.getResourceBundle(),
	         //                             ISystemMessages.MSG_DISCONNECT_FAILED,
	         //                             hostName, exc.getMessage()); 	
	         //RSEUIPlugin.logError("Disconnect failed",exc); // temporary
	    	 SystemMessageDialog msgDlg = new SystemMessageDialog(shell,
	    	            RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DISCONNECT_FAILED).makeSubstitution(hostName,exc));
	    	 msgDlg.setException(exc);
	    	 msgDlg.open();
	    }	

	    /**
	     * Show an error message when the user cancels the disconnection.
	     * Shows a common message by default.
	     * Overridable.
	     */
	    protected void showDisconnectCancelledMessage(Shell shell, String hostName, int port)
	    {
	         //SystemMessage.displayMessage(SystemMessage.MSGTYPE_ERROR, shell, RSEUIPlugin.getResourceBundle(),
	         //                             ISystemMessages.MSG_DISCONNECT_CANCELLED, hostName);
	    	 SystemMessageDialog msgDlg = new SystemMessageDialog(shell,
	    	            RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DISCONNECT_CANCELLED).makeSubstitution(hostName));
	    	 msgDlg.open();
	    }
	}

    /** 
     * Notification from sub-services that our session was lost.
     * Notify all subsystems properly.
     * TODO allow user to try and reconnect?
     */
    public void handleSessionLost() {
    	Activator.trace("SshConnectorService: handleSessionLost"); //$NON-NLS-1$
    	if (fSessionLostHandler!=null) {
    		fSessionLostHandler.sessionLost();
    	}
	}

	private static Display getStandardDisplay() {
    	Display display = Display.getCurrent();
    	if( display==null ) {
    		display = Display.getDefault();
    	}
    	return display;
    }
    
    private static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
    	private String fPassphrase;
    	private String fPassword;
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
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					retval[0] = MessageDialog.openQuestion(null, SshConnectorResources.SshConnectorService_Warning, str); 
				}
			});
			return retval[0]; 
		}
		private String promptSecret(final String message) {
			final String[] retval = new String[1];
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					//TODO Write our own UserValidationDialog instead of re-using the internal one from team.ssh
					UserValidationDialog uvd = new UserValidationDialog(null, null,
							fUser, message);
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
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(null, SshConnectorResources.SshConnectorService_Info, message);
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
    
	public boolean isConnected() {
		if (session!=null) {
			if (session.isConnected()) {
				return true;
			} else if (fSessionLostHandler!=null) {
				Activator.trace("SshConnectorService.isConnected: false -> sessionLost"); //$NON-NLS-1$
				fSessionLostHandler.sessionLost();
			}
		}
		return false;
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
