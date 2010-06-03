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

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.examples.dsf.DsfExamplesPlugin;
import org.eclipse.cdt.examples.dsf.timers.AlarmService.AlarmDMContext;
import org.eclipse.cdt.examples.dsf.timers.AlarmService.TriggerDMContext;
import org.eclipse.cdt.examples.dsf.timers.TimerService.TimerDMContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * View model node that determines whether an "alarm triggered" indicator is 
 * shown in the tree.  This indicator is only shown if a given alarm is 
 * triggered for a given timer.
 * 
 * @see AlarmDMContext
 */
@SuppressWarnings("restriction")
class AlarmsVMNode extends AbstractDMVMNode 
    implements IElementLabelProvider
{
    public AlarmsVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, AlarmDMContext.class);
    }
    
    @Override
    public String toString() {
        return "AlarmsVMNode(" + getSession().getId() + ")";
    }
    

    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        // Check that the service is available and find the trigger and timer contexts.  
        // If not found, fail.
        AlarmService alarmService = getServicesTracker().getService(AlarmService.class, null); 
        TriggerDMContext alarmDmc = findDmcInPath(
            update.getViewerInput(), update.getElementPath(), TriggerDMContext.class);
        TimerDMContext timerDmc = findDmcInPath(
            update.getViewerInput(), update.getElementPath(), TimerDMContext.class);
        if (alarmService == null || alarmDmc == null || timerDmc == null) {
            update.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, "Required elements not found in path"));
            update.done();
            return;
        }
        
        // Get the alarm context then check the triggered value.  
        final AlarmDMContext alarmStatusDmc = alarmService.getAlarm(alarmDmc, timerDmc);
        boolean triggered = alarmService.isAlarmTriggered(alarmStatusDmc); 
        
        // Only return the alarm in list of elements if it is triggered.
        if (triggered) {
            update.setChild(createVMContext(alarmStatusDmc), 0);
        }
        update.done();
    }

    public void update(ILabelUpdate[] updates) {
        for (ILabelUpdate update : updates) {
            update.setLabel("ALARM TRIGGERED", 0);
            update.setImageDescriptor(
                DsfExamplesPlugin.getDefault().getImageRegistry().getDescriptor(
                    DsfExamplesPlugin.IMG_ALARM_TRIGGERED), 
                0);
            update.done();
        }
    }
    
    
    public int getDeltaFlags(Object e) {
        if (e instanceof AlarmService.AlarmTriggeredDMEvent) {
            return IModelDelta.ADDED | IModelDelta.SELECT | IModelDelta.EXPAND;
        }
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor requestMonitor) {
        // The alarm element is added when and selected upon a triggered event.  
        // Parent element is also expanded allow the alarm to be selected.
        if (e instanceof AlarmService.AlarmTriggeredDMEvent) {
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.EXPAND);
            parentDelta.addNode(
                createVMContext( ((AlarmService.AlarmTriggeredDMEvent)e).getDMContext() ),
                0,
                IModelDelta.ADDED | IModelDelta.SELECT);
        }
        requestMonitor.done();
    }
}
