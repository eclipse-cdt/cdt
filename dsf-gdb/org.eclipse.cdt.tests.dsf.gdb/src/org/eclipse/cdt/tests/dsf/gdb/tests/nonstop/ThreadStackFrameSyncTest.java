/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.nonstop;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IMultiRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.service.IGDBFocusSynchronizer;
import org.eclipse.cdt.dsf.gdb.internal.service.IGDBFocusSynchronizer.IGDBFocusChangedEvent;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIThreadInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConsoleStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MITuple;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ThreadStackFrameSyncTest extends BaseParametrizedTestCase {

	final static private int DEFAULT_TIMEOUT = 1000;

	private DsfServicesTracker fServicesTracker;
	private IMultiRunControl fMultiRunControl;
	private IGDBControl fCommandControl;
	private IGDBFocusSynchronizer fGdbSync;
	private DsfSession fSession;
	private List<IDMEvent<? extends IDMContext>> fEventsReceived = new ArrayList<>();

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

	@BeforeClass
	public static void beforeClass() {
		Assume.assumeTrue(supportsNonStop());
	}

	@Override
	public void doBeforeTest() throws Exception {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_12);
		super.doBeforeTest();

		resolveLineTagLocations(SOURCE_NAME, LINE_TAGS);

		fSession = getGDBLaunch().getSession();
		Assert.assertNotNull(fSession);

		Runnable runnable = () -> {

			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
			Assert.assertTrue(fServicesTracker != null);

			fCommandControl = fServicesTracker.getService(IGDBControl.class);
			Assert.assertTrue(fCommandControl != null);

			fMultiRunControl = fServicesTracker.getService(IMultiRunControl.class);
			Assert.assertTrue(fMultiRunControl != null);

			fGdbSync = fServicesTracker.getService(IGDBFocusSynchronizer.class);
			Assert.assertTrue(fGdbSync != null);

			IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
			Assert.assertTrue(procService != null);

			// Register to receive DSF events
			fSession.addServiceEventListener(ThreadStackFrameSyncTest.this, null);
		};
		fSession.getExecutor().submit(runnable).get();
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);

		// Multi run control only makes sense for non-stop mode
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, true);
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();

		if (fSession != null) {
			fSession.getExecutor().submit(() -> fSession.removeServiceEventListener(ThreadStackFrameSyncTest.this))
					.get();
		}
		fEventsReceived.clear();

		if (fServicesTracker != null)
			fServicesTracker.dispose();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// Start of tests
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	* This test verifies that changing the active thread, in GDB, in CLI,
	* triggers a GDB notification that a new thread has been selected.
	*/
	@Test
	public void testChangingCurrentThreadCLINotification() throws Throwable {
		ServiceEventWaitor<MIStoppedEvent> eventWaitor = new ServiceEventWaitor<>(fMultiRunControl.getSession(),
				MIStoppedEvent.class);

		// add a breakpoint in main
		SyncUtil.addBreakpoint(SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_ALL_THREADS_STARTED"), false);
		// add a breakpoint in thread code
		SyncUtil.addBreakpoint("36", false);
		// Run program
		SyncUtil.resumeAll();

		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000)); // Wait for first thread to stop
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000)); // Wait for second thread to stop
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));

		// *** at this point all 5 threads should be stopped

		// Try some thread switching - SwitchThreadAndCaptureThreadSwitchedEvent will
		// capture the "=thread-selected" event and return the newly selected thread
		// for us to compare to what we ordered
		for (int i = 0; i < 2; i++) {
			assertEquals("2", switchThreadAndCaptureThreadSwitchedEvent("2"));
			assertEquals("3", switchThreadAndCaptureThreadSwitchedEvent("3"));
			assertEquals("4", switchThreadAndCaptureThreadSwitchedEvent("4"));
			assertEquals("5", switchThreadAndCaptureThreadSwitchedEvent("5"));
			assertEquals("1", switchThreadAndCaptureThreadSwitchedEvent("1"));
		}
	}

	/**
	 * This test verifies that changing the active frame, in GDB, in CLI,
	 * triggers a GDB notification that a new frame has been selected.
	 */
	@Test
	@Ignore
	public void testChangingCurrentFrameCLINotification() throws Throwable {
		ServiceEventWaitor<MIStoppedEvent> eventWaitor = new ServiceEventWaitor<>(fMultiRunControl.getSession(),
				MIStoppedEvent.class);

		// add a breakpoint in main
		SyncUtil.addBreakpoint(SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_ALL_THREADS_STARTED"), false);
		// add a breakpoint in thread code
		SyncUtil.addBreakpoint("36", false);
		// Run program
		SyncUtil.resumeAll();

		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000)); // Wait for first thread to stop
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000)); // Wait for second thread to stop
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));

		// *** at this point all 5 threads should be stopped

		// switch to a thread that has some stack frames
		assertEquals("2", switchThreadAndCaptureThreadSwitchedEvent("2"));

		// Try some stack frame switching - SwitchFrameAndCaptureThreadSwitchedEvent will
		// capture the "=thread-selected" event and return the newly selected stack frame,
		// for us to compare to what we ordered
		for (int i = 0; i < 5; i++) {
			assertEquals("1", switchFrameAndCaptureStackFrameSwitchedEvent("1"));
			assertEquals("0", switchFrameAndCaptureStackFrameSwitchedEvent("0"));
		}
	}

	/**
	 * This test verifies that the GDB Synchronizer service is able to set
	 * the current GDB thread
	 */
	@Test
	public void testGdbSyncServiceCanSwitchGDBThread() throws Throwable {
		ServiceEventWaitor<MIStoppedEvent> eventWaitor = new ServiceEventWaitor<>(fMultiRunControl.getSession(),
				MIStoppedEvent.class);

		// add a breakpoint in main
		SyncUtil.addBreakpoint(SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_ALL_THREADS_STARTED"), false);
		// add a breakpoint in thread code
		SyncUtil.addBreakpoint("36", false);
		// Run program
		SyncUtil.resumeAll();

		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000)); // Wait for first thread to stop
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000)); // Wait for second thread to stop
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));

		// *** at this point all 5 threads should be stopped

		// have the sync service set GDB current tid to thread 5
		fGdbSync.setFocus(new IDMContext[] { getContextForThreadId(5) }, new ImmediateRequestMonitor());
		assertEquals("5", getCurrentThread());

		fGdbSync.setFocus(new IDMContext[] { getContextForThreadId(4) }, new ImmediateRequestMonitor());
		assertEquals("4", getCurrentThread());

		fGdbSync.setFocus(new IDMContext[] { getContextForThreadId(3) }, new ImmediateRequestMonitor());
		assertEquals("3", getCurrentThread());

		fGdbSync.setFocus(new IDMContext[] { getContextForThreadId(2) }, new ImmediateRequestMonitor());
		assertEquals("2", getCurrentThread());

		fGdbSync.setFocus(new IDMContext[] { getContextForThreadId(1) }, new ImmediateRequestMonitor());
		assertEquals("1", getCurrentThread());
	}

	/**
	 * This test verifies that the GDB Synchronizer service is able to set
	 * the current GDB stack frame
	 */
	@Test
	@Ignore
	public void testGdbSyncServiceCanSwitchGDBStackFrame() throws Throwable {
		ServiceEventWaitor<MIStoppedEvent> eventWaitor = new ServiceEventWaitor<>(fMultiRunControl.getSession(),
				MIStoppedEvent.class);

		// add a breakpoint in main
		SyncUtil.addBreakpoint(SOURCE_NAME + ":" + getLineForTag("LINE_MAIN_ALL_THREADS_STARTED"), false);
		// add a breakpoint in thread code
		SyncUtil.addBreakpoint("36", false);
		// Run program
		SyncUtil.resumeAll();

		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000)); // Wait for first thread to stop
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000)); // Wait for second thread to stop
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));
		eventWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));

		// *** at this point all 5 threads should be stopped

		final IFrameDMContext frame1 = SyncUtil.getStackFrame(1, 1);
		final IFrameDMContext frame0 = SyncUtil.getStackFrame(1, 0);

		// do a few of times
		for (int i = 0; i < 50; i++) {
			// have the sync service switch stack frame to 1
			fSession.getExecutor()
					.execute(() -> fGdbSync.setFocus(new IDMContext[] { frame1 }, new ImmediateRequestMonitor()));
			Thread.sleep(100);
			assertEquals("1", getCurrentStackFrameLevel());

			// have the sync service switch stack frame to 0
			fSession.getExecutor()
					.execute(() -> fGdbSync.setFocus(new IDMContext[] { frame0 }, new ImmediateRequestMonitor()));
			Thread.sleep(100);
			assertEquals("0", getCurrentStackFrameLevel());
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// End of tests
	//////////////////////////////////////////////////////////////////////////////////////

	// SyncUtil.getExecutionContext() takes the index of the
	// array of all threads, so it will return a thread off by one.
	// We compensate for this in this method
	private IMIExecutionDMContext getContextForThreadId(int tid)
			throws InterruptedException, ExecutionException, TimeoutException {
		return SyncUtil.getExecutionContext(tid - 1);
	}

	/**
	 * This is a wrapper around selectGdbThread(), that waits and captures the
	 * expected "=thread-selected" event, and returns the thread id from it.
	 * @throws Throwable
	 */
	private String switchThreadAndCaptureThreadSwitchedEvent(String tid) throws Throwable {
		Thread.sleep(100);
		fEventsReceived.clear();
		selectGdbThread(tid);

		IDMContext ctx = waitForEvent(IGDBFocusChangedEvent.class).getDMContext();
		if (ctx instanceof IMIExecutionDMContext) {
			IMIExecutionDMContext execDmc = (IMIExecutionDMContext) ctx;
			return execDmc.getThreadId();
		} else if (ctx instanceof IFrameDMContext) {
			IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(ctx, IMIExecutionDMContext.class);
			return execDmc.getThreadId();
		}
		return "unknown";
	}

	/**
	 * Waits and captures the expected "=thread-selected" event, and returns the frame id from it.
	 * @throws Throwable
	 */
	private String switchFrameAndCaptureStackFrameSwitchedEvent(String frameLevel) throws Throwable {
		IFrameDMContext newFrame = null;

		Thread.sleep(100);
		fEventsReceived.clear();
		selectGdbStackFrame(frameLevel);

		Object[] elems = fGdbSync.getFocus();
		for (Object elem : elems) {
			if (elem instanceof IFrameDMContext) {
				newFrame = (IFrameDMContext) elem;
				break;
			}
		}

		return newFrame != null ? Integer.toString(newFrame.getLevel()) : null;
	}

	/**
	 * Changes the current thread, using the CLI command "thread <tid>"
	 * @param tid: the thread id of the thread to switch-to. If empty,
	 * the command will simply report the current thread.
	 * @return the tid of the (possibly newly) currently selected gdb thread
	 * @throws Exception
	 */
	private String sendCLIThread(String tid) throws Exception {
		IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		Query<CLIThreadInfo> query = new Query<CLIThreadInfo>() {
			@Override
			protected void execute(DataRequestMonitor<CLIThreadInfo> rm) {
				fCommandControl.queueCommand(new CLICommand<CLIThreadInfo>(containerDmc, "thread " + tid) {
					@Override
					public CLIThreadInfo getResult(MIOutput output) {
						return new CLIThreadInfo(output);
					}
				}, rm);
			}
		};

		fCommandControl.getExecutor().execute(query);
		CLIThreadInfo info = query.get();

		return info.getCurrentThread();
	}

	private String getCurrentThread() throws Exception {
		return sendCLIThread("");
	}

	/**
	 * Changes the current stack frame, using the CLI command "frame <level>". Then parses
	 * the output to extract the current frame.
	 * @param level the frame level wanted. If empty, the command will report the current level
	 * @return newly set level.
	 * @throws Exception
	 */
	private String sendCLIFrame(String level) throws Exception {
		IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		Query<MIInfo> query = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fCommandControl.queueCommand(new CLICommand<MIInfo>(containerDmc, "frame " + level) {
					@Override
					public CLIThreadInfo getResult(MIOutput output) {
						return new CLIThreadInfo(output);
					}
				}, rm);
			}
		};

		fCommandControl.getExecutor().execute(query);

		String frameLevel = null;
		for (MIOOBRecord oobr : query.get().getMIOutput().getMIOOBRecords()) {
			// if frame changed, we'll get this printout:
			if (oobr instanceof MINotifyAsyncOutput) {
				// example of output:
				// =thread-selected,id="2",frame={level="1",addr="0x00007ffff7bc4184",func="start_thread",args=[],from="/lib/x86_64-linux-gnu/libpthread.so.0"}
				MINotifyAsyncOutput out = (MINotifyAsyncOutput) oobr;
				String miEvent = out.getAsyncClass();
				if ("thread-selected".equals(miEvent)) { //$NON-NLS-1$
					// parse =thread-selected to extract current stack frame
					MIResult[] results = out.getMIResults();
					for (int i = 0; i < results.length; i++) {
						String var = results[i].getVariable();
						MIValue val = results[i].getMIValue();
						if (var.equals("frame") && val instanceof MITuple) { //$NON-NLS-1$
							// dig deeper to get the frame level
							MIResult[] res = ((MITuple) val).getMIResults();
							for (int j = 0; j < res.length; j++) {
								var = res[j].getVariable();
								val = res[j].getMIValue();
								if (var.equals("level")) { //$NON-NLS-1$
									if (val instanceof MIConst) {
										frameLevel = ((MIConst) val).getString();
									}
								}
							}
						}
					}
				}
			}
			// if frame command was not given a parameter or the parameter is already
			// the current frame, we'll get this version of the printout:
			else if (oobr instanceof MIConsoleStreamOutput) {
				// example of output (here frame = 0):
				// ~"#0 main (argc=1 ...
				String printout = ((MIConsoleStreamOutput) oobr).getCString();
				int index1 = printout.indexOf('#');
				int index2 = printout.indexOf(' ');
				if (index1 != -1 && index2 != -1) {
					frameLevel = printout.substring(index1 + 1, index2);
					break;
				}
			}
		}
		return frameLevel;
	}

	private String getCurrentStackFrameLevel() throws Throwable {
		return sendCLIFrame("");
	}

	@DsfServiceEventHandler
	public void eventDispatched(IDMEvent<? extends IDMContext> e) {
		synchronized (this) {
			fEventsReceived.add(e);
			notifyAll();
		}
	}

	private void selectGdbThread(String tid) throws Throwable {
		queueConsoleCommand(String.format("thread %s", tid));
	}

	private void selectGdbStackFrame(String frameLevel) throws Throwable {
		queueConsoleCommand(String.format("frame %s", frameLevel));
	}

	private void queueConsoleCommand(String command) throws Throwable {
		queueConsoleCommand(command, TestsPlugin.massageTimeout(DEFAULT_TIMEOUT), TimeUnit.MILLISECONDS);
	}

	private void queueConsoleCommand(final String command, int timeout, TimeUnit unit) throws Throwable {
		Query<MIInfo> query = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fCommandControl.queueCommand(fCommandControl.getCommandFactory()
						.createMIInterpreterExecConsole(fCommandControl.getContext(), command), rm);
			}
		};
		fSession.getExecutor().execute(query);
		query.get(timeout, unit);
	}

	private <V extends IDMEvent<? extends IDMContext>> V waitForEvent(Class<V> eventType) throws Exception {
		return waitForEvent(eventType, TestsPlugin.massageTimeout(DEFAULT_TIMEOUT));
	}

	@SuppressWarnings("unchecked")
	private <V extends IDMEvent<? extends IDMContext>> V waitForEvent(Class<V> eventType, int timeout)
			throws Exception {
		IDMEvent<?> event = getEvent(eventType);
		if (event == null) {
			synchronized (this) {
				try {
					wait(timeout);
				} catch (InterruptedException ex) {
				}
			}
			event = getEvent(eventType);
			if (event == null) {
				throw new Exception(String.format("Timed out waiting for '%s' to occur.", eventType.getName()));
			}
		}
		return (V) event;
	}

	@SuppressWarnings("unchecked")
	private synchronized <V extends IDMEvent<? extends IDMContext>> V getEvent(Class<V> eventType) {
		for (IDMEvent<?> e : fEventsReceived) {
			if (eventType.isAssignableFrom(e.getClass())) {
				fEventsReceived.remove(e);
				return (V) e;
			}
		}
		return null;
	}

}
