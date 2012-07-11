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

package org.eclipse.cdt.debug.internal.core;

import java.util.Hashtable;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;

public class CdtDebugCoreDebugOptions implements DebugOptionsListener{
	
	private static final String DEBUG_FLAG = "org.eclipse.cdt.debug.core/debug"; //$NON-NLS-1$
	private static final String DEBUG_EXECUTABLES_FLAG = "org.eclipse.cdt.debug.core/debug/executables"; //$NON-NLS-1$
	
	public static boolean DEBUG = false;
	public static boolean DEBUG_EXECUTABLES = false;
	
	/**
	 * The {@link DebugTrace} object to print to OSGi tracing
	 */
	private static DebugTrace fgDebugTrace;
	
	/**
	 * Constructor
	 */
	public CdtDebugCoreDebugOptions(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, CDebugCorePlugin.getUniqueIdentifier());
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}


	@Override
	public void optionsChanged(DebugOptions options) {
		fgDebugTrace = options.newDebugTrace(CDebugCorePlugin.getUniqueIdentifier());
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
		DEBUG_EXECUTABLES = DEBUG && options.getBooleanOption(DEBUG_EXECUTABLES_FLAG, false);
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
	
	public static void traceEntry(String option) {
		fgDebugTrace.traceEntry(option);
	}
	
	public static void traceEntry(String option, Object methodArgument) {
		fgDebugTrace.traceEntry(option, methodArgument);
	}
	
	public static void traceEntry(String option, Object[] methodArguments) {
		fgDebugTrace.traceEntry(option, methodArguments);
	}
	
	public static void traceExit(String option) {
		fgDebugTrace.traceExit(option);
	}
	
	public static void traceExit(String option, Object result) {
		fgDebugTrace.traceExit(option, result);
	}
}
