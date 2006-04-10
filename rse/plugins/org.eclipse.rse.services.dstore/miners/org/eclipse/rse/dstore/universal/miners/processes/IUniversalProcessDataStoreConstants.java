/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.dstore.universal.miners.processes;

public interface IUniversalProcessDataStoreConstants
{

	/*
	 * Miner name, used for logging
	 */
	public static final String UNIVERSAL_PROCESS_MINER = "UniversalProcessMiner";

	//
	// Universal Process descriptors for DataStore DataElements
	//
	public static final String UNIVERSAL_PROCESS_ROOT = "universal.process.root";
	public static final String UNIVERSAL_PROCESS_FILTER = "universal.process.filter";
	public static final String UNIVERSAL_PROCESS_DESCRIPTOR = "universal.process.descriptor";

	//
	// Universal Process Miner Commands
	//
	public static final String C_PROCESS_FILTER_QUERY_ALL  = "C_PROCESS_FILTER_QUERY_ALL";
	public static final String C_PROCESS_KILL = "C_PROCESS_KILL";
	public static final String C_PROCESS_QUERY_ALL_PROPERTIES = "C_PROCESS_QUERY_ALL_PROPERTIES";
	
}