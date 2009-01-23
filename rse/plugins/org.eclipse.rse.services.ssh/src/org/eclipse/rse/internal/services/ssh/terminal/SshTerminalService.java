/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Anna Dushistova  (MontaVista) - [261478] Remove SshShellService, SshHostShell (or deprecate and schedule for removal in 3.2)
 *******************************************************************************/

package org.eclipse.rse.internal.services.ssh.terminal;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.internal.services.ssh.ISshService;
import org.eclipse.rse.internal.services.ssh.ISshSessionProvider;
import org.eclipse.rse.internal.services.ssh.SshServiceResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.terminals.AbstractTerminalService;
import org.eclipse.rse.services.terminals.ITerminalShell;

/**
 * A Terminal Service for ssh.
 */
public class SshTerminalService extends AbstractTerminalService implements ISshService {

	private final ISshSessionProvider fSessionProvider;

    public SshTerminalService(ISshSessionProvider sessionProvider) {
    	fSessionProvider = sessionProvider;
    }

    public ISshSessionProvider getSessionProvider() {
		return fSessionProvider;
	}

    public ITerminalShell launchTerminal(String ptyType, String encoding, String[] environment, String initialWorkingDirectory, String commandToRun,
			IProgressMonitor monitor) throws SystemMessageException {
		SshTerminalShell hostShell = new SshTerminalShell(getSessionProvider(), ptyType, encoding, environment, initialWorkingDirectory, commandToRun);
        return hostShell;
    }

    public String getName() {
        return SshServiceResources.SshTerminalService_Name;
    }

    public String getDescription() {
        return SshServiceResources.SshTerminalService_Description;
    }

}
