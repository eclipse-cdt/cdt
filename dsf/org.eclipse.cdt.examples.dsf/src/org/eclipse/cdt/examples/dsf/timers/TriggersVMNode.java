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
import org.eclipse.cdt.examples.dsf.timers.AlarmService.TriggerDMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;


/**
 * View model node that defines how alarm DMContexts are displayed in the view.  Alarm
 * nodes are fairly static, once they are created their label doesn't change.
 * @see TriggerDMContext 
 */
@SuppressWarnings("restriction")
class TriggersVMNode extends AbstractDMVMNode 
    implements IElementEditor, IElementPropertiesProvider, IElementLabelProvider
{
    private static final String PROP_TRIGGER_NUMBER = "alarmNumber"; 
    private static final String PROP_TRIGGER_VALUE = "alarmTriggerValue"; 
    
    // Create and configure the label provider.
    private static final PropertiesBasedLabelProvider fgLabelProvider;
    static {
        fgLabelProvider = new PropertiesBasedLabelProvider();

        LabelColumnInfo idCol = new LabelColumnInfo(
            new LabelAttribute[] { 
                new LabelText("Trigger #{0}", new String[] { PROP_TRIGGER_NUMBER }), 
                new LabelForeground(new RGB(255, 0, 0)),
                new LabelImage(DsfExamplesPlugin.getDefault().getImageRegistry().
                    getDescriptor(DsfExamplesPlugin.IMG_ALARM))
            });
        fgLabelProvider.setColumnInfo(TimersViewColumnPresentation.COL_ID, idCol);
        
        LabelColumnInfo valueCol = new LabelColumnInfo(
            new LabelAttribute[] {
                new LabelText("{0}", new String[] { PROP_TRIGGER_VALUE }) 
            });
        fgLabelProvider.setColumnInfo(TimersViewColumnPresentation.COL_VALUE, 
            valueCol);            
    }

    private TriggerCellModifier fAlarmCellModifier;
    
    public TriggersVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, TriggerDMContext.class);
    }
    
    @Override
    public String toString() {
        return "TriggersVMNode(" + getSession().getId() + ")"; 
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        AlarmService alarmService = getServicesTracker().getService(AlarmService.class, null); 
        if ( alarmService == null ) {
            handleFailedUpdate(update);
            return;
    	}

        TriggerDMContext[] triggers = alarmService.getTriggers();
        fillUpdateWithVMCs(update, triggers);
        update.done();
    }

    public void update(ILabelUpdate[] updates) {
        fgLabelProvider.update(updates);
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
        // Find the trigger context in the element being updated
        TriggerDMContext triggerCtx = findDmcInPath( 
            update.getViewerInput(), update.getElementPath(), TriggerDMContext.class);
        AlarmService alarmService = getServicesTracker().getService(AlarmService.class, null); 
        
        // If either update or service are not valid, fail the update and return.
        if ( triggerCtx == null || alarmService == null) {
        	handleFailedUpdate(update);
            return;
        }
        
        // Calculate and set the update properties.
        int value = alarmService.getTriggerValue(triggerCtx);
        
        if (value == -1) {
            handleFailedUpdate(update);
            return;
        } 

        update.setProperty(PROP_TRIGGER_NUMBER, triggerCtx.getTriggerNumber());
        update.setProperty(PROP_TRIGGER_VALUE, value);
        update.done();
    }
    
    public CellEditor getCellEditor(IPresentationContext context, String columnId, 
        Object element, Composite parent) 
    {
        // Create a cell editor to modify the trigger value.
        if (TimersViewColumnPresentation.COL_VALUE.equals(columnId)) { 
            return new TextCellEditor(parent);
        } 
        return null;
    }

    // Note: this method is synchronized because IElementEditor.getCellModifier can be called
    // on any thread, even though in practice it should be only called on the UI thread.
    public ICellModifier getCellModifier(IPresentationContext context, 
        Object element) 
    {
        // Create the cell modifier if needed.
        if (fAlarmCellModifier == null) {
            fAlarmCellModifier = new TriggerCellModifier(getSession());
        }
        return fAlarmCellModifier; 
    }
    
    public int getDeltaFlags(Object e) {
        // Since the label for triggers doesn't change, this node will generate 
        // delta info only if the list of alarms is changed.
        if (e instanceof AlarmService.TriggersChangedEvent) {
            return IModelDelta.CONTENT;
        }
        return IModelDelta.NO_CHANGE;
    }
    
    
    public void buildDelta(Object event, VMDelta parentDelta, int nodeOffset, 
        RequestMonitor requestMonitor) 
    {
        if (event instanceof AlarmService.TriggersChangedEvent) {
            // The list of alarms has changed, which means that the parent 
            // node needs to refresh its contents, which in turn will re-fetch the
            // elements from this node.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        }
        requestMonitor.done();
    } 
    
    @Override
    public void dispose() {
        if (fAlarmCellModifier != null) {
            fAlarmCellModifier.dispose();
        }
        super.dispose();
    }
}
