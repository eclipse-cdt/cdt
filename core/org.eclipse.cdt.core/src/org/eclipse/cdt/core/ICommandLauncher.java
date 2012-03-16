/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An interface for launchers of external commands.
 *
 * @since 5.1
 */
public interface ICommandLauncher {

	public final static int COMMAND_CANCELED = 1;
	public final static int ILLEGAL_COMMAND = -1;
	public final static int OK = 0;


	/**
	 * Sets the project that this launcher is associated with, or <code>null</code> if there is no such
	 * project.
	 *
	 * @param project
	 */
	public void setProject(IProject project);

	/**
	 * Gets the project this launcher is associated with.
	 *
	 * @return IProject, or <code>null</code> if there is no such project.
	 */
	public IProject getProject();

	/**
	 * Sets if the command should be printed out first before executing.
	 */
	public void showCommand(boolean show);

	/**
	 * Returns a human readable error message corresponding to the last error encountered during command
	 * execution.
	 *
	 * @return A String corresponding to the error, or <code>null</code> if there has been no error.
	 *   The message could be multi-line, however it is NOT guaranteed that it ends with end of line.
	 */
	public  String getErrorMessage();

	/**
	 * Sets the human readable error message corresponding to the last error encountered during command
	 * execution.  A subsequent call to getErrorMessage() will return this string.
	 *
	 * @param error A String corresponding to the error message, or <code>null</code> if the error state is
	 * intended to be cleared.
	 */
	public  void setErrorMessage(String error);

	/**
	 * Returns an array of the command line arguments that were last used to execute a command.
	 *
	 * @return an array of type String[] corresponding to the arguments.  The array can be empty, but should not
	 * be null.
	 */
	public  String[] getCommandArgs();

	/**
	 * Returns the set of environment variables in the context of which
	 * this launcher will execute commands.
	 *
	 * @return Properties
	 */
	public  Properties getEnvironment();

	/**
	 * Returns the constructed command line of the last command executed.
	 *
	 * @return String
	 */
	public  String getCommandLine();

	/**
	 * Execute a command
	 * @param env The list of environment variables in variable=value format.
	 * @throws CoreException if there is an error executing the command.
	 */
	public Process execute(IPath commandPath, String[] args, String[] env, IPath workingDirectory, IProgressMonitor monitor) throws CoreException;

	/**
	 * Reads output form the process to the streams.
	 * @deprecated Deprecated as of CDT 8.1. Use method taking IProgressMonitor instead.
	 */
	@Deprecated
	public  int waitAndRead(OutputStream out, OutputStream err);

	/**
	 * Reads output form the process to the streams. A progress monitor is
	 * polled to test if the cancel button has been pressed. Destroys the
	 * process if the monitor becomes canceled override to implement a different
	 * way to read the process inputs
	 */
	public  int waitAndRead(OutputStream output, OutputStream err, IProgressMonitor monitor);

}