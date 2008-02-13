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

import java.text.MessageFormat;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.dd.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.dd.dsf.ui.viewmodel.properties.LabelAttribute;
import org.eclipse.dd.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.dd.dsf.ui.viewmodel.properties.LabelImage;
import org.eclipse.dd.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.dd.dsf.ui.viewmodel.properties.PropertyBasedLabelProvider;
import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;
import org.eclipse.dd.examples.dsf.timers.AlarmService.AlarmDMC;
import org.eclipse.dd.examples.dsf.timers.AlarmService.AlarmData;
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
import org.eclipse.swt.widgets.Composite;


/**
 * View model node that defines how alarm DMContexts are displayed in the view.  Alarm
 * nodes are fairly static, once they are created their label doesn't change.
 * @see AlarmDMC 
 */
@SuppressWarnings("restriction")
class AlarmsVMNode extends AbstractDMVMNode 
    implements IElementEditor, IElementPropertiesProvider, IElementLabelProvider
{
    public static final String PROP_ALARM_NUMBER = "alarmNumber"; //$NON-NLS-1$
    public static final String PROP_ALARM_TRIGGER_VALUE = "alarmTriggerValue"; //$NON-NLS-1$
    
    private AlarmCellModifier fAlarmCellModifier;
    private PropertyBasedLabelProvider fLabelProvider;
    
    
    public AlarmsVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, AlarmDMC.class);
        
        fLabelProvider = new PropertyBasedLabelProvider();

        LabelColumnInfo idCol = new LabelColumnInfo(
            new LabelAttribute[] { 
                new LabelText(new MessageFormat("Alarm #{0}"), new String[] { PROP_ALARM_NUMBER }), //$NON-NLS-1$
                new LabelImage(DsfExamplesPlugin.getDefault().getImageRegistry().getDescriptor(DsfExamplesPlugin.IMG_ALARM))
            });
        fLabelProvider.setColumnInfo(TimersViewColumnPresentation.COL_ID, idCol);
        
        LabelText valueText = new LabelText(new MessageFormat("{0}"), new String[] { PROP_ALARM_TRIGGER_VALUE }); //$NON-NLS-1$
        LabelColumnInfo valueCol = new LabelColumnInfo(
            new LabelAttribute[] { 
                new LabelText(new MessageFormat("{0}"), new String[] { PROP_ALARM_TRIGGER_VALUE }) //$NON-NLS-1$
            });
        fLabelProvider.setColumnInfo(TimersViewColumnPresentation.COL_VALUE, valueCol);            

    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        if (!checkService(AlarmService.class, null, update)) return;

        // Retrieve the alarm DMContexts, create the corresponding VMCs array, and 
        // set them as result.
        getServicesTracker().getService(AlarmService.class).getAlarms(
            new DataRequestMonitor<AlarmDMC[]>(getSession().getExecutor(), null) { 
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

    public void update(ILabelUpdate[] updates) {
        fLabelProvider.update(updates);
    }
    
    public void update(final IPropertiesUpdate[] updates) {
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
    protected void updatePropertiesInSessionThread(final IPropertiesUpdate update) {
        final AlarmDMC dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), AlarmDMC.class);
        if (!checkDmc(dmc, update) || !checkService(AlarmService.class, null, update)) return;
        
        getDMVMProvider().getModelData(
            this, update, 
            getServicesTracker().getService(AlarmService.class, null),
            dmc, 
            new DataRequestMonitor<AlarmData>(getSession().getExecutor(), null) { 
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

                    update.setProperty(PROP_ALARM_NUMBER, getData().getAlarmNumber());
                    update.setProperty(PROP_ALARM_TRIGGER_VALUE, getData().getTriggeringValue());
                    update.done();
                }
            },
            getExecutor());
    }
    
    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        if (TimersViewColumnPresentation.COL_VALUE.equals(columnId)) { 
            return new TextCellEditor(parent);
        } 
        return null;
    }

    // Note: this method is synchronized because IElementEditor.getCellModifier can be called
    // on any thread, even though in practice it should be only called on the UI thread.
    public synchronized ICellModifier getCellModifier(IPresentationContext context, Object element) {
        if (fAlarmCellModifier == null) {
            fAlarmCellModifier = new AlarmCellModifier(getSession());
        }
        return fAlarmCellModifier; 
    }
    
    public int getDeltaFlags(Object e) {
        // Since the label for alarms doesn't change, this node will generate 
        // delta info only if the list of alarms is changed.
        if (e instanceof AlarmService.AlarmsChangedEvent) {
            return IModelDelta.CONTENT;
        }
        return IModelDelta.NO_CHANGE;
    }
    
    
    public void buildDelta(Object event, VMDelta parentDelta, int nodeOffset, RequestMonitor requestMonitor) {
        if (event instanceof AlarmService.AlarmsChangedEvent) {
            // The list of alarms has changed, which means that the parent 
            // node needs to refresh its contents, which in turn will re-fetch the
            // elements from this node.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        }
        requestMonitor.done();
    } 
    
    @Override
    public synchronized void dispose() {
        synchronized(this) {
            if (fAlarmCellModifier != null) {
                fAlarmCellModifier.dispose();
            }
        }
        super.dispose();
    }

    public String getPropertyDescription(String property) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPropertyName(String property) {
        // TODO Auto-generated method stub
        return null;
    }
}
