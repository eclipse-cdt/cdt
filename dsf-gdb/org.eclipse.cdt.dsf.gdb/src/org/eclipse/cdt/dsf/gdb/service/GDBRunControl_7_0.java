/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
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
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.commands.MICommand;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReverseContinue;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReverseNext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReverseNextInstruction;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReverseStep;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecReverseStepInstruction;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecUncall;
import org.eclipse.cdt.dsf.mi.service.command.commands.RawCommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class GDBRunControl_7_0 extends MIRunControl implements IReverseRunControl {
    private IGDBBackend fGdb;
	private IMIProcesses fProcService;
	private boolean fReverseSupported = true;
	private boolean fReverseStepping = false;
	private boolean fReverseModeEnabled = false;

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

		if (fGdb.getSessionType() == SessionType.CORE) {
			// No execution for core files, so no support for reverse
			fReverseSupported = false;
		}

        register(new String[]{IRunControl.class.getName(), MIRunControl.class.getName(),
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
                        fGdb.interrupt();
                    } else {
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Context cannot be suspended.", null)); //$NON-NLS-1$
                    }
                    rm.done();
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

	@Override
	public void canResume(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		if (fGdb.getSessionType() == SessionType.CORE) {
			rm.setData(false);
			rm.done();
			return;
		}
		super.canResume(context, rm);
	}

	@Override
	public void canSuspend(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		if (fGdb.getSessionType() == SessionType.CORE) {
			rm.setData(false);
			rm.done();
			return;
		}
		super.canSuspend(context, rm);
	}

	@Override
	public void canStep(final IExecutionDMContext context, StepType stepType, final DataRequestMonitor<Boolean> rm) {
		if (fGdb.getSessionType() == SessionType.CORE) {
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
            MIExecReverseContinue cmd = null;
            if (context instanceof IContainerDMContext) {
            	cmd = new MIExecReverseContinue(context);
            } else {
        		IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
    			if (dmc == null){
    	            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
    	            rm.done();
    	            return;
    			}
            	cmd = new MIExecReverseContinue(dmc);
            }

            setResumePending(true);
            // Cygwin GDB will accept commands and execute them after the step
            // which is not what we want, so mark the target as unavailable
            // as soon as we send a resume command.
            getCache().setContextAvailable(context, false);

            // temporary
            final MIExecReverseContinue finalcmd = cmd;
            final IExecutionDMContext finaldmc = context;
            getConnection().queueCommand(
            		new RawCommand(finaldmc, "set exec-direction reverse"), //$NON-NLS-1$
            		new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
            			@Override
            			public void handleSuccess() {
            				getConnection().queueCommand(finalcmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm)  {
                    			@Override
                    			public void handleCompleted() {
                    				if (!isSuccess()) {
                    			    	setResumePending(false);
                    			        getCache().setContextAvailable(context, true);
                    				}

                    	            getConnection().queueCommand(
                    	            		new RawCommand(finaldmc, "set exec-direction forward"), //$NON-NLS-1$
                    	            		new DataRequestMonitor<MIInfo>(getExecutor(), rm));
                    			}
            				});
            			}
            		});
            // end temporary
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

        MICommand<MIInfo> cmd = null;
        switch(stepType) {
            case STEP_INTO:
            	cmd = new MIExecReverseStep(dmc, 1);
                break;
            case STEP_OVER:
                cmd = new MIExecReverseNext(dmc, 1);
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
                    cmd = new MIExecUncall(topFrameDmc);
                } else {
                    rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Cannot create context for command, stack service not available.", null)); //$NON-NLS-1$
                    rm.done();
                    return;
                }
                break;
            case INSTRUCTION_STEP_INTO:
                cmd = new MIExecReverseStepInstruction(dmc, 1);
                break;
            case INSTRUCTION_STEP_OVER:
                cmd = new MIExecReverseNextInstruction(dmc, 1);
                break;
            default:
                rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Given step type not supported", null)); //$NON-NLS-1$
                rm.done();
                return;
        }
        
    	setResumePending(true);
        fReverseStepping = true;
        getCache().setContextAvailable(context, false);

        // temporary
        final MICommand<MIInfo> finalcmd = cmd;
        final IExecutionDMContext finaldmc = context;
        getConnection().queueCommand(
        		new RawCommand(finaldmc, "set exec-direction reverse"), //$NON-NLS-1$
        		new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
        			@Override
        			public void handleSuccess() {
        				getConnection().queueCommand(finalcmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm)  {
                			@Override
                			public void handleCompleted() {
                				if (!isSuccess()) {
                			    	setResumePending(false);
                			        fReverseStepping = false;
                			        getCache().setContextAvailable(context, true);
                				}
                	            getConnection().queueCommand(
                	            		new RawCommand(finaldmc, "set exec-direction forward"), //$NON-NLS-1$
                	            		new DataRequestMonitor<MIInfo>(getExecutor(), rm));
                			}
        				});
        			}
        		});
        // end temporary
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
    	
    	if (enable) {
        	getConnection().queueCommand(
        			new RawCommand(context, "record"), //$NON-NLS-1$
        			new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
        				@Override
        				public void handleSuccess() {
        					setReverseModeEnabled(true);
        					rm.done();
        				}
        			});
    	} else {
        	getConnection().queueCommand(
        			new RawCommand(context, "stoprecord"), //$NON-NLS-1$
        			new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
        				@Override
        				public void handleSuccess() {
        					setReverseModeEnabled(false);
        					rm.done();
        				}
        			});
    	}
	}
    
    /** @since 2.0 */
    public void setReverseModeEnabled(boolean enabled) {
    	fReverseModeEnabled = enabled;
    }
}
