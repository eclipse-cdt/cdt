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
 * A default implementation of a remote path.
 */
public class SystemRemotePath implements ISystemRemotePath {


	
	public static final ISystemRemotePath ROOT = new SystemRemotePath("", "", "/");
	
	protected String profileName;
	protected String connectionName;
	protected String path;

	/**
	 * Constructor for SystemRemotePath.
	 * @param profile name
	 * @param connection name
	 * @param path
	 */
	public SystemRemotePath(String profileName, String connectionName, String path) {
		super();
		setProfileName(profileName);
		setConnectionName(connectionName);
		setPath(path);
	}
	
	/**
	 * Set the profile name.
	 * @param the profile name
	 */
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	
	/**
	 * Set the connection name.
	 * @param the connection name
	 */
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}
	
	/**
	 * Set the remote path.
	 * @param the path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemotePath#getProfileName()
	 */
	public String getProfileName() {
		return profileName;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemotePath#getConnectionName()
	 */
	public String getConnectionName() {
		return connectionName;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemotePath#getPath()
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		return (profileName.equalsIgnoreCase(profileName)) && (connectionName.equalsIgnoreCase(connectionName)) && (path.equalsIgnoreCase(path));
	}
}