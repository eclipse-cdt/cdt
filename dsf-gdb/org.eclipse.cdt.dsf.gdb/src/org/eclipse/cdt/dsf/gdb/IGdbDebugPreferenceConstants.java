/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Sergey Prigogin (Google)
 *     Anton Gorenkov - A preference to use RTTI for variable types determination (Bug 377536)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 2.0
 */
public interface IGdbDebugPreferenceConstants {

	/**
     * Help prefixes.
     */
    public static final String PREFIX = GdbPlugin.PLUGIN_ID + "."; //$NON-NLS-1$
	
	/**
	 * Boolean preference whether to enable GDB traces. Default is <code>true</code>. 
	 */
	public static final String PREF_TRACES_ENABLE = "tracesEnable"; //$NON-NLS-1$

	/**
	 * The maximum number of characters in the GDB trace console.  Default is 500000 characters.
	 * @since 4.0
	 */
	public static final String PREF_MAX_GDB_TRACES = "maxGdbTraces"; //$NON-NLS-1$

	/**
	 * Boolean preference whether to automatically terminate GDB when the inferior exists. Default is <code>true</code>. 
	 */
	public static final String PREF_AUTO_TERMINATE_GDB = "autoTerminateGdb"; //$NON-NLS-1$

	/**
	 * Boolean preference whether to use the advanced Inspect debug text hover. Default is <code>true</code>. 
	 * @since 3.0
	 */
	public static final String PREF_USE_INSPECTOR_HOVER = "useInspectorHover"; //$NON-NLS-1$

	/**
	 * Boolean preference whether to enable pretty printers for MI variable
	 * objects. Default is <code>true</code>.
	 * @since 4.0
	 */
	public static final String PREF_ENABLE_PRETTY_PRINTING = "enablePrettyPrinting"; //$NON-NLS-1$

	/**
	 * The maximum limit of children to be initially fetched by GDB for
	 * collections. Default is 100.
	 * @since 4.0
	 */
	public static final String PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS = "initialChildCountLimitForCollections"; //$NON-NLS-1$
	
	/**
	 * The default command for gdb
	 * @since 4.0
	 */
	public static final String PREF_DEFAULT_GDB_COMMAND = "defaultGdbCommand"; //$NON-NLS-1$
	
	/**
	 * The default command file for gdb
	 * @since 4.0
	 */
	public static final String PREF_DEFAULT_GDB_INIT = "defaultGdbInit"; //$NON-NLS-1$

	/**
	 * The value is a boolean specifying the default for whether to stop at main().
	 * @since 4.0
	 */
	public static final String PREF_DEFAULT_STOP_AT_MAIN = "defaultStopAtMain"; //$NON-NLS-1$

	/**
	 * The value is a string specifying the default symbol to use for the main breakpoint.
	 * @since 4.0
	 */
	public static final String PREF_DEFAULT_STOP_AT_MAIN_SYMBOL = "defaultStopAtMainSymbol"; //$NON-NLS-1$

	/**
	 * The value is a boolean specifying the default for the non-stop debugger mode.
	 * @since 4.0
	 */
	public static final String PREF_DEFAULT_NON_STOP = "defaultNonStop"; //$NON-NLS-1$

	/**
	 * The value is an boolean specifying whether the timeout is used for GDB commands.
	 * @since 4.1
	 */
	public static final String PREF_COMMAND_TIMEOUT = PREFIX + "commandTimeout"; //$NON-NLS-1$

	/**
	 * The value is an integer specifying the timeout value (milliseconds) for GDB commands.
	 * @since 4.1
	 */
	public static final String PREF_COMMAND_TIMEOUT_VALUE = PREFIX + "commandTimeoutValue"; //$NON-NLS-1$

	/**
	 * The value is a string specifying the list of GDB/MI commands with custom timeout values.
	 * @since 4.1
	 */
	public static final String PREF_COMMAND_CUSTOM_TIMEOUTS = PREFIX + "commandCustomTimeouts"; //$NON-NLS-1$

	/**
	 * Default default value for <code>PREF_COMMAND_TIMEOUT</code>;
	 * @since 4.1
	 */
	public static final int COMMAND_TIMEOUT_VALUE_DEFAULT = 10000;

	/**
	 * Boolean preference whether to use RTTI for MI variables type
	 * determination. Default is <code>true</code>.
	 * 
	 * @since 4.1
	 */
	public static final String PREF_USE_RTTI = PREFIX + "useRtti"; //$NON-NLS-1$

	/**
	 * Boolean preference whether to hide or not, the running threads in the debug view.
	 * Default is <code>false</code>.
	 * 
	 * @since 4.1
	 */
	public static final String PREF_HIDE_RUNNING_THREADS = PREFIX + "hideRunningThreads"; //$NON-NLS-1$
}

