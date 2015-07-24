/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial implementation of Test cases
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 437562 - Split the dsf-gdb tests to a plug-in and fragment pair
 *     Jonah Graham (Kichwa Coders) - Bug 469007 - Add MIExpressionsNonStopTest_7_7 to suite
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_7;

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
 * This suite is for tests to be run with GDB 7_7.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	MIRegistersTest_7_7.class,
	MIRunControlTest_7_7.class,
	MIRunControlTargetAvailableTest_7_7.class,
	MIRunControlNonStopTargetAvailableTest_7_7.class,
	MIExpressionsTest_7_7.class,
	MIExpressionsNonStopTest_7_7.class,
	GDBPatternMatchingExpressionsTest_7_7.class,
	MIMemoryTest_7_7.class,
	MIBreakpointsTest_7_7.class,
	MICatchpointsTest_7_7.class,
	MIDisassemblyTest_7_7.class,
	GDBProcessesTest_7_7.class,
	LaunchConfigurationAndRestartTest_7_7.class,
	OperationsWhileTargetIsRunningTest_7_7.class,
	OperationsWhileTargetIsRunningNonStopTest_7_7.class,
	PostMortemCoreTest_7_7.class,
	CommandTimeoutTest_7_7.class,
	GDBMultiNonStopRunControlTest_7_7.class,
	GDBConsoleBreakpointsTest_7_7.class,
	GDBConsoleSynchronizingTest_7_7.class,
	StepIntoSelectionTest_7_7.class,
	StepIntoSelectionNonStopTest_7_7.class,
	RunGDBScriptTest_7_7.class,
	/* Add your test class here */
})

public class Suite_7_7 {
	@BeforeClass
	public static void beforeClassMethod() {
		BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_7);
		BaseTestCase.ignoreIfGDBMissing();
	}
}
