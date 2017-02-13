/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.gdb.service.extensions.GDBBackend_HEAD;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceFactoriesManager;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.tests.dsf.gdb.tests.MIRunControlSimServicesTest.TestServicesFactory.TestBackEndBasicConsole;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests MIRunControlSimServicesTest, validating the behavior when the GDBBasicClionsole is in use
 */
@RunWith(Parameterized.class)
public class MIRunControlSimServicesTest extends BaseParametrizedTestCase {

	private IMIRunControl fRunCtrl;

	private IContainerDMContext fContainerDmc;
	private IExecutionDMContext fThreadExecDmc;
	private IGDBBackend fBackEnd;

	// Breakpoint tags in MultiThread.cc
	public static final String[] LINE_TAGS = new String[] { "LINE_MAIN_BEFORE_THREAD_START", // Just before
																								// StartThread
			"LINE_MAIN_AFTER_THREAD_START", // Just after StartThread
			"LINE_MAIN_ALL_THREADS_STARTED", // Where all threads are guaranteed to be started.
	};

	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "MultiThread.exe";
	private static final String SOURCE_NAME = "MultiThread.cc";

	public class TestServicesFactory extends GdbDebugServicesFactory {
		public TestServicesFactory(String version, ILaunchConfiguration config) {
			super(version, config);
		}

		@Override
		protected IMIBackend createBackendGDBService(DsfSession session, ILaunchConfiguration lc) {
			if (compareVersionWith(GDB_7_12_VERSION) >= 0) {
				return new TestBackEndBasicConsole(session, lc);
			}
			return super.createBackendGDBService(session, lc);
		}

		public class TestBackEndBasicConsole extends GDBBackend_HEAD {
			public TestBackEndBasicConsole(DsfSession session, ILaunchConfiguration lc) {
				super(session, lc);
			}

			@Override
			public boolean isFullGdbConsoleSupported() {
				return false;
			}
		}
	}

	@Override
	public void doBeforeTest() throws Exception {
		removeTeminatedLaunchesBeforeTest();
		setLaunchAttributes();

		// Can't run the launch right away because each test needs to first set some
		// parameters. The individual tests will be responsible for starting the launch.

		// Looks up line tags in source file(s).
		clearLineTags();
		resolveLineTagLocations(SOURCE_NAME, LINE_TAGS);
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);

		// This is crude, but effective. We need to determine if the program was
		// built with cygwin. The easiest way is to scan the binary file looking
		// for 'cygwin1.dll'. In the real world, this wouldn't cut mustard, but
		// since this is just testing code, and we control the programs, it's a
		// no brainer.
		if (runningOnWindows()) {

			// This is interesting. Our tests rely on the working directory.
			// That is, we specify a program path in the launch configuration
			// that is relative to the working directory.
			File file = new File(EXEC_PATH + EXEC_NAME);

			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				Assert.fail(e.getLocalizedMessage());
				return; // needed to avoid warning at fis usage below
			}

			final String MATCH = "cygwin1.dll";
			final int MATCH_LEN = MATCH.length();
			int i = 0;
			int ch = 0;
			while (true) {
				try {
					ch = fis.read();
				} catch (IOException e) {
					Assert.fail("Problem inspecting file to see if it's a cygwin executable : "
							+ e.getLocalizedMessage());
				}
				if (ch == -1) { // EOF
					break;
				}
				if (ch == MATCH.charAt(i)) {
					if (i == MATCH_LEN - 1) {
						break; // found it!
					}
					i++;
				} else {
					i = 0;
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}

	// This method cannot be tagged as @Before, because the launch is not
	// running yet. We have to call this manually after all the proper
	// parameters have been set for the launch
	@Override
	protected void doLaunch() throws Exception {
		// perform the launch
		super.doLaunch();

		final DsfSession session = getGDBLaunch().getSession();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				DsfServicesTracker servicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(),
						session.getId());
				IGDBControl gdbCtrl = servicesTracker.getService(IGDBControl.class);

				IMIProcesses procService = servicesTracker.getService(IMIProcesses.class);
				IProcessDMContext procDmc = procService.createProcessContext(gdbCtrl.getContext(),
						MIProcesses.UNIQUE_GROUP_ID);
				fContainerDmc = procService.createContainerContext(procDmc, MIProcesses.UNIQUE_GROUP_ID);
				IThreadDMContext threadDmc = procService.createThreadContext(procDmc, "1");
				fThreadExecDmc = procService.createExecutionContext(fContainerDmc, threadDmc, "1");

				fRunCtrl = servicesTracker.getService(IMIRunControl.class);
				fBackEnd = servicesTracker.getService(IGDBBackend.class);

				servicesTracker.dispose();
			}
		};
		session.getExecutor().submit(runnable).get();
	}

	protected void registerSimFactory1() throws CoreException {
		// Resolve a unique id for the Test Debug services factory
		String servicesFactoryId = this.getClass().getName() + "#" + testName.getMethodName();

		// Register this test case factory
		getServiceFactoriesManager().addTestServicesFactory(servicesFactoryId,
				new TestServicesFactory(getGdbVersion(), getLaunchConfiguration()));

		// Register the factory id using a launch attribute, so it can be later resolved
		// e.g. by a test launch delegate
		setLaunchAttribute(ServiceFactoriesManager.DEBUG_SERVICES_FACTORY_KEY, servicesFactoryId);
	}

	private void resumeContainerContextExe()
			throws InterruptedException, ExecutionException, TimeoutException {

		final ServiceEventWaitor<IResumedDMEvent> resumedWaitor = new ServiceEventWaitor<IResumedDMEvent>(
				getGDBLaunch().getSession(), IResumedDMEvent.class);

		Query<MIInfo> query = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fRunCtrl.resume(fContainerDmc, rm);
			}
		};

		fRunCtrl.getExecutor().execute(query);
		query.get(TestsPlugin.massageTimeout(5000), TimeUnit.MILLISECONDS);

		try {
			resumedWaitor.waitForEvent(TestsPlugin.massageTimeout(5000));
		} catch (Exception e) {
			Assert.fail("Exception raised:: " + e.getMessage());
			e.printStackTrace();
			return;
		}

		final IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		Query<Boolean> querySuspend = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				rm.setData(fRunCtrl.isSuspended(containerDmc));
				rm.done();
			}
		};

		fRunCtrl.getExecutor().execute(querySuspend);
		Boolean suspended = querySuspend.get(TestsPlugin.massageTimeout(5000), TimeUnit.MILLISECONDS);
		Assert.assertFalse("Target is suspended. It should have been running", suspended);
	}

	/**
	 * Validate we can interrupt via the CLI, This would not be feasible to test in the traditional test
	 * environment e.g. Linux with GDB 7.12 This test case forces the use of the Basic console which triggers
	 * async mode off, and will cause the program interrupt via the CLI (rather than MI -exec-interrupt which
	 * is used with async mode)
	 * 
	 * Note: This test case uses a modified Test BackEnd service which is instrumented before test execution,
	 * see initializeLaunchAttributes
	 */
	private void interruptRunningTargetExe()
			throws InterruptedException, Exception, ExecutionException, TimeoutException {

		ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
				getGDBLaunch().getSession(), ISuspendedDMEvent.class);

		Query<MIInfo> queryResume = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fRunCtrl.resume(fContainerDmc, rm);
			}
		};

		fRunCtrl.getExecutor().execute(queryResume);
		queryResume.get(TestsPlugin.massageTimeout(5000), TimeUnit.MILLISECONDS);

		// Wait one second and attempt to interrupt the target.
		// As of gdb 7.8, interrupting execution after a thread exit does not
		// work well. This test works around it by interrupting before threads
		// exit. Once the bug in gdb is fixed, we should add a test that
		// interrupts after the threads exit.
		// Ref: https://sourceware.org/bugzilla/show_bug.cgi?id=17627
		Thread.sleep(1000);

		Query<MIInfo> requestSuspend = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fRunCtrl.suspend(fThreadExecDmc, rm);
			}
		};

		fRunCtrl.getExecutor().execute(requestSuspend);
		requestSuspend.get(TestsPlugin.massageTimeout(5000), TimeUnit.MILLISECONDS);

		// Wait up to 2 seconds for the target to suspend. Should happen immediately.
		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));

		// Double check that the target is in the suspended state
		final IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		Query<Boolean> querySuspend = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				rm.setData(fRunCtrl.isSuspended(containerDmc));
				rm.done();
			}
		};

		fRunCtrl.getExecutor().execute(querySuspend);
		Boolean suspended = querySuspend.get(TestsPlugin.massageTimeout(5000), TimeUnit.MILLISECONDS);

		Assert.assertTrue("Target is running. It should have been suspended", suspended);
	}

	/**
	 * Validate we resume and then terminate a running program via the CLI, This would not be feasible to test
	 * in the traditional test environment e.g. Linux with GDB 7.12 This test case forces the use of the Basic
	 * console which triggers async mode off, and will cause the program interrupt via the CLI (rather than MI
	 * -exec-interrupt which is used with async mode)
	 * 
	 * Note: This test case uses a modified Test IGDBBackEnd services
	 * 
	 * @throws Throwable
	 */
	@Test
	public void resumeContainerContext_CLI() throws Throwable {
		registerSimFactory1();

		doLaunch();

		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_12);

		// Make sure we are using our test version of IGDBBackEnd
		assertTrue(fBackEnd instanceof TestBackEndBasicConsole);

		resumeContainerContextExe();
		assertFalse("Target should be running with async off, and shall NOT be accepting commands",
				fRunCtrl.isTargetAcceptingCommands());
	}

	/**
	 * Validate we can interrupt via the CLI, This would not be feasible to test in the traditional test
	 * environment e.g. Linux with GDB 7.12 This test case forces the use of the Basic console which triggers
	 * async mode off, and will cause the program interrupt via the CLI (rather than MI -exec-interrupt which
	 * is used with async mode)
	 * 
	 * Note: This test case uses a modified Test IGDBBackEnd services
	 * 
	 */
	@Test
	public void interruptRunningTarget_CLI() throws Throwable {
		registerSimFactory1();

		doLaunch();
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_12);

		// Make sure we are using our test version of IGDBBackEnd
		assertTrue(fBackEnd instanceof TestBackEndBasicConsole);

		interruptRunningTargetExe();
	}
}
