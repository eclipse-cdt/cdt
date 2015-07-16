/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial implementation of Test cases
 *     Jonah Graham (Kichwa Coders) - Bug 469007 - Add MIExpressionsNonStopTest_7_9 to suite
 *     Jonah Graham (Kichwa Coders) - Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_9;

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
 * This suite is for tests to be run with GDB 7_9
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	GDBMultiNonStopRunControlTest_7_9.class,
	GDBRemoteTracepointsTest_7_9.class,
	MIRegistersTest_7_9.class,
	MIRunControlTest_7_9.class,
	MIRunControlTargetAvailableTest_7_9.class,
	MIRunControlNonStopTargetAvailableTest_7_9.class,
	MIExpressionsTest_7_9.class,
	MIExpressionsNonStopTest_7_9.class,
	GDBPatternMatchingExpressionsTest_7_9.class,
	MIMemoryTest_7_9.class,
	MIBreakpointsTest_7_9.class,
	MICatchpointsTest_7_9.class,
	MIDisassemblyTest_7_9.class,
	GDBProcessesTest_7_9.class,
	OperationsWhileTargetIsRunningTest_7_9.class,
	OperationsWhileTargetIsRunningNonStopTest_7_9.class,
	CommandTimeoutTest_7_9.class,
	GDBConsoleBreakpointsTest_7_9.class,
	TraceFileTest_7_9.class,
	GDBConsoleSynchronizingTest_7_9.class,
	StepIntoSelectionTest_7_9.class,
	StepIntoSelectionNonStopTest_7_9.class,
	SourceLookupTest_7_9.class,
	/* Add your test class here */
})

public class Suite_Remote_7_9 extends BaseRemoteSuite {
	@BeforeClass
	public static void beforeClassMethod() {
		BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_9);
		BaseTestCase.ignoreIfGDBMissing();
	}
}
