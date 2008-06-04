/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 *  The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: Noriaki Takatsu and Masao Nishimoto
 *
 * Contributors:
 *   Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 *******************************************************************************/

package org.eclipse.dstore.core.server;

/**
 * @since 3.0
 */
public class SystemServiceManager
{
	private static SystemServiceManager instance = null;
	private static ISystemService _systemService;


	/**
     * Creates an instance of SystemServiceManager to hold the system-specific
     * parts that needs unique implementations for this system.
     *
     */
	private SystemServiceManager()
    {}

	/**
     * Get the SystemServiceManager object for this system.
     *
     * @return the object of the SystemServiceManager
     */
	public static SystemServiceManager getInstance()
	{
		if (instance == null)
		{
			instance = new SystemServiceManager();
		}
		return instance;
	}

	/**
     * Set the SystemService object for this system.
     *
     * @param systemService the object of the SystemService
     */
	public void setSystemService(ISystemService systemService)
	{
		_systemService = systemService;
	}

	/**
     * Get the SystemService object for this system.
     *
     * @return the object of the SystemService stored in SystemServiceManager
     */
	public ISystemService getSystemService()
	{
		return _systemService;
	}


}
