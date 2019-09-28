/*******************************************************************************
 * Copyright (c) 2019 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.language.settings.providers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.managedbuilder.internal.language.settings.providers.messages"; //$NON-NLS-1$

	public static String CompilationDatabaseParser_BuildCommandParserNotConfigured;
	public static String CompilationDatabaseParser_BuildCommandParserNotFound;
	public static String CompilationDatabaseParser_CDBNotConfigured;
	public static String CompilationDatabaseParser_CDBNotFound;
	public static String CompilationDatabaseParser_ErrorProcessingCompilationDatabase;
	public static String CompilationDatabaseParser_Job;
	public static String CompilationDatabaseParser_ProgressApplyingEntries;
	public static String CompilationDatabaseParser_ProgressExcludingFiles;
	public static String CompilationDatabaseParser_ProgressParsingBuildCommands;
	public static String CompilationDatabaseParser_ProgressParsingJSONFile;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
