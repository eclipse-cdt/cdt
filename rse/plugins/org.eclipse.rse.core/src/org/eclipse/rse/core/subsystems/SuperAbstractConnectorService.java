/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 ********************************************************************************/
package org.eclipse.rse.core.subsystems;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.RSEModelObject;

/**
 * This is a base class to make it easier to create connector service classes.
 * <p>
 * An {@link org.eclipse.rse.core.subsystems.IConnectorService} object
 * is returned from a subsystem object via getConnectorService(), and
 * it is used to represent the live connection to a particular subsystem.
 * <p>
 * You must override/implement
 * <ul>
 * <li>isConnected
 * <li>internalConnect
 * <li>internalDisconnect
 * <li>getCredentialsProvider
 * </ul>
 * You should override:
 * <ul>
 * <li>reset 
 * <li>getVersionReleaseModification
 * <li>getHomeDirectory
 * <li>getTempDirectory
 * </ul>
 * You can override:
 * <ul>
 * <li>supportsUserId
 * <li>requiresUserId
 * <li>supportsPassword
 * <li>requiresPassword
 * </ul>
 * 
 */ 
public abstract class SuperAbstractConnectorService extends RSEModelObject implements IConnectorService {

	private Vector commListeners = new Vector(5);
	private ISubSystem _primarySubSystem = null;
	private List _registeredSubSystems = new ArrayList();
	private IHost _host;
	private String _description;
	private String _name;
	private int _port;
	private boolean _usingSSL;

	/**
	 * The result of calling launch in the server launcher object, in the connect method  
	 */
	protected Object launchResult;
	/**
	 * The result of calling connect in the server launcher object, in the connect method
	 */
	protected Object connectResult;

	public SuperAbstractConnectorService(String name, String description, IHost host, int port) {
		_name = name;
		_description = description;
		_host = host;
		_port = port;
	}

	public final boolean isServerLaunchTypeEnabled(ISubSystem subsystem, ServerLaunchType serverLaunchType) {
		IServerLauncher sl = getRemoteServerLauncher();
		if (sl instanceof RemoteServerLauncher) {
			RemoteServerLauncher isl = (RemoteServerLauncher) sl;
			return isl.isEnabledServerLaunchType(serverLaunchType);
		} else
			return subsystem.getSubSystemConfiguration().supportsServerLaunchType(serverLaunchType);
	}
	
	public IServerLauncher getRemoteServerLauncher() {
		return null;
	}

	public boolean supportsRemoteServerLaunching() {
		return false;
	}

	public boolean supportsServerLaunchProperties() {
		return false;
	}
	
	public IServerLauncherProperties getRemoteServerLauncherProperties() {
		return null;
	}

	public void setRemoteServerLauncherProperties(IServerLauncherProperties newRemoteServerLauncher) {
	}

	public final boolean hasRemoteServerLauncherProperties() {
		return getRemoteServerLauncherProperties() != null;
	}

	/**
	 * <i>Fully implemented, no need to override.</i><br>
	 * @see IConnectorService#addCommunicationsListener(ICommunicationsListener)
	 */
	public final void addCommunicationsListener(ICommunicationsListener listener) {
		if (!commListeners.contains(listener)) {
			commListeners.add(listener);
		}
	}

	/**
	 * <i>Fully implemented, no need to override.</i><br>
	 * @see IConnectorService#removeCommunicationsListener(ICommunicationsListener)
	 */
	public final void removeCommunicationsListener(ICommunicationsListener listener) {
		commListeners.remove(listener);		
	}

	/**
	 * Fires the communication event mentioned in the eventType.
	 * @param eventType the communications event to fire.
	 */
	final protected void fireCommunicationsEvent(int eventType) {
		CommunicationsEvent e = new CommunicationsEvent(this, eventType);
		Object[] items = commListeners.toArray();
		for (int loop=0; loop < items.length; loop++) {		
			((ICommunicationsListener) items[loop]).communicationsStateChange(e);
		}							
	}

	public final IHost getHost() {
		return _host;
	}

	public final void setHost(IHost host) {
		_host = host;
	}

	public final String getDescription() {
		return _description;
	}

	/**
	 * 
	 */
	public final String getName() {
		return _name;
	}

	public final void setPort(int port) {
		if (port != _port)
		{
			_port = port;
			setDirty(true);
		}
	}

	public final int getPort() {
		return _port;
	}

	public final ISubSystem getPrimarySubSystem() {
		if (_primarySubSystem == null)
		{
			if (_registeredSubSystems.size() == 0)
			{
				
			}
			else
			{
				ISubSystem ss = (ISubSystem)_registeredSubSystems.get(0);
				_primarySubSystem = ss.getPrimarySubSystem();
			}
		}
		return _primarySubSystem;
	}

	/**
	 * Set the subsystem, when its not known at constructor time
	 */
	public final void registerSubSystem(ISubSystem ss) {
		if (!_registeredSubSystems.contains(ss))
		{    		
			_registeredSubSystems.add(ss);
		}   	
	}

	/**
	 * Removes the subsystem from teh list
	 * @param ss
	 */
	public final void deregisterSubSystem(ISubSystem ss) {
		_registeredSubSystems.remove(ss);
	}

	public final boolean commit() {
		return RSECorePlugin.getThePersistenceManager().commit(getHost());
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Returns the system type for this connection:<br> <code>getSubSystem().getSystemConnection().getSystemType()</code>
	 */
	public final String getHostType() {
		return getHost().getSystemType();
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Returns the host name for the connection this system's subsystem is associated with:</br>
	 * <code>getSubSystem().getSystemConnection().getHostName()</code>
	 */
	public final String getHostName() {
		return getHost().getHostName();
	}

	/**
	 * Return the version, release, modification of the remote system,
	 * if connected, if applicable and if available. Else return null. It
	 * is up to each subsystem to decide how to interpret what is returned.
	 * This implementation returns the empty string.
	 * <p>
	 * This is used to show the VRM in the property sheet 
	 * when the subsystem is selected.
	 * <p>
	 * Up to each implementer to decide if this will be cached.
	 * <p>
	 * @return an empty string
	 */
	public String getVersionReleaseModification() {
		return ""; //$NON-NLS-1$
	}

	public final ISubSystem[] getSubSystems() {
		return (ISubSystem[])_registeredSubSystems.toArray(new ISubSystem[_registeredSubSystems.size()]);
	}

	protected final void intializeSubSystems(IProgressMonitor monitor) {
		for (int i = 0; i < _registeredSubSystems.size(); i++)
		{
			ISubSystem ss = (ISubSystem)_registeredSubSystems.get(i);
			ss.initializeSubSystem(monitor);    		
		}
	}

	protected final void unintializeSubSystems(IProgressMonitor monitor) {
		for (int i = 0; i < _registeredSubSystems.size(); i++)
		{
			ISubSystem ss = (ISubSystem)_registeredSubSystems.get(i);
			ss.uninitializeSubSystem(monitor);    		
		}
	}

	protected final void notifyDisconnection() {
		 // Fire comm event to signal state changed
		if (!isConnected()) fireCommunicationsEvent(CommunicationsEvent.AFTER_DISCONNECT);
	}

	protected final void notifyConnection() {
		if (isConnected()) fireCommunicationsEvent(CommunicationsEvent.AFTER_CONNECT);
	}

	protected final void notifyError() {
		fireCommunicationsEvent(CommunicationsEvent.CONNECTION_ERROR);
	}

	public final boolean isUsingSSL() {
		return _usingSSL;
	}

	public final void setIsUsingSSL(boolean flag) {
		if (_usingSSL != flag)
		{
			_usingSSL = flag;
			setDirty(true);
		}
	}

	/**
	 * Return the temp directory of the remote system for the current user,
	 * if available. This implementation returns the empty string.
	 * Up to each implementer to decide how to implement, and if this will be cached.
	 * @return an empty string
	 */
	public String getTempDirectory() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the home directory of the remote system for the current user,
	 * if available. This implementation returns the empty string.
	 * Up to each implementer to decide how to implement, and if this will be cached.
	 * @return an empty string
	 */
	public String getHomeDirectory() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * <i>Optionally override if you add any instance variables.</i><br>
	 * The following is called whenever a system is redefined or disconnected.
	 * Each subsystem needs to be informed so it can clear out any expansions, etc.
	 * By default it does nothing.
	 * Override if you have an internal object that must be nulled out.
	 * If overridden you should call super.reset();
	 */
	public void reset() {
	}

	/**
	 * Return the port to use for connecting to the remote server, once it is running.
	 * By default, this is the subsystem's port property, via {@link #getPort()}.
	 * Override if appropriate. 
	 * <br> This is called by the default implementation of {@link #connect(IProgressMonitor)}, if
	 * subsystem.getParentSubSystemConfiguration().supportsServerLaunchProperties() is true.
	 */
	protected int getConnectPort() {
		return getPort();
	}

	protected abstract void internalConnect(IProgressMonitor monitor) throws Exception;

	protected abstract void internalDisconnect(IProgressMonitor monitor) throws Exception;
	
	protected abstract ICredentialsProvider getCredentialsProvider();

	/**
	 * Returns true if this connector service can share it's credentials
	 * with other connector services in this host.
	 * This default implementation will always return true.
	 * Override if necessary.
	 * @return true
	 */
	public boolean sharesCredentials() {
	    return true;
	}

	/**
	 * Returns true if this connector service can inherit the credentials of
	 * other connector services in this host.
	 * This default implementation always returns true. 
	 * Override if necessary.
	 * @return true
	 */
	public boolean inheritsCredentials() {
	    return true;
	}

	public final boolean supportsPassword() {
		ICredentialsProvider cp = getCredentialsProvider();
		boolean result = cp.supportsPassword();
		return result;
	}

	public final boolean supportsUserId() {
		ICredentialsProvider cp = getCredentialsProvider();
		boolean result = cp.supportsUserId();
		return result;
	}
		
}