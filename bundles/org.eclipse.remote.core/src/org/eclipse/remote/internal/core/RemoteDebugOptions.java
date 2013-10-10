/*******************************************************************************
 * Copyright (c) 2012 Sage Electronic Engineering, LLC. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jason Litton (Sage Electronic Engineering, LLC) - initial API and implementation
 *    Greg Watson (IBM) - adapted for remote core
 *******************************************************************************/

package org.eclipse.remote.internal.core;

import java.util.Hashtable;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;

/**
 * Hooks our debug options to the Platform trace functonality.
 * In essence, we can open Window -> Preferences -> Tracing
 * and turn on debug options for this package. The debug output
 * will come out on the console and can be saved directly to
 * a file. Classes that need to be debugged can call into
 * RemoteDebugOptions to get debug flags. If new flags need to be
 * created, they will need to have a unique identifier and added to
 * the .options file in this plugin
 */
public class RemoteDebugOptions implements DebugOptionsListener {

	public static final String DEBUG_REMOTE_COMMANDS = "/debug/commands"; //$NON-NLS-1$

	private static DebugTrace fDebugTrace;
	private static DebugOptions fDebugOptions;
	private static RemoteDebugOptions fRemoteDebugOptions;

	public static void configure(BundleContext context) {
		if (fRemoteDebugOptions == null) {
			fRemoteDebugOptions = new RemoteDebugOptions(context);
		}
	}

	private RemoteDebugOptions(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(DebugOptions.LISTENER_SYMBOLICNAME, RemoteCorePlugin.getUniqueIdentifier());
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.osgi.service.debug.DebugOptionsListener#optionsChanged(org.eclipse.osgi.service.debug.DebugOptions)
	 */
	@Override
	public void optionsChanged(DebugOptions options) {
		fDebugOptions = options;
		fDebugTrace = options.newDebugTrace(RemoteCorePlugin.getUniqueIdentifier());
	}

	public static boolean isDebugging() {
		return RemoteCorePlugin.getDefault().isDebugging();
	}

	public static boolean isDebugging(String option) {
		if (fDebugOptions == null) {
			return false;
		}
		return fDebugOptions.getBooleanOption(RemoteCorePlugin.getUniqueIdentifier() + option, false);
	}

	public static void setDebugging(String option, boolean value) {
		if (fDebugOptions != null) {
			if (value) {
				fDebugOptions.setDebugEnabled(true);
			}
			fDebugOptions.setOption(option, Boolean.toString(value));
		}
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if enabled)
	 * 
	 * @param message
	 *            the message or <code>null</code>
	 */
	public static void trace(String message) {
		trace(null, message);
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if enabled)
	 * 
	 * @param option
	 *            the option to determine if tracing is displayed
	 * @param message
	 *            the message or <code>null</code>
	 * @param arguments
	 *            optional arguments for the message or <code>null</code>
	 */
	public static void trace(String option, String message, String... arguments) {
		String traceMsg = message;
		if (arguments.length > 0) {
			traceMsg = NLS.bind(message, arguments);
		}
		if ((option != null && isDebugging(option)) || isDebugging()) {
			System.out.println(traceMsg);
			if (fDebugTrace != null) {
				fDebugTrace.trace(option, traceMsg, null);
			}
		}
	}

}
