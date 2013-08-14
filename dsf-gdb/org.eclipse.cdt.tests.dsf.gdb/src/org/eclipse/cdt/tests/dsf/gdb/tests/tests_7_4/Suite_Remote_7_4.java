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
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4;

import org.eclipse.cdt.dsf.mi.service.command.commands.Suite_Sessionless_Tests;
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
 *  This suite is for tests to be run with GDB 7.4
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	GDBRemoteTracepointsTest_7_4.class,
	MIRegistersTest_7_4.class,
	MIRunControlTest_7_4.class,
	MIRunControlTargetAvailableTest_7_4.class,
	MIRunControlNonStopTargetAvailableTest_7_4.class,
	MIExpressionsTest_7_4.class,
	GDBPatternMatchingExpressionsTest_7_4.class,
	MIMemoryTest_7_4.class,
	MIBreakpointsTest_7_4.class,
	MICatchpointsTest_7_4.class,
	MIDisassemblyTest_7_4.class,
	GDBProcessesTest_7_4.class,
	OperationsWhileTargetIsRunningTest_7_4.class,
	OperationsWhileTargetIsRunningNonStopTest_7_4.class,
	CommandTimeoutTest_7_4.class,
	GDBMultiNonStopRunControlTest_7_4.class,
	Suite_Sessionless_Tests.class,	
	GDBConsoleBreakpointsTest_7_4.class,
	TraceFileTest_7_4.class,
	StepIntoSelectionTest_7_4.class,
	StepIntoSelectionTest_7_4_NS.class,
	/* Add your test class here */
})

public class Suite_Remote_7_4 extends BaseRemoteSuite {
	@BeforeClass
	public static void beforeClassMethod() {
		BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_4);
		BaseTestCase.ignoreIfGDBMissing();
	}
}
