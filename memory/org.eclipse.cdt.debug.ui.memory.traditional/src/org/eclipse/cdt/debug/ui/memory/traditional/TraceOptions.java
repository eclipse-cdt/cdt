/*******************************************************************************
 * Copyright (c) 2015 Freescale, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Teodor Madan (Freescale) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.memory.traditional;


import java.util.Hashtable;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;

/**
 * Hooks debug options to the Platform trace functionality.
 * In essence, we can open Window -> Preferences -> Tracing
 * and turn on debug options for this package. The debug output
 * will come out on the console and can be saved directly to 
 * a file. Classes that need to be debugged can call into 
 * TraceOptions to get debug flags. If new flags need to be
 * created, they will need to have a unique identifier and added to
 * the .options file in this plugin
 * 
 */
class TraceOptions implements DebugOptionsListener {

	private static final String DEBUG_FLAG = "org.eclipse.cdt.debug.ui.memory.traditional/debug"; //$NON-NLS-1$

	public static boolean DEBUG = false;

	/**
	 * The {@link DebugTrace} object to print to OSGi tracing
	 */
	private static DebugTrace fgDebugTrace;
	private String pluginID;

	/**
	 * Constructor
	 */
	public TraceOptions(BundleContext context, String pluginID) {
		this.pluginID = pluginID;
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, pluginID);
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}


	@Override
	public void optionsChanged(DebugOptions options) {
		fgDebugTrace = options.newDebugTrace(pluginID);
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if started)
	 * @param option the option or <code>null</code>
	 * @param message the message to print or <code>null</code>
	 * @param throwable the {@link Throwable} or <code>null</code>
	 */
	public static void trace(String option, String message, Throwable throwable) {
		//divide the string into substrings of 160 chars or less for printing
		//to console
		String systemPrintableMessage = message;
		while (systemPrintableMessage.length() > 160) {
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
