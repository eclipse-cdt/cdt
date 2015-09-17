/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson AB		  - Modified for additional functionality	
 *     Nokia - create and use backend service. 
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Bug 415362
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;


import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.IRunControl3;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator;
import org.eclipse.cdt.dsf.gdb.internal.service.control.StepIntoSelectionActiveOperation;
import org.eclipse.cdt.dsf.gdb.internal.service.control.StepIntoSelectionUtils;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIInferiorExitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISignalEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadExitEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackInfoDepthInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class GDBRunControl extends MIRunControl {
	
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
	private ICommandControlService fConnection;
	
	// Record list of execution contexts
	private IExecutionDMContext[] fOldExecutionCtxts;

	private RunToLineActiveOperation fRunToLineActiveOperation = null;
	
	private StepIntoSelectionActiveOperation fStepInToSelectionActiveOperation = null;

    public GDBRunControl(DsfSession session) {
        super(session);
    }
    
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
            new ImmediateRequestMonitor(requestMonitor) { 
                @Override
                public void handleSuccess() {
                    doInitialize(requestMonitor);
                }});
    }

    private void doInitialize(final RequestMonitor requestMonitor) {
    	
        fGdb = getServicesTracker().getService(IGDBBackend.class);
        fProcService = getServicesTracker().getService(IMIProcesses.class);
        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
        fConnection = getServicesTracker().getService(ICommandControlService.class);

        register(new String[]{IRunControl.class.getName(), 
           		IRunControl2.class.getName(),
           		IRunControl3.class.getName(),
           		IMIRunControl.class.getName(),
           		MIRunControl.class.getName(), 
        		GDBRunControl.class.getName()}, new Hashtable<String,String>());
        requestMonitor.done();
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        unregister();
        super.shutdown(requestMonitor);
    }
    
	/**
	 * Allows us to know if run control operations should be
	 * enabled or disabled.  Run control operations are
	 * disabled, for example, when dealing with post-mortem sessions.
	 * @since 4.2
	 */
	protected boolean runControlOperationsEnabled() {
		return fGdb.getSessionType() != SessionType.CORE;
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
    public void suspend(final IExecutionDMContext context, final RequestMonitor rm){
        canSuspend(
            context, 
            new DataRequestMonitor<Boolean>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    if (getData()) {
                    	int interruptTimeout = getInterruptTimeout();
						// A local Windows attach session requires us to interrupt
						// the inferior instead of gdb. This deficiency was fixed
                    	// in gdb 7.0. Note that there's a GDBRunControl_7_0,
                    	// so we're dealing with gdb < 7.0 if we get here.
	                    // Refer to https://bugs.eclipse.org/bugs/show_bug.cgi?id=304096#c56
	                    // if you have the stomach for it.
                    	if (fGdb.getIsAttachSession() 
                    			&& fGdb.getSessionType() != SessionType.REMOTE
                    			&& Platform.getOS().equals(Platform.OS_WIN32)) {
                    		IMIProcessDMContext processDmc = DMContexts.getAncestorOfType(context, IMIProcessDMContext.class);
                    		String inferiorPid = processDmc.getProcId();
                    		if (inferiorPid != null) {
                    			fGdb.interruptInferiorAndWait(Long.parseLong(inferiorPid), interruptTimeout, rm);
                    		}
                    		else {
                    			assert false : "why don't we have the inferior's pid?";  //$NON-NLS-1$
                    			fGdb.interruptAndWait(interruptTimeout, rm);
                    		}
                    	}
                    	else {
                            fGdb.interruptAndWait(interruptTimeout, rm);
                    	}
                    } else {
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Context cannot be suspended.", null)); //$NON-NLS-1$
                        rm.done();
                    }
                }
            });
    }

    
    /*
	 * This is a HACK. Remove this method when GDB starts to account exited threads id in -thread-list-id command.
	 * Exited threads are reported in -thread-list-id command even after an exit event is raised by GDB
	 * Hence, this method needs a special handling in case of GDB.
	 * Raises ExitEvent when a thread really exits from the system. This is done by comparing the execution contexts list
	 * See bug 200615 for details.
	 */
	@Override
    public void getExecutionContexts(IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		// user groups support 
		IMIExecutionContextTranslator translator = getServicesTracker().getService(IMIExecutionContextTranslator.class);
		if (translator != null) {
			translator.getExecutionContexts(containerDmc, rm);
			return;
		}
		// end user group support
		
		fProcService.getProcessesBeingDebugged(
				containerDmc != null ? containerDmc : fConnection.getContext(),
				new DataRequestMonitor<IDMContext[]>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						if (getData() instanceof IExecutionDMContext[]) {
							IExecutionDMContext[] execDmcs = (IExecutionDMContext[])getData();
							raiseExitEvents(execDmcs);
							fOldExecutionCtxts = execDmcs;
							rm.setData(fOldExecutionCtxts);
						} else {
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid contexts", null)); //$NON-NLS-1$
						}
						rm.done();
					}
				});
    }

	private void raiseExitEvents(IExecutionDMContext[] ctxts){
		if(ctxts == null || fOldExecutionCtxts == null)
			return;
		List<IExecutionDMContext> list = Arrays.asList(ctxts);
		List<IExecutionDMContext> oldThreadList = Arrays.asList(fOldExecutionCtxts);
		Iterator<IExecutionDMContext> iterator = oldThreadList.iterator();
		while(iterator.hasNext()){
			IExecutionDMContext ctxt = iterator.next();
			if(! list.contains(ctxt)){
			    IContainerDMContext containerDmc = DMContexts.getAncestorOfType(ctxt, IContainerDMContext.class); 
                MIEvent<?> e =  new MIThreadExitEvent(containerDmc, Integer.toString(((IMIExecutionDMContext)ctxt).getThreadId()));
                // Dispatch DsfMIThreadExitEvent
                getSession().dispatchEvent(e, getProperties());
			}
		}
	}
	
	/**
	 * @since 2.0
	 */
    @Override
	public void canResume(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		if (!runControlOperationsEnabled()) {
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
		if (!runControlOperationsEnabled()) {
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
		if (!runControlOperationsEnabled()) {
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

	/** @since 3.0 */
	@Override
	public void runToLocation(IExecutionDMContext context, final String location, final boolean skipBreakpoints, final RequestMonitor rm){
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
                		    		fStepInToSelectionActiveOperation = null;

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
    		fStepInToSelectionActiveOperation = null;
    	}
    }

	/** @since 2.0 */
    @Override
    @DsfServiceEventHandler
	public void eventDispatched(final MIStoppedEvent e) {
    	// A disabled signal event is due to interrupting the target
    	// to set a breakpoint.  This can happen during a run-to-line
    	// or step-into operation, so we need to check it first.
    	if (fDisableNextSignalEvent && e instanceof MISignalEvent) {
    		fDisableNextSignalEvent = false;
    		fSilencedSignalEvent = e;
    		// We don't broadcast this stopped event
    		return;
    	}

    	if (processRunToLineStoppedEvent(e)) {
			// If RunToLine is not completed
			return;
		}

		if (!processStepIntoSelection(e)) {
			//Step into Selection is not in progress broadcast the stop event
	    	super.eventDispatched(e);
		}
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.MIRunControl#eventDispatched(org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent)
     */
    @Override
    @DsfServiceEventHandler
	public void eventDispatched(final MIRunningEvent e) {
		if (fDisableNextRunningEvent) {
			// Leave the action to the super class
			super.eventDispatched(e);
			return;
		}

		if (fRunToLineActiveOperation == null && fStepInToSelectionActiveOperation == null) {
			// No special case here, i.e. send notification
			super.eventDispatched(e);
		} else {
			// Either RuntoLine or StepIntoSelection operations are active
			if (fLatestEvent instanceof ISuspendedDMEvent) {
				// Need to send out Running event notification only once per operation, then a stop event is expected at
				// the end of it
				super.eventDispatched(e);
			}
		}

		// No event dispatched if RuntoLine or StepIntoSelection operations are active and a previous event is not a
		// Suspended event, i.e. only one Running event distributed per operation	
	}
    
    private boolean processRunToLineStoppedEvent(final MIStoppedEvent e) {
    	if (fRunToLineActiveOperation != null) {
    		int bpId = 0;
    		if (e instanceof MIBreakpointHitEvent) {
    			bpId = ((MIBreakpointHitEvent)e).getNumber();
    		}

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
			boolean equalFileLocation = false;
			boolean equalAddrLocation = false;
			boolean equalBpId = bpId == fRunToLineActiveOperation.getBreakointId();
			MIFrame frame = e.getFrame();
			if(frame != null) {
				String fileLocation = frame.getFile() + ":" + frame.getLine();  //$NON-NLS-1$
				String addrLocation = frame.getAddress();
				equalFileLocation = fileLocation.equals(fRunToLineActiveOperation.getFileLocation());
				equalAddrLocation = addrLocation.equals(fRunToLineActiveOperation.getAddrLocation());
			}
			
			if (equalFileLocation || equalAddrLocation || equalBpId) {
    			// We stopped at the right place.  All is well.
				fRunToLineActiveOperation = null;
    		} else {
    			// Didn't stop at the right place yet
    			if (fRunToLineActiveOperation.shouldSkipBreakpoints() && e instanceof MIBreakpointHitEvent) {
    				getConnection().queueCommand(
    						fCommandFactory.createMIExecContinue(fRunToLineActiveOperation.getThreadContext()),
    						new DataRequestMonitor<MIInfo>(getExecutor(), null));

    				// Don't send the stop event since we are resuming again.
    				return true;
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
    				fStepInToSelectionActiveOperation = null;
    			}
    		}
    	}
    	
    	return false;
    }
    
	private boolean processStepIntoSelection(final MIStoppedEvent e) {
		if (fStepInToSelectionActiveOperation == null) {
			return false;
		}
		
		// First check if it is the right thread that stopped
		final IMIExecutionDMContext threadDmc = DMContexts.getAncestorOfType(e.getDMContext(), IMIExecutionDMContext.class);
		if (fStepInToSelectionActiveOperation.getThreadContext().equals(threadDmc)) {
			final MIFrame frame = e.getFrame();

			assert(fRunToLineActiveOperation == null);
			
			if (fStepInToSelectionActiveOperation.getRunToLineFrame() == null) {
				assert(fStepInToSelectionActiveOperation.getLine() == frame.getLine());
				// Shall now be at the runToline location
				fStepInToSelectionActiveOperation.setRunToLineFrame(frame);
			}
			
			// Step - Not at the right place just yet
			// Initiate an async call chain parent
			getStackDepth(threadDmc, new DataRequestMonitor<Integer>(getExecutor(), null) {
				private int originalStackDepth = fStepInToSelectionActiveOperation.getOriginalStackDepth();

				@Override
				protected void handleSuccess() {
					int frameDepth = getStackDepth();
					
					if (frameDepth > originalStackDepth) {
						//shall be true as this is using stepinto step type vs instruction stepinto
						assert(frameDepth == originalStackDepth + 1);
						
						// Check for a match
						if (StepIntoSelectionUtils.sameSignature(frame, fStepInToSelectionActiveOperation)) {				
							// Hit !!
							stopStepIntoSelection(e);
							return;
						}
						
						// Located deeper in the stack, Shall continue step / search
						// Step return
						continueStepping(e, StepType.STEP_RETURN);
					} else if (frameDepth == originalStackDepth) {
						// Continue step / search as long as
						// this is the starting base line for the search
						String currentLocation = frame.getFile() + ":" + frame.getLine(); //$NON-NLS-1$
						String searchLineLocation = fStepInToSelectionActiveOperation.getFileLocation();
						if (currentLocation.equals(searchLineLocation)) {
							continueStepping(e, StepType.STEP_INTO);
						} else {
							// We have moved to a line
							// different from the base
							// search line i.e. missed the
							// target function !!
							StepIntoSelectionUtils.missedSelectedTarget(fStepInToSelectionActiveOperation);
							stopStepIntoSelection(e);	
						}
					} else {
						// missed the target point
						StepIntoSelectionUtils.missedSelectedTarget(fStepInToSelectionActiveOperation);					}
				}

				@Override
				protected void handleFailure() {
					// log error
					if (getStatus() != null) {
						GdbPlugin.getDefault().getLog().log(getStatus());
					}

					stopStepIntoSelection(e);
				}

				private int getStackDepth() {
					Integer stackDepth = null;
					if (isSuccess() && getData() != null) {
						stackDepth = getData();
						// This is the base frame, the original stack depth shall be updated
						if (frame == fStepInToSelectionActiveOperation.getRunToLineFrame()) {
							fStepInToSelectionActiveOperation.setOriginalStackDepth(stackDepth);
							originalStackDepth = stackDepth;
						}
					}

					if (stackDepth == null) {
						// Unsuccessful resolution of stack depth, default to same stack depth to detect a change of line within the original frame
						return fStepInToSelectionActiveOperation.getOriginalStackDepth();
					}

					return stackDepth.intValue();
				}
			});
			
			//Processing step into selection
			return true;
		}
		
		//All threads stopped, however outside the scope of the step into selection context
		//We need to abort the step into selection and broadcast the stop
		fStepInToSelectionActiveOperation = null;
		return false;
	}
    
	private void stopStepIntoSelection(final MIStoppedEvent e) {
		fStepInToSelectionActiveOperation = null;
		// Need to broadcast the stop
		super.eventDispatched(e);
    }

    /**
	 * @since 4.2
	 */
    protected int getInterruptTimeout() {
    	return IGDBBackend.INTERRUPT_TIMEOUT_DEFAULT;
	}

	private void continueStepping(final MIStoppedEvent event, StepType steptype) {
		step(fStepInToSelectionActiveOperation.getThreadContext(), steptype, false, new RequestMonitor(getExecutor(), null) {
			@Override
			protected void handleFailure() {
				// log error
				if (getStatus() != null) {
					GdbPlugin.getDefault().getLog().log(getStatus());
				}

				stopStepIntoSelection(event);
			}
		});
	}
	
	// ------------------------------------------------------------------------
	// Step into Selection
	// ------------------------------------------------------------------------
	private void stepIntoSelection(final IExecutionDMContext context, final int baseLine, final String baseLineLocation, final boolean skipBreakpoints, final IFunctionDeclaration targetFunction,
			final RequestMonitor rm) {

		assert context != null;

		final IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Given context: " + context + " is not an MI execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		if (!doCanResume(dmc)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Cannot resume context", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		if (fLatestEvent == null || !(fLatestEvent instanceof SuspendedEvent)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Given context: " + context + " invalid suspended event.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		SuspendedEvent suspendedEvent = (SuspendedEvent) fLatestEvent;
		final MIFrame currentFrame = suspendedEvent.getMIEvent().getFrame();
		if (currentFrame == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Given event: " + suspendedEvent + " with invalid frame.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		getStackDepth(dmc, new DataRequestMonitor<Integer>(getExecutor(), rm) {
			@Override
			public void handleSuccess() {
				if (getData() != null) {
					final int framesSize = getData().intValue();

					// make sure the operation is removed upon
					// failure detection
					final RequestMonitor rms = new RequestMonitor(getExecutor(), rm) {
						@Override
						protected void handleFailure() {
							fStepInToSelectionActiveOperation = null;
							super.handleFailure();
						}
					};

					if ((currentFrame.getFile() + ":" + currentFrame.getLine()).endsWith(baseLineLocation)) { //$NON-NLS-1$
						// Save the step into selection information
						fStepInToSelectionActiveOperation = new StepIntoSelectionActiveOperation(dmc, baseLine, targetFunction, framesSize,
								currentFrame);
						// Ready to step into a function selected
						// within a current line
						step(dmc, StepType.STEP_INTO, rms);
					} else {
						// Save the step into selection information
						fStepInToSelectionActiveOperation = new StepIntoSelectionActiveOperation(dmc, baseLine, targetFunction, framesSize, null);
						// Pointing to a line different than the current line
						// Needs to RunToLine before stepping to the selection
						runToLocation(dmc, baseLineLocation, skipBreakpoints, rms);
					}
				} else {
					rm.done();
				}
			}
		});
	}

	/**
	 * @since 4.2
	 */
	@Override
	public void canStepIntoSelection(IExecutionDMContext context, String sourceFile, int lineNumber, IFunctionDeclaration selectedFunction, DataRequestMonitor<Boolean> rm) {
		canStep(context, StepType.STEP_INTO, rm);
	}
    
	/**
	 * @since 4.2
	 */
	@Override
	public void stepIntoSelection(final IExecutionDMContext context, String sourceFile, final int lineNumber, final boolean skipBreakpoints, final IFunctionDeclaration selectedFunction, final RequestMonitor rm) {
		determineDebuggerPath(context, sourceFile, new ImmediateDataRequestMonitor<String>(rm) {
			@Override
			protected void handleSuccess() {
				stepIntoSelection(context, lineNumber, getData() + ":" + Integer.toString(lineNumber), skipBreakpoints, selectedFunction, rm); //$NON-NLS-1$
			}
		});
	}
	
	/**
	 * Help method used when the stopped event has not been broadcasted e.g. in the middle of step into selection
	 * 
	 * @param dmc
	 * @param rm
	 */
	private void getStackDepth(final IMIExecutionDMContext dmc, final DataRequestMonitor<Integer> rm) {
		if (dmc != null) {
			fConnection.queueCommand(fCommandFactory.createMIStackInfoDepth(dmc), new DataRequestMonitor<MIStackInfoDepthInfo>(getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					rm.setData(getData().getDepth());
					rm.done();
				}
			});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
			rm.done();
		}
	}
    
}
