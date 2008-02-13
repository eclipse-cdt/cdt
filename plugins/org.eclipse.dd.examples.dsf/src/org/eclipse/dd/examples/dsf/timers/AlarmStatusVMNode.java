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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;
import org.eclipse.dd.examples.dsf.timers.AlarmService.AlarmDMC;
import org.eclipse.dd.examples.dsf.timers.AlarmService.AlarmStatusContext;
import org.eclipse.dd.examples.dsf.timers.AlarmService.AlarmStatusData;
import org.eclipse.dd.examples.dsf.timers.TimerService.TimerDMC;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * View model node that determines whether an "alarm triggered" indicator is 
 * shown in the tree.  This indicator is only shown if a given alarm is 
 * triggered for a given timer.
 * 
 * @see AlarmStatusContext
 */
@SuppressWarnings("restriction")
class AlarmStatusVMNode extends AbstractDMVMNode 
    implements IElementLabelProvider
{
    public AlarmStatusVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, AlarmStatusContext.class);
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        if (!checkService(AlarmService.class, null, update)) return;
        if (!checkService(TimerService.class, null, update)) return;

        AlarmDMC alarmDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), AlarmDMC.class);
        TimerDMC timerDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), TimerDMC.class);
        if (alarmDmc == null || timerDmc == null) {
            update.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, "Required elements not found in path")); //$NON-NLS-1$
            update.done();
            return;
        }
        
        // Get the alarm status DMC then check the triggered value to make sure it's triggered.
        final AlarmStatusContext alarmStatusDmc = getServicesTracker().getService(AlarmService.class).
            getAlarmStatus(alarmDmc, timerDmc);
        getServicesTracker().getService(AlarmService.class).getAlarmStatusData(
            alarmStatusDmc, 
            new DataRequestMonitor<AlarmStatusData>(getSession().getExecutor(), null) { 
                @Override
                public void handleCompleted() {
                    if (isDisposed()) return;
                    if (!getStatus().isOK()) {
                        update.setStatus(getStatus());
                    } else {
                        if (getData().isTriggered()) {
                            update.setChild(createVMContext(alarmStatusDmc), 0);
                        } 
                    }
                    update.done();
                }});
    }

    public void update(ILabelUpdate[] updates) {
        for (ILabelUpdate update : updates) {
            update.setLabel("ALARM TRIGGERED", 0); //$NON-NLS-1$
            update.setImageDescriptor(
                DsfExamplesPlugin.getDefault().getImageRegistry().getDescriptor(
                    DsfExamplesPlugin.IMG_ALARM_TRIGGERED), 
                0);
            update.done();
        }
    }
    
    
    public int getDeltaFlags(Object e) {
        // This node generates delta if the timers have changed, or if the 
        // label has changed.
        if (e instanceof AlarmService.AlarmTriggeredEvent) {
            return IModelDelta.ADDED | IModelDelta.SELECT | IModelDelta.EXPAND;
        }
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor requestMonitor) {
        // An element is added when and selected upon a triggered event.  
        // Parent element is also expanded allow element to be selected.
        if (e instanceof AlarmService.AlarmTriggeredEvent) {
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.EXPAND);
            parentDelta.addNode(
                createVMContext( ((AlarmService.AlarmTriggeredEvent)e).getDMContext() ),
                0,
                IModelDelta.ADDED | IModelDelta.SELECT);
        }
        requestMonitor.done();
    }
}
