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

package org.eclipse.rse.files.ui.resources;

/**
 * This interface defines a remote path. It is not intended to be implemented by
 * clients.
 */
public interface ISystemRemotePath {



	/**
	 * Get the profile name.
	 * @return the profile name
	 */
	public String getProfileName();
	
	/**
	 * Get the connection name.
	 * @return the connection name
	 */
	public String getConnectionName();
	
	/**
	 * Get the path of the resource on the server.
	 * @return the path of the resource on the server
	 */
	public String getPath();
	
	/**
	 * Returns the complete string representation of the remote path.
	 * @return complete string representation of the remote path
	 */
	public String toString();
	
	/**
	 * Returns whether this path equals the given object.
	 * This is system dependent.
	 */
	public boolean equals(Object obj);  
}