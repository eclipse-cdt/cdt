/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight   (IBM) - [283613] [dstore] Create a Constants File for all System Properties we support
 *******************************************************************************/

package org.eclipse.dstore.internal.core.model;

public interface IDataStoreSystemProperties {

	/*****************************************************************
	 * Tracing
	 *****************************************************************/
	// log directory allows customization of log location relative to user dir
	public static final String DSTORE_LOG_DIRECTORY="DSTORE_LOG_DIRECTORY"; //$NON-NLS-1$
	
	// indicates whether dstore tracing is on or not ("true" or "false")
	public static final String DSTORE_TRACING_ON="DSTORE_TRACING_ON"; //$NON-NLS-1$
	 
	
	/*****************************************************************
	 * Idle Shutdown 
	 *****************************************************************/
	// indicates how long to wait on an idle connection before disconnecting
	// a value of 0 disables this
	public static final String DSTORE_IDLE_SHUTDOWN_TIMEOUT="DSTORE_IDLE_SHUTDOWN_TIMEOUT";	  //$NON-NLS-1$

	/*****************************************************************
	 * Version
	 *****************************************************************/
	// indicates the dstore protocol version - rarely updated since the protocol itself is stable
	public static final String DSTORE_VERSION="DSTORE_VERSION"; //$NON-NLS-1$
	
	
	/*****************************************************************
	 * SSL
	 *****************************************************************/
	// for custom default certificate alias used in SSL mode
	public static final String DSTORE_DEFAULT_CERTIFICATE_ALIAS="DSTORE_DEFAULT_CERTIFICATE_ALIAS"; //$NON-NLS-1$
	 
	/*****************************************************************
	 *  Spiriting
	 *****************************************************************/
	// used to determine whether spirting is on ("true" or "false")
	public static final String DSTORE_SPIRIT_ON="DSTORE_SPIRIT_ON"; //$NON-NLS-1$
	
	// used to determine whether memlogging is on ("true" or "false")
	public static final String DSTORE_MEMLOGGING_ON="DSTORE_MEMLOGGING_ON"; //$NON-NLS-1$
	
	
	/*****************************************************************
	 * Server Logger
	 *****************************************************************/
	// used to determine the logger level:
	// 	0 is ERROR
	// 	1 is WARNING
	//  2 is INFO
	//  3 is DEBUG
	public static final String DSTORE_LOGGER_LOG_LEVEL="DSTORE_LOGGER_LOG_LEVEL"; //$NON-NLS-1$
	
	// used to determine the maximum log file size. A new log file gets created when the max is exceeded
	public static final String RSECOMM_LOGFILE_MAX="RSECOMM_LOGFILE_MAX"; //$NON-NLS-1$
	
	/*****************************************************************
	 * RSE shell
	 *****************************************************************/
	// allows for custom shell invocation
	public static final String DSTORE_SHELL_INVOCATION="DSTORE_SHELL_INVOCATION"; //$NON-NLS-1$
	
	// the maximum line length in a shell before wrap
	public static final String DSTORE_SHELL_MAX_LINE="DSTORE_SHELL_MAX_LINE"; //$NON-NLS-1$
	
	// special encoding to use when reading IO
	public static final String DSTORE_STDIN_ENCODING="dstore.stdin.encoding"; //$NON-NLS-1$
	
	/*****************************************************************
	 * Search
	 *****************************************************************/
	// tells search to only search unique folders ("true" or "false")
	public static final String DSTORE_SEARCH_ONLY_UNIQUE_FOLDERS="DSTORE_SEARCH_ONLY_UNIQUE_FOLDERS"; //$NON-NLS-1$
	
	// used for memory management - default is .8
	public static final String SEARCH_THRESHOLD="search.threshold"; //$NON-NLS-1$
	
	/*****************************************************************
	 * Daemon
	 *****************************************************************/
	// used to customized authentication program (normally we use auth.pl)
	public static final String RSE_AUTH="RSE.AUTH"; //$NON-NLS-1$
	
	// path to the server jars
	public static final String A_PLUGIN_PATH="A_PLUGIN_PATH"; //$NON-NLS-1$
	

	/*****************************************************************
	 * TCPNODELAY option
	 *****************************************************************/
	// specifying this as true disables Nagle's algorithm 
	public static final String DSTORE_TCP_NO_DELAY="DSTORE_TCP_NO_DELAY"; //$NON-NLS-1$

}
