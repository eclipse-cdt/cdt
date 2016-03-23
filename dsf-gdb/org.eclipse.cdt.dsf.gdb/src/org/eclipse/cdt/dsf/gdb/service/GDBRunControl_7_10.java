/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoRecordInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/** @since 5.0 */
public class GDBRunControl_7_10 extends GDBRunControl_7_6 implements IReverseRunControl2 {

	private IMICommandControl fCommandControl;
	private CommandFactory fCommandFactory;

	private ReverseDebugMethod fReverseTraceMethod; // default: no trace

	public GDBRunControl_7_10(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(
			new ImmediateRequestMonitor(requestMonitor) {
				@Override
				public void handleSuccess() {
					doInitialize(requestMonitor);
				}
			});
	}

	private void doInitialize(RequestMonitor requestMonitor) {

		fCommandControl = getServicesTracker().getService(IMICommandControl.class);
		fCommandFactory = fCommandControl.getCommandFactory();
		fReverseTraceMethod = ReverseDebugMethod.OFF;

		if (fCommandControl == null) {
			requestMonitor.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Service is not available")); //$NON-NLS-1$
			return;
		}

		// Don't register as an event listener because our base class does it already

		register(new String[]{ IReverseRunControl2.class.getName() },
			 	 new Hashtable<String,String>());

		requestMonitor.done();
	}

	@Override
	public void getReverseTraceMethod(ICommandControlDMContext context, DataRequestMonitor<ReverseDebugMethod> rm) {
		rm.setData(fReverseTraceMethod);
		rm.done();
	}

	@Override
	public void enableReverseMode(final ICommandControlDMContext context,final ReverseDebugMethod traceMethod, final RequestMonitor rm) {
		if (!getReverseSupported()) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Reverse mode is not supported.", null)); //$NON-NLS-1$
			return;
		}

		if (fReverseTraceMethod == traceMethod) {
			rm.done();
			return;
		}

		if (fReverseTraceMethod == ReverseDebugMethod.OFF || traceMethod == ReverseDebugMethod.OFF) {
			getConnection().queueCommand(
				fCommandFactory.createCLIRecord(context, traceMethod),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
					@Override
					public void handleSuccess() {
						boolean enabled = false;
						fReverseTraceMethod = traceMethod;
						if (fReverseTraceMethod != ReverseDebugMethod.OFF) {
							enabled = true;
						}
						setReverseModeEnabled(enabled );
						rm.done();
					}
					@Override
					public void handleFailure() {
						rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Trace method could not be selected", null)); //$NON-NLS-1$
					}
				});
			return;
		}

		getConnection().queueCommand(
			fCommandFactory.createCLIRecord(context, ReverseDebugMethod.OFF),
			new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
				@Override
				public void handleSuccess() {
					setReverseModeEnabled(false);
					getConnection().queueCommand(
						fCommandFactory.createCLIRecord(context, traceMethod),
						new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
							@Override
							public void handleSuccess() {
								fReverseTraceMethod = traceMethod;
								setReverseModeEnabled(true);
								rm.done();
							}
							@Override
							public void handleFailure() {
								rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Trace method could not be selected", null)); //$NON-NLS-1$
								setReverseModeEnabled(false);
								fReverseTraceMethod = ReverseDebugMethod.OFF;
								rm.done();
							}
						});
				}
			});
	}


	@Override
	public void eventReceived(Object output) {
		if (output instanceof MIOutput) {
			MIOOBRecord[] records = ((MIOutput)output).getMIOOBRecords();
			for (MIOOBRecord r : records) {
				if (r instanceof MINotifyAsyncOutput) {
					MINotifyAsyncOutput notifyOutput = (MINotifyAsyncOutput)r;
					String asyncClass = notifyOutput.getAsyncClass();
					// These events have been added with GDB 7.6
					if ("record-started".equals(asyncClass) || //$NON-NLS-1$
						"record-stopped".equals(asyncClass)) {	 //$NON-NLS-1$
						if ("record-stopped".equals(asyncClass)) { //$NON-NLS-1$
							fReverseTraceMethod = ReverseDebugMethod.OFF;
							setReverseModeEnabled(false);
						} else {
							getConnection().queueCommand(
								fCommandFactory.createCLIInfoRecord(getConnection().getContext()),
								new DataRequestMonitor<CLIInfoRecordInfo>(getExecutor(), null) {
									@Override
									public void handleCompleted() {
										if (isSuccess()) {
											fReverseTraceMethod = getData().getReverseMethod();
										} else {
											// Use a default value in case of error
											fReverseTraceMethod = ReverseDebugMethod.SOFTWARE;
										}
										setReverseModeEnabled(true);
									}
								});
						}
					}
				}
			}
		}
	}
}
