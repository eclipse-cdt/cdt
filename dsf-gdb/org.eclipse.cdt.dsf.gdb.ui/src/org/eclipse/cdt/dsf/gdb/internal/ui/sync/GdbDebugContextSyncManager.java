/*******************************************************************************
 * Copyright (c) 2016 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.sync;

import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.internal.service.IGDBFocusSynchronizer;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;

/**
 * This instance propagates the selection of debug context elements e.g. Thread to the back end GDB
 */
public class GdbDebugContextSyncManager implements IDebugContextListener {

	public void startup() {
		DebugUITools.getDebugContextManager().addDebugContextListener(this);
	}

	public void shutdown() {
		DebugUITools.getDebugContextManager().removeDebugContextListener(this);
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		// Make sure that it's a change of selection that caused the event
		if ((event.getFlags() != DebugContextEvent.ACTIVATED)) {
			return;
		}

		// Get selected element in the Debug View
		IAdaptable context = DebugUITools.getDebugContext();

		if (context != null) {
			final IDMContext dmc = context.getAdapter(IDMContext.class);

			if (dmc instanceof IMIContainerDMContext || dmc instanceof IMIExecutionDMContext
					|| dmc instanceof IFrameDMContext) {
				// A process, thread or stack frame was selected. In each case, have GDB switch to the new
				// corresponding thread, if required.

				// Resolve the debug session
				String eventSessionId = dmc.getSessionId();
				if (!(DsfSession.isSessionActive(eventSessionId))) {
					return;
				}

				DsfSession session = DsfSession.getSession(eventSessionId);

				// order GDB to switch thread
				session.getExecutor().execute(() -> {
					DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), eventSessionId);
					IGDBFocusSynchronizer gdbSync = tracker.getService(IGDBFocusSynchronizer.class);
					tracker.dispose();

					if (gdbSync != null) {
						gdbSync.setFocus(new IDMContext[] { dmc }, new ImmediateRequestMonitor() {
							@Override
							protected void handleFailure() {
								// do not set error - it's normal in some cases to fail to switch thread
								// for example in a remote session with the inferior running and in all-stop mode
							}
						});
					}
				});
			}
		}
	}
}
