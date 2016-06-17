/*******************************************************************************
 * Copyright (c) 2016 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.sync;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBSynchronizer;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;

public class GdbDebugContextSyncManager implements IDebugSelectionSyncManager, IDebugContextListener {

	@Override
	public void startup() {
		DebugUITools.getDebugContextManager().addDebugContextListener(this);
	}

	@Override
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

			if (dmc instanceof IMIExecutionDMContext || dmc instanceof IFrameDMContext) {
				// A thread or stack frame was selected. In either case, have GDB switch to the new
				// corresponding thread, if required.

				// Get the execution context and then get the thread from the execution model.
				final IMIExecutionDMContext executionDMC = DMContexts.getAncestorOfType(dmc,
						IMIExecutionDMContext.class);
				if (executionDMC == null) {
					return;
				}

				// Resolve the debug session
				String eventSessionId = executionDMC.getSessionId();
				if (!(DsfSession.isSessionActive(eventSessionId))) {
					return;
				}

				DsfSession session = DsfSession.getSession(eventSessionId);

				// order GDB to switch thread
				session.getExecutor().execute(new Runnable() {
					@Override
					public void run() {
						DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(),
								eventSessionId);
						IGDBSynchronizer gdbSync = tracker.getService(IGDBSynchronizer.class);
						if (gdbSync != null) {
							gdbSync.setFocus(new Object[] {dmc});
						}

						tracker.dispose();
					}
				});
			}
		}
	}
}
