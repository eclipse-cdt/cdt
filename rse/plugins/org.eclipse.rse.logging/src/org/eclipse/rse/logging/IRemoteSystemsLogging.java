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

package org.eclipse.rse.logging;

/**
 * Defines all logging constants.
 */
public interface IRemoteSystemsLogging {


	// All attributes here are static final.
	/**
	 * Name of the key that controls the logging level.<br>
	 */
	String PLUGIN_ID = "org.eclipse.rse.logging";

	/**
	 * Name of the key that controls the logging level.<br>
	 * (value is "debug_level").
	 */
	String DEBUG_LEVEL = "debug_level";

	/**
	 * Name of the key that controls the log location.<br>
	 * (value is "log_location").
	 */
	String LOG_LOCATION = "log_location";

	/**
	 * Set debug_level to this value to get Error messages.<br>
	 * (value is 0).
	 */
	int LOG_ERROR = 0;

	/**
	 * Set debug_level to this value to get Warning messages.<br>
	 * (value is 1).
	 */
	int LOG_WARNING = 1;

	/**
	 * Set debug_level to this value to get Information messages.<br>
	 * (value is 2).
	 */
	int LOG_INFO = 2;

	/**
	 * Set debug_level to this value to get Debug messages.<br>
	 * (value is 3).
	 */
	int LOG_DEBUG = 3;

	/**
	 * Set log_location to this value to log to a file.<br>
	 * (value is "Log_To_File").
	 */
	String LOG_TO_FILE = "Log_To_File";

	/**
	 * Set log_location to this value to log to a Std out.<br>
	 * (value is "Log_To_StdOut").
	 */
	String LOG_TO_STDOUT = "Log_To_StdOut";

}