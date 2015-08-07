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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
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

		validateThreadData();
	}

	protected void validateThreadData()
			throws Throwable, InterruptedException, ExecutionException, TimeoutException {
		// We need to get there to make sure that all the threads have their name set.
		SyncUtil.runToLocation(SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_ALL_THREADS_STARTED"));

		IThreadDMData mainThreadData = SyncUtil.getThreadData(1);

		// Test that thread id is only a series of numbers
		Pattern pattern = Pattern.compile("\\d*", Pattern.MULTILINE); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(mainThreadData.getId());
		assertTrue("Thread ID is a series of number", matcher.matches());

		// Check the thread names. We did not change the main thread's name, so
		// it should be the same as the executable name.
		final String names[] = { EXEC_NAME, "monday", "tuesday", "wednesday", "thursday", "friday" };

		// Check that we have correct data for PrintHello
		for (int i = 1; i <= 6; i++) {
			IThreadDMData threadData = SyncUtil.getThreadData(i);
			String name = threadData.getName();
			String expectedName = threadNamesSupported() ? names[i - 1] : "";
			assertEquals("Thread name of thread " + i, expectedName, name);
		}
	}
}
