/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable and add Javadoc
 * Martin Oberhuber (Wind River) - [226301][api] IShellService should throw SystemMessageException on error
 ********************************************************************************/

package org.eclipse.rse.services.shells;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * IShellService is an abstraction for running shells and shell commands.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 *              Search service implementations must subclass
 *              {@link AbstractShellService} rather than implementing this
 *              interface directly.
 */
public interface IShellService extends IService
{
	/**
	 * Launch a new shell in the specified directory with a default encoding.
	 *
	 * This is a convenience method, passing <code>null</code> as encoding
	 * into {@link #launchShell(String, String, String[], IProgressMonitor)}.
	 *
	 * @throws SystemMessageException in case an error occurred or the user
	 *             chose to cancel the operation via the progress monitor.
	 */
	public IHostShell launchShell(String initialWorkingDirectory, String[] environment, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Launch a new shell in the specified directory.
	 *
	 * @param initialWorkingDirectory initial working directory or empty String
	 *            ("") if not relevant. The remote shell will launch in a
	 *            directory of its own choice in that case (typically a user's
	 *            home directory).
	 * @param encoding Stream encoding to use, or <code>null</code> to fall
	 *            back to a default encoding. The Shell Service will make
	 *            efforts to determine a proper default encoding on the remote
	 *            side but this is not guaranteed to be correct.
	 * @param environment Array of environment variable Strings of the form
	 *            "var=text". Since not all shell implementations support the
	 *            passing of environment variables, there is no guarantee that
	 *            the created shell will actually have the specified environment
	 *            set.
	 * @param monitor Progress Monitor for monitoring and cancellation
	 * @return the shell object. Note that the shell may not actually be usable
	 *         in case the remote side allows opening a channel but immediately
	 *         closes it again. In this case, {@link IHostShell#isActive()}
	 *         returns <code>false</code> on the created Shell object.
	 * @throws SystemMessageException in case an error occurred or the user
	 *             chose to cancel the operation via the progress monitor.
	 */
	public IHostShell launchShell(String initialWorkingDirectory, String encoding, String[] environment, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 *
	 * Run a single command in it's own shell with a default encoding.
	 *
	 * This is a convenience method, passing <code>null</code> as encoding
	 * into
	 * {@link #runCommand(String, String, String, String[], IProgressMonitor)}.
	 *
	 * @throws SystemMessageException in case an error occurred or the user
	 *             chose to cancel the operation via the progress monitor.
	 */
	public IHostShell runCommand(String initialWorkingDirectory, String command, String[] environment, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Run a single command in it's own shell.
	 *
	 * This method is similar to
	 * {@link #launchShell(String, String, String[], IProgressMonitor)} but
	 * immediately executes a specified command rather than just opening a
	 * shell. There is no guarantee that after the host shell will accept any
	 * subsequent commands after the initial command has been executed; there
	 * is, however, also no guarantee that the host shell will terminate the
	 * connection automatically. Clients need to call {@link IHostShell#exit()}
	 * in case the shell remains active after the initial command is completed.
	 *
	 * @param initialWorkingDirectory initial working directory or empty String
	 *            ("") if not relevant. The remote command will launch in a
	 *            directory of its own choice in that case (typically a user's
	 *            home directory).
	 * @param command initial command to send to the remote side.
	 * @param encoding Stream encoding to use, or <code>null</code> to fall
	 *            back to a default encoding. The Shell Service will make
	 *            efforts to determine a proper default encoding on the remote
	 *            side but this is not guaranteed to be correct.
	 * @param environment Array of environment variable Strings of the form
	 *            "var=text". Since not all shell implementations support the
	 *            passing of environment variables, there is no guarantee that
	 *            the created shell will actually have the specified environment
	 *            set.
	 * @param monitor Progress Monitor for monitoring and cancellation
	 * @return the shell object for getting output and error streams. Note that
	 *         the shell may not actually be usable in case an error occurred on
	 *         the remote side, such as the command not being executable. In
	 *         this case, {@link IHostShell#isActive()} returns
	 *         <code>false</code> on the created Shell object.
	 * @throws SystemMessageException in case an error occurred or the user
	 *             chose to cancel the operation via the progress monitor.
	 */
	public IHostShell runCommand(String initialWorkingDirectory, String command, String encoding, String[] environment, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return an array of environment variables that describe the environment on
	 * the remote system. Each String returned is of the format "var=text":
	 * Everything up to the first equals sign is the name of the given
	 * environment variable, everything after the equals sign is its contents.
	 *
	 * @return Array of environment variable Strings of the form "var=text" if
	 *         supported by a shell service implementation. Should return an
	 *         empty array in case environment variable retrieval is not
	 *         supported on a particular shell service implementation.
	 * @throws SystemMessageException in case an error occurred or the user
	 *             chose to cancel the operation via the progress monitor.
	 */
	public String[] getHostEnvironment() throws SystemMessageException;
}