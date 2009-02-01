/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Anna Dushistova  (MontaVista) - adapted from SshTerminalService
 * Anna Dushistova  (MontaVista) - [240523] [rseterminals] Provide a generic adapter factory that adapts any ITerminalService to an IShellService
 *******************************************************************************/

package org.eclipse.rse.internal.services.telnet.terminal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.internal.services.telnet.ITelnetService;
import org.eclipse.rse.internal.services.telnet.ITelnetSessionProvider;
import org.eclipse.rse.internal.services.telnet.TelnetServiceResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.terminals.AbstractTerminalService;
import org.eclipse.rse.services.terminals.ITerminalShell;

/**
 * A Terminal Service for telnet.
 * 
 * @since 2.0
 */
public class TelnetTerminalService extends AbstractTerminalService implements ITelnetService {

	private final ITelnetSessionProvider fSessionProvider;

    public TelnetTerminalService(ITelnetSessionProvider sessionProvider) {
    	fSessionProvider = sessionProvider;
    }

    public ITelnetSessionProvider getSessionProvider() {
		return fSessionProvider;
	}

    public ITerminalShell launchTerminal(String ptyType, String encoding, String[] environment, String initialWorkingDirectory, String commandToRun,
			IProgressMonitor monitor) throws SystemMessageException {
		TelnetTerminalShell hostShell = new TelnetTerminalShell(fSessionProvider, ptyType, encoding, environment, initialWorkingDirectory, commandToRun);
        return hostShell;
    }

	public String getName() {
        return TelnetServiceResources.TelnetShellService_Name;
    }

    public String getDescription() {
        return TelnetServiceResources.TelnetShellService_Description;
    }

}
