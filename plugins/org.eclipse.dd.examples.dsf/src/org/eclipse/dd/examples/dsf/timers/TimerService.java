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
package org.eclipse.dd.examples.dsf.timers;

import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;
import org.osgi.framework.BundleContext;

/**
 * Timer service tracks a set of timers, which are created per user request.
 * The timers and their data are provided by the service using the DSF data 
 * model interfaces. 
 * <p>
 * When each timer is created, an event is issued that the service contents are
 * changed, and clients should re-query the list of timers.  The timers 
 * increment their value at rate of one per second (but they are not synchronous),
 * and an event is issued for every tick.      
 */
public class TimerService extends AbstractDsfService 
    implements IDMService
{
    /**
     * Event indicating that the list of timers is changed and the clients 
     * which display timers should re-query this list. 
     */
    public class TimersChangedEvent extends AbstractDMEvent<IDMContext> {
        TimersChangedEvent() { super(fTimersContext); }
    }
    
    /**
     * Timer context represents a timer in this service.  Clients can use this
     * context to retrieve timer data.  This class implements the <code>Comaparable</code>
     * interfaces so that the objects can be stored in a TreeMap, which keeps them sorted. 
     */
    public static class TimerDMC extends AbstractDMContext 
        implements Comparable<TimerDMC>
    {
        /**
         * Timer number, which is also index to timers map.
         */
        final int fTimer;
        
        public TimerDMC(TimerService service, int timer) {
            super(service, new IDMContext[] { service.fTimersContext });
            fTimer = timer;
        }
        
        /**
         * Timer context objects are created as needed and not cached, so the 
         * equals method implementation is critical.
         */
        @Override
        public boolean equals(Object other) {
            return baseEquals(other) && ((TimerDMC)other).fTimer == fTimer;
        }
        
        @Override
        public int hashCode() { return baseHashCode() + fTimer; } 
        
        @Override
        public String toString() {
            return baseToString() + ".timer[" + fTimer + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        public int compareTo(TimerDMC other) {
            TimerDMC otherTimer = other;
            return (fTimer < otherTimer.fTimer ? -1 : (fTimer == otherTimer.fTimer ? 0 : 1));
        }
    }            
    
    /**
     * Data about the timer in the service.  This object references internal
     * service data, so it has to guard agains this data being obsolete.
     */
    public class TimerData implements IDMData {
        TimerDMC fTimerDMC;
        
        TimerData(TimerDMC timer) { fTimerDMC = timer; }
        public boolean isValid() { return fTimers.containsKey(fTimerDMC); }
        public int getTimerNumber() { return fTimerDMC.fTimer; }
        
        public int getTimerValue() {
            if (!isValid()) return -1;
            return fTimers.get(fTimerDMC);
        }
        
        @Override public String toString() { return "Timer " + fTimerDMC.fTimer + " = " + getTimerValue(); } //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Event indicating that a timer's value has incremented.  The context in 
     * the event points to the timer that has changed.   
     */
    public class TimerTickEvent extends AbstractDMEvent<TimerDMC> {
        public TimerTickEvent(TimerDMC context) {
            super(context);
        }
    }

    /** Parnet context for all timers */
    private final IDMContext fTimersContext;
    
    /** Counter for generating timer numbers */
    private int fTimerCounter = 1;
    
    /** Map holding the timers */
    private Map<TimerDMC, Integer> fTimers = new TreeMap<TimerDMC, Integer>();
    
    private Map<TimerDMC, Future<?>> fTimerFutures = new TreeMap<TimerDMC, Future<?>>();
    
    /** Constructor requires only the session for this service */
    TimerService(DsfSession session) {
        super(session);
        fTimersContext = new AbstractDMContext(this, new IDMContext[0]) {
            private final Object fHashObject = new Object();
            
            @Override
            public boolean equals(Object obj) { return (this == obj); };
            
            @Override
            public int hashCode() { return fHashObject.hashCode(); }
            
            @Override
            public String toString() { return "#timers"; } //$NON-NLS-1$
        };
    }
    
    @Override 
    protected BundleContext getBundleContext() {
        return DsfExamplesPlugin.getBundleContext();
    }    

    @Override 
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
            new RequestMonitor(getExecutor(), requestMonitor) { 
                @Override
                public void handleOK() {
                    doInitialize(requestMonitor);
                }});
    }

    @Override 
    public void shutdown(RequestMonitor requestMonitor) {
        /* 
         * Go through all the timer futures and cancel them, so that they 
         * don't fire any more events.
         */
        for (Future<?> future : fTimerFutures.values()) {
            future.cancel(false);
        }
        unregister();
        super.shutdown(requestMonitor);
    }

    /**
     * Performs the relevant initialization for this service: registering and
     * scheduling the timer.
     * @param requestMonitor
     */
    private void doInitialize(RequestMonitor requestMonitor) {
        register(new String[]{TimerService.class.getName()}, new Hashtable<String,String>());
        requestMonitor.done();
    }

    public boolean isValid() { return true; }
    
    @SuppressWarnings("unchecked")
    public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
        if (dmc instanceof TimerDMC) {
            getTimerData((TimerDMC)dmc, (DataRequestMonitor<TimerData>)rm);
            return;
        } else if (dmc == fTimersContext) {
            ((DataRequestMonitor<TimerService>)rm).setData(this);
        } else {
            rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
        }
        rm.done();            
    }
    
    /**
     * Retrieves the list of timer contexts.
     * 
     * <br>Note: this method doesn't need to be asynchronous, because all the 
     * data is stored locally.  But using an asynchronous method makes this a
     * more applicable example.
     *   
     * @param rm Return data token.
     */
    public void getTimers(DataRequestMonitor<TimerDMC[]> rm) {
        rm.setData( fTimers.keySet().toArray(new TimerDMC[fTimers.size()]) );
        rm.done();
    }

    /**
     * Retrieves the data object for given timer context.
     * 
     * <br>Note: likewise this method doesn't need to be asynchronous.
     */
    public void getTimerData(TimerDMC context, DataRequestMonitor<TimerData> rm) {
        rm.setData(new TimerData(context));
        rm.done();        
    }
    
    /**
     * Creates a new timer and returns its context.
     */
    public TimerDMC startTimer() {
        final TimerDMC newTimer = new TimerDMC(this, fTimerCounter++); 
        fTimers.put(newTimer, 0);
        Future<?> timerFuture = getExecutor().scheduleAtFixedRate(
            new Runnable() {
                public void run() {
                    fTimers.put(newTimer, fTimers.get(newTimer) + 1);
                    getSession().dispatchEvent(new TimerTickEvent(newTimer), getProperties());
                }
                @Override
                public String toString() { return "Scheduled timer runnable for timer " + newTimer; } //$NON-NLS-1$
            }, 
            1, 1, TimeUnit.SECONDS);
        fTimerFutures.put(newTimer, timerFuture); 
        getSession().dispatchEvent(new TimersChangedEvent(), getProperties());
        return newTimer;
    }
    
    /**
     * Removes given timer from list of timers.
     */
    public void killTimer(TimerDMC timerContext) {
        if (fTimers.containsKey(timerContext)) {
            fTimers.remove(timerContext);
            fTimerFutures.remove(timerContext).cancel(false);
        }
        getSession().dispatchEvent(new TimersChangedEvent(), getProperties());
    }
}
