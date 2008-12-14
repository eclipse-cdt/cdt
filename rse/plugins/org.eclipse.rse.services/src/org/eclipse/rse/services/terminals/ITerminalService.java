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

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * Interface for getting Terminal Connections from a remote side, also known as
 * terminal session with Streams.
 * 
 * One ITerminalService instance is typically associated with one particular
 * connection to a (potentially remote) system, such that the ITerminalService
 * instance can also hold state data about that session, such as connected
 * state, login and authentication information, configuration data or
 * environment variables.
 * 
 * Each
 * {@link #launchTerminal(String, String, String[], String, String, IProgressMonitor)}
 * invocation, however, acts as a factory method such that it creates a new
 * (remote) process and associated {@link ITerminalShell} connection.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients must subclass the {@link AbstractTerminalService} class
 *              rather than implementing this interface directly.
 * 
 * @since org.eclipse.rse.services 3.1
 * 
 */
public interface ITerminalService extends IService {

    /**
	 * Launch a new terminal connection, also known as shell session with
	 * Streams.
	 *
	 * @param ptyType requested terminal type for the new Terminal. Since not
	 *            all Terminal implementations support specifying the Terminal
	 *            Type, there is no guarantee that a particular setting has any
	 *            effect. Use <code>null</code> to fall back to a default
	 *            terminal type.
	 * @param encoding Stream encoding to use for sending initial working
	 *            directory and initial commandToRun, and to return on
	 *            {@link ITerminalShell#getDefaultEncoding()} request. Use
	 *            <code>null</code> to fall back to a default encoding. The
	 *            Terminal Service will make efforts to determine a proper
	 *            default encoding on the remote side but this is not guaranteed
	 *            to be correct.
	 * @param environment Array of environment variable Strings of the form
	 *            "var=text". Since not all terminal implementations support the
	 *            passing of environment variables, there is no guarantee that
	 *            the created shell will actually have the specified environment
	 *            set. Passing <code>null</code> is allowed in order to
	 *            specify that no specific environment needs to be passed.
	 * @param initialWorkingDirectory initial working directory or empty String
	 *            ("") if not relevant. The remote shell will launch in a
	 *            directory of its own choice in that case (typically a user's
	 *            home directory).
	 * @param commandToRun initial command to send to the remote side.
	 * @param monitor Progress Monitor for monitoring and cancellation during
	 *            connection creation.
	 * @return the terminal connection object. Note that the connection may not
	 *         actually be usable in case the remote side allows opening a
	 *         channel but immediately closes it again. In this case,
	 *         {@link ITerminalShell#getInputStream()} will throw an
	 *         exception or be closed from the very beginning.
	 * @throws SystemMessageException in case an error occurred or the user
	 *             chose to cancel the operation via the progress monitor.
	 */
    public ITerminalShell launchTerminal(String ptyType, String encoding, String[] environment, String initialWorkingDirectory, String commandToRun,
			IProgressMonitor monitor) throws SystemMessageException;

}
