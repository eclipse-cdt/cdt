/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 * David McKnight   (IBM) - [195285] mount path mapper changes
 * David McKnight   (IBM)        - [245260] Different user's connections on a single host are mapped to the same temp files cache
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.resources;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.files.ui.resources.ISystemMountPathMapper;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFilePreferencesConstants;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.RSEUIPlugin;

public class DefaultMountPathMapper implements ISystemMountPathMapper
{

	public boolean handlesMappingFor(String hostname, String remotePath, IRemoteFileSubSystem subsystem)
	{
		return false;
	}
	
	public String getActualHostFor(String hostname, String remotePath)
	{
		return hostname;
	}
	
	public String getWorkspaceMappingFor(String hostname, String remotePath, IRemoteFileSubSystem subSystem)
	{
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		boolean shareCachedFiles = store.getBoolean(ISystemFilePreferencesConstants.SHARECACHEDFILES);
		
		// if we're not sharing cached files, then we need a unique path for each connection
		if (!shareCachedFiles){
			// prefix with the connection alias
			String alias = subSystem.getHostAliasName();
			String configID = subSystem.getConfigurationId();
			return alias + '.' + configID + File.separatorChar + remotePath;
		}
		else {
			return remotePath;
		}
	}
	
	/**
	 * Returns the remote path.  
	 */
	public String getMountedMappingFor(String hostname, String remotePath)
	{
		return remotePath;
	}

	public int getPriority(String hostname, String remotePath,
			IRemoteFileSubSystem subsystem) {
		return Integer.MAX_VALUE;
	}
}
