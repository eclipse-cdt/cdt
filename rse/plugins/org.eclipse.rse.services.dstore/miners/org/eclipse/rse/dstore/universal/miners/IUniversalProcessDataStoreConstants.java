/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.dstore.universal.miners;

public interface IUniversalProcessDataStoreConstants
{

	/*
	 * Miner name, used for logging
	 */
	public static final String UNIVERSAL_PROCESS_MINER = "UniversalProcessMiner"; //$NON-NLS-1$

	//
	// Universal Process descriptors for DataStore DataElements
	//
	public static final String UNIVERSAL_PROCESS_ROOT = "universal.process.root"; //$NON-NLS-1$
	public static final String UNIVERSAL_PROCESS_FILTER = "universal.process.filter"; //$NON-NLS-1$
	public static final String UNIVERSAL_PROCESS_DESCRIPTOR = "universal.process.descriptor"; //$NON-NLS-1$
	public static final String UNIVERSAL_PROCESS_TEMP = "universal.process.temp"; //$NON-NLS-1$

	//
	// Universal Process Miner Commands
	//
	public static final String C_PROCESS_FILTER_QUERY_ALL  = "C_PROCESS_FILTER_QUERY_ALL"; //$NON-NLS-1$
	public static final String C_PROCESS_KILL = "C_PROCESS_KILL"; //$NON-NLS-1$
	public static final String C_PROCESS_QUERY_ALL_PROPERTIES = "C_PROCESS_QUERY_ALL_PROPERTIES"; //$NON-NLS-1$
	public static final String C_PROCESS_QUERY_USERNAME  = "C_PROCESS_QUERY_USERNAME"; //$NON-NLS-1$
	
}
