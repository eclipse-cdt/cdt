/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSteppingModeTarget;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * SteppingModeTarget that allows to disable the button when dealing
 * with a post-mortem debugging session.
 *
 * @since 2.0
 */
public class GdbSteppingModeTarget extends DsfSteppingModeTarget {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public GdbSteppingModeTarget(DsfSession session) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
	}

	public void dispose() {
		fTracker.dispose();
	}

	@Override
	public boolean supportsInstructionStepping() {
		Query<Boolean> supportInstructionStepping = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				IGDBBackend backend = fTracker.getService(IGDBBackend.class);
				if (backend != null) {
					// PostMortem sessions do not support instruction stepping
					rm.setData(backend.getSessionType() != SessionType.CORE);
				} else {
					rm.setData(false);
				}

				rm.done();
			}
		};

		fExecutor.execute(supportInstructionStepping);
		try {
			return supportInstructionStepping.get();
		} catch (InterruptedException e1) {
		} catch (ExecutionException e1) {
		}
		return false;
	}
}
