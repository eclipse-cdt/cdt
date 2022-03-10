/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a process running on a remote system. Remote process are created using the {@link IRemoteProcessBuilder} interface.
 */
public interface IRemoteProcess {
	/**
	 * The interface that is extend by services provided for this remote connection.
	 *
	 * @since 2.0
	 */
	interface Service {
		IRemoteProcess getRemoteProcess();

		interface Factory {
			<T extends Service> T getService(IRemoteProcess remoteProcess, Class<T> service);
		}
	}

	/**
	 * Terminate the process
	 */
	void destroy();

	/**
	 * Returns the exit value for the process
	 *
	 * @return the exit value
	 */
	int exitValue();

	/**
	 * Gets the error output stream of the process
	 *
	 * Note: some implementations (e.g. JSch) will not work correctly if the remote process generates stdout or stderr but the
	 * calling thread does not read the corresponding output or error streams.
	 *
	 * @return the output stream connected to the standard
	 *         error of the process
	 */
	InputStream getErrorStream();

	/**
	 * Gets an InputStream which can be used to read the standard output stream of the process
	 *
	 * Note: some implementations (e.g. JSch) will not work correctly if the remote process generates stdout or stderr but the
	 * calling thread does not read the corresponding input or error streams.
	 *
	 * @return the input stream connected to the standard
	 *         output of the process
	 */
	InputStream getInputStream();

	/**
	 * Gets an output stream which can be used to write to the standard input stream of the process
	 *
	 * @return the output stream connected to the standard
	 *         input of the process
	 */
	OutputStream getOutputStream();

	/**
	 * Get the service for this remote process that implements the given interface.
	 *
	 * @param service
	 *            the interface the required service must implements
	 * @return the desired service or null if there is no such service available
	 * @since 2.0
	 */
	<T extends Service> T getService(Class<T> service);

	/**
	 * Does this remote process support the given service.
	 *
	 * @param service
	 *            The service to be tested
	 * @return true if this connection supports the service
	 * @since 2.0
	 */
	<T extends Service> boolean hasService(Class<T> service);

	/**
	 * Wait until the process has terminated
	 *
	 * Note: some implementations (e.g. JSch) will not work correctly if the remote process generates stdout or stderr but the
	 * calling thread does not read the corresponding input or error streams.
	 *
	 * @return the exit value of the process
	 * @throws InterruptedException
	 *             if the current thread is
	 *             interrupted by another thread while it is waiting
	 */
	int waitFor() throws InterruptedException;

	/**
	 * Check if the remote process has completed.
	 *
	 * Note: some implementations (e.g. JSch) will not work correctly if the remote process generates stdout or stderr but the
	 * calling thread does not read the corresponding input or error streams.
	 *
	 * @return true if remote process has completed
	 */
	boolean isCompleted();

	/**
	 * Get the connection that is used by this process
	 *
	 * @return connection used by this process
	 * @since 2.0
	 */
	IRemoteConnection getRemoteConnection();

	/**
	 * Get the process builder used to create this process
	 *
	 * @return process builder used to create this process
	 * @since 2.0
	 */
	IRemoteProcessBuilder getProcessBuilder();
}
