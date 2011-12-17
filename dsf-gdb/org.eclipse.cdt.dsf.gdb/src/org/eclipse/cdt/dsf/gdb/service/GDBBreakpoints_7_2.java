/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation 
 *     Marc Khouzam (Ericsson) - Support for fast tracepoints (Bug 346320)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;

/**
 * Breakpoint service for GDB 7.2.
 * It support MI for tracepoints.
 * It also support for fast vs slow tracepoints.
 *
 * @since 4.1
 */
public class GDBBreakpoints_7_2 extends GDBBreakpoints_7_0
{
	private IMICommandControl fConnection;
	
	private enum TracepointMode { FAST_THEN_SLOW, FAST_ONLY, SLOW_ONLY };
	   
	private TracepointMode fTracepointMode = TracepointMode.SLOW_ONLY;

	public GDBBreakpoints_7_2(DsfSession session) {
		super(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
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

		setTracepointMode();
		
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
	
	private void setTracepointMode() {
		ILaunch launch = (ILaunch)getSession().getModelAdapter(ILaunch.class);
		String tpMode = IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_MODE_DEFAULT;
		try {
			tpMode = launch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_TRACEPOINT_MODE,
					IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_MODE_DEFAULT);
		} catch (CoreException e) {
		}

		if (tpMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_FAST_ONLY)) {
			fTracepointMode = TracepointMode.FAST_ONLY;
		} else if (tpMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_SLOW_ONLY)) {
			fTracepointMode = TracepointMode.SLOW_ONLY;
		} else if (tpMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_FAST_THEN_SLOW)) {
			fTracepointMode = TracepointMode.FAST_THEN_SLOW;
		} else {
			assert false : "Invalid tracepoint mode: " + tpMode; //$NON-NLS-1$
			fTracepointMode = TracepointMode.SLOW_ONLY;         
		}
	}

	protected void sendTracepointCommand(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes, boolean isFastTracepoint, final DataRequestMonitor<IBreakpointDMContext> drm) {
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
		final String  condition      = (String)  getProperty(attributes, MIBreakpoints.CONDITION,         NULL_STRING);

		fConnection.queueCommand(
				fConnection.getCommandFactory().createMIBreakInsert(context, false, isFastTracepoint, condition, 0, location, 0, !enabled, true),
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
	/**
	 * Add a tracepoint using MI.  We have three settings:
	 *   1- set only a fast tracepoint but if it fails, set a slow tracepoint
	 *   2- only set a fast tracepoint even if it fails
	 *   3- only set a slow tracepoint even if a fast tracepoint could have been used
	 */
	@Override
	protected void addTracepoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> drm) {
		// Unless we should only set slow tracepoints, we try to set a fast tracepoint.
		boolean isFastTracepoint = fTracepointMode != TracepointMode.SLOW_ONLY;

		sendTracepointCommand(context, attributes, isFastTracepoint, new ImmediateDataRequestMonitor<IBreakpointDMContext>(drm) {
			@Override
			protected void handleSuccess() {
				// Tracepoint was set successfully.
				drm.setData(getData());
				drm.done();
			}
			@Override
			protected void handleError() {
				// Tracepoint failed to be set.
				if (fTracepointMode == TracepointMode.FAST_THEN_SLOW) {
					// In this case, we failed to set a fast tracepoint, but we should try to set a slow one.
					sendTracepointCommand(context, attributes, false, drm);
				} else {
					// We either failed to set a fast tracepoint and we should not try to set a slow one,
					// or we failed to set a slow one.  Either way, we are done.
					drm.setStatus(getStatus());
					drm.done();
				}
			}
		});
	}
}
