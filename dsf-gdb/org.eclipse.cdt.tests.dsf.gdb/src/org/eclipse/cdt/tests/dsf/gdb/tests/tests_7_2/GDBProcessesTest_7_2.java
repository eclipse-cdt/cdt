/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupExitedEvent;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_1.GDBProcessesTest_7_1;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class GDBProcessesTest_7_2 extends GDBProcessesTest_7_1 {

	private DsfSession fSession;

	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_2);
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, true);
		// Pause program before termination to give a chance to arm a ServiceEventWaitor
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "1");
	}

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();
		fSession = getGDBLaunch().getSession();
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();
		// Setting it to null to start next case with a single process behavior
		SyncUtil.setActiveContainerContext(null);
	}

	@Test
	public void debugNewProcess() throws Throwable {
		int maxNumThreads = 5;
		int partialNumThreads = 2;
		// Process 1
		// Start the threads one by one to make sure they are discovered by gdb in the right
		// order.
		for (int i = 0; i < maxNumThreads; i++) {
			SyncUtil.runToLocation(SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_AFTER_THREAD_START"));
		}

		validateThreadData();
		System.out.println("Validation for process 1 passed");

		// Keep track of all processes involved in this test
		List<IContainerDMContext> testProcessList = new ArrayList<IContainerDMContext>();
		IContainerDMContext process1 = SyncUtil.getContainerContext();
		testProcessList.add(process1);

		// Validate number of threads i.e. created by program + 1 for the main thread
		IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertEquals("Unexpected number of Threads running", maxNumThreads + 1, threads.length);

		// Process 2
		IContainerDMContext process2 = SyncUtil.debugNewProcess(getGDBLaunch(), EXEC_PATH + EXEC_NAME);
		// Switch SyncUtil to use this process in all subsequent calls
		SyncUtil.setActiveContainerContext(process2);
		testProcessList.add(process2);

		// Start a portion of threads one by one to make sure they are discovered by gdb in the right
		// order.
		for (int i = 0; i < maxNumThreads; i++) {
			// Validate number of threads mid way
			if (i == partialNumThreads) {
				// Validate number of threads i.e. created by program + 1 for the main thread
				threads = SyncUtil.getExecutionContexts();
				assertEquals("Unexpected number of Threads running", partialNumThreads + 1, threads.length);
			}
			SyncUtil.runToLocation(SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_AFTER_THREAD_START"));
		}

		validateThreadData();
		System.out.println("Validation for process 2 passed");

		// Validate the number of processes running on target
		IContainerDMContext[] targetProcContexts = SyncUtil.getContainerContexts();
		assertNotNull("No processes being debugged", targetProcContexts);
		assertEquals("Unexpected number of processes being debugged", testProcessList.size(),
				targetProcContexts.length);

		// Assert that test processes are the ones present in the target
		for (IContainerDMContext proc : targetProcContexts) {
			assertTrue("Test process not found in target", testProcessList.contains(proc));
		}

		// Resume till processes terminate with a small program pause to give us the chance to arm the
		// ServiceEventWaitor below see setLaunchAttributes
		SyncUtil.resume(); /* process 2 */
		SyncUtil.setActiveContainerContext(process1);
		SyncUtil.resume(); /* process 1 */

		// Verify that all test processes exited
		ServiceEventWaitor<MIThreadGroupExitedEvent> procExitWaitor = new ServiceEventWaitor<>(fSession,
				MIThreadGroupExitedEvent.class);
		List<MIThreadGroupExitedEvent> exitEvents = procExitWaitor
				.waitForEvents(TestsPlugin.massageTimeout(1500));
		assertThat(exitEvents.size(), equalTo(testProcessList.size()));
	}
}