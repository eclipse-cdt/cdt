/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core;

import org.eclipse.cdt.codan.internal.core.CheckersTimeStats;
import org.eclipse.cdt.codan.internal.core.CodeAnalysisNature;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class CodanCorePlugin extends Plugin {
	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.cdt.codan.core"; //$NON-NLS-1$
	/**
	 * The nature used to run Codan builder.
	 * @noreference This constant is not intended to be referenced by clients.
	 */
	public static final String NATURE_ID = CodeAnalysisNature.NATURE_ID;
	// The shared instance
	private static CodanCorePlugin plugin;
	private static DebugTrace trace;
	private static DebugOptions debugOptions;

	/**
	 * The constructor
	 */
	public CodanCorePlugin() {
	}

	/**
	 * @return the preferences node for this plug-in.
	 */
	public IEclipsePreferences getStorePreferences() {
		return InstanceScope.INSTANCE.getNode(PLUGIN_ID);
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		if (isDebuggingEnabled("/debug/performance")) { //$NON-NLS-1$
			CheckersTimeStats.getInstance().setEnabled(true);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CodanCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 *
	 * @param status
	 *        status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Logs an internal error with the specified throwable
	 *
	 * @param e the exception to be logged
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(Throwable e) {
		if (Boolean.valueOf(System.getProperty("codan.rethrow"))) //$NON-NLS-1$
			throw new RuntimeException(e);
		log("Internal Error", e); //$NON-NLS-1$
	}

	/**
	 * Logs an internal error with the specified message and throwable
	 *
	 * @param message the error message to log
	 * @param e the exception to be logged
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message, Throwable e) {
		log(new Status(IStatus.ERROR, CodanCorePlugin.PLUGIN_ID, 1, message, e));
	}

	/**
	 * Logs an internal error with the specified message.
	 *
	 * @param message the error message to log
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, null));
	}

	private final boolean isDebuggingEnabled(final String optionPath) {
		if (optionPath == null)
			return true;
		if (debugOptions == null)
			getTrace();
		if (debugOptions == null)
			return false;
		boolean debugEnabled = false;
		if (debugOptions.isDebugEnabled()) {
			final String option = getDefault().getBundle().getSymbolicName() + optionPath;
			debugEnabled = debugOptions.getBooleanOption(option, false);
		}
		return debugEnabled;
	}

	/**
	 * Use a no-op trace when a real one isn't available. Simplifies life for
	 * clients; no need to check for null.
	 */
	private static final DebugTrace NULL_TRACE = new DebugTrace() {
		@Override
		public void trace(String option, String message) {
		}

		@Override
		public void trace(String option, String message, Throwable error) {
		}

		@Override
		public void traceDumpStack(String option) {
		}

		@Override
		public void traceEntry(String option) {
		}

		@Override
		public void traceEntry(String option, Object methodArgument) {
		}

		@Override
		public void traceEntry(String option, Object[] methodArguments) {
		}

		@Override
		public void traceExit(String option) {
		}

		@Override
		public void traceExit(String option, Object result) {
		}
	};

	synchronized private static DebugTrace getTrace() {
		if (trace == null) {
			Plugin plugin = getDefault();
			if (plugin != null) {
				Bundle bundle = plugin.getBundle();
				if (bundle != null) {
					BundleContext context = bundle.getBundleContext();
					if (context != null) {
						ServiceTracker<DebugOptions, DebugOptions> tracker = new ServiceTracker<>(context,
								DebugOptions.class.getName(), null);
						try {
							tracker.open();
							debugOptions = tracker.getService();
							if (debugOptions != null) {
								trace = debugOptions.newDebugTrace(bundle.getSymbolicName());
							}
						} finally {
							tracker.close();
						}
					}
				}
			}

		}
		return trace != null ? trace : NULL_TRACE;
	}
}
