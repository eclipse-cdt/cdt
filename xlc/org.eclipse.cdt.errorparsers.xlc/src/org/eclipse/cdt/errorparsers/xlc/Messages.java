/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This content and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Gvozdev - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.errorparsers.xlc;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.errorparsers.xlc.messages"; //$NON-NLS-1$
	public static String XlcErrorParser_MacroRedefinitionErrorPattern;
	public static String XlcErrorParser_CompilerErrorPattern;
	public static String XlcErrorParser_FlagUnrecoverable;
	public static String XlcErrorParser_FlagSevere;
	public static String XlcErrorParser_FlagError;
	public static String XlcErrorParser_FlagWarning;
	public static String XlcErrorParser_FlagInfo;

	public static String XlcErrorParser_LinkerErrorPattern;
	public static String XlcErrorParser_LinkerErrorPattern2;
	public static String XlcErrorParser_LinkerInfoPattern;
	public static String XlcErrorParser_LinkerWarning;
	public static String XlcErrorParser_LinkerError;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
