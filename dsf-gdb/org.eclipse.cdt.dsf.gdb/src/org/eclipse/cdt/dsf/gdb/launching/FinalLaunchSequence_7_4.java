/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Subclass for GDB >= 7.4.
 * 
 * @since 4.4
 */
public class FinalLaunchSequence_7_4 extends FinalLaunchSequence_7_2 {

	private IGDBControl fCommandControl;
	private IGDBBackend	fGDBBackend;

	private DsfServicesTracker fTracker;
	private DsfSession fSession;

	public FinalLaunchSequence_7_4(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
		fSession = session;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<String>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Now insert our steps right after the initialization of the base class.
			orderList.add(orderList.indexOf("stepInitializeFinalLaunchSequence_7_2") + 1, "stepInitializeFinalLaunchSequence_7_4"); //$NON-NLS-1$ //$NON-NLS-2$
			// For GDB 7.4, breakpoints no longer need to be set for each inferior.  Than can be set once for GDB itself,
			// and GDB will properly install them for each inferior.
			orderList.add(orderList.indexOf("stepRemoteConnection") + 1, "stepStartTrackingBreakpoints"); //$NON-NLS-1$ //$NON-NLS-2$
			
			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}
	
	/** 
	 * Initialize the members of the FinalLaunchSequence_7_4 class.
	 * This step is mandatory for the rest of the sequence to complete.
	 */
	@Execute
	public void stepInitializeFinalLaunchSequence_7_4(RequestMonitor rm) {
		fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSession.getId());
		fGDBBackend = fTracker.getService(IGDBBackend.class);
		if (fGDBBackend == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain GDBBackend service", null)); //$NON-NLS-1$
			return;
		}

		fCommandControl = fTracker.getService(IGDBControl.class);
		if (fCommandControl == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain control service", null)); //$NON-NLS-1$
			return;
		}

		rm.done();
	}

	/** 
	 * Rollback method for {@link #stepInitializeFinalLaunchSequence_7_4()}
	 */
	@RollBack("stepInitializeFinalLaunchSequence_7_4")
	public void rollBackInitializeFinalLaunchSequence_7_4(RequestMonitor rm) {
		if (fTracker != null) fTracker.dispose();
		fTracker = null;
		rm.done();
	}

	/**
	 * Start tracking the breakpoints.  Note that for remote debugging
	 * we should first connect to the target.
	 */
	@Execute
	public void stepStartTrackingBreakpoints(RequestMonitor rm) {
		if (fGDBBackend.getSessionType() != SessionType.CORE) {
			MIBreakpointsManager bpmService = fTracker.getService(MIBreakpointsManager.class);
			IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fCommandControl.getContext(), IBreakpointsTargetDMContext.class);
			bpmService.startTrackingBreakpoints(bpTargetDmc, rm);
		} else {
			rm.done();
		}
	}

}
