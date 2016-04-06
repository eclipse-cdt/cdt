/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.GDBRunControl_7_0.GdbReverseModeChangedDMEvent;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/** @since 5.0 */
public class GDBRunControl_7_11_NS extends GDBRunControl_7_2_NS implements IReverseRunControl2 {

	private IMICommandControl fCommandControl;
	private CommandFactory fCommandFactory;

	private boolean fReverseModeEnabled;

	private ReverseDebugMethod fReverseTraceMethod; // default: no trace

	public GDBRunControl_7_11_NS(DsfSession session) {
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

		// No need to worry about post-mortem debugging here, as we can 
		// never instantiate this service in this case.

		// Don't register as an event listener because our base class does it already

		register(new String[]{ IReverseRunControl.class.getName(),
							   IReverseRunControl2.class.getName() },
			 	 new Hashtable<String,String>());

		requestMonitor.done();
	}

    public void setReverseModeEnabled(boolean enabled) {
    	if (fReverseModeEnabled != enabled) {
    		fReverseModeEnabled = enabled;
    		getSession().dispatchEvent(new GdbReverseModeChangedDMEvent(fCommandControl.getContext(), fReverseModeEnabled), 
    				                   getProperties());
    	}
    }
    
	@Override
	public void canEnableReverseMode(ICommandControlDMContext context, DataRequestMonitor<Boolean> rm) {
		// Reverse debugging is now supported in non-stop mode for branch trace.
		// Let's enable it all the time and let GDB return a failure if the user tries
		// to enable software reverse.  This is more user-friendly as the user will understand
		// why reverse does not work.
		// Also, it will allow software reverse to work automatically with non-stop if GDB
		// ever supports it.
		rm.done(true);
	}

	@Override
    public void isReverseModeEnabled(ICommandControlDMContext context, DataRequestMonitor<Boolean> rm) {
		rm.done(fReverseModeEnabled);
	}

	@Override
    public void enableReverseMode(ICommandControlDMContext context, final boolean enable, final RequestMonitor rm) {    	
    	if (fReverseModeEnabled == enable) {
    		rm.done();
    		return;
    	}
    	
    	fCommandControl.queueCommand(
    		fCommandFactory.createCLIRecord(context, enable),
    		new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    			@Override
    			public void handleSuccess() {
    				setReverseModeEnabled(enable);
    				rm.done();
    			}
    		});
	}

	@Override
	public void enableReverseMode(final ICommandControlDMContext context,final ReverseDebugMethod traceMethod, final RequestMonitor rm) {
		if (fReverseTraceMethod == traceMethod) {
			rm.done();
			return;
		}

		if (fReverseTraceMethod == ReverseDebugMethod.OFF || traceMethod == ReverseDebugMethod.OFF) {
			fCommandControl.queueCommand(
				fCommandFactory.createCLIRecord(context, traceMethod),
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
				});
			return;
		}

		// Here, we know reverse is on, so first turn it off, then turn it back on with the new mode
		fCommandControl.queueCommand(
			fCommandFactory.createCLIRecord(context, ReverseDebugMethod.OFF),
			new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
				@Override
				public void handleSuccess() {
					setReverseModeEnabled(false);
					fReverseTraceMethod = ReverseDebugMethod.OFF;

					fCommandControl.queueCommand(
						fCommandFactory.createCLIRecord(context, traceMethod),
						new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
							@Override
							public void handleSuccess() {
								fReverseTraceMethod = traceMethod;
								setReverseModeEnabled(true);
								rm.done();
							}
						});
				}
			});
	}
	
	@Override
	public void getReverseTraceMethod(ICommandControlDMContext context, DataRequestMonitor<ReverseDebugMethod> rm) {
		rm.done(fReverseTraceMethod);
	}

	@Override
	public void canReverseResume(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		if (!getRunControlOperationsEnabled()) {
			rm.done(false);
			return;
		}

		rm.done(fReverseModeEnabled && doCanResume(context));
	}

	@Override
	public void canReverseStep(IExecutionDMContext context, StepType stepType, DataRequestMonitor<Boolean> rm) {
		if (!getRunControlOperationsEnabled()) {
			rm.done(false);
			return;
		}

		// If it's a thread, just look it up
		if (context instanceof IMIExecutionDMContext) {
			if (stepType == StepType.STEP_RETURN) {	
				// Check the stuff we know first, before going to the backend for 
				// stack info
				if (!fReverseModeEnabled || !doCanResume(context)) {
					rm.done(false);
					return;    			
				}

				// A step return will always be done in the top stack frame.
				// If the top stack frame is the only stack frame, it does not make sense
				// to do a step return since GDB will reject it.
				MIStack stackService = getServicesTracker().getService(MIStack.class);
				if (stackService != null) {
					// Check that the stack is at least two deep.
					stackService.getStackDepth(context, 2, new DataRequestMonitor<Integer>(getExecutor(), rm) {
						@Override
						public void handleCompleted() {
							if (isSuccess() && getData() == 1) {
								rm.done(false);
							} else {
								canReverseResume(context, rm);
							}
						}
					});
					return;
				}
			}
			canReverseResume(context, rm);
			return;
		}

		// If it's a container, then we don't want to step it
		rm.done(false);
	}

	@Override
	public boolean isReverseStepping(IExecutionDMContext context) {
		// If it's a thread, just look it up
		if (context instanceof IMIExecutionDMContext) {
			MIThreadRunState threadState = fThreadRunStates.get(context);
			return (threadState == null) ? false : !isTerminated() && threadState.fReverseStepping;
		}

		// Default case
		return false;
	}

	@Override
	public void reverseResume(final IExecutionDMContext context, final RequestMonitor rm) {
		
		assert context != null;

		if (fReverseModeEnabled && doCanResume(context)) {

			// Thread case
			IMIExecutionDMContext thread = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
			if (thread != null) {
				doReverseResume(thread, rm);
				return;
			}

			// Container case
			IMIContainerDMContext container = DMContexts.getAncestorOfType(context, IMIContainerDMContext.class);
			if (container != null) {
				doReverseResume(container, rm);
				return;
			}

			// Default case
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Invalid context type.", null)); //$NON-NLS-1$
		}		
	}

	@Override
	public void reverseStep(final IExecutionDMContext context, StepType stepType, final RequestMonitor rm) {
		assert context != null;

		IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
				"Given context: " + context + " is not an MI execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		if (!fReverseModeEnabled || !doCanResume(dmc)) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Cannot resume context", null)); //$NON-NLS-1$
			return;
		}

		final MIThreadRunState threadState = fThreadRunStates.get(context);
		if (threadState == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
				"Given context: " + context + " can't be found.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		ICommand<MIInfo> cmd = null;
		switch (stepType) {
		case STEP_INTO:
			cmd = fCommandFactory.createMIExecReverseStep(dmc);
			break;
		case STEP_OVER:
			cmd = fCommandFactory.createMIExecReverseNext(dmc);
			break;
		case STEP_RETURN:
			// The -exec-finish command operates on the selected stack frame, but here we always
			// want it to operate on the stop stack frame. So we manually create a top-frame
			// context to use with the MI command.
			// We get a local instance of the stack service because the stack service can be shut
			// down before the run control service is shut down. So it is possible for the
			// getService() request below to return null.
			MIStack stackService = getServicesTracker().getService(MIStack.class);
			if (stackService != null) {
				IFrameDMContext topFrameDmc = stackService.createFrameDMContext(dmc, 0);
				cmd = fCommandFactory.createMIExecUncall(topFrameDmc);
			} else {
				rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
						"Cannot create context for command, stack service not available.", null)); //$NON-NLS-1$
				return;
			}
			break;
		case INSTRUCTION_STEP_INTO:
			cmd = fCommandFactory.createMIExecReverseStepInstruction(dmc);
			break;
		case INSTRUCTION_STEP_OVER:
			cmd = fCommandFactory.createMIExecReverseNextInstruction(dmc);
			break;
		default:
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					INTERNAL_ERROR, "Given step type not supported", null)); //$NON-NLS-1$
			return;
		}
		
		threadState.fResumePending = true;
		threadState.fReverseStepping = true;
		fCommandControl.queueCommand(cmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
			@Override
			public void handleFailure() {
				threadState.fResumePending = false;
				threadState.fReverseStepping = false;

				super.handleFailure();
			}   
		});
	}

	private void doReverseResume(IMIExecutionDMContext context, final RequestMonitor rm) {
		if (!doCanResume(context)) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
				"Given context: " + context + ", is already running.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		final MIThreadRunState threadState = fThreadRunStates.get(context);
		if (threadState == null) {
            rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
                "Given context: " + context + " can't be found.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		
		threadState.fResumePending = true;
		fCommandControl.queueCommand(fCommandFactory.createMIExecReverseContinue(context), new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
			@Override
			protected void handleFailure() {
				threadState.fResumePending = false;
				super.handleFailure();
			}
		});
	}

	private void doReverseResume(IMIContainerDMContext context, final RequestMonitor rm) {
		if (!doCanResume(context)) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
				"Given context: " + context + ", is already running.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		fCommandControl.queueCommand(fCommandFactory.createMIExecReverseContinue(context), new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}
	
//	public void eventReceived(Object output) {
//		if (output instanceof MIOutput) {
//			MIOOBRecord[] records = ((MIOutput)output).getMIOOBRecords();
//			for (MIOOBRecord r : records) {
//				if (r instanceof MINotifyAsyncOutput) {
//					MINotifyAsyncOutput notifyOutput = (MINotifyAsyncOutput)r;
//					String asyncClass = notifyOutput.getAsyncClass();
//					// These events have been added with GDB 7.6
//					if ("record-started".equals(asyncClass) || //$NON-NLS-1$
//						"record-stopped".equals(asyncClass)) {	 //$NON-NLS-1$
//						if ("record-stopped".equals(asyncClass)) { //$NON-NLS-1$
//							fReverseTraceMethod = ReverseDebugMethod.OFF;
//							setReverseModeEnabled(false);
//						} else {
//							fCommandControl.queueCommand(
//								fCommandFactory.createCLIInfoRecord(fCommandControl.getContext()),
//								new DataRequestMonitor<CLIInfoRecordInfo>(getExecutor(), null) {
//									@Override
//									public void handleCompleted() {
//										if (isSuccess()) {
//											fReverseTraceMethod = getData().getReverseMethod();
//										} else {
//											// Use a default value in case of error
//											fReverseTraceMethod = ReverseDebugMethod.SOFTWARE;
//										}
//										setReverseModeEnabled(true);
//									}
//								});
//						}
//					}
//				}
//			}
//		}
//	}
}
