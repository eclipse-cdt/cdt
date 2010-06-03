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

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.DsfExamplesPlugin;
import org.eclipse.cdt.examples.dsf.timers.TimerService.TimerDMContext;
import org.eclipse.cdt.examples.dsf.timers.TimerService.TimerTickDMEvent;
import org.osgi.framework.BundleContext;

/**
 * The alarm service tracks triggers and alarms.  Triggers have a specified
 * value and can be created and removed independently.  Alarms are created
 * for a specific timer and a trigger, and can indicate whether an alarm is
 * triggered. 
 * <p>
 * This service depends on the TimerService, so the TimerService has to be
 * initialized before this service is initialized. 
 * </p>
 */
public class AlarmService extends AbstractDsfService
{
    /** Event indicating that the list of triggers is changed. */
    @Immutable
    public static class TriggersChangedEvent {}

    /** Context representing an alarm tracked by this service. */
    @Immutable
    public static class TriggerDMContext extends AbstractDMContext {
        /** Alarm number, also index into alarm map */
        final int fNumber;
        
        private TriggerDMContext(String sessionId, int number) {
            super(sessionId, new IDMContext[0]);
            fNumber = number;
        }
        
        @Override
        public boolean equals(Object other) {
            return baseEquals(other) && 
                ((TriggerDMContext)other).fNumber == fNumber;
        }
        
        public int getTriggerNumber() {
            return fNumber;
        }
        
        @Override
        public int hashCode() { 
            return baseHashCode() + fNumber; 
        }
        
        @Override
        public String toString() { 
            return baseToString() + ".trigger[" + fNumber + "]"; 
        }
    }            
    
    /**
     * Context representing the "triggered" status of an alarm with respect to 
     * a specific timer.   
     */
    @Immutable
    public static class AlarmDMContext extends AbstractDMContext {
        // An alarm requires both a timer and alarm context, both of which
        // become parents of the alarm context.
        // Note: beyond the parent contexts this context does not contain
        // any other data, because no other data is needed.
        private AlarmDMContext(String sessionId, 
            TimerDMContext timerCtx, TriggerDMContext alarmCtx) 
        {
            super(sessionId, new IDMContext[] { timerCtx, alarmCtx });
        }

        @Override
        public boolean equals(Object other) { return baseEquals(other); }
        
        @Override
        public int hashCode() { return baseHashCode(); }
        
        @Override
        public String toString() {
            return baseToString() + ":alarm"; //$NON-NLS-1$
        }        
    }            
    
    /**
     * Event indicating that an alarm has been triggered by a timer. 
     */
    public class AlarmTriggeredDMEvent extends AbstractDMEvent<AlarmDMContext> {
        public AlarmTriggeredDMEvent(AlarmDMContext context) {
            super(context);
        }
    }

    private int fTriggerNumberCounter = 1;
    private Map<TriggerDMContext, Integer> fTriggers = 
        new LinkedHashMap<TriggerDMContext, Integer>();
    
    AlarmService(DsfSession session) {
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
                protected void handleSuccess() {
                    // After super-class is finished initializing
                    // perform TimerService initialization.
                    doInitialize(requestMonitor);
                }});
    }
            
    private void doInitialize(RequestMonitor requestMonitor) {
        // Add this class as a listener for service events, in order to receive 
        // TimerTickEvent events.
        getSession().addServiceEventListener(this, null);
        
        // Register service
        register(new String[]{AlarmService.class.getName()}, new Hashtable<String,String>());
        
        requestMonitor.done();
    }

    @Override 
    public void shutdown(RequestMonitor requestMonitor) {
        getSession().removeServiceEventListener(this);
        unregister();
        super.shutdown(requestMonitor);
    }

    public boolean isValid() { return true; }
    
    @DsfServiceEventHandler
    public void eventDispatched(TimerTickDMEvent event) {
        final TimerDMContext timerContext = event.getDMContext();
        
        int timerValue = getServicesTracker().getService(TimerService.class).
            getTimerValue(event.getDMContext());
        
        //  If a timer triggers an alarm, this  service needs to issue an alarm 
        // triggered event.
        checkAlarmsForTimer(timerContext, timerValue);
    }

    private void checkAlarmsForTimer(TimerDMContext timerContext, int timerValue) {
        // Check the existing alarms for whether they are triggered by given 
        // timer.  
        for (Map.Entry<TriggerDMContext, Integer> entry : fTriggers.entrySet()) {
            if (timerValue == entry.getValue()) {
                // Generate the AlarmTriggeredEvent
                AlarmDMContext alarmCtx = new AlarmDMContext(
                    getSession().getId(), timerContext, entry.getKey());
                getSession().dispatchEvent( 
                    new AlarmTriggeredDMEvent(alarmCtx), getProperties());
            }
        }
    }

    
    /** Returns the list of triggers. */
    public TriggerDMContext[] getTriggers() {
        return fTriggers.keySet().toArray(new TriggerDMContext[fTriggers.size()]);
    }

    /** Returns the trigger value. */
    public int getTriggerValue(TriggerDMContext alarmCtx) {
        Integer value = fTriggers.get(alarmCtx);
        if (value != null) {
            return value;
        } else {
            return -1;
        }
    }

    /** Returns the alarm context for given timer and trigger contexts. */
    public AlarmDMContext getAlarm(TriggerDMContext alarmCtx, TimerDMContext timerCtx) {
        return new AlarmDMContext(getSession().getId(), timerCtx, alarmCtx);
    }

    /** Returns true if the given alarm is triggered */
    public boolean isAlarmTriggered(AlarmDMContext alarmCtx) {
        // Extract the timer and trigger contexts.  They should always be part 
        // of the alarm.
        TimerService.TimerDMContext timerCtx = DMContexts.getAncestorOfType(
            alarmCtx, TimerService.TimerDMContext.class);
        TriggerDMContext triggerCtx = DMContexts.getAncestorOfType(
            alarmCtx, TriggerDMContext.class);

        assert triggerCtx != null && timerCtx != null;

        // Find the trigger and check whether the timers value has surpassed it. 
        if (fTriggers.containsKey(triggerCtx)) {
            int timerValue = getServicesTracker().getService(TimerService.class).
                getTimerValue(timerCtx);
            
            return timerValue >= fTriggers.get(triggerCtx);
        }
        
        return false;
    }
    
    /** Creates a new alarm object with given value. */
    public TriggerDMContext createTrigger(int value) {
        TriggerDMContext triggerCtx = 
            new TriggerDMContext(getSession().getId(), fTriggerNumberCounter++);
        fTriggers.put(triggerCtx, value);
        getSession().dispatchEvent(new TriggersChangedEvent(), getProperties());
        return triggerCtx;
    }    
    
    /** Removes given alarm from service. */
    public void deleteTrigger(TriggerDMContext alarmCtx) {
        fTriggers.remove(alarmCtx);
        getSession().dispatchEvent(new TriggersChangedEvent(), getProperties());
    }
    
    /** Changes the value of the given trigger. */
    public void setTriggerValue(TriggerDMContext ctx, int newValue) {
        if (fTriggers.containsKey(ctx)) {
            fTriggers.put(ctx, newValue);
        }
        getSession().dispatchEvent(new TriggersChangedEvent(), getProperties());
    }
}
