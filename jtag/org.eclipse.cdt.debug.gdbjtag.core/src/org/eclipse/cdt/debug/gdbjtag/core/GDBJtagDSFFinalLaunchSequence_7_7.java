/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetDPrintfStyle;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Subclass for GDB >= 7.7.
 *
 * @since 8.4
 */
public class GDBJtagDSFFinalLaunchSequence_7_7 extends GDBJtagDSFFinalLaunchSequence_7_2 {

	public GDBJtagDSFFinalLaunchSequence_7_7(DsfSession session, Map<String, Object> attributes,
			RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Add the dprintf style steps before we source the gdbinit file
			orderList.add(orderList.indexOf("stepSourceGDBInitFile"), "stepSetDPrinfStyle"); //$NON-NLS-1$ //$NON-NLS-2$

			return orderList.toArray(new String[orderList.size()]);
		}

		return super.getExecutionOrder(group);
	}

	/**
	 * Specify how dynamic printf should be handled by GDB.
	 */
	@Execute
	public void stepSetDPrinfStyle(final RequestMonitor rm) {
		DsfServicesTracker tracker = new DsfServicesTracker(Activator.getBundleContext(), getSession().getId());
		IGDBControl gdbControl = tracker.getService(IGDBControl.class);
		tracker.dispose();

		if (gdbControl != null) {
			// For hardware debug the 'call' style does not work with GDB
			// Let's use the 'gdb' style instead
			gdbControl.queueCommand(gdbControl.getCommandFactory().createMIGDBSetDPrintfStyle(gdbControl.getContext(),
					MIGDBSetDPrintfStyle.GDB_STYLE), new ImmediateDataRequestMonitor<MIInfo>(rm) {
						@Override
						protected void handleCompleted() {
							// We accept errors
							rm.done();
						}
					});
		} else {
			// Should not happen but accept errors in this case
			rm.done();
		}
	}
}
