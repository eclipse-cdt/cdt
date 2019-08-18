/*******************************************************************************
 * Copyright (c) 2019 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Umair Sair (Mentor Graphics)  - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.mi.service.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests MIAsyncErrorProcessor for continue and step return command failure
 */
@RunWith(Parameterized.class)
public class MIAsyncErrorProcessorTests extends BaseParametrizedTestCase {

	private static final String EXEC_NAME = "MultiThread.exe";

	private MIRunControl runControl;
	private IGDBControl commandControl;
	private IContainerDMContext containerDmc;

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	@Override
	public void doBeforeTest() throws Exception {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_12);

		super.doBeforeTest();

		DsfSession session = getGDBLaunch().getSession();
		Runnable runnable = () -> {
			DsfServicesTracker servicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(),
					session.getId());
			assert (servicesTracker != null);

			runControl = servicesTracker.getService(MIRunControl.class);
			assert (runControl != null);

			commandControl = servicesTracker.getService(IGDBControl.class);
			assert (commandControl != null);

			servicesTracker.dispose();
		};

		session.getExecutor().submit(runnable).get();
		containerDmc = SyncUtil.getContainerContext();

		try {
			prepareEnvironment();
		} catch (Throwable e) {
			throw new Exception(e);
		}
	}

	private void prepareEnvironment() throws Throwable {
		AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		/*
		 * 1. Confirm that target is suspended initially
		 */
		assertTrue(runControl.isSuspended(containerDmc));

		/*
		 * 2. 'set confirm off' as we'll will be deleting all breakpoints latter using 'del' command
		 */
		commandControl.queueCommand(
				commandControl.getCommandFactory().createMIInterpreterExecConsole(containerDmc, "set confirm off"),
				new ImmediateDataRequestMonitor<MIInfo>() {
					@Override
					public void handleCompleted() {
						wait.waitFinished(getStatus());
					}
				});

		wait.waitUntilDone(TestsPlugin.massageTimeout(1000));
		wait.waitReset();

		/*
		 * 3. Add breakpoint at ThreadSetName symbol and resume so that we have deeper stack
		 */
		SyncUtil.addBreakpoint("ThreadSetName");
		performOperationThenResumeAndWaitForSuspend(SyncUtil::resume);

		/*
		 * 4. Add breakpoint at 0x0 which will cause continue and step return failure
		 */
		SyncUtil.addBreakpoint("*0x0", false);
	}

	@Test
	public void executeContinueTest() throws Throwable {
		performOperationThenResumeAndWaitForSuspend(SyncUtil::resume);

		assertTrue(isTargetStoppedWithError());
	}

	@Test
	public void executeStepReturnTest() throws Throwable {
		performOperationThenResumeAndWaitForSuspend(() -> SyncUtil.step(StepType.STEP_RETURN));

		assertTrue(isTargetStoppedWithError());
	}

	@Test
	public void executeContinueTestThenRemoveBreakpointsAndResume() throws Throwable {
		executeContinueTest();

		AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		/*
		 * Remove all breakpoints and resume. Target should be in resumed state now
		 */
		commandControl.queueCommand(
				commandControl.getCommandFactory().createMIInterpreterExecConsole(containerDmc, "del"),
				new ImmediateDataRequestMonitor<MIInfo>() {
					@Override
					public void handleCompleted() {
						wait.waitFinished(getStatus());
					}
				});

		wait.waitUntilDone(TestsPlugin.massageTimeout(1000));
		wait.waitReset();

		SyncUtil.resume();

		Thread.sleep(1000);

		assertFalse(runControl.isSuspended(containerDmc));
	}

	private void performOperationThenResumeAndWaitForSuspend(Operation operation) throws Throwable {
		ServiceEventWaitor<IContainerResumedDMEvent> resumeEventWaitor = new ServiceEventWaitor<>(
				getGDBLaunch().getSession(), IContainerResumedDMEvent.class);
		ServiceEventWaitor<MIStoppedEvent> stopEventWaitor = new ServiceEventWaitor<>(getGDBLaunch().getSession(),
				MIStoppedEvent.class);

		operation.run();

		resumeEventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));
		stopEventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));
		waitUntil("Waiting for suspend", () -> runControl.isSuspended(containerDmc), TestsPlugin.massageTimeout(1000));
	}

	private boolean isTargetStoppedWithError() throws Exception {
		if (!runControl.isSuspended(containerDmc))
			return false;

		Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				runControl.getExecutionData(containerDmc, new ImmediateDataRequestMonitor<IExecutionDMData>(rm) {
					@Override
					protected void handleCompleted() {
						rm.done(isSuccess() && getData().getStateChangeReason() == StateChangeReason.ERROR);
					}
				});
			}
		};

		runControl.getExecutor().execute(query);

		return query.get(TestsPlugin.massageTimeout(2000), TimeUnit.MILLISECONDS);
	}

	private interface Operation {
		void run() throws Throwable;
	}
}
