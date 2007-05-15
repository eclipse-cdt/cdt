/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [cleanup] Fix javadoc
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.core.model;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.model.IRemotePath;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;


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
	 * @param fullyQualifiedPath the fully qualified path.
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
	 * Set the profile name.
	 * @param profileName the profile name.
	 */
	protected void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	/**
	 * Set the connection name.
	 * @param connectionName the connection name.
	 */
	protected void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	/**
	 * Set the absolute path string below the connection.
	 * Interpretation of this string is up to the subsystem.
	 * @param absolutePath the absolute path string.
	 */
	protected void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#getProfileName()
	 */
	public String getProfileName() {
		return profileName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#getConnectionName()
	 */
	public String getConnectionName() {
		return connectionName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#getAbsolutePath()
	 */
	public String getAbsolutePath() {
		return absolutePath;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#getFullyQualifiedPath()
	 */
	public String getFullyQualifiedPath() {
		
		if ((profileName == null) || (connectionName == null) || (absolutePath == null)) {
			return null;
		}
		else {
			return profileName + "." + connectionName + ":" + absolutePath; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/*
	 * (non-Javadoc)
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
					return ""; //$NON-NLS-1$
				}
			}
			else {
				return null;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#addFileExtension(java.lang.String)
	 */
	public IRemotePath addFileExtension(String extension) {
		
		if (absolutePath == null) {
			return null;
		}
		
		if (absolutePath.endsWith("/") || absolutePath.endsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
			return this;
		}
		else {
			String newAbsolutePath = absolutePath + "." + extension; //$NON-NLS-1$
			return new RemotePath(profileName, connectionName, newAbsolutePath);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#removeFileExtension()
	 */
	public IRemotePath removeFileExtension() {

		if (absolutePath == null) {
			return null;
		}
		
		if (absolutePath.endsWith("/") || absolutePath.endsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
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
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#isVirtual()
	 */
	public boolean isVirtual() {
		return ArchiveHandlerManager.isVirtual(absolutePath);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.IRemotePath#toRemoteFile()
	 */
	public IRemoteFile toRemoteFile() {
		
		if ((profileName == null) || (connectionName == null) || (absolutePath == null)) {
			return null;
		}
		
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
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
			remoteFile = subsys.getRemoteFileObject(absolutePath, new NullProgressMonitor());
		}
		catch (SystemMessageException e) {
			SystemBasePlugin.logError("Error occured trying to get remote file", e); //$NON-NLS-1$
		}
		
		return remoteFile;
	}
}
