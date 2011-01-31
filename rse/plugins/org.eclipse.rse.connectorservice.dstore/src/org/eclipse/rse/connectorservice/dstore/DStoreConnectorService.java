/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [186128][refactoring] Move IProgressMonitor last in public base classes
 * David McKnight   (IBM)        - [202822] need to enable spiriting on the server side
 * David McKnight   (IBM)        - [199565] taking out synchronize for internalConnect
 * David McKnight   (IBM)        - [205986] attempt SSL before non-SSL for daemon connect
 * David McKnight   (IBM)        - [186363] get rid of obsolete calls to SubSystem.connect()
 * David McKnight   (IBM)        - [196624] dstore miner IDs should be String constants rather than dynamic lookup
 * David McKnight   (IBM)        - [216596] dstore preferences (timeout, and others)
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [218685] [api][breaking][dstore] Unable to connect when using SSL.
 * David McKnight  (IBM)         - [220123][dstore] Configurable timeout on irresponsiveness
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David McKnight   (IBM)        - [220123] [api][dstore] Configurable timeout on irresponsiveness
 * David McKnight   (IBM)        - [223204] [cleanup] fix broken nls strings in files.ui and others
 * David Dykstal (IBM) - [225089][ssh][shells][api] Canceling connection leads to exception
 * David McKnight   (IBM)        - [227406] [dstore] DStoreFileService must listen to buffer size preference changes
 * David McKnight   (IBM)        - [228334][api][breaking][dstore] Default DataStore connection timeout is too short
 * David McKnight   (IBM)        - [235756] [dstore] Unable to connect to host with SSL via REXEC
 * David McKnight   (IBM)        - [244116] [dstore][daemon][ssl]  Connecting to RSE server doesn't complete when the connection is SSL
 * David McKnight   (IBM)        - [233160] [dstore] SSL/non-SSL alert are not appropriate
 * David Dykstal (IBM) [235284] Cancel password change causes problem
 * David McKnight   (IBM)        - [267236] [dstore] Can't connect after a wrong password
 * David McKnight   (IBM)        - [274688] [api][dstore] DStoreConnectorService.internalConnect() needs to be cleaned up
 * David McKnight   (IBM)        - [258529] Unable to display connection failure error message
 * David McKnight   (IBM)        - [306989] [dstore] workspace in strange condition if expanding projects during  logon
 * David McKnight   (IBM)        - [313653] [dstore] Not Secured using SSL message appears twice per connect
 * David McKnight   (IBM) 		 - [284950] [dstore] Error binding socket on relaunch
 *******************************************************************************/

package org.eclipse.rse.connectorservice.dstore;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.dstore.core.client.ClientConnection;
import org.eclipse.dstore.core.client.ConnectionStatus;
import org.eclipse.dstore.core.java.IRemoteClassInstance;
import org.eclipse.dstore.core.java.RemoteClassLoader;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.IDataStoreConstants;
import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.dstore.core.model.ISSLProperties;
import org.eclipse.dstore.internal.core.client.ClientSSLProperties;
import org.eclipse.dstore.internal.core.util.XMLparser;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.connectorservice.dstore.util.ConnectionStatusListener;
import org.eclipse.rse.connectorservice.dstore.util.StatusMonitor;
import org.eclipse.rse.connectorservice.dstore.util.StatusMonitorFactory;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.comm.ISystemKeystoreProvider;
import org.eclipse.rse.core.comm.SystemKeystoreProviderManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.IRemoteServerLauncher;
import org.eclipse.rse.core.subsystems.IServerLauncher;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ServerLaunchType;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.internal.connectorservice.dstore.Activator;
import org.eclipse.rse.internal.connectorservice.dstore.ConnectorServiceResources;
import org.eclipse.rse.internal.connectorservice.dstore.IConnectorServiceMessageIds;
import org.eclipse.rse.internal.connectorservice.dstore.RexecDstoreServer;
import org.eclipse.rse.internal.ui.SystemPropertyResources;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.DisplayHidableSystemMessageAction;
import org.eclipse.rse.ui.actions.DisplaySystemMessageAction;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.subsystems.StandardConnectorService;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * System class required by the remote systems framework.
 * This represents the live connection at tool runtime.
 * <p>
 * The universal subsystems are based on datastore technology so we use that
 * to do the connection.
 */
public class DStoreConnectorService extends StandardConnectorService implements IDataStoreProvider
{
	private class ConnectionStatusPair {
		private ConnectionStatus _connectStatus;
		private ConnectionStatus _launchStatus;
		private Boolean _alertedNonSSL;
		
		public ConnectionStatusPair(ConnectionStatus connectStatus, ConnectionStatus launchStatus, Boolean alertedNonSSL){
			_connectStatus = connectStatus;
			_launchStatus = launchStatus;
			_alertedNonSSL = alertedNonSSL;
		}
		
		public ConnectionStatus getConnectStatus(){
			return _connectStatus;
		}
		
		public ConnectionStatus getLaunchStatus(){
			return _launchStatus;
		}
		
		public Boolean getAlertedNonSSL(){
			return _alertedNonSSL;
		}
	}
	
	private class StartSpiritThread extends Thread
	{
		private DataStore _dataStore;
		public StartSpiritThread(DataStore dataStore)
		{
			_dataStore = dataStore;
		}

		public void run()
		{
			if (_dataStore.isDoSpirit()) _dataStore.queryServerSpiritState();
		}
	}

	private ClientConnection clientConnection = null;
	private ConnectionStatusListener _connectionStatusListener = null;
	private IServerLauncher starter = null;
	private IServerLauncherProperties _remoteServerLauncherProperties = null;
	private boolean _isConnecting = false;

	// Shortcut to sysInfo to save time
	private transient DataElement sysInfo = null;
	private transient DataElement installInfo = null;
	private transient DataElement clientIP = null;
	private static String DSTORE_PACKAGE = "org.eclipse.dstore.core"; //$NON-NLS-1$

	private Exception connectException;
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
				ISubSystem ss = getPrimarySubSystem();
				if (ss != null)
				{
					if (Display.getCurrent() == null) {
						ss.connect(new NullProgressMonitor(), false);
					} else {
						ss.connect(false, null);
					}
				}
			}
			catch (Exception e)
			{
				SystemBasePlugin.logError("UniversalSystem.getSystemInfoProperty: error during connect", e); //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			}
		}

		String propertyValue = null;
		DataElement envMinerData = null;
		DataStore ds = getDataStore();

		// Check if we have sysInfo cached already
		if (sysInfo == null)
		{
			envMinerData = ds.findMinerInformation(IUniversalDataStoreConstants.UNIVERSAL_ENVIRONMENT_MINER_ID);
			if (envMinerData != null)
			{
				sysInfo = ds.find(envMinerData, DE.A_NAME, "systemInfo", 1); //$NON-NLS-1$
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
				propertyValue = ""; //$NON-NLS-1$
		}
		else
		{
			// at connect time, the info node is not guaranteed to be present, so this gets logged a lot
			// it doesn't cause any functional problems, so I'm commenting this out
			//SystemBasePlugin.logError("UniversalSystem.getSystemInfoNode:  sysInfo node not found", null); //$NON-NLS-1$
			//SystemBasePlugin.logError("UniversalSystem.getSystemInfoNode:  miner data = " + envMinerData, null); //$NON-NLS-1$
			propertyValue = ""; //$NON-NLS-1$
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

		StringBuffer buffer = new StringBuffer(getSystemInfoProperty("os.name")); //$NON-NLS-1$
		buffer.append(" "); //$NON-NLS-1$
		buffer.append(getSystemInfoProperty("os.version")); //$NON-NLS-1$

		return buffer.toString();
	}

	/**
	 * Return the home directory of the remote system for the current user, if available.
	 */
	public String getHomeDirectory()
	{
		return getSystemInfoProperty("user.home"); //$NON-NLS-1$
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
	    return ""; //$NON-NLS-1$
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
		return getSystemInfoProperty("temp.dir"); //$NON-NLS-1$
	}

	protected int getSocketTimeOutValue()
	{
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		return store.getInt(IUniversalDStoreConstants.RESID_PREF_SOCKET_TIMEOUT);
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#disconnect(IProgressMonitor)
	 */
	protected void internalDisconnect(IProgressMonitor monitor) throws Exception
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
				if (clientConnection != null)
				{
					clientConnection.disconnect();
				}

//				 Fire comm event to signal state changed
				notifyDisconnection();

				clientConnection = null;
				// DKM - no need to clear uid cache
				clearPassword(false, true); // clear in-memory password
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
		URL pluginsURL = bundle.getEntry("/"); //$NON-NLS-1$

		try
		{
			String path = FileLocator.resolve(pluginsURL).getPath();
			File systemsPluginDir = new File(path);
			path = systemsPluginDir.getParentFile().getAbsolutePath();
			String version = (String)(bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION));
			Version v = new Version(version);
			String versionString = v.toString();
			String dstorePath = getDStorePath(path, versionString);
			System.setProperty("A_PLUGIN_PATH", dstorePath); //$NON-NLS-1$
		}
		catch (IOException e)
		{
		}
	}

	private String getDStorePath(String pluginDir, String version)
	{
			File dstorePath = new File(pluginDir + "/" + DSTORE_PACKAGE + "_" + version); //$NON-NLS-1$  //$NON-NLS-2$
			if (!dstorePath.exists())
			{
				// might be in workspace
				dstorePath = new File(pluginDir + "/" + DSTORE_PACKAGE); //$NON-NLS-1$
			}

		return dstorePath.getAbsolutePath();
	}

	/**
	 * Return if you support remotely launching a server script
	 * @deprecated Use instead {@link #isServerLaunchTypeEnabled(ISubSystem, ServerLaunchType)}
	 *  or {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchType(ServerLaunchType)}
	 */
	public boolean getRexecLaunchEnabled(SubSystem subsystemImpl) {
		return isServerLaunchTypeEnabled(subsystemImpl, ServerLaunchType.REXEC_LITERAL);
	}


	/**
	 * Return if you support connecting to a server already running
	 * @deprecated Use instead {@link #isServerLaunchTypeEnabled(ISubSystem, ServerLaunchType)}
	 *  or {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchType(ServerLaunchType)}
	 */
	public boolean getNoLaunchEnabled(SubSystem subsystemImpl) {
		return isServerLaunchTypeEnabled(subsystemImpl, ServerLaunchType.RUNNING_LITERAL);
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

	public IServerLauncherProperties getRemoteServerLauncherProperties() {
		return _remoteServerLauncherProperties;
	}

	public void setRemoteServerLauncherProperties(IServerLauncherProperties newRemoteServerLauncher) {
		if (_remoteServerLauncherProperties != newRemoteServerLauncher)
		{
			_remoteServerLauncherProperties = newRemoteServerLauncher;
			setDirty(true);
		}
	}
	
	/**
	 *  Connect to the server by using REXEC as a daemon to launch it.
	 *  
	 * @param info the signon information
	 * @param serverLauncher the server launcher
	 * @param monitor the progress monitor
	 * 
	 * @return the connection status
	 *  
	 * @since 3.1
	 */
	protected ConnectionStatus connectWithREXEC(SystemSignonInformation info, IRemoteServerLauncher serverLauncher, IProgressMonitor monitor) throws Exception {
		if (monitor != null) {
			String cmsg = ConnectorServiceResources.MSG_STARTING_SERVER_VIA_REXEC;
			monitor.subTask(cmsg);
		}

		ConnectionStatus connectStatus = null;
		boolean autoDetectSSL = true;
		if (serverLauncher != null){
		    autoDetectSSL = serverLauncher.getAutoDetectSSL();
		}

		// GC: - if failed to get a connection in another way, try
		// starting the datastore server with rexec
		IServerLauncher starter = getRemoteServerLauncher();
		starter.setSignonInformation(info);
		starter.setServerLauncherProperties(serverLauncher);
		
		// get Socket Timeout Value Preference
		int timeout = getSocketTimeOutValue();
		
		if (starter instanceof RexecDstoreServer){
			((RexecDstoreServer)starter).setSocketTimeoutValue(timeout);
		}		
		
		if (autoDetectSSL) timeout = 3000;
		else setSSLProperties(isUsingSSL());

		int iServerPort = launchUsingRexec(info, serverLauncher, monitor);

		if(iServerPort != 0)
		{
			clientConnection.setPort("" + iServerPort); //$NON-NLS-1$

			if (monitor != null) {
				String cmsg = NLS.bind(ConnectorServiceResources.MSG_CONNECTING_TO_SERVER, clientConnection.getPort());
				monitor.subTask(cmsg);
			}

			// connect to launched server
			connectStatus = clientConnection.connect(null, timeout);
			if (!connectStatus.isConnected() && connectStatus.getMessage().startsWith(ClientConnection.CANNOT_CONNECT) && autoDetectSSL){
				if (setSSLProperties(true)){
					iServerPort = launchUsingRexec(info, serverLauncher, monitor);
					if (iServerPort != 0)
					{
						clientConnection.setPort("" + iServerPort); //$NON-NLS-1$
						connectStatus = clientConnection.connect(null, timeout);
					}
				}
			}
		}
		else {
			connectStatus = new ConnectionStatus(false);
			SystemMessage msg = starter.getErrorMessage();
			String errorMsg = null;
			if (msg == null)
			{
				errorMsg = NLS.bind(ConnectorServiceResources.MSG_COMM_CONNECT_FAILED, getHostName());
			}
			else
			{
				errorMsg = msg.getLevelTwoText();
			}
			connectStatus.setMessage(errorMsg);
		}
		return connectStatus;
	}
	
	/**
	 * Connection to a server via the RSE daemon.
	 * 
	 * @param info the signon information
	 * @param serverLauncher the server launcher
	 * @param alertedNONSSL indication of whether an alert for NON-ssl has already been issued
	 * @param monitor the progress monitor
	 * 
	 * @return a pair of connection statuses - the launch status for the daemon and the connect status for the server
	 * @since 3.1
	 */
	protected ConnectionStatusPair connectWithDaemon(SystemSignonInformation info, IRemoteServerLauncher serverLauncher, Boolean alertedNONSSL, IProgressMonitor monitor) throws InterruptedException {
		if (monitor != null) {
			String cmsg = ConnectorServiceResources.MSG_STARTING_SERVER_VIA_DAEMON;
			monitor.subTask(cmsg);
		}

		ConnectionStatus connectStatus = null;
		
		// DY:  getLocalUserId() may return null for Windows connections because
		// we no longer prompt for userid / pwd.  But for other connections the userid
		// should be the same as the one stored in the password info (and for Windows
		// this will be the temp remoteuser userid.
		//launchStatus = clientConnection.launchServer(getLocalUserId(), getPassword(getPasswordInformation()));

		
		int daemonPort = 0;
		if (serverLauncher != null)
			daemonPort = serverLauncher.getDaemonPort();

		// 205986  FIRST TRY SSL, THEN NON-SECURE!
		boolean usedSSL = true;
		setSSLProperties(true);
		
		// get Socket Timeout Value Preference
		int timeout = getSocketTimeOutValue();

		ConnectionStatus launchStatus = launchServer(clientConnection, info, daemonPort, monitor, timeout);
		if (!launchStatus.isConnected() && !clientConnection.isKnownStatus(launchStatus.getMessage()))
		{
			Throwable conE = launchStatus.getException();
			if (conE instanceof SSLHandshakeException)
			{
				List certs = launchStatus.getUntrustedCertificates();
				if (certs != null && certs.size() > 0)
				{
					ISystemKeystoreProvider provider = SystemKeystoreProviderManager.getInstance().getDefaultProvider();
					if (provider != null){
						if (provider.importCertificates(certs, getHostName())){
							return connectWithDaemon(info, serverLauncher, alertedNONSSL, monitor);
						}
						else{
							_isConnecting = false;
							throw new InterruptedException();
						}
					}
				}

			}

			if (setSSLProperties(false))
			{
				usedSSL = false;

				boolean allowNonSSL = true;
				// warning before launch without SSL
				IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
				if (store.getBoolean(ISystemPreferencesConstants.ALERT_NONSSL))
				{
					String cmsg = NLS.bind(ConnectorServiceResources.MSG_COMM_NOT_USING_SSL, getHostName());
					SystemMessage msg = createSystemMessage(IConnectorServiceMessageIds.MSG_COMM_NOT_USING_SSL, IStatus.INFO, cmsg);

					DisplayHidableSystemMessageAction msgAction = new DisplayHidableSystemMessageAction(msg, store, ISystemPreferencesConstants.ALERT_NONSSL);
					Display.getDefault().syncExec(msgAction);
					if (msgAction.getReturnCode() != IDialogConstants.YES_ID){
						allowNonSSL = false;
					} else {
						alertedNONSSL = new Boolean(true); // changing value to true
					}						
				}
				if (allowNonSSL){
					launchStatus = launchServer(clientConnection, info, daemonPort, monitor, timeout);
				}
				else {
					_isConnecting = false;
					clientConnection = null;
					
					throw new OperationCanceledException();
				}
			}
		}

		if (!launchStatus.isConnected()) { // launch failed
			String launchMsg = launchStatus.getMessage();
			// If password has expired and must be changed
			if (launchMsg != null && (isPasswordExpired(launchMsg) || isNewPasswordInvalid(launchMsg)))
			{
				SystemSignonInformation oldCredentials = (SystemSignonInformation) getCredentialsProvider().getCredentials();
				SystemSignonInformation newCredentials = null;
				while (launchMsg != null && (isPasswordExpired(launchMsg) || isNewPasswordInvalid(launchMsg)))
				{
					String pmsg = null;
					String pmsgDetails = null;
					String msgId = null;
					boolean expired = isPasswordExpired(launchMsg);
					if (expired){
						pmsg = ConnectorServiceResources.MSG_VALIDATE_PASSWORD_EXPIRED;
						pmsgDetails = ConnectorServiceResources.MSG_VALIDATE_PASSWORD_EXPIRED_DETAILS;
						msgId = IConnectorServiceMessageIds.MSG_VALIDATE_PASSWORD_EXPIRED;
					}
					else {
						pmsg = ConnectorServiceResources.MSG_VALIDATE_PASSWORD_INVALID;
						pmsgDetails = ConnectorServiceResources.MSG_VALIDATE_PASSWORD_INVALID_DETAILS;
						msgId = IConnectorServiceMessageIds.MSG_VALIDATE_PASSWORD_INVALID;
					}

					SystemMessage message = createSystemMessage(msgId,IStatus.ERROR, pmsg, pmsgDetails);
					try {
						getCredentialsProvider().repairCredentials(message);
					} catch (OperationCanceledException e) {
						_isConnecting = false;
						clientConnection = null;
						throw e;
					}
					newCredentials = (SystemSignonInformation) getCredentialsProvider().getCredentials();
					launchStatus = changePassword(clientConnection, oldCredentials, serverLauncher, monitor, newCredentials.getPassword());
					launchMsg = launchStatus.getMessage();
				}
				if (newCredentials != null){
					info = newCredentials;
				}
				if (launchMsg != null && launchMsg.equals(IDataStoreConstants.ATTEMPT_RECONNECT)){
					return connectWithDaemon(info, serverLauncher, alertedNONSSL, monitor);
				}
			}
			else if (launchMsg != null && isPortOutOfRange(launchMsg))
			{
				_isConnecting = false;

				int colonIndex = launchMsg.indexOf(':');
				String portRange = launchMsg.substring(colonIndex + 1);

				String pmsg =NLS.bind(ConnectorServiceResources.MSG_PORT_OUT_RANGE, portRange);
				SystemMessage message = createSystemMessage(IConnectorServiceMessageIds.MSG_PORT_OUT_RANGE, IStatus.ERROR, pmsg);

				// message handled here
				ShowConnectMessage msgAction = new ShowConnectMessage(message);
				Display.getDefault().asyncExec(msgAction);
				return null; // null here indicates no further processing required by internalConnect
			}
			else
			{
				SystemBasePlugin.logError("Error launching server: " + launchStatus.getMessage(), null); //$NON-NLS-1$
			}
		}
		else { // launch succeeded
			if (monitor != null)
			{
				if (clientConnection == null){
					SystemBasePlugin.logError("client connection is null!"); //$NON-NLS-1$
				}
				String pmsg = NLS.bind(ConnectorServiceResources.MSG_CONNECTING_TO_SERVER, clientConnection.getPort());
				monitor.subTask(pmsg);
			}
			// connect to launched server
			connectStatus = clientConnection.connect(launchStatus.getTicket(), timeout);
			Throwable conE = connectStatus.getException();

			if (!connectStatus.isConnected() &&
					(connectStatus.getMessage().startsWith(ClientConnection.CANNOT_CONNECT) || conE instanceof SSLException)) { // failed to connect to the server that was launched 
				if (conE instanceof SSLHandshakeException){ // cause of failure was an SSL handshake exception
					List certs = connectStatus.getUntrustedCertificates();
					if (certs != null && certs.size() > 0) {
						ISystemKeystoreProvider provider = SystemKeystoreProviderManager.getInstance().getDefaultProvider();
						if (provider != null){							
							if (provider.importCertificates(certs, getHostName())) { // import the certificates and try again								
								return connectWithDaemon(info, serverLauncher, alertedNONSSL, monitor);
							}
							else {
								_isConnecting = false;
								throw new InterruptedException();
							}
						}
					}
				}
				if (usedSSL && connectStatus.isSLLProblem()){
					clientConnection.setPort(Integer.toString(getPort()));
					// relaunching the server via the daemon so that we can connect again to the launched server with toggled useSSL settings
					launchStatus = launchServer(clientConnection, info, daemonPort, monitor);
					if (launchStatus.isConnected()) {
						if (setSSLProperties(!usedSSL)){
							connectStatus = clientConnection.connect(launchStatus.getTicket(), timeout);
						}					
					}
				}
			}
			
			// failure to connect diagnosis - not sure why this is here since I would expect this case was already handled
			// leaving it here just in case - will review later
			if (!connectStatus.isConnected() && connectStatus.isSLLProblem())
			{
				List certs = connectStatus.getUntrustedCertificates();
				if (certs != null && certs.size() > 0) {
					ISystemKeystoreProvider provider = SystemKeystoreProviderManager.getInstance().getDefaultProvider();
					if (provider != null) {
						if (provider.importCertificates(certs, getHostName())){
							return connectWithDaemon(info, serverLauncher, alertedNONSSL, monitor);
						}
						else {
							_isConnecting = false;
							throw new InterruptedException();
						}
					}
				}
			}
		} 

		return new ConnectionStatusPair(connectStatus, launchStatus, alertedNONSSL);
	}
	
	/**
	 *  Connect to a running server.
	 *  
	 * @param monitor the progress monitor
	 * 
	 * @return the connection status
	 * @since 3.1
	 */
	protected ConnectionStatus connectWithRunning(IProgressMonitor monitor){
		if (monitor != null)
		{
			String cmsg = NLS.bind(ConnectorServiceResources.MSG_CONNECTING_TO_SERVER, clientConnection.getPort());
			monitor.subTask(cmsg);
		}
		// connect directly
		boolean useSSL = isUsingSSL();
		setSSLProperties(useSSL);
		
		// get Socket Timeout Value Preference
		int timeout = getSocketTimeOutValue();
		return clientConnection.connect(null, timeout);
	}
	
	/**
	 * Connect via an overridden launchServer method
	 * 
	 * @param clientConnection the clientConnection
	 * @param info the signon info
	 * @param serverLauncher the server launcher
	 * @param monitor the progress monitor
	 * 
	 * @return the connection status
	 * 
	 * @since 3.1
	 */
	protected ConnectionStatus connectWithOther(ClientConnection clientConnection, SystemSignonInformation info, IServerLauncherProperties serverLauncher, IProgressMonitor monitor) throws Exception {
		ConnectionStatus connectStatus = launchServer(clientConnection, info, serverLauncher, monitor);
		if (!connectStatus.isConnected() && !clientConnection.isKnownStatus(connectStatus.getMessage())){
			if (connectStatus.isSLLProblem()){
				if (setSSLProperties(true)){
					connectStatus = launchServer(clientConnection, info, serverLauncher, monitor);
					if (!connectStatus.isConnected() && connectStatus.isSLLProblem()){						
						List certs = connectStatus.getUntrustedCertificates();
						if (certs != null && certs.size() > 0){ // we have untrusted certificates - so import them
							ISystemKeystoreProvider provider = SystemKeystoreProviderManager.getInstance().getDefaultProvider();
							if (provider != null){
								if (provider.importCertificates(certs, getHostName())){
									return connectWithOther(clientConnection, info, serverLauncher, monitor);
								}
								else
								{
									_isConnecting = false;
									throw new InterruptedException();
								}
							}
						}
						else { // SSL connect problem
							return null;
						}
					}
				}
			}
		}
		return connectStatus;
	}
	/**
	 * Initialize the DataStore connection.
	 * 
	 * @param launchStatus the launch status if the server was launched via the daemon.  Otherwise, null.
	 * @param connectStatus the connect status for the server
	 * @param alertedNONSSL a boolean indicating whether the user has been alerted to a NON-ssl connection
	 * @param monitor the status monitor
	 * 
	 * @since 3.1
	 */
	protected void initializeConnection(ConnectionStatus launchStatus, ConnectionStatus connectStatus, Boolean alertedNONSSL, IProgressMonitor monitor) throws Exception {
		SystemMessage msg = null;
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		if (clientConnection.getDataStore().usingSSL() && store.getBoolean(ISystemPreferencesConstants.ALERT_SSL))
		{
			String cmsg = NLS.bind(ConnectorServiceResources.MSG_COMM_USING_SSL, getHostName());
			msg = createSystemMessage(IConnectorServiceMessageIds.MSG_COMM_USING_SSL, IStatus.INFO, cmsg);

			DisplayHidableSystemMessageAction msgAction = new DisplayHidableSystemMessageAction(msg, store, ISystemPreferencesConstants.ALERT_SSL);
			Display.getDefault().syncExec(msgAction);
			if (msgAction.getReturnCode() != IDialogConstants.YES_ID)
			{			
				internalDisconnect(monitor);
				_isConnecting = false;
				throw new InterruptedException();
			}
		}
		else if (!clientConnection.getDataStore().usingSSL() && store.getBoolean(ISystemPreferencesConstants.ALERT_NONSSL))
		{
			if (!alertedNONSSL.booleanValue()){ // only alert if we haven't already
				String cmsg = NLS.bind(ConnectorServiceResources.MSG_COMM_NOT_USING_SSL, getHostName());
				msg = createSystemMessage(IConnectorServiceMessageIds.MSG_COMM_NOT_USING_SSL, IStatus.INFO, cmsg);
				
				DisplayHidableSystemMessageAction msgAction = new DisplayHidableSystemMessageAction(msg, store, ISystemPreferencesConstants.ALERT_NONSSL);
				Display.getDefault().syncExec(msgAction);
				if (msgAction.getReturnCode() != IDialogConstants.YES_ID)
				{
					internalDisconnect(monitor);
					_isConnecting = false;
					throw new InterruptedException();
				}
			}
		}

		DataStore dataStore = clientConnection.getDataStore();

		_connectionStatusListener = new ConnectionStatusListener(dataStore.getStatus(), this);
		dataStore.getDomainNotifier().addDomainListener(_connectionStatusListener);

		StatusMonitor statusMonitor = StatusMonitorFactory.getInstance().getStatusMonitorFor(this, dataStore);

		if (launchStatus != null && launchStatus.isConnected())
		{
			DataElement ticket = dataStore.createTicket(launchStatus.getTicket());
			dataStore.queryShowTicket(ticket);
		}
		else
		{
			dataStore.showTicket(null);
		}

		StartSpiritThread thread = new StartSpiritThread(dataStore);
		thread.start();

		// Fire comm event to signal state changed
		fireCommunicationsEvent(CommunicationsEvent.AFTER_CONNECT);

		// is there a warning message?
		String message = connectStatus.getMessage();
		if (message != null)
		{
			if (message.startsWith(ClientConnection.CLIENT_OLDER))
			{
				String cmsg = NLS.bind(ConnectorServiceResources.MSG_COMM_CLIENT_OLDER_WARNING, getHostName());
				String cmsgDetail = ConnectorServiceResources.MSG_COMM_CLIENT_OLDER_WARNING_DETAILS;

				msg = createSystemMessage(IConnectorServiceMessageIds.MSG_COMM_CLIENT_OLDER_WARNING, IStatus.WARNING, cmsg, cmsgDetail);

			}
			else if (message.startsWith(ClientConnection.SERVER_OLDER))
			{
				String cmsg = NLS.bind(ConnectorServiceResources.MSG_COMM_SERVER_OLDER_WARNING, getHostName());
				String cmsgDetail = ConnectorServiceResources.MSG_COMM_SERVER_OLDER_WARNING_DETAILS;

				msg = createSystemMessage(IConnectorServiceMessageIds.MSG_COMM_SERVER_OLDER_WARNING, IStatus.WARNING, cmsg, cmsgDetail);
			}

			if (store.getBoolean(IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER)){
				DisplayHidableSystemMessageAction msgAction = new DisplayHidableSystemMessageAction(msg, store, IUniversalDStoreConstants.ALERT_MISMATCHED_SERVER, false);
				Display.getDefault().syncExec(msgAction);
			}
		}

		// register the classloader for this plugin with the datastore
		dataStore.registerLocalClassLoader(getClass().getClassLoader());

		int serverVersion = getServerVersion();
		if (serverVersion >= 8 || (serverVersion == 7 && getServerMinor() >= 1))
		{
			//	 register the preference for remote class caching with the datastore
			boolean cacheRemoteClasses = store.getBoolean(IUniversalDStoreConstants.RESID_PREF_CACHE_REMOTE_CLASSES);

			// this preference is set on the server side
			dataStore.setPreference(RemoteClassLoader.CACHING_PREFERENCE, cacheRemoteClasses ? "true" : "false", true); //$NON-NLS-1$  //$NON-NLS-2$

			if (serverVersion >= 8){ // keepalive preferences
				boolean doKeepalive = store.getBoolean(IUniversalDStoreConstants.RESID_PREF_DO_KEEPALIVE);

				int keepaliveResponseTimeout = store.getInt(IUniversalDStoreConstants.RESID_PREF_KEEPALIVE_RESPONSE_TIMEOUT);
				if (keepaliveResponseTimeout == 0){ // use the default
					keepaliveResponseTimeout = store.getDefaultInt(IUniversalDStoreConstants.RESID_PREF_KEEPALIVE_RESPONSE_TIMEOUT);
				}

				int socketTimeout =  store.getInt(IUniversalDStoreConstants.RESID_PREF_SOCKET_READ_TIMEOUT);
				if (socketTimeout == 0){ // use the default
					socketTimeout = store.getDefaultInt(IUniversalDStoreConstants.RESID_PREF_SOCKET_READ_TIMEOUT);
				}

				// these preferences are only for the client
				dataStore.setPreference(XMLparser.KEEPALIVE_ENABLED_PREFERENCE, doKeepalive ? "true" : "false", false);  //$NON-NLS-1$//$NON-NLS-2$
				dataStore.setPreference(XMLparser.KEEPALIVE_RESPONSE_TIMEOUT_PREFERENCE, ""+ keepaliveResponseTimeout, false); //$NON-NLS-1$
				dataStore.setPreference(XMLparser.IO_SOCKET_READ_TIMEOUT_PREFERENCE, ""+socketTimeout, false); //$NON-NLS-1$
			}
		}
		else
		{
			dataStore.addMinersLocation("."); //$NON-NLS-1$
			// older servers initialized in one shot
			dataStore.getSchema();

	         // Initialzie the miners
	         if (monitor != null)
	         {
	        	 String imsg = ConnectorServiceResources.MSG_INITIALIZING_SERVER;
	            monitor.subTask(imsg);
	         }
	         DataElement initStatus = dataStore.initMiners();
	         statusMonitor.waitForUpdate(initStatus);
		}
	}

	/**
	 * Diagnostics the occurs after the failure of a connect.
	 * 
	 * @param launchStatus the status of the launching of the server (if a daemon was used)
	 * @param connectStatus the status of the connecting to the server
	 * @param serverLauncher the server launcher
	 * @param serverLauncherType the type of server launcher
	 * @param monitor the progress monitor
	 * 
	 * @since 3.1
	 */
	protected void handleConnectionFailure(ConnectionStatus launchStatus, ConnectionStatus connectStatus, IRemoteServerLauncher serverLauncher, ServerLaunchType serverLauncherType, IProgressMonitor monitor) throws Exception
	{
		SystemMessage msg = null;
		boolean launchFailed = false;
		if (launchStatus != null){
			launchFailed = !launchStatus.isConnected();
		}
		// if daemon launch failed because of an SSL problem			
	    if (launchFailed && launchStatus.isSLLProblem())
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
							_isConnecting = false;
							internalConnect(monitor);
							return;
						}
					}
				}
				else
				{
					String cmsg = NLS.bind(ConnectorServiceResources.MSG_CONNECT_SSL_EXCEPTION, launchStatus.getMessage());
					String cmsgDetails = ConnectorServiceResources.MSG_CONNECT_SSL_EXCEPTION_DETAILS;
					msg = createSystemMessage(IConnectorServiceMessageIds.MSG_CONNECT_SSL_EXCEPTION, IStatus.ERROR, cmsg, cmsgDetails);
				}
			}
	    }

	    // if daemon launch failed (SSL or otherwise)
		if (launchFailed && launchStatus != null)
		{
			String launchMsg = launchStatus.getMessage();
			if (launchStatus.getException() != null && serverLauncher != null)
			{
				Throwable exception = launchStatus.getException();
				String fmsg = NLS.bind(ConnectorServiceResources.MSG_CONNECT_DAEMON_FAILED_EXCEPTION, getHostName(), ""+serverLauncher.getDaemonPort()); //$NON-NLS-1$

				msg = createSystemMessage(IConnectorServiceMessageIds.MSG_CONNECT_DAEMON_FAILED_EXCEPTION, IStatus.ERROR, fmsg, exception);
			}
			else if (launchMsg != null && launchMsg.indexOf(IDataStoreConstants.AUTHENTICATION_FAILED) != -1)
			{
				_isConnecting = false;
				if (launchFailed)
			    {
			        clearPassword(true, true);
			    }

				// Display error message
				String msgTxt = CommonMessages.MSG_COMM_AUTH_FAILED;
				String msgDetails = NLS.bind(CommonMessages.MSG_COMM_AUTH_FAILED_DETAILS, getHostName());

				msg = createSystemMessage(ICommonMessageIds.MSG_COMM_AUTH_FAILED, IStatus.ERROR, msgTxt, msgDetails);

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
							acquireCredentials(true);
						}
						catch (OperationCanceledException e)
						{
							connectException = e;
						}
					}
				});

				// Check if the user cancelled the prompt
				if (connectException instanceof OperationCanceledException)
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
				_isConnecting = false;
				SystemSignonInformation oldCredentials = (SystemSignonInformation) getCredentialsProvider().getCredentials();
				SystemSignonInformation newCredentials = null;
				while (launchMsg != null && (isPasswordExpired(launchMsg) || isNewPasswordInvalid(launchMsg)))
				{
					String msgTxt = ConnectorServiceResources.MSG_VALIDATE_PASSWORD_INVALID;
					String msgDetails = ConnectorServiceResources.MSG_VALIDATE_PASSWORD_INVALID_DETAILS;
					String msgId = IConnectorServiceMessageIds.MSG_VALIDATE_PASSWORD_INVALID;
					if (isPasswordExpired(launchMsg)){
						msgTxt = ConnectorServiceResources.MSG_VALIDATE_PASSWORD_EXPIRED;
						msgDetails = ConnectorServiceResources.MSG_VALIDATE_PASSWORD_EXPIRED_DETAILS;
						msgId = IConnectorServiceMessageIds.MSG_VALIDATE_PASSWORD_EXPIRED;
					}

					SystemMessage message = createSystemMessage(msgId, IStatus.ERROR, msgTxt, msgDetails);

					getCredentialsProvider().repairCredentials(message);
					newCredentials = (SystemSignonInformation) getCredentialsProvider().getCredentials();
					launchStatus = changePassword(clientConnection, oldCredentials, serverLauncher, monitor, newCredentials.getPassword());
					launchMsg = launchStatus.getMessage();
				}
				if (launchMsg != null && launchMsg.equals(IDataStoreConstants.ATTEMPT_RECONNECT))
				{						
					internalConnect(monitor);
					return;
				}
//				NewPasswordInfo newPasswordInfo = null;
//				while (launchMsg != null && (isPasswordExpired(launchMsg) || isNewPasswordInvalid(launchMsg)))
//				{
//					newPasswordInfo = promptForNewPassword(isPasswordExpired(launchMsg) ? RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PASSWORD_EXPIRED) : RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PASSWORD_INVALID));
//					launchStatus = changePassword(clientConnection, getPasswordInformation(), serverLauncher, monitor, newPasswordInfo.newPassword);
//					launchMsg = launchStatus.getMessage();
//				}
//				if (newPasswordInfo != null)
//				{
//					setPassword(getPasswordInformation().getUserid(), newPasswordInfo.newPassword, newPasswordInfo.savePassword);
//				}
//				if (launchMsg != null && launchMsg.equals(IDataStoreConstants.ATTEMPT_RECONNECT))
//				{
//					internalConnect(monitor);
//					return;
//				}
			}
			else if (launchMsg != null)
			{
				String msgTxt = NLS.bind(ConnectorServiceResources.MSG_CONNECT_DAEMON_FAILED, getHostName(), clientConnection.getPort());
				msg = createSystemMessage(IConnectorServiceMessageIds.MSG_CONNECT_DAEMON_FAILED, IStatus.ERROR, msgTxt, launchMsg);
			}
		}

		// if connection failed for known reason
		else if (connectStatus != null && !connectStatus.isConnected())
		{
			if (connectStatus.getMessage().startsWith(ClientConnection.INCOMPATIBLE_SERVER_UPDATE))
			{
				String msgTxt = NLS.bind(ConnectorServiceResources.MSG_COMM_INCOMPATIBLE_UPDATE, getHostName());
				String msgDetails = ConnectorServiceResources.MSG_COMM_INCOMPATIBLE_UPDATE_DETAILS;

				msg = createSystemMessage(IConnectorServiceMessageIds.MSG_COMM_INCOMPATIBLE_UPDATE, IStatus.ERROR, msgTxt, msgDetails);
			}
			else if (connectStatus.getMessage().startsWith(ClientConnection.INCOMPATIBLE_PROTOCOL))
			{
				String msgTxt = NLS.bind(ConnectorServiceResources.MSG_COMM_INCOMPATIBLE_PROTOCOL, getHostName());
				String msgDetails = ConnectorServiceResources.MSG_COMM_INCOMPATIBLE_PROTOCOL_DETAILS;

				msg = createSystemMessage(IConnectorServiceMessageIds.MSG_COMM_INCOMPATIBLE_PROTOCOL, IStatus.ERROR, msgTxt, msgDetails);
			}
			else
			{
				Throwable exception = connectStatus.getException();
				if (exception instanceof SSLHandshakeException)
				{
					List certs = connectStatus.getUntrustedCertificates();
					if (certs != null && certs.size() > 0)
					{
						ISystemKeystoreProvider provider = SystemKeystoreProviderManager.getInstance().getDefaultProvider();
						if (provider != null)
						{
							_isConnecting = false;
							provider.importCertificates(certs, getHostName());								

							
							// Don't attempt reconnect when server was started manually.  The problem is that 
							// in that situation, the server will have terminated on the failed connection
							// due to the missing certs
							if (serverLauncherType != ServerLaunchType.RUNNING_LITERAL){
								internalConnect(monitor);
							}
							return;
						}
					}
				}
				else if (exception != null)
				{
					String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, getHostName());
					msg = createSystemMessage(ICommonMessageIds.MSG_CONNECT_FAILED, IStatus.ERROR, msgTxt, exception);
				}
				else if (connectStatus.getMessage() != null){
					msg = createSystemMessage(ICommonMessageIds.MSG_CONNECT_FAILED, IStatus.ERROR, connectStatus.getMessage());
				}
			}
		}

		// if connect failed for unknown reason
		else if (connectStatus == null)
		{
			SystemBasePlugin.logError("Failed to connect to remote system", null); //$NON-NLS-1$
			String msgTxt = NLS.bind(ConnectorServiceResources.MSG_COMM_CONNECT_FAILED, getHostName());
			String msgDetails = NLS.bind(ConnectorServiceResources.MSG_COMM_CONNECT_FAILED_DETAILS, getHostName());
			msg = createSystemMessage(IConnectorServiceMessageIds.MSG_COMM_CONNECT_FAILED, IStatus.ERROR, msgTxt, msgDetails);
		}

		// if, for some reason, we don't have a message
		if (msg == null && connectStatus != null)
		{
			SystemBasePlugin.logError("Failed to connect to remote system" + connectStatus.getMessage(), null); //$NON-NLS-1$
			String msgTxt = NLS.bind(ConnectorServiceResources.MSG_COMM_CONNECT_FAILED, getHostName());
			String msgDetails = NLS.bind(ConnectorServiceResources.MSG_COMM_CONNECT_FAILED_DETAILS, getHostName());
			msg = createSystemMessage(IConnectorServiceMessageIds.MSG_COMM_CONNECT_FAILED, IStatus.ERROR, msgTxt, msgDetails);
		}

		clientConnection.disconnect();
		clientConnection = null;

		// yantzi: artemis 6.0, check for invalid login (user ID / pwd) and reprompt for signon information
		if (msg != null &&
				// tODO use ID or something instead of string
				msg.getLevelOneText().startsWith(NLS.bind(ConnectorServiceResources.MSG_COMM_INVALID_LOGIN, getHostName())))
		{
			if (launchFailed)
		    {
		        clearPassword(true, true);
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
						acquireCredentials(true);
					}
					catch (OperationCanceledException e)
					{
						connectException = e;
					}
				}
			});
			
			_isConnecting = false;

			// Check if the user cancelled the prompt
			if (connectException instanceof OperationCanceledException)
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

		_isConnecting = false;
		throw new SystemMessageException(msg);

	}
	

	
	/**
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#connect(IProgressMonitor)
	 */
	protected void internalConnect(IProgressMonitor monitor) throws Exception
	{
	    if (isConnected() || _isConnecting) {
	        return;
	    }
	    
	    _isConnecting = true;
	    try{
		    Boolean alertedNONSSL = new Boolean(false);
	
			// set A_PLUGIN_PATH so that dstore picks up the property
			setPluginPathProperty();
	
			// Fire comm event to signal state about to change
			fireCommunicationsEvent(CommunicationsEvent.BEFORE_CONNECT);
	
			ConnectionStatus connectStatus = null;
			ConnectionStatus launchStatus = null;
	
			clientConnection = new ClientConnection(getPrimarySubSystem().getHost().getAliasName());
	
			clientConnection.setHost(getHostName());
			clientConnection.setPort(Integer.toString(getPort()));
	
			getPrimarySubSystem();
			IRemoteServerLauncher serverLauncher = getDStoreServerLauncher();
	
			ServerLaunchType serverLauncherType = null;
			if (serverLauncher != null){
			    serverLauncherType = serverLauncher.getServerLaunchType();
			}
	
			SystemSignonInformation info = getSignonInformation();
			if (serverLauncherType == ServerLaunchType.REXEC_LITERAL){	// start the server via REXEC			
				connectStatus = connectWithREXEC(info, serverLauncher, monitor);
			}
			else if (serverLauncherType == ServerLaunchType.DAEMON_LITERAL) { // start the server via the daemon
			
				ConnectionStatusPair connectStatusPair = connectWithDaemon(info, serverLauncher, alertedNONSSL, monitor);
				connectStatus = connectStatusPair.getConnectStatus();
				launchStatus = connectStatusPair.getLaunchStatus();
				alertedNONSSL = connectStatusPair.getAlertedNonSSL();
			}
			else if (serverLauncherType == ServerLaunchType.RUNNING_LITERAL){ // connect to running server
				connectStatus = connectWithRunning(monitor);
			}		
			else { // server launcher type is unknown
				connectStatus = connectWithOther(clientConnection, info, serverLauncher, monitor);		
			}
	
			if (connectStatus != null && connectStatus.isConnected()){  // connected 
				initializeConnection(launchStatus, connectStatus, alertedNONSSL, monitor);
			}
			else  {	// diagnosis, reconnection and other connection failure handling
				handleConnectionFailure(launchStatus, connectStatus, serverLauncher, serverLauncherType, monitor);
			}
	    }
	    finally {
		_isConnecting = false;
	    }
	}

	protected boolean isPortOutOfRange(String message)
	{
		return message.indexOf(IDataStoreConstants.PORT_OUT_RANGE) != -1;
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

	protected int launchUsingRexec(SystemSignonInformation info, IServerLauncherProperties serverLauncherProperties, IProgressMonitor monitor) throws Exception
	{
		IServerLauncher starter = getRemoteServerLauncher();
		starter.setSignonInformation(info);
		starter.setServerLauncherProperties(serverLauncherProperties);

		String serverPort = (String)starter.launch(monitor);
		if (monitor.isCanceled())
		{
			_isConnecting = false;
			SystemMessage msg = createSystemMessage(ICommonMessageIds.MSG_OPERATION_CANCELLED, IStatus.CANCEL, CommonMessages.MSG_OPERATION_CANCELLED);
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



	protected boolean promptForTrusting( X509Certificate cert)
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
	     return clientConnection.launchServer(info.getUserId(), info.getPassword(), daemonPort, timeout);
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

	public boolean supportsRemoteServerLaunching()
	{
		return true;
	}

	public boolean supportsServerLaunchProperties()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsPassword()
	 */
	public boolean supportsPassword() {
		boolean result = super.supportsPassword();
		IHost host = getHost();
		if (host.getSystemType().isWindows()) {
			result = false;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsUserid()
	 */
	public boolean supportsUserId() {
		boolean result = super.supportsUserId();
		if (getHost().getSystemType().isWindows()) {
			result = false;
		}
		return result;
	}


	/**
	 * @since org.eclipse.rse.connectorservice.dstore 3.0
	 */
	protected SystemMessage createSystemMessage(String msgId, int severity, String msg) {
		return createSystemMessage(msgId, severity, msg, (String)null);
	}

	/**
	 * @since org.eclipse.rse.connectorservice.dstore 3.0
	 */
	protected SystemMessage createSystemMessage(String msgId, int severity, String msg, Throwable e) {
		return new SimpleSystemMessage(Activator.PLUGIN_ID, msgId, severity, msg, e);
	}

	/**
	 * @since org.eclipse.rse.connectorservice.dstore 3.0
	 */
	protected SystemMessage createSystemMessage(String msgId, int severity, String msg, String msgDetails) {
		return new SimpleSystemMessage(Activator.PLUGIN_ID, msgId, severity, msg, msgDetails);
	}

}

