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

import org.eclipse.core.resources.IFile;

/**
 * This interface defines some common functionality required from all remote
 * resources for edit, irrespective of whether the remote system is an
 * OS/400, Windows, Linux or Unix operating system.
 */
public interface ISystemRemoteEditResource extends ISystemRemoteResource {


	
	/**
	 * Returns the local resource. The local resource does not exist if the method
	 * returns null, or if calling exists() on the returned resource returs false.
	 * @return the local resource.
	 */
	public IFile getLocalResource();
}