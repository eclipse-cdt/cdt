/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Version of BreakpointsManager for GDB version starting with 7.2.
 * @since 4.7
 */
public class GDBBreakpointsManager_7_2 extends GDBBreakpointsManager_7_0 {
	public GDBBreakpointsManager_7_2(DsfSession session, String debugModelId) {
		super(session, debugModelId);
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(final RequestMonitor rm) {
		register(new String[] { GDBBreakpointsManager_7_2.class.getName() }, new Hashtable<String, String>());

		rm.done();
	}

	@Override
	protected void updateContextOnStartEvent(IStartedDMEvent e) {
		// No longer need to update the context as the logic
		// of the base class was to work around an issue with
		// GDB 7.0 and 7.1
	}
}
