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
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetDPrintfStyle;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Subclass for GDB >= 7.7.
 *
 * @since 4.4
 */
public class FinalLaunchSequence_7_7 extends FinalLaunchSequence_7_2 {

	private IGDBControl fControl;

	public FinalLaunchSequence_7_7(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Now insert our steps right after the initialization of the base class.
			orderList.add(orderList.indexOf("stepInitializeFinalLaunchSequence_7_2") + 1, //$NON-NLS-1$
					"stepInitializeFinalLaunchSequence_7_7"); //$NON-NLS-1$
			orderList.add(orderList.indexOf("stepSourceGDBInitFile"), "stepSetDPrinfStyle"); //$NON-NLS-1$ //$NON-NLS-2$

			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}

	/**
	 * Initialize the members of the FinalLaunchSequence_7_7 class.
	 * This step is mandatory for the rest of the sequence to complete.
	 */
	@Execute
	public void stepInitializeFinalLaunchSequence_7_7(RequestMonitor rm) {
		DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), getSession().getId());
		fControl = tracker.getService(IGDBControl.class);
		tracker.dispose();

		if (fControl == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Cannot obtain service", null)); //$NON-NLS-1$
			return;
		}

		rm.done();
	}

	/**
	 * Specify how dynamic printf should be handled by GDB.
	 */
	@Execute
	public void stepSetDPrinfStyle(final RequestMonitor rm) {
		// We use the 'call' style which will
		// have dprintf call the printf function in the program.
		fControl.queueCommand(fControl.getCommandFactory().createMIGDBSetDPrintfStyle(fControl.getContext(),
				MIGDBSetDPrintfStyle.CALL_STYLE), new ImmediateDataRequestMonitor<MIInfo>(rm) {
					@Override
					protected void handleCompleted() {
						// We accept errors
						rm.done();
					}
				});
	}
}
