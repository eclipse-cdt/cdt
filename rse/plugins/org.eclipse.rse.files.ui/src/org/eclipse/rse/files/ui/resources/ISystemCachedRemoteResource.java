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
 * This is the cached remote resource.
 */
public interface ISystemCachedRemoteResource {


	
	/**
	 * Constant indicating no changes.
	 */
	public static final int CHANGES_NONE = 0;
	
	/**
	 * Constant indicating local copy has changed, but not server copy.
	 */
	public static final int CHANGES_OUTGOING = 1;
	
	/**
	 * Constant indicating server copy has changed, but not local copy.
	 */
	public static final int CHANGES_INCOMING = 2;
	
	/**
	 * Constant indicating both local and server copy have changed.
	 */
	public static final int CHANGES_CONFLICT = 3;

	/**
	 * Get the last modified time on the server. Calling this when connected will
	 * query it from the server, but calling it in disconnected mode will
	 * query it from disk.
	 * @return the last modified time on the server
	 */
	public long lastRemoteModified();
	
	/**
	 * Get the last modified time on the client.
	 * @return the last modified time on the client
	 */
	public long lastLocalModified();
	
	/**
	 * Get the last time of download or synchronization.
	 * @return the time of the last download or synchronization
	 */
	public long lastSuccessfulSynch();
	
	/**
	 * Returns whether the local and remote copies are synchronized.
	 * @return <code>true</code> if the local and remote copies are synchronized,
	 * <code>false</code> otherwise.
	 */
	public boolean isSynchronized();
	
	/**
	 * Get the state of the cache.
	 * @return CHANGES_NONE if both local and remote copies are in sync,
	 * 			CHANGES_OUTGOING if the local copy has changed but not the server copy,
	 * 			CHANGES_INCOMING if the server copy has changed but not the local copy,
	 *			CHANGES_CONFLICT if both the local copy and server copy have changed.
	 */
	public int getState();
	
	/**
	 * Dispose of the cache.
	 */
	public void dispose();
}