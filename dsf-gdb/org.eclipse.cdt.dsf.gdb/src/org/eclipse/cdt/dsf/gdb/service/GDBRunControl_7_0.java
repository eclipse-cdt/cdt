/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson		      - Modified for additional functionality
 *     Ericsson           - Version 7.0
 *     Nokia              - create and use backend service. 
 *     Ericsson           - Added IReverseControl support
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;


import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIInferiorExitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class GDBRunControl_7_0 extends MIRunControl implements IReverseRunControl {
	
	private static class RunToLineActiveOperation {
		private IMIExecutionDMContext fThreadContext;
		private int fBpId;
		private String fFileLocation;
		private String fAddrLocation;
		private boolean fSkipBreakpoints;
		
		public RunToLineActiveOperation(IMIExecutionDMContext threadContext,
				int bpId, String fileLoc, String addr, boolean skipBreakpoints) {
			fThreadContext = threadContext;
			fBpId = bpId;
			fFileLocation = fileLoc;
			fAddrLocation = addr;
			fSkipBreakpoints = skipBreakpoints;
		}
		
		public IMIExecutionDMContext getThreadContext() { return fThreadContext; }
		public int getBreakointId() { return fBpId; }
		public String getFileLocation() { return fFileLocation; }
		public String getAddrLocation() { return fAddrLocation; }
		public boolean shouldSkipBreakpoints() { return fSkipBreakpoints; }
	}

    private IGDBBackend fGdb;
	private IMIProcesses fProcService;
	private CommandFactory fCommandFactory;

	private boolean fReverseSupported = true;
	private boolean fReverseStepping = false;
	private boolean fReverseModeEnabled = false;
	
	/**
	 * This variable allows us to know if run control operation
	 * should be enabled or disabled.  Run control operations are
	 * always enabled except when dealing with post-mortem debug
	 * session, or when visualizing tracepoints.
	 */
	private boolean fRunControlOperationsEnabled = true;

	private RunToLineActiveOperation fRunToLineActiveOperation = null;

    public GDBRunControl_7_0(DsfSession session) {
        super(session);
    }
    
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
            new RequestMonitor(getExecutor(), requestMonitor) { 
                @Override
                public void handleSuccess() {
                    doInitialize(requestMonitor);
                }});
    }

    private void doInitialize(final RequestMonitor requestMonitor) {
    	
        fGdb = getServicesTracker().getService(IGDBBackend.class);
        fProcService = getServicesTracker().getService(IMIProcesses.class);
        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		if (fGdb.getSessionType() == SessionType.CORE) {
			// No execution for core files, so no support for reverse
			fRunControlOperationsEnabled = false;
			fReverseSupported = false;
		}

        register(new String[]{IRunControl.class.getName(),
        					  IRunControl2.class.getName(),
        					  IMIRunControl.class.getName(),
        					  MIRunControl.class.getName(),
        					  IReverseRunControl.class.getName()}, 
        		 new Hashtable<String,String>());
        requestMonitor.done();
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        unregister();
        super.shutdown(requestMonitor);
    }
    
    @Override
	public IMIExecutionDMContext createMIExecutionContext(IContainerDMContext container, int threadId) {
        IProcessDMContext procDmc = DMContexts.getAncestorOfType(container, IProcessDMContext.class);
        
        IThreadDMContext threadDmc = null;
        if (procDmc != null) {
        	// For now, reuse the threadId as the OSThreadId
        	threadDmc = fProcService.createThreadContext(procDmc, Integer.toString(threadId));
        }

        return fProcService.createExecutionContext(container, threadDmc, Integer.toString(threadId));
    }

    @Override
    public void suspend(IExecutionDMContext context, final RequestMonitor rm){
        canSuspend(
            context, 
            new DataRequestMonitor<Boolean>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    if (getData()) {
                        fGdb.interruptAndWait(IGDBBackend.INTERRUPT_TIMEOUT_DEFAULT, rm);
                    } else {
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Context cannot be suspended.", null)); //$NON-NLS-1$
                        rm.done();
                    }
                }
            });
    }

	@Override
    public void getExecutionContexts(IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		fProcService.getProcessesBeingDebugged(
				containerDmc,
				new DataRequestMonitor<IDMContext[]>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						if (getData() instanceof IExecutionDMContext[]) {
							IExecutionDMContext[] execDmcs = (IExecutionDMContext[])getData();
							rm.setData(execDmcs);
						} else {
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid contexts", null)); //$NON-NLS-1$
						}
						rm.done();
					}
				});
    }

	/**
	 * @since 2.0
	 */
	@Override
	public void canResume(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		if (fRunControlOperationsEnabled == false) {
			rm.setData(false);
			rm.done();
			return;
		}
		super.canResume(context, rm);
	}

	/**
	 * @since 2.0
	 */
	@Override
	public void canSuspend(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		if (fRunControlOperationsEnabled == false) {
			rm.setData(false);
			rm.done();
			return;
		}
		super.canSuspend(context, rm);
	}

	/**
	 * @since 2.0
	 */
	@Override
	public void canStep(final IExecutionDMContext context, StepType stepType, final DataRequestMonitor<Boolean> rm) {
		if (fRunControlOperationsEnabled == false) {
			rm.setData(false);
			rm.done();
			return;
		}

    	if (context instanceof IContainerDMContext) {
    		rm.setData(false);
    		rm.done();
    		return;
    	}
    	
    	if (stepType == StepType.STEP_RETURN) {
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
            				rm.setData(false);
            				rm.done();
            			} else {
            	    		canResume(context, rm);
            			}
            		}
            	});
            	return;
            }
    	}
    	
    	canResume(context, rm);
    }
    
    /** @since 2.0 */
	public void canReverseResume(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		rm.setData(fReverseModeEnabled && doCanResume(context));
		rm.done();
	}

    /** @since 2.0 */
	public void canReverseStep(final IExecutionDMContext context, StepType stepType, final DataRequestMonitor<Boolean> rm) {
	   	if (context instanceof IContainerDMContext) {
    		rm.setData(false);
    		rm.done();
    		return;
    	}

    	if (stepType == StepType.STEP_RETURN) {
    		
    		// Check the stuff we know first, before going to the backend for 
    		// stack info
    		if (!fReverseModeEnabled || !doCanResume(context)) {
    	   		rm.setData(false);
        		rm.done();
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
            				rm.setData(false);
            				rm.done();
            			} else {
            				canReverseResume(context, rm);
            			}
            		}
            	});
            	return;
            }
    	}

	   	canReverseResume(context, rm);		
	}

    /** @since 2.0 */
	public boolean isReverseStepping(IExecutionDMContext context) {
		return !isTerminated() && fReverseStepping;
	}

    /** @since 2.0 */
	public void reverseResume(final IExecutionDMContext context, final RequestMonitor rm) {
		if (fReverseModeEnabled && doCanResume(context)) {
			ICommand<MIInfo> cmd = null;
            if (context instanceof IContainerDMContext) {
            	cmd = fCommandFactory.createMIExecReverseContinue(context);
            } else {
        		IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
    			if (dmc == null){
    	            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
    	            rm.done();
    	            return;
    			}
            	cmd = fCommandFactory.createMIExecReverseContinue(dmc);
            }

            setResumePending(true);
            // Cygwin GDB will accept commands and execute them after the step
            // which is not what we want, so mark the target as unavailable
            // as soon as we send a resume command.
            getCache().setContextAvailable(context, false);

            getConnection().queueCommand(cmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm)  {
            	@Override
                public void handleFailure() {
           			setResumePending(false);
           			getCache().setContextAvailable(context, true);
            	}
            });
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Given context: " + context + ", is already running or reverse not enabled.", null)); //$NON-NLS-1$ //$NON-NLS-2$
            rm.done();
        }
		
	}

    /** @since 2.0 */
	public void reverseStep(final IExecutionDMContext context, StepType stepType, final RequestMonitor rm) {
    	assert context != null;

    	IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null){
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
            rm.done();
            return;
		}
    	
    	if (!fReverseModeEnabled || !doCanResume(context)) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Cannot resume context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        ICommand<MIInfo> cmd = null;
        switch(stepType) {
            case STEP_INTO:
            	cmd = fCommandFactory.createMIExecReverseStep(dmc, 1);
                break;
            case STEP_OVER:
                cmd = fCommandFactory.createMIExecReverseNext(dmc, 1);
                break;
            case STEP_RETURN:
                // The -exec-finish command operates on the selected stack frame, but here we always
                // want it to operate on the top stack frame.  So we manually create a top-frame
                // context to use with the MI command.
                // We get a local instance of the stack service because the stack service can be shut
                // down before the run control service is shut down.  So it is possible for the
                // getService() request below to return null.
                MIStack stackService = getServicesTracker().getService(MIStack.class);
                if (stackService != null) {
                    IFrameDMContext topFrameDmc = stackService.createFrameDMContext(dmc, 0);
                    cmd = fCommandFactory.createMIExecUncall(topFrameDmc);
                } else {
                    rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Cannot create context for command, stack service not available.", null)); //$NON-NLS-1$
                    rm.done();
                    return;
                }
                break;
            case INSTRUCTION_STEP_INTO:
                cmd = fCommandFactory.createMIExecReverseStepInstruction(dmc, 1);
                break;
            case INSTRUCTION_STEP_OVER:
                cmd = fCommandFactory.createMIExecReverseNextInstruction(dmc, 1);
                break;
            default:
                rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Given step type not supported", null)); //$NON-NLS-1$
                rm.done();
                return;
        }
        
    	setResumePending(true);
        fReverseStepping = true;
        getCache().setContextAvailable(context, false);

        getConnection().queueCommand(cmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm)  {
        	@Override
        	public void handleFailure() {
        		setResumePending(false);
        		fReverseStepping = false;
        		getCache().setContextAvailable(context, true);
        	}
        });
	}

    /** @since 2.0 */
	public void canEnableReverseMode(ICommandControlDMContext context, DataRequestMonitor<Boolean> rm) {
		rm.setData(fReverseSupported);
		rm.done();
	}

    /** @since 2.0 */
    public void isReverseModeEnabled(ICommandControlDMContext context, DataRequestMonitor<Boolean> rm) {
		rm.setData(fReverseModeEnabled);
		rm.done();
	}

    /** @since 2.0 */
    public void enableReverseMode(ICommandControlDMContext context, final boolean enable, final RequestMonitor rm) {
    	if (!fReverseSupported) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Reverse mode is not supported.", null)); //$NON-NLS-1$
    		rm.done();
    		return;
    	}
    	
    	if (fReverseModeEnabled == enable) {
    		rm.done();
    		return;
    	}
    	
    	getConnection().queueCommand(
    			fCommandFactory.createCLIRecord(context, enable),
    			new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    				@Override
    				public void handleSuccess() {
    					setReverseModeEnabled(enable);
    					rm.done();
    				}
    			});
	}
    	
	/** @since 3.0 */
	@Override
	public void runToLocation(final IExecutionDMContext context, final String location, final boolean skipBreakpoints, final RequestMonitor rm){
	    
    	assert context != null;

    	final IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null){
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
            rm.done();
            return;
		}

        if (doCanResume(dmc)) {
        	IBreakpointsTargetDMContext bpDmc = DMContexts.getAncestorOfType(context, IBreakpointsTargetDMContext.class);
        	getConnection().queueCommand(
        			fCommandFactory.createMIBreakInsert(bpDmc, true, false, null, 0, 
        					          location, dmc.getThreadId()), 
        		    new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), rm) {
        				@Override
        				public void handleSuccess() {
        					// We must set are RunToLineActiveOperation *before* we do the resume
        					// or else we may get the stopped event, before we have set this variable.
           					int bpId = getData().getMIBreakpoints()[0].getNumber();
           					String addr = getData().getMIBreakpoints()[0].getAddress();
        		        	fRunToLineActiveOperation = new RunToLineActiveOperation(dmc, bpId, location, addr, skipBreakpoints);

        					resume(dmc, new RequestMonitor(getExecutor(), rm) {
                				@Override
                				public void handleFailure() {
                		    		IBreakpointsTargetDMContext bpDmc = DMContexts.getAncestorOfType(fRunToLineActiveOperation.getThreadContext(),
                		    				IBreakpointsTargetDMContext.class);
                		    		int bpId = fRunToLineActiveOperation.getBreakointId();

                		    		getConnection().queueCommand(fCommandFactory.createMIBreakDelete(bpDmc, new int[] {bpId}),
                		    				new DataRequestMonitor<MIInfo>(getExecutor(), null));
                		    		fRunToLineActiveOperation = null;

                		    		super.handleFailure();
                		    	}
        					});
        				}
        			});
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
            		"Cannot resume given DMC.", null)); //$NON-NLS-1$
            rm.done();
        }
	}

	/** @since 3.0 */
	@Override
	public void resumeAtLocation(IExecutionDMContext context, String location, RequestMonitor rm) {
		assert context != null;

		final IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null){
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Given context: " + context + " is not an thread execution context.", null)); //$NON-NLS-1$  //$NON-NLS-2$
			rm.done();
			return;
		}

		if (doCanResume(dmc)) {
			setResumePending(true);
			getCache().setContextAvailable(dmc, false);
			getConnection().queueCommand(
					fCommandFactory.createMIExecJump(dmc, location),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
						@Override
						protected void handleFailure() {
							setResumePending(false);
							getCache().setContextAvailable(dmc, true);

							super.handleFailure();
						}
					});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
					"Cannot resume given DMC.", null)); //$NON-NLS-1$
					rm.done();
		}		
	}
	
    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     * 
     * @since 2.0
     */
    @DsfServiceEventHandler
    public void eventDispatched(MIInferiorExitEvent e) {
    	if (fRunToLineActiveOperation != null) {
    		IBreakpointsTargetDMContext bpDmc = DMContexts.getAncestorOfType(fRunToLineActiveOperation.getThreadContext(),
    				IBreakpointsTargetDMContext.class);
    		int bpId = fRunToLineActiveOperation.getBreakointId();

    		getConnection().queueCommand(fCommandFactory.createMIBreakDelete(bpDmc, new int[] {bpId}),
    				new DataRequestMonitor<MIInfo>(getExecutor(), null));
    		fRunToLineActiveOperation = null;
    	}
    }

	/** @since 2.0 */
    @Override
    @DsfServiceEventHandler
    public void eventDispatched(final MIStoppedEvent e) {
    	if (fRunToLineActiveOperation != null) {
    		int bpId = 0;
    		if (e instanceof MIBreakpointHitEvent) {
    			bpId = ((MIBreakpointHitEvent)e).getNumber();
    		}
			String fileLocation = e.getFrame().getFile() + ":" + e.getFrame().getLine();  //$NON-NLS-1$
			String addrLocation = e.getFrame().getAddress();
			// Here we check three different things to see if we are stopped at the right place
			// 1- The actual location in the file.  But this does not work for breakpoints that
			//    were set on non-executable lines
			// 2- The address where the breakpoint was set.  But this does not work for breakpoints
			//    that have multiple addresses (GDB returns <MULTIPLE>.)  I think that is for multi-process
			// 3- The breakpoint id that was hit.  But this does not work if another breakpoint
			//    was also set on the same line because GDB may return that breakpoint as being hit.
			//
			// So this works for the large majority of cases.  The case that won't work is when the user
			// does a runToLine to a line that is non-executable AND has another breakpoint AND
			// has multiple addresses for the breakpoint.  I'm mean, come on!
			if (fileLocation.equals(fRunToLineActiveOperation.getFileLocation()) ||
				addrLocation.equals(fRunToLineActiveOperation.getAddrLocation()) ||
				bpId == fRunToLineActiveOperation.getBreakointId()) {
    			// We stopped at the right place.  All is well.
				fRunToLineActiveOperation = null;
    		} else {
    			// Didn't stop at the right place yet
    			if (fRunToLineActiveOperation.shouldSkipBreakpoints() && e instanceof MIBreakpointHitEvent) {
    				getConnection().queueCommand(
    						fCommandFactory.createMIExecContinue(fRunToLineActiveOperation.getThreadContext()),
    						new DataRequestMonitor<MIInfo>(getExecutor(), null));

    				// Don't send the stop event since we are resuming again.
    				return;
    			} else {
    				// Stopped at another breakpoint that we should not skip.
    				// Or got an interrupt signal from a suspend command.
    				// Or got an interrupt signal because the user set/changed a breakpoint.  This last case is tricky.
    				// We could let the run-to-line continue its job, however, I'm thinking that if the user creates
    				// a new breakpoint, she may want to force the program to stop, in a way to abort the run-to-line.
    				// So, let's cancel the run-to-line in this case.
    				//
    				// Just remove our temporary one since we don't want it to hit later
    				IBreakpointsTargetDMContext bpDmc = DMContexts.getAncestorOfType(fRunToLineActiveOperation.getThreadContext(),
    						IBreakpointsTargetDMContext.class);

    				getConnection().queueCommand(fCommandFactory.createMIBreakDelete(bpDmc, new int[] {fRunToLineActiveOperation.getBreakointId()}),
    						new DataRequestMonitor<MIInfo>(getExecutor(), null));
    				fRunToLineActiveOperation = null;
    			}
    		}
    	}

    	super.eventDispatched(e);
    }
    
    /**
	 * @since 3.0
	 */
    @DsfServiceEventHandler 
    public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
    	// We have started looking at trace records.  We can no longer
    	// do run control operations.
    	fRunControlOperationsEnabled = false;
    }

    /** @since 2.0 */
    public void setReverseModeEnabled(boolean enabled) {
    	fReverseModeEnabled = enabled;
    }
}
