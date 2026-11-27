/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBExit;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.Assume;
import org.junit.Test;

/**
 * Terminates sessions while the program starts threads, see
 * <a href="https://github.com/eclipse-cdt/cdt/issues/1065">#1065</a>.
 */
public class GDBExitTest extends BaseParametrizedTestCase {
	private static final String EXEC_NAME = "ThreadSpawner.exe";
	private static final String ATTACH = "Attached";
	private static final String NON_STOP = "NonStop";
	private static final long TIMEOUT_MS = 10000;

	private Process fAttachedProgram;
	private IGDBBackend fBackend;
	private IGDBControl fControl;
	private IProcesses fProcesses;
	private final AtomicBoolean fGdbExitAnswered = new AtomicBoolean();
	private final ICommandListener fExitListener = new ICommandListener() {
		@Override
		public void commandQueued(ICommandToken token) {
		}

		@Override
		public void commandSent(ICommandToken token) {
		}

		@Override
		public void commandRemoved(ICommandToken token) {
		}

		@Override
		public void commandDone(ICommandToken token, ICommandResult result) {
			// An ^error reply also comes here, so check for ^exit
			if (token.getCommand() instanceof MIGDBExit && result instanceof MIInfo info && !info.isError()) {
				fGdbExitAnswered.set(true);
			}
		}
	};

	@Override
	public void doBeforeTest() throws Exception {
		if (testName.getMethodName().contains(ATTACH)) {
			assumeLocalSession();
			assumeAttachPermitted();
		}
		if (testName.getMethodName().contains(NON_STOP)) {
			Assume.assumeTrue("Non-stop mode is not supported", supportsNonStop()); //$NON-NLS-1$
		}
		super.doBeforeTest();

		DsfSession session = getGDBLaunch().getSession();
		fBackend = session.getExecutor().submit(() -> {
			DsfServicesTracker tracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), session.getId());
			try {
				// Record whether GDB answered -gdb-exit, or the force-quit timer destroyed it before
				fControl = tracker.getService(IGDBControl.class);
				fControl.addCommandListener(fExitListener);
				fProcesses = tracker.getService(IProcesses.class);
				return tracker.getService(IGDBBackend.class);
			} finally {
				tracker.dispose();
			}
		}).get();

		// GDB prints each new thread to its CLI console. Nobody reads that console in
		// headless tests, so GDB blocks when the buffer is full. Read and drop the output.
		if (fBackend.isFullGdbConsoleSupported()) {
			InputStream console = fBackend.getProcess().getInputStream();
			Thread drain = new Thread(() -> {
				byte[] buffer = new byte[4096];
				try {
					while (console.read(buffer) >= 0) {
						// Drop the console output
					}
				} catch (IOException e) {
					// GDB has exited
				}
			}, "GDB console drain"); //$NON-NLS-1$
			drain.setDaemon(true);
			drain.start();
		}
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
		if (testName.getMethodName().contains(NON_STOP)) {
			setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, true);
		}
		if (testName.getMethodName().contains(ATTACH)) {
			try {
				fAttachedProgram = new ProcessBuilder(new File(EXEC_PATH + EXEC_NAME).getAbsolutePath()).start();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
			setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, (int) fAttachedProgram.pid());
			setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
		}
	}

	@Override
	protected boolean isTargetExpectedToStopAfterLaunch() {
		// In non-stop mode, an attached program keeps running
		String name = testName.getMethodName();
		if (name.contains(ATTACH) && name.contains(NON_STOP)) {
			return false;
		}
		return super.isTargetExpectedToStopAfterLaunch();
	}

	@Override
	public void doAfterTest() throws Exception {
		removeExitListener();
		try {
			super.doAfterTest();
		} finally {
			if (fAttachedProgram != null) {
				fAttachedProgram.destroyForcibly();
			}
		}
	}

	private void removeExitListener() throws Exception {
		if (fControl == null || getGDBLaunch() == null) {
			return; // The test was skipped before the launch
		}
		DsfSession session = getGDBLaunch().getSession();
		if (!DsfSession.isSessionActive(session.getId())) {
			return; // The listener goes away with the session
		}
		try {
			session.getExecutor().submit(() -> fControl.removeCommandListener(fExitListener)).get();
		} catch (RejectedExecutionException e) {
			// The session has ended
		}
	}

	/**
	 * Terminating the session must stop GDB also while the program starts threads.
	 */
	@Test
	public void terminateWhileProgramStartsThreads() throws Throwable {
		resumeUntilThreadStarts();

		terminateAndAssertGdbExits();
	}

	/**
	 * Terminating a non-stop session must stop GDB also while the program starts threads.
	 */
	@Test
	public void terminateNonStopWhileProgramStartsThreads() throws Throwable {
		resumeUntilThreadStarts();

		terminateAndAssertGdbExits();
	}

	/**
	 * Terminating an attached session must let the program run on.
	 */
	@Test
	public void terminateAttachedSessionWhileProgramStartsThreads() throws Throwable {
		resumeUntilThreadStarts();

		terminateAndAssertGdbExits();

		assertTrue("The program stopped with the session", fAttachedProgram.isAlive()); //$NON-NLS-1$
		Optional<Duration> cpuBefore = fAttachedProgram.toHandle().info().totalCpuDuration();
		Assume.assumeTrue("The system does not give the CPU time of a process", cpuBefore.isPresent()); //$NON-NLS-1$
		Thread.sleep(500);
		Optional<Duration> cpuAfter = fAttachedProgram.toHandle().info().totalCpuDuration();
		assertTrue("The program does not run after the session", //$NON-NLS-1$
				cpuAfter.isPresent() && cpuAfter.get().compareTo(cpuBefore.get()) > 0);
	}

	/**
	 * Killing the process, as the Terminate button does, must work also while the program
	 * starts threads.
	 */
	@Test
	public void terminateProcessWhileProgramStartsThreads() throws Throwable {
		resumeUntilThreadStarts();

		endProcessAndAssertGdbExits(false);
	}

	/**
	 * Killing the process in non-stop mode must work also while the program starts threads.
	 */
	@Test
	public void terminateProcessNonStopWhileProgramStartsThreads() throws Throwable {
		resumeUntilThreadStarts();

		endProcessAndAssertGdbExits(false);
	}

	/**
	 * Detaching from an attached process in non-stop mode must work also while the program
	 * starts threads, and the program must run on.
	 */
	@Test
	public void detachAttachedNonStopWhileProgramStartsThreads() throws Throwable {
		resumeUntilThreadStarts();

		endProcessAndAssertGdbExits(true);

		assertTrue("The program stopped with the session", fAttachedProgram.isAlive()); //$NON-NLS-1$
	}

	/**
	 * Resumes the program and, in a local session, waits until it starts a thread, so that
	 * the program starts threads all the time when the test terminates the session.
	 */
	private void resumeUntilThreadStarts() throws Throwable {
		if (isRemoteSession()) {
			// GDB gets no thread events from gdbserver while the program runs, only when it
			// stops, so there is no event to wait for
			SyncUtil.resumeAll();
			return;
		}
		ServiceEventWaitor<IStartedDMEvent> threadStarted = new ServiceEventWaitor<>(getGDBLaunch().getSession(),
				IStartedDMEvent.class);
		SyncUtil.resumeAll();
		threadStarted.waitForEvent((int) TIMEOUT_MS);
	}

	private void terminateAndAssertGdbExits() throws Exception {
		// Before the fix, GDB hung in -gdb-exit and this failed with "Terminate failed"
		endAndAssertGdbExits(() -> getGDBLaunch().terminate());
	}

	/**
	 * Kills the process or detaches from it, the way the Terminate and Disconnect buttons do.
	 * Then GDB exits by itself, because the session has no process left.
	 */
	private void endProcessAndAssertGdbExits(boolean detach) throws Exception {
		IContainerDMContext container = SyncUtil.getContainerContext();
		Query<Object> query = new Query<>() {
			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				if (detach) {
					fProcesses.detachDebuggerFromProcess(container, rm);
				} else {
					fProcesses.terminate(DMContexts.getAncestorOfType(container, IProcessDMContext.class), rm);
				}
			}
		};
		// Before the fix, GDB could hang in kill or detach
		endAndAssertGdbExits(() -> {
			getGDBLaunch().getSession().getExecutor().execute(query);
			query.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
		});
	}

	private interface SessionEnd {
		void run() throws Exception;
	}

	private void endAndAssertGdbExits(SessionEnd end) throws Exception {
		Process gdb = fBackend.getProcess();
		try {
			end.run();
			assertLaunchTerminates();
			// Before the fix, GDB replied ^exit and then hung, so this is the check that finds the hang
			assertTrue("GDB did not exit", gdb.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS)); //$NON-NLS-1$
			// The force-quit timer must not be what stopped GDB
			assertTrue("GDB did not reply ^exit to -gdb-exit before it was destroyed", fGdbExitAnswered.get()); //$NON-NLS-1$
		} finally {
			// If the test fails, GDB can still hang. Kill it here, else the hung GDB and the
			// program it started stay alive after the test run, for example on CI machines.
			if (runningOnWindows()) {
				// The Spawner's pid() is not the native process ID on Windows
				gdb.destroy();
			} else if (gdb.isAlive()) {
				// A hung GDB ignores SIGTERM, so kill it. Also kill the program that GDB started
				// (a descendant). Only while GDB is alive: else the OS can give its pid to
				// another process.
				ProcessHandle.of(gdb.pid()).ifPresent(handle -> {
					handle.descendants().forEach(ProcessHandle::destroyForcibly);
					handle.destroyForcibly();
				});
			}
		}
	}

	/**
	 * Skips the test if the system does not let GDB attach to a process that it did not start.
	 */
	private static void assumeAttachPermitted() throws IOException {
		Path ptraceScope = Path.of("/proc/sys/kernel/yama/ptrace_scope"); //$NON-NLS-1$
		if (Files.isReadable(ptraceScope)) {
			Assume.assumeTrue("Attach needs ptrace_scope 0", Files.readString(ptraceScope).trim().equals("0")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
