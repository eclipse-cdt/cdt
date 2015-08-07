/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *     Simon Marchi (Ericsson) - Check for thread name support, add thread name test.
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupExitedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class GDBProcessesTest extends BaseTestCase {
	/*
	 * Name of the executable
	 */
	protected static final String EXEC_NAME = "MultiThread.exe";
	protected static final String SOURCE_NAME = "MultiThread.cc";

	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();

		resolveLineTagLocations(SOURCE_NAME, MIRunControlTest.LINE_TAGS);

	    fSession = getGDBLaunch().getSession();
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
            	fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
            }
        };
        fSession.getExecutor().submit(runnable).get();
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();

		fServicesTracker.dispose();
	}
	
	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, 
				           EXEC_PATH + EXEC_NAME);
	}

	@Test
    /*
     *  Get the process data for the current program. Process is executable name in case of GDB back end
     */
	public void getProcessData() throws InterruptedException, ExecutionException, TimeoutException {
		/*
		 * Get process data. Name of the process is the executable name in case of GDB back-end.
		 */
		IThreadDMData processData = SyncUtil.getProcessData();
		Assert.assertNotNull("No process data is returned for Process DMC", processData);
		Assert.assertTrue("Process data should be executable name " + EXEC_NAME +
				"but we got " + processData.getName(), processData.getName().contains(EXEC_NAME));
	}

	/*
	 * Return whether thread names are reported by the debugger.
	 *
	 * This defaults to false, and is overridden for specific versions of gdb.
	 */
	protected boolean threadNamesSupported() {
		return false;
	}

	/* 
	 * getThreadData() for multiple threads
	 */
	@Test
	public void getThreadData() throws Throwable {
		// Start the threads one by one to make sure they are discovered by gdb in the right
		// order.
		for (int i = 0; i < 5; i++) {
			SyncUtil.runToLocation(SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_AFTER_THREAD_START"));

		}

		validateThreadData(SyncUtil.getContainerContext());
	}

	protected void validateThreadData(IContainerDMContext containerDmc)
			throws Throwable, InterruptedException, ExecutionException, TimeoutException {
		// We need to get there to make sure that all the threads have their name set.
		SyncUtil.runToLocation(containerDmc, SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_ALL_THREADS_STARTED"));

		IThreadDMData mainThreadData = SyncUtil.getThreadData(containerDmc, 1);

		// Test that thread id is only a series of numbers
		Pattern pattern = Pattern.compile("\\d*", Pattern.MULTILINE); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(mainThreadData.getId());
		assertTrue("Thread ID is a series of number", matcher.matches());

		// Check the thread names. We did not change the main thread's name, so
		// it should be the same as the executable name.
		final String names[] = { EXEC_NAME, "monday", "tuesday", "wednesday", "thursday", "friday" };

		// Check that we have correct data for PrintHello
		for (int i = 1; i <= 6; i++) {
			IThreadDMData threadData = SyncUtil.getThreadData(containerDmc, i);
			String name = threadData.getName();
			String expectedName = threadNamesSupported() ? names[i - 1] : "";
			assertEquals("Thread name of thread " + i, expectedName, name);
		}
	}
	
	@Test
	public void debugNewProcess() throws Throwable {
		boolean non_stop = getGDBLaunch().getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, false);
		if (!non_stop) {
			// Multi-process is only valid for non stop
			return;
		}
		
		int maxNumThreads = 5;
		// Keep track of all processes involved in this test
		List<IContainerDMContext> testProcessList = new ArrayList<IContainerDMContext>();
		
		// Process 1
		IContainerDMContext process1 = SyncUtil.getContainerContext();
		
		// Start the threads one by one to make sure they are discovered by gdb in the right
		// order.
		advanceAndValidateThreads(maxNumThreads, process1);
		System.out.println("Validation for process 1 passed");

		testProcessList.add(process1);

		// Process 2
		IContainerDMContext process2 = SyncUtil.debugNewProcess(getGDBLaunch(), EXEC_PATH + EXEC_NAME);
		testProcessList.add(process2);

		// Start a portion of threads one by one to make sure they are discovered by gdb in the right
		// order.
		advanceAndValidateThreads(maxNumThreads, process2);
		System.out.println("Validation for process 2 passed");

		
		// Containers
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
		SyncUtil.resume(process2); 
		SyncUtil.resume(process1); 

		// Verify that all test processes exited
		ServiceEventWaitor<MIThreadGroupExitedEvent> procExitWaitor = new ServiceEventWaitor<>(fSession,
				MIThreadGroupExitedEvent.class);
		List<MIThreadGroupExitedEvent> exitEvents = procExitWaitor
				.waitForEvents(TestsPlugin.massageTimeout(1500));
		assertThat(exitEvents.size(), equalTo(testProcessList.size()));
	}

	private void advanceAndValidateThreads(int maxNumThreads, IContainerDMContext process1)
			throws InterruptedException, ExecutionException, TimeoutException, Throwable {
		for (int i = 0; i < maxNumThreads; i++) {
			// Validate number of threads i.e. created by program + 1 for the main thread
			IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts(process1);
			assertEquals("Unexpected number of Threads running", i + 1, threads.length);
			//Advance Thread
			SyncUtil.runToLocation(process1, SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_AFTER_THREAD_START"));
		}

		validateThreadData(process1);
	}
}
