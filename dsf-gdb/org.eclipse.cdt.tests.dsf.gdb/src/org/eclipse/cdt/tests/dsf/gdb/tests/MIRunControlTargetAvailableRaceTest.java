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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadInfoInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests {@link IMIRunControl#executeWithTargetAvailable} when the target
 * stops on its own (at a breakpoint) while the operation interrupts it.
 * See https://github.com/eclipse-cdt/cdt/issues/62
 */
@RunWith(Parameterized.class)
public class MIRunControlTargetAvailableRaceTest extends BaseParametrizedTestCase {
	private static final String EXEC_NAME = "TargetAvailRace.exe";
	private static final int ITERATIONS = 40;

	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;
	private IGDBControl fGDBCtrl;
	private IMIRunControl fRunCtrl;
	private IContainerDMContext fContainerDmc;

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();

		fSession = getGDBLaunch().getSession();
		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
			fGDBCtrl = fServicesTracker.getService(IGDBControl.class);
			fRunCtrl = fServicesTracker.getService(IMIRunControl.class);
		};
		fSession.getExecutor().submit(runnable).get();

		fContainerDmc = SyncUtil.getContainerContext();

		// With the full GDB console, GDB also prints every stop to its CLI
		// console. In these headless tests nobody reads that console, so after
		// a few dozen stops its buffer is full and GDB blocks. Read and drop
		// the console output.
		IGDBBackend backend = fSession.getExecutor().submit(() -> fServicesTracker.getService(IGDBBackend.class)).get();
		if (backend.isFullGdbConsoleSupported()) {
			InputStream console = backend.getProcess().getInputStream();
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
	public void doAfterTest() throws Exception {
		if (fServicesTracker != null)
			fServicesTracker.dispose();
		super.doAfterTest();
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	/**
	 * Resume the target and at once run an operation that needs the target
	 * to be available. The target hits a breakpoint at about the same time as
	 * the operation interrupts it. In all cases, when the operation completes,
	 * the target must be stopped at the breakpoint, and the run control
	 * service and GDB must agree on this. After that, a normal suspend must
	 * still be reported.
	 */
	@Test
	public void targetStopsByItselfWhileMadeAvailable() throws Throwable {
		String bpId = SyncUtil.addBreakpoint("RaceTarget", false);

		for (int i = 0; i < ITERATIONS; i++) {
			ServiceEventWaitor<IResumedDMEvent> resumedWaitor = new ServiceEventWaitor<>(fSession,
					IResumedDMEvent.class);
			ServiceEventWaitor<ISuspendedDMEvent> suspendedWaitor = new ServiceEventWaitor<>(fSession,
					ISuspendedDMEvent.class);

			SyncUtil.resume();
			resumedWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));

			runOperationWithTargetAvailable();

			try {
				suspendedWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));
			} catch (Exception e) {
				fail("Iteration " + i + ": no suspended event. Run control reports suspended: " + isSuspended()
						+ ", GDB reports thread state: " + getGdbThreadState());
			}
			// Give a target that was wrongly resumed the time to run.
			Thread.sleep(100);

			assertTrue("Iteration " + i + ": run control does not report the target as suspended", isSuspended());
			assertEquals("Iteration " + i + ": GDB does not report the thread as stopped", "stopped",
					getGdbThreadState());
		}

		// Remove the breakpoint and check that a normal suspend is still reported.
		deleteBreakpoint(bpId);
		SyncUtil.resume();
		ServiceEventWaitor<ISuspendedDMEvent> suspendedWaitor = new ServiceEventWaitor<>(fSession,
				ISuspendedDMEvent.class);
		Query<Object> suspend = new Query<>() {
			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				fRunCtrl.suspend(fContainerDmc, rm);
			}
		};
		fRunCtrl.getExecutor().execute(suspend);
		suspend.get(TestsPlugin.massageTimeout(2000), TimeUnit.MILLISECONDS);
		suspendedWaitor.waitForEvent(TestsPlugin.massageTimeout(2000));
		assertTrue("Run control does not report the target as suspended after a suspend", isSuspended());
	}

	private void runOperationWithTargetAvailable() throws Exception {
		final Step[] steps = new Step[] { new Step() {
			@Override
			public void execute(RequestMonitor rm) {
				IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc,
						IBreakpointsTargetDMContext.class);
				fGDBCtrl.queueCommand(fGDBCtrl.getCommandFactory().createMIBreakList(bpTargetDmc),
						new DataRequestMonitor<MIBreakListInfo>(fGDBCtrl.getExecutor(), rm));
			}
		} };

		Query<Object> query = new Query<>() {
			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps, rm);
			}
		};
		fRunCtrl.getExecutor().execute(query);
		query.get(TestsPlugin.massageTimeout(2000), TimeUnit.MILLISECONDS);
	}

	private boolean isSuspended() throws Exception {
		return fSession.getExecutor().submit(() -> fRunCtrl.isSuspended(fContainerDmc))
				.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
	}

	private String getGdbThreadState() throws Exception {
		Query<MIThreadInfoInfo> query = new Query<>() {
			@Override
			protected void execute(DataRequestMonitor<MIThreadInfoInfo> rm) {
				fGDBCtrl.queueCommand(fGDBCtrl.getCommandFactory().createMIThreadInfo(fGDBCtrl.getContext()), rm);
			}
		};
		fGDBCtrl.getExecutor().execute(query);
		return query.get(TestsPlugin.massageTimeout(1000), TimeUnit.MILLISECONDS).getThreadList()[0].getState();
	}

	private void deleteBreakpoint(String bpId) throws Exception {
		Query<MIInfo> query = new Query<>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc,
						IBreakpointsTargetDMContext.class);
				fGDBCtrl.queueCommand(
						fGDBCtrl.getCommandFactory().createMIBreakDelete(bpTargetDmc, new String[] { bpId }), rm);
			}
		};
		fGDBCtrl.getExecutor().execute(query);
		query.get(TestsPlugin.massageTimeout(1000), TimeUnit.MILLISECONDS);
	}
}
