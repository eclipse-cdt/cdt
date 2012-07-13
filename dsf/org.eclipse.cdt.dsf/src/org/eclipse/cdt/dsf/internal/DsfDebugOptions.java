/*******************************************************************************
 * Copyright (c) 2012 Sage Electronic Engineering, LLC. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jason Litton (Sage Electronic Engineering, LLC) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.internal;

import java.util.Hashtable;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;

public class DsfDebugOptions implements DebugOptionsListener {

	private static final String DEBUG_FLAG = "org.eclipse.cdt.dsf/debug"; //$NON-NLS-1$
	private static final String DEBUG_EXECUTOR_FLAG = "org.eclipse.cdt.dsf/debug/executor"; //$NON-NLS-1$
	private static final String DEBUG_EXECUTOR_NAME_FLAG = "org.eclipse.cdt.dsf/debug/executorName"; //$NON-NLS-1$
	private static final String DEBUG_MONITORS_FLAG = "org.eclipse.cdt.dsf/debug/monitors"; //$NON-NLS-1$
	private static final String DEBUG_CACHE_FLAG = "org.eclipse.cdt.dsf/debugCache"; //$NON-NLS-1$
	private static final String DEBUG_SESSION_FLAG = "org.eclipse.cdt.dsf/debug/session"; //$NON-NLS-1$
	private static final String DEBUG_SESSION_LISTENERS_FLAG = "org.eclipse.cdt.dsf/debug/session/listeners"; //$NON-NLS-1$
	private static final String DEBUG_SESSION_DISPATCHES_FLAG = "org.eclipse.cdt.dsf/debug/session/dispatches"; //$NON-NLS-1$
	private static final String DEBUG_SESSION_MODEL_ADAPTERS_FLAG = "org.eclipse.cdt.dsf/debug/session/modelAdapters"; //$NON-NLS-1$

	public static boolean DEBUG = false;
	public static boolean DEBUG_EXECUTOR = false;
	public static String DEBUG_EXECUTOR_NAME = ""; //$NON-NLS-1$
	public static boolean DEBUG_MONITORS = false;
	public static boolean DEBUG_CACHE = false;
	public static boolean DEBUG_SESSION = false;
	public static boolean DEBUG_SESSION_LISTENERS = false;
	public static boolean DEBUG_SESSION_DISPATCHES = false;
	public static boolean DEBUG_SESSION_MODEL_ADAPTERS = false;

	/**
	 * The {@link DebugTrace} object to print to OSGi tracing
	 */
	private static DebugTrace fgDebugTrace;

	/**
	 * Constructor
	 */
	public DsfDebugOptions(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, DsfPlugin.PLUGIN_ID);
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}


	@Override
	public void optionsChanged(DebugOptions options) {
		fgDebugTrace = options.newDebugTrace(DsfPlugin.PLUGIN_ID);
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
		DEBUG_EXECUTOR = options.getBooleanOption(DEBUG_EXECUTOR_FLAG, false);
		DEBUG_EXECUTOR_NAME = options.getOption(DEBUG_EXECUTOR_NAME_FLAG, ""); //$NON-NLS-1$
		DEBUG_MONITORS = options.getBooleanOption(DEBUG_MONITORS_FLAG, false);
		DEBUG_CACHE = options.getBooleanOption(DEBUG_CACHE_FLAG, false);
		DEBUG_SESSION = options.getBooleanOption(DEBUG_SESSION_FLAG, false);
		DEBUG_SESSION_LISTENERS = options.getBooleanOption(DEBUG_SESSION_LISTENERS_FLAG, false);
		DEBUG_SESSION_DISPATCHES = options.getBooleanOption(DEBUG_SESSION_DISPATCHES_FLAG, false);
		DEBUG_SESSION_MODEL_ADAPTERS = options.getBooleanOption(DEBUG_SESSION_MODEL_ADAPTERS_FLAG, false);
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if started)
	 * @param option the option or <code>null</code>
	 * @param message the message to print or <code>null</code>
	 * @param throwable the {@link Throwable} or <code>null</code>
	 */
	public static void trace(String option, String message, Throwable throwable) {
		//divide the string into substrings of 100 chars or less for printing
		//to console
		String systemPrintableMessage = message; 
		while (systemPrintableMessage.length() > 100) {
			String partial = systemPrintableMessage.substring(0, 100); 
			systemPrintableMessage = systemPrintableMessage.substring(100);
			System.out.println(partial + "\\"); //$NON-NLS-1$
		}
		System.out.println(systemPrintableMessage);
		//then pass the original message to be traced into a file
		if(fgDebugTrace != null) {
			fgDebugTrace.trace(option, message, throwable);
		}
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if enabled)
	 *
	 * @param message the message or <code>null</code>
	 */
	public static void trace(String message) {
		trace(null, message, null);
	}
}
