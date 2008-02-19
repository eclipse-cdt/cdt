/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.internal.DsfDebugPlugin;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.debug.service.IRunControl.StepType;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.osgi.framework.BundleContext;

/**
 * This service builds on top of standard run control service to provide 
 * step queuing functionality.  Step queuing essentially allows user to press
 * and hold the step key and achieve maximum stepping speed.  If this service
 * is used, other service implementations, such as stack and expressions, can 
 * use it to avoid requesting data from debugger back end if another step is 
 * about to be executed. 
 */
public class StepQueueManager extends AbstractDsfService
{
    /**
     * Amount of time in miliseconds, that it takes the ISteppingTimedOutEvent 
     * event to be issued after a step is started. 
     * @see ISteppingTimedOutEvent  
     */
    public final static int STEPPING_TIMEOUT = 500;
    
    /**
     * The depth of the step queue.  In other words, the maximum number of steps 
     * that are queued before the step queue manager throwing them away. 
     */
    public final static int STEP_QUEUE_DEPTH = 3;
    
    /**
     * Indicates that the given context has been stepping for some time, 
     * and the UI (views and actions) may need to be updated accordingly. 
     */
    public interface ISteppingTimedOutEvent extends IDMEvent<IExecutionDMContext> {}

    
    private static class StepRequest {
        StepType fStepType;
        boolean fIsInstructionStep;
        StepRequest(StepType type, boolean instruction) {
            fStepType = type;
            fIsInstructionStep = instruction;
        }
    }
    
    private IRunControl fRunControl;
    private int fQueueDepth = STEP_QUEUE_DEPTH;
    private Map<IExecutionDMContext,List<StepRequest>> fStepQueues = new HashMap<IExecutionDMContext,List<StepRequest>>();
    private Map<IExecutionDMContext,Boolean> fTimedOutFlags = new HashMap<IExecutionDMContext,Boolean>();
    private Map<IExecutionDMContext,ScheduledFuture<?>> fTimedOutFutures = new HashMap<IExecutionDMContext,ScheduledFuture<?>>();
    
    public StepQueueManager(DsfSession session) {
        super(session);
    }

    ///////////////////////////////////////////////////////////////////////////
    // IDsfService
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
            new RequestMonitor(getExecutor(), requestMonitor) { 
                @Override
                protected void handleOK() {
                    doInitialize(requestMonitor);
                }});
    }

    private void doInitialize(final RequestMonitor requestMonitor) {
        fRunControl = getServicesTracker().getService(IRunControl.class);

        getSession().addServiceEventListener(this, null);
        register(new String[]{ StepQueueManager.class.getName()}, new Hashtable<String,String>());
        requestMonitor.done();
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        unregister();
        getSession().removeServiceEventListener(this);
        super.shutdown(requestMonitor);
    }
    
    @Override
    protected BundleContext getBundleContext() {
        return DsfDebugPlugin.getBundleContext();
    }

    /**
     * Checks whether a step command can be queued up for given context.
     */
    public boolean canEnqueueStep(IExecutionDMContext execCtx) {
        return (fRunControl.isSuspended(execCtx) && fRunControl.canStep(execCtx)) || 
               (fRunControl.isStepping(execCtx) && !isSteppingTimedOut(execCtx));
    }

    /**
     * Checks whether an instruction step command can be queued up for given context.
     */
    public boolean canEnqueueInstructionStep(IExecutionDMContext execCtx) {
        return (fRunControl.isSuspended(execCtx) && fRunControl.canInstructionStep(execCtx)) || 
               (fRunControl.isStepping(execCtx) && !isSteppingTimedOut(execCtx));
    }

    /** 
     * Returns the number of step commands that are queued for given execution
     * context.
     */
    public int getPendingStepCount(IExecutionDMContext execCtx) {
        List<StepRequest> stepQueue = fStepQueues.get(execCtx);
        if (stepQueue == null) return 0;
        return stepQueue.size();
    }

    /**
     * Adds a step command to the execution queue for given context.
     * @param execCtx Execution context that should perform the step. 
     * @param stepType Type of step to execute.
     */
    public void enqueueStep(IExecutionDMContext execCtx, StepType stepType) {
        if (fRunControl.canStep(execCtx)) {
            fRunControl.step(execCtx, stepType, new RequestMonitor(getExecutor(), null)); 
        } else if (canEnqueueStep(execCtx)) {
            List<StepRequest> stepQueue = fStepQueues.get(execCtx);
            if (stepQueue == null) {
                stepQueue = new LinkedList<StepRequest>();
                fStepQueues.put(execCtx, stepQueue);
            }
            if (stepQueue.size() < fQueueDepth) {
                stepQueue.add(new StepRequest(stepType, false));
            }
        }
    }

    /**
     * Adds an instruction step command to the execution queue for given 
     * context.
     * @param execCtx Execution context that should perform the step. 
     * @param stepType Type of step to execute.
     */
    public void enqueueInstructionStep(IExecutionDMContext execCtx, StepType stepType) {
        if (fRunControl.canInstructionStep(execCtx)) {
            fRunControl.instructionStep(execCtx, stepType, new RequestMonitor(getExecutor(), null)); 
        } else if (canEnqueueInstructionStep(execCtx)) {
            List<StepRequest> stepQueue = fStepQueues.get(execCtx);
            if (stepQueue == null) {
                stepQueue = new LinkedList<StepRequest>();
                fStepQueues.put(execCtx, stepQueue);
            }
            if (stepQueue.size() < fQueueDepth) {
                stepQueue.add(new StepRequest(stepType, true));
            }
        }
    }

    /**
     * Returns whether the step instruction for the given context has timed out.
     */
    public boolean isSteppingTimedOut(IExecutionDMContext execCtx) {
        return fTimedOutFlags.containsKey(execCtx) ? fTimedOutFlags.get(execCtx) : false;
    }
    

    ///////////////////////////////////////////////////////////////////////////

    @DsfServiceEventHandler 
    public void eventDispatched(ISuspendedDMEvent e) {
        // Take care of the stepping time out
        fTimedOutFlags.remove(e.getDMContext());
        ScheduledFuture<?> future = fTimedOutFutures.remove(e.getDMContext()); 
        if (future != null) future.cancel(false);
        
        // Check if there's a step pending, if so execute it
        if (fStepQueues.containsKey(e.getDMContext())) {
            List<StepRequest> queue = fStepQueues.get(e.getDMContext());
            StepRequest request = queue.remove(queue.size() - 1);
            if (queue.isEmpty()) fStepQueues.remove(e.getDMContext());
            if (request.fIsInstructionStep) {
                if (fRunControl.canInstructionStep(e.getDMContext())) {
                    fRunControl.instructionStep(
                        e.getDMContext(), request.fStepType, new RequestMonitor(getExecutor(), null));
                } else {
                    // For whatever reason we can't step anymore, so clear out
                    // the step queue.
                    fStepQueues.remove(e.getDMContext());
                }
            } else {
                if (fRunControl.canStep(e.getDMContext())) {
                    fRunControl.step(e.getDMContext(), request.fStepType,new RequestMonitor(getExecutor(), null));
                } else {
                    // For whatever reason we can't step anymore, so clear out
                    // the step queue.
                    fStepQueues.remove(e.getDMContext());
                }
            }
        }
    }

    @DsfServiceEventHandler 
    public void eventDispatched(final IResumedDMEvent e) {
        if (e.getReason().equals(StateChangeReason.STEP)) {
            fTimedOutFlags.put(e.getDMContext(), Boolean.FALSE);
            // We shouldn't have a stepping timeout running unless we get two 
            // stepping events in a row without a suspended, which would be a 
            // protocol error.
            assert !fTimedOutFutures.containsKey(e.getDMContext());
            fTimedOutFutures.put(
                e.getDMContext(), 
                getExecutor().schedule(
                    new DsfRunnable() { public void run() {
                        fTimedOutFutures.remove(e.getDMContext());

                        if (getSession().isActive()) {
                            // Issue the stepping time-out event.
                            getSession().dispatchEvent(
                                new ISteppingTimedOutEvent() { 
                                    public IExecutionDMContext getDMContext() { return e.getDMContext(); }
                                }, 
                                getProperties());
                        }
                    }},
                    STEPPING_TIMEOUT, TimeUnit.MILLISECONDS)
                );
            
        } 
    }    

    @DsfServiceEventHandler 
    public void eventDispatched(ISteppingTimedOutEvent e) {
        fTimedOutFlags.put(e.getDMContext(), Boolean.TRUE);
    }

}
