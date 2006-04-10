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

import org.eclipse.rse.core.subsystems.IRemoteLineReference;

/**
 * This interface represents a handle to a line of remote command output.
 */
public interface IRemoteOutput extends IRemoteLineReference
{


	/**
	 * Gets the type of a line of output.  By default remote output is "stdout", but it could be one of a 
	 * number of things (i.e. "error", "warning", "file", "directory", "grep", etc.)
	 *  
	 * @return the output type
	 */
	public String getType();

	/**
	 * Gets the text to display for a line of output. 
	 * 
	 * @return the output text
	 */
	public String getText();
	
	/**
	 * Gets the index of this output within a command
	 * 
	 * @return the index within the command
	 */
	public int getIndex();

	

}