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

/**
 * Hooks our debug options to the Platform trace functonality.
 * In essence, we can open Window -> Preferences -> Tracing
 * and turn on debug options for this package. The debug output
 * will come out on the console and can be saved directly to 
 * a file. Classes that need to be debugged can call into 
 * DsfDebugOptions to get debug flags. If new flags need to be
 * created, they will need to have a unique identifier and added to
 * the .options file in this plugin
 * 
 *
 */
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
	
    /** 
     * Flag indicating that tracing of the DSF executor is enabled.  It enables
     * storing of the "creator" information as well as tracing of disposed
     * runnables that have not been submitted to the executor.  
     */
	public static boolean DEBUG_EXECUTOR = false;
	
	public static String DEBUG_EXECUTOR_NAME = ""; //$NON-NLS-1$
	
	/**
	 * Flag indicating that monitor objects should be instrumented. A monitor is
	 * an object that is usually constructed as an anonymous inner classes and
	 * is used when making an asynchronous call--one that needs to return some
	 * result or at least notify its caller when it has completed. These objects
	 * usually end up getting chained together at runtime, forming what is
	 * effectively a very disjointed code path. When this trace option is
	 * enabled, these objects are given a String field at construction time that
	 * contains the instantiation backtrace. This turns out to be a fairly
	 * dependable alternative to the standard program stack trace, which is of
	 * virtually no help when debugging asynchronous, monitor-assisted code.
	 */
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
		DEBUG_EXECUTOR = DEBUG && options.getBooleanOption(DEBUG_EXECUTOR_FLAG, false);
		DEBUG_EXECUTOR_NAME = DEBUG ? options.getOption(DEBUG_EXECUTOR_NAME_FLAG, "") : ""; //$NON-NLS-1$ //$NON-NLS-2$
		DEBUG_MONITORS = DEBUG && options.getBooleanOption(DEBUG_MONITORS_FLAG, false);
		DEBUG_CACHE = DEBUG && options.getBooleanOption(DEBUG_CACHE_FLAG, false);
		DEBUG_SESSION = DEBUG && options.getBooleanOption(DEBUG_SESSION_FLAG, false);
		DEBUG_SESSION_LISTENERS = DEBUG_SESSION && options.getBooleanOption(DEBUG_SESSION_LISTENERS_FLAG, false);
		DEBUG_SESSION_DISPATCHES = DEBUG_SESSION && options.getBooleanOption(DEBUG_SESSION_DISPATCHES_FLAG, false);
		DEBUG_SESSION_MODEL_ADAPTERS = DEBUG_SESSION && options.getBooleanOption(DEBUG_SESSION_MODEL_ADAPTERS_FLAG, false);
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if started)
	 * @param option the option or <code>null</code>
	 * @param message the message to print or <code>null</code>
	 * @param throwable the {@link Throwable} or <code>null</code>
	 */
	public static void trace(String option, String message, Throwable throwable) {
		if (DEBUG) {
			//print to console
			System.out.println(message);
		}
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
