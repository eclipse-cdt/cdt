/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial implementation of Test cases
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 437562 - Split the dsf-gdb tests to a plug-in and fragment pair
 *     Jonah Graham (Kichwa Coders) - Bug 469007 - Add MIExpressionsNonStopTest_7_5 to suite
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_5;

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
 * This suite is for tests to be run with GDB 7.5.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	GDBMultiNonStopRunControlTest_7_5.class,
	MIRegistersTest_7_5.class,
	MIRunControlTest_7_5.class,
	MIRunControlTargetAvailableTest_7_5.class,
	MIRunControlNonStopTargetAvailableTest_7_5.class,
	MIExpressionsTest_7_5.class,
	MIExpressionsNonStopTest_7_5.class,
	GDBPatternMatchingExpressionsTest_7_5.class,
	MIMemoryTest_7_5.class,
	MIBreakpointsTest_7_5.class,
	MICatchpointsTest_7_5.class,
	MIDisassemblyTest_7_5.class,
	GDBProcessesTest_7_5.class,
	LaunchConfigurationAndRestartTest_7_5.class,
	OperationsWhileTargetIsRunningTest_7_5.class,
	OperationsWhileTargetIsRunningNonStopTest_7_5.class,
	PostMortemCoreTest_7_5.class,
	CommandTimeoutTest_7_5.class,
	GDBConsoleBreakpointsTest_7_5.class,
	StepIntoSelectionTest_7_5.class,
	StepIntoSelectionNonStopTest_7_5.class,
	RunGDBScriptTest_7_5.class,
	/* Add your test class here */
})

public class Suite_7_5 {
	@BeforeClass
	public static void beforeClassMethod() {
		BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_5);
		BaseTestCase.ignoreIfGDBMissing();
	}
}
