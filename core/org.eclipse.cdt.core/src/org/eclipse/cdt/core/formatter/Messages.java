/*******************************************************************************
 *  Copyright (c) 2005, 2016, 2017 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Ben Konrath <ben@bagu.org> - initial implementation
 *     Red Hat Incorporated - improvements based on comments from JDT developers
 *     IBM Corporation - Code review and integration
 *     IBM Corporation - Fix for 340181
 *     Mustafa Yuecel <mustafa.yuecel@siemens.com> - Adaptations for CDT
 *******************************************************************************/
package org.eclipse.cdt.core.formatter;

import org.eclipse.osgi.util.NLS;

/**
 * @since 6.3
 */
public class Messages extends NLS {
	public static String CommandLineConfigFile;
	public static String CommandLineDone;
	public static String CommandLineErrorConfig;
	public static String CommandLineErrorFileTryFullPath;
	public static String CommandLineErrorFile;
	public static String CommandLineErrorFileDir;
	public static String CommandLineErrorQuietVerbose;
	public static String CommandLineErrorNoConfigFile;
	public static String CommandLineFormatting;
	public static String CommandLineStart;
	public static String CommandLineUsage;
	public static String ConfigFileNotFoundErrorTryFullPath;
	public static String ConfigFileReadingError;
	public static String FormatProblem;
	public static String CaughtException;
	public static String ExceptionSkip;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
