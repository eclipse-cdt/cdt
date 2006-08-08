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

import org.eclipse.core.resources.IProject;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;


/**
 * This interface represents a handle to a remote command,
 * which is either a transient command or a command shell.
 */ 
public interface IRemoteCommandShell
{

	/**
	 * Gets the ID of the command shell
	 * @return the id
	 */
	public String getId();
	

	/**
	 * Gets the name of the command shell or command that is being run.
	 * @return the name of the command
	 */
	public String getName();

	/**
	 * Gets the type of the command shell or command that is being run.  The type may either be a "Shell" or a "Command".
	 * @return the type of the command
	 */
	public String getType();

	/**
	 * Get the list of output objects for this command.
	 * @return the list of output objects
	 */
	public Object[] listOutput();

	/**
	 * Get the current context for this command shell. In the case of a unix shell, this
	 * will be the working directory as an 
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile.
	 * 
	 * This is used in order to store the state of the current shell on disconnect,
	 * such that the same directory can be set on reconnect. It may also be used
	 * as input for content assist.
	 * 
	 * @return the current context
	 */
	public Object getContext();
	

	/**
	 * Return the number of output objects for this command.
	 * @return the number of output objects
	 */
	public int getSize();

	/**
	 * Return the file subsystem associated with this command
	 * @return the file subsystem associated with this command
	 */
	public IRemoteFileSubSystem getFileSubSystem();
	
	/**
	 * Return the command subsystem associated with this command
	 * @return the command subsystem associated with this command
	 */
	public IRemoteCmdSubSystem getCommandSubSystem();
	
	
	/*
	 * Set the associated project for this command shell.  The
	 * associated project is looked up first when resolving files
	 * from shell (i.e. during double click on errors)
	 * @param the project to associate with this shell
	 */
	public void associateProject(IProject project);

	/*
	 * Return the associated project for this command shell.  The
	 * associated project is looked up first when resolving files
	 * from shell (i.e. during double click on errors)
	 * @return the associated project for this command shell
	 */
	public IProject getAssociatedProject();

	/**
	 * Add an output object to the list of output for this command
	 * @param output the output object to add
	 */
	public void addOutput(Object output);

	/**
	 * Remove output from the list of output for this command
	 */
	public void removeOutput();
	
	/**
	 * Remove output from the list of output for this command
	 */
	public void removeOutput(Object output);
	
	/**
	 * Get the position of this output object in the command container
	 */
	public int getIndexOf(Object output);

	/**
	 * Get the output object the specified index
	 */
	public Object getOutputAt(int index);
	
	/**
	 * Indicate whether the corresponding command is running or not
	 * @return whether the command is running or not
	 */
	public boolean isActive();

	/**
	 * Return the list of commands passed into this command
	 * @return the list of commands issued
	 */
	public String[] getHistory();
	
	public void updateHistory(String cmd);
	
	/**
	 * Return the list of possible commands for this command shell
	 * @return the list of possible commands
	 */
	public ICandidateCommand[] getCandidateCommands();
}