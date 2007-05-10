/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
	 * @param initialWorkingDirectory
	 * @param environment Array of environment variable Strings of the form "var=text"
	 * @param monitor
	 * @return the shell object
	 */
	public IHostShell launchShell(String initialWorkingDirectory, String[] environment, IProgressMonitor monitor);
	
	/**
	 * Launch a new shell in the specified directory
	 * @param initialWorkingDirectory
	 * @param encoding
	 * @param environment Array of environment variable Strings of the form "var=text"
	 * @param monitor
	 * @return the shell object
	 */
	public IHostShell launchShell(String initialWorkingDirectory, String encoding, String[] environment, IProgressMonitor monitor);

	/**
	 * Run a command in it's own shell
	 * @param initialWorkingDirectory
	 * @param command
	 * @param environment Array of environment variable Strings of the form "var=text"
	 * @param monitor
	 * @return the shell object for getting output and error streams
	 */
	public IHostShell runCommand(String initialWorkingDirectory, String command, String[] environment, IProgressMonitor monitor);
	
	/**
	 * Run a command in it's own shell
	 * @param initialWorkingDirectory
	 * @param command
	 * @param encoding
	 * @param environment Array of environment variable Strings of the form "var=text"
	 * @param monitor
	 * @return the shell object for getting output and error streams
	 */
	public IHostShell runCommand(String initialWorkingDirectory, String command, String encoding, String[] environment, IProgressMonitor monitor);

	/**
	 * Return an array of environment variables that describe the environment on the host.
	 * Each String returned is of the format "var=text": Everything up to the
	 * first equals sign is the name of the given environment variable, everything
	 * after the equals sign is its contents.
	 * @return Array of environment variable Strings of the form "var=text"
	 */
	public String[] getHostEnvironment();
}