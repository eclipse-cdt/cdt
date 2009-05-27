/********************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - 168977: refactoring IConnectorService
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [185750] Remove IConnectorService.getHostType()
 * Martin Oberhuber (Wind River) - [187218] Fix error reporting for connect()
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [210474] Deny save password function missing
 * David Dykstal (IBM) - [225089][ssh][shells][api] Canceling connection leads to exception
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * A connector service provides the means of establishing a connection from a
 * subsystem (or a number of subsystems) to a target system (a host).
 * <p>
 * A connector service manages a live connection to a remote system, with
 * operations for connecting and disconnecting, and storing information
 * typically cached from a subsystem: credentials (such as a userId/password),
 * port, etc.
 * <p>
 * The SubSystem interface includes a method, getConnectorService(), which
 * returns an instance of an object that implements this interface for that
 * subsystem.
 * <p>
 * A single connector service object can be unique to a subsystem instance, but
 * it can also be shared across multiple subsystems in a single host if those
 * subsystems share a physical connection to the remote system. This sharing is
 * done using implementers of {@link IConnectorServiceManager} which are
 * returned by another getter method in SubSystem.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should subclass {@link AbstractConnectorService}
 *              instead.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IConnectorService extends IRSEModelObject {

	/**
	 * @return the primary subsystem object this connector service is associated
	 * with. This is usually the subsystem that first established this
	 * connector service.
	 */
	public ISubSystem getPrimarySubSystem();

	/**
	 * Return all the subsystems that use this connector service
	 * @return the subsystems that use this service
	 */
	public ISubSystem[] getSubSystems();

	/**
	 * Adds a subsystem to this connector service. Does nothing if the
	 * subsystem is already known to this connector service.
	 * @param ss a subsystem that is using this connector service.
	 */
	public void registerSubSystem(ISubSystem ss);

	/**
	 * Deregister the subsystem. Does nothing if the subsystem is not present.
	 * @param ss the subsystem to remove from this connector service.
	 */
	public void deregisterSubSystem(ISubSystem ss);

	/**
	 * @return true if currently connected.
	 */
	public boolean isConnected();

	/**
	 * Connect to the remote system.
	 *
	 * @param monitor a monitor for tracking the progress and canceling a
	 *            connect operation.
	 * @throws OperationCanceledException if the connect was cancelled by the
	 *             user
	 * @throws Exception if there is a failure connecting. Typically, this will
	 *             be a {@link SystemMessageException}.
	 * @since org.eclipse.rse.core 3.0 throws OperationCanceledException instead
	 *        of InterruptedException
	 */
	public void connect(IProgressMonitor monitor) throws Exception;

	/**
	 * Disconnect from the remote system.
	 *
	 * @param monitor a monitor for tracking the progress and canceling a
	 *            disconnect operation.
	 * @throws Exception an exception of the disconnect fails. Typically, this
	 *             will be a {@link SystemMessageException}.
	 */
	public void disconnect(IProgressMonitor monitor) throws Exception;

	/**
	 * Reset after some fundamental change, such as a hostname change.
	 * Clear any memory of the current connection.
	 */
	public void reset();

	/**
	 * @return the version, release, modification of the remote system,
	 * if connected, if applicable, and if available. Return null if
	 * this information is not available.
	 */
	public String getVersionReleaseModification();

	/**
	 * @return the home directory of the remote system for the current user,
	 * if available.
	 */
	public String getHomeDirectory();

	/**
	 * @return the temporary directory of the remote system for the current user,
	 * if available.
	 */
	public String getTempDirectory();

	/**
	 * Sets the host used by this connector service.
	 * @param host the host to be used for this connector service
	 */
	public void setHost(IHost host);

	/**
	 * @return the host used by this connector service.
	 */
	public IHost getHost();

	/**
	 * @return the host name for the connection associated with this
	 * connector service.
	 */
	public String getHostName();

	/**
	 * @return the port for this connector service. Usually only used for
	 * IP based connections.
	 */
	public int getPort();

	/**
	 * Set the port for this connector. Usually only used by IP based
	 * connections.
	 * @param port the IP port used by this connector service.
	 */
	public void setPort(int port);

	/**
	 * @return true if this connector service will attempt to
	 * use SSL when establishing its connection.
	 */
	public boolean isUsingSSL();

	/**
	 * @param flag true if the connector service should attempt to use SSL when
	 * establishing the connection.
	 */
	public void setIsUsingSSL(boolean flag);

	/**
	 * Reports if this connector service can use a user identifier.
	 * Typically used to indicate if a login dialog needs to be presented when connecting.
	 * @return true if and only if the connector service can use a user id.
	 */
	public boolean supportsUserId();

	/**
	 * Determines if this connector service understand the concept of a password.
	 * @return true if the connector service can use a password,
	 * false if a password is irrelevant.
	 */
	public boolean supportsPassword();

	/**
	 * @return the user id that will be used by this connector when
	 * establishing its connection.
	 */
	public String getUserId();

	/**
	 * Set the user id this connector service will use when establishing its
	 * connection.
	 * @param userId the user id string for this connector service.
	 */
	public void setUserId(String userId);

	/**
	 * Causes the user id known to the connector service, if any, to be
	 * persisted.
	 */
	public void saveUserId();

	/**
	 * Causes the persisted (default) user id known to this
	 * connector service, if any, to be forgotten.
	 */
	public void removeUserId();

	/**
	 * Sets the password used by this connector service.
	 * Can be used if the connector service acquires a password by some external
	 * means.
	 * @param matchingUserId The user id to be associated with this password.
	 * @param password the password
	 * @param persist true if the password is to be persisted for later use.
	 * @param propagate true if this password should be propagated to related connector services.
	 */
	public void setPassword(String matchingUserId, String password, boolean persist, boolean propagate);

	/**
	 * Causes the password known to this connector service, if any, to be
	 * persisted.
	 */
	public void savePassword();

	/**
	 * Causes the persisted password known to this connector service, if any, to
	 * be forgotten.
	 */
	public void removePassword();

	/**
	 * Clear password held by this service and optionally
	 * clear its persistent form.
	 * Called when user uses the property dialog to
	 * change his userId.
	 * @param persist if true, clears the persistent form of the password
	 * @param propagate true if this password should be cleared in related connector services.
	 */
	public void clearPassword(boolean persist, boolean propagate);

	/**
	 * @param persistent also check for the persistent form of the password.
	 * @return true if a password is currently known to this connector service.
	 */
	public boolean hasPassword(boolean persistent);

	/**
	 * Returns true if this system can inherit the credentials of
	 * from the other connector services in this host.
	 * @return true if it can inherit the credentials, false otherwise
	 */
	public boolean inheritsCredentials();

	/**
	 * Return true if this system can share it's credentials
	 * with other connector services in this host.
	 * @return true if it can share the credentials
	 */
	public boolean sharesCredentials();

	/**
	 * Clears the credentials held by this service.
	 * Should be called if there is a change to any part of the credentials
	 * expected by any using subsystem.
	 */
	public void clearCredentials();

	/**
	 * Acquire the credentials for this connector service. Acquisition may be
	 * temporarily suppressed by using the {@link #setSuppressed(boolean)}.
	 * <p>
	 * Implementations may retain a remembered credentials or use this to
	 * acquire the credentials using some implementation defined means.
	 * <p>
	 *
	 * @param refresh if true will force the connector service to discard any
	 *            remembered value and reacquire the credentials.
	 * @throws OperationCanceledException if acquisition of the credentials is
	 *             cancelled or is being suppressed.
	 * @since org.eclipse.rse.core 3.0 throws OperationCanceledException instead
	 *        of InterruptedException
	 */
	public void acquireCredentials(boolean refresh) throws OperationCanceledException;

	/**
	 * @return true if the acquisition of credentials is being suppressed.
	 */
	public boolean isSuppressed();

	/**
	 * Suppresses the acquisition of a credentials by the connector service.
	 * Causes {@link #acquireCredentials(boolean)} to immediately
	 * throw an InterruptedException.
	 * <p>
	 * The intent is to allow tool writers to prevent multiple
	 * attempts to acquire credentials during a set period of time.
	 * <b>It is the responsibility of the caller to set this value
	 * back to false when the tool no longer needs to suppress
	 * acquisition credentials.</b>
	 *
	 * @param suppress <code>true</code> if acquisition is to be suppressed.
	 * <code>false</code> if acquisition is to be allowed.
	 */
	public void setSuppressed(boolean suppress);

	/**
	 * Register a communications listener. These listeners will be informed
	 * of connect and disconnect events.
	 * @param listener a listener for the communications event.
	 */
	public void addCommunicationsListener(ICommunicationsListener listener);

	/**
	 * Remove a communications listener.
	 * @param listener a listener for the communications event.
	 */
	public void removeCommunicationsListener(ICommunicationsListener listener);

	/**
	 * This methods returns the enablement state of a server launch type.
	 * If {@link RemoteServerLauncher#enableServerLaunchType(ServerLaunchType, boolean)} has not been
	 * called for this server launch type, then it is enabled by default.
	 * @param subsystem the subystem for which this may be enabled.
	 * @param serverLaunchType the type to check for enabledment.
	 * @return true if the connector service supports server launching and
	 * this launch type is enabled.
	 * @see org.eclipse.rse.core.subsystems.ServerLaunchType
	 */
	public boolean isServerLaunchTypeEnabled(ISubSystem subsystem, ServerLaunchType serverLaunchType);

	/**
	 * Gets the properties associated with a remote server launcher.
	 * These may be null.
	 * This an optional object containing
	 * properties used to launch the remote server that
	 * communicates with this client.
	 * @return the properties of the server launcher
	 */
	IServerLauncherProperties getRemoteServerLauncherProperties();

	/**
	 * Set the properties for the remote server launcher
	 * This is an object containing
	 * properties used to launch a remote server that
	 * communicates with this client.
	 * @param value the new value of the '<em>Remote Server Launcher</em>' containment reference.
	 */
	void setRemoteServerLauncherProperties(IServerLauncherProperties value);

	/**
	 * @return true if the connector service has server launcher properties.
	 */
	boolean hasRemoteServerLauncherProperties();

	/**
	 * @return true if the connector service supports the concept of remote
	 * server launch properties.
	 * This will always return false {@link #supportsRemoteServerLaunching()}
	 * is false.
	 */
	boolean supportsServerLaunchProperties();

	/**
	 * @return true if the connector service supports the concept of remote
	 * server launching.
	 */
	boolean supportsRemoteServerLaunching();

	/**
	 * @return the server launcher. Will be null unless
	 * {@link #supportsRemoteServerLaunching()} is true.
	 */
	IServerLauncher getRemoteServerLauncher();

	/**
	 * Test if this connector service requires a password.
	 * @return true if this connector service supports passwords and
	 * requires a password to connect to its target system.
	 */
	boolean requiresPassword();

	/**
	 * Test if this connector service requires a user id.
	 * @return true if this connector service understands the concept of a
	 * user id and requires one to connect to its target system.
	 */
	boolean requiresUserId();

	/**
	 * Sets the attribute for this connector service instance that denies a
	 * password to be saved. If the attribute has never been set it defaults to
	 * false. If set to true, it will clear any saved passwords for this system
	 * and not allow any further passwords to be stored. This property of a
	 * system is persistent from session to session, but is not sharable.
	 *
	 * @param deny If true, forget any saved passwords and do not allow any
	 *            others to be saved. If false, allow passwords to be saved in
	 *            the keyring.
	 * @return the number of saved passwords removed by this operation. This
	 *         will always be zero if "deny" is false.
	 * @since org.eclipse.rse.core 3.0
	 */
	public int setDenyPasswordSave(boolean deny);

	/**
	 * Retrieves the value of the "DENY_PASSWORD_SAVE" property of this
	 * connector service. If the value has never been set, this will return
	 * false.
	 *
	 * @return true if password saving is denied.
	 * @since org.eclipse.rse.core 3.0
	 */
	public boolean getDenyPasswordSave();


}