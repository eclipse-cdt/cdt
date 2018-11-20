/*******************************************************************************
 * Copyright (c) 2008, 2018  Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc Khouzam (Ericsson) - Support for fast tracepoints (Bug 346320)
 *     Anton Gorenkov - Need to use a process factory (Bug 210366)
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;

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
	public static final String ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP = GdbPlugin.PLUGIN_ID
			+ ".USE_SOLIB_SYMBOLS_FOR_APP"; //$NON-NLS-1$

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
	 * Launch configuration attribute key. String value to select reverse debugging method at launch time.
	 * @since 5.0
	 */
	public static final String ATTR_DEBUGGER_REVERSE_MODE = GdbPlugin.PLUGIN_ID + ".REVERSE_MODE"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value. See
	 * IGDBBackend.getUpdateThreadListOnSuspend()
	 *
	 * @since 3.0
	 */
	public static final String ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND = GdbPlugin.PLUGIN_ID
			+ ".UPDATE_THREADLIST_ON_SUSPEND"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a String specifying the type of post mortem launch.
	 * @since 3.0
	 */
	public static final String ATTR_DEBUGGER_POST_MORTEM_TYPE = GdbPlugin.PLUGIN_ID + ".POST_MORTEM_TYPE"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the 'detach-on-fork' GDB option.
	 * When detach-on-fork is off, we will automatically attach to forked processes.  This will yield
	 * a multi-process session, which is supported with GDB >= 7.2
	 * Note that detach-on-fork == !ATTR_DEBUGGER_DEBUG_ON_FORK
	 * @since 4.0
	 */
	public static final String ATTR_DEBUGGER_DEBUG_ON_FORK = GdbPlugin.PLUGIN_ID + ".DEBUG_ON_FORK"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. Boolean value to set the 'new-console' GDB option.
	 * @since 5.4
	 */
	public static final String ATTR_DEBUGGER_EXTERNAL_CONSOLE = GdbPlugin.PLUGIN_ID + ".EXTERNAL_CONSOLE"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a String specifying the type of Tracepoint mode
	 * that should be used for this launch.
	 * @since 4.1
	 */
	public static final String ATTR_DEBUGGER_TRACEPOINT_MODE = GdbPlugin.PLUGIN_ID + ".TRACEPOINT_MODE"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a String specifying the path of the executable
	 * on the target.
	 * @since 4.2
	 */
	public static final String ATTR_DEBUGGER_REMOTE_BINARY = GdbPlugin.PLUGIN_ID + ".REMOTE_BINARY"; //$NON-NLS-1$

	/**
	 * Enablement setting to set Remote Timeout in GDB to (set remotetimeout num).
	 * The value to use is in {@link #ATTR_DEBUGGER_REMOTE_TIMEOUT_VALUE}
	 *
	 * @since 5.5
	 */
	public static final String ATTR_DEBUGGER_REMOTE_TIMEOUT_ENABLED = GdbPlugin.PLUGIN_ID + ".REMOTE_TIMEOUT_ENABLED"; //$NON-NLS-1$

	/**
	 * Setting to set Remote Timeout in GDB to (set remotetimeout num) if enabled
	 * with {@link #ATTR_DEBUGGER_REMOTE_TIMEOUT_ENABLED} The value is a string and
	 * does not have to be a number (but it normally is).
	 *
	 * @since 5.5
	 */
	public static final String ATTR_DEBUGGER_REMOTE_TIMEOUT_VALUE = GdbPlugin.PLUGIN_ID + ".REMOTE_TIMEOUT_VALUE"; //$NON-NLS-1$

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
	 * Possible attribute value for the key is ATTR_DEBUGGER_REVERSE_MODE.
	 * @since 5.0
	 */
	public static final String DEBUGGER_REVERSE_MODE_SOFTWARE = "UseSoftTrace"; //$NON-NLS-1$

	/**
	 * Possible attribute value for the key is ATTR_DEBUGGER_REVERSE_MODE.
	 * @since 5.0
	 */
	public static final String DEBUGGER_REVERSE_MODE_HARDWARE = "UseHardTrace"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_REVERSE_MODE.
	 * @since 5.0
	 */
	public static final String DEBUGGER_REVERSE_MODE_DEFAULT = DEBUGGER_REVERSE_MODE_SOFTWARE;

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

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_DEBUG_ON_FORK.
	 * @since 4.0
	 */
	public static final boolean DEBUGGER_DEBUG_ON_FORK_DEFAULT = false;

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_EXTERNAL_CONSOLE.
	 * @since 5.4
	 */
	public static final boolean DEBUGGER_EXTERNAL_CONSOLE_DEFAULT = false;

	/**
	 * Possible attribute value for the key is ATTR_DEBUGGER_TRACEPOINT_MODE.
	 * Indicates that only normal tracepoints should be used.
	 * @since 4.1
	 */
	public static final String DEBUGGER_TRACEPOINT_NORMAL_ONLY = "TP_NORMAL_ONLY"; //$NON-NLS-1$

	/**
	 * Possible attribute value for the key is ATTR_DEBUGGER_TRACEPOINT_MODE.
	 * Indicates that only fast tracepoints should be used.
	 * @since 4.1
	 */
	public static final String DEBUGGER_TRACEPOINT_FAST_ONLY = "TP_FAST_ONLY"; //$NON-NLS-1$

	/**
	 * Possible attribute value for the key is ATTR_DEBUGGER_TRACEPOINT_MODE.
	 * Indicates that normal tracepoints should be used whenever a fast tracepoint
	 * cannot be inserted.
	 * @since 4.1
	 */
	public static final String DEBUGGER_TRACEPOINT_FAST_THEN_NORMAL = "TP_FAST_THEN_NORMAL"; //$NON-NLS-1$

	/**
	 * Default attribute value for the key is ATTR_DEBUGGER_TRACEPOINT_MODE.
	 * @since 4.1
	 */
	public static final String DEBUGGER_TRACEPOINT_MODE_DEFAULT = DEBUGGER_TRACEPOINT_NORMAL_ONLY;

	/**
	 * The default value of DebugPlugin.ATTR_PROCESS_FACTORY_ID.
	 * @since 4.1
	 */
	// Bug 210366
	public static final String DEBUGGER_ATTR_PROCESS_FACTORY_ID_DEFAULT = "org.eclipse.cdt.dsf.gdb.GdbProcessFactory"; //$NON-NLS-1$

	/**
	 * The default value of {@link #ATTR_DEBUGGER_REMOTE_TIMEOUT_ENABLED}. To get the user set workspace default see
	 * {@link LaunchUtils#getRemoteTimeoutEnabledDefault()}
	 * @since 5.5
	 */
	public static final boolean DEBUGGER_REMOTE_TIMEOUT_ENABLED_DEFAULT = false;

	/**
	 * The default value of {@link #ATTR_DEBUGGER_REMOTE_TIMEOUT_VALUE}. To get the user set workspace default see
	 * {@link LaunchUtils#getRemoteTimeoutValueDefault()}
	 * @since 5.5
	 */
	public static final String DEBUGGER_REMOTE_TIMEOUT_VALUE_DEFAULT = ""; //$NON-NLS-1$
}
