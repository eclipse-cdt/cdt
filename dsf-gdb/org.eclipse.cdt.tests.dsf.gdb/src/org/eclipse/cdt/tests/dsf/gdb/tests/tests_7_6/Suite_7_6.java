/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_6;

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
 * This suite is for tests to be run with GDB 7.6.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	MIRegistersTest_7_6.class,
	MIRunControlTest_7_6.class,
	MIRunControlTargetAvailableTest_7_6.class,
	MIRunControlNonStopTargetAvailableTest_7_6.class,
	MIExpressionsTest_7_6.class,
	GDBPatternMatchingExpressionsTest_7_6.class,
	MIMemoryTest_7_6.class,
	MIBreakpointsTest_7_6.class,
	MICatchpointsTest_7_6.class,
	MIDisassemblyTest_7_6.class,
	GDBProcessesTest_7_6.class,
	LaunchConfigurationAndRestartTest_7_6.class,
	OperationsWhileTargetIsRunningTest_7_6.class,
	OperationsWhileTargetIsRunningNonStopTest_7_6.class,
	PostMortemCoreTest_7_6.class,
	CommandTimeoutTest_7_6.class,
	GDBMultiNonStopRunControlTest_7_6.class,
	Suite_Sessionless_Tests.class,
	GDBConsoleBreakpointsTest_7_6.class,
	GDBConsoleSynchronizingTest_7_6.class,
	StepIntoSelectionTest_7_6.class,
	StepIntoSelectionTest_7_6_NS.class,
	/* Add your test class here */
})

public class Suite_7_6 {
	@BeforeClass
	public static void beforeClassMethod() {
		BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_6);
		BaseTestCase.ignoreIfGDBMissing();
	}
}
