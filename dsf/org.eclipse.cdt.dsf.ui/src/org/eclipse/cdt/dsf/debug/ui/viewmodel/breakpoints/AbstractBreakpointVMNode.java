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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.ModelProxyInstalledEvent;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Base class for breakpoint VM Nodes.  Concrete implementations must 
 * implement the breakpoint object to be populated into the view.  
 * Also this node only implements the content provider so sub-classes
 * must also implement a label provider, element editor, etc.
 * 
 * @since 2.1
 */
public abstract class AbstractBreakpointVMNode extends AbstractVMNode {

    public AbstractBreakpointVMNode(BreakpointVMProvider provider) {
        super(provider);
    }

    /**
     * Class that creates the element object for the corresponding breakpoints.  
     * This element object will be populated in the breakpoitns view to represent
     * the given breakpoint.
     */
    abstract protected Object createBreakpiontElement(IBreakpoint bp);
    
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
                            fillUpdateWithBreakpointElements(update, getData());
                        }
                        update.done();
                    }
                });
        }
    }    

    private void fillUpdateWithBreakpointElements(IChildrenUpdate update, IBreakpoint[] bps) {
        int updateIdx = update.getOffset() != -1 ? update.getOffset() : 0;
        int endIdx = updateIdx + (update.getLength() != -1 ? update.getLength() : bps.length);
        while (updateIdx < endIdx && updateIdx < bps.length) {
            update.setChild(createBreakpiontElement(bps[updateIdx]), updateIdx);
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
            } else if (IBreakpointUIConstants.PROP_BREAKPOINTS_TRACK_SELECTION.equals(propertyEvent.getProperty()) &&
                       Boolean.TRUE.equals(propertyEvent.getNewValue()) )
            {
                return IModelDelta.EXPAND | IModelDelta.SELECT;
            } 
        } 
        else if (event instanceof DebugContextEvent && (((DebugContextEvent)event).getFlags() | DebugContextEvent.ACTIVATED) != 0) {
            int flags = IModelDelta.NO_CHANGE;
            if ( Boolean.TRUE.equals(getVMProvider().getPresentationContext().getProperty(IBreakpointUIConstants.PROP_BREAKPOINTS_FILTER_SELECTION)) ) {
                flags |= IModelDelta.CONTENT;
            } 
            if (Boolean.TRUE.equals(getVMProvider().getPresentationContext().getProperty(IBreakpointUIConstants.PROP_BREAKPOINTS_TRACK_SELECTION)) ) {
                flags |= IModelDelta.EXPAND | IModelDelta.SELECT;
            }
            return flags;
        } else if (event instanceof ModelProxyInstalledEvent) {
            // Upon model proxy installed, check whether we need to select a 
            // breakpoint in linking with Debug view
            if (Boolean.TRUE.equals(getVMProvider().getPresentationContext().getProperty(IBreakpointUIConstants.PROP_BREAKPOINTS_TRACK_SELECTION)) ) {
                return IModelDelta.EXPAND | IModelDelta.SELECT;
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
            } else if (IBreakpointUIConstants.PROP_BREAKPOINTS_TRACK_SELECTION.equals(propertyEvent.getProperty()) &&
                       Boolean.TRUE.equals(propertyEvent.getNewValue()) )
            {
                IWorkbenchWindow window = getVMProvider().getPresentationContext().getWindow();
                if (window != null) {
                    ISelection activeContext = DebugUITools.getDebugContextManager().getContextService(window).getActiveContext();
                    buildTrackSelectionDelta(activeContext, parent, nodeOffset, rm);
                    // Do not call rm.done() in this method!
                    return;
                }
            } 
        } 
        else if (event instanceof DebugContextEvent && (((DebugContextEvent)event).getFlags() | DebugContextEvent.ACTIVATED) != 0) {
            if ( Boolean.TRUE.equals(getVMProvider().getPresentationContext().getProperty(IBreakpointUIConstants.PROP_BREAKPOINTS_FILTER_SELECTION)) ) {
                parent.setFlags(parent.getFlags() | IModelDelta.CONTENT);
            } 
            if (Boolean.TRUE.equals(getVMProvider().getPresentationContext().getProperty(IBreakpointUIConstants.PROP_BREAKPOINTS_TRACK_SELECTION)) ) {
                buildTrackSelectionDelta(((DebugContextEvent)event).getContext(), parent, nodeOffset, rm);
                // Do not call rm.done() in this method!
                return;
            }
        } else if (event instanceof ModelProxyInstalledEvent) {
            if (Boolean.TRUE.equals(getVMProvider().getPresentationContext().getProperty(IBreakpointUIConstants.PROP_BREAKPOINTS_TRACK_SELECTION)) ) {
                IWorkbenchWindow window = getVMProvider().getPresentationContext().getWindow();
                if (window != null) {
                    ISelection activeContext = DebugUITools.getDebugContextManager().getContextService(window).getActiveContext();
                    buildTrackSelectionDelta(activeContext, parent, nodeOffset, rm);
                    return;
                }
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
                        IBreakpoint eventBp = event.getBreakpoints()[i];
                        int bpIndex = findBreakpointIndex(eventBp, getData());
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
    
    protected void buildTrackSelectionDelta(ISelection debugContext, final VMDelta parent, final int nodeOffset, final RequestMonitor rm) {
        assert getVMProvider() instanceof BreakpointVMProvider;
        
        ((BreakpointVMProvider)getVMProvider()).getBreakpointsForDebugContext(
            debugContext, 
            new DataRequestMonitor<IBreakpoint[]>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    if (getData().length == 0) {
                        // No breakpoints to select, we're done.
                        rm.done();
                        return;
                    }
                    final IBreakpoint[] bpsToSelect = getData();
                    
                    getVMProvider().updateNode(AbstractBreakpointVMNode.this, new VMChildrenUpdate(
                        parent, getVMProvider().getPresentationContext(), -1, -1, 
                        new DataRequestMonitor<List<Object>>(getExecutor(), rm) {
                            @Override
                            protected void handleSuccess() {
                                for (int i = 0; i < bpsToSelect.length; i++) {
                                    int bpIndex = findBreakpointIndex(bpsToSelect[i], getData());
                                    if (bpIndex >= 0) {
                                        // Select only the first breakpoint that was added
                                        if (i == 0) {
                                            parent.addNode(getData().get(bpIndex), bpIndex, IModelDelta.SELECT);
                                        }
                                        // For all other added breakpoints, expand the parent.
                                        parent.setFlags(parent.getFlags() | IModelDelta.EXPAND);
                                    }
                                }
                                rm.done();
                            }
                        }));
                }
                
                @Override
                protected void handleErrorOrWarning() {
                    rm.done();
                }
            });
    }
    
    private int findBreakpointIndex(IBreakpoint bp, List<Object> bpElements) {
        for (int j = 0; j < bpElements.size(); j++) {
            IBreakpoint elementBp = (IBreakpoint)DebugPlugin.getAdapter(bpElements.get(j), IBreakpoint.class);
            if (elementBp != null && elementBp.equals(bp)) {
                return j;
            }
        }
        return -1;
    }
}
