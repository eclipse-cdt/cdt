/*******************************************************************************
 * Copyright (c) 2016 QNX Software System and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.CommandTimeoutTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.GDBConsoleBreakpointsTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.GDBConsoleSynchronizingTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.GDBMultiNonStopRunControlTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.GDBPatternMatchingExpressionsTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.GDBProcessesTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.LaunchConfigurationAndRestartTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.MICatchpointsTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.MIDisassemblyTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.MIExpressionsNonStopTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.MIExpressionsTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.MIMemoryTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.MIRegistersTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.MIRunControlNonStopTargetAvailableTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.MIRunControlTargetAvailableTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.MIRunControlTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.OperationsWhileTargetIsRunningNonStopTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.OperationsWhileTargetIsRunningTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.PostMortemCoreTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.SourceLookupTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.StepIntoSelectionNonStopTest_7_11;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11.StepIntoSelectionTest_7_11;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This suite is for tests to be run with GDB.
 *
 * If you running this from IDE use java var to control version like this -Dcdt.tests.dsf.gdb.versions=gdb.7.7,gdbserver.7.7
 * If you don't it will run default gdb (without version postfix) for new tests. It will run 7.11 for all non-converted tests.
 *
 * If you adding a new test class do not use gdb version naming. Use flat version extending BaseParametrizedTestCase,
 * see {@link MIBreakpointsTest}
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// temporary we still use hardcoded gdb version name, we will slowly flatten them
	MIRegistersTest_7_11.class,
	MIRunControlTest_7_11.class,
	MIRunControlTargetAvailableTest_7_11.class,
	MIRunControlNonStopTargetAvailableTest_7_11.class,
	MIExpressionsTest_7_11.class,
	MIExpressionsNonStopTest_7_11.class,
	GDBPatternMatchingExpressionsTest_7_11.class,
	MIMemoryTest_7_11.class,
	MIBreakpointsTest.class, // this is flat version
	MICatchpointsTest_7_11.class,
	MIDisassemblyTest_7_11.class,
	GDBProcessesTest_7_11.class,
	LaunchConfigurationAndRestartTest_7_11.class,
	OperationsWhileTargetIsRunningTest_7_11.class,
	OperationsWhileTargetIsRunningNonStopTest_7_11.class,
	PostMortemCoreTest_7_11.class,
	CommandTimeoutTest_7_11.class,
	GDBMultiNonStopRunControlTest_7_11.class,
	GDBConsoleBreakpointsTest_7_11.class,
	GDBConsoleSynchronizingTest_7_11.class,
	StepIntoSelectionTest_7_11.class,
	StepIntoSelectionNonStopTest_7_11.class,
	SourceLookupTest_7_11.class,
	/* Add your test class here */
})
public class SuiteGdb {
	@BeforeClass
	public static void before() {
		// If we running this suite we have to clean up global options since
		// each test will set local version of these properly.
		// If our tests are running from other suites they
		// may have globals that will override local values.
		BaseParametrizedTestCase.resetGlobalState();
	}
}