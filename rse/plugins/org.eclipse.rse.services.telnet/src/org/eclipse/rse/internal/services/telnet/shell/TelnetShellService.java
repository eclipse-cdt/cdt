/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - Adapted from LocalShellService.
 * Sheldon D'souza  (Celunite)   - Adapted from SshShellService.
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable
 * Martin Oberhuber (Wind River) - [226301][api] IShellService should throw SystemMessageException on error
 * Anna Dushistova  (MontaVista) - [246422] Possible bug in TelnetShellService.runCommand()
 *******************************************************************************/
package org.eclipse.rse.internal.services.telnet.shell;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.internal.services.telnet.ITelnetService;
import org.eclipse.rse.internal.services.telnet.ITelnetSessionProvider;
import org.eclipse.rse.internal.services.telnet.TelnetServiceResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.AbstractShellService;
import org.eclipse.rse.services.shells.IHostShell;

public class TelnetShellService extends AbstractShellService implements ITelnetService {

	private ITelnetSessionProvider fTelnetSessionProvider;

	public TelnetShellService( ITelnetSessionProvider sessionProvider) {
		this.fTelnetSessionProvider = sessionProvider;
	}

	public IHostShell launchShell(String initialWorkingDirectory,
			String encoding, String[] environment,
			IProgressMonitor monitor) throws SystemMessageException {
		TelnetHostShell hostShell = new TelnetHostShell(fTelnetSessionProvider, initialWorkingDirectory, TelnetHostShell.SHELL_INVOCATION, encoding, environment);
		return hostShell;
	}

	public IHostShell runCommand(String initialWorkingDirectory,
			String command, String encoding, String[] environment,
			IProgressMonitor monitor) throws SystemMessageException {
		TelnetHostShell hostShell = new TelnetHostShell(fTelnetSessionProvider, initialWorkingDirectory, command, encoding, environment);
		return hostShell;
	}

	public String getDescription() {
		return TelnetServiceResources.TelnetShellService_Description;
	}

	public String getName() {
		return TelnetServiceResources.TelnetShellService_Name;
	}

}
