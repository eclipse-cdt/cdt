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

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface should be implemented by all remote resource related
 * managers
 */
public interface ISystemRemoteManager {



	/**
	 * This method is called during plugin startup.
	 * @param a progress monitor for long running operations, or null
	 * if none is desired.
	 */
	public void startup(IProgressMonitor monitor);
	
	/**
	 * This method is called during plugin shutdown.
	 * @param a progress monitor for long running operations, or null
	 * if none is desired.
	 */
	public void shutdown(IProgressMonitor monitor);
}