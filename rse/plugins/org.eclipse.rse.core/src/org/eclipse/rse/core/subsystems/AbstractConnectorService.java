/********************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [185750] Remove IConnectorService.getHostType()
 * David Dykstal (IBM) - [189483] fix spelling in initialize/uninitialize method signatures
 * David Dykstal (IBM) - [210474] Deny save password function missing
 * David McKnight (IBM) - [249222] [api] Access to communication listeners in AbstractConnectorService
 * David McKnight   (IBM)        - [272882] [api] Handle exceptions in IService.initService()
 * David Dykstal (IBM) - [321766] safely serialize connect and disconnect operations
 * David McKnight (IBM) - [415088] potential dual connect on RSE restore when user expands filter
 ********************************************************************************/
package org.eclipse.rse.core.subsystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.services.Mutex;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * This is a base class to make it easier to create connector services.
 * <p>
 * An {@link org.eclipse.rse.core.subsystems.IConnectorService} object
 * is returned from a subsystem object via getConnectorService(), and
 * it is used to maintain the connection to a particular set of subsystems.
 * <p>
 * This class implements the protocol for much of the
 * standard bookkeeping for connector services including
 * server launchers (if none are required), event handling,
 * hosts, ports, addresses, descriptions, and registered subsystems.
 * Subclasses must concern themselves with actually authenticating and connecting.
 */
public abstract class AbstractConnectorService extends RSEModelObject implements IConnectorService {

	private Vector commListeners = new Vector(5);
	private ISubSystem _primarySubSystem = null;
	private List _registeredSubSystems = new ArrayList();
	private IHost _host;
	private String _description;
	private String _name;
	private int _port;
	private boolean _usingSSL;

	/**
	 * Construct a new connector service. This should be called during the construction
	 * of any subclasses.
	 * @param name The name of the connector service.
	 * @param description A description of the connector service.
	 * @param host The host associated with this connector service. A host may have multiple
	 * connector services.
	 * @param port The port associated with this connector service if this connector service
	 * is IP based. If not IP based this can be used for some other purpose.
	 */
	public AbstractConnectorService(String name, String description, IHost host, int port) {
		_name = name;
		_description = description;
		_host = host;
		_port = port;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isServerLaunchTypeEnabled(org.eclipse.rse.core.subsystems.ISubSystem, org.eclipse.rse.core.subsystems.ServerLaunchType)
	 */
	public final boolean isServerLaunchTypeEnabled(ISubSystem subsystem, ServerLaunchType serverLaunchType) {
		IServerLauncher sl = getRemoteServerLauncher();
		if (sl instanceof RemoteServerLauncher) {
			RemoteServerLauncher isl = (RemoteServerLauncher) sl;
			return isl.isEnabledServerLaunchType(serverLaunchType);
		} else
			return subsystem.getSubSystemConfiguration().supportsServerLaunchType(serverLaunchType);
	}

	/**
	 * @return null, may be overriden
	 * @see IConnectorService#getRemoteServerLauncher()
	 */
	public IServerLauncher getRemoteServerLauncher() {
		return null;
	}

	/**
	 * @return false, may be overridden
	 * @see IConnectorService#supportsRemoteServerLaunching()
	 */
	public boolean supportsRemoteServerLaunching() {
		return false;
	}

	/**
	 * @return false, may be overridden
	 * @see IConnectorService#supportsServerLaunchProperties()
	 */
	public boolean supportsServerLaunchProperties() {
		return false;
	}

	/**
	 * @return null, may be overridden
	 * @see IConnectorService#getRemoteServerLauncherProperties()
	 */
	public IServerLauncherProperties getRemoteServerLauncherProperties() {
		return null;
	}

	/**
	 * Do nothing, may be overridden
	 * @param newRemoteServerLauncher the server launcher properties
	 * @see IConnectorService#setRemoteServerLauncherProperties(IServerLauncherProperties)
	 */
	public void setRemoteServerLauncherProperties(IServerLauncherProperties newRemoteServerLauncher) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#hasRemoteServerLauncherProperties()
	 */
	public final boolean hasRemoteServerLauncherProperties() {
		return getRemoteServerLauncherProperties() != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#addCommunicationsListener(org.eclipse.rse.core.subsystems.ICommunicationsListener)
	 */
	public final void addCommunicationsListener(ICommunicationsListener listener) {
		if (!commListeners.contains(listener)) {
			commListeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#removeCommunicationsListener(org.eclipse.rse.core.subsystems.ICommunicationsListener)
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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getHost()
	 */
	public final IHost getHost() {
		return _host;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setHost(org.eclipse.rse.core.model.IHost)
	 */
	public final void setHost(IHost host) {
		_host = host;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.RSEModelObject#getDescription()
	 */
	public final String getDescription() {
		return _description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEModelObject#getName()
	 */
	public final String getName() {
		return _name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setPort(int)
	 */
	public final void setPort(int port) {
		if (port != _port)
		{
			_port = port;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getPort()
	 */
	public final int getPort() {
		return _port;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getPrimarySubSystem()
	 */
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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#registerSubSystem(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public final void registerSubSystem(ISubSystem ss) {
		if (!_registeredSubSystems.contains(ss))
		{
			_registeredSubSystems.add(ss);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#deregisterSubSystem(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public final void deregisterSubSystem(ISubSystem ss) {
		_registeredSubSystems.remove(ss);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#commit()
	 */
	public final boolean commit() {
		return getHost().commit();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#getPersistableParent()
	 */
	public final IRSEPersistableContainer getPersistableParent() {
		return _host;
	}

	public IRSEPersistableContainer[] getPersistableChildren() {
		List children = new ArrayList(20);
		IServerLauncherProperties launcherProperties = getRemoteServerLauncherProperties();
		if (launcherProperties != null) {
			children.add(getRemoteServerLauncherProperties());
		}
		children.addAll(_registeredSubSystems);
		children.addAll(Arrays.asList(getPropertySets()));
		IRSEPersistableContainer[] result = new IRSEPersistableContainer[children.size()];
		children.toArray(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getHostName()
	 */
	public final String getHostName() {
		return getHost().getHostName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getVersionReleaseModification()
	 */
	public String getVersionReleaseModification() {
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getSubSystems()
	 */
	public final ISubSystem[] getSubSystems() {
		return (ISubSystem[])_registeredSubSystems.toArray(new ISubSystem[_registeredSubSystems.size()]);
	}

	/**
	 * Initialize any subsystems just after connecting to the host.
	 * @param monitor a progress monitor to report progress of initialization.
	 */
	protected final void initializeSubSystems(IProgressMonitor monitor) throws SystemMessageException {
		for (int i = 0; i < _registeredSubSystems.size(); i++)
		{
			ISubSystem ss = (ISubSystem)_registeredSubSystems.get(i);
			ss.initializeSubSystem(monitor);
		}
	}

	/**
	 * Uninitialize any subsystem just after disconnecting from the host.
	 * @param monitor a progress monitor used to track uninitialization progress.
	 */
	protected final void uninitializeSubSystems(IProgressMonitor monitor) {
		for (int i = 0; i < _registeredSubSystems.size(); i++)
		{
			ISubSystem ss = (ISubSystem)_registeredSubSystems.get(i);
			ss.uninitializeSubSystem(monitor);
		}
	}

	/**
	 * Send the event to notify listeners of a disconnection.
	 * Used by the framework and should
	 * usually not be invoked by concrete subclasses.
	 */
	protected final void notifyDisconnection() {
		 // Fire comm event to signal state changed
		if (!isConnected()) fireCommunicationsEvent(CommunicationsEvent.AFTER_DISCONNECT);
	}

	/**
	 * Send the event to notify listeners of a connection.
	 * Used by the framework and should
	 * usually not be invoked by concrete subclasses.
	 */
	protected final void notifyConnection() {
		if (isConnected()) fireCommunicationsEvent(CommunicationsEvent.AFTER_CONNECT);
	}

	/**
	 * Send the event to notify listeners of a connection establishment error.
	 * Used by the framework and should
	 * usually not be invoked by concrete subclasses.
	 */
	protected final void notifyError() {
		fireCommunicationsEvent(CommunicationsEvent.CONNECTION_ERROR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isUsingSSL()
	 */
	public final boolean isUsingSSL() {
		return _usingSSL;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setIsUsingSSL(boolean)
	 */
	public final void setIsUsingSSL(boolean flag) {
		if (_usingSSL != flag)
		{
			_usingSSL = flag;
			setDirty(true);
		}
	}

	/**
	 * Returns the temp directory of the remote system for the current user,
	 * if available. This implementation returns the empty string.
	 * @return an empty string
	 * @see IConnectorService#getTempDirectory()
	 */
	public String getTempDirectory() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the home directory of the remote system for the current user,
	 * if available. This implementation returns the empty string.
	 * @return an empty string
	 * @see IConnectorService#getHomeDirectory()
	 */
	public String getHomeDirectory() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Reset the connector service state if a connector service is redefined
	 * or disconnected.
	 * Each subsystem needs to be informed so it can clear out any expansions.
	 * This implementation does nothing.
	 * Implementations should override and call {@link #reset()}
	 * if there is internal state to reset.
	 * @see IConnectorService#reset()
	 */
	public void reset() {
	}

	/**
	 * This implementation returns the connector service's port property.
	 * Override if appropriate.
	 * <br> This is called by the default implementation of
	 * {@link #connect(IProgressMonitor)},
	 * if #supportsServerLaunchProperties() is true.
	 * @return the port used for connecting to the target
	 */
	protected int getConnectPort() {
		return getPort();
	}

	/**
	 * A SafeRunner makes sure that instances of UnsafeRunnableWithProgress will run one
	 * at a time. A timeout value is specified. If the runnable cannot be started within 
	 * the timeout value it is not run and a TimeoutException is thrown.
	 * <p>
	 * A SafeRunner keeps track of the thread that is running. If that thread 
	 * reenters the SafeRunner, then it is allowed to continue execution.
	 */
	private class SafeRunner {
		private Mutex semaphore = new Mutex();
		private Thread semaphoreOwner = null;
		/**
		 * Run a runnable. If one is already running in this runner then this one will wait up to
		 * the specified timeout period. If the timeout expires an exception is thrown.
		 * @param runnable the runnable to run
		 * @param timeout the timeout value in milliseconds
		 * @param monitor the monitor that is tracking progress
		 * @throws TimeoutException if the timeout expires before the runner becomes unblocked.
		 */
		void run(UnsafeRunnableWithProgress runnable, long timeout, IProgressMonitor monitor) throws Exception {
			if (semaphoreOwner != Thread.currentThread()) {
				if (semaphore.waitForLock(monitor, timeout)) {
					semaphoreOwner = Thread.currentThread();
					try {
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						runnable.run(monitor);
					} finally {
						semaphore.release();
						semaphoreOwner = null;
					}
				} else {
					throw new TimeoutException(timeout);
				}
			} else {
				runnable.run(monitor);
			}
		}
	}
	
	/**
	 * The TimeoutException is to be thrown when an operation experiences a time-out.
	 */
	// TODO it may be possible to replace this exception with the one in JRE 5.0 when that becomes the base
	private class TimeoutException extends Exception {
		private static final long serialVersionUID = 1L;
		private long timeoutValue;
		/**
		 * Creates a new TimeoutException with a particular value.
		 * @param timeoutValue The value of the timeout that expired in milliseconds.
		 */
		TimeoutException(long timeoutValue) {
			this.timeoutValue = timeoutValue;
		}
		/**
		 * @return The value of the timeout in milliseconds.
		 */
		long getTimeout() {
			return timeoutValue;
		}
	}
	
	/**
	 * This interface is used to describe operations that require a progress
	 * monitor and may throw arbitrary exceptions during their execution.
	 * It is meant to be a companion to the SafeRunner class which should 
	 * serialize these and handle the exceptions appropriately.
	 */
	private interface UnsafeRunnableWithProgress {
		void run(IProgressMonitor monitor) throws Exception;
	}
	
	private final SafeRunner safeRunner = new SafeRunner();

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#connect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void connect(IProgressMonitor monitor) throws Exception {
		long timeout = 120000; // two minute timeout, this is arbitrary but seems to be a good amount
		UnsafeRunnableWithProgress runnable = new UnsafeRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws Exception {
				if (!isConnected()){
					preConnect();
					internalConnect(monitor);
					initializeSubSystems(monitor);
					postConnect();
				}
			}
		};
		try {
			safeRunner.run(runnable, timeout, monitor);
		} catch (TimeoutException e) {
			String id = RSECorePlugin.getDefault().getBundle().getSymbolicName();
			String messageTemplate = "Connect operation timed out after {0} milliseconds. Operation canceled."; //TODO externalize this message in 3.3
			String message = NLS.bind(messageTemplate, new Long(e.getTimeout()));
			IStatus status = new Status(IStatus.INFO, id, message);
			RSECorePlugin.getDefault().getLog().log(status);
			throw new OperationCanceledException();
		}
	}

	/**
	 * Disconnects from the target system.
	 * Calls {@link #internalDisconnect(IProgressMonitor)}
	 * and {@link #postDisconnect()}
	 * @throws Exception if the disconnect fails
	 */
	public final void disconnect(IProgressMonitor monitor) throws Exception {
		long timeout = 120000; // two minute timeout
		UnsafeRunnableWithProgress runnable = new UnsafeRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws Exception {
				preDisconnect();
				internalDisconnect(monitor);
				uninitializeSubSystems(monitor);
				postDisconnect();
			}
		};
		try {
			safeRunner.run(runnable, timeout, monitor);
		} catch (TimeoutException e) {
			String id = RSECorePlugin.getDefault().getBundle().getSymbolicName();
			String messageTemplate = "Disconnect operation timed out after {0} milliseconds."; //TODO externalize this message in 3.3
			String message = NLS.bind(messageTemplate, new Long(e.getTimeout()));
			IStatus status = new Status(IStatus.INFO, id, message);
			RSECorePlugin.getDefault().getLog().log(status);
		}
	}

	/**
	 * Performs the actual connection to the target system.
	 * @param monitor for cancellation and progress reporting
	 * @throws Exception if connection does not succeed
	 */
	protected abstract void internalConnect(IProgressMonitor monitor) throws Exception;

	/**
	 * Performs the actual disconnection from the target system.
	 * @param monitor for cancellation and progress reporting
	 * @throws Exception if disconnection does not succeed
	 */
	protected abstract void internalDisconnect(IProgressMonitor monitor) throws Exception;

	/**
	 * Performs any cleanup required after disconnecting.
	 * This implementation does nothing.
	 * May be overridden.
	 * If overridden, invoke via super.
	 */
	protected void postDisconnect() {
	}

	/**
	 * Performs any tasks required immediately prior to disconnecting.
	 * This implementation does nothing.
	 * May be overridden.
	 * If overridden, invoke via super.
	 */
	protected void preDisconnect() {
	}

	/**
	 * Performs any tasks required immediately prior to connecting.
	 * This implementation does nothing.
	 * May be overridden.
	 * If overridden, invoke via super.
	 */
	protected void preConnect() {
	}

	/**
	 * Performs any tasks required immediately after connecting.
	 * This implementation does nothing.
	 * May be overridden.
	 * If overridden, invoke via super.
	 */
	protected void postConnect() {
	}

	/**
	 * {@inheritDoc}
	 * @see IConnectorService#setDenyPasswordSave(boolean)
	 * @since org.eclipse.rse.core 3.0
	 */
	public final int setDenyPasswordSave(boolean deny) {
		IHost host = getHost();
		String hostAddress = host.getHostName();
		IRSESystemType systemType = host.getSystemType();
		int result = RSEPreferencesManager.setDenyPasswordSave(systemType, hostAddress, deny);
		return result;
	}

	/**
	 * {@inheritDoc}
	 * @see IConnectorService#getDenyPasswordSave()
	 * @since org.eclipse.rse.core 3.0
	 */
	public final boolean getDenyPasswordSave() {
		IHost host = getHost();
		String hostAddress = host.getHostName();
		IRSESystemType systemType = host.getSystemType();
		boolean result = RSEPreferencesManager.getDenyPasswordSave(systemType, hostAddress);
		return result;
	}

	/**
	 * Check if there are any active communication listeners listening to
	 * this connector service.
	 * 
	 * @return true if there are any active communication listeners
	 * 
	 * @since 3.1
	 */
	public boolean hasActiveCommunicationListeners() {
		if (commListeners.size() > 0){
			for (int i = 0; i < commListeners.size(); i++){
				ICommunicationsListener listener = (ICommunicationsListener)commListeners.get(i);
				if (!listener.isPassiveCommunicationsListener()){
					return true;
				}
			}
		}
		return false;
	}



}