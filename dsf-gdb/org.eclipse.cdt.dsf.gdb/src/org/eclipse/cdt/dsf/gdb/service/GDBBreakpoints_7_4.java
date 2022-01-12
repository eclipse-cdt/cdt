/*******************************************************************************
 * Copyright (c) 2012, 2016 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsSynchronizer;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MITuple;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Breakpoints service for GDB 7.4.
 * Using breakpoint notifications introduced in 7.4 supports synchronization between the breakpoints
 * set from the GDB console and the Breakpoints view as well as the tracepoints reported form trace files.
 *
 * @since 4.2
 */
public class GDBBreakpoints_7_4 extends GDBBreakpoints_7_2 implements IEventListener {

	// Breakpoint notifications
	private static final String BREAKPOINT_PREFIX = "breakpoint-"; //$NON-NLS-1$
	private static final String BREAKPOINT_CREATED = BREAKPOINT_PREFIX + "created"; //$NON-NLS-1$
	private static final String BREAKPOINT_MODIFIED = BREAKPOINT_PREFIX + "modified"; //$NON-NLS-1$
	private static final String BREAKPOINT_DELETED = BREAKPOINT_PREFIX + "deleted"; //$NON-NLS-1$

	private IMICommandControl fConnection;

	public GDBBreakpoints_7_4(DsfSession session) {
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
		if (fConnection == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Service is not available")); //$NON-NLS-1$
			rm.done();
			return;
		}
		fConnection.addEventListener(this);

		// Register this service
		register(
				new String[] { IBreakpoints.class.getName(), IBreakpointsExtension.class.getName(),
						MIBreakpoints.class.getName(), GDBBreakpoints_7_0.class.getName(),
						GDBBreakpoints_7_2.class.getName(), GDBBreakpoints_7_4.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		ICommandControl control = getCommandControl();
		if (control != null) {
			control.removeEventListener(this);
		}
		unregister();
		super.shutdown(requestMonitor);
	}

	@Override
	public void eventReceived(Object output) {
		if (output instanceof MIOutput) {
			MIBreakpointsSynchronizer bs = getServicesTracker().getService(MIBreakpointsSynchronizer.class);
			if (bs != null) {
				MIOOBRecord[] records = ((MIOutput) output).getMIOOBRecords();
				for (MIOOBRecord r : records) {
					if (r instanceof MINotifyAsyncOutput) {
						MINotifyAsyncOutput notifyOutput = (MINotifyAsyncOutput) r;
						String asyncClass = notifyOutput.getAsyncClass();
						if (BREAKPOINT_CREATED.equals(asyncClass)) {
							MIBreakpoint bpt = getMIBreakpointFromOutput(notifyOutput);
							if (bpt != null)
								bs.targetBreakpointCreated(bpt);
						} else if (BREAKPOINT_DELETED.equals(asyncClass)) {
							String id = getMIBreakpointIdFromOutput(notifyOutput);
							if (!id.isEmpty())
								bs.targetBreakpointDeleted(id);
						} else if (BREAKPOINT_MODIFIED.equals(asyncClass)) {
							MIBreakpoint bpt = getMIBreakpointFromOutput(notifyOutput);
							if (bpt != null)
								bs.targetBreakpointModified(bpt);
						}
					}
				}
			}
		}
	}

	private IMICommandControl getCommandControl() {
		return fConnection;
	}

	private MIBreakpoint getMIBreakpointFromOutput(MINotifyAsyncOutput notifyOutput) {
		MIBreakpoint bpt = null;
		MIResult[] results = notifyOutput.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue val = results[i].getMIValue();
			if (var.equals("bkpt")) { //$NON-NLS-1$
				if (val instanceof MITuple) {
					bpt = createMIBreakpoint((MITuple) val);
				}
			}
		}
		return bpt;
	}

	private String getMIBreakpointIdFromOutput(MINotifyAsyncOutput notifyOutput) {
		MIResult[] results = notifyOutput.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue val = results[i].getMIValue();
			if (var.equals("id") && val instanceof MIConst) { //$NON-NLS-1$
				try {
					return ((MIConst) val).getCString().trim();
				} catch (NumberFormatException e) {
					GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Invalid breakpoint id")); //$NON-NLS-1$
				}
			}
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	protected void addBreakpoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes,
			final DataRequestMonitor<IBreakpointDMContext> finalRm) {
		final MIBreakpointsSynchronizer bs = getServicesTracker().getService(MIBreakpointsSynchronizer.class);
		if (bs != null) {
			// Skip the breakpoints set from the console or from outside of Eclipse
			// because they are already installed on the target.
			bs.getTargetBreakpoint(context, attributes, new DataRequestMonitor<MIBreakpoint>(getExecutor(), finalRm) {
				@Override
				@ConfinedToDsfExecutor("fExecutor")
				protected void handleSuccess() {
					MIBreakpoint miBpt = getData();
					if (miBpt != null) {
						bs.removeCreatedTargetBreakpoint(context, miBpt);
						MIBreakpointDMData newBreakpoint = createMIBreakpointDMData(miBpt);
						getBreakpointMap(context).put(newBreakpoint.getNumber(), newBreakpoint);
						IBreakpointDMContext dmc = new MIBreakpointDMContext(GDBBreakpoints_7_4.this,
								new IDMContext[] { context }, newBreakpoint.getNumber());
						finalRm.setData(dmc);
						getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());
						finalRm.done();
					} else {
						GDBBreakpoints_7_4.super.addBreakpoint(context, attributes, finalRm);
					}
				}
			});
		} else {
			super.addBreakpoint(context, attributes, finalRm);
		}
	}

	@Override
	protected void addTracepoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes,
			final DataRequestMonitor<IBreakpointDMContext> drm) {
		final MIBreakpointsSynchronizer bs = getServicesTracker().getService(MIBreakpointsSynchronizer.class);
		if (bs != null) {
			// Skip the breakpoints set from the console or from outside of Eclipse
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
						IBreakpointDMContext dmc = new MIBreakpointDMContext(GDBBreakpoints_7_4.this,
								new IDMContext[] { context }, newBreakpoint.getNumber());
						drm.setData(dmc);
						getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());
						drm.done();
					} else {
						GDBBreakpoints_7_4.super.addTracepoint(context, attributes, drm);
					}
				}
			});
		} else {
			super.addTracepoint(context, attributes, drm);
		}
	}

	@Override
	protected void addWatchpoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes,
			final DataRequestMonitor<IBreakpointDMContext> drm) {
		final MIBreakpointsSynchronizer bs = getServicesTracker().getService(MIBreakpointsSynchronizer.class);
		if (bs != null) {
			// Skip the breakpoints set from the console or from outside of Eclipse
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
						IBreakpointDMContext dmc = new MIBreakpointDMContext(GDBBreakpoints_7_4.this,
								new IDMContext[] { context }, newBreakpoint.getNumber());
						drm.setData(dmc);
						getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());
						drm.done();
					} else {
						GDBBreakpoints_7_4.super.addWatchpoint(context, attributes, drm);
					}
				}
			});
		} else {
			super.addWatchpoint(context, attributes, drm);
		}
	}

	@Override
	protected void deleteBreakpointFromTarget(IBreakpointsTargetDMContext context, String reference,
			RequestMonitor finalRm) {
		MIBreakpointsSynchronizer bs = getServicesTracker().getService(MIBreakpointsSynchronizer.class);
		if (bs != null) {
			// Do nothing if the breakpoint is deleted from the console.
			if (bs.isTargetBreakpointDeleted(context, reference, true)) {
				finalRm.done();
				return;
			}
		}
		super.deleteBreakpointFromTarget(context, reference, finalRm);
	}

	@Override
	public String adjustDebuggerPath(String originalPath) {
		// No adjustment is required
		return originalPath;
	}
}
