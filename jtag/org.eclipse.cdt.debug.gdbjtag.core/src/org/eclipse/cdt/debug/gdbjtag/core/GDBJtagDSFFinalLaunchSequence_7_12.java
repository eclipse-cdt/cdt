/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Subclass for GDB >= 7.12.
 *
 * @since 9.1
 */
public class GDBJtagDSFFinalLaunchSequence_7_12 extends GDBJtagDSFFinalLaunchSequence_7_7 {
	public GDBJtagDSFFinalLaunchSequence_7_12(DsfSession session, Map<String, Object> attributes,
			RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the steps from the base class
			List<String> orderList = new ArrayList<>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Add the new step after we source the gdbinit file to make sure the user
			// cannot change this behavior
			orderList.add(orderList.indexOf("stepSourceGDBInitFile") + 1, //$NON-NLS-1$
					"stepSetTargetAsync"); //$NON-NLS-1$

			return orderList.toArray(new String[orderList.size()]);
		}

		return super.getExecutionOrder(group);
	}

	@Execute
	public void stepSetTargetAsync(RequestMonitor rm) {
		// Processing this request after sourcing the gdbinit file to make sure the user
		// cannot change this behavior
		DsfServicesTracker tracker = new DsfServicesTracker(Activator.getBundleContext(), getSession().getId());
		IMICommandControl commandControl = tracker.getService(IMICommandControl.class);
		IGDBBackend gdbBackEnd = tracker.getService(IGDBBackend.class);
		tracker.dispose();

		if (commandControl != null && gdbBackEnd != null) {
			// Use target async when interfacing with the full GDB console (i.e. minimum GDB version 7.12)
			// otherwise explicitly set it to off.
			commandControl
					.queueCommand(
							commandControl.getCommandFactory().createMIGDBSetTargetAsync(commandControl.getContext(),
									gdbBackEnd.isFullGdbConsoleSupported()),
							new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
								@Override
								protected void handleError() {
									// Accept errors for older GDBs
									rm.done();
								}
							});
		} else {
			// Should not happen
			rm.done();
		}
	}
}
