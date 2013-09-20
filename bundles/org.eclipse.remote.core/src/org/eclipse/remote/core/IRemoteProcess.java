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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstraction of a process running on a remote system. Remote process are created using the {@link IRemoteProcessBuilder}
 * interface.
 */
public interface IRemoteProcess {
	/**
	 * Terminate the process
	 */
	public void destroy();

	/**
	 * Returns the exit value for the process
	 * 
	 * @return the exit value
	 */
	public int exitValue();

	/**
	 * Gets the error output stream of the process
	 * 
	 * @return the output stream connected to the standard
	 *         error of the process
	 */
	public InputStream getErrorStream();

	/**
	 * Gets an InputStream which can be used to read the standard output stream of the process
	 * 
	 * @return the input stream connected to the standard
	 *         output of the process
	 */
	public InputStream getInputStream();

	/**
	 * Gets an output stream which can be used to write to the standard input stream of the process
	 * 
	 * @return the output stream connected to the standard
	 *         input of the process
	 */
	public OutputStream getOutputStream();

	/**
	 * Wait until the process has terminated
	 * 
	 * @return the exit value of the process
	 * @throws InterruptedException
	 *             if the current thread is
	 *             interrupted by another thread while it is waiting
	 */
	public int waitFor() throws InterruptedException;

	/**
	 * Check if the remote process has completed
	 * 
	 * @return true if remote process has completed
	 */
	public boolean isCompleted();
}
