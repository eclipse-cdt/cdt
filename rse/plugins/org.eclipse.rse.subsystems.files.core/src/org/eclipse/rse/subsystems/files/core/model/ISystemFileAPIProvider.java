/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 ********************************************************************************/

package org.eclipse.rse.subsystems.files.core.model;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemViewInputProvider;



/**
 * Where to start when looking to traverse a remote file system
 * @see org.eclipse.rse.internal.subsystems.files.core.SystemFileAPIProviderImpl
 */
public interface ISystemFileAPIProvider extends ISystemViewInputProvider
{
	/**
	 * Get the directories-only mode. 
	 */
	public boolean isDirectoriesOnly();	
    /**
     * Return all connections which have at least one subsystem that implements/extends RemoteFileSubSystem
     */
    public IHost[] getConnections();
    /**
     * Return a count of all connections which have at least one subsystem that implements/extends RemoteFileSubSystem
     */
    public int getConnectionCount();	
}