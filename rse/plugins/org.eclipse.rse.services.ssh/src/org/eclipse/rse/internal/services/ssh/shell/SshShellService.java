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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable
 *******************************************************************************/

package org.eclipse.rse.internal.services.ssh.shell;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.internal.services.ssh.ISshService;
import org.eclipse.rse.internal.services.ssh.ISshSessionProvider;
import org.eclipse.rse.internal.services.ssh.SshServiceResources;
import org.eclipse.rse.services.shells.AbstractShellService;
import org.eclipse.rse.services.shells.IHostShell;

/**
 * A Shell Services for ssh.
 * Adapted from LocalShellService.
 */
public class SshShellService extends AbstractShellService implements ISshService {

	private ISshSessionProvider fSessionProvider;

	public SshShellService(ISshSessionProvider sessionProvider) {
		fSessionProvider = sessionProvider;
	}

	public IHostShell launchShell(String initialWorkingDirectory,
			String encoding, String[] environment,
			IProgressMonitor monitor) {
		SshHostShell hostShell = new SshHostShell(fSessionProvider, initialWorkingDirectory, SshHostShell.SHELL_INVOCATION, encoding, environment);
		return hostShell;
	}

	public IHostShell runCommand(String initialWorkingDirectory,
			String command, String encoding, String[] environment,
			IProgressMonitor monitor) {
		SshHostShell hostShell = new SshHostShell(fSessionProvider, initialWorkingDirectory, command, encoding, environment);
		return hostShell;
	}

	public String[] getHostEnvironment() {
		//TODO getHostEnvironment is not yet implemented for ssh (needs running remote command and parsing)
		return new String[0];
	}

	public String getName() {
		return SshServiceResources.SshShellService_Name;
	}

	public String getDescription() {
		return SshServiceResources.SshShellService_Description;
	}

}
