/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.shells.core.subsystems;

//


import java.util.List;

import org.eclipse.rse.core.subsystems.IRemoteSystemEnvVar;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.swt.widgets.Shell;

/**
 * @lastgen interface RemoteCmdSubSystem extends SubSystem {}
 */

public interface IRemoteCmdSubSystem extends ISubSystem{




	/**
	 * Return parent subsystem factory, cast to a RemoteCmdSubSystemFactory
	 */
	public IRemoteCmdSubSystemConfiguration getParentRemoteCmdSubSystemFactory();

	/**
	 * Execute a remote command. This is only applicable if the subsystem factory reports
	 *  true for supportsCommands().
	 * @param command Command to be executed remotely.
	 * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
	 * @param Object context context of a command (i.e. working directory).  Null is valid and means to run the 
	 * 			command as a shell command in the default shell.
	 * @return Array of objects that are the result of running this command. Typically, these
	 *   are messages logged by the command.
     * @see org.eclipse.rse.core.subsystems.shells.ui.RemoteCommandHelpers
	 */
	public Object[] runCommand(String command, Shell shell, Object context) throws Exception;
	
	/**
	 * Execute a remote command. This is only applicable if the subsystem factory reports
	 *  true for supportsCommands().
	 * @param command Command to be executed remotely.
	 * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
	 * @param Object context context of a command (i.e. working directory).  Null is valid and means to run the 
	 * 			command as a shell command in the default shell.
	 * @param interpretOutput whether to interpret the output or not
	 * @return Array of objects that are the result of running this command. Typically, these
	 *   are messages logged by the command.
     * @see org.eclipse.rse.core.subsystems.shells.ui.RemoteCommandHelpers
	 */
	public Object[] runCommand(String command, Shell shell, Object context, boolean interpretOutput) throws Exception;


	/**
	 * Launch a new command shell. This is only applicable if the subsystem factory reports
	 *  true for supportsCommands().
	 * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
	 * @param Object context context of a shell (i.e. working directory).  Null is valid and means to use the default context.
	 * @return An object that represents the command and it's output.
     * @see org.eclipse.rse.core.subsystems.shells.ui.RemoteCommandHelpers
	 */
	public IRemoteCommandShell runShell(Shell shell, Object context) throws Exception;


	/**
	 * Send a command as input to a running command shell. 
	 * @param String input the command to invoke in the shell.
	 * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
	 * @param Object commandObject the shell or command to send the invocation to.
     * @see org.eclipse.rse.core.subsystems.shells.ui.RemoteCommandHelpers
	 */
	public void sendCommandToShell(String input, Shell shell, Object commandObject) throws Exception;

	/**
	 * Cancel a shell or running command.
	 * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
	 * @param Object commandObject the shell or command to cancel.
	 */
	public void cancelShell(Shell shell, Object commandObject) throws Exception;

	/**
	 * Remove a shell.  If the shell is running cancel it first.
	 * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
	 * @param Object commandObject the shell or command to cancel & remove.
	 */
	public void removeShell(Shell shell, Object commandObject) throws Exception;

	/**
	* Get the default running command shell for this command subsystem.  If no such shell exists or is running, a new one is launched.
	* @param shell a window used for notification
	* @return the default running command shell 
	*/
	public IRemoteCommandShell getDefaultShell(Shell shell) throws Exception;

	/**
	 * Get all command shells and transient commands that have been run or are running for this command subsystem.
	 * @return the list of running command shells and commands
	 */
	public IRemoteCommandShell[] getShells();

	/**
	 * Restore all the command shells that were open before disconnecting during
	 * the previous session
	 * @param shell a window used for notification
	 * @return the command shells that were restored
	 */
	public IRemoteCommandShell[] restoreShellState(Shell shell);
	
	/**
	 * Determine whether the command subsystem can run a shell
	 * @return whether a shell can be run or not
	 */
	public boolean canRunShell();	

	/**
	 * Determine whether the command subsystem can run a command
	 * @return whether a command can be run or not
	 */
	public boolean canRunCommand();	
	

	/**
	 * Provide list of executed commands on subsystem.This is only applicable if the subsystem factory reports
	 *  true for supportsCommands().
	 */
	public String[] getExecutedCommands();	
	
		
	/**
	 * Returns the shell encoding to assume when reading IO from a remote shell
	 */
	public String getShellEncoding();
	
	
	/**
	 * Provide a list of possible commands for the specified context.  This method is primarily used
	 * for command line assist to get a list of possible command completions.  Typically the context is
	 * a handle to a shell or command, such as an IRemoteCommandShell, but the interface is left generic, with Object,
	 * because some command subsystems have different notions of context.
	 * 
	 * @param context the context for the possible commands
	 * @return a list of possible commands
	 */
	public ICandidateCommand[] getCandidateCommands(Object context);	

	// ----------------------------------------
	// ENVIRONMENT VARIABLE METHODS ...
	// ----------------------------------------
	/**
	 * Get the initial environment variable list as a string of RemoteSystemEnvVar objects.
	 * Array returned may be size zero but will not be null.
	 */
	public IRemoteSystemEnvVar[] getEnvironmentVariableList();
	/**
	 * Set the initial environment variable list entries, all in one shot, using 
	 * a pair of String arrays: the first is the environment variable names,
	 * the second is the corresponding environment variable values.
	 */
	public void setEnvironmentVariableList(String[] envVarNames, String[] envVarValues);
	/**
	 * Add environment variable entry, given a name and value
	 */
	public void addEnvironmentVariable(String name, String value);
	/**
	 * Add environment variable entry, given a RemoteSystemEnvVar object
	 */
	public void addEnvironmentVariable(IRemoteSystemEnvVar rsev);
	/**
	 * Remove environment variable entry given its RemoteSystemEnvVar object
	 */
	public void removeEnvironmentVariable(IRemoteSystemEnvVar rsev);
	/**
	 * Remove environment variable entry given only its environment variable name
	 */
	public void removeEnvironmentVariable(String name);
	/**
	 * Given an environment variable name, find its RemoteSystemEnvVar object.
	 * Returns null if not found!
	 */
	public IRemoteSystemEnvVar getEnvironmentVariable(String name);
	/**
	 * Given an environment variable name, find its value.
	 * Returns null if not found.
	 */
	public String getEnvironmentVariableValue(String name);

	/**
	 * Get the invalid characters for an environment variable name.  Used
	 * by the environment variables property page to diagnose invalid
	 * environment variable names.
	 */
	public String getInvalidEnvironmentVariableNameCharacters(); 	
	
	/**
	 * <i><b>Private</b>. Do not override or call.</i>
	 * @generated This field/method will be replaced during code generation 
	 * @return The list of EnvVars references
	 */
	java.util.List getEnvVars();
	
	public List getHostEnvironmentVariables();
	
	
}