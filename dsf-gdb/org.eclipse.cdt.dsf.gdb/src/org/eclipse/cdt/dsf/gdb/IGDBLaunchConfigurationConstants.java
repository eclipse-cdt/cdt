/*******************************************************************************
 * Copyright (c) 2008, 2010  Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;


public class IGDBLaunchConfigurationConstants {

	//
	// Taken from org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants
	//
	public static final String ATTR_REMOTE_TCP = GdbPlugin.PLUGIN_ID + ".REMOTE_TCP"; //$NON-NLS-1$
	public static final String ATTR_HOST = GdbPlugin.PLUGIN_ID + ".HOST"; //$NON-NLS-1$
	public static final String ATTR_PORT = GdbPlugin.PLUGIN_ID + ".PORT"; //$NON-NLS-1$
	public static final String ATTR_DEV = GdbPlugin.PLUGIN_ID + ".DEV"; //$NON-NLS-1$
	public static final String ATTR_DEV_SPEED = GdbPlugin.PLUGIN_ID + ".DEV_SPEED"; //$NON-NLS-1$
	//
	//
	
	//
	// New to DSF GDB/MI
	public static final String DEBUGGER_MODE_REMOTE = "remote"; //$NON-NLS-1$
	public static final String DEBUGGER_MODE_REMOTE_ATTACH = "remote_attach"; //$NON-NLS-1$
	//
	//

	//
	// Taken from org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants
	//
	/**
	 * Launch configuration attribute key. The value is the name of
	 * the Debuger associated with a C/C++ launch configuration.
	 */
	public static final String ATTR_DEBUG_NAME = GdbPlugin.PLUGIN_ID + ".DEBUG_NAME"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the gdb command file
	 * Debuger/gdb/MI property.
	 */
	public static final String ATTR_GDB_INIT = GdbPlugin.PLUGIN_ID + ".GDB_INIT"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the non-stop mode
	 * Debuger/gdb/MI property.
	 * @since 1.1
	 */
	public static final String ATTR_DEBUGGER_NON_STOP = GdbPlugin.PLUGIN_ID + ".NON_STOP"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the 'automatically load shared library symbols' flag of the debugger.
	 */
	public static final String ATTR_DEBUGGER_AUTO_SOLIB = GdbPlugin.PLUGIN_ID + ".AUTO_SOLIB"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the 'use shared library symbols for application' flag of the debugger.
	 * @since 1.1
	 */
	public static final String ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP = GdbPlugin.PLUGIN_ID + ".USE_SOLIB_SYMBOLS_FOR_APP"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a List (array of String) of directories for the search path of shared libraries.
	 */
	public static final String ATTR_DEBUGGER_SOLIB_PATH = GdbPlugin.PLUGIN_ID + ".SOLIB_PATH"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a List (array of String) of shared libraries to load symbols automatically.
	 */
	public static final String ATTR_DEBUGGER_AUTO_SOLIB_LIST = GdbPlugin.PLUGIN_ID + ".AUTO_SOLIB_LIST"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to enable reverse debugging at launch time.
	 * @since 2.0
	 */
	public static final String ATTR_DEBUGGER_REVERSE = GdbPlugin.PLUGIN_ID + ".REVERSE"; //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute key. Boolean value. See
	 * IGDBBackend.getUpdateThreadListOnSuspend()
	 * 
	 * @since 3.0
	 */
	public static final String ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND = GdbPlugin.PLUGIN_ID + ".UPDATE_THREADLIST_ON_SUSPEND"; //$NON-NLS-1$	

	/**         
	 * Launch configuration attribute key. The value is a String specifying the type of post mortem launch.
	 * @since 3.0
	 */     
	public static final String ATTR_DEBUGGER_POST_MORTEM_TYPE = GdbPlugin.PLUGIN_ID + ".POST_MORTEM_TYPE"; //$NON-NLS-1$
	
	
	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUG_NAME.
	 */
	public static final String DEBUGGER_DEBUG_NAME_DEFAULT = "gdb"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is ATTR_GDB_INIT.
	 */
	public static final String DEBUGGER_GDB_INIT_DEFAULT = ".gdbinit"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_NON_STOP.
	 * @since 1.1
	 */
	public static final boolean DEBUGGER_NON_STOP_DEFAULT = false;

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_AUTO_SOLIB.
	 */
	public static final boolean DEBUGGER_AUTO_SOLIB_DEFAULT = true;

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP.
     * @since 1.1
	 */
	public static final boolean DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP_DEFAULT = false;

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_REVERSE.
	 * @since 2.0
	 */
	public static final boolean DEBUGGER_REVERSE_DEFAULT = false;

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND
	 * 
	 * @since 3.0
	 */
	public static final boolean DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND_DEFAULT = false;
	
	/**  
	 * Possible attribute value for the key is ATTR_DEBUGGER_POST_MORTEM_TYPE.
	 * Indicates a core file.
	 *   
	 * @since 3.0                        
	 */                                                 
	public static final String DEBUGGER_POST_MORTEM_CORE_FILE = "CORE_FILE"; //$NON-NLS-1$

	/**
	 * Possible attribute value for the key is ATTR_DEBUGGER_POST_MORTEM_TYPE.
	 * Indicates a trace data file.
	 *      
	 * @since 3.0 
	 */     
	public static final String DEBUGGER_POST_MORTEM_TRACE_FILE = "TRACE_FILE"; //$NON-NLS-1$

	/**  
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_POST_MORTEM_TYPE.
	 * @since 3.0
	 */
	public static final String DEBUGGER_POST_MORTEM_TYPE_DEFAULT = DEBUGGER_POST_MORTEM_CORE_FILE;

}
