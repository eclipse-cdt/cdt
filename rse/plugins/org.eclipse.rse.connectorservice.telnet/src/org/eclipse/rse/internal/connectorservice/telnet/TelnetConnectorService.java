/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * David Dykstal    (IBM)        - [168977] refactoring IConnectorService and ServerLauncher hierarchies
 * Sheldon D'souza  (Celunite)   - adapted from SshConnectorService
 * Martin Oberhuber (Wind River) - apply refactorings for StandardConnectorService
 * Martin Oberhuber (Wind River) - [178606] fix endless loop in readUntil()
 * Sheldon D'souza  (Celunite)   - [186536] login and password should be configurable
 * Sheldon D'souza  (Celunite)   - [186570] handle invalid user id and password more gracefully
 * Martin Oberhuber (Wind River) - [187218] Fix error reporting for connect()
 * Sheldon D'souza  (Celunite)   - [187301] support multiple telnet shells
 * Sheldon D'souza  (Celunite)   - [194464] fix create multiple telnet shells quickly
 * Martin Oberhuber (Wind River) - [186761] make the port setting configurable
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Anna Dushistova  (MontaVista) - [240523] [rseterminals] Provide a generic adapter factory that adapts any ITerminalService to an IShellService
 * Anna Dushistova  (MontaVista) - [198819] [telnet] TelnetConnectorService does not send CommunicationsEvent.BEFORE_CONNECT
 *******************************************************************************/
package org.eclipse.rse.internal.connectorservice.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.net.telnet.TelnetClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.PropertyType;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.internal.services.telnet.ITelnetSessionProvider;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.subsystems.StandardConnectorService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class TelnetConnectorService extends StandardConnectorService implements
		ITelnetSessionProvider {

	public static final String PROPERTY_SET_NAME = "Telnet Settings"; //$NON-NLS-1$
	public static final String PROPERTY_LOGIN_REQUIRED = "Login.Required"; //$NON-NLS-1$
	public static final String PROPERTY_LOGIN_PROMPT = "Login.Prompt"; //$NON-NLS-1$
	public static final String PROPERTY_PASSWORD_PROMPT = "Password.Prompt"; //$NON-NLS-1$
	public static final String PROPERTY_COMMAND_PROMPT = "Command.Prompt"; //$NON-NLS-1$

	private static final int TELNET_DEFAULT_PORT = 23; // TODO Make configurable
	private static final int TELNET_CONNECT_TIMEOUT = 60; //seconds - TODO: Make configurable
	private List fTelnetClients = new ArrayList();
	private SessionLostHandler fSessionLostHandler;
	private IPropertySet telnetPropertySet = null;
	private static final int ERROR_CODE = 100; // filed error code
	private static final int SUCCESS_CODE = 150; // login pass code
	private static final int CONNECT_CLOSED = 200; // code for end of login attempts
	private static final int CONNECT_CANCELLED = 250; // code for cancel progress

	public TelnetConnectorService(IHost host) {
		super(TelnetConnectorResources.TelnetConnectorService_Name,
				TelnetConnectorResources.TelnetConnectorService_Description,
				host, TELNET_DEFAULT_PORT);
		fSessionLostHandler = null;
		telnetPropertySet = getTelnetPropertySet();
	}

	/**
	 * Return the telnet property set, and fill it with default values if it has
	 * not been created yet. Extenders may override in order to set different
	 * default values.
	 *
	 * @return a property set holding properties understood by the telnet
	 *         connector service.
	 */
	protected IPropertySet getTelnetPropertySet() {
		IPropertySet telnetSet = getPropertySet(PROPERTY_SET_NAME);
		if (telnetSet == null) {
			telnetSet = createPropertySet(PROPERTY_SET_NAME,
					TelnetConnectorResources.PropertySet_Description);
			telnetSet.addProperty(PROPERTY_LOGIN_REQUIRED,
							"true", PropertyType.getEnumPropertyType(new String[] { "true", "false" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			telnetSet.addProperty(PROPERTY_LOGIN_PROMPT,
					"ogin: ", PropertyType.getStringPropertyType()); //$NON-NLS-1$
			telnetSet.addProperty(PROPERTY_PASSWORD_PROMPT,
					"assword: ", PropertyType.getStringPropertyType()); //$NON-NLS-1$
			telnetSet.addProperty(PROPERTY_COMMAND_PROMPT,
					"$", PropertyType.getStringPropertyType()); //$NON-NLS-1$
		}
		return telnetSet;
	}

	public static void checkCancelled(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}

	protected void internalConnect(IProgressMonitor monitor) throws Exception {
		try {
			// Fire comm event to signal state about to change
			fireCommunicationsEvent(CommunicationsEvent.BEFORE_CONNECT);
			TelnetClient client = makeNewTelnetClient(monitor);
			if( client != null ) {
				synchronized(this) {
					fTelnetClients.add(client);
					if (fSessionLostHandler==null) {
						fSessionLostHandler = new SessionLostHandler(this);
					}
				}
				notifyConnection();
			}
		}catch( Exception e) {
			if( e instanceof SystemMessageException ) {
				internalDisconnect( null );
				throw e;
			}
		}
	}

	protected int getTelnetPort() {
		int port = getPort();
		if (port<=0) {
			//Legacy "default port" setting
			port = TELNET_DEFAULT_PORT;
		}
		return port;
	}

	public TelnetClient makeNewTelnetClient(IProgressMonitor monitor ) throws Exception {
		TelnetClient client = new TelnetClient();
		return 	loginTelnetClient(client, monitor);
	}

	public TelnetClient loginTelnetClient(TelnetClient client, IProgressMonitor monitor) throws SystemMessageException {
		String host = getHostName();
		String user = getUserId();
		String password = ""; //$NON-NLS-1$
		int status = ERROR_CODE;
		Exception nestedException = null;
		try {
			Activator.trace("Telnet Service: Connecting....."); //$NON-NLS-1$
			client.connect(host, getTelnetPort());
			SystemSignonInformation ssi = getSignonInformation();
			if (ssi != null) {
				password = ssi.getPassword();
			}

			long millisToEnd = System.currentTimeMillis() + TELNET_CONNECT_TIMEOUT*1000;
			LoginThread checkLogin = new LoginThread(user, password,client.getInputStream(),new PrintStream( client.getOutputStream() ));
			checkLogin.start();
			while (checkLogin.isAlive() && System.currentTimeMillis()<millisToEnd) {
				if (monitor!=null) {
					monitor.worked(1);
					if (monitor.isCanceled()) {
						status = CONNECT_CANCELLED;
						//Thread will be interrupted by sessionDisconnect()
						//checkLogin.interrupt();
						break;
					}
				}
				Display d = Display.getCurrent();
				if (d!=null) {
					while(d.readAndDispatch()) {
						//get next event if on dispatch thread
					}
				}
				checkLogin.join(500);
			}
			if (status != CONNECT_CANCELLED) {
				status = checkLogin.getLoginStatus();
				checkLogin.join();
			}
		} catch (Exception e) {
			Activator.trace("Telnet Service failed: " + e.toString()); //$NON-NLS-1$
			nestedException = e;
		} finally {
			if (status == CONNECT_CANCELLED) {
				Activator.trace("Telnet Service: Cancelled"); //$NON-NLS-1$
				try {
					client.disconnect(); //will eventually destroy the LoginThread
				} catch(Exception e) {
					/*ignore on forced disconnect*/
				}
				client = null;
			} else if (status == SUCCESS_CODE) {
				Activator.trace("Telnet Service: Connected"); //$NON-NLS-1$
			} else {
				Activator.trace("Telnet Service: Connect failed"); //$NON-NLS-1$
				//TODO pass the nested exception as well as original prompts
				//from the remote side with the SystemMessageException for user diagnostics
				SystemMessage msg;
				if (nestedException!=null) {
					msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
							ICommonMessageIds.MSG_EXCEPTION_OCCURRED,
							IStatus.ERROR,
							CommonMessages.MSG_EXCEPTION_OCCURRED, nestedException);
				} else {
					msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
							ICommonMessageIds.MSG_COMM_AUTH_FAILED,
							IStatus.ERROR,
							CommonMessages.MSG_COMM_AUTH_FAILED,
							NLS.bind(CommonMessages.MSG_COMM_AUTH_FAILED_DETAILS, getHost().getAliasName()));

					msg.makeSubstitution(getHost().getAliasName());
				}
				throw new SystemMessageException(msg);
			}
		}
		return client;
	}

	/**
	 * Disconnect the telnet session. Synchronized in order to avoid NPE's from
	 * commons.net when called quickly in succession.
	 */
	private synchronized void sessionDisconnect() {
		Activator.trace("TelnetConnectorService.sessionDisconnect"); //$NON-NLS-1$
		Iterator it = fTelnetClients.iterator();
		while (it.hasNext()) {
			TelnetClient client = (TelnetClient)it.next();
			if (client.isConnected()) {
				try {
					client.disconnect();
				} catch(IOException e) {
					// Avoid NPE on disconnect shown in UI
					// This is a non-critical exception so print only in debug mode
					if (Activator.isTracingOn())
						e.printStackTrace();
				}
			}
			it.remove();
		}
	}

	public int readUntil(String pattern,InputStream in) {
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			StringBuffer sb = new StringBuffer();
			int ch = in.read();
			while (ch >= 0) {
				char tch = (char) ch;
				if (Activator.isTracingOn())
					System.out.print(tch);
				sb.append(tch);
				if (tch=='t' && sb.indexOf("incorrect") >= 0) { //$NON-NLS-1$
					return ERROR_CODE;
				}
				if (tch=='d' && sb.indexOf("closed") >= 0) { //$NON-NLS-1$
					return CONNECT_CLOSED;
				}
				if (tch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						return SUCCESS_CODE;
					}
				}
				ch = in.read();
			}
		} catch (Exception e) {
			SystemBasePlugin.logError(e.getMessage() == null ? 	e.getClass().getName() : e.getMessage(), e);
		}
		return CONNECT_CLOSED;
	}

	public void write(String value,PrintStream out) {
		try {
			out.println(value);
			out.flush();
			if (Activator.isTracingOn()) {
				// Avoid printing password to stdout
				// Activator.trace("write: "+value ); //$NON-NLS-1$
				int len = value.length() + 6;
				Activator.trace("write: ******************".substring(0, len <= 24 ? len : 24)); //$NON-NLS-1$
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void internalDisconnect(IProgressMonitor monitor)
			throws Exception {

		Activator.trace("Telnet Service: Disconnecting ....."); //$NON-NLS-1$

		boolean sessionLost = (fSessionLostHandler != null && fSessionLostHandler.isSessionLost());
		// no more interested in handling session-lost, since we are
		// disconnecting anyway
		fSessionLostHandler = null;
		// handle events
		if (sessionLost) {
			notifyError();
		} else {
			// Fire comm event to signal state about to change
			fireCommunicationsEvent(CommunicationsEvent.BEFORE_DISCONNECT);
		}

		sessionDisconnect();

		// Fire comm event to signal state changed
		notifyDisconnection();
	}

	/**
	 * Handle session-lost events. This is generic for any sort of connector
	 * service. Most of this is extracted from dstore's
	 * ConnectionStatusListener.
	 *
	 * TODO should be refactored to make it generally available, and allow
	 * dstore to derive from it.
	 */
	public static class SessionLostHandler implements Runnable,
			IRunnableWithProgress {
		private IConnectorService _connection;
		private boolean fSessionLost;

		public SessionLostHandler(IConnectorService cs) {
			_connection = cs;
			fSessionLost = false;
		}

		/**
		 * Notify that the connection has been lost. This may be called multiple
		 * times from multiple subsystems. The SessionLostHandler ensures that
		 * actual user feedback and disconnect actions are done only once, on
		 * the first invocation.
		 */
		public void sessionLost() {
			// avoid duplicate execution of sessionLost
			boolean showSessionLostDlg = false;
			synchronized (this) {
				if (!fSessionLost) {
					fSessionLost = true;
					showSessionLostDlg = true;
				}
			}
			if (showSessionLostDlg) {
				// invokes this.run() on dispatch thread
				Display.getDefault().asyncExec(this);
			}
		}

		public synchronized boolean isSessionLost() {
			return fSessionLost;
		}

		public void run() {
			Shell shell = getShell();
			try {
				// TODO I think we should better use a Job for disconnecting?
				// But what about error messages?
				IRunnableContext runnableContext = getRunnableContext(getShell());
				// will do this.run(IProgressMonitor mon)
				// runnableContext.run(false,true,this); // inthread,
				// cancellable, IRunnableWithProgress
				runnableContext.run(true, true, this); // fork, cancellable,
														// IRunnableWithProgress
				_connection.reset();
				ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
				sr.connectedStatusChange(_connection.getPrimarySubSystem(),
						false, true, true);
			} catch (Exception e) {
				SystemBasePlugin.logError(TelnetConnectorResources.TelnetConnectorService_ErrorDisconnecting, e);
			}
			// TODO need a more correct message for "session lost"
			// TODO allow users to reconnect from this dialog
			// SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_UNKNOWNHOST);

			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ICommonMessageIds.MSG_CONNECT_CANCELLED,
					IStatus.CANCEL,
					NLS.bind(CommonMessages.MSG_CONNECT_CANCELLED, _connection.getHost().getAliasName()));

			SystemMessageDialog dialog = new SystemMessageDialog(getShell(), msg);
			dialog.open();
		}

		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
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
		protected void showDisconnectErrorMessage(Shell shell, String hostName,
				int port, Exception exc) {
			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ICommonMessageIds.MSG_DISCONNECT_FAILED,
					IStatus.ERROR,
					NLS.bind(CommonMessages.MSG_DISCONNECT_FAILED, hostName), exc);

			SystemMessageDialog msgDlg = new SystemMessageDialog(shell,msg);
			msgDlg.setException(exc);
			msgDlg.open();
		}

		/**
		 * Show an error message when the user cancels the disconnection. Shows
		 * a common message by default. Overridable.
		 */
		protected void showDisconnectCancelledMessage(Shell shell,
				String hostName, int port) {
			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ICommonMessageIds.MSG_DISCONNECT_CANCELLED,
					IStatus.CANCEL,
					NLS.bind(CommonMessages.MSG_DISCONNECT_CANCELLED, hostName));
			SystemMessageDialog msgDlg = new SystemMessageDialog(shell,msg);
			msgDlg.open();
		}
	}

	/*
	 * A Login Thread to catch errors during login into telnet session
	 */

	private class LoginThread extends Thread {

		private String username;
		private String password;
		private int status = SUCCESS_CODE;
		private InputStream in;
		private PrintStream out;

		public LoginThread(String username, String password,InputStream in,PrintStream out) {
			this.username = username;
			this.password = password;
			this.in = in;
			this.out = out;
		}

		public void run() {

			telnetPropertySet = getTelnetPropertySet();
			String login_required = telnetPropertySet
					.getPropertyValue(PROPERTY_LOGIN_REQUIRED);
			String login_prompt = telnetPropertySet
					.getPropertyValue(PROPERTY_LOGIN_PROMPT);
			String password_prompt = telnetPropertySet
					.getPropertyValue(PROPERTY_PASSWORD_PROMPT);
			String command_prompt = telnetPropertySet
					.getPropertyValue(PROPERTY_COMMAND_PROMPT);

			if (Boolean.valueOf(login_required).booleanValue()) {
				status = SUCCESS_CODE;
				if (login_prompt != null && login_prompt.length() > 0) {
					status = readUntil(login_prompt,this.in);
					write(username,this.out);
				}
				if (status == SUCCESS_CODE && password_prompt != null && password_prompt.length() > 0) {
					status = readUntil(password_prompt,this.in);
					write(password,this.out);
				}
				if (status == SUCCESS_CODE && command_prompt != null && command_prompt.length() > 0) {
					status = readUntil(command_prompt,this.in);
				}
			} else {
				if (command_prompt != null && command_prompt.length() > 0) {
					status = readUntil(command_prompt,this.in);
				}
			}
		}

		public int getLoginStatus() {
			return this.status;
		}
	}

	protected static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	public boolean isConnected() {
		boolean anyConnected = false;
		synchronized(this) {
			Iterator it = fTelnetClients.iterator();
			while (it.hasNext()) {
				TelnetClient client = (TelnetClient)it.next();
				if (client.isConnected()) {
					anyConnected = true;
				} else {
					it.remove();
				}
			}
		}
		if (!anyConnected && fSessionLostHandler != null) {
			Activator.trace("TelnetConnectorService.isConnected: false -> sessionLost"); //$NON-NLS-1$
			fSessionLostHandler.sessionLost();
		}
		return anyConnected;
	}

	/**
	 * Test if this connector service requires a password. Telnet connector
	 * service returns false since a password is not necessarily required, i.e.
	 * the corresponding password field may be empty.
	 *
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#requiresPassword()
	 * @return false
	 */
	public boolean requiresPassword() {
		return false;
	}

	/**
	 * Test if this connector service requires a user id. Telnet connector
	 * service returns false since a user id is not necessarily required, i.e.
	 * the corresponding user id field may be empty.
	 *
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#requiresPassword()
	 * @return false
	 */
	public boolean requiresUserId() {
		return false;
	}

	/**
	 * Test if this connector service requires logging in.
	 *
	 * @return false if the Property {@link #PROPERTY_LOGIN_REQUIRED} is set and
	 *         false. Returns true otherwise.
	 */
	protected boolean supportsLogin() {
		boolean result = true;
		if (telnetPropertySet != null) {
			String login_required = telnetPropertySet.getPropertyValue(PROPERTY_LOGIN_REQUIRED);
			if (login_required != null && login_required.equalsIgnoreCase("false")) { //$NON-NLS-1$
				result = false;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.subsystems.StandardConnectorService#supportsPassword()
	 */
	public boolean supportsPassword() {
		return supportsLogin();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.subsystems.StandardConnectorService#supportsUserId()
	 */
	public boolean supportsUserId() {
		return supportsLogin();
	}

}
