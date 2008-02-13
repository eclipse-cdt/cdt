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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;
import org.eclipse.dd.examples.dsf.timers.TimerService.TimerDMC;
import org.eclipse.dd.examples.dsf.timers.TimerService.TimerData;
import org.eclipse.dd.examples.dsf.timers.TimerService.TimerTickEvent;
import org.osgi.framework.BundleContext;

/**
 * Alarm service tracks a set of alarm objects which are occacionally
 * triggered by the timers from the TimerService. 
 * <p>
 * This service depends on the TimerService, so the TimerService has to be
 * running before this service is initialized.  However, the alarm objects
 * themeselves do not depend on the timers, they can be listed, created, 
 * removed without any timers present.  So a separate context object exists
 * to track alarm status, which requires both an alarm and a timer in order
 * to exist. 
 */
public class AlarmService extends AbstractDsfService
    implements IDMService
{
    /**
     * Event indicating that the list of alarms is changed and the clients 
     * which display alarms should re-query this list. 
     */
    public class AlarmsChangedEvent extends AbstractDMEvent<IDMContext> {
        AlarmsChangedEvent() { super(fAlarmsContext); }
    }

    /**
     * Context representing an alarm tracked by this service.
     */
    public static class AlarmDMC extends AbstractDMContext {
        /** Alarm number, also index into alarm map */
        final int fAlarm;
        
        public AlarmDMC(AlarmService service, int alarm) {
            super(service, new IDMContext[] { service.fAlarmsContext });
            fAlarm = alarm;
        }
        
        @Override
        public boolean equals(Object other) {
            return baseEquals(other) && ((AlarmDMC)other).fAlarm == fAlarm;
        }
        
        @Override
        public int hashCode() { return baseHashCode() + fAlarm; }
        @Override
        public String toString() { return baseToString() + ".alarm[" + fAlarm + "]"; } //$NON-NLS-1$ //$NON-NLS-2$
    }            
    
    /**
     * Data object containing information about the alarm.   This object 
     * references internal service data, so it has to guard agains this data 
     * being obsolete.
     */
    public class AlarmData implements IDMData {
        private int fAlarmNumber;
        
        AlarmData(int alarmNumber) { fAlarmNumber = alarmNumber; }
        public boolean isValid() { return fAlarms.containsKey(fAlarmNumber); }
        public int getAlarmNumber() { return fAlarmNumber; }
        
        public int getTriggeringValue() {
            if (!isValid()) return -1;
            return fAlarms.get(fAlarmNumber);
        }
    }

    /**
     * Context representing the "triggered" status of an alarm with respect to 
     * a specific timer.  Having this object separate from the alarm itself 
     * allows the alarm object to exist independently of the timers. 
     */
    public class AlarmStatusContext extends AbstractDMContext {
        /**
         * An alarm status requires both a timer and alarm context, both of which
         * become parents of the status context.
         */
        public AlarmStatusContext(AbstractDsfService service, TimerDMC timerCtx, AlarmDMC alarmCtx) {
            super(service.getSession().getId(), new IDMContext[] { timerCtx, alarmCtx });
        }

        @Override
        public boolean equals(Object other) { return baseEquals(other); }
        @Override
        public int hashCode() { return baseHashCode(); }         
        @Override
        public String toString() {
            return baseToString() + ":alarm_status"; //$NON-NLS-1$
        }        
    }            
    
    /**
     * Data about alarm status.  No surprises here.
     *
     */
    public class AlarmStatusData implements IDMData {
        private boolean fIsTriggered;

        public boolean isValid() { return true; }
        AlarmStatusData(boolean triggered) { fIsTriggered = triggered; }
        public boolean isTriggered() { return fIsTriggered; }
    }
    
    /**
     * Event indicating that an alarm has been triggered by a timer.  The
     * status context object's parents indicate which alarm and timer are 
     * involved.
     */
    public class AlarmTriggeredEvent extends AbstractDMEvent<AlarmStatusContext> {
        public AlarmTriggeredEvent(AlarmStatusContext context) {
            super(context);
        }
    }

    
    /** Parent context for all alarms */
    private final IDMContext fAlarmsContext;

    /** Counter for generating alarm numbers */
    private int fAlarmCounter = 1;

    /** Map holding the alarms */
    private Map<Integer,Integer> fAlarms = new TreeMap<Integer,Integer>();
    
    /** Constructor requires only the session for this service */
    AlarmService(DsfSession session) {
        super(session);
        fAlarmsContext = new AbstractDMContext(this, new IDMContext[0]) {
            private final Object fHashObject = new Object();
            
            @Override
            public boolean equals(Object obj) { return (this == obj); };
            
            @Override
            public int hashCode() { return fHashObject.hashCode(); }
            
            @Override
            public String toString() { return "#alarms"; } //$NON-NLS-1$
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
                protected void handleOK() {
                    doInitialize(requestMonitor);
                }});
    }
            
    /** 
     * Initialization routine registers the service, and adds it as a listener 
     * to service events. 
     */
    private void doInitialize(RequestMonitor requestMonitor) {
        getSession().addServiceEventListener(this, null);
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
    
    @SuppressWarnings("unchecked")
    public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
        if (dmc instanceof AlarmDMC) {
            getAlarmData((AlarmDMC)dmc, (DataRequestMonitor<AlarmData>)rm);
            return;
        } else if (dmc instanceof AlarmStatusContext) {
            getAlarmStatusData((AlarmStatusContext)dmc, (DataRequestMonitor<AlarmStatusData>)rm);
            return;
        } else if (dmc == fAlarmsContext) {
            ((DataRequestMonitor<AlarmService>)rm).setData(this);
        } else {
            rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
        }
        rm.done();            
    }
    
    /**
     * Listener for timer ticks events.  If a timer triggers an alarm, this 
     * service needs to issue an alarm triggered event.
     * @param event
     */
    @DsfServiceEventHandler
    public void eventDispatched(TimerTickEvent event) {
        final TimerDMC timerContext = event.getDMContext();
        
        getServicesTracker().getService(TimerService.class).getTimerData(
            event.getDMContext(), 
            new DataRequestMonitor<TimerData>(getExecutor(), null) { 
                @Override
                protected void handleCompleted() {
                    if (!getStatus().isOK()) return;
                    checkAlarmsForTimer(timerContext, getData().getTimerValue());
                }
                @Override public String toString() { return "Got timer data: " + getData(); } //$NON-NLS-1$
            });
    }

    /**
     * Checks the existing alarms for whether they are triggered by given timer.  
     * @param timerContext Context of the timer that is changed.
     * @param timerValue Current value of the timer.
     */
    private void checkAlarmsForTimer(TimerDMC timerContext, int timerValue) {
        for (Map.Entry<Integer,Integer> entry : fAlarms.entrySet()) {
            if (timerValue == entry.getValue()) {
                getSession().dispatchEvent(new AlarmTriggeredEvent(
                    new AlarmStatusContext(this, timerContext, new AlarmDMC(this, entry.getKey()))),
                    getProperties());
            }
        }
    }

    
    /**
     * Retrieves the list of alarm contexts.
     * 
     * <br>Note: this method doesn't need to be asynchronous, because all the 
     * data is stored locally.  But using an asynchronous method makes this a
     * more applicable example.
     *   
     * @param rm Return data token.
     */
    public void getAlarms(DataRequestMonitor<AlarmDMC[]> rm) {
        AlarmDMC[] alarmContexts = new AlarmDMC[fAlarms.size()];
        int i = 0;
        for (int alarm : fAlarms.keySet()) {
            alarmContexts[i++] = new AlarmDMC(this, alarm);
        }
        rm.setData(alarmContexts);
        rm.done();
    }

    /**
     * Retrieves the data object for given alarm context.
     * 
     * <br>Note: likewise this method doesn't need to be asynchronous.
     */
    public void getAlarmData(AlarmDMC alarmCtx, DataRequestMonitor<AlarmData> rm) {
        if (!fAlarms.containsKey(alarmCtx.fAlarm)) {
            rm.setStatus(new Status(
                IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, INVALID_HANDLE, "Alarm context invalid", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        rm.setData(new AlarmData(alarmCtx.fAlarm));
        rm.done();
    }

    /**
     * Returns the alarm status context object, for given timer and alarms.  
     * 
     * <br>Note: this method is synchronous... for variety.
     */
    public AlarmStatusContext getAlarmStatus(AlarmDMC alarmCtx, TimerDMC timerCtx) {
        return new AlarmStatusContext(this, timerCtx, alarmCtx);
    }

    /**
     * Returns the data object for given alarm status object.
     */
    public void getAlarmStatusData(AlarmStatusContext alarmStatusCtx, final DataRequestMonitor<AlarmStatusData> rm) {
        final TimerService.TimerDMC timerCtx = DMContexts.getAncestorOfType(
            alarmStatusCtx, TimerService.TimerDMC.class);
        final AlarmDMC alarmCtx = DMContexts.getAncestorOfType(
            alarmStatusCtx, AlarmDMC.class);

        assert alarmCtx != null && timerCtx != null;
        
        getServicesTracker().getService(TimerService.class).getTimerData(
            timerCtx,
            new DataRequestMonitor<TimerData>(getExecutor(), rm) { 
                @Override
                protected void handleOK() {
                    if (!fAlarms.containsKey(alarmCtx.fAlarm)) {
                        rm.setStatus(new Status(
                            IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, INVALID_HANDLE, "Alarm context invalid", null)); //$NON-NLS-1$
                        rm.done();
                        return;
                    }
                    boolean isTriggered = getData().getTimerValue() >= fAlarms.get(alarmCtx.fAlarm);
                    rm.setData(new AlarmStatusData(isTriggered));
                    rm.done();
                }
            });
    }
    
    /**
     * Creates a new alarm object with given value.
     * @return context of the new alarm.
     */
    public AlarmDMC createAlarm(int value) {
        int newAlarm = fAlarmCounter++; 
        fAlarms.put(newAlarm, value);
        getSession().dispatchEvent(new AlarmsChangedEvent(), getProperties());
        return new AlarmDMC(this, newAlarm);
    }    
    
    /** Removes given alarm from service. */
    public void deleteAlarm(AlarmDMC alarmCtx) {
        fAlarms.remove(alarmCtx.fAlarm);
        getSession().dispatchEvent(new AlarmsChangedEvent(), getProperties());
    }
    
    /**
     * Changes the value of the given alarm.  
     * @param dmc Alarm to change
     * @param newValue New alarm value.
     */
    public void setAlarmValue(AlarmDMC dmc, int newValue) {
        if (fAlarms.containsKey(dmc.fAlarm)) {
            fAlarms.put(dmc.fAlarm, newValue);
        }
        getSession().dispatchEvent(new AlarmsChangedEvent(), getProperties());
    }
}
