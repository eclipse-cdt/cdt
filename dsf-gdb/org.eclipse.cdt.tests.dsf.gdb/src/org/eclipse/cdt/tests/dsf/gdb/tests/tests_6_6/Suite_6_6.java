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
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_6;

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
 *  This suite is for tests to be run with GDB 6.6
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	MIRegistersTest_6_6.class,
	MIRunControlTest_6_6.class,
	MIRunControlTargetAvailableTest_6_6.class,
	MIExpressionsTest_6_6.class,
	GDBPatternMatchingExpressionsTest_6_6.class,
	MIMemoryTest_6_6.class,
	MIBreakpointsTest_6_6.class,
	MICatchpointsTest_6_6.class,
	MIDisassemblyTest_6_6.class,
	GDBProcessesTest_6_6.class,
	LaunchConfigurationAndRestartTest_6_6.class,
	OperationsWhileTargetIsRunningTest_6_6.class,
	PostMortemCoreTest_6_6.class,
	Suite_Sessionless_Tests.class,
	CommandTimeoutTest_6_6.class,
	/* Add your test class here */
})

public class Suite_6_6 {
	@BeforeClass
	public static void beforeClassMethod() {
		BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_6_6);
		BaseTestCase.ignoreIfGDBMissing();
	}
}

