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

package org.eclipse.rse.core.subsystems;

/**
 * Interface for a cache manager that can optionally associated with a SubSystem.
 * 
 * @since RSE 6.0
 */
public interface ICacheManager {
	

	/**
	 * This flag is set if the Remote System Explorer is restoring a remote object
	 * from a memento.  This gives the subsystem the option to restore from the cache 
	 * instead of connecting to the remote system.
	 * 
	 * @param restore true if the RSE is currently restoring a remote object associated
	 * with this cache manager from a memento, otherwise false.
	 */
	public void setRestoreFromMemento(boolean restore);

	/**
	 * Check if the Remote System Explorer is restoring a remote object
	 * from a memento.
	 * 
	 * @return true if the RSE is currently restoring a remote object associated
	 * with this cache manager from a memento, otherwise false.
	 */
	public boolean isRestoreFromMemento();

}