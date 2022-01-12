/*******************************************************************************
 * Copyright (c) 2016 QNX Software System and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.tests.nonstop.GDBMultiNonStopRunControlTest;
import org.eclipse.cdt.tests.dsf.gdb.tests.nonstop.MIExpressionsNonStopTest;
import org.eclipse.cdt.tests.dsf.gdb.tests.nonstop.MIRunControlNonStopTargetAvailableTest;
import org.eclipse.cdt.tests.dsf.gdb.tests.nonstop.OperationsWhileTargetIsRunningNonStopTest;
import org.eclipse.cdt.tests.dsf.gdb.tests.nonstop.StepIntoSelectionNonStopTest;
import org.eclipse.cdt.tests.dsf.gdb.tests.nonstop.ThreadStackFrameSyncTest;
import org.eclipse.cdt.tests.dsf.mi.service.command.MIAsyncErrorProcessorTests;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This suite is for tests to be run with GDB.
 *
 * If you running this from IDE use java var to control version like this -Dcdt.tests.dsf.gdb.versions=gdb.7.7,gdbserver.7.7
 * If you don't it will run default gdb (without version postfix) for new tests. It will run 7.11 for all non-converted tests.
 *
 * If you adding a new test class do not use gdb version naming.
 * Use flat version extending BaseParametrizedTestCase see {@link MIBreakpointsTest}
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		// new style tests
		MIBreakpointsTest.class, MICatchpointsTest.class, MIRegistersTest.class, MIExpressionsTest.class,
		LaunchConfigurationAndRestartTest.class, SourceLookupTest.class, StepIntoSelectionTest.class,
		OperationsWhileTargetIsRunningTest.class, MIModifiedServicesTest.class, MIRunControlTest.class,
		MIRunControlTargetAvailableTest.class, MIRunControlReverseTest.class, GDBPatternMatchingExpressionsTest.class,
		GDBMultiNonStopRunControlTest.class, GDBConsoleBreakpointsTest.class,
		MIRunControlNonStopTargetAvailableTest.class, MIExpressionsNonStopTest.class,
		OperationsWhileTargetIsRunningNonStopTest.class, StepIntoSelectionNonStopTest.class,
		GDBRemoteTracepointsTest.class, TraceFileTest.class, GDBConsoleSynchronizingTest.class, MIMemoryTest.class,
		MIDisassemblyTest.class, GDBProcessesTest.class, PostMortemCoreTest.class, CommandTimeoutTest.class,
		ThreadStackFrameSyncTest.class, CommandLineArgsTest.class, MIAsyncErrorProcessorTests.class
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