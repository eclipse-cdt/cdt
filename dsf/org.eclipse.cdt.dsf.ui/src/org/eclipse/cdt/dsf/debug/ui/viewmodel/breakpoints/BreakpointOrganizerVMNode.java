/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints;

import java.util.List;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointOrganizer;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * 
 * 
 * @since 2.1
 */
public class BreakpointOrganizerVMNode extends AbstractVMNode {

    private final IBreakpointOrganizer fOrganizer;

    private final IPropertyChangeListener fOrganizerListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            ((BreakpointVMProvider)getVMProvider()).handleEventInExecThread(event);
        }
    };
    
    public BreakpointOrganizerVMNode(BreakpointVMProvider provider, IBreakpointOrganizer organizer) {
        super(provider);
        fOrganizer = organizer;
        fOrganizer.addPropertyChangeListener(fOrganizerListener);
    }
    
    @Override
    public void dispose() {
        fOrganizer.removePropertyChangeListener(fOrganizerListener);
        super.dispose();
    }
    
    public IBreakpointOrganizer getOrganizer() {
        return fOrganizer;
    }
    
    public void update(final IHasChildrenUpdate[] updates) {
        for (final IHasChildrenUpdate update : updates) {
            if (!checkUpdate(update)) continue;
            ((BreakpointVMProvider)getVMProvider()).getBreakpointOrganizerVMCs(
                this, update.getElementPath(), 
                new ViewerDataRequestMonitor<List<BreakpointOrganizerVMContext>>(getExecutor(), update) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            update.setHasChilren(!getData().isEmpty());
                        } else {
                            update.setHasChilren(false);
                        }
                        update.done();
                    }
                });
        }
    }

    public void update(final IChildrenCountUpdate[] updates) {
        for (final IChildrenCountUpdate update : updates) {
            if (!checkUpdate(update)) continue;
            ((BreakpointVMProvider)getVMProvider()).getBreakpointOrganizerVMCs(
                this, update.getElementPath(), 
                new ViewerDataRequestMonitor<List<BreakpointOrganizerVMContext>>(getExecutor(), update) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            update.setChildCount(getData().size());
                        } else {
                            update.setChildCount(0);
                        }
                        update.done();
                    }
                });
        }
    }
    
    public void update(IChildrenUpdate[] updates) {
        for (final IChildrenUpdate update : updates) {
            if (!checkUpdate(update)) continue;
            ((BreakpointVMProvider)getVMProvider()).getBreakpointOrganizerVMCs(
                this, update.getElementPath(), 
                new ViewerDataRequestMonitor<List<BreakpointOrganizerVMContext>>(getExecutor(), update) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            int updateIdx = update.getOffset() != -1 ? update.getOffset() : 0;
                            int endIdx = updateIdx + (update.getLength() != -1 ? update.getLength() : (getData().size()));
                            while (updateIdx < endIdx && updateIdx < getData().size()) {
                                update.setChild(getData().get(updateIdx), updateIdx);
                                updateIdx++;
                            }
                        }
                        update.done();
                    }
                });
        }
    }    
    
    protected BreakpointOrganizerVMContext createVMContext(IAdaptable category, IBreakpoint[] breakpoints) {
        return new BreakpointOrganizerVMContext(this, category, breakpoints);
    }
    
    public int getDeltaFlags(Object event) {
        if (event instanceof BreakpointsChangedEvent) {
            return IModelDelta.CONTENT;
        }
        else if (BreakpointVMProvider.isPresentationContextEvent(event)) {
            PropertyChangeEvent propertyEvent = (PropertyChangeEvent)event;
            if (IBreakpointUIConstants.PROP_BREAKPOINTS_FILTER_SELECTION.equals(propertyEvent.getProperty()) ||
                IBreakpointUIConstants.PROP_BREAKPOINTS_ELEMENT_COMPARATOR.equals(propertyEvent.getProperty()) ||
                IBreakpointUIConstants.PROP_BREAKPOINTS_ORGANIZERS.equals(propertyEvent.getProperty())) 
            {
                return IModelDelta.CONTENT;
            } 
        } else if (BreakpointVMProvider.isBreakpointOrganizerEvent(event)) {
            return IModelDelta.CONTENT;
        }
        return 0;
    }

    public void buildDelta(Object event, VMDelta parent, int nodeOffset, RequestMonitor requestMonitor) {
        if (event instanceof BreakpointsChangedEvent) {
            parent.setFlags(parent.getFlags() | IModelDelta.CONTENT);
        }
        else if (BreakpointVMProvider.isPresentationContextEvent(event)) {
            PropertyChangeEvent propertyEvent = (PropertyChangeEvent)event;
            if (IBreakpointUIConstants.PROP_BREAKPOINTS_FILTER_SELECTION.equals(propertyEvent.getProperty()) ||
                IBreakpointUIConstants.PROP_BREAKPOINTS_ELEMENT_COMPARATOR.equals(propertyEvent.getProperty()) || 
                IBreakpointUIConstants.PROP_BREAKPOINTS_ORGANIZERS.equals(propertyEvent.getProperty())) 
            {
                parent.setFlags(parent.getFlags() | IModelDelta.CONTENT);
            }
        } else if (BreakpointVMProvider.isBreakpointOrganizerEvent(event)) {
            parent.setFlags(parent.getFlags() | IModelDelta.CONTENT);
        }
        
        requestMonitor.done();
    }

}
