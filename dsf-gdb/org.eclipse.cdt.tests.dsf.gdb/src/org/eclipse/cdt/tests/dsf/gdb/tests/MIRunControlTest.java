/*******************************************************************************
 * Copyright (c) 2007, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson AB - Initial implementation of Test cases
 *     Simon Marchi (Ericsson) - Add and use runningOnWindows().
 *     Simon Marchi (Ericsson) - Adapt test code to thread platform compatibility layer.
 *     Simon Marchi (Ericsson) - Change breakpoint line numbers.
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
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
import org.eclipse.core.runtime.IStatus;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests MIRunControl class for Multi-threaded application.
 */
@RunWith(Parameterized.class)
public class MIRunControlTest extends BaseParametrizedTestCase {

	/**
	 * The cygwin runtime/emulation spawns a thread, so even the most basic
	 * program has two threads. The tests have to take this into consideration
	 * since the same is not true in other environments (POSIX, MinGW). We
	 * examine the test program and set this flag to true if it uses the cygwin
	 * dll.
	 */
	private static boolean sProgramIsCygwin;

	private DsfServicesTracker fServicesTracker;

	private IGDBControl fGDBCtrl;
	private IMIRunControl fRunCtrl;
	private IGDBBackend fBackEnd;

	private IContainerDMContext fContainerDmc;
	private IExecutionDMContext fThreadExecDmc;

	// Breakpoint tags in MultiThread.cc
	public static final String[] LINE_TAGS = new String[] { "LINE_MAIN_BEFORE_THREAD_START", // Just before StartThread
			"LINE_MAIN_AFTER_THREAD_START", // Just after StartThread
			"LINE_MAIN_ALL_THREADS_STARTED", // Where all threads are guaranteed to be started.
	};

	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "MultiThread.exe";
	private static final String SOURCE_NAME = "MultiThread.cc";

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();

		resolveLineTagLocations(SOURCE_NAME, LINE_TAGS);

		final DsfSession session = getGDBLaunch().getSession();

		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), session.getId());
			fGDBCtrl = fServicesTracker.getService(IGDBControl.class);

			IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
			IProcessDMContext procDmc = procService.createProcessContext(fGDBCtrl.getContext(),
					MIProcesses.UNIQUE_GROUP_ID);
			fContainerDmc = procService.createContainerContext(procDmc, MIProcesses.UNIQUE_GROUP_ID);
			IThreadDMContext threadDmc = procService.createThreadContext(procDmc, "1");
			fThreadExecDmc = procService.createExecutionContext(fContainerDmc, threadDmc, "1");

			fRunCtrl = fServicesTracker.getService(IMIRunControl.class);
			fBackEnd = fServicesTracker.getService(IGDBBackend.class);
		};
		session.getExecutor().submit(runnable).get();
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();

		if (fServicesTracker != null)
			fServicesTracker.dispose();
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
						sProgramIsCygwin = true;
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

	/*
	 * For Multi-threaded application - In case of one thread, Thread id should start with 1.
	 */
	@Test
	public void getExecutionContext() throws InterruptedException, ExecutionException, TimeoutException {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		/*
		 * Create a request monitor
		 */
		final DataRequestMonitor<IExecutionDMContext[]> rm = new DataRequestMonitor<IExecutionDMContext[]>(
				fRunCtrl.getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					wait.setReturnInfo(getData());
				}
				wait.waitFinished(getStatus());
			}
		};

		final IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		/*
		 * Test getExecutionContexts() when only one thread exist.
		 */
		fRunCtrl.getExecutor().submit(() -> fRunCtrl.getExecutionContexts(containerDmc, rm));
		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		Assert.assertTrue(wait.getMessage(), wait.isOK());

		/*
		 * Get data from the Request Monitor
		 */
		IExecutionDMContext[] ctxts = (IExecutionDMContext[]) wait.getReturnInfo();

		// Context can not be null
		Assert.assertNotNull(ctxts);
		Assert.assertEquals("Unexpected number of threads for a simple program", sProgramIsCygwin ? 2 : 1,
				ctxts.length);

		// The ordering of the contexts is not deterministic
		LinkedList<Integer> ids = new LinkedList<>(Arrays.asList(new Integer[] { 1 }));
		if (sProgramIsCygwin) {
			ids.add(2);
		}

		// Note that List.remove(int) and List.remove(Integer) have different effects so this should stay remove(Integer)
		assertTrue(ids.remove(Integer.valueOf(((IMIExecutionDMContext) ctxts[0]).getThreadId())));
		if (sProgramIsCygwin) {
			assertTrue(ids.remove(Integer.valueOf(((IMIExecutionDMContext) ctxts[1]).getThreadId())));
		}

		wait.waitReset();
	}

	/*
	 * Get Execution DMCs for a valid container DMC
	 * Testing for two execution DMC with id 1 & 2
	 */
	@Test
	public void getExecutionContexts() throws Throwable {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		/*
		 * Create a request monitor
		 */
		final DataRequestMonitor<IExecutionDMContext[]> rmExecutionCtxts = new DataRequestMonitor<IExecutionDMContext[]>(
				fRunCtrl.getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					wait.setReturnInfo(getData());
				}
				wait.waitFinished(getStatus());
			}
		};

		// Prepare a waiter to make sure we have received the thread started event
		final ServiceEventWaitor<IStartedDMEvent> startedEventWaitor = new ServiceEventWaitor<>(
				getGDBLaunch().getSession(), IStartedDMEvent.class);

		SyncUtil.runToLocation(SOURCE_NAME + ':' + getLineForTag("LINE_MAIN_AFTER_THREAD_START"));

		final IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		/*
		 * Test getExecutionContexts for a valid container DMC
		 */
		fRunCtrl.getExecutor().submit(() -> fRunCtrl.getExecutionContexts(containerDmc, rmExecutionCtxts));
		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		Assert.assertTrue(wait.getMessage(), wait.isOK());
		wait.waitReset();

		// Make sure thread started event was received
		// We check this _after_ we ask for the execution contexts because when running remote (i.e., with gdbserver),
		// thread events are not sent by gdb until a request for a thread list is given (Bug 455992)
		IStartedDMEvent startedEvent = startedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));
		Assert.assertEquals("Thread created event is for wrong thread id", sProgramIsCygwin ? "3" : "2",
				((IMIExecutionDMContext) startedEvent.getDMContext()).getThreadId());

		/*
		 * Get data
		 */
		IExecutionDMContext[] data = rmExecutionCtxts.getData();
		/*
		 * Contexts returned can not be null
		 */
		Assert.assertNotNull(data);

		Assert.assertEquals("Unexpected number of threads", sProgramIsCygwin ? 3 : 2, data.length);

		// The ordering of the contexts is not deterministic
		LinkedList<Integer> ids = new LinkedList<>(Arrays.asList(new Integer[] { 1, 2 }));
		if (sProgramIsCygwin) {
			ids.add(3);
		}

		assertTrue(ids.remove(Integer.valueOf(((IMIExecutionDMContext) data[0]).getThreadId())));
		assertTrue(ids.remove(Integer.valueOf(((IMIExecutionDMContext) data[1]).getThreadId())));
		if (sProgramIsCygwin) {
			assertTrue(ids.remove(Integer.valueOf(((IMIExecutionDMContext) data[2]).getThreadId())));
		}
	}

	/*
	 * Testing getModelData() for ExecutionDMC
	 */
	@Test
	public void getModelDataForThread() throws InterruptedException, ExecutionException, TimeoutException {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		/*
		 * Create a request monitor
		 */
		final DataRequestMonitor<IExecutionDMData> rm = new DataRequestMonitor<IExecutionDMData>(fRunCtrl.getExecutor(),
				null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					wait.setReturnInfo(getData());
				}
				wait.waitFinished(getStatus());
			}
		};

		final IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		/*
		 * Call getModelData for Execution DMC
		 */
		fRunCtrl.getExecutor().submit(() -> fRunCtrl
				.getExecutionData(((MIRunControl) fRunCtrl).createMIExecutionContext(containerDmc, "1"), rm));
		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		Assert.assertTrue(wait.getMessage(), wait.isOK());

		IRunControl.IExecutionDMData data = rm.getData();
		if (data == null)
			Assert.fail("No data returned.");
		else {
			/*
			 * getModelData should return StateChangeReason.
			 */
			Assert.assertEquals("Unexpected state change reason.", StateChangeReason.BREAKPOINT,
					data.getStateChangeReason());
		}
	}

	/**
	 * Allows subclasses to override the expected reason for the stop on main.
	 * @return
	 */
	protected StateChangeReason getExpectedMainThreadStopReason() {
		return StateChangeReason.USER_REQUEST;
	}

	@Test
	public void getModelDataForThreadWhenStep() throws Throwable {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		/*
		 * Run till step returns
		 */
		final MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_OVER);

		final DataRequestMonitor<IExecutionDMData> rm = new DataRequestMonitor<IExecutionDMData>(fRunCtrl.getExecutor(),
				null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					wait.setReturnInfo(getData());
				}
				wait.waitFinished(getStatus());
			}
		};
		/*
		 * getModelData for Execution DMC
		 */
		fRunCtrl.getExecutor().submit(() -> fRunCtrl.getExecutionData(stoppedEvent.getDMContext(), rm));
		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		Assert.assertTrue(wait.getMessage(), wait.isOK());

		IRunControl.IExecutionDMData data = rm.getData();
		if (data == null)
			Assert.fail("No data Returned.");
		else {
			/*
			 * getModelData for Execution DMC in case Step has been performed.
			 */
			Assert.assertTrue("getModelData for ExecutionDMC in case of step should be STEP.",
					StateChangeReason.STEP == data.getStateChangeReason());
		}
	}

	/*
	 * getModelData() for ExecutionDMC when a breakpoint is hit
	 */
	@Test
	public void getModelDataForThreadWhenBreakpoint() throws Throwable {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		/*
		 * Add a breakpoint
		 */
		SyncUtil.addBreakpoint(SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_BEFORE_THREAD_START"), false);

		/*
		 * Resume till the breakpoint is hit
		 */
		final MIStoppedEvent stoppedEvent = SyncUtil.resumeUntilStopped();

		final DataRequestMonitor<IExecutionDMData> rm = new DataRequestMonitor<IExecutionDMData>(fRunCtrl.getExecutor(),
				null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					wait.setReturnInfo(getData());
				}
				wait.waitFinished(getStatus());
			}
		};
		fRunCtrl.getExecutor().submit(() -> fRunCtrl.getExecutionData(stoppedEvent.getDMContext(), rm));
		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		Assert.assertTrue(wait.getMessage(), wait.isOK());

		IRunControl.IExecutionDMData data = rm.getData();
		if (data == null)
			Assert.fail("No data Returned.");
		else {
			/*
			 * getModelData for ExecutionDMC in case a breakpoint is hit
			 */
			Assert.assertTrue("getModelData for an Execution DMC when a breakpoint is hit is not BREAKPOINT and is "
					+ data.getStateChangeReason(), StateChangeReason.BREAKPOINT == data.getStateChangeReason());
		}
	}

	/*
	 * getModelData() for Container DMC
	 */
	@Test
	public void getModelDataForContainer() throws Throwable {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		/*
		 * Add a breakpoint
		 */
		SyncUtil.addBreakpoint(SOURCE_NAME + ":21", false);
		/*
		 * Resume till the breakpoint is hit
		 */
		SyncUtil.resumeUntilStopped();

		final DataRequestMonitor<IExecutionDMData> rm = new DataRequestMonitor<IExecutionDMData>(fRunCtrl.getExecutor(),
				null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					wait.setReturnInfo(getData());
				}
				wait.waitFinished(getStatus());
			}
		};

		fRunCtrl.getExecutor().submit(() -> fRunCtrl.getExecutionData(fContainerDmc, rm));
		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		Assert.assertTrue(wait.getMessage(), wait.isOK());

		IRunControl.IExecutionDMData data = rm.getData();
		if (data == null)
			Assert.fail("No data returned.");
		else {
			Assert.assertTrue(" State change reason for a normal execution should be BREAKPOINT instead of "
					+ data.getStateChangeReason(), StateChangeReason.BREAKPOINT == data.getStateChangeReason());
		}
	}

	/*
	 * getExecutionContexts for an invalid container DMC
	 */
	@Ignore
	@Test
	public void getExecutionContextsForInvalidContainerDMC() throws InterruptedException {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		final DataRequestMonitor<IExecutionDMContext[]> rm = new DataRequestMonitor<IExecutionDMContext[]>(
				fRunCtrl.getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					wait.setReturnInfo(getData());
				}
				wait.waitFinished(getStatus());
			}
		};
		//        final IContainerDMContext ctxt = new GDBControlDMContext("-1", getClass().getName() + ":" + 1);
		fRunCtrl.getExecutor().submit(() -> fRunCtrl.getExecutionContexts(fContainerDmc, rm));
		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		Assert.assertTrue(wait.getMessage(), !wait.isOK());

		IStatus status = rm.getStatus();
		Assert.assertEquals("Error message for invalid container", IStatus.ERROR, status.getSeverity());
	}

	/*
	 * Cache after ContainerSuspendEvent should be re-set
	 */
	@Test
	public void cacheAfterContainerSuspendEvent() throws InterruptedException {
		/*
		 * Step to fire ContainerSuspendEvent
		 */
		try {
			SyncUtil.step(StepType.STEP_OVER);
		} catch (Throwable e) {
			Assert.fail("Exception in SyncUtil.SyncStep: " + e.getMessage());
		}
		/*
		 * Cache should be re-set
		 */
		//TODO TRy going to back end and fetching values instead
		//Assert.assertEquals(fRunCtrl.getCache().getCachedContext().size(), 0);
	}

	//Also test Cache after ContainerResumeEvent
	@Test
	public void resume() throws InterruptedException, ExecutionException, TimeoutException {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		final DataRequestMonitor<MIInfo> rm = new DataRequestMonitor<MIInfo>(fRunCtrl.getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				wait.waitFinished(getStatus());
				//TestsPlugin.debug("handleCompleted over");
			}
		};
		final ServiceEventWaitor<IResumedDMEvent> eventWaitor = new ServiceEventWaitor<>(getGDBLaunch().getSession(),
				IResumedDMEvent.class);

		final IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		fRunCtrl.getExecutor().submit(() -> fRunCtrl.resume(containerDmc, rm));
		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));

		try {
			eventWaitor.waitForEvent(TestsPlugin.massageTimeout(5000));
		} catch (Exception e) {
			Assert.fail("Exception raised:: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		Assert.assertTrue(wait.getMessage(), wait.isOK());

		wait.waitReset();

		fRunCtrl.getExecutor().submit(() -> {
			wait.setReturnInfo(fRunCtrl.isSuspended(containerDmc));
			wait.waitFinished();
		});

		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		Assert.assertFalse("Target is suspended. It should have been running", (Boolean) wait.getReturnInfo());

		wait.waitReset();
	}

	@Test
	public void resumeContainerContext() throws InterruptedException, ExecutionException, TimeoutException {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		final DataRequestMonitor<MIInfo> rm = new DataRequestMonitor<MIInfo>(fRunCtrl.getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				wait.waitFinished(getStatus());
			}
		};

		final ServiceEventWaitor<IResumedDMEvent> eventWaitor = new ServiceEventWaitor<>(getGDBLaunch().getSession(),
				IResumedDMEvent.class);

		fRunCtrl.getExecutor().submit(() -> fRunCtrl.resume(fContainerDmc, rm));
		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		try {
			eventWaitor.waitForEvent(TestsPlugin.massageTimeout(5000));
			//TestsPlugin.debug("DsfMIRunningEvent received");
		} catch (Exception e) {
			Assert.fail("Exception raised:: " + e.getMessage());
			e.printStackTrace();
			return;
		}

		Assert.assertTrue(wait.getMessage(), wait.isOK());

		wait.waitReset();

		final IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		fRunCtrl.getExecutor().submit(() -> {
			wait.setReturnInfo(fRunCtrl.isSuspended(containerDmc));
			wait.waitFinished();
		});

		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		Assert.assertFalse("Target is suspended. It should have been running", (Boolean) wait.getReturnInfo());

		wait.waitReset();

		// In all-stop mode, MI async is active when the Full GDB console is used
		if (fBackEnd.isFullGdbConsoleSupported()) {
			assertTrue("Target should be running with async on, and shall be accepting commands",
					fRunCtrl.isTargetAcceptingCommands());
			return;
		}

		assertFalse("Target should be running with async off, and shall NOT be accepting commands",
				fRunCtrl.isTargetAcceptingCommands());
	}

	@Test
	public void runToLine() throws Throwable {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<>(
				getGDBLaunch().getSession(), ISuspendedDMEvent.class);

		fRunCtrl.getExecutor().submit(() -> fRunCtrl.runToLine(fThreadExecDmc, SOURCE_NAME,
				getLineForTag("LINE_MAIN_ALL_THREADS_STARTED"), true, new RequestMonitor(fRunCtrl.getExecutor(), null) {
					@Override
					protected void handleCompleted() {
						wait.waitFinished(getStatus());
					}
				}));

		wait.waitUntilDone(TestsPlugin.massageTimeout(1000));
		Assert.assertTrue(wait.getMessage(), wait.isOK());
		wait.waitReset();

		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(10000));
		final IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		fRunCtrl.getExecutor().submit(() -> {
			wait.setReturnInfo(fRunCtrl.isSuspended(containerDmc));
			wait.waitFinished();
		});

		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		Assert.assertTrue("Target is running. It should have been suspended", (Boolean) wait.getReturnInfo());

		wait.waitReset();
	}

	/**
	 * Test that interrupting a running target works
	 */
	@Test
	public void interruptRunningTarget() throws Throwable {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<>(
				getGDBLaunch().getSession(), ISuspendedDMEvent.class);

		// Resume the target
		fRunCtrl.getExecutor()
				.submit(() -> fRunCtrl.resume(fThreadExecDmc, new RequestMonitor(fRunCtrl.getExecutor(), null) {
					@Override
					protected void handleCompleted() {
						wait.waitFinished(getStatus());
					}
				}));
		wait.waitUntilDone(TestsPlugin.massageTimeout(1000));
		Assert.assertTrue(wait.getMessage(), wait.isOK());
		wait.waitReset();

		// Wait one second and attempt to interrupt the target.
		// As of gdb 7.8, interrupting execution after a thread exit does not
		// work well. This test works around it by interrupting before threads
		// exit. Once the bug in gdb is fixed, we should add a test that
		// interrupts after the threads exit.
		// Ref: https://sourceware.org/bugzilla/show_bug.cgi?id=17627
		Thread.sleep(1000);
		fRunCtrl.getExecutor()
				.submit(() -> fRunCtrl.suspend(fThreadExecDmc, new RequestMonitor(fRunCtrl.getExecutor(), null) {
					@Override
					protected void handleCompleted() {
						wait.waitFinished(getStatus());
					}
				}));

		wait.waitUntilDone(TestsPlugin.massageTimeout(1000));
		Assert.assertTrue(wait.getMessage(), wait.isOK());
		wait.waitReset();

		// Wait up to 2 seconds for the target to suspend. Should happen immediately.
		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));

		// Double check that the target is in the suspended state
		final IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		fRunCtrl.getExecutor().submit(() -> {
			wait.setReturnInfo(fRunCtrl.isSuspended(containerDmc));
			wait.waitFinished();
		});
		wait.waitUntilDone(TestsPlugin.massageTimeout(2000));
		Assert.assertTrue("Target is running. It should have been suspended", (Boolean) wait.getReturnInfo());
		wait.waitReset();
	}
}
