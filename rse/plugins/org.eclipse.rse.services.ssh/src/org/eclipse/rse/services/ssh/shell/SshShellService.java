/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Adapted from LocalShellService.
 ********************************************************************************/

package org.eclipse.rse.services.ssh.shell;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.services.ssh.ISshService;
import org.eclipse.rse.services.ssh.ISshSessionProvider;
import org.eclipse.rse.services.ssh.SshServiceResources;

/**
 * A Shell Services for ssh.
 * Adapted from LocalShellService.
 */
public class SshShellService implements ISshService, IShellService {

	private static final String SHELL_INVOCATION = ">"; //$NON-NLS-1$
	private ISshSessionProvider fSessionProvider;
	
	public SshShellService(ISshSessionProvider sessionProvider) {
		fSessionProvider = sessionProvider;
	}

	//TODO abstract base class should handle default encodings
	public IHostShell launchShell(IProgressMonitor monitor,
			String initialWorkingDirectory, String[] environment) {
		String defaultEncoding = System.getProperty("file.encoding"); //$NON-NLS-1$
		return launchShell(monitor, initialWorkingDirectory, defaultEncoding, environment);
	}

	public IHostShell launchShell(IProgressMonitor monitor,
			String initialWorkingDirectory, String encoding,
			String[] environment) {
		SshHostShell hostShell = new SshHostShell(fSessionProvider, initialWorkingDirectory, SHELL_INVOCATION, encoding, environment);
		return hostShell;
	}

	//TODO abstract base class should handle default encodings
	public IHostShell runCommand(IProgressMonitor monitor,
			String initialWorkingDirectory, String command, String[] environment) {
		String defaultEncoding = System.getProperty("file.encoding"); //$NON-NLS-1$
		return runCommand(monitor, initialWorkingDirectory, command, defaultEncoding, environment);
	}

	//TODO command is ignored by SshHostShell for now (just like DStoreHostShell).
	public IHostShell runCommand(IProgressMonitor monitor,
			String initialWorkingDirectory, String command, String encoding,
			String[] environment) {
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

	public void initService(IProgressMonitor monitor) {
		// nothing to do
	}

	public void uninitService(IProgressMonitor monitor) {
		// nothing to do
	}

	public SystemMessage getMessage(String messageID) {
		return null;
	}

}
