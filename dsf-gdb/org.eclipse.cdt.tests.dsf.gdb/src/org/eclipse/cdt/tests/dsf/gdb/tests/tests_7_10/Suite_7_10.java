/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial implementation of Test cases
 *     Jonah Graham (Kichwa Coders) - Bug 469007 - Add MIExpressionsNonStopTest_7_10 to suite
 *     Jonah Graham (Kichwa Coders) - Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_10;

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
 * This suite is for tests to be run with GDB 7_10.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	MIRegistersTest_7_10.class,
	MIRunControlTest_7_10.class,
	MIRunControlTargetAvailableTest_7_10.class,
	MIRunControlNonStopTargetAvailableTest_7_10.class,
	MIExpressionsTest_7_10.class,
	MIExpressionsNonStopTest_7_10.class,
	GDBPatternMatchingExpressionsTest_7_10.class,
	MIMemoryTest_7_10.class,
	MIBreakpointsTest_7_10.class,
	MICatchpointsTest_7_10.class,
	MIDisassemblyTest_7_10.class,
	GDBProcessesTest_7_10.class,
	LaunchConfigurationAndRestartTest_7_10.class,
	OperationsWhileTargetIsRunningTest_7_10.class,
	OperationsWhileTargetIsRunningNonStopTest_7_10.class,
	PostMortemCoreTest_7_10.class,
	CommandTimeoutTest_7_10.class,
	GDBMultiNonStopRunControlTest_7_10.class,
	GDBConsoleBreakpointsTest_7_10.class,
	GDBConsoleSynchronizingTest_7_10.class,
	StepIntoSelectionTest_7_10.class,
	StepIntoSelectionNonStopTest_7_10.class,
	SourceLookupTest_7_10.class,
	/* Add your test class here */
})

public class Suite_7_10 {
	@BeforeClass
	public static void beforeClassMethod() {
		BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_10);
		BaseTestCase.ignoreIfGDBMissing();
	}
}
