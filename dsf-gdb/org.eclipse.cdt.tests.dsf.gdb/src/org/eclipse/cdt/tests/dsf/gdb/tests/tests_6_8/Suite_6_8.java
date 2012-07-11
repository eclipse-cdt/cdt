/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_8;

import org.eclipse.cdt.dsf.mi.service.command.commands.Suite_Sessionless_Tests;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is meant to be empty.  It enables us to define
 * the annotations which list all the different JUnit class we
 * want to run.  When creating a new test class, it should be
 * added to the list below.
 * 
 *  This suite is for tests to be run with GDB 6.8
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	MIRegistersTest_6_8.class,
	MIRunControlTest_6_8.class,
	MIRunControlTargetAvailableTest_6_8.class,
	MIExpressionsTest_6_8.class,
	GDBPatternMatchingExpressionsTest_6_8.class,
	MIMemoryTest_6_8.class,
	MIBreakpointsTest_6_8.class,
	MICatchpointsTest_6_8.class,
	MIDisassemblyTest_6_8.class,
	GDBProcessesTest_6_8.class,
	LaunchConfigurationAndRestartTest_6_8.class,
	OperationsWhileTargetIsRunningTest_6_8.class,
	PostMortemCoreTest_6_8.class,
	CommandTimeoutTest_6_8.class,
	Suite_Sessionless_Tests.class,
	/* Add your test class here */
})

public class Suite_6_8 {
	@BeforeClass
	public static void beforeClassMethod() {
		BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_6_8);
		BaseTestCase.ignoreIfGDBMissing();
	}
}
