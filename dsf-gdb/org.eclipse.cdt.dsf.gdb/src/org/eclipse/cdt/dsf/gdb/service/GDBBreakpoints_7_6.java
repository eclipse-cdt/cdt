/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation 
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Breakpoint service for GDB 7.6.
 *
 * @since 4.2
 */
public class GDBBreakpoints_7_6 extends GDBBreakpoints_7_4
{
	private IMICommandControl fConnection;

	public GDBBreakpoints_7_6(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(final RequestMonitor rm) {
    	// Get the services references
		fConnection = getServicesTracker().getService(IMICommandControl.class);
		
		// Register this service
		register(new String[] { IBreakpoints.class.getName(),
		                        IBreakpointsExtension.class.getName(),
								MIBreakpoints.class.getName(),
								GDBBreakpoints_7_0.class.getName(),
								GDBBreakpoints_7_2.class.getName(),
								GDBBreakpoints_7_4.class.getName(),
								GDBBreakpoints_7_6.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
        unregister();
		super.shutdown(requestMonitor);
	}
	
	/** 
	 * {@inheritDoc}
	 * 
	 * Starting with GDB 7.6, no longer need to use CLIBreakInfo since -break-list will tell use which process each breakpoint applies to.
	 */
	@Override
	public void getBreakpoints(final IBreakpointsTargetDMContext context, final DataRequestMonitor<IBreakpointDMContext[]> drm)
	{
		// Validate the context
		if (context == null) {
       		drm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_EXECUTION_CONTEXT, null));
			return;
		}

		// Select the breakpoints context map
		// If it doesn't exist then no breakpoint was ever inserted for this breakpoint space.
		// In that case, return an empty list.
		final Map<Integer, MIBreakpointDMData> breakpointContext = getBreakpointMap(context);
		if (breakpointContext == null) {
       		drm.done(new IBreakpointDMContext[0]);
			return;
		}

		// Execute the command
		fConnection.queueCommand(fConnection.getCommandFactory().createMIBreakList(context),
			new DataRequestMonitor<MIBreakListInfo>(getExecutor(), drm) {
				@Override
				protected void handleSuccess() {
					final MIBreakpoint[] breakpoints = getData().getMIBreakpoints();

					// Refresh the breakpoints map and format the result
					breakpointContext.clear();
					IBreakpointDMContext[] result = new IBreakpointDMContext[breakpoints.length];
					for (int i = 0; i < breakpoints.length; i++) {
						MIBreakpointDMData breakpointData = new MIBreakpointDMData(breakpoints[i]);
						int reference = breakpointData.getReference();						
						result[i] = new MIBreakpointDMContext(GDBBreakpoints_7_6.this, new IDMContext[] { context }, reference);
						breakpointContext.put(reference, breakpointData);
					}
					drm.done(result);
				}
		});
	}
}
