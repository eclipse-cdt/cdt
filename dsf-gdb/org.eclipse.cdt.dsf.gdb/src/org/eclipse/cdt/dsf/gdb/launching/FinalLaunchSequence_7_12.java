/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
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
 * @since 5.2
 */
public class FinalLaunchSequence_7_12 extends FinalLaunchSequence_7_7 {

	private IGDBControl fControl;
	private IGDBControl fCommandControl;
	private CommandFactory fCommandFactory;
	private Map<String, Object> fAttributes;

	public FinalLaunchSequence_7_12(DsfSession session, Map<String, Object> attributes,
			RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
		fAttributes = attributes;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<String>(
					Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Now insert our steps right after the initialization of the base class.
			orderList.add(orderList.indexOf("stepInitializeFinalLaunchSequence_7_7") + 1, //$NON-NLS-1$
					"stepInitializeFinalLaunchSequence_7_12"); //$NON-NLS-1$

			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}

	/**
	 * Initialize the members of the FinalLaunchSequence_7_12 class. This step is mandatory for the rest of
	 * the sequence to complete.
	 */
	@Execute
	public void stepInitializeFinalLaunchSequence_7_12(RequestMonitor rm) {
		DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(),
				getSession().getId());
		fControl = tracker.getService(IGDBControl.class);
		tracker.dispose();

		if (fControl == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Cannot obtain service", null)); //$NON-NLS-1$
			return;
		}

		fCommandFactory = fCommandControl.getCommandFactory();

		rm.done();

	}

	@Override
	@Execute
	public void stepSetNonStop(final RequestMonitor requestMonitor) {
		boolean isNonStop = CDebugUtils.getAttribute(fAttributes,
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
				LaunchUtils.getIsNonStopModeDefault());

		// Use target async when interfacing with GDB 7.12 or higher
		// this will allow us to use the new enhanced GDB Full CLI console
		fCommandControl.queueCommand(
				fCommandFactory.createMIGDBSetTargetAsync(fCommandControl.getContext(), true),
				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
					@Override
					protected void handleSuccess() {
						if (isNonStop) {
							// GDBs that don't support non-stop don't allow you to set it to false.
							// We really should set it to false when GDB supports it though.
							// Something to fix later.
							fCommandControl.queueCommand(
									fCommandFactory.createMIGDBSetNonStop(fCommandControl.getContext(), true),
									new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));

							// Note: No need to set Pagination to off, this is already set by the
							// GDBBackend_7.12
						} else {
							super.handleSuccess();
						}
					}

					@Override
					protected void handleError() {
						// We should only be calling this for GDB >= 7.0,
						// but just in case, accept errors for older GDBs
						requestMonitor.done();
					}
				});
	}
}
