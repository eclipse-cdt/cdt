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
package org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers.messages"; //$NON-NLS-1$
	public static String CompilationDatabaseParserOptionPage_Browse;
	public static String CompilationDatabaseParserOptionPage_BuildOutputParserError;
	public static String CompilationDatabaseParserOptionPage_BuildParser;
	public static String CompilationDatabaseParserOptionPage_ChooseFile;
	public static String CompilationDatabaseParserOptionPage_CompileCommandsPath;
	public static String CompilationDatabaseParserOptionPage_CompileCommandsPathError;
	public static String CompilationDatabaseParserOptionPage_ExcludeFiles;
	public static String CompilationDatabaseParserOptionPage_NoBuildOutputParserError;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
