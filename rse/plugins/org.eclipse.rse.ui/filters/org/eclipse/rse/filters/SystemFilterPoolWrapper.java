/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.filters;

/**
 * The system filter wizard allows callers to pass a list of wrapper objects
 *  for the user to select a filter pool.
 * <p>
 * This is a default implementation of the wrapper interface, that allows the
 * display name and wrappered filter pool to be set via the constructor.
 */
public class SystemFilterPoolWrapper implements ISystemFilterPoolWrapper 
{


	private String displayName;
	private ISystemFilterPool pool;

	/**
	 * Constructor for SystemFilterPoolWrapper.
	 */
	public SystemFilterPoolWrapper(String displayName, ISystemFilterPool poolToWrapper) 
	{
		super();
		this.displayName = displayName;
		this.pool = poolToWrapper;
	}

	/**
	 * @see org.eclipse.rse.filters.ISystemFilterPoolWrapper#getDisplayName()
	 */
	public String getDisplayName() 
	{
		return displayName;
	}

	/**
	 * @see org.eclipse.rse.filters.ISystemFilterPoolWrapper#getSystemFilterPool()
	 */
	public ISystemFilterPool getSystemFilterPool() 
	{
		return pool;
	}

}