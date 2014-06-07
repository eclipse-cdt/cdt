/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.exception.RemoteConnectionException;

/**
 * Abstraction of a connection to a remote system. Clients should use the set methods to provide information on the remote system,
 * then call the {{@link #open(IProgressMonitor)} method. Once the connection is completed, call the {@link #close()} method to
 * terminate the connection.
 */
public interface IRemoteConnection extends Comparable<IRemoteConnection> {
	public final static String OS_NAME_PROPERTY = "os.name"; //$NON-NLS-1$
	public final static String OS_VERSION_PROPERTY = "os.version"; //$NON-NLS-1$
	public final static String OS_ARCH_PROPERTY = "os.arch"; //$NON-NLS-1$
	/**
	 * @since 6.0
	 */
	public final static String FILE_SEPARATOR_PROPERTY = "file.separator"; //$NON-NLS-1$
	/**
	 * @since 6.0
	 */
	public final static String PATH_SEPARATOR_PROPERTY = "path.separator"; //$NON-NLS-1$
	/**
	 * @since 6.0
	 */
	public final static String LINE_SEPARATOR_PROPERTY = "line.separator"; //$NON-NLS-1$
	/**
	 * @since 4.0
	 */
	public final static String USER_HOME_PROPERTY = "user.home"; //$NON-NLS-1$

	/**
	 * Register a listener that will be notified when this connection's status changes.
	 * 
	 * @param listener
	 */
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener);

	/**
	 * Close the connection. Must be called to terminate the connection.
	 */
	public void close();

	/**
	 * Notify all listeners when this connection's status changes. See {{@link IRemoteConnectionChangeEvent} for a list of event
	 * types.
	 * 
	 * @param event
	 *            event type indicating the nature of the event
	 */
	public void fireConnectionChangeEvent(int type);

	/**
	 * Forward local port localPort to remote port fwdPort on remote machine fwdAddress. If this IRemoteConnection is not to
	 * fwdAddress, the port will be routed via the connection machine to fwdAddress.
	 * 
	 * @param localPort
	 *            local port to forward
	 * @param fwdAddress
	 *            address of remote machine
	 * @param fwdPort
	 *            remote port on remote machine
	 * @throws RemoteConnectionException
	 */
	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort) throws RemoteConnectionException;

	/**
	 * Forward a local port to remote port fwdPort on remote machine fwdAddress. The local port is chosen dynamically and returned
	 * by the method. If this IRemoteConnection is not to fwdAddress, the port will be routed via the connection machine to
	 * fwdAddress.
	 * 
	 * @param fwdAddress
	 * @param fwdPort
	 * @param monitor
	 * @return local port number
	 * @throws RemoteConnectionException
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException;

	/**
	 * Forward remote port remotePort to port fwdPort on machine fwdAddress. When a connection is made to remotePort on the remote
	 * machine, it is forwarded via this IRemoteConnection to fwdPort on machine fwdAddress.
	 * 
	 * @param remotePort
	 *            remote port to forward
	 * @param fwdAddress
	 *            address of recipient machine
	 * @param fwdPort
	 *            port on recipient machine
	 * @throws RemoteConnectionException
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort) throws RemoteConnectionException;

	/**
	 * Forward a remote port to port fwdPort on remote machine fwdAddress. The remote port is chosen dynamically and returned by the
	 * method. When a connection is made to this port on the remote machine, it is forwarded via this IRemoteConnection to fwdPort
	 * on machine fwdAddress.
	 * 
	 * If fwdAddress is the empty string ("") then the fwdPort will be bound to any address on all interfaces. Note that this
	 * requires enabling the GatewayPort sshd option on some systems.
	 * 
	 * @param fwdAddress
	 * @param fwdPort
	 * @param monitor
	 * @return remote port number
	 * @throws RemoteConnectionException
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException;

	/**
	 * Gets the implementation dependent address for this connection
	 * 
	 * return address
	 */
	public String getAddress();

	/**
	 * Get the implementation specific attributes for the connection.
	 * 
	 * NOTE: the attributes do not include any security related information (e.g. passwords, keys, etc.)
	 * 
	 * @return a map containing the connection attribute keys and values
	 */
	public Map<String, String> getAttributes();

	/**
	 * Get a remote process that runs a command shell on the remote system. The shell will be the user's default shell on the remote
	 * system. The flags may be used to modify behavior of the remote process. These flags may only be supported by specific types
	 * of remote service providers. Clients can use {@link IRemoteProcessBuilder#getSupportedFlags()} to find out the flags
	 * supported by the service provider.
	 * 
	 * <pre>
	 * Current flags are:
	 *   {@link IRemoteProcessBuilder#NONE}			- disable any flags
	 *   {@link IRemoteProcessBuilder#ALLOCATE_PTY}	- allocate a pseudo-terminal for the process (RFC-4254 Sec. 6.2)
	 *   {@link IRemoteProcessBuilder#FORWARD_X11}	- enable X11 forwarding (RFC-4254 Sec. 6.3)
	 * </pre>
	 * 
	 * @param flags
	 *            bitwise-or of flags
	 * @return remote process object
	 * @throws IOException
	 * @since 7.0
	 */
	public IRemoteProcess getCommandShell(int flags) throws IOException;

	/**
	 * Returns an unmodifiable string map view of the remote environment. The connection must be open prior to calling this method.
	 * 
	 * @return the remote environment
	 * @since 5.0
	 */
	public Map<String, String> getEnv();

	/**
	 * Returns the value of an environment variable. The connection must be open prior to calling this method.
	 * 
	 * @param name
	 *            name of the environment variable
	 * @return value of the environment variable or null if the variable is not defined
	 */
	public String getEnv(String name);

	/**
	 * Get a file manager for managing remote files
	 * 
	 * @return file manager or null if connection is not open
	 */
	public IRemoteFileManager getFileManager();

	/**
	 * Get unique name for this connection.
	 * 
	 * @return connection name
	 */
	public String getName();

	/**
	 * Gets the port for this connection. Only valid if supported by the service provider.
	 * 
	 * return port number
	 * 
	 * @since 5.0
	 */
	public int getPort();

	/**
	 * Get a process builder for creating remote processes
	 * 
	 * @return process builder or null if connection is not open
	 */
	public IRemoteProcessBuilder getProcessBuilder(List<String> command);

	/**
	 * Get a process builder for creating remote processes
	 * 
	 * @return process builder or null if connection is not open
	 */
	public IRemoteProcessBuilder getProcessBuilder(String... command);

	/**
	 * Gets the remote system property indicated by the specified key. The connection must be open prior to calling this method.
	 * 
	 * The following keys are supported:
	 * 
	 * <pre>
	 * os.name			Operating system name 
	 * os.arch			Operating system architecture
	 * os.version		Operating system version
	 * file.separator	File separator ("/" on UNIX)
	 * path.separator	Path separator (":" on UNIX)
	 * line.separator	Line separator ("\n" on UNIX)
	 * user.home		Home directory
	 * </pre>
	 * 
	 * @param key
	 *            the name of the property
	 * @return the string value of the property, or null if no property has that key
	 */
	public String getProperty(String key);

	/**
	 * Get the remote services provider for this connection.
	 * 
	 * @return remote services provider
	 * @since 4.0
	 */
	public IRemoteServices getRemoteServices();

	/**
	 * Gets the username for this connection
	 * 
	 * return username
	 */
	public String getUsername();

	public IRemoteConnectionWorkingCopy getWorkingCopy();

	/**
	 * Get the working directory. Relative paths will be resolved using this path.
	 * 
	 * The remote connection does not need to be open to use this method, however a default directory path, rather than the actual
	 * working directory, may be returned in this case.
	 * 
	 * @return String representing the current working directory
	 * @since 4.0
	 */
	public String getWorkingDirectory();

	/**
	 * Test if the connection is open.
	 * 
	 * @return true if connection is open.
	 */
	public boolean isOpen();

	/**
	 * Open the connection. Must be called before the connection can be used.
	 * 
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the user. It is the caller's responsibility to call done()
	 *            on the given monitor. Accepts null, indicating that no progress should be reported and that the operation cannot
	 *            be cancelled.
	 * @throws RemoteConnectionException
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException;

	/**
	 * Remove a listener that will be notified when this connection's status changes.
	 * 
	 * @param listener
	 */
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener);

	/**
	 * Remove the local port forwarding associated with the given port.
	 * 
	 * @param port
	 *            forwarded port
	 * @throws RemoteConnectionException
	 * @since 7.0
	 */
	public void removeLocalPortForwarding(int port) throws RemoteConnectionException;

	/**
	 * Remove the remote port forwarding associated with the given port.
	 * 
	 * @param port
	 *            forwarded port
	 * @throws RemoteConnectionException
	 * @since 7.0
	 */
	public void removeRemotePortForwarding(int port) throws RemoteConnectionException;

	/**
	 * Set the working directory while the connection is open. The working directory will revert to the default when the connection
	 * is closed then subsequently reopened.
	 * 
	 * Relative paths will be resolved using this path. The path must be valid and absolute for any changes to be made.
	 * 
	 * @param path
	 *            String representing the current working directory
	 * @since 4.0
	 */
	public void setWorkingDirectory(String path);

	/**
	 * Test if this connection supports forwarding of TCP connections
	 * 
	 * @return true if TCP port forwarding is supported
	 */
	public boolean supportsTCPPortForwarding();
}
