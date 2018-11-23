/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.extensions.GDBBackend_HEAD;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceFactoriesManager;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * The tests of this class use special versions of services.
 */
@RunWith(Parameterized.class)
public class MIModifiedServicesTest extends BaseParametrizedTestCase {

	private IMIRunControl fRunCtrl;

	private IContainerDMContext fContainerDmc;
	private IExecutionDMContext fThreadExecDmc;
	private IGDBBackend fBackend;

	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "MultiThread.exe";
	private static final String SOURCE_NAME = "MultiThread.cc";

	/**
	 * A backend class that disables the full GDB console and uses the basic console instead.
	 */
	private class TestBackendBasicConsole extends GDBBackend_HEAD {
		public TestBackendBasicConsole(DsfSession session, ILaunchConfiguration lc) {
			super(session, lc);
		}

		@Override
		public boolean isFullGdbConsoleSupported() {
			return false;
		}
	}

	/**
	 * A services factory that uses the test backend service that instantiates
	 * a basic console instead of a full console.
	 */
	private class TestServicesFactoryBasicConsole extends GdbDebugServicesFactory {
		public TestServicesFactoryBasicConsole(String version, ILaunchConfiguration config) {
			super(version, config);
		}

		@Override
		protected IMIBackend createBackendGDBService(DsfSession session, ILaunchConfiguration lc) {
			if (compareVersionWith(GDB_7_12_VERSION) >= 0) {
				return new TestBackendBasicConsole(session, lc);
			}
			return super.createBackendGDBService(session, lc);
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
		resolveLineTagLocations(SOURCE_NAME, MIRunControlTest.LINE_TAGS);
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
					Assert.fail(
							"Problem inspecting file to see if it's a cygwin executable : " + e.getLocalizedMessage());
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

		fContainerDmc = SyncUtil.getContainerContext();

		final DsfSession session = getGDBLaunch().getSession();

		Query<Void> query = new Query<Void>() {
			@Override
			protected void execute(DataRequestMonitor<Void> rm) {
				DsfServicesTracker servicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(),
						session.getId());

				IMIProcesses procService = servicesTracker.getService(IMIProcesses.class);
				IProcessDMContext procDmc = DMContexts.getAncestorOfType(fContainerDmc, IProcessDMContext.class);
				IThreadDMContext threadDmc = procService.createThreadContext(procDmc, "1");
				fThreadExecDmc = procService.createExecutionContext(fContainerDmc, threadDmc, "1");

				fRunCtrl = servicesTracker.getService(IMIRunControl.class);
				fBackend = servicesTracker.getService(IGDBBackend.class);

				servicesTracker.dispose();

				rm.done();
			}
		};
		session.getExecutor().execute(query);
		query.get(TestsPlugin.massageTimeout(5000), TimeUnit.MILLISECONDS);

	}

	protected void registerServicesFactoryForBasicConsole() throws CoreException {
		// Resolve a unique id for the Test Debug services factory
		String servicesFactoryId = this.getClass().getName() + "#" + testName.getMethodName();

		// Register this test case factory
		getServiceFactoriesManager().addTestServicesFactory(servicesFactoryId,
				new TestServicesFactoryBasicConsole(getGdbVersion(), getLaunchConfiguration()));

		// Register the factory id using a launch attribute, so it can be later resolved
		// e.g. by a test launch delegate
		setLaunchAttribute(ServiceFactoriesManager.DEBUG_SERVICES_FACTORY_KEY, servicesFactoryId);
	}

	private void resumeContainerContextExe() throws InterruptedException, ExecutionException, TimeoutException {

		final ServiceEventWaitor<IResumedDMEvent> resumedWaitor = new ServiceEventWaitor<>(getGDBLaunch().getSession(),
				IResumedDMEvent.class);

		Query<Void> query = new Query<Void>() {
			@Override
			protected void execute(DataRequestMonitor<Void> rm) {
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

		Query<Boolean> querySuspend = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				rm.done(fRunCtrl.isSuspended(fContainerDmc));
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
	 * Note: This test case uses a modified Test Backend service which is instrumented before test execution,
	 * see initializeLaunchAttributes
	 */
	private void interruptRunningTargetExe()
			throws InterruptedException, Exception, ExecutionException, TimeoutException {
		ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<>(
				getGDBLaunch().getSession(), ISuspendedDMEvent.class);

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
		Query<Boolean> querySuspend = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				rm.done(fRunCtrl.isSuspended(fContainerDmc));
			}
		};

		fRunCtrl.getExecutor().execute(querySuspend);
		Boolean suspended = querySuspend.get(TestsPlugin.massageTimeout(5000), TimeUnit.MILLISECONDS);

		Assert.assertTrue("Target is running. It should have been suspended", suspended);
	}

	/**
	 * Validate that with the basic console, GDB will not accept commands while the target is running.
	 * This would not be feasible to test with our standard services. This test case forces the use of the Basic
	 * console by using a test IGDBBackEnd service.
	 */
	@Test
	public void doNotAcceptCommandsWhenTargetRunning_BasicConsole() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_12);

		registerServicesFactoryForBasicConsole();

		doLaunch();

		// Make sure we are using our test version of IGDBBackEnd
		assertTrue("Not using the expected backend service", fBackend instanceof TestBackendBasicConsole);

		resumeContainerContextExe();

		// Verify that with the basic console, we cannot accept commands when the target is running.
		assertFalse("Target should be running with async off, and should NOT be accepting commands",
				fRunCtrl.isTargetAcceptingCommands());
	}

	/**
	 * Validate that with the basic console, interrupting a running target does work.
	 * This would not be feasible to test with our standard services.
	 * This test case forces the use of the Basic console by using a test IGDBBackEnd service.
	 */
	@Test
	public void interruptRunningTarget_BasicConsole() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_12);

		registerServicesFactoryForBasicConsole();

		doLaunch();

		// Make sure we are using our test version of IGDBBackEnd
		assertTrue("Not using the expected backend service", fBackend instanceof TestBackendBasicConsole);

		resumeContainerContextExe();

		interruptRunningTargetExe();
	}
}
