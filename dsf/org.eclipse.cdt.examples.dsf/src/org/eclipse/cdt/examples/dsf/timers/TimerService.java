/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.timers;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.DsfExamplesPlugin;
import org.osgi.framework.BundleContext;

/**
 * Timer service tracks a set of timers, which are created per user request.
 * The timers are represented using a Data Model context object, which 
 * implements {@link IDMContext}.  Each timers value, which can be retrieved
 * by calling {@link #getTimerValue(TimerDMContext)}, is incremented every 
 * second.  When a timer value is incremented the TimerService issues a 
 * {@link TimerTickDMEvent}.
 */
public class TimerService extends AbstractDsfService 
{
    /** Event indicating that the list of timers is changed. */
    @Immutable
    public static class TimersChangedEvent  {}
    
    /** Data Model context representing a timer. */
    @Immutable
    public static class TimerDMContext extends AbstractDMContext {
        final int fNumber;
        
        public TimerDMContext(String sessionId, int timer) {
            super(sessionId, new IDMContext[0]);
            fNumber = timer;
        }
        
        /** Returns the sequential creation number of this timer. */
        public int getTimerNumber() {
            return fNumber;
        }
        
        // Timer context objects are created as needed and not cached, so the 
        // equals method implementation is critical.
        @Override
        public boolean equals(Object other) {
            return baseEquals(other) && 
                ((TimerDMContext)other).fNumber == fNumber;
        }
        
        @Override
        public int hashCode() { return baseHashCode() + fNumber; } 
        
        @Override
        public String toString() {
            return baseToString() + ".timer[" + fNumber + "]";
        }
    }            
    
    /**
     * Event indicating that a timer's value has incremented.  The context in 
     * the event points to the timer that has changed.   
     */
    public class TimerTickDMEvent extends AbstractDMEvent<TimerDMContext> {
        public TimerTickDMEvent(TimerDMContext context) {
            super(context);
        }
    }

    /** Counter for generating timer numbers */
    private int fTimerNumberCounter = 1;
    
    // Use a linked hash in order to be able to return an ordered list of timers.
    private Map<TimerDMContext, Integer> fTimers = 
        new LinkedHashMap<TimerDMContext, Integer>();
    
    private Map<TimerDMContext, Future<?>> fTimerFutures = 
        new HashMap<TimerDMContext, Future<?>>();
    
    
    TimerService(DsfSession session) {
        super(session);
    }
    
    @Override 
    protected BundleContext getBundleContext() {
        return DsfExamplesPlugin.getDefault().getBundle().getBundleContext();
    }    

    @Override 
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
            new RequestMonitor(getExecutor(), requestMonitor) { 
                @Override
                public void handleSuccess() {
                    // After super-class is finished initializing
                    // perform TimerService initialization.
                    doInitialize(requestMonitor);
                }});
    }

    private void doInitialize(RequestMonitor requestMonitor) {
        // Register service
        register( new String[]{ TimerService.class.getName() }, 
            new Hashtable<String,String>() );
        requestMonitor.done();
    }

    @Override 
    public void shutdown(RequestMonitor requestMonitor) {
        // Cancel timer futures to avoid firing more events.
        for (Future<?> future : fTimerFutures.values()) {
            future.cancel(false);
        }
        unregister();
        super.shutdown(requestMonitor);
    }
    
    /** Retrieves the list of timer contexts. */
    public TimerDMContext[] getTimers() {
        return fTimers.keySet().toArray(new TimerDMContext[fTimers.size()]);
    }

    /** Retrieves the timer value for the given context. */
    public int getTimerValue(TimerDMContext context) {
        Integer value = fTimers.get(context);
        if (value != null) {
            return value;
        } 
        return -1;
    }
    
    /** Creates a new timer and returns its context. */
    public TimerDMContext startTimer() {
        // Create a new timer context and add it to the internal list.
        final TimerDMContext newTimer = 
            new TimerDMContext(getSession().getId(), fTimerNumberCounter++); 
        fTimers.put(newTimer, 0);

        // Create a new runnable that will execute every second and increment
        // the timer value.  The returned future is the handle that allows 
        // for canceling the scheduling of the runnable.
        Future<?> timerFuture = getExecutor().scheduleAtFixedRate(
            new Runnable() {
                public void run() {
                    fTimers.put(newTimer, fTimers.get(newTimer) + 1);
                    getSession().dispatchEvent(new TimerTickDMEvent(newTimer), getProperties());
                }
                @Override
                public String toString() { return "Scheduled timer runnable for timer " + newTimer; } //$NON-NLS-1$
            }, 
            1, 1, TimeUnit.SECONDS);
        fTimerFutures.put(newTimer, timerFuture);
        
        // Issue an event to allow clients to update the list of timers.
        getSession().dispatchEvent(new TimersChangedEvent(), getProperties());
        return newTimer;
    }
    
    /** Removes given timer from list of timers. */
    public void killTimer(TimerDMContext timerContext) {
        if (fTimers.containsKey(timerContext)) {
            fTimers.remove(timerContext);
            fTimerFutures.remove(timerContext).cancel(false);
        }
        getSession().dispatchEvent(new TimersChangedEvent(), getProperties());
    }
}
