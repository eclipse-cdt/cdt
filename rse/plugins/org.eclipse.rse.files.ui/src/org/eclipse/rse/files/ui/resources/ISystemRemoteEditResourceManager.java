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
 * This manager class manages remote resources.
 */
public interface ISystemRemoteEditResourceManager extends ISystemRemoteManager {


	
	/**
	 * Store a remote edit object with the given remote path as the key.
	 * The idea is that each unique remote object can have an associated
	 * edit object associated with it, and this object will take care
	 * of remote editing of that object. Using the remote path as a key ensures
	 * that a resource with a unique path will have a unique remote object.
	 * @param the remote path to use as a key
	 * @param the editbable object
	 * @return the previously stored edit object, or null if none
	 */
	public Object putEditObject(ISystemRemotePath key, Object editObj);
	
	/**
	 * Get a remote edit object given the remote path as a key.
	 * @param the remote path as a key
	 * @return the stored edit object
	 */
	public Object getEditObject(ISystemRemotePath key);
	
	/**
	 * Save the edit object information to disk.
	 * Clients must not call this method.
	 */
	public void save();
	
	/**
	 * Restore the edit object information from disk.
	 * Clients must not call this method.
	 */
	public void restore();
}