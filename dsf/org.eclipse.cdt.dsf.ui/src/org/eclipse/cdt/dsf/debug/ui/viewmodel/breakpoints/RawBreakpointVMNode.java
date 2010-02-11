/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * 
 * 
 * @since 2.1
 */
public class RawBreakpointVMNode extends AbstractVMNode {

    public RawBreakpointVMNode(IVMProvider provider) {
        super(provider);
    }
    
    public void update(final IHasChildrenUpdate[] updates) {
        for (final IHasChildrenUpdate update : updates) {
            if (!checkUpdate(update)) continue;
            ((BreakpointVMProvider)getVMProvider()).getNestingCategoryBreakpoints(
                update.getElementPath(), 
                new ViewerDataRequestMonitor<IBreakpoint[]>(getExecutor(), update) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            update.setHasChilren(getData().length != 0);
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
            ((BreakpointVMProvider)getVMProvider()).getNestingCategoryBreakpoints(
                update.getElementPath(), 
                new ViewerDataRequestMonitor<IBreakpoint[]>(getExecutor(), update) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            update.setChildCount(getData().length);
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
            ((BreakpointVMProvider)getVMProvider()).getNestingCategoryBreakpoints(
                update.getElementPath(), 
                new ViewerDataRequestMonitor<IBreakpoint[]>(getExecutor(), update) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            @SuppressWarnings("unchecked")
                            Comparator<Object> comparator = 
                                (Comparator<Object>)getVMProvider().getPresentationContext().getProperty(
                                IBreakpointUIConstants.PROP_BREAKPOINTS_ELEMENT_COMPARATOR);
                            if (comparator != null) {
                                Arrays.sort(getData(), comparator);
                            }
                            fillUpdateWithBreakpoints(update, getData());
                        }
                        update.done();
                    }
                });
        }
    }    

    private void fillUpdateWithBreakpoints(IChildrenUpdate update, IBreakpoint[] bps) {
        int updateIdx = update.getOffset() != -1 ? update.getOffset() : 0;
        int endIdx = updateIdx + (update.getLength() != -1 ? update.getLength() : bps.length);
        while (updateIdx < endIdx && updateIdx < bps.length) {
            update.setChild(bps[updateIdx], updateIdx);
            updateIdx++;
        }
    }

    public int getDeltaFlags(Object event) {
        if (event instanceof BreakpointsChangedEvent) {
            BreakpointsChangedEvent bpChangedEvent = ((BreakpointsChangedEvent)event);
            if (BreakpointsChangedEvent.Type.ADDED.equals(bpChangedEvent.getType())) {
                return IModelDelta.CONTENT | IModelDelta.SELECT | IModelDelta.EXPAND;
            }
            return IModelDelta.CONTENT;
        }
        else if (BreakpointVMProvider.isPresentationContextEvent(event)) {
            PropertyChangeEvent propertyEvent = (PropertyChangeEvent)event;
            if (IBreakpointUIConstants.PROP_BREAKPOINTS_FILTER_SELECTION.equals(propertyEvent.getProperty()) ||
                IBreakpointUIConstants.PROP_BREAKPOINTS_ELEMENT_COMPARATOR.equals(propertyEvent.getProperty())) 
            {
                return IModelDelta.CONTENT;
            } else if (IBreakpointUIConstants.PROP_BREAKPOINTS_ORGANIZERS.equals(propertyEvent.getProperty())) {
                return IModelDelta.EXPAND | IModelDelta.CONTENT;
            } 
        } 
        return 0;
    }

    public void buildDelta(Object event, VMDelta parent, int nodeOffset, RequestMonitor rm) {
        if (event instanceof BreakpointsChangedEvent) {
            BreakpointsChangedEvent bpChangedEvent = ((BreakpointsChangedEvent)event);
            if (BreakpointsChangedEvent.Type.ADDED.equals(bpChangedEvent.getType())) {
                buildBreakpointAddedDelta(bpChangedEvent, parent, nodeOffset, rm);
                // Do not call rm.done() in this method!
                return;
            } else {
                parent.setFlags(parent.getFlags() | IModelDelta.CONTENT);
            }
        }
        else if (BreakpointVMProvider.isPresentationContextEvent(event)) {
            PropertyChangeEvent propertyEvent = (PropertyChangeEvent)event;
            if (IBreakpointUIConstants.PROP_BREAKPOINTS_FILTER_SELECTION.equals(propertyEvent.getProperty()) ||
                IBreakpointUIConstants.PROP_BREAKPOINTS_ELEMENT_COMPARATOR.equals(propertyEvent.getProperty())) 
            {
                parent.setFlags(parent.getFlags() | IModelDelta.CONTENT);
            } else if (IBreakpointUIConstants.PROP_BREAKPOINTS_ORGANIZERS.equals(propertyEvent.getProperty())) {
                parent.setFlags(parent.getFlags() | IModelDelta.EXPAND | IModelDelta.CONTENT);
            } 
        } 
        
        rm.done();
    }

    private void buildBreakpointAddedDelta(final BreakpointsChangedEvent event, final VMDelta parent, final int nodeOffset, final RequestMonitor rm) {
        getVMProvider().updateNode(this, new VMChildrenUpdate(
            parent, getVMProvider().getPresentationContext(), -1, -1, 
            new DataRequestMonitor<List<Object>>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    for (int i = 0; i < event.getBreakpoints().length; i++) {
                        int bpIndex = getData().indexOf(event.getBreakpoints()[i]); 
                        if (bpIndex >= 0) {
                            // Select only the first breakpoint that was added
                            if (i == 0) {
                                parent.addNode(getData().get(bpIndex), bpIndex, IModelDelta.SELECT);
                            }
                            // For all other added breakpoints, expand the parent.
                            parent.setFlags(parent.getFlags() | IModelDelta.CONTENT | IModelDelta.EXPAND);
                        }
                    }
                    rm.done();
                }
            }));
    }
}
