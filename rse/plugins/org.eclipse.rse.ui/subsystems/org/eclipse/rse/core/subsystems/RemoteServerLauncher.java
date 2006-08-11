/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core.subsystems;
import java.util.Arrays;
import java.util.List;

import org.eclipse.rse.core.internal.subsystems.RemoteServerLauncherConstants;
import org.eclipse.rse.core.internal.subsystems.ServerLauncher;
import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IPropertyType;
import org.eclipse.rse.model.PropertyType;
import org.eclipse.rse.ui.SystemResources;


/**
 * <!-- begin-user-doc -->
 * This subclass of {@link IServerLauncherProperties} is for use by some dstore-based subsystems, although
 *  is possibly of value to vendors as well. The dstore-based subsystems use server code
 *  written in Java, on top of the datastore technology. You can read about this in the 
 *  developer guide for Remote System Explorer. The bottom line, however, is we offer the 
 *  user a number of ways to start that remote server from the client, as well to connect
 *  to it if it is already running. This class encapsulates the properties to support that.
 * <p>
 * Create instances via {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#createRemoteServerLauncher(ISubSystem)}
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.rse.core.subsystems.RemoteServerLauncher#getServerLaunchType <em>Server Launch Type</em>}</li>
 *   <li>{@link org.eclipse.rse.core.subsystems.RemoteServerLauncher#getPort <em>Port</em>}</li>
 *   <li>{@link org.eclipse.rse.core.subsystems.RemoteServerLauncher#getRexecPort <em>Rexec Port</em>}</li>
 *   <li>{@link org.eclipse.rse.core.subsystems.RemoteServerLauncher#getDaemonPort <em>Daemon Port</em>}</li>
 *   <li>{@link org.eclipse.rse.core.subsystems.RemoteServerLauncher#getServerPath <em>Server Path</em>}</li>
 *   <li>{@link org.eclipse.rse.core.subsystems.RemoteServerLauncher#getServerScript <em>Server Script</em>}</li>
 *   <li>{@link org.eclipse.rse.core.subsystems.RemoteServerLauncher#getRemoteAttributes <em>Remote Attributes</em>}</li>
 *   <li>{@link org.eclipse.rse.core.subsystems.RemoteServerLauncher#getRestrictedTypes <em>Restricted Types</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RemoteServerLauncher extends ServerLauncher implements IRemoteServerLauncher
{

	protected static final ServerLaunchType SERVER_LAUNCH_TYPE_EDEFAULT = ServerLaunchType.DAEMON_LITERAL;

	// proeprty set keys
	protected final String PROPERTY_SET_NAME = "Launcher Properties";

	protected final String KEY_DAEMON_PORT = "daemon.port";
	protected final String KEY_REXEC_PORT = "rexec.port";
	protected final String KEY_SERVER_LAUNCH_TYPE_NAME = "server.launch.type.name";
	protected final String KEY_SERVER_PATH = "server.path";
	protected final String KEY_SERVER_SCRIPT = "server.script";
	protected final String KEY_AUTODETECT_SSL = "autodetect.ssl";


	protected ServerLaunchType _serverLaunchType = SERVER_LAUNCH_TYPE_EDEFAULT;

	protected boolean _hasSetServerLaunchType = false;

	protected static final int REXEC_PORT_EDEFAULT = 512;

	protected int _rexecPort = REXEC_PORT_EDEFAULT;

	protected static final int DAEMON_PORT_EDEFAULT = 4035;

	protected int _daemonPort = DAEMON_PORT_EDEFAULT;

	protected static final String SERVER_PATH_EDEFAULT = null;

	protected String _serverPath = SERVER_PATH_EDEFAULT;

	protected static final String SERVER_SCRIPT_EDEFAULT = null;

	protected String _serverScript = SERVER_SCRIPT_EDEFAULT;

	protected static final String REMOTE_ATTRIBUTES_EDEFAULT = null;

	protected static final String RESTRICTED_TYPES_EDEFAULT = null;
	
	protected static final boolean AUTODETECT_SSL_EDEFAULT = true;
	
	protected boolean _autoDetectSSL = AUTODETECT_SSL_EDEFAULT;

	protected IPropertyType _serverLauncherEnumType;
	
	protected RemoteServerLauncher(String name, IConnectorService connectorService)
	{
		super(name, connectorService);		
	}
	
	public IPropertyType getServerLauncherPropertyType()
	{
		if (_serverLauncherEnumType == null)
		{
			// for persistence
			List values = Arrays.asList(getSupportedLauncherEnumTypes());
				// DKM - only need supported types 
				/// ServerLaunchType.VALUES;
			
			String[] enumValues = new String[values.size()];
			for (int i = 0; i < values.size(); i++)
			{
				ServerLaunchType type = (ServerLaunchType)values.get(i);
				enumValues[i] = type.getName();
			}
			_serverLauncherEnumType = PropertyType.getEnumPropertyType(enumValues);
		}
		return _serverLauncherEnumType;
	}
	
	protected ServerLaunchType[] getSupportedLauncherEnumTypes()
	{
		return new ServerLaunchType[]
		                  {
		ServerLaunchType.DAEMON_LITERAL,
		ServerLaunchType.REXEC_LITERAL,
		ServerLaunchType.RUNNING_LITERAL
		                  };
	}
	
	public void restoreFromProperties()
	{
		IPropertySet set = getPropertySet(PROPERTY_SET_NAME);
		if (set != null)
		{
			try
			{
				IProperty launchTypeProperty = set.getProperty(KEY_SERVER_LAUNCH_TYPE_NAME);
				launchTypeProperty.setLabel(SystemResources.RESID_PROP_SERVERLAUNCHER_MEANS_LABEL);
				String launchTypeName = launchTypeProperty.getValue();
				_serverLaunchType  = ServerLaunchType.get(launchTypeName);
				
				IProperty daemonPortProperty = set.getProperty(KEY_DAEMON_PORT);
				daemonPortProperty.setEnabled(_serverLaunchType.getType() == ServerLaunchType.DAEMON);
				daemonPortProperty.setLabel(SystemResources.RESID_CONNECTION_DAEMON_PORT_LABEL);
				
				_daemonPort = Integer.parseInt(daemonPortProperty.getValue());
				
				IProperty autoDetectProperty = set.getProperty(KEY_AUTODETECT_SSL);
				if (autoDetectProperty != null)
				{
					autoDetectProperty.setEnabled(_serverLaunchType.getType() == ServerLaunchType.REXEC);
					autoDetectProperty.setLabel(SystemResources.RESID_SUBSYSTEM_AUTODETECT_LABEL);
				
					_autoDetectSSL = Boolean.getBoolean(autoDetectProperty.getValue());
				}
				
				boolean usingRexec = _serverLaunchType.getType() == ServerLaunchType.REXEC;
				IProperty rexecPortProperty = set.getProperty(KEY_REXEC_PORT);
				rexecPortProperty.setEnabled(usingRexec);
				rexecPortProperty.setLabel(SystemResources.RESID_CONNECTION_PORT_LABEL);
				
				_rexecPort  = Integer.parseInt(rexecPortProperty.getValue());
				
				IProperty serverPathProperty = set.getProperty(KEY_SERVER_PATH);
				serverPathProperty.setEnabled(usingRexec);
				serverPathProperty.setLabel(SystemResources.RESID_PROP_SERVERLAUNCHER_PATH);
				_serverPath = serverPathProperty.getValue();
				
				IProperty serverScriptProperty = set.getProperty(KEY_SERVER_SCRIPT);
				serverScriptProperty.setEnabled(usingRexec);
				serverScriptProperty.setLabel(SystemResources.RESID_PROP_SERVERLAUNCHER_INVOCATION);
				_serverScript = serverScriptProperty.getValue();
				
				_hasSetServerLaunchType = true;
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void saveToProperties()
	{
		IPropertySet set = getPropertySet(PROPERTY_SET_NAME);
		if (set == null)
		{
			set = createPropertySet(PROPERTY_SET_NAME, getDescription());						
		}
		
		IProperty launchTypeProperty = set.addProperty(KEY_SERVER_LAUNCH_TYPE_NAME, _serverLaunchType.getName(), getServerLauncherPropertyType());
		launchTypeProperty.setLabel(SystemResources.RESID_PROP_SERVERLAUNCHER_MEANS_LABEL);
		
		IProperty daemonPortProperty = set.addProperty(KEY_DAEMON_PORT, ""+_daemonPort, PropertyType.getIntegerPropertyType());
		daemonPortProperty.setEnabled(_serverLaunchType.getType() == ServerLaunchType.DAEMON);
		daemonPortProperty.setLabel(SystemResources.RESID_CONNECTION_DAEMON_PORT_LABEL);
		
		IProperty rexecPortProperty  = set.addProperty(KEY_REXEC_PORT, ""+_rexecPort, PropertyType.getIntegerPropertyType());	
		boolean usingRexec = _serverLaunchType.getType() == ServerLaunchType.REXEC;
		rexecPortProperty.setEnabled(usingRexec);
		rexecPortProperty.setLabel(SystemResources.RESID_CONNECTION_PORT_LABEL);
		
		IProperty autoDetectSSLProperty  = set.addProperty(KEY_AUTODETECT_SSL, ""+_autoDetectSSL, PropertyType.getBooleanPropertyType());	
		autoDetectSSLProperty.setEnabled(usingRexec);
		autoDetectSSLProperty.setLabel(SystemResources.RESID_SUBSYSTEM_AUTODETECT_LABEL);
		
		IProperty serverPathProperty  = set.addProperty(KEY_SERVER_PATH, ""+_serverPath);
		serverPathProperty.setLabel(SystemResources.RESID_PROP_SERVERLAUNCHER_PATH);
		serverPathProperty.setEnabled(usingRexec);
		
		IProperty serverScriptProperty  = set.addProperty(KEY_SERVER_SCRIPT, ""+_serverScript);
		serverScriptProperty.setEnabled(usingRexec);
		serverScriptProperty.setLabel(SystemResources.RESID_PROP_SERVERLAUNCHER_INVOCATION);
	}
	
	
	/**
	 * Clone the contents of this server launcher into the given server launcher
	 * <i>Your sublcass must override this if you add additional attributes! Be sure
	 *  to call super.cloneServerLauncher(newOne) first.</i>
	 * @return the given new server launcher, for convenience.
	 */
	public IServerLauncherProperties cloneServerLauncher(IServerLauncherProperties newOne)
	{
		super.cloneServerLauncher(newOne);
		IRemoteServerLauncher remoteNewOne = (IRemoteServerLauncher)newOne;
		remoteNewOne.addPropertySets(getPropertySets());
		remoteNewOne.setDaemonPort(getDaemonPort());
		remoteNewOne.setRexecPort(getRexecPort());		
		remoteNewOne.setServerLaunchType(getServerLaunchTypeGen());
		remoteNewOne.setServerPath(getServerPath());
		remoteNewOne.setServerScript(getServerScript());
		remoteNewOne.setAutoDetectSSL(getAutoDetectSSL());
		return remoteNewOne;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * This is the means by which to start the server-side code, as specified by the user, typically.
	 * <!-- end-user-doc -->
	 */
	public ServerLaunchType getServerLaunchType()
	{		
		if (!isSetServerLaunchType())
		{
			if (isEnabledServerLaunchType(ServerLaunchType.DAEMON_LITERAL))
				return ServerLaunchType.DAEMON_LITERAL;
			else if (isEnabledServerLaunchType(ServerLaunchType.REXEC_LITERAL))
				return ServerLaunchType.REXEC_LITERAL;
			else if (isEnabledServerLaunchType(ServerLaunchType.RUNNING_LITERAL))
				return ServerLaunchType.RUNNING_LITERAL;
			else if (isEnabledServerLaunchType(ServerLaunchType.TELNET_LITERAL))
				return ServerLaunchType.TELNET_LITERAL;
			else if (isEnabledServerLaunchType(ServerLaunchType.SSH_LITERAL))
				return ServerLaunchType.SSH_LITERAL;
			else if (isEnabledServerLaunchType(ServerLaunchType.FTP_LITERAL))
				return ServerLaunchType.FTP_LITERAL;
			else
				return ServerLaunchType.HTTP_LITERAL;							
		}
		return _serverLaunchType;
	}
	/**
	 * <!-- begin-user-doc -->
	 * Generated. Do not call or use.
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ServerLaunchType getServerLaunchTypeGen()
	{
		return _serverLaunchType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * This is the means by which to start the server-side code, as specified by the user, typically.
	 * It is one of the constants in the enumeration class {@link org.eclipse.rse.core.subsystems.ServerLaunchType}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setServerLaunchType(ServerLaunchType newServerLaunchType)
	{
		ServerLaunchType oldServerLaunchType = _serverLaunchType;
		if (oldServerLaunchType != newServerLaunchType)
		{
			_serverLaunchType = newServerLaunchType == null ? SERVER_LAUNCH_TYPE_EDEFAULT : newServerLaunchType;
			_hasSetServerLaunchType = true;
			setDirty(true);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetServerLaunchType()
	{
		return _hasSetServerLaunchType;
	}

	/**
	 * Return the port used for the REXEC option, as an Integer
	 */
	public int getRexecPort()
	{
		return _rexecPort;
	}
	/**
	 * Set the REXEC port value, as an int
	 */
	public void setRexecPort(int newRexecPort)
	{
		if (newRexecPort != _rexecPort)
		{
			_rexecPort = newRexecPort;
			setDirty(true);
		}
	}
	
	/**
	 * Return the whether or not to auto-detect SSL
	 */
	public boolean getAutoDetectSSL()
	{
		return _autoDetectSSL;
	}
	/**
	 * Sets whether or not to auto-detect SSL
	 */
	public void setAutoDetectSSL(boolean auto)
	{
		if (auto != _autoDetectSSL)
		{
			_autoDetectSSL = auto;
			setDirty(true);
		}
	}

	/**
	 * Return the port used for the DAEMON option, as an Integer
	 */
	public int getDaemonPort()
	{
		return _daemonPort;
	}
	/**
	 * Set the DAEMON port value, as an int
	 */
	public void setDaemonPort(int newDaemonPort)
	{
		if (_daemonPort != newDaemonPort)
		{
			_daemonPort = newDaemonPort;
			setDirty(true);
		}		
	}

	/**
	 * <!-- begin-user-doc -->
	 * The path where the server lives on the remote system. Used by at least the REXEC server launch type.
	 * Will be null if not set.
	 * <!-- end-user-doc -->
	 */
	public String getServerPath()
	{
		String serverPath = _serverPath;
		if ((serverPath == null) || (serverPath.length() == 0))
		{
			serverPath = RemoteServerLauncherConstants.DEFAULT_REXEC_PATH; 
		}
		return serverPath;
	}
	/**
	 * <!-- begin-user-doc -->
	 * The path where the server lives on the remote system. Used by at least the REXEC server launch type.
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setServerPath(String newServerPath)
	{
		String oldServerPath = _serverPath;
		if (oldServerPath == null || !oldServerPath.equals(newServerPath))
		{
			_serverPath = newServerPath;
			setDirty(true);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * The script to run on the remote system, to start the server code.
	 * <!-- end-user-doc -->
	 */
	public String getServerScript()
	{
		String serverScript = _serverScript;
		
		if ((serverScript == null) || (serverScript.length() == 0)) 
		{
			serverScript = "server." + getConnectorService().getHost().getSystemType().toLowerCase(); 
		}
		
		return serverScript;
	}

	
	public void setServerScript(String newServerScript)
	{
		String oldServerScript = _serverScript;
		if (oldServerScript != newServerScript)
		{
			_serverScript = newServerScript;
			setDirty(true);
		}
	}

	/**
	 * This methods returns the enablement state per server launch type.
	 * If {@link #enableServerLaunchType(ServerLaunchType,boolean)} has not been
	 *  called for this server launch type, then we defer to the subsystem factory's
	 *  method: 
	 * {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchType(ServerLaunchType)}.
	 * @see org.eclipse.rse.core.subsystems.ServerLaunchType
	 */
	public boolean isEnabledServerLaunchType(ServerLaunchType serverLaunchType)
	{
		IPropertySet set = getPropertySet("restrictedTypes");
		if (set != null)
		{
			String value = set.getPropertyValue(serverLaunchType.getName());
			if (value != null)
			{
				return value.equals("true");
			}
		}
		
		ISubSystem primarySS = getConnectorService().getPrimarySubSystem();
		if (primarySS != null)
		{
			return primarySS.getSubSystemConfiguration().supportsServerLaunchType(serverLaunchType);		

		}
		return true;
	}


	

	/**
	 * @deprecated
	 */
	public void setRestrictedType(String attributeName, String attributeValue)
	{
		IPropertySet set = getPropertySet("restrictedTypes");
		if (set == null)
		{
			set = createPropertySet("restrictedTypes", getDescription());
		}
		set.addProperty(attributeName, attributeValue);
	}
	
	public void enableServerLaunchType(ServerLaunchType serverLaunchType, boolean enable) {
		// TODO Auto-generated method stubS
		
	}

} //RemoteServerLauncherImpl