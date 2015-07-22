/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.launch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence_7_7;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.gdb.GDBExamplePlugin;
import org.eclipse.cdt.examples.dsf.gdb.service.IGDBExtendedFunctions;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class GdbExtendedFinalLaunchSequence extends FinalLaunchSequence_7_7 {

	private IGDBControl fControl;
	private DsfServicesTracker fTracker;

	public GdbExtendedFinalLaunchSequence(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<String>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Now insert our init step right after the initialization of the base class.
			orderList.add(orderList.indexOf("stepInitializeFinalLaunchSequence_7_7") + 1, "stepInitializeExtendedFinalLaunchSequence"); //$NON-NLS-1$ //$NON-NLS-2$

			// As the first operation to do, show the user the version of GDB
			orderList.add(orderList.indexOf("stepInitializeExtendedFinalLaunchSequence") + 1, "stepNotifyVersion"); //$NON-NLS-1$ //$NON-NLS-2$

			// Add the step to set pagination before the .gdbinit file is sourced
			// that way the user can override this setting using .gdbinit.
			orderList.add(orderList.indexOf("stepSourceGDBInitFile"), "stepSetPagination"); //$NON-NLS-1$ //$NON-NLS-2$
			
			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}
	
	@Execute
	public void stepInitializeExtendedFinalLaunchSequence(RequestMonitor rm) {
		fTracker = new DsfServicesTracker(GDBExamplePlugin.getBundleContext(), getSession().getId());
		fControl = fTracker.getService(IGDBControl.class);
		
        if (fControl == null) {
			rm.done(new Status(IStatus.ERROR, GDBExamplePlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot obtain service", null)); //$NON-NLS-1$
			return;
		}
		
		rm.done();
	}

	@RollBack("stepInitializeExtendedFinalLaunchSequence")
	public void rollBackInitializeExtendedFinalLaunchSequence(RequestMonitor rm) {
		if (fTracker != null) fTracker.dispose();
		fTracker = null;
		rm.done();
	}

	@Execute
	public void stepNotifyVersion(final RequestMonitor rm) {
		final IGDBExtendedFunctions funcService = fTracker.getService(IGDBExtendedFunctions.class);
		funcService.getVersion(
				fControl.getContext(), 
				new ImmediateDataRequestMonitor<String>(rm) {
					@Override
					protected void handleCompleted() {
						String str;
						if (isSuccess()) {
							str = "======= GDB version: " + getData() + " ======="; //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							str = "Could not obtain GDB version.  Error: " + //$NON-NLS-1$
											getStatus();
						}
						funcService.notify(fControl.getContext(), str, rm);
					}
				});
	}

	@Execute
	public void stepSetPagination(final RequestMonitor rm) {
		// Make sure pagination is always off
		fControl.queueCommand(
				fControl.getCommandFactory().createMIGDBSetPagination(fControl.getContext(), false),
				new ImmediateDataRequestMonitor<MIInfo>(rm) {
					@Override
					protected void handleCompleted() {
						// We accept errors
						rm.done();
					}
				});
	}

	@Execute
	public void stepCleanupExtendedFinalLaunchSequence(final RequestMonitor rm) {
		if (fTracker != null) fTracker.dispose();
		fTracker = null;
		rm.done();
	}
}
