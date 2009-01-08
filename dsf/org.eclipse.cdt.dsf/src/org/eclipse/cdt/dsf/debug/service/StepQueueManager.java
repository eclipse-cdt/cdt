/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.osgi.framework.BundleContext;

/**
 * This service builds on top of standard run control service to provide 
 * step queuing functionality.  Step queuing essentially allows user to press
 * and hold the step key and achieve maximum stepping speed.  If this service
 * is used, other service implementations, such as stack and expressions, can 
 * use it to avoid requesting data from debugger back end if another step is 
 * about to be executed. 
 * 
 * @deprecated The functionality has been integrated in the UI layer.
 */
@Deprecated
public class StepQueueManager extends AbstractDsfService implements IStepQueueManager
{
    /**
     * Amount of time in milliseconds, that it takes the ISteppingTimedOutEvent 
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
        StepRequest(StepType type) {
            fStepType = type;
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
                protected void handleSuccess() {
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
        return DsfPlugin.getBundleContext();
    }

    /*
	 * @see org.eclipse.cdt.dsf.debug.service.IStepQueueManager#canEnqueueStep(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, org.eclipse.cdt.dsf.debug.service.IRunControl.StepType, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
    public void canEnqueueStep(IExecutionDMContext execCtx, StepType stepType, DataRequestMonitor<Boolean> rm) {
        if (doCanEnqueueStep(execCtx, stepType)) {
            rm.setData(true);
            rm.done();
        } else {
            fRunControl.canStep(execCtx, stepType, rm);
        }
    }

    private boolean doCanEnqueueStep(IExecutionDMContext execCtx, StepType stepType) {
        return fRunControl.isStepping(execCtx) && !isSteppingTimedOut(execCtx); 
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

    /*
	 * @see org.eclipse.cdt.dsf.debug.service.IStepQueueManager#enqueueStep(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, org.eclipse.cdt.dsf.debug.service.IRunControl.StepType)
	 */
    public void enqueueStep(final IExecutionDMContext execCtx, final StepType stepType) {
        fRunControl.canStep(
            execCtx, stepType, new DataRequestMonitor<Boolean>(getExecutor(), null) {
                @Override
                protected void handleCompleted() {
                    if (isSuccess() && getData()) {
                        fRunControl.step(execCtx, stepType, new RequestMonitor(getExecutor(), null)); 
                    } else if (doCanEnqueueStep(execCtx, stepType)) {
                        List<StepRequest> stepQueue = fStepQueues.get(execCtx);
                        if (stepQueue == null) {
                            stepQueue = new LinkedList<StepRequest>();
                            fStepQueues.put(execCtx, stepQueue);
                        }
                        if (stepQueue.size() < fQueueDepth) {
                            stepQueue.add(new StepRequest(stepType));
                        }
                    }
                }
            });
    }

    /**
     * Returns whether the step instruction for the given context has timed out.
     */
    public boolean isSteppingTimedOut(IExecutionDMContext execCtx) {
        for (IExecutionDMContext timedOutCtx : fTimedOutFlags.keySet()) {
            if (execCtx.equals(timedOutCtx) || DMContexts.isAncestorOf(execCtx, timedOutCtx)) {
                return fTimedOutFlags.get(timedOutCtx);
            }
        }
        return false;
    }
    

    ///////////////////////////////////////////////////////////////////////////

    @DsfServiceEventHandler 
    public void eventDispatched(final ISuspendedDMEvent e) {
        // Take care of the stepping time out
        fTimedOutFlags.remove(e.getDMContext());
        ScheduledFuture<?> future = fTimedOutFutures.remove(e.getDMContext()); 
        if (future != null) future.cancel(false);
        
        // Check if there's a step pending, if so execute it
        if (fStepQueues.containsKey(e.getDMContext())) {
            List<StepRequest> queue = fStepQueues.get(e.getDMContext());
            final StepRequest request = queue.remove(queue.size() - 1);
            if (queue.isEmpty()) fStepQueues.remove(e.getDMContext());
            fRunControl.canStep(
                e.getDMContext(), request.fStepType, 
                new DataRequestMonitor<Boolean>(getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess() && getData()) {
                            fRunControl.step(
                                e.getDMContext(), request.fStepType, new RequestMonitor(getExecutor(), null));
                        } else {
                            // For whatever reason we can't step anymore, so clear out
                            // the step queue.
                            fStepQueues.remove(e.getDMContext());
                        }
                    }
                });
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
