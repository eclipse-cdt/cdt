/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.boost;

import org.eclipse.osgi.util.NLS;

public class BoostTestsRunnerMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.internal.boost.BoostTestsRunnerMessages"; //$NON-NLS-1$
	public static String BoostTestsRunner_error_format;
	public static String BoostTestsRunner_io_error_prefix;
	public static String BoostTestsRunner_wrong_tests_paths_count;
	public static String BoostTestsRunner_xml_error_prefix;
	public static String BoostXmlLogHandler_exception_suffix;
	public static String BoostXmlLogHandler_wrong_tag_name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, BoostTestsRunnerMessages.class);
	}

	private BoostTestsRunnerMessages() {
	}
}
