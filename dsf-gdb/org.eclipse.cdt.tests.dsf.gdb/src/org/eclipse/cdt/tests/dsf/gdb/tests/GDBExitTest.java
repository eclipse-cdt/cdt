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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBExit;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
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
	private final AtomicBoolean fGdbExitDone = new AtomicBoolean();

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
				tracker.getService(IGDBControl.class).addCommandListener(new ICommandListener() {
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
						if (token.getCommand() instanceof MIGDBExit) {
							fGdbExitDone.set(true);
						}
					}
				});
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
	public void doAfterTest() throws Exception {
		try {
			super.doAfterTest();
		} finally {
			if (fAttachedProgram != null) {
				fAttachedProgram.destroyForcibly();
			}
		}
	}

	/**
	 * Terminating the session must stop GDB also while the program starts threads.
	 */
	@Test
	public void terminateWhileProgramStartsThreads() throws Throwable {
		SyncUtil.resumeAll();
		Thread.sleep(300); // The program now starts threads all the time

		terminateAndAssertGdbExits();
	}

	/**
	 * Terminating a non-stop session must stop GDB also while the program starts threads.
	 */
	@Test
	public void terminateNonStopWhileProgramStartsThreads() throws Throwable {
		SyncUtil.resumeAll();
		Thread.sleep(300);

		terminateAndAssertGdbExits();
	}

	/**
	 * Terminating an attached session must let the program run on.
	 */
	@Test
	public void terminateAttachedSessionWhileProgramStartsThreads() throws Throwable {
		SyncUtil.resumeAll();
		Thread.sleep(300);

		terminateAndAssertGdbExits();

		assertTrue("The program stopped with the session", fAttachedProgram.isAlive()); //$NON-NLS-1$
		Optional<Duration> cpuBefore = fAttachedProgram.toHandle().info().totalCpuDuration();
		Thread.sleep(500);
		Optional<Duration> cpuAfter = fAttachedProgram.toHandle().info().totalCpuDuration();
		if (cpuBefore.isPresent() && cpuAfter.isPresent()) {
			assertTrue("The program does not run after the session", cpuAfter.get().compareTo(cpuBefore.get()) > 0); //$NON-NLS-1$
		}
	}

	private void terminateAndAssertGdbExits() throws Exception {
		Process gdb = fBackend.getProcess();
		try {
			// Before the fix, GDB hung in -gdb-exit and this failed with "Terminate failed"
			getGDBLaunch().terminate();
			assertLaunchTerminates();
			assertTrue("GDB did not exit", gdb.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS)); //$NON-NLS-1$
			assertTrue("GDB was destroyed before -gdb-exit", fGdbExitDone.get()); //$NON-NLS-1$
		} finally {
			if (runningOnWindows()) {
				// The Spawner's pid() is not the native process ID on Windows
				gdb.destroy();
			} else {
				// A hung GDB ignores SIGTERM. Also stop the program that GDB started.
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
