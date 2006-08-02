/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.connectorservice.dstore;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.client.ClientConnection;
import org.eclipse.dstore.core.client.ClientSSLProperties;
import org.eclipse.dstore.core.client.ConnectionStatus;
import org.eclipse.dstore.core.java.IRemoteClassInstance;
import org.eclipse.dstore.core.java.RemoteClassLoader;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.IDataStoreConstants;
import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.dstore.core.model.ISSLProperties;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.connectorservice.dstore.util.ConnectionStatusListener;
import org.eclipse.rse.connectorservice.dstore.util.StatusMonitor;
import org.eclipse.rse.connectorservice.dstore.util.StatusMonitorFactory;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.comm.ISystemKeystoreProvider;
import org.eclipse.rse.core.comm.SystemKeystoreProviderManager;
import org.eclipse.rse.core.subsystems.AbstractConnectorService;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.IRemoteServerLauncher;
import org.eclipse.rse.core.subsystems.IServerLauncher;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ServerLaunchType;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.dstore.universal.miners.environment.EnvironmentMiner;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.SystemSignonInformation;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.actions.DisplayHidableSystemMessageAction;
import org.eclipse.rse.ui.actions.DisplaySystemMessageAction;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
/**
 * System class required by the remote systems framework.
 * This represents the live connection at tool runtime.
 * <p>
 * The universal subsystems are based on datastore technology so we use that
 * to do the connection.
 */
public class DStoreConnectorService extends AbstractConnectorService implements IDataStoreProvider
{
	
	private ClientConnection clientConnection = null;
	private ConnectionStatusListener _connectionStatusListener = null;

	// Shortcut to sysInfo to save time
	private transient DataElement sysInfo = null;
	private transient DataElement installInfo = null;
	private transient DataElement clientIP = null;
	private static String DSTORE_PACKAGE = "org.eclipse.dstore.core";
	
	private Exception connectException;
	//private Hashtable restrictedTypes = null;
	private IServerLauncher starter;	

	
	private class ShowConnectMessage implements Runnable
	{
		private SystemMessage _msg;
		public ShowConnectMessage(SystemMessage msg)
		{
			_msg = msg;
		}
		
		public void run()
		{
			SystemMessageDialog dlg = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), _msg);
			dlg.open();
		}
	}
	
	/**
	 * Constructor when we don't have a subsystem yet.
	 * Call setSubSystem after. 
	 */
	public DStoreConnectorService(String name, String description, IHost host)
	{
		super(name, description, host, 0);
	}
	
	
	
	/*
	 * Set the subsystem, when its not known at constructor time
	 *
	public void setSubSystem(SubSystem ss)
	{
		super.setSubSystem(ss);
		setDaemonLaunchEnabled((SubSystemImpl)ss, false);		    	
	}*/
	
	
	public int getServerVersion()
	{
		return clientConnection.getServerVersion();
	}
	
	public int getServerMinor()
	{
		return clientConnection.getServerMinor();
	}
	
	/**
	 * Retrieve the value of a property which is contained in the environment miners
	 * system info child.  Currently supported properties:
	 * <ul>
	 * 		<li>os.name
	 * 		<li>os.version
	 *  	<li>user.home
	 *  	<li>temp.dir
	 * </ul>
	 * @return The String value of the property or "" if the property was not found.
	 */
	private String getSystemInfoProperty(String propertyName)
	{
		// System properties require a connection
		if (!isConnected())
		{
			try
			{
				getPrimarySubSystem().connect();
			}
			catch (Exception e)
			{
				SystemBasePlugin.logError("UniversalSystem.getSystemInfoProperty: error during connect", e);
				return "";
			}
		}

		String propertyValue = null;
		DataElement envMinerData = null;
		DataStore ds = getDataStore();

		// Check if we have sysInfo cached already
		if (sysInfo == null)
		{
			envMinerData = ds.findMinerInformation(EnvironmentMiner.MINER_ID);
			if (envMinerData != null)
			{
				sysInfo = ds.find(envMinerData, DE.A_NAME, "systemInfo", 1);
			}
		}

		if (sysInfo != null)
		{
			DataElement propertyNode = ds.find(sysInfo, DE.A_NAME, propertyName, 1);
			if (propertyNode != null)
			{
				propertyValue = propertyNode.getSource();
			}
			else
				propertyValue = "";
		}
		else
		{
			SystemBasePlugin.logError("UniversalSystem.getSystemInfoNode:  sysInfo node not found", null);
			SystemBasePlugin.logError("UniversalSystem.getSystemInfoNode:  miner data = " + envMinerData, null);
			propertyValue = "";
		}

		return propertyValue;
	}

	/**
	 * Return the version, release, modification of the remote system
	 */
	public String getVersionReleaseModification()
	{
		if (!isConnected())
			return SystemPropertyResources.RESID_TERM_NOTAVAILABLE;

		StringBuffer buffer = new StringBuffer(getSystemInfoProperty("os.name"));
		buffer.append(" ");
		buffer.append(getSystemInfoProperty("os.version"));

		return buffer.toString();
	}

	/**
	 * Return the home directory of the remote system for the current user, if available.
	 */
	public String getHomeDirectory()
	{
		return getSystemInfoProperty("user.home");
	}

	public boolean runClassInstanceRemotely(IRemoteClassInstance instance)
	{
		DataStore dataStore = getDataStore();
		dataStore.registerLocalClassLoader(instance.getClass().getClassLoader());
		dataStore.runRemoteClassInstance(instance);
		return true;
	}
	
	/**
	 * Return the location where the RSE server is installed
	 * @return the server install location
	 */
	public String getServerInstallPath()
	{		
	    if (clientConnection != null)
	    {
	    	if (installInfo == null)
	    	{
	    		DataStore ds = clientConnection.getDataStore();
	    		installInfo = ds.queryInstall();
	    	}
	        return installInfo.getAttribute(DE.A_SOURCE);
	    }
	    return "";
	}
	
	/**
	 * Return the Client IP that the RSE server is connected to.  When connected,
	 * the client IP is obtained from the server-side.   When not-connected, 
	 * the fall back is to get the IP locally (note that the IP obtained locally
	 * may be not be what you want when using VPN).
	 * @return the client ip
	 */
	public String getClientIP()
	{
	    if (clientConnection != null && clientConnection.isConnected())
	    {	        
	        if (clientIP == null)
	        {	        
	        	DataStore ds = clientConnection.getDataStore();
	        	clientIP = ds.queryClientIP();
	        }
	        return clientIP.getAttribute(DE.A_SOURCE);
	    }
	    
	    // fall back to getting local machine ip address
	    // this may be incorrect for the server in certain cases
	    // like over VPN
	    return RSECorePlugin.getLocalMachineIPAddress();
	}
	
	/**
	 * Return the temp directory of the remote system for the current user, if available.
	 */
	public String getTempDirectory()
	{
		return getSystemInfoProperty("temp.dir");
	}
	
	protected int getSocketTimeOutValue()
	{
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		return store.getInt(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT);
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#disconnect()
	 */
	public void internalDisconnect(IProgressMonitor monitor) throws Exception
	{
		try
		{
			if (clientConnection != null)
			{
				// Is disconnect being called because the network (connection) went down?
				if (_connectionStatusListener != null && _connectionStatusListener.isConnectionDown())
				{
					notifyError();
				}
				else
				{
					// Fire comm event to signal state about to change
					fireCommunicationsEvent(CommunicationsEvent.BEFORE_DISCONNECT);
				}

				DataStore dataStore = getDataStore();
				if (dataStore != null && _connectionStatusListener != null)
				{
					dataStore.getDomainNotifier().removeDomainListener(_connectionStatusListener);
				}

				clientConnection.disconnect();
				
//				 Fire comm event to signal state changed
				notifyDisconnection();
				clientConnection = null;
				// DKM - no need to clear uid cache
				clearPasswordCache(false); // clear in-memory password
				//clearUserIdCache(); // Clear any cached local user IDs
				sysInfo = null;
				installInfo = null;
				clientIP = null;

				
			}
		}
		catch (Exception exc)
		{
			throw new java.lang.reflect.InvocationTargetException(exc);
		}
	}
	
	private IRemoteServerLauncher getDStoreServerLauncher()
	{
		IServerLauncherProperties sl = getRemoteServerLauncherProperties();
		//System.out.println("in UniversalSystem#getServerLauncher: sl = "+sl);
		if (sl != null && sl instanceof IRemoteServerLauncher)
		{			
			return (IRemoteServerLauncher)sl;
		}	
		else
			//return ((SubSystemConfigurationImpl)ss.getParentSubSystemConfiguration()).getDefaultIBMServerLauncher(ss);
			return null; // should never happen!		
	}

	protected void setPluginPathProperty()
	{
		Bundle bundle = RSEUIPlugin.getDefault().getBundle();
		URL pluginsURL = bundle.getEntry("/");
	
		try
		{
			String path = FileLocator.resolve(pluginsURL).getPath();
			File systemsPluginDir = new File(path);
			path = systemsPluginDir.getParentFile().getAbsolutePath();
			String version = (String)(bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION));
			Version v = new Version(version);
			String versionString = v.toString();
			String dstorePath = getDStorePath(path, versionString);
			System.setProperty("A_PLUGIN_PATH", dstorePath);
		}
		catch (IOException e)
		{
		}
	}
	
	private String getDStorePath(String pluginDir, String version)
	{
			File dstorePath = new File(pluginDir + "/" + DSTORE_PACKAGE + "_" + version);
			if (!dstorePath.exists())
			{
				// might be in workspace
				dstorePath = new File(pluginDir + "/" + DSTORE_PACKAGE);
			}
	
		return dstorePath.getAbsolutePath();
	}
	
	/**
	 * Specify if you support connecting to a running daemon  
	 * @deprecated use {@link #enableServerLaunchType(ISubSystem, ServerLaunchType, boolean)}
	 *  or your subsystem factory should override {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchType(ServerLaunchType)} 
	 */	
	public void setDaemonLaunchEnabled(SubSystem subsystemImpl, boolean enable) {
		enableServerLaunchType(subsystemImpl, ServerLaunchType.DAEMON_LITERAL, enable);
	}

	/**
	 * Return if you support connecting to a running daemon  
	 * @deprecated Use instead {@link #isEnabledServerLaunchType(ISubSystem, ServerLaunchType)} 
	 *  or {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchType(ServerLaunchType)}
	 */		
	public boolean getDaemonLaunchEnabled(SubSystem subsystemImpl) {
		return isEnabledServerLaunchType(subsystemImpl, ServerLaunchType.DAEMON_LITERAL); 
	}

	/**
	 * Specify if you support remotely launching a server script 
	 * @deprecated use {@link #enableServerLaunchType(ISubSystem, ServerLaunchType, boolean)} 
	 *  or your subsystem factory should override {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchType(ServerLaunchType)} 
	 */		
	public void setRexecLaunchEnabled(SubSystem subsystemImpl, boolean enable) {
		enableServerLaunchType(subsystemImpl, ServerLaunchType.REXEC_LITERAL, enable);
	}

	/**
	 * Return if you support remotely launching a server script 
	 * @deprecated Use instead {@link #isEnabledServerLaunchType(ISubSystem, ServerLaunchType)} 
	 *  or {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchType(ServerLaunchType)}
	 */			
	public boolean getRexecLaunchEnabled(SubSystem subsystemImpl) {
		return isEnabledServerLaunchType(subsystemImpl, ServerLaunchType.REXEC_LITERAL);
	}

	/**
	 * Specify if you support connecting to a server already running 
	 * @deprecated use {@link #enableServerLaunchType(ISubSystem, ServerLaunchType, boolean)} 
	 *  or your subsystem factory should override {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchType(ServerLaunchType)} 
	 */			
	public void setNoLaunchEnabled(SubSystem subsystemImpl, boolean enable) {
		enableServerLaunchType(subsystemImpl, ServerLaunchType.RUNNING_LITERAL, enable);
	}

	/**
	 * Return if you support connecting to a server already running 
	 * @deprecated Use instead {@link #isEnabledServerLaunchType(ISubSystem, ServerLaunchType)} 
	 *  or {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchType(ServerLaunchType)} 
	 */			
	public boolean getNoLaunchEnabled(SubSystem subsystemImpl) {
		return isEnabledServerLaunchType(subsystemImpl, ServerLaunchType.RUNNING_LITERAL);
	}	
	
	/**
	 * Return the remote server launcher, which implements IServerLauncher.
	 * This is called by the default implementation of connect, if 
	 * subsystem.getParentSubSystemConfiguration().supportsServerLaunchProperties returns true.
	 */
	public IServerLauncher getRemoteServerLauncher()
	{
		if (starter == null)
		  starter = new RexecDstoreServer();
		((RexecDstoreServer)starter).setClientConnection(clientConnection);
		((RexecDstoreServer)starter).setSocketTimeoutValue(getSocketTimeOutValue());
		return starter;
	}
	

	
	/**
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#connect(IProgressMonitor)
	 */
	protected synchronized void internalConnect(IProgressMonitor monitor) throws Exception
	{
	    if (isConnected()) {
	        return;
	    }
	    
		// set A_PLUGIN_PATH so that dstore picks up the property
		setPluginPathProperty();
		
		// Fire comm event to signal state about to change
		fireCommunicationsEvent(CommunicationsEvent.BEFORE_CONNECT);

		ConnectionStatus connectStatus = null;
		ConnectionStatus launchStatus = null;

		clientConnection = new ClientConnection(getPrimarySubSystem().getHost().getAliasName());

		clientConnection.setHost(getHostName());
		clientConnection.setPort(Integer.toString(getPort()));

//		ISubSystem ss = getPrimarySubSystem();
		getPrimarySubSystem();
		IRemoteServerLauncher serverLauncher = getDStoreServerLauncher(); 
		
		ServerLaunchType serverLauncherType = null;
		boolean autoDetectSSL = true;
		if (serverLauncher != null)
		{
		    serverLauncherType = serverLauncher.getServerLaunchType();
		    autoDetectSSL = serverLauncher.getAutoDetectSSL();
		}
		else
		{
		  //  System.out.println("server launcher is null");
		}

		long t1 = System.currentTimeMillis();
		SystemMessage msg = null;	
		boolean launchFailed = false;

		// get Socket Timeout Value Preference
		int timeout = getSocketTimeOutValue();
		
		if (serverLauncherType == ServerLaunchType.REXEC_LITERAL)
		{	
			if (monitor != null)
			{
				SystemMessage cmsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_STARTING_SERVER_VIA_REXEC);
				monitor.subTask(cmsg.getLevelOneText());	
			}
			
			SystemSignonInformation info = getPasswordInformation();
			
			// GC: - if failed to get a connection in another way, try
			// starting the datastore server with rexec
			IServerLauncher starter = getRemoteServerLauncher();
			starter.setSignonInformation(info);
			starter.setServerLauncherProperties(serverLauncher);
			if (autoDetectSSL) timeout = 3000;
			else setSSLProperties(isUsingSSL());

			int iServerPort = launchUsingRexec(monitor, info, serverLauncher);
			
			if(iServerPort != 0)
			{				
				clientConnection.setPort("" + iServerPort);
				
				if (monitor != null)
				{
					SystemMessage cmsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECTING_TO_SERVER);
					cmsg.makeSubstitution(clientConnection.getPort());
					monitor.subTask(cmsg.getLevelOneText());
				}
				
				// connect to launched server
				connectStatus = clientConnection.connect(null, timeout);
				if (!connectStatus.isConnected() && connectStatus.getMessage().startsWith(ClientConnection.CANNOT_CONNECT) && autoDetectSSL)
				{
					if (setSSLProperties(true))
					{
						iServerPort = launchUsingRexec(monitor, info, serverLauncher);
						if (iServerPort != 0)
						{
							clientConnection.setPort("" + iServerPort);
							connectStatus = clientConnection.connect(null, timeout);
						}
					}
				}
			}
			else
			{
				launchFailed = true;
				connectStatus = new ConnectionStatus(false);
				msg = starter.getErrorMessage();
				String errorMsg = null;
				if (msg == null)
				{
					errorMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_CONNECT_FAILED).getLevelOneText();
				}
				else
				{
					errorMsg = msg.getLevelTwoText();
				}
				connectStatus.setMessage(errorMsg);
			}
		}
		// Start the server via the daemon
		else if (serverLauncherType == ServerLaunchType.DAEMON_LITERAL)
		{
			if (monitor != null)
			{
				SystemMessage cmsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_STARTING_SERVER_VIA_DAEMON);
				monitor.subTask(cmsg.getLevelOneText());		
			}

			// DY:  getLocalUserId() may return null for Windows connections because
			// we no longer prompt for userid / pwd.  But for other connections the userid 
			// should be the same as the one stored in the password info (and for Windows
			// this will be the temp remoteuser userid.
			//launchStatus = clientConnection.launchServer(getLocalUserId(), getPassword(getPasswordInformation()));
			SystemSignonInformation info = getPasswordInformation();
			int daemonPort = serverLauncher.getDaemonPort();
			
			/* String daemonPortStr = getSubSystem().getVendorAttribute("IBM", "DAEMON_PORT");
			if (daemonPortStr != null && daemonPortStr.length() > 0)
			{
				daemonPort = Integer.parseInt(daemonPortStr);
			}*/
			
			// DKM - changed to use protected member so that others can override
			//launchStatus = clientConnection.launchServer(info.getUserid(), info.getPassword(), daemonPort);
			boolean usedSSL = false;
			launchStatus = launchServer(clientConnection, info, daemonPort, monitor);
			if (!launchStatus.isConnected() && !clientConnection.isKnownStatus(launchStatus.getMessage()))
			{
				if (setSSLProperties(true))
				{
					usedSSL = true;
					launchStatus = launchServer(clientConnection, info, daemonPort, monitor);
				}
			}
			
			if (!launchStatus.isConnected())
			{
				String launchMsg = launchStatus.getMessage();
				// If password has expired and must be changed
				if (launchMsg != null && (isPasswordExpired(launchMsg) || isNewPasswordInvalid(launchMsg)))
				{
					NewPasswordInfo newPasswordInfo = null;
					while (launchMsg != null && (isPasswordExpired(launchMsg) || isNewPasswordInvalid(launchMsg)))
					{
						newPasswordInfo = promptForNewPassword(isPasswordExpired(launchMsg) ? RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PASSWORD_EXPIRED) : RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PASSWORD_INVALID));
						launchStatus = changePassword(clientConnection, getPasswordInformation(), serverLauncher, monitor, newPasswordInfo.newPassword);
						launchMsg = launchStatus.getMessage();
					}
					if (newPasswordInfo != null) 
					{
						setPassword(info.getUserid(), newPasswordInfo.newPassword, newPasswordInfo.savePassword);
						info = getPasswordInformation();
					}
					if (launchMsg != null && launchMsg.equals(IDataStoreConstants.ATTEMPT_RECONNECT))
					{
						internalConnect(monitor);
						return;
					}
				}
				else
				{
					launchFailed = true;
					SystemBasePlugin.logError("Error launching server: " + launchStatus.getMessage(), null);
				}
			}
			if (launchStatus.isConnected())
			{
				if (monitor != null)
				{
					SystemMessage cmsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECTING_TO_SERVER);
					cmsg.makeSubstitution(clientConnection.getPort());
					monitor.subTask(cmsg.getLevelOneText());
				}
				// connect to launched server
				connectStatus = clientConnection.connect(launchStatus.getTicket(), timeout);
				Throwable conE = connectStatus.getException();
				if (!connectStatus.isConnected() && 
						(connectStatus.getMessage().startsWith(ClientConnection.CANNOT_CONNECT) ||
						 conE instanceof SSLException 
						 )		
						)
				{
					if (conE instanceof SSLHandshakeException)
					{
						List certs = connectStatus.getUntrustedCertificates();
						if (certs != null && certs.size() > 0)
						{	
							ISystemKeystoreProvider provider = SystemKeystoreProviderManager.getInstance().getDefaultProvider();
							if (provider != null)
							{
								if (provider.importCertificates(certs, getHostName()))
								{
									connect(monitor);
									return;
								}
								else
								{
									throw new InterruptedException();
								}
							}
						}
						
					}
					launchStatus = launchServer(clientConnection, info, daemonPort, monitor);
					if (!launchStatus.isConnected())
					{
						launchFailed = true;
					}
					else
					{
						if (setSSLProperties(!usedSSL))
						{
							connectStatus = clientConnection.connect(launchStatus.getTicket(), timeout);
						}
					}
				}
				if (!connectStatus.isConnected() && connectStatus.isSLLProblem())
				{					
					importCertsAndReconnect(connectStatus, monitor);
					return;
				}

				/*
				if (connectStatus != null && connectStatus.getMessage().startsWith(ClientConnection.INCOMPATIBLE_UPDATE))
				{
					// offer to update it
					clientConnection.getDataStore().queryInstall();
				}
				*/
			}
			else
			{
				connectStatus = new ConnectionStatus(false);
				connectStatus.setMessage(
					RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_CONNECT_FAILED).getLevelOneText());
			}
		}
		else if (serverLauncherType == ServerLaunchType.RUNNING_LITERAL)
		{
			if (monitor != null)
			{
				SystemMessage cmsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECTING_TO_SERVER);
				cmsg.makeSubstitution(clientConnection.getPort());
				monitor.subTask(cmsg.getLevelOneText());
			}
			// connection directly
			boolean useSSL = isUsingSSL();
			setSSLProperties(useSSL);
			connectStatus = clientConnection.connect(null, timeout);
		}
		// server launcher type is unknown
		else
		{
			SystemSignonInformation info = getPasswordInformation();
			connectStatus = launchServer(clientConnection, info, serverLauncher, monitor);
			if (!connectStatus.isConnected() && !clientConnection.isKnownStatus(connectStatus.getMessage()))
			{
				if (setSSLProperties(true))
				{
					connectStatus = launchServer(clientConnection, info, serverLauncher, monitor);
					if (!connectStatus.isConnected() && connectStatus.isSLLProblem())
					{
						importCertsAndReconnect(connectStatus, monitor);
						return;
					}
				}
			}

		}

		// if connected
		if (connectStatus != null && connectStatus.isConnected())
		{
			IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
			if (clientConnection.getDataStore().usingSSL() && store.getBoolean(ISystemPreferencesConstants.ALERT_SSL))
			{
				msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_USING_SSL);
				msg.makeSubstitution(getHostName());
				DisplayHidableSystemMessageAction msgAction = new DisplayHidableSystemMessageAction(msg, store, ISystemPreferencesConstants.ALERT_SSL);
				Display.getDefault().syncExec(msgAction);
				if (msgAction.getReturnCode() != IDialogConstants.YES_ID)
				{
					internalDisconnect(monitor);
					throw new InterruptedException();
				}
			}
			else if (!clientConnection.getDataStore().usingSSL() && store.getBoolean(ISystemPreferencesConstants.ALERT_NONSSL))
			{
				msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_NOT_USING_SSL);
				msg.makeSubstitution(getHostName());
				DisplayHidableSystemMessageAction msgAction = new DisplayHidableSystemMessageAction(msg, store, ISystemPreferencesConstants.ALERT_NONSSL);
				Display.getDefault().syncExec(msgAction);
				if (msgAction.getReturnCode() != IDialogConstants.YES_ID)
				{
					internalDisconnect(monitor);
					throw new InterruptedException();
				}
			}
	
			DataStore dataStore = clientConnection.getDataStore();

			_connectionStatusListener = new ConnectionStatusListener(dataStore.getStatus(), this);
			dataStore.getDomainNotifier().addDomainListener(_connectionStatusListener);
			
		
			
			// DKM: dataStore needs a miners location
			//		for now, I'll use dstore.miners as default location
			//		(I've inserted the universal miner in it's minerFile.dat file)		

			// DY:  defect 46811 The minerFile.dat does not exist in this directory which causes a
			// java.io.FileNotFoundException to be printed to the console (not very 
			// encouraging for the end user.)  So I'm setting it to the current directory (.)
			// which should be where the code is run from
			//dataStore.addMinersLocation("org.eclipse.dstore.miners");
			
			
			StatusMonitor statusMonitor = StatusMonitorFactory.getInstance().getStatusMonitorFor(this, dataStore);
			
			if (launchStatus != null && launchStatus.isConnected())
			{
				//dataStore.showTicket(launchStatus.getTicket()); // send security token to server, this must be done first
				DataElement ticket = dataStore.createTicket(launchStatus.getTicket());
				dataStore.queryShowTicket(ticket);
				//statusMonitor.waitForUpdate(ticketStatus);					
			}
			else
			{
				dataStore.showTicket(null);
			}
			
	        if (dataStore.isDoSpirit()) dataStore.queryServerSpiritState();

			// Fire comm event to signal state changed
			fireCommunicationsEvent(CommunicationsEvent.AFTER_CONNECT);
			
			// is there a warning message?
			String message = connectStatus.getMessage();
			if (message != null)
			{
				if (message.startsWith(ClientConnection.CLIENT_OLDER))
				{
					
					msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_CLIENT_OLDER_WARNING);
					msg.makeSubstitution(getHostName());
				}
				else if (message.startsWith(ClientConnection.SERVER_OLDER))
				{
					msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_SERVER_OLDER_WARNING);
					msg.makeSubstitution(getHostName());
				}
				ShowConnectMessage msgAction = new ShowConnectMessage(msg);
				Display.getDefault().asyncExec(msgAction);				
			}
			
			// register the classloader for this plugin with the datastore
			dataStore.registerLocalClassLoader(getClass().getClassLoader());
	        
			int serverVersion = getServerVersion();
			if (serverVersion >= 8 || (serverVersion == 7 && getServerMinor() >= 1))
			{
				//	 register the preference for remote class caching with the datastore
				store.setDefault(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES, IUniversalDStoreConstants.DEFAULT_PREF_CACHE_REMOTE_CLASSES);
				boolean cacheRemoteClasses = store.getBoolean(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES);
				
				dataStore.setPreference(RemoteClassLoader.CACHING_PREFERENCE, cacheRemoteClasses ? "true" : "false");
			}
			else
			{						
				dataStore.addMinersLocation(".");
				// older servers initialized in one shot
				DataElement schemaStatus = dataStore.getSchema();
		 
		         // Initialzie the miners
		         if (monitor != null)
		         {
		            SystemMessage imsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_INITIALIZING_SERVER);
		            monitor.subTask(imsg.getLevelOneText());
		         }
		         DataElement initStatus = dataStore.initMiners();	         
		         statusMonitor.waitForUpdate(schemaStatus);
		         statusMonitor.waitForUpdate(initStatus);
			}

			long t2 = System.currentTimeMillis();
			
			//System.out.println("connect time = "+(t2 - t1));
		
			
		
		}
		else
		{
			// if daemon launch failed because of an SSL problem
		    if (launchFailed && launchStatus != null && launchStatus.isSLLProblem())
		    {
		    	if (launchStatus.isSLLProblem())
				{
					launchStatus.getException();
					
					List certs = launchStatus.getUntrustedCertificates();
					if (certs.size() > 0)
					{	
						ISystemKeystoreProvider provider = SystemKeystoreProviderManager.getInstance().getDefaultProvider();
						if (provider != null)
						{
							if (provider.importCertificates(certs, getHostName()))
							{
								internalConnect(monitor);
								return;
							}
						}				
					}
					else
					{
					
						msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_SSL_EXCEPTION);
						msg.makeSubstitution(launchStatus.getMessage());
					}
				} 	
		    }
		    
		    // if daemon launch failed (SSL or otherwise)
			if (launchFailed && launchStatus != null)
			{
				String launchMsg = launchStatus.getMessage();
				if (launchStatus.getException() != null)
				{
					Throwable exception = launchStatus.getException();
					msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_DAEMON_FAILED_EXCEPTION);
					msg.makeSubstitution(getHostName(), ""+serverLauncher.getDaemonPort(), exception);
				}
				else if (launchMsg != null && launchMsg.indexOf(IDataStoreConstants.AUTHENTICATION_FAILED) != -1)
				{
					if (launchFailed)
				    {
				        clearPasswordCache(true);
				    }
				
					// Display error message
					msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_AUTH_FAILED);
					msg.makeSubstitution(getHostName());
					DisplaySystemMessageAction msgAction = new DisplaySystemMessageAction(msg);
					Display.getDefault().syncExec(msgAction);

					// Re-prompt for password
					connectException = null;
					Display.getDefault().syncExec(new Runnable()
					{
						public void run()
						{
							try
							{
								promptForPassword(Display.getDefault().getActiveShell(), true);
							}
							catch (InterruptedException e)
							{
								connectException = e;
							}
						}
					});

					// Check if the user cancelled the prompt
					if (connectException instanceof InterruptedException)
					{
						throw connectException;
					}

					// Try to connect again.  This is a recursive call, but will only 
					// call if the user presses OK on the password prompt dialog, otherwise
					// it will continue and return
					internalConnect(monitor);

					// Since we got here we must be connected so skip error checking below
					return;
				}
				// If password has expired and must be changed
				else if (launchMsg != null && (isPasswordExpired(launchMsg) || isNewPasswordInvalid(launchMsg)))
				{
					NewPasswordInfo newPasswordInfo = null;
					while (launchMsg != null && (isPasswordExpired(launchMsg) || isNewPasswordInvalid(launchMsg)))
					{
						newPasswordInfo = promptForNewPassword(isPasswordExpired(launchMsg) ? RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PASSWORD_EXPIRED) : RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PASSWORD_INVALID));
						launchStatus = changePassword(clientConnection, getPasswordInformation(), serverLauncher, monitor, newPasswordInfo.newPassword);
						launchMsg = launchStatus.getMessage();
					}
					if (newPasswordInfo != null) 
					{
						setPassword(getPasswordInformation().getUserid(), newPasswordInfo.newPassword, newPasswordInfo.savePassword);
					}
					if (launchMsg != null && launchMsg.equals(IDataStoreConstants.ATTEMPT_RECONNECT))
					{
						internalConnect(monitor);
						return;
					}
				}
				else if (launchMsg != null)
				{					
					msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_DAEMON_FAILED);
					msg.makeSubstitution(getHostName(), clientConnection.getPort(), launchMsg);
				}
			}
			
			// if connection failed for known reason
			else if (connectStatus != null && !connectStatus.isConnected())
			{
				if (connectStatus.getMessage().startsWith(ClientConnection.INCOMPATIBLE_SERVER_UPDATE))
				{
					msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_INCOMPATIBLE_UPDATE);
					msg.makeSubstitution(getHostName());
				}
				else if (connectStatus.getMessage().startsWith(ClientConnection.INCOMPATIBLE_PROTOCOL))
				{
					msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_INCOMPATIBLE_PROTOCOL);
					msg.makeSubstitution(getHostName());
				}
				else
				{
					Throwable exception = connectStatus.getException();
					if (exception != null)
					{
						msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_FAILED);
						msg.makeSubstitution(getHostName(), exception);
					}
				}
			}
			
			// if connect failed for unknown reason
			else if (connectStatus == null)
			{
				SystemBasePlugin.logError("Failed to connect to remote system", null);
				msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_CONNECT_FAILED);
				msg.makeSubstitution(getHostName());
			}

			// if, for some reason, we don't have a message
			if (msg == null)
			{
				SystemBasePlugin.logError("Failed to connect to remote system" + connectStatus.getMessage(), null);
				msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_CONNECT_FAILED);
				msg.makeSubstitution(getHostName());
			}

			clientConnection.disconnect();
			clientConnection = null;
			
			// yantzi: artemis 6.0, check for invalid login (user ID / pwd) and reprompt for signon information
			if (msg.getFullMessageID().startsWith(ISystemMessages.MSG_COMM_INVALID_LOGIN))
			{
				if (launchFailed)
			    {
			        clearPasswordCache(true);
			    }
				
				DisplaySystemMessageAction msgAction = new DisplaySystemMessageAction(msg);
				Display.getDefault().syncExec(msgAction);

				// Re-prompt for password
				connectException = null;
				Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						try
						{
							promptForPassword(Display.getDefault().getActiveShell(), true);
						}
						catch (InterruptedException e)
						{
							connectException = e;
						}
					}
				});

				// Check if the user cancelled the prompt
				if (connectException instanceof InterruptedException)
				{
					throw connectException;
				}

				// Try to connect again.  This is a recursive call, but will only 
				// call if the user presses OK on the password prompt dialog, otherwise
				// it will continue and return
				internalConnect(monitor);
				
				// we are connected from recursive so continue
				return; 
			}

			throw new SystemMessageException(msg);
		}
	}
	
	protected boolean isPasswordExpired(String message)
	{
		return message.indexOf(IDataStoreConstants.PASSWORD_EXPIRED) != -1;
	}
	
	protected boolean isNewPasswordInvalid(String message)
	{
		return message.indexOf(IDataStoreConstants.NEW_PASSWORD_INVALID) != -1;
	}
	
	protected void importCertsAndReconnect(ConnectionStatus connectStatus, IProgressMonitor monitor) throws Exception
	{
		List certs = connectStatus.getUntrustedCertificates();
		if (certs != null && certs.size() > 0)
		{	
			ISystemKeystoreProvider provider = SystemKeystoreProviderManager.getInstance().getDefaultProvider();
			if (provider != null)
			{
				if (provider.importCertificates(certs, getHostName()))
				{
					internalConnect(monitor);
					return;
				}
				else
				{
					throw new InterruptedException();
				}
			}
		}
	}
	
	protected int launchUsingRexec(IProgressMonitor monitor, SystemSignonInformation info, IServerLauncherProperties serverLauncherProperties) throws Exception
	{
		IServerLauncher starter = getRemoteServerLauncher();
		starter.setSignonInformation(info);
		starter.setServerLauncherProperties(serverLauncherProperties);

		String serverPort = (String)starter.launch(monitor);	
		if (monitor.isCanceled())
		{
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_OPERATION_CANCELLED);
			throw new SystemMessageException(msg);
		}
		
		int iServerPort = 0;
		if (serverPort != null)
		{
			iServerPort = Integer.parseInt(serverPort);		
		}
		return iServerPort;
	}
	
	protected boolean setSSLProperties(boolean enable)
	{
		ISystemKeystoreProvider provider = SystemKeystoreProviderManager.getInstance().getDefaultProvider();
		if (provider != null)
		{
			String keyStore = provider.getKeyStorePath();
			String password = provider.getKeyStorePassword();
			
			ISSLProperties properties = new ClientSSLProperties(enable, keyStore, password);
			clientConnection.setSSLProperties(properties);
			return true;
		}
		else return false;
	}

	
	
	protected boolean promptForTrusting(Shell shell, X509Certificate cert)
	{
		return true;
	}
	
	
	/* 
	 * Launch a DataStore server using a daemon.   This method can be overridden if a custom implementation is required.
	 * The default implementation uses the daemon client that is built into ClientConnection.
	 */
	 protected ConnectionStatus launchServer(ClientConnection clientConnection, SystemSignonInformation info, int daemonPort, IProgressMonitor monitor)
	 {
	     return launchServer(clientConnection, info, daemonPort, monitor, 0);
	 }
	 
	 
	/* 
	 * Launch a DataStore server using a daemon.   This method can be overridden if a custom implementation is required.
	 * The default implementation uses the daemon client that is built into ClientConnection.
	 */
	 protected ConnectionStatus launchServer(ClientConnection clientConnection, SystemSignonInformation info, int daemonPort, IProgressMonitor monitor, int timeout)
	 {
	     return clientConnection.launchServer(info.getUserid(), info.getPassword(), daemonPort, timeout);
	 }

	 /*
	  * Launch a DataStore server using a specified server launcher.  By default, this method does nothing since UniversalSystem does
	  * not know how to handle this particular launch type.   This method should be overridden to provide a custom implementation
	  * of the launch.
	  */
	 protected ConnectionStatus launchServer(ClientConnection clientConnection, SystemSignonInformation info, IServerLauncherProperties launcher, IProgressMonitor monitor)
	 {
		 return null;
	 }
	 
	 /**
	  * Change the password on a remote system and optionally remain connected to it. Subclasses must implement this
	  * method if they wish to 
	  * @param clientConnection The connection on which the password must be changed
	  * @param info The old SystemSignonInformation, including the old password.
	  * @param serverLauncherProperties The properties of the server launcher used to connect to the server. Use this object to get the type of serverlauncher, if your implementation varies depending on the type.
	  * @param monitor a progress monitor
	  * @param newPassword the new password to which the old one will be changed.
	  * @return the status of the password change and optionally the connection. If the new password is rejected by the remote
	  * system, return new ConnectionStatus(false, IDataStoreConstants.NEW_PASSWORD_INVALID).
	  * If the system is now connected, and the server is ready to be connected, construct a new ConnectionStatus(true) and if using the RSE daemon, set the ticket on it
	  * to the ticket number of the server. If you wish to just have the UniversalSystem attempt a reconnect from the beginning after changing the password,
	  * return new ConnectionStatus(true, IDataStoreConstants.ATTEMPT_RECONNECT).
	  */
	 protected ConnectionStatus changePassword(ClientConnection clientConnection, SystemSignonInformation info, IServerLauncherProperties serverLauncherProperties, IProgressMonitor monitor, String newPassword)
	 {
		 return new ConnectionStatus(false, IDataStoreConstants.AUTHENTICATION_FAILED);
	 }
	 
	/**
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isConnected()
	 */
	public boolean isConnected()
	{
		if (clientConnection != null)
		{
			return clientConnection.isConnected();
		}

		return false;
	}

	/**
	 * Shortcut to checking if the network is down
	 */
	public boolean isNetworkError()
	{
		if (_connectionStatusListener != null)
		{
			return _connectionStatusListener.isConnectionDown();
		}

		return false;
	}

	/**
	 * Show any warning messages returned by host api calls.
	 * @param shell Parent UI
	 * @param warnings Vector of String or toString()'able messages.
	 */
	public void showWarningMsgs(Shell shell, Vector warnings)
	{
		for (int idx = 0; idx < warnings.size(); idx++)
		{
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_GENERIC_W);
			msg.makeSubstitution((warnings.elementAt(idx)).toString());
			SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
			msgDlg.open();
		}
	}

	/**
	 * @return The DataStore currently being used by this connection.
	 */
	public DataStore getDataStore()
	{
		if (clientConnection != null)
		{
			return clientConnection.getDataStore();
		}
		else
		{
			return null;
		}
	}

	/**
	* @see org.eclipse.rse.core.subsystems.AbstractConnectorService#getPasswordInformation()
	*/
	public SystemSignonInformation getPasswordInformation()
	{
		// For Windows we want to avoid the signon prompt (because we never
		// really authenticate with the remote system and this would be decieving 
		// for the end user

		if (getPrimarySubSystem().getHost().getSystemType().equals(IRSESystemType.SYSTEMTYPE_WINDOWS))
		{
			String userid = getPrimarySubSystem().getUserId();
			if (userid == null)
			{
				userid = "remoteuser";
			}
			SystemSignonInformation info = new SystemSignonInformation(getPrimarySubSystem().getHost().getHostName(),
																	   userid, "", IRSESystemType.SYSTEMTYPE_WINDOWS);
			return info;
		}
		else
		{
			return super.getPasswordInformation();
		}
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#isPasswordCached()
	 */
//	public boolean isPasswordCached() // DWD is this method needed?
//	{
//		// For Windows we never prompt for userid / password so we don't need 
//		// to clear the password cache
//		if (getPrimarySubSystem().getHost().getSystemType().equals(IRSESystemType.SYSTEMTYPE_WINDOWS))
//		{
//			return false;
//		}
//		else
//		{
//			return super.isPasswordCached();
//		}
//	}





	public boolean hasRemoteServerLauncherProperties() 
	{
		return getRemoteServerLauncherProperties() != null;
	}



	public boolean supportsRemoteServerLaunching() 
	{
		return true;
	}



	public boolean supportsServerLaunchProperties()
	{
		return true;
	}

}