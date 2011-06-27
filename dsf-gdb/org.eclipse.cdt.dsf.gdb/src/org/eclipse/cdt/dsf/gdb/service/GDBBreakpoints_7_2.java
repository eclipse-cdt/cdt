/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Breakpoint service for GDB 7.2.
 * It support MI for tracepoints.
 *
 * @since 4.1
 */
public class GDBBreakpoints_7_2 extends GDBBreakpoints_7_0
{
	private IMICommandControl fConnection;

	public GDBBreakpoints_7_2(DsfSession session) {
		super(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new RequestMonitor(ImmediateExecutor.getInstance(), rm) {
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
								GDBBreakpoints_7_2.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
        unregister();
		super.shutdown(requestMonitor);
	}

	/**
	 * Add a tracepoint using MI
	 */
	@Override
	protected void addTracepoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> drm)
	{
		// Select the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		drm.done();
			return;
		}

		// Extract the relevant parameters (providing default values to avoid potential NPEs)
		final String location = formatLocation(attributes);
		if (location.equals(NULL_STRING)) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		drm.done();
			return;
		}

		
		final Boolean enabled        = (Boolean) getProperty(attributes, MIBreakpoints.IS_ENABLED,        true);
		final Boolean isHardware     = (Boolean) getProperty(attributes, MIBreakpointDMData.IS_HARDWARE,  false);
		final String  condition      = (String)  getProperty(attributes, MIBreakpoints.CONDITION,         NULL_STRING);

		fConnection.queueCommand(
				fConnection.getCommandFactory().createMIBreakInsert(context, false, isHardware, condition, 0, location, 0, !enabled, true),
				new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), drm) {
					@Override
					protected void handleSuccess() {
						// With MI, an invalid location won't generate an error
						if (getData().getMIBreakpoints().length == 0) {
							drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, BREAKPOINT_INSERTION_FAILURE, null));
							drm.done();
							return;
						}

						// Create a breakpoint object and store it in the map
						final MIBreakpointDMData newBreakpoint = new MIBreakpointDMData(getData().getMIBreakpoints()[0]);
						int reference = newBreakpoint.getNumber();
						if (reference == -1) {
							drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, BREAKPOINT_INSERTION_FAILURE, null));
							drm.done();
							return;
						}
						contextBreakpoints.put(reference, newBreakpoint);

						// Format the return value
						MIBreakpointDMContext dmc = new MIBreakpointDMContext(GDBBreakpoints_7_2.this, new IDMContext[] { context }, reference);
						drm.setData(dmc);

						// Flag the event
						getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());
						
						// Tracepoints are created with no passcount (passcount are not 
						// the same thing as ignore-count, which is not supported by
						// tracepoints).  We have to set the passcount manually now.
						// Same for commands.
						Map<String,Object> delta = new HashMap<String,Object>();
						delta.put(MIBreakpoints.PASS_COUNT, getProperty(attributes, MIBreakpoints.PASS_COUNT, 0));
						delta.put(MIBreakpoints.COMMANDS, getProperty(attributes, MIBreakpoints.COMMANDS, "")); //$NON-NLS-1$
						modifyBreakpoint(dmc, delta, drm, false);
					}

					@Override
					protected void handleError() {
						drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, BREAKPOINT_INSERTION_FAILURE, null));
						drm.done();
					}
				});
	}
}
