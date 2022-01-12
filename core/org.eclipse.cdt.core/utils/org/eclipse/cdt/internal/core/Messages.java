/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String Util_unexpectedError;
	public static String Addr_valueOutOfRange;
	public static String CommandLauncher_CommandCancelled;
	public static String CommandLauncher_InvalidWorkingDirectory;
	public static String CommandLauncher_ProgramNotFoundInPath;
	public static String convention_illegalIdentifier;
	public static String convention_invalid;
	public static String convention_reservedKeyword;
	public static String convention_scope_leadingUnderscore;
	public static String convention_scope_lowercaseName;
	public static String convention_scope_nullName;
	public static String convention_scope_emptyName;
	public static String convention_scope_dotName;
	public static String convention_scope_nameWithBlanks;

	public static String convention_class_nullName;
	public static String convention_class_nameWithBlanks;
	public static String convention_class_dollarName;
	public static String convention_class_leadingUnderscore;
	public static String convention_class_lowercaseName;
	public static String convention_class_invalidName;

	public static String convention_namespace_nullName;
	public static String convention_namespace_nameWithBlanks;
	public static String convention_namespace_dollarName;
	public static String convention_namespace_leadingUnderscore;

	public static String convention_filename_nullName;
	public static String convention_filename_possiblyInvalid;
	public static String convention_filename_nameWithBlanks;
	public static String convention_headerFilename_filetype;
	public static String convention_sourceFilename_filetype;

	public static String convention_enum_nullName;
	public static String convention_enum_nameWithBlanks;
	public static String convention_enum_dollarName;
	public static String convention_enum_leadingUnderscore;
	public static String convention_enum_lowercaseName;
	public static String convention_enum_invalidName;
	public static String XmlUtil_InternalErrorLoading;
	public static String XmlUtil_InternalErrorSerializing;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
