/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Martin Oberhuber (Wind River) - [175686] Adapted to new IJSchService API
 *    - copied code from org.eclipse.team.cvs.ssh2/JSchSession (Copyright IBM)
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [186761] make the port setting configurable
 * Martin Oberhuber (Wind River) - [198790] make SSH createSession() protected
 * Martin Oberhuber (Wind River) - [203500] Support encodings for SSH Sftp paths
 * Martin Oberhuber (Wind River) - [155026] Add keepalives for SSH connection
 * David McKnight (IBM) - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight (IBM) - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Johnson Ma (Wind River) - [218880] Add UI setting for ssh keepalives
 * Martin Oberhuber (Wind River) - [227135] Cryptic exception when sftp-server is missing
 *******************************************************************************/

package org.eclipse.rse.internal.connectorservice.ssh;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ILabeledObject;
import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.PropertyType;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.internal.services.ssh.ISshSessionProvider;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemOperationFailedException;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.subsystems.StandardConnectorService;

/**
 * Create SSH connections.
 */
public class SshConnectorService extends StandardConnectorService implements ISshSessionProvider
{
	private static final int SSH_DEFAULT_PORT = 22;
	private static final int CONNECT_DEFAULT_TIMEOUT = 60; //seconds

	/** Property Keys. These are API because they are stored persistently. */
	private static final String PROPERTY_SET_SSH_SETTINGS = "SSH Settings"; //$NON-NLS-1$
	private static final String PROPERTY_KEY_TIMEOUT = "timeout(sec)"; //$NON-NLS-1$
	private static final String PROPERTY_KEY_KEEPALIVE = "keepalive(sec)"; //$NON-NLS-1$

	private Session session;
	private SessionLostHandler fSessionLostHandler;
	/** Indicates the default string encoding on this platform */
	private static String _defaultEncoding = new java.io.InputStreamReader(new java.io.ByteArrayInputStream(new byte[0])).getEncoding();

	public SshConnectorService(IHost host) {
		super(SshConnectorResources.SshConnectorService_Name, SshConnectorResources.SshConnectorService_Description, host, SSH_DEFAULT_PORT);
		fSessionLostHandler = null;
		getPropertySet();
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

		IPropertySet propertySet = getPropertySet();
		String timeoutStr = propertySet.getPropertyValue(PROPERTY_KEY_TIMEOUT);
		int timeout = 0; //default is never timeout
		try {
			int value = Integer.parseInt(timeoutStr);
			if (value > 0) {
				timeout = value * 1000;
			}
		} catch (NumberFormatException e) {
			//wrong input - should never happen because property type is Integer
		}
		session.setTimeout(timeout);

		int keepalive = 300000; //default is 5 minutes
		String keepaliveStr = propertySet.getPropertyValue(PROPERTY_KEY_KEEPALIVE);
		try {
			int value = Integer.parseInt(keepaliveStr);
			if (value >= 0) {
				keepalive = value * 1000;
			}
		} catch (NumberFormatException e) {
			//wrong input - should never happen because property type is Integer
		}
		if (keepalive > 0) {
			session.setServerAliveInterval(keepalive);
		}


		session.setServerAliveCountMax(6); //give up after 6 tries (remote will be dead after 30 min)
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

	protected int getSshPort() {
		int port = getPort();
		if (port<=0) {
			//Legacy "default port" setting
			port = SSH_DEFAULT_PORT;
		}
		return port;
	}

	protected void internalConnect(IProgressMonitor monitor) throws Exception
	{
		// Fire comm event to signal state about to change
		fireCommunicationsEvent(CommunicationsEvent.BEFORE_CONNECT);

		String host = getHostName();
		String user = getUserId();
		String password=""; //$NON-NLS-1$
		SystemSignonInformation ssi = getSignonInformation();
		if (ssi!=null) {
			password = getSignonInformation().getPassword();
		}
		MyUserInfo userInfo = new MyUserInfo(user, password);
		userInfo.aboutToConnect();

		try {
			session = createSession(user, password, host, getSshPort(),
					userInfo, monitor);

			//java.util.Hashtable config=new java.util.Hashtable();
			//config.put("StrictHostKeyChecking", "no");
			//session.setConfig(config);
			userInfo.aboutToConnect();

			Activator.trace("SshConnectorService.connecting..."); //$NON-NLS-1$
			//wait for 60 sec maximum during connect
			session.connect(CONNECT_DEFAULT_TIMEOUT * 1000);
			Activator.trace("SshConnectorService.connected"); //$NON-NLS-1$
		} catch (JSchException e) {
			Activator.trace("SshConnectorService.connect failed: "+e.toString()); //$NON-NLS-1$
			sessionDisconnect();
			if(e.toString().indexOf("Auth cancel")>=0) {  //$NON-NLS-1$
				throw new OperationCanceledException();
			}
			throw new SystemOperationFailedException(Activator.PLUGIN_ID, NLS.bind(SshConnectorResources.SshConnectorService_Missing_sshd, 
					host, Integer.toString(getSshPort())), e);
		}
		userInfo.connectionMade();
		fSessionLostHandler = new SessionLostHandler(this);
		notifyConnection();
	}

	/**
	 * Disconnect the ssh session.
	 * Synchronized in order to avoid NPE's from Jsch when called
	 * quickly in succession.
	 */
	private synchronized void sessionDisconnect() {
		Activator.trace("SshConnectorService.sessionDisconnect"); //$NON-NLS-1$
		try {
			if (session.isConnected())
				session.disconnect();
		} catch(Exception e) {
			//Bug 175328: NPE on disconnect shown in UI
			//This is a non-critical exception so print only in debug mode
			if (Activator.isTracingOn()) e.printStackTrace();
		}
	}

	protected void internalDisconnect(IProgressMonitor monitor) throws Exception
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
				sessionDisconnect();

				// Fire comm event to signal state changed
				notifyDisconnection();
				//TODO MOB - keep the session to avoid NPEs in services (disables gc for the session!)
				// session = null;
				// DKM - no need to clear uid cache
				clearPassword(false, true); // clear in-memory password
				//clearUserIdCache(); // Clear any cached local user IDs
			}
		}
		catch (Exception exc)
		{
			throw new java.lang.reflect.InvocationTargetException(exc);
		}
	}

	//TODO avoid having jsch type "Session" in the API.
	// Could be done by instantiating SshShellService and SshFileService here,
	//and implementing IShellService getShellService()
	//and IFileService getFileService().
	public Session getSession() {
		return session;
	}

	public String getControlEncoding() {
		//TODO this code should be in IHost
		String encoding = getHost().getDefaultEncoding(true);
		if (encoding==null) encoding = _defaultEncoding;
		//</code to be in IHost>
		return encoding;
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
			try
			{
				//TODO I think we should better use a Job for disconnecting?
				//But what about error messages?
				IRunnableContext runnableContext = getRunnableContext(getShell());
				// will do this.run(IProgressMonitor mon)
				//runnableContext.run(false,true,this); // inthread, cancellable, IRunnableWithProgress
				runnableContext.run(true,true,this); // fork, cancellable, IRunnableWithProgress
				_connection.reset();
				ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
				sr.connectedStatusChange(_connection.getPrimarySubSystem(), false, true, true);
			}
			catch (Exception e)
			{
				SystemBasePlugin.logError(SshConnectorResources.SshConnectorService_ErrorDisconnecting, e);
			}
			//TODO need a more correct message for "session lost"
			//TODO allow users to reconnect from this dialog
			//SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_UNKNOWNHOST);

			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ICommonMessageIds.MSG_CONNECT_CANCELLED, IStatus.CANCEL,
					NLS.bind(CommonMessages.MSG_CONNECT_CANCELLED,
							_connection.getHost().getAliasName()));

			SystemMessageDialog dialog = new SystemMessageDialog(getShell(), msg);
			dialog.open();
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
			//RSEUIPlugin.logError("Disconnect failed",exc); // temporary
			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID, ICommonMessageIds.MSG_DISCONNECT_FAILED, IStatus.ERROR, NLS.bind(CommonMessages.MSG_DISCONNECT_FAILED, hostName), exc);
			SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
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
			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID, ICommonMessageIds.MSG_DISCONNECT_CANCELLED, IStatus.CANCEL, NLS.bind(CommonMessages.MSG_DISCONNECT_CANCELLED, hostName));
			SystemMessageDialog msgDlg = new SystemMessageDialog(shell,msg);
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

	protected static Display getStandardDisplay() {
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
			final String finUser = fUser;
			getStandardDisplay().syncExec(new Runnable() {
				public void run() {
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

	public boolean requiresPassword() {
		return false;
	}

	public boolean requiresUserId() {
		return false;
	}

	private IPropertySet getPropertySet()
	{
		IPropertySet propertySet = getPropertySet(PROPERTY_SET_SSH_SETTINGS);
		if(propertySet==null) {
			propertySet = createPropertySet(PROPERTY_SET_SSH_SETTINGS);
		}
		if (propertySet instanceof ILabeledObject) {
			//RSE 3.0 and later: translatable labels for properties
			String label = SshConnectorResources.SSH_SETTINGS_LABEL;
			((ILabeledObject)propertySet).setLabel(label);
		}

		//timeout
		IProperty p = propertySet.getProperty(PROPERTY_KEY_TIMEOUT);
		if (p==null) {
			p = propertySet.addProperty(PROPERTY_KEY_TIMEOUT, "0", PropertyType.getIntegerPropertyType()); //$NON-NLS-1$
		}
		p.setLabel(SshConnectorResources.PROPERTY_LABEL_TIMEOUT);

		//keepalive
		p = propertySet.getProperty(PROPERTY_KEY_KEEPALIVE);
		if (p==null) {
			p = propertySet.addProperty(PROPERTY_KEY_KEEPALIVE,"300", PropertyType.getIntegerPropertyType()); //$NON-NLS-1$
		}
		p.setLabel(SshConnectorResources.PROPERTY_LABEL_KEEPALIVE);
		return propertySet;
	}

}
