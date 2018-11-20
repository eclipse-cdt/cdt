/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov.
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
package org.eclipse.cdt.testsrunner.internal.gtest;

import org.eclipse.osgi.util.NLS;

public class GoogleTestsRunnerMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.internal.gtest.GoogleTestsRunnerMessages"; //$NON-NLS-1$
	public static String GoogleTestsRunner_error_format;
	public static String GoogleTestsRunner_io_error_prefix;
	public static String OutputHandler_getparam_message;
	public static String OutputHandler_unexpected_case_end;
	public static String OutputHandler_unexpected_output;
	public static String OutputHandler_unexpected_suite_end;
	public static String OutputHandler_unknown_error_prefix;
	public static String OutputHandler_unknown_location_format;
	public static String OutputHandler_unknown_test_status;
	public static String OutputHandler_wrong_groups_count;
	public static String OutputHandler_wrong_suite_name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, GoogleTestsRunnerMessages.class);
	}

	private GoogleTestsRunnerMessages() {
	}
}
