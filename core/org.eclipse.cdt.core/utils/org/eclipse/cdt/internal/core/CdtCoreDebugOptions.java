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

package org.eclipse.cdt.internal.core;

import java.util.Hashtable;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;

public class CdtCoreDebugOptions implements DebugOptionsListener {
	
	private static final String DEBUG_FLAG = "org.eclipse.cdt.core/debug"; //$NON-NLS-1$
	private static final String DEBUG_MODEL_FLAG = "org.eclipse.cdt.core/debug/model"; //$NON-NLS-1$
	private static final String DEBUG_PARSER_FLAG = "org.eclipse.cdt.core/debug/parser"; //$NON-NLS-1$
	private static final String DEBUG_PARSER_EXCEPTIONS_FLAG = "org.eclipse.cdt.core/debug/parser/exceptions"; //$NON-NLS-1$
	private static final String DEBUG_SCANNER_FLAG = "org.eclipse.cdt.core/debug/scanner"; //$NON-NLS-1$
	private static final String DEBUG_SCANNER_MISSING_INCLUDE_GUARDS_FLAG = "org.eclipse.cdt.core/debug/scanner/missingIncludeGuards"; //$NON-NLS-1$
	private static final String DEBUG_DELTA_FLAG = "org.eclipse.cdt.core/debug/deltaprocessor"; //$NON-NLS-1$
	private static final String DEBUG_RESOURCE_LOOKUP_FLAG = "org.eclipse.cdt.core/debug/resourceLookup"; //$NON-NLS-1$
	private static final String DEBUG_FORMATTER_FLAG = "org.eclipse.cdt.core/debug/formatter"; //$NON-NLS-1$
	private static final String DEBUG_AST_CACHE_FLAG = "org.eclipse.cdt.core/debug/ASTCache"; //$NON-NLS-1$
	private static final String DEBUG_PDOM_INDEX_LOCKS_FLAG = "org.eclipse.cdt.core/debug/index/locks"; //$NON-NLS-1$
	private static final String DEBUG_SEARCH_FLAG = "org.eclipse.cdt.core/debug/search"; //$NON-NLS-1$
	private static final String DEBUG_MATCH_LOCATER_FLAG = "org.eclipse.cdt.core/debug/matchlocator"; //$NON-NLS-1$
	private static final String DEBUG_TYPE_RESOLVER_FLAG = "org.eclipse.cdt.core/debug/typeresolver"; //$NON-NLS-1$
	private static final String DEBUG_INDEXER_ACTIVITY_FLAG = "org.eclipse.cdt.core/debug/indexer/activity"; //$NON-NLS-1$
	private static final String DEBUG_INDEXER_STATISTICS_FLAG = "org.eclipse.cdt.core/debug/indexer/statistics"; //$NON-NLS-1$
	private static final String DEBUG_INDEXER_PROBLEMS_INCLUSION_FLAG = "org.eclipse.cdt.core/debug/indexer/problems/inclusion"; //$NON-NLS-1$
	private static final String DEBUG_INDEXER_PROBLEMS_SCANNER_FLAG = "org.eclipse.cdt.core/debug/indexer/problems/scanner"; //$NON-NLS-1$
	private static final String DEBUG_INDEXER_PROBLEMS_SYNTAX_FLAG = "org.eclipse.cdt.core/debug/indexer/problems/syntax"; //$NON-NLS-1$
	private static final String DEBUG_INDEXER_PROBLEMS_FLAG = "org.eclipse.cdt.core/debug/indexer/problems"; //$NON-NLS-1$
	private static final String DEBUG_INDEXER_SETUP_FLAG = "org.eclipse.cdt.core/debug/indexer/setup"; //$NON-NLS-1$
	
	public static boolean DEBUG = false;
	public static boolean DEBUG_MODEL = false;
	public static boolean DEBUG_PARSER = false;
	public static boolean DEBUG_PARSER_EXCEPTIONS = false;
	public static boolean DEBUG_SCANNER = false;
	public static boolean DEBUG_SCANNER_MISSING_INCLUDE_GUARDS = false;
	public static boolean DEBUG_DELTA = false;
	public static boolean DEBUG_RESOURCE_LOOKUP = false;
	public static boolean DEBUG_FORMATTER = false;
	public static boolean DEBUG_AST_CACHE = false;
	public static boolean DEBUG_PDOM_INDEX_LOCKS = false;
	public static boolean DEBUG_SEARCH = false;
	public static boolean DEBUG_MATCH_LOCATER = false;
	public static boolean DEBUG_TYPE_RESOLVER = false;
	public static boolean DEBUG_INDEXER_ACTIVITY = false;
	public static boolean DEBUG_INDEXER_STATISTICS = false;
	public static boolean DEBUG_INDEXER_PROBLEMS_INCLUSION = false;
	public static boolean DEBUG_INDEXER_PROBLEMS_SCANNER = false;
	public static boolean DEBUG_INDEXER_PROBLEMS_SYNTAX = false;
	public static boolean DEBUG_INDEXER_PROBLEMS = false;
	public static boolean DEBUG_INDEXER_SETUP = false;
	
	/**
	 * The {@link DebugTrace} object to print to OSGi tracing
	 */
	private static DebugTrace fgDebugTrace;
	
	/**
	 * Constructor
	 */
	public CdtCoreDebugOptions(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, CCorePlugin.getUniqueIdentifier());
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}


	@Override
	public void optionsChanged(DebugOptions options) {
		fgDebugTrace = options.newDebugTrace(CCorePlugin.getUniqueIdentifier());
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
		DEBUG_MODEL = options.getBooleanOption(DEBUG_MODEL_FLAG, false);
		DEBUG_PARSER = options.getBooleanOption(DEBUG_PARSER_FLAG, false);
		DEBUG_PARSER_EXCEPTIONS = options.getBooleanOption(DEBUG_PARSER_EXCEPTIONS_FLAG, false);
		DEBUG_SCANNER = options.getBooleanOption(DEBUG_SCANNER_FLAG, false);
		DEBUG_SCANNER_MISSING_INCLUDE_GUARDS = options.getBooleanOption(DEBUG_SCANNER_MISSING_INCLUDE_GUARDS_FLAG, false);
		DEBUG_DELTA = options.getBooleanOption(DEBUG_DELTA_FLAG, false);
		DEBUG_RESOURCE_LOOKUP = options.getBooleanOption(DEBUG_RESOURCE_LOOKUP_FLAG, false);
		DEBUG_FORMATTER = options.getBooleanOption(DEBUG_FORMATTER_FLAG, false);
		DEBUG_AST_CACHE = options.getBooleanOption(DEBUG_AST_CACHE_FLAG, false);
		DEBUG_PDOM_INDEX_LOCKS = options.getBooleanOption(DEBUG_PDOM_INDEX_LOCKS_FLAG, false);
		DEBUG_SEARCH = options.getBooleanOption(DEBUG_SEARCH_FLAG, false);
		DEBUG_MATCH_LOCATER = options.getBooleanOption(DEBUG_MATCH_LOCATER_FLAG, false);
		DEBUG_TYPE_RESOLVER = options.getBooleanOption(DEBUG_TYPE_RESOLVER_FLAG, false);
		DEBUG_INDEXER_STATISTICS = options.getBooleanOption(DEBUG_INDEXER_STATISTICS_FLAG, false);
		DEBUG_INDEXER_ACTIVITY = options.getBooleanOption(DEBUG_INDEXER_ACTIVITY_FLAG, false);
		DEBUG_INDEXER_PROBLEMS_INCLUSION = options.getBooleanOption(DEBUG_INDEXER_PROBLEMS_INCLUSION_FLAG, false);
		DEBUG_INDEXER_PROBLEMS_SCANNER = options.getBooleanOption(DEBUG_INDEXER_PROBLEMS_SCANNER_FLAG, false);
		DEBUG_INDEXER_PROBLEMS_SYNTAX = options.getBooleanOption(DEBUG_INDEXER_PROBLEMS_SYNTAX_FLAG, false);
		DEBUG_INDEXER_PROBLEMS = options.getBooleanOption(DEBUG_INDEXER_PROBLEMS_FLAG, false);
		DEBUG_INDEXER_SETUP = options.getBooleanOption(DEBUG_INDEXER_SETUP_FLAG, false);
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
