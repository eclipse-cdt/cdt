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
 *     Jonah Graham (Kichwa Coders) - Bug 469007 - Add MIExpressionsNonStopTest_7_8 to suite
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_8;

import org.eclipse.cdt.tests.dsf.gdb.framework.BaseRemoteSuite;
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
 * This suite is for tests to be run with GDB 7_8
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	GDBMultiNonStopRunControlTest_7_8.class,
	GDBRemoteTracepointsTest_7_8.class,
	MIRegistersTest_7_8.class,
	MIRunControlTest_7_8.class,
	MIRunControlTargetAvailableTest_7_8.class,
	MIRunControlNonStopTargetAvailableTest_7_8.class,
	MIExpressionsTest_7_8.class,
	MIExpressionsNonStopTest_7_8.class,
	GDBPatternMatchingExpressionsTest_7_8.class,
	MIMemoryTest_7_8.class,
	MIBreakpointsTest_7_8.class,
	MICatchpointsTest_7_8.class,
	MIDisassemblyTest_7_8.class,
	GDBProcessesTest_7_8.class,
	OperationsWhileTargetIsRunningTest_7_8.class,
	OperationsWhileTargetIsRunningNonStopTest_7_8.class,
	CommandTimeoutTest_7_8.class,
	GDBConsoleBreakpointsTest_7_8.class,
	TraceFileTest_7_8.class,
	GDBConsoleSynchronizingTest_7_8.class,
	StepIntoSelectionTest_7_8.class,
	StepIntoSelectionNonStopTest_7_8.class,
	RunGDBScriptTest_7_8.class,
	/* Add your test class here */
})

public class Suite_Remote_7_8 extends BaseRemoteSuite {
	@BeforeClass
	public static void beforeClassMethod() {
		BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_8);
		BaseTestCase.ignoreIfGDBMissing();
	}
}
