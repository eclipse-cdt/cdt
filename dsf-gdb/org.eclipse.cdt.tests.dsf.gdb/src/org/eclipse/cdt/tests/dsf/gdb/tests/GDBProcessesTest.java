/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *     Simon Marchi (Ericsson) - Check for thread name support, add thread name test.
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GDBProcessesTest extends BaseParametrizedTestCase {
	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "MultiThread.exe";
	private static final String SOURCE_NAME = "MultiThread.cc";

	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;

	private IMIProcesses fProcService;

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();

		resolveLineTagLocations(SOURCE_NAME, MIRunControlTest.LINE_TAGS);

		fSession = getGDBLaunch().getSession();
		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
			fProcService = fServicesTracker.getService(IMIProcesses.class);
		};
		fSession.getExecutor().submit(runnable).get();
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();

		fProcService = null;
		if (fServicesTracker != null)
			fServicesTracker.dispose();
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	@Test
	/*
	 *  Get the process data for the current program. Process is executable name in case of GDB back end
	 */
	public void getProcessData() throws InterruptedException, ExecutionException, TimeoutException {
		final IProcessDMContext processContext = DMContexts.getAncestorOfType(SyncUtil.getContainerContext(),
				IProcessDMContext.class);
		Query<IThreadDMData> query = new Query<IThreadDMData>() {
			@Override
			protected void execute(DataRequestMonitor<IThreadDMData> rm) {
				fProcService.getExecutionData(processContext, rm);
			}
		};

		fProcService.getExecutor().execute(query);

		/*
		 * Get process data. Name of the process is the executable name in case of GDB back-end.
		 */
		IThreadDMData processData = query.get(TestsPlugin.massageTimeout(2000), TimeUnit.MILLISECONDS);
		Assert.assertNotNull("No process data is returned for Process DMC", processData);
		Assert.assertTrue("Process data should be executable name " + EXEC_NAME + "but we got " + processData.getName(),
				processData.getName().contains(EXEC_NAME));
	}

	/*
	 * Return whether thread names are reported by the debugger.
	 */
	protected boolean threadNamesSupported() {
		return !runningOnWindows() && !isRemoteSession();
	}

	/*
	 * getThreadData() for multiple threads
	 */
	@Test
	public void getThreadData() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_3);
		// Start the threads one by one to make sure they are discovered by gdb in the right
		// order.
		for (int i = 0; i < 5; i++) {
			SyncUtil.runToLocation(SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_AFTER_THREAD_START"));

		}

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
