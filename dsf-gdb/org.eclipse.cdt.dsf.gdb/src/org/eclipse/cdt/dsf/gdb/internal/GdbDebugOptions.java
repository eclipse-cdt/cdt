/*******************************************************************************
 * Copyright (c) 2012, 2014 Sage Electronic Engineering, LLC. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jason Litton (Sage Electronic Engineering, LLC) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal;

import java.util.Hashtable;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;

/**
 * Hooks our debug options to the Platform trace functionality.
 * In essence, we can open Window -> Preferences -> Tracing
 * and turn on debug options for this package. The debug output
 * will come out on the console and can be saved directly to
 * a file. Classes that need to be debugged can call into
 * GdbDebugOptions to get debug flags. If new flags need to be
 * created, they will need to have a unique identifier and added to
 * the .options file in this plugin
 *
 * @since 4.1
 *
 */
public class GdbDebugOptions implements DebugOptionsListener {

	private static final String DEBUG_FLAG = "org.eclipse.cdt.dsf.gdb/debug"; //$NON-NLS-1$
	private static final String DEBUG_TIMEOUTS_FLAG = "org.eclipse.cdt.dsf.gdb/debug/timeouts"; //$NON-NLS-1$

	public static boolean DEBUG = false;
	public static boolean DEBUG_COMMAND_TIMEOUTS = false;

	/**
	 * The {@link DebugTrace} object to print to OSGi tracing
	 */
	private static DebugTrace fgDebugTrace;

	/**
	 * Constructor
	 */
	public GdbDebugOptions(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<>(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, GdbPlugin.getUniqueIdentifier());
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}

	@Override
	public void optionsChanged(DebugOptions options) {
		fgDebugTrace = options.newDebugTrace(GdbPlugin.getUniqueIdentifier());
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
		DEBUG_COMMAND_TIMEOUTS = options.getBooleanOption(DEBUG_TIMEOUTS_FLAG, false);
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if started)
	 * @param option the option or <code>null</code>
	 * @param message the message to print or <code>null</code>
	 * @param throwable the {@link Throwable} or <code>null</code>
	 */
	public static void trace(String option, String message, Throwable throwable) {
		trace(option, message, 100, throwable);
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if started)
	 * @param option the option or <code>null</code>
	 * @param message the message to print or <code>null</code>
	 * @param lineMax the number of character at which point the line should be
	 *        split. Minimum 100.  Negative number to indicate no split.
	 * @param throwable the {@link Throwable} or <code>null</code>
	 */
	public static void trace(String option, String message, int lineMax, Throwable throwable) {
		if (lineMax < 0) {
			lineMax = Integer.MAX_VALUE;
		} else if (lineMax < 100) {
			lineMax = 100;
		}

		//divide the string into substrings of 'lineMax' chars or less for printing to console
		String systemPrintableMessage = message;
		while (systemPrintableMessage.length() > lineMax) {
			String partial = systemPrintableMessage.substring(0, lineMax);
			systemPrintableMessage = systemPrintableMessage.substring(lineMax);
			System.out.println(partial + "\\"); //$NON-NLS-1$
		}
		System.out.print(systemPrintableMessage);
		//then pass the original message to be traced into a file
		if (fgDebugTrace != null) {
			fgDebugTrace.trace(option, message, throwable);
		}
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if enabled)
	 *
	 * @param message the message or <code>null</code>
	 * @param lineMax the number of character at which point the line should be
	 *        split. Minimum 100.  Negative number to indicate no split.
	 */
	public static void trace(String message, int lineMax) {
		trace(null, message, lineMax, null);
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
