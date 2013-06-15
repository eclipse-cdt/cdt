/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_1;

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
 *  This suite is for tests to be run with GDB 7.1
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	GDBRemoteTracepointsTest_7_1.class,
	MIRegistersTest_7_1.class,
	MIRunControlTargetAvailableTest_7_1.class,
	MIRunControlNonStopTargetAvailableTest_7_1.class,
	MIRunControlTest_7_1.class,
	MIExpressionsTest_7_1.class,
	GDBPatternMatchingExpressionsTest_7_1.class,
	MIMemoryTest_7_1.class,
	MIBreakpointsTest_7_1.class,
	MICatchpointsTest_7_1.class,
	MIDisassemblyTest_7_1.class,
	GDBProcessesTest_7_1.class,
	OperationsWhileTargetIsRunningTest_7_1.class,
	OperationsWhileTargetIsRunningNonStopTest_7_1.class,
	CommandTimeoutTest_7_1.class,
	GDBMultiNonStopRunControlTest_7_1.class,
	Suite_Sessionless_Tests.class,
	StepIntoSelectionTest_7_1.class,
	StepIntoSelectionTest_7_1_NS.class,
	/* Add your test class here */
})

public class Suite_Remote_7_1 extends BaseRemoteSuite {
	@BeforeClass
	public static void beforeClassMethod() {
		BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_1);
		BaseTestCase.ignoreIfGDBMissing();
	}
}
