/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * This class builds on top of standard run control service to provide
 * functionality for step queuing and delaying. Step queuing essentially allows
 * user to press and hold the step key and achieve maximum stepping speed. If
 * this class is used, other service implementations, such as stack and
 * expressions, can use it to avoid requesting data from debugger back end if
 * another step is about to be executed.
 * 
 * @since 1.1
 */
@ConfinedToDsfExecutor("#getExecutor()")
public final class SteppingController {
    /**
     * Default delay in milliseconds, that it takes the SteppingTimedOutEvent 
     * event to be issued after a step is started. 
     * @see SteppingTimedOutEvent
     * @see #setStepTimeout(int)
     * @see #getStepTimeout()
     */
    public final static int STEPPING_TIMEOUT = 500;
    
    /**
     * The depth of the step queue.  In other words, the maximum number of steps 
     * that are queued before the step queue manager is throwing them away. 
     */
    public final static int STEP_QUEUE_DEPTH = 2;

	/**
	 * The maximum delay (in milliseconds) between steps when synchronized
	 * stepping is enabled. This also serves as a safeguard in the case stepping
	 * control participants fail to indicate completion of event processing.
	 */
    public final static int MAX_STEP_DELAY= 5000;

    private final static boolean DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.cdt.dsf.ui/debug/stepping")); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Indicates that the given context has been stepping for some time, 
     * and the UI (views and actions) may need to be updated accordingly. 
     */
	public static final class SteppingTimedOutEvent extends AbstractDMEvent<IExecutionDMContext> {
		private SteppingTimedOutEvent(IExecutionDMContext execCtx) {
			super(execCtx);
		}
	}

	/**
	 * Interface for clients interested in stepping control. When a stepping
	 * control participant is registered with the stepping controller, it is
	 * expected to call
	 * {@link SteppingController#doneStepping(IExecutionDMContext, ISteppingControlParticipant)
	 * doneStepping} as soon as a "step", i.e. a suspended event has been
	 * processed. If synchronized stepping is enabled, further stepping is
	 * blocked until all stepping control participants have indicated completion
	 * of event processing or the maximum timeout
	 * {@link SteppingController#MAX_STEP_DELAY} has been reached.
	 * 
	 * @see SteppingController#addSteppingControlParticipant(ISteppingControlParticipant)
	 * @see SteppingController#removeSteppingControlParticipant(ISteppingControlParticipant)
	 */
	public interface ISteppingControlParticipant {
	}

	private static class StepRequest {
		IExecutionDMContext fContext;
        StepType fStepType;
        boolean inProgress = false;
        StepRequest(IExecutionDMContext execCtx, StepType type) {
        	fContext = execCtx;
            fStepType = type;
        }
    }

	private class TimeOutRunnable extends DsfRunnable{
		
		TimeOutRunnable(IExecutionDMContext dmc) {
			fDmc = dmc;
		}
		
		private final IExecutionDMContext fDmc;
		
		public void run() {
			fTimedOutFutures.remove(fDmc);

            if (getSession().isActive()) {
                fTimedOutFlags.put(fDmc, Boolean.TRUE);
                enableStepping(fDmc);
                // Issue the stepping time-out event.
                getSession().dispatchEvent(
                    new SteppingTimedOutEvent(fDmc), 
                    null);
            }
        }
	}
	
	private final DsfSession fSession;
	private final DsfServicesTracker fServicesTracker;

    private IRunControl fRunControl;
    private int fQueueDepth = STEP_QUEUE_DEPTH;
    
    private final Map<IExecutionDMContext,List<StepRequest>> fStepQueues = new HashMap<IExecutionDMContext,List<StepRequest>>();
    private final Map<IExecutionDMContext,Boolean> fTimedOutFlags = new HashMap<IExecutionDMContext,Boolean>();
    private final Map<IExecutionDMContext,ScheduledFuture<?>> fTimedOutFutures = new HashMap<IExecutionDMContext,ScheduledFuture<?>>();

	/**
	 * Records the time of the last step for an execution context.
	 */
	private final Map<IExecutionDMContext, Long> fLastStepTimes= new HashMap<IExecutionDMContext, Long>();

	/**
	 * Minimum step interval in milliseconds.
	 */
	private volatile int fMinStepInterval;

    /**
     * Step timeout in milliseconds.
     */
    private volatile int fStepTimeout = STEPPING_TIMEOUT;

	/**
	 * Map of execution contexts for which a step is in progress.
	 */
	private final Map<IExecutionDMContext, List<ISteppingControlParticipant>> fStepInProgress = new HashMap<IExecutionDMContext, List<ISteppingControlParticipant>>();

	/**
	 * List of registered stepping control participants.
	 */
	private final List<ISteppingControlParticipant> fParticipants = Collections.synchronizedList(new ArrayList<ISteppingControlParticipant>());

	/**
	 * Property change listener.  It updates the stepping control settings.
	 */
	private IPropertyChangeListener fPreferencesListener;

    public SteppingController(DsfSession session) {
        fSession = session;
        fServicesTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
        
        final IPreferenceStore store= DsfUIPlugin.getDefault().getPreferenceStore();

        fPreferencesListener = new IPropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent event) {
                handlePropertyChanged(store, event);
            }};
        store.addPropertyChangeListener(fPreferencesListener);
        
        setMinimumStepInterval(store.getInt(IDsfDebugUIConstants.PREF_MIN_STEP_INTERVAL));
    }

    @ThreadSafe
    public void dispose() {
        try {
            fSession.getExecutor().execute(new DsfRunnable() {
                public void run() {
                    if (fRunControl != null) {
                        getSession().removeServiceEventListener(SteppingController.this);
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            // Session already gone.
        }
    	
        IPreferenceStore store= DsfUIPlugin.getDefault().getPreferenceStore();
        store.removePropertyChangeListener(fPreferencesListener);

        fServicesTracker.dispose();
    }

    /**
     * Configure the minimum time (in milliseconds) to wait between steps.
     * 
     * @param interval
     */
    @ThreadSafe
    public void setMinimumStepInterval(int interval) {
    	fMinStepInterval = interval;
    }

    /**
     * Configure the step timeout value. This controls the delay how long it takes
     * to send out the {@link SteppingTimedOutEvent} after a step has been issued.
     * 
     * @param timeout  The timeout value in milliseconds (must be non-negative).
     * @since 2.2
     */
    @ThreadSafe
    public void setStepTimeout(int timeout) {
        assert timeout >= 0;
        fStepTimeout = timeout;
    }
    
    /**
     * @return the currently configured step timeout value
     * @since 2.2
     */
    @ThreadSafe
    public int getStepTimeout() {
        return fStepTimeout;
    }

	/**
	 * Register given stepping control participant.
	 * <p>
	 * Participants are obliged to call
	 * {@link #doneStepping(IExecutionDMContext, ISteppingControlParticipant)}
	 * when they have received and completed processing an
	 * {@link ISuspendedDMEvent}. If synchronized stepping is enabled, further
	 * stepping is disabled until all participants have indicated completion of
	 * processing the event.
	 * </p>
	 * 
	 * @param participant
	 */
    @ThreadSafe
    public void addSteppingControlParticipant(ISteppingControlParticipant participant) {
    	fParticipants.add(participant);
    }

    /**
     * Unregister given stepping control participant.
     * 
     * @param participant
     */
    @ThreadSafe
    public void removeSteppingControlParticipant(final ISteppingControlParticipant participant) {
    	fParticipants.remove(participant);
    }

    /**
     * Indicate that participant has completed processing of the last step.
     * 
     * @param execCtx
     */
    public void doneStepping(final IExecutionDMContext execCtx, final ISteppingControlParticipant participant) {
    	if (DEBUG) System.out.println("[SteppingController] doneStepping participant=" + participant.getClass().getSimpleName()); //$NON-NLS-1$
    	List<ISteppingControlParticipant> participants = fStepInProgress.get(execCtx);
    	if (participants != null) {
    		participants.remove(participant);
    		if (participants.isEmpty()) {
    			doneStepping(execCtx);
    		}
    	} else {
    		for (IExecutionDMContext disabledCtx : fStepInProgress.keySet()) {
    			if (DMContexts.isAncestorOf(disabledCtx, execCtx)) {
    				participants = fStepInProgress.get(disabledCtx);
    		    	if (participants != null) {
    		    		participants.remove(participant);
    		    		if (participants.isEmpty()) {
    		    			doneStepping(disabledCtx);
    		    		}
    		    	}
    			}
    		}
    	}
    }

    @ThreadSafe
	public DsfSession getSession() {
		return fSession;
	}

	/**
	 * All access to this class should happen through this executor.
	 * @return the executor this class is confined to
	 */
    @ThreadSafe
	public DsfExecutor getExecutor() {
		return getSession().getExecutor();
	}
	
	private DsfServicesTracker getServicesTracker() {
		return fServicesTracker;
	}

	private IRunControl getRunControl() {
		if (fRunControl == null) {
	        fRunControl = getServicesTracker().getService(IRunControl.class);
	        getSession().addServiceEventListener(this, null);
		}
		return fRunControl;
	}

	/**
     * Checks whether a step command can be queued up for given context.
     */
    public void canEnqueueStep(IExecutionDMContext execCtx, StepType stepType, DataRequestMonitor<Boolean> rm) {
        if (doCanEnqueueStep(execCtx, stepType)) {
            rm.setData(true);
            rm.done();
        } else {
            getRunControl().canStep(execCtx, stepType, rm);
        }
    }

    private boolean doCanEnqueueStep(IExecutionDMContext execCtx, StepType stepType) {
        return getRunControl().isStepping(execCtx) && !isSteppingTimedOut(execCtx); 
    }

    /**
     * Check whether the next step on the given execution context should be delayed
     * based on the configured step delay.
     * 
	 * @param execCtx
	 * @return <code>true</code> if the step should be delayed
	 */
	private boolean shouldDelayStep(IExecutionDMContext execCtx) {
        final int stepDelay= getStepDelay(execCtx);
        if (DEBUG) System.out.println("[SteppingController] shouldDelayStep delay=" + stepDelay); //$NON-NLS-1$
		return stepDelay > 0;
	}

	/**
	 * Compute the delay in milliseconds before the next step for the given context may be executed.
	 * 
	 * @param execCtx
	 * @return  the number of milliseconds before the next possible step
	 */
	private int getStepDelay(IExecutionDMContext execCtx) {
	    int minStepInterval = fMinStepInterval;
		if (minStepInterval > 0) {
	        for (IExecutionDMContext lastStepCtx : fLastStepTimes.keySet()) {
	            if (execCtx.equals(lastStepCtx) || DMContexts.isAncestorOf(execCtx, lastStepCtx)) {
	                long now = System.currentTimeMillis();
					int delay= (int) (fLastStepTimes.get(lastStepCtx) + minStepInterval - now);
					return Math.max(delay, 0);
	            }
	        }
		}
        return 0;
	}

	private void updateLastStepTime(IExecutionDMContext execCtx) {
        long now = System.currentTimeMillis();
		fLastStepTimes.put(execCtx, now);
        for (IExecutionDMContext lastStepCtx : fLastStepTimes.keySet()) {
            if (!execCtx.equals(lastStepCtx) && DMContexts.isAncestorOf(execCtx, lastStepCtx)) {
				fLastStepTimes.put(lastStepCtx, now);
            }
        }
	}

	private long getLastStepTime(IExecutionDMContext execCtx) {
		if (fLastStepTimes.containsKey(execCtx)) {
			return fLastStepTimes.get(execCtx);
		}
		for (IExecutionDMContext lastStepCtx : fLastStepTimes.keySet()) {
			if (DMContexts.isAncestorOf(execCtx, lastStepCtx)) {
				return fLastStepTimes.get(lastStepCtx);
			}
		}
		return 0;
	}

	/**
     * Returns the number of step commands that are queued for given execution
     * context.
     */
    public int getPendingStepCount(IExecutionDMContext execCtx) {
        List<StepRequest> stepQueue = getStepQueue(execCtx);
        if (stepQueue == null) return 0;
        return stepQueue.size();
    }

    /**
     * Adds a step command to the execution queue for given context.
     * @param execCtx Execution context that should perform the step. 
     * @param stepType Type of step to execute.
     */
    public void enqueueStep(final IExecutionDMContext execCtx, final StepType stepType) {
    	if (DEBUG) System.out.println("[SteppingController] enqueueStep ctx=" + execCtx); //$NON-NLS-1$
        if (!shouldDelayStep(execCtx) || doCanEnqueueStep(execCtx, stepType)) {
            doEnqueueStep(execCtx, stepType);
            processStepQueue(execCtx);
        }
    }

	private void doStep(final IExecutionDMContext execCtx, final StepType stepType) {
		if (DEBUG) System.out.println("[SteppingController] doStep ctx="+execCtx); //$NON-NLS-1$
	    disableStepping(execCtx);
        updateLastStepTime(execCtx);
        
		getRunControl().step(execCtx, stepType, new RequestMonitor(getExecutor(), null) {
		    @Override
		    protected void handleSuccess() {
	            fTimedOutFlags.put(execCtx, Boolean.FALSE);
	            // We shouldn't have a stepping timeout running unless 
	            // running/stopped events are out of order.
	            assert !fTimedOutFutures.containsKey(execCtx);
	            fTimedOutFutures.put(execCtx, getExecutor().schedule(new TimeOutRunnable(execCtx), fStepTimeout, TimeUnit.MILLISECONDS));
		    }
		    
		    @Override
		    protected void handleFailure() {
		    	// in case of a failed step - enable stepping again (bug 265267)
		    	enableStepping(execCtx);
		        if (getStatus().getCode() == IDsfStatusConstants.INVALID_STATE) {
	                // Ignore errors.  During fast stepping there can be expected race
	                // conditions leading to stepping errors.
		            return;
		        } 
		        super.handleFailure();
		    }
		});
	}

	/**
	 * Enqueue the given step for later execution.
	 * 
	 * @param execCtx
	 * @param stepType
	 */
	private void doEnqueueStep(final IExecutionDMContext execCtx, final StepType stepType) {
		List<StepRequest> stepQueue = fStepQueues.get(execCtx);
		if (stepQueue == null) {
		    stepQueue = new LinkedList<StepRequest>();
		    fStepQueues.put(execCtx, stepQueue);
		}
		if (stepQueue.size() < fQueueDepth) {
		    stepQueue.add(new StepRequest(execCtx, stepType));
		}
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
    
	/**
	 * Process next step on queue if any.
	 * @param execCtx
	 */
	private void processStepQueue(final IExecutionDMContext execCtx) {
        final List<StepRequest> queue = getStepQueue(execCtx);
		if (queue != null) {
			final int stepDelay = getStepDelay(execCtx);
			if (stepDelay > 0) {
				getExecutor().schedule(new DsfRunnable() {
					public void run() {
						processStepQueue(execCtx);
					}
				}, stepDelay, TimeUnit.MILLISECONDS);
				return;
			}
            final StepRequest request = queue.get(0);
    		if (DEBUG) System.out.println("[SteppingController] processStepQueue request-in-progress="+request.inProgress); //$NON-NLS-1$
            if (!request.inProgress) {
        		if (isSteppingDisabled(request.fContext)) {
        			return;
        		}
                request.inProgress = true;
                getRunControl().canStep(
                    request.fContext, request.fStepType, 
                    new DataRequestMonitor<Boolean>(getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (isSuccess() && getData()) {
                        		queue.remove(0);
                                if (queue.isEmpty()) fStepQueues.remove(request.fContext);                               
                        		doStep(request.fContext, request.fStepType);
                            } else { 
                                // For whatever reason we can't step anymore, so clear out
                                // the step queue.
                                fStepQueues.remove(request.fContext);
                            }
                        }
                    });
            } 
        }
	}

	private List<StepRequest> getStepQueue(IExecutionDMContext execCtx) {
		List<StepRequest> queue = fStepQueues.get(execCtx);
		if (queue == null) {
	        for (IExecutionDMContext stepCtx : fStepQueues.keySet()) {
	            if (DMContexts.isAncestorOf(stepCtx, execCtx)) {
	            	queue = fStepQueues.get(stepCtx);
	            	break;
	            }
	        }
		}
		return queue;
	}

	/**
	 * Disable stepping for the given execution context.
	 * 
	 * @param execCtx
	 */
	private void disableStepping(IExecutionDMContext execCtx) {
		if (!fParticipants.isEmpty()) {
			fStepInProgress.put(execCtx, new ArrayList<ISteppingControlParticipant>(fParticipants));
		}
	}

    /**
     * Indicate that processing of the last step has completed and
     * the next step can be issued.
     * 
     * @param execCtx
     */
    private void doneStepping(final IExecutionDMContext execCtx) {
    	if (DEBUG) System.out.println("[SteppingController] doneStepping ctx=" + execCtx); //$NON-NLS-1$
        enableStepping(execCtx);
        processStepQueue(execCtx);
    }

	/**
	 * Enable stepping for the given execution context.
	 * 
	 * @param execCtx
	 */
	public void enableStepping(final IExecutionDMContext execCtx) {
        fStepInProgress.remove(execCtx);
		for (IExecutionDMContext disabledCtx : fStepInProgress.keySet()) {
			if (DMContexts.isAncestorOf(disabledCtx, execCtx)) {
				fStepInProgress.remove(disabledCtx);
			}
		}
	}

	private boolean isSteppingDisabled(IExecutionDMContext execCtx) {
        boolean disabled= fStepInProgress.containsKey(execCtx);
        if (!disabled) {
	        for (IExecutionDMContext disabledCtx : fStepInProgress.keySet()) {
				if (DMContexts.isAncestorOf(execCtx, disabledCtx)) {
					disabled = true;
					break;
				}
			}
        }
        if (disabled) {
        	long now = System.currentTimeMillis();
        	long lastStepTime = getLastStepTime(execCtx);
        	if (now - lastStepTime > MAX_STEP_DELAY) {
        		if (DEBUG) System.out.println("[SteppingController] stepping control participant(s) timed out"); //$NON-NLS-1$
        		enableStepping(execCtx);
        		disabled = false;
        	}
        }
        return disabled;
	}

	protected void handlePropertyChanged(final IPreferenceStore store, final PropertyChangeEvent event) {
		String property = event.getProperty();
		if (IDsfDebugUIConstants.PREF_MIN_STEP_INTERVAL.equals(property)) {
			setMinimumStepInterval(store.getInt(property));
		}
	}


    ///////////////////////////////////////////////////////////////////////////

    @DsfServiceEventHandler 
    public void eventDispatched(final ISuspendedDMEvent e) {
        // Take care of the stepping time out
    	boolean timedOut = false;
        IExecutionDMContext dmc = e.getDMContext();
        for (Iterator<Map.Entry<IExecutionDMContext, Boolean>> itr = fTimedOutFlags.entrySet().iterator(); itr.hasNext();) {
            Map.Entry<IExecutionDMContext,Boolean> entry = itr.next();
            IExecutionDMContext nextDmc = entry.getKey();
            if (nextDmc.equals(dmc) || DMContexts.isAncestorOf(nextDmc, dmc)) {
                if (entry.getValue()) {
                    // after step timeout do not process queued steps
                    fStepQueues.remove(dmc);
                    timedOut = true;
                }
                itr.remove();
            }
        }
        
        for (Iterator<Map.Entry<IExecutionDMContext, ScheduledFuture<?>>> itr = fTimedOutFutures.entrySet().iterator(); itr.hasNext();) {
            Map.Entry<IExecutionDMContext, ScheduledFuture<?>> entry = itr.next();
            IExecutionDMContext nextDmc = entry.getKey();
            if (nextDmc.equals(dmc) || DMContexts.isAncestorOf(entry.getKey(), dmc)) {
                entry.getValue().cancel(false);
                itr.remove();
            }            
        }
        
        if (e.getReason() != StateChangeReason.STEP) {
        	// after any non-step suspend reason do not process queued steps for given context
        	fStepQueues.remove(dmc);
        } else if (!timedOut){
        	// Check if there's a step pending, if so execute it
        	processStepQueue(dmc);
        }
    }

    @DsfServiceEventHandler 
    public void eventDispatched(final IResumedDMEvent e) {
        if (e.getReason().equals(StateChangeReason.STEP)) {
            final IExecutionDMContext dmc = e.getDMContext();
            fTimedOutFlags.put(dmc, Boolean.FALSE);
            
            
            // Find any time-out futures for contexts that are children of the 
            // resumed context, and cancel them as they'll be replaced.
            for (Iterator<Map.Entry<IExecutionDMContext, ScheduledFuture<?>>> itr = fTimedOutFutures.entrySet().iterator(); itr.hasNext();) {
                Map.Entry<IExecutionDMContext, ScheduledFuture<?>> entry = itr.next();
                if (DMContexts.isAncestorOf(entry.getKey(), dmc) && !dmc.equals(entry.getKey())) {
                    entry.getValue().cancel(false);
                    itr.remove();
                }            
            }
            
            fTimedOutFutures.put(dmc, getExecutor().schedule(new TimeOutRunnable(dmc), fStepTimeout, TimeUnit.MILLISECONDS));
        } 
    }    
}