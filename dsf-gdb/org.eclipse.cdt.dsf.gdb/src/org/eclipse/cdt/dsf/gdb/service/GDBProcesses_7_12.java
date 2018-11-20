/*******************************************************************************
 * Copyright (c) 2016, 2017 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 5.2
 */
public class GDBProcesses_7_12 extends GDBProcesses_7_11 {

	public GDBProcesses_7_12(DsfSession session) {
		super(session);
	}

	@Override
	protected Sequence getStartOrRestartProcessSequence(DsfExecutor executor, IContainerDMContext containerDmc,
			Map<String, Object> attributes, boolean restart, DataRequestMonitor<IContainerDMContext> rm) {
		return new StartOrRestartProcessSequence_7_12(executor, containerDmc, attributes, restart, rm);
	}

	@Override
	public void terminate(IThreadDMContext thread, RequestMonitor rm) {
		IGDBBackend backend = getServicesTracker().getService(IGDBBackend.class);
		if (!backend.isFullGdbConsoleSupported()) {
			super.terminate(thread, rm);
			return;
		}

		// If we are running the full GDB console, there is a bug with GDB 7.12
		// where after we terminate the process, the GDB prompt does not come
		// back in the console.  As a workaround, we first interrupt the process
		// to get the prompt back, and only then kill the process.
		// https://sourceware.org/bugzilla/show_bug.cgi?id=20766
		if (thread instanceof IMIProcessDMContext) {
			getDebuggingContext(thread, new ImmediateDataRequestMonitor<IDMContext>(rm) {
				@Override
				protected void handleSuccess() {
					if (getData() instanceof IMIContainerDMContext) {
						IMIContainerDMContext containerDmc = (IMIContainerDMContext) getData();
						IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
						if (runControl != null && !runControl.isSuspended(containerDmc)) {
							runControl.suspend(containerDmc, new ImmediateRequestMonitor(rm) {
								@Override
								protected void handleCompleted() {
									GDBProcesses_7_12.super.terminate(thread, rm);
								}
							});
						} else {
							GDBProcesses_7_12.super.terminate(thread, rm);
						}
					} else {
						rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
								"Invalid process context.", null)); //$NON-NLS-1$
					}
				}
			});
		} else {
			super.terminate(thread, rm);
		}
	}

	@Override
	public void detachDebuggerFromProcess(IDMContext dmc, RequestMonitor rm) {
		if (DMContexts.getAncestorOfType(dmc, MIExitedProcessDMC.class) != null) {
			super.detachDebuggerFromProcess(dmc, rm);
			return;
		}

		IGDBBackend backend = getServicesTracker().getService(IGDBBackend.class);
		if (!backend.isFullGdbConsoleSupported()) {
			super.detachDebuggerFromProcess(dmc, rm);
			return;
		}

		// If we are running the full GDB console, there is a bug with GDB 7.12
		// where after we detach the process, the GDB prompt does not come
		// back in the console.  As a workaround, we first interrupt the process
		// to get the prompt back, and only then detach the process.
		// https://sourceware.org/bugzilla/show_bug.cgi?id=20766
		if (!doCanDetachDebuggerFromProcess()) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Detach not supported.", null)); //$NON-NLS-1$
			return;
		}

		IMIContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
		IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
		if (containerDmc != null && runControl != null && !runControl.isSuspended(containerDmc)) {
			runControl.suspend(containerDmc, new ImmediateRequestMonitor(rm) {
				@Override
				protected void handleCompleted() {
					GDBProcesses_7_12.super.detachDebuggerFromProcess(dmc, rm);
				}
			});
		} else {
			super.detachDebuggerFromProcess(dmc, rm);
		}
	}

	/**
	 * @since 5.4
	 */
	@Override
	protected boolean targetAttachRequiresTrailingNewline() {
		return true;
	}
}
