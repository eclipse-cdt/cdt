/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 437562 - Split the dsf-gdb tests to a plug-in and fragment pair
 *     Jonah Graham (Kichwa Coders) - Bug 469007 - Add MIExpressionsNonStopTest_7_0 to suite
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_0;

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
 *  This suite is for tests to be run with GDB 7.0
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	GDBRemoteTracepointsTest_7_0.class,
	MIRegistersTest_7_0.class,
	MIRunControlTargetAvailableTest_7_0.class,
	MIRunControlNonStopTargetAvailableTest_7_0.class,
	MIRunControlTest_7_0.class,
	MIExpressionsTest_7_0.class,
	MIExpressionsNonStopTest_7_0.class,
	GDBPatternMatchingExpressionsTest_7_0.class,
	MIMemoryTest_7_0.class,
	MIBreakpointsTest_7_0.class,
	MICatchpointsTest_7_0.class,
	MIDisassemblyTest_7_0.class,
	GDBProcessesTest_7_0.class,
	OperationsWhileTargetIsRunningTest_7_0.class,
	OperationsWhileTargetIsRunningNonStopTest_7_0.class,
	CommandTimeoutTest_7_0.class,
	GDBMultiNonStopRunControlTest_7_0.class,
	StepIntoSelectionTest_7_0.class,
	StepIntoSelectionNonStopTest_7_0.class,
	RunGDBScriptTest_7_0.class,
	/* Add your test class here */
})

public class Suite_Remote_7_0 extends BaseRemoteSuite {
	@BeforeClass
	public static void beforeClassMethod() {
		BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_0);
		BaseTestCase.ignoreIfGDBMissing();
	}
}
