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
package org.eclipse.cdt.examples.dsf.timers;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelAttribute;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelForeground;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelImage;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesBasedLabelProvider;
import org.eclipse.cdt.examples.dsf.DsfExamplesPlugin;
import org.eclipse.cdt.examples.dsf.timers.TimerService.TimerDMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.swt.graphics.RGB;


/**
 * View model node that defines how timer DMContexts are displayed in the view. Timers
 * change with every tick of the timer, so the label has to be repained 
 * upon timer tick events.
 * @see TimerDMContext
 */
@SuppressWarnings("restriction")
class TimersVMNode extends AbstractDMVMNode 
    implements IElementLabelProvider, IElementPropertiesProvider
{
    private static final String PROP_TIMER_NUMBER = "alarmNumber"; 
    private static final String PROP_TIMER_VALUE = "alarmTriggerValue"; 
    
    // Create and configure the label provider.
    private static final PropertiesBasedLabelProvider fgLabelProvider;
    static {
        fgLabelProvider = new PropertiesBasedLabelProvider();

        LabelColumnInfo idCol = new LabelColumnInfo(
            new LabelAttribute[] { 
                new LabelText("Timer #{0}", new String[] { PROP_TIMER_NUMBER }),
                new LabelForeground(new RGB(0, 0, 255)),
                new LabelImage(DsfExamplesPlugin.getDefault().getImageRegistry().
                    getDescriptor(DsfExamplesPlugin.IMG_TIMER))
            });
        fgLabelProvider.setColumnInfo(TimersViewColumnPresentation.COL_ID, idCol);
        
        LabelColumnInfo valueCol = new LabelColumnInfo(
            new LabelAttribute[] { 
                new LabelText("{0}", new String[] { PROP_TIMER_VALUE }) 
            });
        fgLabelProvider.setColumnInfo(TimersViewColumnPresentation.COL_VALUE, 
            valueCol);            
        
    }

    
    public TimersVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, TimerDMContext.class);
    }
    
    @Override
    public String toString() {
        return "TimersVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void update(ILabelUpdate[] updates) {
        fgLabelProvider.update(updates);
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        TimerService timerService = getServicesTracker().getService(TimerService.class, null);        
    	if ( timerService == null ) {
            handleFailedUpdate(update);
            return;
    	}

        // Retrieve the timer DMContexts, create the corresponding VMCs array, and 
        // set them as result.
        TimerDMContext[] timers = timerService.getTimers();
        fillUpdateWithVMCs(update, timers);
        update.done();
    }


    public void update(final IPropertiesUpdate[] updates) {
        // Switch to the session thread before processing the updates.
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    for (IPropertiesUpdate update : updates) {
                        updatePropertiesInSessionThread(update);
                    }
                }});
        } catch (RejectedExecutionException e) {
            for (IViewerUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }

    @ConfinedToDsfExecutor("getSession#getExecutor")
    private void updatePropertiesInSessionThread(final IPropertiesUpdate update) {
        // Find the timer context in the element being updated
        TimerDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), TimerDMContext.class);
        TimerService timerService = getServicesTracker().getService(TimerService.class, null);

        // If either update or service are not valid, fail the update and exit.
        if ( dmc == null || timerService == null) {
        	handleFailedUpdate(update);
            return;
        }
        
        int value = timerService.getTimerValue(dmc);
        
        if (value == -1) {
            handleFailedUpdate(update);
            return;
        }

        update.setProperty(PROP_TIMER_NUMBER, dmc.getTimerNumber());
        update.setProperty(PROP_TIMER_VALUE, value);
        update.done();
    }

    public int getDeltaFlags(Object e) {
        // This node generates delta if the timers have changed, or if the 
        // label has changed.
        if (e instanceof TimerService.TimerTickDMEvent) {
            return IModelDelta.STATE;
        } else if (e instanceof TimerService.TimersChangedEvent) {
            return IModelDelta.CONTENT;
        }
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor requestMonitor) {
        if (e instanceof TimerService.TimerTickDMEvent) {
            // Add delta indicating that the given timer has changed.
            parentDelta.addNode( createVMContext(((TimerService.TimerTickDMEvent)e).getDMContext()), IModelDelta.STATE );
        } else if (e instanceof TimerService.TimersChangedEvent) {
            // The list of timers has changed, which means that the parent 
            // node needs to refresh its contents, which in turn will re-fetch the
            // elements from this node.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        }
        requestMonitor.done();
    }
}
