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

package org.eclipse.rse.subsystems.files.core.model;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;


/**
 * Class representing a remote path.
 */
public class RemotePath implements IRemotePath {
	
	protected String profileName;
	protected String connectionName;
	protected String absolutePath;

	/**
	 * Constructor.
	 * @param profileName the profile name.
	 * @param connectionName the connection name.
	 * @param absolutePath the absolute path.
	 */
	public RemotePath(String profileName, String connectionName, String absolutePath) {
		super();
		setProfileName(profileName);
		setConnectionName(connectionName);
		setAbsolutePath(absolutePath);
	}
	

	/** 
	 * Sets the profile name, connection name and absolute path on the remote machine
	 * from the fully qualified path. The fully qualified path is the absolute path on the remote machine,
	 * prefixed by profile name and connection name. It must be of the form <code>"profileName.connectionName:absolutePath"</code>.
	 * @param fullyQualifiedName the fully qualified name.
	 */
	public RemotePath(String fullyQualifiedPath) {
		
		if (fullyQualifiedPath != null) {
			
			int dotIndex = fullyQualifiedPath.indexOf('.');
			
			if ((dotIndex != -1) && (dotIndex != (fullyQualifiedPath.length() - 1))) {
				int colonIndex = fullyQualifiedPath.indexOf(':', dotIndex+1);
				
				if ((colonIndex != -1) && (colonIndex != (fullyQualifiedPath.length() - 1))) {
					setProfileName(fullyQualifiedPath.substring(0, dotIndex));
					setConnectionName(fullyQualifiedPath.substring(dotIndex+1, colonIndex));
					setAbsolutePath(fullyQualifiedPath.substring(colonIndex+1));
				}
			}
		}
	}

	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#setProfileName(java.lang.String)
	 */
	protected void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#setConnectionName(java.lang.String)
	 */
	protected void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#setAbsolutePath(java.lang.String)
	 */
	protected void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#getProfileName()
	 */
	public String getProfileName() {
		return profileName;
	}

	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#getConnectionName()
	 */
	public String getConnectionName() {
		return connectionName;
	}

	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#getAbsolutePath()
	 */
	public String getAbsolutePath() {
		return absolutePath;
	}
	
	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#getFullyQualifiedPath()
	 */
	public String getFullyQualifiedPath() {
		
		if ((profileName == null) || (connectionName == null) || (absolutePath == null)) {
			return null;
		}
		else {
			return profileName + "." + connectionName + ":" + absolutePath;
		}
	}

	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#getFileExtension()
	 */
	public String getFileExtension() {
		
		if (absolutePath == null) {
			return null;
		}
		else {
			
			int dotIndex = absolutePath.lastIndexOf('.');
			
			if (dotIndex != -1) {
				
				if (dotIndex != (absolutePath.length() - 1)) {
					return absolutePath.substring(dotIndex+1);
				}
				else {
					return "";
				}
			}
			else {
				return null;
			}
		}
	}
	
	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#addFileExtension(java.lang.String)
	 */
	public IRemotePath addFileExtension(String extension) {
		
		if (absolutePath == null) {
			return null;
		}
		
		if (absolutePath.endsWith("/") || absolutePath.endsWith("\\")) {
			return this;
		}
		else {
			String newAbsolutePath = absolutePath + "." + extension;
			return new RemotePath(profileName, connectionName, newAbsolutePath);
		}
	}
	
	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#removeFileExtension(java.lang.String)
	 */
	public IRemotePath removeFileExtension() {

		if (absolutePath == null) {
			return null;
		}
		
		if (absolutePath.endsWith("/") || absolutePath.endsWith("\\")) {
			return this;
		}
		else {
			int dotIndex = absolutePath.lastIndexOf('.');
			
			if (dotIndex == -1) {
				return this;
			}
			else {
				String newAbsolutePath = absolutePath.substring(0, dotIndex);
				return new RemotePath(profileName, connectionName, newAbsolutePath);
			}
		}
	}
	
	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#isVirtual()
	 */
	public boolean isVirtual() {
		return ArchiveHandlerManager.isVirtual(absolutePath);
	}
	
	/**
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#toRemoteFile()
	 */
	public IRemoteFile toRemoteFile() {
		
		if ((profileName == null) || (connectionName == null) || (absolutePath == null)) {
			return null;
		}
		
		ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
		ISystemProfile profile = registry.getSystemProfile(profileName);
		
		if (profile == null) {
			return null;
		}
		
		IHost conn = registry.getHost(profile, connectionName);
		
		if (conn == null) {
			return null;
		}
		
		IRemoteFileSubSystem subsys = RemoteFileUtility.getFileSubSystem(conn);
		IRemoteFile remoteFile = null;
		
		try {
			remoteFile = subsys.getRemoteFileObject(absolutePath);
		}
		catch (SystemMessageException e) {
			SystemBasePlugin.logError("Error occured trying to get remote file", e);
			remoteFile = null;
		}
		
		return remoteFile;
	}
}