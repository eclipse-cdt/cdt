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

	public boolean hasRemoteServerLauncherProperties() {
		return false;
	}

	/**
	 * <i>Fully implemented, no need to override.</i><br>
	 * @see IConnectorService#addCommunicationsListener(ICommunicationsListener)
	 */
	public void addCommunicationsListener(ICommunicationsListener listener) {
		if (!commListeners.contains(listener)) {
			commListeners.add(listener);
		}
	}

	/**
	 * <i>Fully implemented, no need to override.</i><br>
	 * @see IConnectorService#removeCommunicationsListener(ICommunicationsListener)
	 */
	public void removeCommunicationsListener(ICommunicationsListener listener) {
		commListeners.remove(listener);		
	}

	/**
	 * <i><b>Private</b> - used internally.</i><br>
	 * Returns the count of active communication listeners (i.e. excludes
	 * passive listeners.)
	 */
	protected int getCommunicationListenerCount() {
		int count = 0;
		for (int i = 0; i < commListeners.size(); i++)
		{
			if (!((ICommunicationsListener) commListeners.get(i)).isPassiveCommunicationsListener())
			{
				count++;
			}
		}
		
		return count;
	}

	/**
	 * <i><b>Private</b> - used internally.</i><br>
	 */
	protected void clearCommunicationListeners() {
		commListeners.clear();
	}

	public final IHost getHost() {
		return _host;
	}

	public final void setHost(IHost host) {
		_host = host;
	}

	/**
	 * <i><b>Private</b> - used internally.</i><br>
	 * Helper method for firing communication events
	 */
	protected void fireCommunicationsEvent(int eventType) {
		CommunicationsEvent e = new CommunicationsEvent(this, eventType);
		
		Object[] items = commListeners.toArray();
	
		for (int loop=0; loop < items.length; loop++) {		
			((ICommunicationsListener) items[loop]).communicationsStateChange(e);
		}							
		
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

	public void setPort(int port) {
		if (port != _port)
		{
			_port = port;
			setDirty(true);
		}
	}

	public int getPort() {
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
	public void registerSubSystem(ISubSystem ss) {
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

	public boolean commit() {
		return RSECorePlugin.getThePersistenceManager().commit(getHost());
	}

	/**
	 * <i>Not implemented, you should override if possible.</i><br>
	 * Return the home directory of the remote system for the current user, if available.
	 * <p>
	 * Up to each implementer to decide how to implement, and if this will be cached.
	 * <p>
	 * Returns an empty string by default, override if possible
	 */
	public String getHomeDirectory() {
		return ""; //$NON-NLS-1$
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
	 * <i>Not implemented, you should override if possible.</i><br>
	 * Return the version, release, modification of the remote system,
	 *  if connected, if applicable and if available. Else return null. It
	 *  is up to each subsystem to decide how to interpret what is returned.<br>
	 * This is used to show the VRM in the property sheet, when the subsystem is selected.
	 * <p>
	 * Up to each implementer to decide if this will be cached.
	 * <p>
	 * Returns an empty string by default, override if possible
	 */
	public String getVersionReleaseModification() {
		return ""; //$NON-NLS-1$
	}

	protected void intializeSubSystems(IProgressMonitor monitor) {
		for (int i = 0; i < _registeredSubSystems.size(); i++)
		{
			ISubSystem ss = (ISubSystem)_registeredSubSystems.get(i);
			ss.initializeSubSystem(monitor);    		
		}
	}

	protected void unintializeSubSystems(IProgressMonitor monitor) {
		for (int i = 0; i < _registeredSubSystems.size(); i++)
		{
			ISubSystem ss = (ISubSystem)_registeredSubSystems.get(i);
			ss.uninitializeSubSystem(monitor);    		
		}
	}

	/**
	 * <i>Optionally override if you add any instance variables.</i><br>
	 * The following is called whenever a system is redefined or disconnected.
	 * Each subsystem needs to be informed so it can clear out any expansions, etc.
	 * By default it does nothing. Override if you have an internal object that must be nulled out.
	 */
	public void reset() {
	}

	public void notifyDisconnection() {
		
		 // Fire comm event to signal state changed
		if (!isConnected()) fireCommunicationsEvent(CommunicationsEvent.AFTER_DISCONNECT);
	}

	public void notifyConnection() {
		if (isConnected()) fireCommunicationsEvent(CommunicationsEvent.AFTER_CONNECT);
	}

	public void notifyError() {
		fireCommunicationsEvent(CommunicationsEvent.CONNECTION_ERROR);
	
	}

	public boolean isUsingSSL() {
		return _usingSSL;
	}

	public void setIsUsingSSL(boolean flag) {
		if (_usingSSL != flag)
		{
			_usingSSL = flag;
			setDirty(true);
		}
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

	public final ISubSystem[] getSubSystems() {
		return (ISubSystem[])_registeredSubSystems.toArray(new ISubSystem[_registeredSubSystems.size()]);
	}

	/**
	 * <i>Not implemented, you should override if possible.</i><br>
	 * Return the temp directory of the remote system for the current user, if available.
	 * <p>
	 * Up to each implementer to decide how to implement, and if this will be cached.
	 * <p>
	 * Returns an empty string by default, override if possible
	 */
	public String getTempDirectory() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * This methods returns the enablement state per server launch type.
	 * If {@link RemoteServerLauncher#enableServerLaunchType(ServerLaunchType, boolean)} has not been
	 *  called for this server launch type, then it is enabled by default.
	 * @see org.eclipse.rse.core.subsystems.ServerLaunchType
	 */
	protected boolean isEnabledServerLaunchType(ISubSystem subsystem, ServerLaunchType serverLaunchType) {
		IServerLauncherProperties sl = getRemoteServerLauncherProperties();
		if (sl instanceof RemoteServerLauncher)
		{
			RemoteServerLauncher isl = (RemoteServerLauncher)sl;
			return isl.isEnabledServerLaunchType(serverLaunchType);
		}
		else
			return subsystem.getSubSystemConfiguration().supportsServerLaunchType(serverLaunchType);
	}

	/**
	 * Return the remote server launcher, which implements IServerLauncher.
	 * This is called by the default implementation of connect() and disconnect(), if 
	 * subsystem.getParentSubSystemConfiguration().supportsServerLaunchProperties returns true.
	 * <p>This returns null be default!
	 */
	public IServerLauncher getRemoteServerLauncher() {
		return null;
	}

	/**
	 * <i>You must override</i>
	 * unless subsystem.getParentSubSystemConfiguration().supportsServerLaunchProperties 
	 * returns true.
	 * <p>
	 * Attempt to connect to the remote system.<br> 
	 * If the subsystem supports server launch,
	 * the default behavior is to get the remote server launcher by
	 * {@link #getRemoteServerLauncher()}, and if {@link IServerLauncher#isLaunched()}
	 * returns false, to call {@link IServerLauncher#launch(IProgressMonitor)}. 
	 * <p>
	 * This is called, by default, from the connect(...) methods of the subsystem.
	 */
	protected abstract void internalConnect(IProgressMonitor monitor) throws Exception;

	protected abstract void internalDisconnect(IProgressMonitor monitor) throws Exception;
}