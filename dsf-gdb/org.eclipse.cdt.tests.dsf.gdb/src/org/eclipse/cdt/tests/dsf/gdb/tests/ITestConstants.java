/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *     Marc Khouzam (Ericsson) - Tests for GDB 7.4
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;

public class ITestConstants {
	public static final String SUFFIX_GDB_6_6 = "6.6";
	public static final String SUFFIX_GDB_6_7 = "6.7";
	public static final String SUFFIX_GDB_6_8 = "6.8";
	public static final String SUFFIX_GDB_7_0 = "7.0";
	public static final String SUFFIX_GDB_7_1 = "7.1";
	public static final String SUFFIX_GDB_7_2 = "7.2";
	public static final String SUFFIX_GDB_7_3 = "7.3";
	public static final String SUFFIX_GDB_7_4 = "7.4";
	public static final String SUFFIX_GDB_7_5 = "7.5";
	public static final String SUFFIX_GDB_7_6 = "7.6";
	public static final String SUFFIX_GDB_7_7 = "7.7";
	public static final String SUFFIX_GDB_7_8 = "7.8";
	public static final String SUFFIX_GDB_7_9 = "7.9";
	public static final String SUFFIX_GDB_7_10 = "7.10";
	public static final String SUFFIX_GDB_7_11 = "7.11";
	public static final String SUFFIX_GDB_7_12 = "7.12";
	public static final String SUFFIX_GDB_8_0 = "8.0";
	public static final String SUFFIX_GDB_8_1 = "8.1";
	public static final String SUFFIX_GDB_8_2 = "8.2";

	public static String[] ALL_SUPPORTED_VERSIONS = new String[] {
			// add new versions here
			ITestConstants.SUFFIX_GDB_8_2, ITestConstants.SUFFIX_GDB_8_1, ITestConstants.SUFFIX_GDB_8_0,
			ITestConstants.SUFFIX_GDB_7_12, ITestConstants.SUFFIX_GDB_7_11, ITestConstants.SUFFIX_GDB_7_10,
			ITestConstants.SUFFIX_GDB_7_9, ITestConstants.SUFFIX_GDB_7_8, ITestConstants.SUFFIX_GDB_7_7,
			ITestConstants.SUFFIX_GDB_7_6, ITestConstants.SUFFIX_GDB_7_5, };

	public static String[] ALL_UNSUPPORTED_VERSIONS = new String[] { ITestConstants.SUFFIX_GDB_7_4,
			ITestConstants.SUFFIX_GDB_7_3, ITestConstants.SUFFIX_GDB_7_2, ITestConstants.SUFFIX_GDB_7_1,
			ITestConstants.SUFFIX_GDB_7_0, ITestConstants.SUFFIX_GDB_6_8, ITestConstants.SUFFIX_GDB_6_7,
			ITestConstants.SUFFIX_GDB_6_6, };

	public static String[] ALL_KNOWN_VERSIONS;
	static {
		// Initialize all known version based on the other arrays to avoid code duplication
		ALL_KNOWN_VERSIONS = new String[ALL_SUPPORTED_VERSIONS.length + ALL_UNSUPPORTED_VERSIONS.length];
		System.arraycopy(ALL_SUPPORTED_VERSIONS, 0, ALL_KNOWN_VERSIONS, 0, ALL_SUPPORTED_VERSIONS.length);
		System.arraycopy(ALL_UNSUPPORTED_VERSIONS, 0, ALL_KNOWN_VERSIONS, ALL_SUPPORTED_VERSIONS.length,
				ALL_UNSUPPORTED_VERSIONS.length);
	}

	// Attribute that allows a test to request not to start gdbserver even if the session is a remote one
	public static final String LAUNCH_GDB_SERVER = TestsPlugin.PLUGIN_ID + ".launchGdbServer";
}
