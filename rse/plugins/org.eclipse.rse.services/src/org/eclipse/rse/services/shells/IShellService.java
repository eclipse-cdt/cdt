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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.shells;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.IService;

/**
 * IShellService is an abstraction for running shells and
 * shell commands
 *
 */
public interface IShellService extends IService
{
	/**
	 * Launch a new shell in the specified directory
	 * @param monitor
	 * @param initialWorkingDirectory
	 * @param environment Array of environment variable Strings of the form "var=text"
	 * @return the shell object
	 */
	public IHostShell launchShell(IProgressMonitor monitor, String initialWorkingDirectory, String[] environment);
	
	/**
	 * Launch a new shell in the specified directory
	 * @param monitor
	 * @param initialWorkingDirectory
	 * @param encoding
	 * @param environment Array of environment variable Strings of the form "var=text"
	 * @return the shell object
	 */
	public IHostShell launchShell(IProgressMonitor monitor, String initialWorkingDirectory, String encoding, String[] environment);

	/**
	 * Run a command in it's own shell
	 * @param monitor
	 * @param initialWorkingDirectory
	 * @param command
	 * @param environment Array of environment variable Strings of the form "var=text"
	 * @return the shell object for getting output and error streams
	 */
	public IHostShell runCommand(IProgressMonitor monitor, String initialWorkingDirectory, String command, String[] environment);
	
	/**
	 * Run a command in it's own shell
	 * @param monitor
	 * @param initialWorkingDirectory
	 * @param command
	 * @param encoding
	 * @param environment Array of environment variable Strings of the form "var=text"
	 * @return the shell object for getting output and error streams
	 */
	public IHostShell runCommand(IProgressMonitor monitor, String initialWorkingDirectory, String command, String encoding, String[] environment);

	/**
	 * Return an array of environment variables that describe the environment on the host.
	 * Each String returned is of the format "var=text": Everything up to the
	 * first equals sign is the name of the given environment variable, everything
	 * after the equals sign is its contents.
	 * @return Array of environment variable Strings of the form "var=text"
	 */
	public String[] getHostEnvironment();
}