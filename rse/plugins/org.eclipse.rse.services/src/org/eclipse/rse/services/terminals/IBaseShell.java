/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Anna Dushistova  (MontaVista) - [258631][api] ITerminalService should be public API
 *******************************************************************************/

package org.eclipse.rse.services.terminals;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.rse.internal.services.terminals.ProcessBaseShell;

/**
 * A basic shell, representing the connection to some process that may be
 * running local or remote. Clients may implement this interface.
 *
 * Clients implementing this interface are encouraged to extend
 * {@link PlatformObject} for providing the {@link #getAdapter(Class)}
 * functionality.
 *
 * A simple implementation of IBaseShell is the {@link ProcessBaseShell}, which
 * wraps a Java {@link java.lang.Process} object in the IBaseShell interface to
 * provide more convenient access to it through the {{@link #isActive()} and {{@link #waitFor(long)}
 * methods, as well as making it adaptable.
 *
 * The resulting IBaseShell can be decorated by clients with additional
 * functionality easily by instantiating their subclassed variant of
 * {@link BaseShellDecorator}.
 *
 * @see java.lang.Process
 * @see org.eclipse.rse.internal.services.terminals.ProcessBaseShell
 * @see BaseShellDecorator
 * @since org.eclipse.rse.services 3.1
 */
public interface IBaseShell extends IAdaptable {

	/**
	 * Get a local-to-remote OutputStream connected to the standard input of the
	 * underlying Process.
	 *
	 * Clients must not close the obtained OutputStream themselves, since the
	 * behavior that this may have on the underlying shell or process is
	 * undefined. Use {#exit()} instead to terminate the shell if that is
	 * desired, it will close all relevant Streams.
	 *
	 * @return an OutputStream for talking to the underlying process.
	 */
	public OutputStream getOutputStream();

	/**
	 * Get a remote-to-local InputStream connected to the standard output of the
	 * underlying Process.
	 *
	 * Clients must not close the obtained InputStream themselves, since the
	 * behavior that this may have on the underlying shell or process is
	 * undefined. Use {#exit()} instead to terminate the shell if that is
	 * desired, it will close all relevant Streams.
	 *
	 * @return an InputStream for reading from the underlying process.
	 */
	public InputStream getInputStream();

	/**
	 * Get a remote-to-local InputStream connected to the standard error output
	 * of the underlying Process.
	 * 
	 * Implementations may return <code>null</code> if they do not support
	 * separate Streams for output and error.
	 * 
	 * Clients must not close the obtained InputStream themselves, since the
	 * behavior that this may have on the underlying shell or process is
	 * undefined. Use {#exit()} instead to terminate the shell if that is
	 * desired, it will close all relevant Streams.
	 * 
	 * @return an InputStream for reading error output from the underlying
	 *         process, or <code>null</code> if separate output and error
	 *         streams are not supported. Error output will be merged with the
	 *         Stream obtained from {@link #getInputStream()} in that case.
	 */
	public InputStream getErrorStream();

	/**
	 * Test whether this connection is active.
	 *
	 * @return <code>true</code> if the connection is active, i.e. the Process
	 *         underlying this connection is running, and the Streams connected
	 *         to it are not closed.
	 * @see #exitValue()
	 */
	public boolean isActive();

	/**
	 * Exit this shell.
	 *
	 * Implementations are encouraged to try terminating the underlying process
	 * in a clean way, if they can. Depending on the implementation, this may be
	 * possible or not. What's guaranteed to happen is that the Streams
	 * connected with the process are closed so the shell will not be active any
	 * more.
	 *
	 * Execution of this method may run asynchronously in the sense that the
	 * method performs everything to initiate terminating the shell but then
	 * returns immediately. Clients may use {@link #waitFor(long)} after calling
	 * this method to know when the shell is really terminated. At any rate, the
	 * exit() method returns quickly and guarantees that the shell will be
	 * terminated as soon as possible, after any required (and possible) cleanup
	 * is finished.
	 *
	 * @see java.lang.Process#destroy()
	 */
	public void exit();

	/**
	 * Return the exit value of the Process connected by this shell.
	 *
	 * Depending on the underlying implementation, this call may not be
	 * supported. Implementations which do not support this must throw an
	 * IllegalThreadStateException when the shell is still active, or return 0
	 * the shell has terminated.
	 *
	 * @return the exit value of the Process connected by this shell, provided
	 *         that it has already terminated. By convention, the exit value 0
	 *         indicates successful completion or the fact that transmission of
	 *         exit values is not supported by an implementation.
	 * @exception IllegalThreadStateException when the shell is still active.
	 * @see java.lang.Process#exitValue()
	 */
	public int exitValue();

	/**
	 * Block the calling Thread until this shell is no longer active, or the
	 * specified timeout has elapsed. If the underlying shell has already
	 * terminated, this method returns immediately.
	 *
	 * When this method returns <code>false</code>, the shell is no longer
	 * active, so an {@link #exitValue()} may be obtained.
	 *
	 * @param timeout the maximum time (in milliseconds) to wait.
	 *            Implementations may return sooner even if the underlying
	 *            Process has not yet terminated, so clients always need to keep
	 *            track of time themselves and need to check the return value. A
	 *            timeout value of zero causes this method to not limit the wait
	 *            time. Negative wait time has undefined behavior.
	 * @return <code>true</code> if the Shell is still active after waiting
	 *         (e.g. because the timeout has elapsed); <code>false</code> if
	 *         the shell is no longer active.
	 * @throws InterruptedException if the waiting Thread has been interrupted,
	 *             e.g. because the main application is shutting down.
	 * @see #isActive()
	 */
	public boolean waitFor(long timeout) throws InterruptedException;
}
