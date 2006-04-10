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

package org.eclipse.rse.filters;
/**
 * A save policy dictates how filter framework artifacts are persisted to disk.
 */
public interface ISystemFilterSavePolicies 
{
	/**
	 * No saving. All save/restoring handled elsewhere.
	 */
	public static final int SAVE_POLICY_NONE = -1;
	/**
	 * Save all filter pools and all filters in one file, with same name as the manager
	 */
	public static final int SAVE_POLICY_ONE_FILE_PER_MANAGER = 0;
	/**
	 * Save all filters in each filter pool in one file per pool, with the same name as the pool.
	 * Each pool also has its own unique folder.
	 */
	public static final int SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL = 1;	
	/**
	 * Save all filters in each filter pool in one file per pool, with the same name as the pool
	 * All pool files go into the same folder.
	 */
	public static final int SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER = 2;		
	/**
	 * Save each filter in each filter pool in its own file, with the same name as the filter
	 */
	public static final int SAVE_POLICY_ONE_FILE_PER_FILTER = 3;	

}