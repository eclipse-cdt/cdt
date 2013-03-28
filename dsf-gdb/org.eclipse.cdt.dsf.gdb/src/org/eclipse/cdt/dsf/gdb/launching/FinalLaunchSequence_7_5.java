/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
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
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetDPrintfStyle;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Subclass for GDB >= 7.5.
 * 
 * @since 4.3
 */
public class FinalLaunchSequence_7_5 extends FinalLaunchSequence_7_2 {

	private IGDBControl fControl;
	private IGDBBackend fBackend;
	private DsfSession fSession;
	
	public FinalLaunchSequence_7_5(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
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
			orderList.add(orderList.indexOf("stepInitializeFinalLaunchSequence_7_2") + 1, "stepInitializeFinalLaunchSequence_7_5"); //$NON-NLS-1$ //$NON-NLS-2$
			orderList.add(orderList.indexOf("stepSourceGDBInitFile"), "stepSetDPrinfStyle"); //$NON-NLS-1$ //$NON-NLS-2$
			
			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}
	
	/** 
	 * Initialize the members of the FinalLaunchSequence_7_5 class.
	 * This step is mandatory for the rest of the sequence to complete.
	 */
	@Execute
	public void stepInitializeFinalLaunchSequence_7_5(RequestMonitor rm) {
		DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSession.getId());
		fControl = tracker.getService(IGDBControl.class);
		fBackend = tracker.getService(IGDBBackend.class);
		tracker.dispose();
		
        if (fControl == null || fBackend == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot obtain service", null)); //$NON-NLS-1$
			return;
		}
		
		rm.done();
	}
	
	/**
	 * Specify how dynamic printf should be handled by GDB.
	 */
	@Execute
	public void stepSetDPrinfStyle(final RequestMonitor rm) {
		// For local sessions we use the 'call' style which will
		// have dprintf call the printf function in the program.
		String style = MIGDBSetDPrintfStyle.CALL_STYLE;
		if (fBackend.getSessionType() == SessionType.REMOTE) {
			// For remote sessions we can use the 'agent' style.
			// The agent style is more efficient but only available
			// with gdbserver.
			style = MIGDBSetDPrintfStyle.AGENT_STYLE;
		}
		
		fControl.queueCommand(
				fControl.getCommandFactory().createMIGDBSetDPrintfStyle(fControl.getContext(), style),
				new ImmediateDataRequestMonitor<MIInfo>(rm) {
					@Override
					protected void handleCompleted() {
						// We accept errors
						rm.done();
					}
				});
	}
}
