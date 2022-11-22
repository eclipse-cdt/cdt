/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
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
	private IGDBControl fCommandControl;
	private CommandFactory fCommandFactory;
	private Map<String, Object> fAttributes;
	private IGDBBackend fGdbBackEnd;

	public FinalLaunchSequence_7_12(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
		fAttributes = attributes;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Now insert our steps right after the initialization of the base class.
			orderList.add(orderList.indexOf("stepInitializeFinalLaunchSequence_7_7") + 1, //$NON-NLS-1$
					"stepInitializeFinalLaunchSequence_7_12"); //$NON-NLS-1$

			orderList.add(orderList.indexOf("stepSourceGDBInitFile") + 1, //$NON-NLS-1$
					"stepSetTargetAsync"); //$NON-NLS-1$

			orderList.add(orderList.indexOf("stepSetTargetAsync") + 1, //$NON-NLS-1$
					"stepSetRecordFullStopAtLimit"); //$NON-NLS-1$

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
		DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), getSession().getId());
		fCommandControl = tracker.getService(IGDBControl.class);
		fGdbBackEnd = tracker.getService(IGDBBackend.class);

		tracker.dispose();

		if (fCommandControl == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Cannot obtain service", null)); //$NON-NLS-1$
			return;
		}

		fCommandFactory = fCommandControl.getCommandFactory();

		rm.done();

	}

	private boolean isNonStop() {
		boolean isNonStop = CDebugUtils.getAttribute(fAttributes,
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, LaunchUtils.getIsNonStopModeDefault());
		return isNonStop;
	}

	@Execute
	public void stepSetTargetAsync(RequestMonitor requestMonitor) {
		if (fCommandControl == null || fGdbBackEnd == null) {
			requestMonitor.done();
			return;
		}

		// Use target async for non-stop mode or when
		// it is specifically enabled by backend in all-stop mode
		// Otherwise Explicitly set target-async to off
		boolean asyncOn = false;
		if (isNonStop() || fGdbBackEnd.useTargetAsync()) {
			asyncOn = true;
		}

		fCommandControl.queueCommand(fCommandFactory.createMIGDBSetMIAsync(fCommandControl.getContext(), asyncOn),
				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
					@Override
					protected void handleError() {
						// Accept errors for older GDBs
						requestMonitor.done();
					}
				});
	}

	/**
	 * Set reverse debugging record full stop-at-limit to off, so GDB does not halt waiting for user input
	 * when the recording buffer gets full
	 * @param requestMonitor
	 */
	@Execute
	public void stepSetRecordFullStopAtLimit(RequestMonitor requestMonitor) {
		fCommandControl.queueCommand(
				fCommandFactory.createMIGDBSetRecordFullStopAtLimit(fCommandControl.getContext(), false),
				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
					@Override
					protected void handleError() {
						// Accept errors since this is not essential
						requestMonitor.done();
					}
				});
	}

	@Override
	@Execute
	public void stepSetNonStop(final RequestMonitor requestMonitor) {
		if (isNonStop()) {
			// GDBs that don't support non-stop don't allow you to set it to false.
			// We really should set it to false when GDB supports it though.
			// Something to fix later.
			// Note: The base class is setting pagination to off, this is only necessary when
			// using the Full GDB console (The basic console is started in MI mode and does not paginate).
			// When the Full GDB console is used, pagination is set to off when GDB is started.
			fCommandControl.queueCommand(fCommandFactory.createMIGDBSetNonStop(fCommandControl.getContext(), true),
					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
		} else {
			requestMonitor.done();
		}
	}
}
