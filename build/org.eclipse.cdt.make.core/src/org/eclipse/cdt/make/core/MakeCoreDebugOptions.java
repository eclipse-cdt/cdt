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

package org.eclipse.cdt.make.core;

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
 * GdbDebugOptions to get debug flags. If new flags need to be
 * created, they will need to have a unique identifier and added to
 * the .options file in this plugin
 * 
 * @since 7.3
 *
 */
public class MakeCoreDebugOptions implements DebugOptionsListener {
	
	private static final String DEBUG_FLAG = "org.eclipse.cdt.make.core/debug"; //$NON-NLS-1$
	private static final String DEBUG_SCANNER_CONFIG_FLAG = "org.eclipse.cdt.make.core/debug/scdiscovery"; //$NON-NLS-1$
	
	public static boolean DEBUG = false;
	public static boolean DEBUG_SCANNER_CONFIG = false;
	
	/**
	 * The {@link DebugTrace} object to print to OSGi tracing
	 */
	private static DebugTrace fgDebugTrace;
	
	/**
	 * Constructor
	 */
	public MakeCoreDebugOptions(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, MakeCorePlugin.getUniqueIdentifier());
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}


	@Override
	public void optionsChanged(DebugOptions options) {
		fgDebugTrace = options.newDebugTrace(MakeCorePlugin.getUniqueIdentifier());
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
		DEBUG_SCANNER_CONFIG = options.getBooleanOption(DEBUG_SCANNER_CONFIG_FLAG, false);
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if started)
	 * @param option the option or <code>null</code>
	 * @param message the message to print or <code>null</code>
	 * @param throwable the {@link Throwable} or <code>null</code>
	 */
	public static void trace(String option, String message, Throwable throwable) {
		//print message to console
		System.out.println(message);
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

