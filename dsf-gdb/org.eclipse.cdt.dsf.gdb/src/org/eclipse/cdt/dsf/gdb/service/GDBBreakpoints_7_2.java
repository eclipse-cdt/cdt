/*******************************************************************************
 * Copyright (c) 2011, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Marc Khouzam (Ericsson) - Support for fast tracepoints (Bug 346320)
 *     Marc Khouzam (Ericsson) - Fetch groupIds when getting breakpoints (Bug 360735)
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
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoBreakInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;

/**
 * Breakpoint service for GDB 7.2.
 * It support MI for tracepoints.
 * It also support for fast vs normal tracepoints.
 *
 * @since 4.1
 */
public class GDBBreakpoints_7_2 extends GDBBreakpoints_7_0 {
	private IMICommandControl fConnection;

	private enum TracepointMode {
		FAST_THEN_NORMAL, FAST_ONLY, NORMAL_ONLY
	}

	private TracepointMode fTracepointMode = TracepointMode.NORMAL_ONLY;

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
		register(new String[] { IBreakpoints.class.getName(), IBreakpointsExtension.class.getName(),
				MIBreakpoints.class.getName(), GDBBreakpoints_7_0.class.getName(), GDBBreakpoints_7_2.class.getName() },
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
	 * Starting with GDB 7.2, also provides information about which process each breakpoint applies to.
	 */
	@Override
	public void getBreakpoints(final IBreakpointsTargetDMContext context,
			final DataRequestMonitor<IBreakpointDMContext[]> drm) {
		if (bpThreadGroupInfoAvailable()) {
			// With GDB 7.6, we obtain the thread-groups to which a breakpoint applies
			// directly in the -break-list command, so we don't need to do any special processing.
			super.getBreakpoints(context, drm);
			return;
		}

		// Validate the context
		if (context == null) {
			drm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_EXECUTION_CONTEXT, null));
			drm.done();
			return;
		}

		// Select the breakpoints context map
		// If it doesn't exist then no breakpoint was ever inserted for this breakpoint space.
		// In that case, return an empty list.
		final Map<String, MIBreakpointDMData> breakpointContext = getBreakpointMap(context);
		if (breakpointContext == null) {
			drm.setData(new IBreakpointDMContext[0]);
			drm.done();
			return;
		}

		// Execute the command
		fConnection.queueCommand(fConnection.getCommandFactory().createMIBreakList(context),
				new DataRequestMonitor<MIBreakListInfo>(getExecutor(), drm) {
					@Override
					protected void handleSuccess() {
						final MIBreakpoint[] breakpoints = getData().getMIBreakpoints();

						// Also fetch the information about which breakpoint belongs to which
						// process.  We currently can only obtain this information from the CLI
						// command.  This information is needed for breakpoint filtering.
						// Bug 360735
						fConnection.queueCommand(fConnection.getCommandFactory().createCLIInfoBreak(context),
								new ImmediateDataRequestMonitor<CLIInfoBreakInfo>(drm) {
									@Override
									protected void handleSuccess() {
										Map<String, String[]> groupIdMap = getData().getBreakpointToGroupMap();

										// Refresh the breakpoints map and format the result
										breakpointContext.clear();
										IBreakpointDMContext[] result = new IBreakpointDMContext[breakpoints.length];
										for (int i = 0; i < breakpoints.length; i++) {
											MIBreakpointDMData breakpointData = createMIBreakpointDMData(
													breakpoints[i]);

											// Now fill in the thread-group information into the breakpoint data
											// It is ok to get null.  For example, pending breakpoints are not
											// associated to a thread-group; also, when debugging a single process,
											// the thread-group list is empty.
											String reference = breakpointData.getReference();
											String[] groupIds = groupIdMap.get(reference);
											breakpointData.setGroupIds(groupIds);

											result[i] = new MIBreakpointDMContext(GDBBreakpoints_7_2.this,
													new IDMContext[] { context }, reference);
											breakpointContext.put(reference, breakpointData);
										}
										drm.setData(result);
										drm.done();
									}
								});
					}
				});
	}

	private void setTracepointMode() {
		ILaunch launch = (ILaunch) getSession().getModelAdapter(ILaunch.class);
		String tpMode = IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_MODE_DEFAULT;
		try {
			tpMode = launch.getLaunchConfiguration().getAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_TRACEPOINT_MODE,
					IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_MODE_DEFAULT);
		} catch (CoreException e) {
		}

		if (tpMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_FAST_ONLY)) {
			fTracepointMode = TracepointMode.FAST_ONLY;
		} else if (tpMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_NORMAL_ONLY)) {
			fTracepointMode = TracepointMode.NORMAL_ONLY;
		} else if (tpMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_FAST_THEN_NORMAL)) {
			fTracepointMode = TracepointMode.FAST_THEN_NORMAL;
		} else {
			assert false : "Invalid tracepoint mode: " + tpMode; //$NON-NLS-1$
			fTracepointMode = TracepointMode.NORMAL_ONLY;
		}
	}

	protected void sendTracepointCommand(final IBreakpointsTargetDMContext context,
			final Map<String, Object> attributes, boolean isFastTracepoint,
			final DataRequestMonitor<IBreakpointDMContext> drm) {
		// Select the context breakpoints map
		final Map<String, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
			drm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			drm.done();
			return;
		}

		// Extract the relevant parameters (providing default values to avoid potential NPEs)
		final String location = formatLocation(attributes);
		if (location.equals(NULL_STRING)) {
			drm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			drm.done();
			return;
		}

		final Boolean enabled = (Boolean) getProperty(attributes, MIBreakpoints.IS_ENABLED, true);
		final String condition = (String) getProperty(attributes, MIBreakpoints.CONDITION, NULL_STRING);

		fConnection.queueCommand(
				fConnection.getCommandFactory().createMIBreakInsert(context, false, isFastTracepoint, condition, 0,
						location, "0", !enabled, true), //$NON-NLS-1$
				new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), drm) {
					@Override
					protected void handleSuccess() {
						// With MI, an invalid location won't generate an error
						if (getData().getMIBreakpoints().length == 0) {
							drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
									BREAKPOINT_INSERTION_FAILURE, null));
							drm.done();
							return;
						}

						// Create a breakpoint object and store it in the map
						final MIBreakpointDMData newBreakpoint = createMIBreakpointDMData(
								getData().getMIBreakpoints()[0]);
						String reference = newBreakpoint.getNumber();
						if (reference.isEmpty()) {
							drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
									BREAKPOINT_INSERTION_FAILURE, null));
							drm.done();
							return;
						}
						contextBreakpoints.put(reference, newBreakpoint);

						// Format the return value
						MIBreakpointDMContext dmc = new MIBreakpointDMContext(GDBBreakpoints_7_2.this,
								new IDMContext[] { context }, reference);
						drm.setData(dmc);

						// Flag the event
						getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

						// Tracepoints are created with no passcount (passcount are not
						// the same thing as ignore-count, which is not supported by
						// tracepoints).  We have to set the passcount manually now.
						// Same for commands.
						Map<String, Object> delta = new HashMap<>();
						delta.put(MIBreakpoints.PASS_COUNT, getProperty(attributes, MIBreakpoints.PASS_COUNT, 0));
						delta.put(MIBreakpoints.COMMANDS, getProperty(attributes, MIBreakpoints.COMMANDS, "")); //$NON-NLS-1$
						modifyBreakpoint(dmc, delta, drm, false);
					}

					@Override
					protected void handleError() {
						drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
								BREAKPOINT_INSERTION_FAILURE, getStatus().getException()));
						drm.done();
					}
				});
	}

	/**
	 * Add a tracepoint using MI.  We have three settings:
	 *   1- set only a fast tracepoint but if it fails, set a normal tracepoint
	 *   2- only set a fast tracepoint even if it fails
	 *   3- only set a normal tracepoint even if a fast tracepoint could have been used
	 */
	@Override
	protected void addTracepoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes,
			final DataRequestMonitor<IBreakpointDMContext> drm) {
		// Unless we should only set normal tracepoints, we try to set a fast tracepoint.
		boolean isFastTracepoint = fTracepointMode != TracepointMode.NORMAL_ONLY;

		sendTracepointCommand(context, attributes, isFastTracepoint,
				new ImmediateDataRequestMonitor<IBreakpointDMContext>(drm) {
					@Override
					protected void handleSuccess() {
						// Tracepoint was set successfully.
						drm.setData(getData());
						drm.done();
					}

					@Override
					protected void handleError() {
						// Tracepoint failed to be set.
						if (fTracepointMode == TracepointMode.FAST_THEN_NORMAL) {
							// In this case, we failed to set a fast tracepoint, but we should try to set a normal one.
							sendTracepointCommand(context, attributes, false, drm);
						} else {
							// We either failed to set a fast tracepoint and we should not try to set a normal one,
							// or we failed to set a normal one.  Either way, we are done.
							drm.setStatus(getStatus());
							drm.done();
						}
					}
				});
	}

	/**
	 * Does the MI command -break-list provide information
	 * about which thread-group a breakpoint applies to?
	 * The use of this method allows us to avoid duplicating code.
	 * See Bug 402217
	 *
	 * @return true if the information is available (GDB >= 7.6),
	 *         false otherwise.
	 * @since 4.2
	 */
	protected boolean bpThreadGroupInfoAvailable() {
		return false;
	}
}
