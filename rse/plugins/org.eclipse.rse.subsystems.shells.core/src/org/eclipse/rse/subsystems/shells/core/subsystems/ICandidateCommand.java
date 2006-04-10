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

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This interface represents a candidate command.  A candidate command is a command that can be
 * run in a command subsystem.  The command subsystem api, getCandidateCommands(), returns a list
 * of candidate commands.  The primary use of candidate commands is for command line content assist.
 * Implement this interface to provide unique command candidates for a particular command subsystem.
 */ 
public interface ICandidateCommand
{

	/**
	 * Gets the name of the candidate command
	 * @return the name of the candidate command
	 */
	public String getName();

	/**
	 * Gets the type of the candidate command. 
	 * @return the type of the command
	 */
	public String getType();

	/**
	 * Gets the description for a candidate command.
	 * @return the description of the command
	 */
	public String getDescription();
	
	/**
	 * Gets the image descriptor to display for a candidate command
	 * @return the image descriptor for the command
	 */
	public ImageDescriptor getImageDescriptor();
	
	/**
	 * Gets the path for a candidate command
	 * @return the path of the command if one exists
	 */
	public String getPath();
	
}