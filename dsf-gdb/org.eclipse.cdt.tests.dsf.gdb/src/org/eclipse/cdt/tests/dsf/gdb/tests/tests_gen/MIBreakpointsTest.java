/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_gen;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_10.MIBreakpointsTest_7_10;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MIBreakpointsTest extends MIBreakpointsTest_7_10 {
	@Parameterized.Parameters(name = "gdb {0}")
	public static Collection<String> getVersions() {
		return Arrays.asList(new String[] {
				ITestConstants.SUFFIX_GDB_7_7,
				ITestConstants.SUFFIX_GDB_7_10,
				ITestConstants.SUFFIX_GDB_7_11
		});
	}
	private final String gdbversion;

	public MIBreakpointsTest(String gdbVersion) {
		this.gdbversion = gdbVersion;
		setGdbProgramNamesLaunchAttributes(gdbVersion);
	}

	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(gdbversion);
	}

	@Override
	public void doBeforeTest() throws Exception {
		setGdbProgramNamesLaunchAttributes(gdbversion);
		ignoreIfGDBMissing();
		super.doBeforeTest();
	}

	@Override
	public void doAfterTest() throws Exception {
		ignoreIfGDBMissing();
		super.doAfterTest();
	}
}
