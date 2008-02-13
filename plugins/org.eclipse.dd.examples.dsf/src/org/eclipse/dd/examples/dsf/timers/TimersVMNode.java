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

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;
import org.eclipse.dd.examples.dsf.timers.TimerService.TimerDMC;
import org.eclipse.dd.examples.dsf.timers.TimerService.TimerData;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;


/**
 * View model node that defines how timer DMContexts are displayed in the view. Timers
 * change with every tick of the timer, so the label has to be repained 
 * upon timer tick events.
 * @see TimerDMC
 */
@SuppressWarnings("restriction")
class TimersVMNode extends AbstractDMVMNode 
    implements IElementLabelProvider
{
    
    public TimersVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, TimerDMC.class);
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        if (!checkService(AlarmService.class, null, update)) return;

        // Retrieve the timer DMContexts, create the corresponding VMCs array, and 
        // set them as result.
        getServicesTracker().getService(TimerService.class).getTimers(
            new DataRequestMonitor<TimerDMC[]>(getSession().getExecutor(), null) { 
                @Override
                public void handleCompleted() {
                    if (!getStatus().isOK()) {
                        update.setStatus(getStatus());
                    } else {
                        fillUpdateWithVMCs(update, getData());
                    }
                    update.done();
                }});
    }


    public void update(final ILabelUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    updateLabelInSessionThread(updates);
                }});
        } catch (RejectedExecutionException e) {
            for (ILabelUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }


    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
            final TimerDMC dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), TimerDMC.class);
            if (!checkDmc(dmc, update) || !checkService(TimerService.class, null, update)) continue;
            
            getDMVMProvider().getModelData(
                this, update, 
                getServicesTracker().getService(TimerService.class, null),
                dmc, 
                new DataRequestMonitor<TimerData>(getSession().getExecutor(), null) { 
                    @Override
                    protected void handleCompleted() {
                        /*
                         * Check that the request was evaluated and data is still
                         * valid.  The request could fail if the state of the 
                         * service changed during the request, but the view model
                         * has not been updated yet.
                         */ 
                        if (!getStatus().isOK() || !getData().isValid()) {
                            assert getStatus().isOK() || 
                                   getStatus().getCode() != IDsfService.INTERNAL_ERROR || 
                                   getStatus().getCode() != IDsfService.NOT_SUPPORTED;
                            handleFailedUpdate(update);
                            return;
                        }
                        
                        /*
                         * If columns are configured, call the protected methods to 
                         * fill in column values.  
                         */
                        String[] localColumns = update.getPresentationContext().getColumns();
                        if (localColumns == null) localColumns = new String[] { null };
                        
                        for (int i = 0; i < localColumns.length; i++) {
                            fillColumnLabel(dmc, getData(), localColumns[i], i, update);
                        }
                        update.done();
                    }
                },
                getExecutor());
        }
    }

    protected void fillColumnLabel(TimerDMC dmContext, TimerData dmData, String columnId, int idx,
                                   ILabelUpdate update) 
    {
        if (TimersViewColumnPresentation.COL_ID.equals(columnId)) {
            update.setLabel( Integer.toString(dmData.getTimerNumber()), idx ); 
            update.setImageDescriptor(
                DsfExamplesPlugin.getDefault().getImageRegistry().getDescriptor(DsfExamplesPlugin.IMG_TIMER), idx);
        } else if (TimersViewColumnPresentation.COL_VALUE.equals(columnId)) {
            update.setLabel( Integer.toString(dmData.getTimerValue()), idx);
        }       
    }
    
    public int getDeltaFlags(Object e) {
        // This node generates delta if the timers have changed, or if the 
        // label has changed.
        if (e instanceof TimerService.TimerTickEvent) {
            return IModelDelta.STATE;
        } else if (e instanceof TimerService.TimersChangedEvent) {
            return IModelDelta.CONTENT;
        }
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor requestMonitor) {
        if (e instanceof TimerService.TimerTickEvent) {
            // Add delta indicating that the VMC for the given timer context 
            // has changed.
            parentDelta.addNode( createVMContext(((TimerService.TimerTickEvent)e).getDMContext()), IModelDelta.STATE );
        } else if (e instanceof TimerService.TimersChangedEvent) {
            // The list of timers has changed, which means that the parent 
            // node needs to refresh its contents, which in turn will re-fetch the
            // elements from this node.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        }
        requestMonitor.done();
    }
}
