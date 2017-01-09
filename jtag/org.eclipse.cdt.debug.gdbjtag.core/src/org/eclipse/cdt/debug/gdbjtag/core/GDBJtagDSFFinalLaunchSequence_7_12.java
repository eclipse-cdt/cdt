/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Subclass for GDB >= 7.12.
 * 
 * @since 9.1
 */
public class GDBJtagDSFFinalLaunchSequence_7_12 extends GDBJtagDSFFinalLaunchSequence_7_7 {
	private IGDBControl fCommandControl;
	private CommandFactory fCommandFactory;

	public GDBJtagDSFFinalLaunchSequence_7_12(DsfSession session, Map<String, Object> attributes,
			RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the steps from the base class
			List<String> orderList = new ArrayList<String>(
					Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Now insert our steps right after the initialization of the base class.
			orderList.add(orderList.indexOf("stepInitializeJTAGSequence_7_2") + 1, //$NON-NLS-1$
					"stepInitializeFinalLaunchSequence_7_12"); //$NON-NLS-1$
			
			orderList.add(orderList.indexOf("stepSourceGDBInitFile") + 1, //$NON-NLS-1$
					"stepSetTargetAsync"); //$NON-NLS-1$

			return orderList.toArray(new String[orderList.size()]);
		}

		return super.getExecutionOrder(group);
	}

	@Execute
	public void stepInitializeFinalLaunchSequence_7_12(RequestMonitor rm) {
		DsfServicesTracker tracker = new DsfServicesTracker(Activator.getBundleContext(),
				getSession().getId());
		fCommandControl = tracker.getService(IGDBControl.class);
		tracker.dispose();

		if (fCommandControl == null) {
			rm.done(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Cannot obtain service", null)); //$NON-NLS-1$
			return;
		}

		fCommandFactory = fCommandControl.getCommandFactory();

		rm.done();
	}

	@Execute
	public void stepSetTargetAsync(RequestMonitor requestMonitor) {
		// Use target async when interfacing with GDB 7.12 or higher
		// this will allow us to use the new enhanced GDB Full CLI console
		fCommandControl.queueCommand(
				fCommandFactory.createMIGDBSetTargetAsync(fCommandControl.getContext(), true),
				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
					@Override
					protected void handleError() {
						// We should only be calling this for GDB >= 7.12,
						// but just in case, accept errors for older GDBs
						requestMonitor.done();
					}
				});
	}
}
