/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Sheldon D'souza (Celunite) - adapted from SshConnectorService
 * Martin Oberhuber (Wind River) - apply refactorings for StandardConnectorService
 * Martin Oberhuber (Wind River) - [178606] fix endless loop in readUntil()
 * Sheldon D'souza (Celunite) - [186536] login and password should be configurable
 *******************************************************************************/
package org.eclipse.rse.internal.connectorservice.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;

import org.apache.commons.net.telnet.TelnetClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.PropertyType;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ICredentials;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.internal.services.telnet.ITelnetSessionProvider;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.subsystems.StandardConnectorService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class TelnetConnectorService extends StandardConnectorService implements ITelnetSessionProvider  {
	
	public static final String PROPERTY_SET_NAME="Telnet Settings"; //$NON-NLS-1$
	public static final String PROPERTY_LOGIN_REQUIRED="Login.Required"; //$NON-NLS-1$
	public static final String PROPERTY_LOGIN_PROMPT="Login.Prompt"; //$NON-NLS-1$
	public static final String PROPERTY_PASSWORD_PROMPT="Password.Prompt"; //$NON-NLS-1$
	public static final String PROPERTY_COMMAND_PROMPT="Command.Prompt"; //$NON-NLS-1$
	
	private static final int TELNET_DEFAULT_PORT = 23;
	private static TelnetClient fTelnetClient = new TelnetClient();
	private SessionLostHandler fSessionLostHandler;
	private InputStream in;
    private PrintStream out;
	private IPropertySet telnetPropertySet = null;
	
	public TelnetConnectorService(IHost host) {
		super(TelnetConnectorResources.TelnetConnectorService_Name,
				TelnetConnectorResources.TelnetConnectorService_Description,
				host, 0);
		fSessionLostHandler = null;
		telnetPropertySet = getTelnetPropertySet();
	}
	
	/**
	 * Return the telnet property set, and fill it with default
	 * values if it has not been created yet.
	 * Extenders may override in order to set different default values. 
	 * @return a property set holding properties understood by the telnet
	 *    connector service.
	 */
	protected IPropertySet getTelnetPropertySet() {
		IPropertySet telnetSet = getPropertySet(PROPERTY_SET_NAME);
		if (telnetSet == null) {
			telnetSet = createPropertySet(PROPERTY_SET_NAME, TelnetConnectorResources.PropertySet_Description);
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

	public static void checkCanceled(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}
		
	protected void internalConnect(IProgressMonitor monitor) throws Exception {
		String host = getHostName();
	    String user = getUserId();
	    String password = ""; //$NON-NLS-1$
		telnetPropertySet = getTelnetPropertySet();
		String login_required = telnetPropertySet.getPropertyValue(PROPERTY_LOGIN_REQUIRED); 
		String login_prompt = telnetPropertySet.getPropertyValue(PROPERTY_LOGIN_PROMPT);
		String password_prompt = telnetPropertySet.getPropertyValue(PROPERTY_PASSWORD_PROMPT);
		String command_prompt = telnetPropertySet.getPropertyValue(PROPERTY_COMMAND_PROMPT);
        try {
        	Activator.trace("Telnet Service: Connecting....."); //$NON-NLS-1$
        	fTelnetClient.connect(host,TELNET_DEFAULT_PORT );
        	ICredentials cred = getCredentialsProvider().getCredentials();
            if (cred!=null) {
            	password = cred.getPassword();
            }
            
            in = fTelnetClient.getInputStream();
            out = new PrintStream( fTelnetClient.getOutputStream() );
            //Send login and password if needed
            if( Boolean.valueOf(login_required ).booleanValue() ) {
            	if (login_prompt!=null && login_prompt.length()>0) {
    	            readUntil(login_prompt);
    	            write(user);
            	}
            	if (password_prompt!=null && password_prompt.length()>0) {
    	            readUntil(password_prompt);
    	            write(password);
            	}
            }
            if (command_prompt!=null && command_prompt.length()>0) {
	            readUntil(command_prompt);
            }
        	Activator.trace("Telnet Service: Connected"); //$NON-NLS-1$
        }catch( SocketException se) {
        	Activator.trace("Telnet Service failed: "+se.toString()); //$NON-NLS-1$
        	sessionDisconnect();
        }catch( IOException ioe ) {
        	Activator.trace("Telnet Service failed: "+ioe.toString()); //$NON-NLS-1$
        	sessionDisconnect();
        }
        
		fSessionLostHandler = new SessionLostHandler( this );
		notifyConnection();
		
	}
	
	/**
	 * Disconnect the telnet session.
	 * Synchronized in order to avoid NPE's from commons.net when called
	 * quickly in succession.
	 */
	private synchronized void sessionDisconnect() {
    	Activator.trace("TelnetConnectorService.sessionDisconnect"); //$NON-NLS-1$
    	try {
        	if (fTelnetClient!=null && fTelnetClient.isConnected())
        		fTelnetClient.disconnect();
    	} catch(Exception e) {
    		//Avoid NPE on disconnect shown in UI
    		//This is a non-critical exception so print only in debug mode
    		if (Activator.isTracingOn()) e.printStackTrace();
    	}
	}
	
	public String readUntil(String pattern) {
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			StringBuffer sb = new StringBuffer();
			int ch = (char) in.read();
			while (ch >= 0) {
				char tch = (char) ch;
				if (Activator.isTracingOn())
					System.out.print(tch);
				sb.append(tch);
				if (tch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						return sb.toString();
					}
				}
				ch = in.read();
			}
		} catch (Exception e) {
			SystemBasePlugin.logError(e.getMessage()==null ? e.getClass().getName() : e.getMessage(), e);
		}
		return null;
	}
	
	public void write( String value ) {
	   try {
		 out.println( value );
		 out.flush();
		 if (Activator.isTracingOn()) {
			 //Avoid printing password to stdout
			 //Activator.trace("write: "+value ); //$NON-NLS-1$
			 int len = value.length()+6;
			 Activator.trace("write: ******************".substring(0, len<=24 ? len : 24)); //$NON-NLS-1$
		 }
	   }
	   catch( Exception e ) {
		 e.printStackTrace();
	   }
	}
					 
	protected void internalDisconnect(IProgressMonitor monitor) throws Exception {
		
		Activator.trace("Telnet Service: Disconnecting ....."); //$NON-NLS-1$
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
	}
	
	public TelnetClient getTelnetClient() {
		return fTelnetClient;
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
				ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();    	    
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
				SystemBasePlugin.logError(TelnetConnectorResources.TelnetConnectorService_ErrorDisconnecting, e);
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

	/* Notification from sub-services that our session was lost.
    * Notify all subsystems properly.
    * TODO allow user to try and reconnect?
    */
	public void handleSessionLost() {
	   	Activator.trace("TelnetConnectorService: handleSessionLost"); //$NON-NLS-1$
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
	
	public boolean isConnected() {
		boolean connected;
		synchronized(this) {
			connected = fTelnetClient.isConnected();
		}
		if (!connected && fSessionLostHandler!=null) {
			Activator.trace("TelnetConnectorService.isConnected: false -> sessionLost"); //$NON-NLS-1$
			fSessionLostHandler.sessionLost();
		}
		return connected;
	}

	/**
	 * @return false
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#requiresPassword()
	 */
	public boolean requiresPassword() {
		return false;
	}
	
}
