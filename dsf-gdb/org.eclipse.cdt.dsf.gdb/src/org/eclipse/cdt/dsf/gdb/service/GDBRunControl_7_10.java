/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
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
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
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

		register(new String[] { IReverseRunControl2.class.getName() }, new Hashtable<String, String>());

		requestMonitor.done();
	}

	@Override
	public void setReverseModeEnabled(boolean enabled) {
		super.setReverseModeEnabled(enabled);
		if (!enabled) {
			// Keep the disabled state in sync with the trace method
			// This is needed e.g. to restart reverse mode during
			// a process restart
			fReverseTraceMethod = ReverseDebugMethod.OFF;
		}
	}

	/** @since 5.1 */
	protected void setReverseTraceMethod(ReverseDebugMethod traceMethod) {
		if (fReverseTraceMethod != traceMethod) {
			boolean enabled = false;
			fReverseTraceMethod = traceMethod;
			if (fReverseTraceMethod != ReverseDebugMethod.OFF) {
				enabled = true;
			}
			setReverseModeEnabled(enabled);
		}
	}

	@Override
	public void getReverseTraceMethod(ICommandControlDMContext context, DataRequestMonitor<ReverseDebugMethod> rm) {
		rm.setData(fReverseTraceMethod);
		rm.done();
	}

	@Override
	public void enableReverseMode(final ICommandControlDMContext context, final ReverseDebugMethod traceMethod,
			final RequestMonitor rm) {
		if (!getReverseSupported()) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Reverse mode is not supported.", //$NON-NLS-1$
					null));
			return;
		}

		if (fReverseTraceMethod == traceMethod) {
			rm.done();
			return;
		}

		if (fReverseTraceMethod == ReverseDebugMethod.OFF || traceMethod == ReverseDebugMethod.OFF) {
			getConnection().queueCommand(fCommandFactory.createCLIRecord(context, traceMethod),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
						@Override
						public void handleSuccess() {
							boolean enabled = false;
							fReverseTraceMethod = traceMethod;
							if (fReverseTraceMethod != ReverseDebugMethod.OFF) {
								enabled = true;
							}
							setReverseModeEnabled(enabled);
							rm.done();
						}

						@Override
						public void handleFailure() {
							rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
									"Trace method could not be selected", null)); //$NON-NLS-1$
						}
					});
			return;
		}

		getConnection().queueCommand(fCommandFactory.createCLIRecord(context, ReverseDebugMethod.OFF),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
					@Override
					public void handleSuccess() {
						setReverseModeEnabled(false);
						getConnection().queueCommand(fCommandFactory.createCLIRecord(context, traceMethod),
								new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
									@Override
									public void handleSuccess() {
										fReverseTraceMethod = traceMethod;
										setReverseModeEnabled(true);
										rm.done();
									}

									@Override
									public void handleFailure() {
										rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
												"Trace method could not be selected", null)); //$NON-NLS-1$
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
			MIOOBRecord[] records = ((MIOutput) output).getMIOOBRecords();
			for (MIOOBRecord r : records) {
				if (r instanceof MINotifyAsyncOutput) {
					MINotifyAsyncOutput notifyOutput = (MINotifyAsyncOutput) r;
					String asyncClass = notifyOutput.getAsyncClass();
					// These events have been added with GDB 7.6
					if ("record-started".equals(asyncClass) || //$NON-NLS-1$
							"record-stopped".equals(asyncClass)) { //$NON-NLS-1$
						if ("record-stopped".equals(asyncClass)) { //$NON-NLS-1$
							setReverseTraceMethod(ReverseDebugMethod.OFF);
						} else {
							// With GDB 7.12, we are provided with the type of record
							// that was started.
							ReverseDebugMethod newMethod = getTraceMethodFromOutput(notifyOutput);
							if (newMethod != null) {
								setReverseTraceMethod(newMethod);
							} else {
								// Don't know what the new method is.  Let's ask GDB
								getConnection().queueCommand(
										fCommandFactory.createCLIInfoRecord(getConnection().getContext()),
										new DataRequestMonitor<CLIInfoRecordInfo>(getExecutor(), null) {
											@Override
											public void handleCompleted() {
												if (isSuccess()) {
													setReverseTraceMethod(getData().getReverseMethod());
												} else {
													// Use a default value in case of error
													setReverseTraceMethod(ReverseDebugMethod.SOFTWARE);
												}
											}
										});
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @return The ReverseDebugMethod as specified by the =record-started event.
	 *         Returns null if the event does provide that information (GDB < 7.12)
	 */
	private ReverseDebugMethod getTraceMethodFromOutput(MINotifyAsyncOutput notifyOutput) {
		// With GDB 7.12, we are provided with the type of record
		// that was started.
		//   =record-started,thread-group="i1",method="btrace",format="bts"
		//   =record-started,thread-group="i1",method="btrace",format="pt"
		//   =record-started,thread-group="i1",method="full"

		String methodStr = ""; //$NON-NLS-1$
		String formatStr = ""; //$NON-NLS-1$
		MIResult[] results = notifyOutput.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue val = results[i].getMIValue();
			if (var.equals("method")) { //$NON-NLS-1$
				if (val instanceof MIConst) {
					methodStr = ((MIConst) val).getString();
				}
			} else if (var.equals("format")) { //$NON-NLS-1$
				if (val instanceof MIConst) {
					formatStr = ((MIConst) val).getString();
				}
			}
		}

		if (methodStr.equals("full")) { //$NON-NLS-1$
			assert formatStr.isEmpty() : "Unexpected format string for full method in =record-started: " + formatStr; //$NON-NLS-1$
			return ReverseDebugMethod.SOFTWARE;
		}

		if (methodStr.equals("btrace")) { //$NON-NLS-1$
			if (formatStr.equals("bts")) { //$NON-NLS-1$
				return ReverseDebugMethod.BRANCH_TRACE;
			} else if (formatStr.equals("pt")) { //$NON-NLS-1$
				return ReverseDebugMethod.PROCESSOR_TRACE;
			} else {
				assert false : "Unexpected format string for bts method in =record-started: " + formatStr; //$NON-NLS-1$
			}
		}

		// No "method" field matching what we expect, so this must be GDB that does not provide that field
		assert methodStr.isEmpty() : "Unexpected trace method in =record-started: " + methodStr; //$NON-NLS-1$
		assert formatStr.isEmpty() : "Unexpected format string in =record-started: " + formatStr; //$NON-NLS-1$

		return null;
	}
}
