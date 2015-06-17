/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *     Marc Khouzam (Ericsson) - Tests for GDB 7.4
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;

public interface ITestConstants {

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
	
	// Attribute that allows a test to request not to start gdbserver even if the session is a remote one
	public static final String LAUNCH_GDB_SERVER = TestsPlugin.PLUGIN_ID + ".launchGdbServer";
}
