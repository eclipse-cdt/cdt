/*******************************************************************************
 * Copyright (c) 201 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Class for getting a singleton instance of DebugTrace, an extremely useful
 * utility for troubleshooting race condition bugs. Each trace statement
 * contains the thread and the time (in millisecond granularity). The trace goes
 * to the file: {workspace}/.metadata/trace.log
 * 
 * <p>
 * For performance reasons, trace statements should explicitly check a trace
 * flag before calling into a DebugTrace method. E.g.,
 * 
 * <p>
 * <code>
 * if (Trace.DEBUG_EXECUTABLES) DebugTrace.getTrace.trace(null, ...);
 * </code>
 * 
 * <p>
 * The alternative is to have DebugTrace check the debug option for you (i.e.,
 * don't pass null for first param), but this incurs a relatively heavy price
 * considering the trace statements are present in release code. An advantage of
 * asking DebugTrace to do the check is that is that it supports trace options
 * changing during the workbench lifetime. However that's an unlikely and
 * esoteric scenario.
 * 
 * <p>
 * This class is also a central location for trace flags. They are public static
 * fields, so checking them in trace statements is very efficient. They are set
 * at plugin startup.
 * 
 * <p>
 * DebugTrace objects are particular to a plugin. Plugins can reuse most of this
 * class definition. However, since it's all based on static methods and fields,
 * reuse means copy-n-paste. Making this not rely on statics would complicate
 * things and simplicity and efficiency is what we need most when it comes to
 * trace. When making a copy of this class for your plugin, make sure to update
 * the Activator class reference (appears three times). Also, the DEBUG_XXXX
 * fields will need to be whatever options are used in your plugin.
 */
public class Trace {
	// See .options file in plugin for description of these flags. DEBUG is the base option.
	public static boolean DEBUG;
	public static boolean DEBUG_EXECUTABLES;

	/**
	 * Use a no-op trace when a real one isn't available. Simplifies life for
	 * clients; no need to check for null.
	 */
	private static final DebugTrace NULL_TRACE = new DebugTrace() {
		@Override
		public void trace(String option, String message) {}
		@Override
		public void trace(String option, String message, Throwable error) {}
		@Override
		public void traceDumpStack(String option) {}
		@Override
		public void traceEntry(String option) {}
		@Override
		public void traceEntry(String option, Object methodArgument) {}
		@Override
		public void traceEntry(String option, Object[] methodArguments) {}
		@Override
		public void traceExit(String option) {}
		@Override
		public void traceExit(String option, Object result) {}
	};

	/** Should be called by plugin's  startup method() */
	public static void init() {
		DEBUG = CDebugCorePlugin.getDefault().isDebugging();

		String option = Platform.getDebugOption(CDebugCorePlugin.PLUGIN_ID + "/debug/executables"); //$NON-NLS-1$
		DEBUG_EXECUTABLES = DEBUG && ((option != null) ? option.equalsIgnoreCase("true") : false); //$NON-NLS-1$
	}
	/** Singleton trace object */
	private static DebugTrace trace;

	/**
	 * Gets the singleton trace object, or a null trace object if a real one
	 * isn't available
	 * 
	 * @return trace object; never null
	 */
	synchronized public static DebugTrace getTrace() {
		if (trace == null) {
			Plugin plugin = CDebugCorePlugin.getDefault();
			if (plugin != null) {
				Bundle bundle = plugin.getBundle();
				if (bundle != null) {
					BundleContext context = bundle.getBundleContext();
					if (context != null) {
						ServiceTracker<DebugOptions, DebugOptions> tracker = new ServiceTracker<DebugOptions, DebugOptions>(context, DebugOptions.class.getName(), null);
						try {
							tracker.open();
							DebugOptions debugOptions = tracker.getService();
							if (debugOptions != null) {
								trace = debugOptions.newDebugTrace(bundle.getSymbolicName());
							}
						}
						finally {
							tracker.close();
						}
					}
				}
			}

		}
		return trace != null ? trace : NULL_TRACE;
	}
}
