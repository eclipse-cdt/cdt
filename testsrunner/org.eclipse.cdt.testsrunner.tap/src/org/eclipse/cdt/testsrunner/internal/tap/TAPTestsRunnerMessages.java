/*******************************************************************************
 * Copyright (c) 2011, 2015 Anton Gorenkov.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.tap;

import org.eclipse.osgi.util.NLS;

public class TAPTestsRunnerMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.internal.tap.TAPTestsRunnerMessages"; //$NON-NLS-1$
	public static String TAPTestsRunner_error_format;
	public static String TAPTestsRunner_io_error_prefix;
	public static String TAPTestsRunner_tap_error_prefix;
	public static String TAPTestsRunner_invalid_version_line;
	public static String TAPTestsRunner_multiple_plans;
	public static String TAPTestsRunner_invalid_test_number;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, TAPTestsRunnerMessages.class);
	}

	private TAPTestsRunnerMessages() {
	}
}
