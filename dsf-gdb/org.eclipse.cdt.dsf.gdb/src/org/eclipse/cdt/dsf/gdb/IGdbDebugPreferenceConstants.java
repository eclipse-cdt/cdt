/*******************************************************************************
 * Copyright (c) 2009, 2018 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Sergey Prigogin (Google)
 *     Anton Gorenkov - A preference to use RTTI for variable types determination (Bug 377536)
 *     Marc Khouzam (Ericsson) - Add preference for aggressive breakpoint filtering (Bug 360735)
 *     Intel Corporation - Added Reverse Debugging BTrace support
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
	 * Boolean preference whether to enable the max number of lines for messages.
	 * Default is {@value #MAX_MI_OUTPUT_LINES_ENABLE_DEFAULT}.
	 * @since 5.6
	 */
	public static final String PREF_MAX_MI_OUTPUT_LINES_ENABLE = "maxMiOutputLinesEnable"; //$NON-NLS-1$

	/**
	 * The default for whether {@link #PREF_MAX_MI_OUTPUT_LINES} is enabled.
	 * @since 5.6
	 */
	public static final boolean MAX_MI_OUTPUT_LINES_ENABLE_DEFAULT = true;

	/**
	 * The maximum number of lines a single message can be. Default is {@value #MAX_MI_OUTPUT_LINES_DEFAULT}.
	 * @since 5.6
	 */
	public static final String PREF_MAX_MI_OUTPUT_LINES = "maxMiOutputLines"; //$NON-NLS-1$

	/**
	 * The default maximum number of lines a single message can be.
	 * @since 5.6
	 */
	public static final int MAX_MI_OUTPUT_LINES_DEFAULT = 5;

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
	 * Boolean preference whether to use new-console. Default is
	 * {@link IGDBLaunchConfigurationConstants#DEBUGGER_EXTERNAL_CONSOLE_DEFAULT}
	 *
	 * @since 5.4
	 */
	public static final String PREF_EXTERNAL_CONSOLE = PREFIX + "externalConsole"; //$NON-NLS-1$

	/**
	 * Boolean preference whether to hide or not, the running threads in the debug view.
	 * Default is <code>false</code>.
	 *
	 * @since 4.1
	 */
	public static final String PREF_HIDE_RUNNING_THREADS = PREFIX + "hideRunningThreads"; //$NON-NLS-1$

	/**
	 * Boolean preference whether to use the new behavior of the command
	 * "Show Breakpoints Supported by Selected Target" from the Breakpoints view.
	 * The original behavior is to only show breakpoints that apply to the current debug
	 * session; so all C/C++ breakpoints but not Java ones.
	 * The new behavior is to only show breakpoints that are actually installed in the current
	 * debug session.
	 *
	 * Default is <code>true</code>.
	 *
	 * @since 4.2
	 */
	public static final String PREF_AGGRESSIVE_BP_FILTER = PREFIX + "aggressiveBpFilter"; //$NON-NLS-1$

	/**
	 * String preference controlling trace method used for hardware tracing.
	 * @since 5.0
	 */
	public static final String PREF_REVERSE_TRACE_METHOD_HARDWARE = PREFIX + ".reversedebugpref.tracemethodHardware"; //$NON-NLS-1$

	/**
	 * String preference controlling trace method used for hardware tracing.
	 * @since 5.0
	 */
	public static final String PREF_REVERSE_TRACE_METHOD_GDB_TRACE = "UseGdbTrace"; //$NON-NLS-1$

	/**
	* String preference controlling trace method used for hardware tracing.
	* @since 5.0
	*/
	public static final String PREF_REVERSE_TRACE_METHOD_BRANCH_TRACE = "UseBranchTrace"; //$NON-NLS-1$

	/**
	* String preference controlling trace method used for hardware tracing.
	* @since 5.0
	*/
	public static final String PREF_REVERSE_TRACE_METHOD_PROCESSOR_TRACE = "UseProcessorTrace"; //$NON-NLS-1$

	/**
	 * Preference key controlling the coloring of GDB CLI consoles
	 * @since 5.2
	 */
	public static final String PREF_CONSOLE_INVERTED_COLORS = PREFIX + "consoleInvertedColors"; //$NON-NLS-1$

	/**
	 * Default preference value for the colors used by GDB CLI consoles
	 * @since 5.2
	 */
	public static final Boolean CONSOLE_INVERTED_COLORS_DEFAULT = false;

	/**
	 * Preference key controlling the number of buffered lines used by GDB CLI consoles
	 * @since 5.2
	 */
	public static final String PREF_CONSOLE_BUFFERLINES = PREFIX + "consoleBufferLines"; //$NON-NLS-1$

	/**
	 * Default preference value for the number of buffered lines used by GDB CLI consoles
	 * @since 5.2
	 */
	public static final int CONSOLE_BUFFERLINES_DEFAULT = 1000;

	/**
	 * The value is a boolean specifying the default for whether to issue "set
	 * remotetimout" with the value being {@link #PREF_DEFAULT_REMOTE_TIMEOUT_VALUE}
	 *
	 * @since 5.5
	 */
	public static final String PREF_DEFAULT_REMOTE_TIMEOUT_ENABLED = "defaultRemoteTimeoutEnabled"; //$NON-NLS-1$

	/**
	 * The value, if enabled with {@link #PREF_DEFAULT_REMOTE_TIMEOUT_ENABLED}, the
	 * value for GDB "set remotetimout"
	 *
	 * @since 5.5
	 */
	public static final String PREF_DEFAULT_REMOTE_TIMEOUT_VALUE = "defaultRemoteTimeoutValue"; //$NON-NLS-1$
}
