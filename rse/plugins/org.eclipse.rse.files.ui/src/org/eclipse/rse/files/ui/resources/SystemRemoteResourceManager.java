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

import java.util.Hashtable;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class manages remote resources.
 */
public class SystemRemoteResourceManager implements ISystemRemoteManager {


	
	private static SystemRemoteResourceManager instance;
	
	// resource info hash table
	private Hashtable resourceInfos = new Hashtable();

	/**
	 * Constructor for SystemRemoteResourceManager.
	 */
	private SystemRemoteResourceManager() {
		super();
		restore();
	}
	
	/**
	 * Get the singleton instance.
	 * @return the singleton instance.
	 */
	public static SystemRemoteResourceManager getInstance() {
		
		if (instance == null) {
			instance = new SystemRemoteResourceManager();
		}
		
		return instance;
	}
	
	/**
	 * Get a resource info given a path.
	 */
	public SystemRemoteResourceInfo getResourceInfo(ISystemRemotePath path) {
		return (SystemRemoteResourceInfo)(resourceInfos.get(path.toString()));
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteManager#startup(IProgressMonitor)
	 */
	public void startup(IProgressMonitor monitor) {
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteManager#shutdown(IProgressMonitor)
	 */
	public void shutdown(IProgressMonitor monitor) {
	}
	
	/**
	 * Read all resource information from disk.
	 */
	public void restore() {}
	
	/**
	 * Write all resource information to disk.
	 */
	public void save() {}
}