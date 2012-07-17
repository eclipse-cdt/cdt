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

package org.eclipse.cdt.ui;

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
 * CUIDebugOptions to get debug flags. If new flags need to be
 * created, they will need to have a unique identifier and added to
 * the .options file in this plugin
 * 
 *
 */
public class CUIDebugOptions implements DebugOptionsListener {

	private static final String DEBUG_FLAG = "org.eclipse.cdt.ui/debug"; //$NON-NLS-1$
	private static final String DEBUG_CONTENT_ASSIST_FLAG = "org.eclipse.cdt.ui/debug/contentassist"; //$NON-NLS-1$
	private static final String DEBUG_SEMANTIC_HIGHLIGHTING_FLAG = "org.eclipse.cdt.ui/debug/SemanticHighlighting"; //$NON-NLS-1$
	private static final String DEBUG_FOLDING_FLAG = "org.eclipse.cdt.ui/debug/folding"; //$NON-NLS-1$
	private static final String DEBUG_RESULT_COLLECTOR_FLAG = "org.eclipse.cdt.ui/debug/ResultCollector"; //$NON-NLS-1$
	private static final String DEBUG_LINE_DELIMITERS_FLAG = "org.eclipse.cdt.ui/debug/LineDelimiters"; //$NON-NLS-1$

	public static boolean DEBUG = false;
	public static boolean DEBUG_CONTENT_ASSIST = false;
	public static boolean DEBUG_SEMANTIC_HIGHLIGHTING = false;
	public static boolean DEBUG_FOLDING = false;
	public static boolean DEBUG_RESULT_COLLECTOR = false;
	public static boolean DEBUG_LINE_DELIMITERS = false;

	/**
	 * The {@link DebugTrace} object to print to OSGi tracing
	 */
	private static DebugTrace fgDebugTrace;

	/**
	 * Constructor
	 */
	public CUIDebugOptions(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, CUIPlugin.PLUGIN_ID);
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}


	@Override
	public void optionsChanged(DebugOptions options) {
		fgDebugTrace = options.newDebugTrace(CUIPlugin.PLUGIN_ID);
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
		DEBUG_CONTENT_ASSIST = options.getBooleanOption(DEBUG_CONTENT_ASSIST_FLAG, false);
		DEBUG_SEMANTIC_HIGHLIGHTING = options.getBooleanOption(DEBUG_SEMANTIC_HIGHLIGHTING_FLAG, false);
		DEBUG_FOLDING = options.getBooleanOption(DEBUG_FOLDING_FLAG, false);
		DEBUG_RESULT_COLLECTOR = options.getBooleanOption(DEBUG_RESULT_COLLECTOR_FLAG, false);
		DEBUG_LINE_DELIMITERS = options.getBooleanOption(DEBUG_LINE_DELIMITERS_FLAG, false);
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if started)
	 * @param option the option or <code>null</code>
	 * @param message the message to print or <code>null</code>
	 * @param throwable the {@link Throwable} or <code>null</code>
	 */
	public static void trace(String option, String message, Throwable throwable) {
		//print the message to console
		String systemPrintableMessage = message;
		while (systemPrintableMessage.length() > 100) {	
			String partial = systemPrintableMessage.substring(0, 100);
			systemPrintableMessage = systemPrintableMessage.substring(100);
			System.out.println(partial + "\\"); //$NON-NLS-1$
		}
		if (systemPrintableMessage.endsWith("\n")) { //$NON-NLS-1$
			System.err.print(systemPrintableMessage);
		} else {
			System.out.println(systemPrintableMessage);
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
