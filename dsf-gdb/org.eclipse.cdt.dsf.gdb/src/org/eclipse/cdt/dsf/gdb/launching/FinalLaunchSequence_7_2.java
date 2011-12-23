/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc Khouzam (Ericsson) - Use new FinalLaunchSequence_7_0 as base class (Bug 365471)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Subclass for GDB >= 7.2.
 * 
 * @since 4.0
 */
public class FinalLaunchSequence_7_2 extends FinalLaunchSequence_7_0 {

	private IGDBControl fGdbControl;
	private DsfSession fSession;
	
	// The launchConfiguration attributes
	private Map<String, Object> fAttributes;

	public FinalLaunchSequence_7_2(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
		fSession = session;
		fAttributes = attributes;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<String>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Now insert our steps right after the initialization of the base class.
			orderList.add(orderList.indexOf("stepInitializeFinalLaunchSequence_7_0") + 1, "stepInitializeFinalLaunchSequence_7_2"); //$NON-NLS-1$ //$NON-NLS-2$
			orderList.add(orderList.indexOf("stepSetBreakpointPending") + 1, "stepDetachOnFork"); //$NON-NLS-1$ //$NON-NLS-2$
			
			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}
	
	/** 
	 * Initialize the members of the FinalLaunchSequence_7_2 class.
	 * This step is mandatory for the rest of the sequence to complete.
	 */
	@Execute
	public void stepInitializeFinalLaunchSequence_7_2(RequestMonitor rm) {
		DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSession.getId());
		fGdbControl = tracker.getService(IGDBControl.class);
		tracker.dispose();
		
        if (fGdbControl == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot obtain service", null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		
		rm.done();
	}
	
	/**
	 * Tell GDB whether to automatically attach to a forked process or not.
	 */
	@Execute
	public void stepDetachOnFork(final RequestMonitor rm) {
		boolean debugOnFork = CDebugUtils.getAttribute(fAttributes, 
				                                       IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_DEBUG_ON_FORK,
				                                       IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_ON_FORK_DEFAULT);

		fGdbControl.queueCommand(
				fGdbControl.getCommandFactory().createMIGDBSetDetachOnFork(fGdbControl.getContext(), !debugOnFork), 
				new ImmediateDataRequestMonitor<MIInfo>(rm));
	}
}
