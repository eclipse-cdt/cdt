/*******************************************************************************
 * Copyright (c) 2014 - 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsSynchronizer;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Breakpoints service for GDB 7.7.
 * This version supports dynamic printf
 *
 * @since 4.4
 */
public class GDBBreakpoints_7_7 extends GDBBreakpoints_7_6 {

	private IMICommandControl fConnection;
	private IMIRunControl fRunControl;
	private CommandFactory fCommandFactory;

	public GDBBreakpoints_7_7(DsfSession session) {
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
		fConnection = getServicesTracker().getService(IMICommandControl.class);
		fRunControl = getServicesTracker().getService(IMIRunControl.class);
		if (fConnection == null || fRunControl == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Service is not available")); //$NON-NLS-1$
			return;
		}

		fCommandFactory = fConnection.getCommandFactory();

		// Register this service
		register(
				new String[] { IBreakpoints.class.getName(), IBreakpointsExtension.class.getName(),
						MIBreakpoints.class.getName(), GDBBreakpoints_7_0.class.getName(),
						GDBBreakpoints_7_2.class.getName(), GDBBreakpoints_7_4.class.getName(),
						GDBBreakpoints_7_6.class.getName(), GDBBreakpoints_7_7.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}

	@Override
	protected void addDynamicPrintf(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes,
			final DataRequestMonitor<IBreakpointDMContext> drm) {
		final MIBreakpointsSynchronizer bs = getServicesTracker().getService(MIBreakpointsSynchronizer.class);
		if (bs != null) {
			// Skip the dprintf set from the console or from outside of Eclipse
			// because they are already installed on the target.
			bs.getTargetBreakpoint(context, attributes, new DataRequestMonitor<MIBreakpoint>(getExecutor(), drm) {
				@Override
				@ConfinedToDsfExecutor("fExecutor")
				protected void handleSuccess() {
					MIBreakpoint miBpt = getData();
					if (miBpt != null) {
						bs.removeCreatedTargetBreakpoint(context, miBpt);
						MIBreakpointDMData newBreakpoint = createMIBreakpointDMData(miBpt);
						getBreakpointMap(context).put(newBreakpoint.getNumber(), newBreakpoint);
						IBreakpointDMContext dmc = new MIBreakpointDMContext(GDBBreakpoints_7_7.this,
								new IDMContext[] { context }, newBreakpoint.getNumber());
						drm.setData(dmc);
						getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());
						drm.done();
					} else {
						doAddDynamicPrintf(context, attributes, drm);
					}
				}
			});
		} else {
			doAddDynamicPrintf(context, attributes, drm);
		}
	}

	/**
	 * Add a Dynamic Printf.
	 */
	protected void doAddDynamicPrintf(final IBreakpointsTargetDMContext context, Map<String, Object> attributes,
			final DataRequestMonitor<IBreakpointDMContext> finalRm) {
		// Select the context breakpoints map
		final Map<String, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
			finalRm.done(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			return;
		}

		// Extract the relevant parameters (providing default values to avoid potential NPEs)
		final String location = formatLocation(attributes);
		if (location.equals(NULL_STRING)) {
			finalRm.done(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			return;
		}

		final String printfStr = (String) getProperty(attributes, MIBreakpoints.PRINTF_STRING, ""); //$NON-NLS-1$
		final Boolean enabled = (Boolean) getProperty(attributes, MIBreakpoints.IS_ENABLED, true);
		final Boolean isTemporary = (Boolean) getProperty(attributes, MIBreakpointDMData.IS_TEMPORARY, false);
		final String condition = (String) getProperty(attributes, MIBreakpoints.CONDITION, NULL_STRING);
		final Integer ignoreCount = (Integer) getProperty(attributes, MIBreakpoints.IGNORE_COUNT, 0);
		String threadId = (String) getProperty(attributes, MIBreakpointDMData.THREAD_ID, "0"); //$NON-NLS-1$
		final int tid = Integer.parseInt(threadId);

		final Step insertDPrintf = new Step() {
			@Override
			public void execute(final RequestMonitor rm) {
				// Execute the command
				fConnection.queueCommand(
						fCommandFactory.createMIDPrintfInsert(context, isTemporary, condition, ignoreCount, tid,
								!enabled, location, printfStr),
						new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), rm) {
							@Override
							protected void handleSuccess() {

								// With MI, an invalid location won't generate an error
								if (getData().getMIBreakpoints().length == 0) {
									rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
											DYNAMIC_PRINTF_INSERTION_FAILURE, null));
									return;
								}

								// Create a breakpoint object and store it in the map
								final MIBreakpointDMData newBreakpoint = createMIBreakpointDMData(
										getData().getMIBreakpoints()[0]);
								String reference = newBreakpoint.getNumber();
								if (reference.isEmpty()) {
									rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
											DYNAMIC_PRINTF_INSERTION_FAILURE, null));
									return;
								}
								contextBreakpoints.put(reference, newBreakpoint);

								// Format the return value
								MIBreakpointDMContext dmc = new MIBreakpointDMContext(GDBBreakpoints_7_7.this,
										new IDMContext[] { context }, reference);
								finalRm.setData(dmc);

								// Flag the event
								getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

								rm.done();
							}

							@Override
							protected void handleError() {
								rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
										DYNAMIC_PRINTF_INSERTION_FAILURE, getStatus().getException()));
							}
						});
			}
		};

		fRunControl.executeWithTargetAvailable(context, new Step[] { insertDPrintf }, finalRm);
	}
}
