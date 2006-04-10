/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core;
/**
 * Constants for system types.
 * These are kept in synch with the definitions from plugin.xml in org.eclipse.rse.core.
 */
public interface ISystemTypes
{
	
	/**
	 * Linux system type, "Linux".
	 */
	public static final String SYSTEMTYPE_LINUX = "Linux";
	
	/**
	 * Power Linux type, "Power Linux".
	 */
	public static final String SYSTEMTYPE_POWER_LINUX = "Power Linux";
	
	/**
	 * Power Linux type, "zSeries Linux".
	 */
	public static final String SYSTEMTYPE_ZSERIES_LINUX = "zSeries Linux";
	
	/**
	 * Unix system type, "Unix".
	 */
	public static final String SYSTEMTYPE_UNIX = "Unix";
	
	/**
	 * AIX system type, "AIX".
	 */
	public static final String SYSTEMTYPE_AIX = "AIX";
	
	/**
	 * PASE system type, "PASE".
	 */
	public static final String SYSTEMTYPE_PASE = "PASE";
	
	/**
	 * iSeries system type, "iSeries".
	 */
	public static final String SYSTEMTYPE_ISERIES = "iSeries";
	
	/**
	 * Local system type, "Local".
	 */
	public static final String SYSTEMTYPE_LOCAL = "Local";
	
	/**
	 * z/OS system type, "z/OS".
	 */
	public static final String SYSTEMTYPE_ZSERIES = "z/OS";
	
	/**
	 * Windows system type, "Windows".
	 */
	public static final String SYSTEMTYPE_WINDOWS = "Windows";
}