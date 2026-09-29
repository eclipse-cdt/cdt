/*******************************************************************************
 * Copyright (c) 2026 Sergii Zolotarov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests that closing the standard input of a debugged inferior, as done by the
 * console EOF action or when input is redirected from a file, makes the
 * inferior read end of input.
 */
@RunWith(Parameterized.class)
public class InferiorStdinEofTest extends BaseParametrizedTestCase {
	private static final String EXEC_NAME = "StdinEofTestApp.exe";

	@Override
	public void doBeforeTest() throws Exception {
		assumeLocalSession();
		Assume.assumeFalse("Skipping Windows, EOT only ends input on a POSIX terminal", runningOnWindows());
		super.doBeforeTest();
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	/**
	 * Input closed while the inferior is suspended at main must be seen as EOF
	 * once it runs.
	 */
	@Test
	public void testCloseInputWhileSuspended() throws Throwable {
		ServiceEventWaitor<ICommandControlShutdownDMEvent> shutdownWaitor = new ServiceEventWaitor<>(
				getGDBLaunch().getSession(), ICommandControlShutdownDMEvent.class);

		closeInferiorInput();
		SyncUtil.resume();

		assertInferiorExitedAfterEof(shutdownWaitor);
	}

	/**
	 * Input closed while the inferior is running and blocked reading it must end
	 * the read with EOF.
	 */
	@Test
	public void testCloseInputWhileRunning() throws Throwable {
		ServiceEventWaitor<ICommandControlShutdownDMEvent> shutdownWaitor = new ServiceEventWaitor<>(
				getGDBLaunch().getSession(), ICommandControlShutdownDMEvent.class);

		SyncUtil.resume();
		// Give the inferior time to block in its read of standard input
		Thread.sleep(TestsPlugin.massageTimeout(500));
		closeInferiorInput();

		assertInferiorExitedAfterEof(shutdownWaitor);
	}

	private InferiorRuntimeProcess getInferiorProcess() {
		for (IProcess process : getGDBLaunch().getProcesses()) {
			if (process instanceof InferiorRuntimeProcess inferior) {
				return inferior;
			}
		}
		throw new AssertionError("No inferior process in launch");
	}

	private void closeInferiorInput() throws Exception {
		IStreamsProxy proxy = getInferiorProcess().getStreamsProxy();
		assertTrue("Streams proxy does not support closing input", proxy instanceof IStreamsProxy2);
		((IStreamsProxy2) proxy).closeInputStream();
	}

	private void assertInferiorExitedAfterEof(ServiceEventWaitor<ICommandControlShutdownDMEvent> shutdownWaitor)
			throws Exception {
		// Without EOF the inferior keeps waiting for input and the session never shuts down
		shutdownWaitor.waitForEvent(TestsPlugin.massageTimeout(5000));

		InferiorRuntimeProcess inferior = getInferiorProcess();
		for (int i = 0; i < 100 && !inferior.isTerminated(); i++) {
			synchronized (inferior) {
				inferior.wait(10);
			}
		}
		// The test program returns the number of bytes it read before EOF
		assertThat(inferior.getExitValue(), is(0));
	}
}
