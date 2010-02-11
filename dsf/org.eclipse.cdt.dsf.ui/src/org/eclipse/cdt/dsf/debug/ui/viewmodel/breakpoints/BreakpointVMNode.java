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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IMemento;

/**
 * 
 * 
 * @since 2.1
 */
public class BreakpointVMNode extends AbstractVMNode implements IElementLabelProvider, IElementMementoProvider {

    public BreakpointVMNode(IVMProvider provider) {
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
                            fillUpdateWithBreakpointVMCs(update, getData());
                        }
                        update.done();
                    }
                });
        }
    }    

    private void fillUpdateWithBreakpointVMCs(IChildrenUpdate update, IBreakpoint[] bps) {
        int updateIdx = update.getOffset() != -1 ? update.getOffset() : 0;
        int endIdx = updateIdx + (update.getLength() != -1 ? update.getLength() : bps.length);
        while (updateIdx < endIdx && updateIdx < bps.length) {
            update.setChild(createVMContext(bps[updateIdx]), updateIdx);
            updateIdx++;
        }
    }
    
    protected BreakpointVMContext createVMContext(IBreakpoint bp) {
        return new BreakpointVMContext(this, bp);
    }

    public void update(ILabelUpdate[] updates) {
        Map<IElementLabelProvider, List<ILabelUpdate>> delegatesMap = new HashMap<IElementLabelProvider, List<ILabelUpdate>>(1,1);
        
        for (final ILabelUpdate update : updates) {
            final IBreakpoint bp = ((BreakpointVMContext)update.getElement()).getBreakpoint();
            IElementLabelProvider provider = (IElementLabelProvider)bp.getAdapter(IElementLabelProvider.class);
            if (provider == null) {
                update.done();
                continue;
            }
            
            List<ILabelUpdate> delegatesList = delegatesMap.get(provider);
            if (delegatesList == null) {
                delegatesList = new ArrayList<ILabelUpdate>(updates.length);
                delegatesMap.put(provider, delegatesList);
            }
            delegatesList.add(new ICheckUpdate() {
                public void setChecked(boolean checked, boolean grayed) {
                    if (update instanceof ICheckUpdate) {
                        ((ICheckUpdate)update).setChecked(checked, grayed);
                    }
                }
                public String[] getColumnIds() { return update.getColumnIds(); }
                public void setLabel(String text, int columnIndex) { 
                	update.setLabel(text, columnIndex); 
                	}
                public void setFontData(FontData fontData, int columnIndex) { update.setFontData(fontData, columnIndex); }
                public void setImageDescriptor(ImageDescriptor image, int columnIndex) { update.setImageDescriptor(image, columnIndex); }
                public void setForeground(RGB foreground, int columnIndex) { update.setForeground(foreground, columnIndex); }
                public void setBackground(RGB background, int columnIndex) { update.setBackground(background, columnIndex); }
                public IPresentationContext getPresentationContext() { return update.getPresentationContext(); }
                public Object getElement() { return bp; }
                public TreePath getElementPath() { return update.getElementPath().getParentPath().createChildPath(bp); }
                public Object getViewerInput() { return update.getViewerInput(); }
                public void setStatus(IStatus status) { update.setStatus(status); }
                public IStatus getStatus() { return update.getStatus(); }
                public void done() { update.done(); }
                public void cancel() { update.cancel(); }
                public boolean isCanceled() { return update.isCanceled(); }
            });
        }
        
        for (IElementLabelProvider provider : delegatesMap.keySet()) {
            List<ILabelUpdate> updatesList = delegatesMap.get(provider);
            provider.update(updatesList.toArray(new ILabelUpdate[updatesList.size()]));
        }
    }
    
    public void encodeElements(IElementMementoRequest[] updates) {
        Map<IElementMementoProvider, List<IElementMementoRequest>> delegatesMap = new HashMap<IElementMementoProvider, List<IElementMementoRequest>>(1,1);
        
        for (final IElementMementoRequest update : updates) {
            final IBreakpoint bp = ((BreakpointVMContext)update.getElement()).getBreakpoint();
            IElementMementoProvider provider = (IElementMementoProvider)bp.getAdapter(IElementMementoProvider.class);
            if (provider == null) {
                update.done();
                continue;
            }
            
            List<IElementMementoRequest> delegatesList = delegatesMap.get(provider);
            if (delegatesList == null) {
                delegatesList = new ArrayList<IElementMementoRequest>(updates.length);
                delegatesMap.put(provider, delegatesList);
            }
            delegatesList.add(new IElementMementoRequest() {
                public IMemento getMemento() { return update.getMemento(); }
                public IPresentationContext getPresentationContext() { return update.getPresentationContext(); }
                public Object getElement() { return bp; }
                public TreePath getElementPath() { return update.getElementPath().getParentPath().createChildPath(bp); }
                public Object getViewerInput() { return update.getViewerInput(); }
                public void setStatus(IStatus status) { update.setStatus(status); }
                public IStatus getStatus() { return update.getStatus(); }
                public void done() { update.done(); }
                public void cancel() { update.cancel(); }
                public boolean isCanceled() { return update.isCanceled(); }
            });
        }
        
        for (IElementMementoProvider provider : delegatesMap.keySet()) {
            List<IElementMementoRequest> updatesList = delegatesMap.get(provider);
            provider.encodeElements(updatesList.toArray(new IElementMementoRequest[updatesList.size()]));
        }
    }
    
    public void compareElements(IElementCompareRequest[] updates) {
        Map<IElementMementoProvider, List<IElementCompareRequest>> delegatesMap = new HashMap<IElementMementoProvider, List<IElementCompareRequest>>(1,1);
        
        for (final IElementCompareRequest update : updates) {
            final IBreakpoint bp = ((BreakpointVMContext)update.getElement()).getBreakpoint();
            IElementMementoProvider provider = (IElementMementoProvider)bp.getAdapter(IElementMementoProvider.class);
            if (provider == null) {
                update.done();
                continue;
            }
            
            List<IElementCompareRequest> delegatesList = delegatesMap.get(provider);
            if (delegatesList == null) {
                delegatesList = new ArrayList<IElementCompareRequest>(updates.length);
                delegatesMap.put(provider, delegatesList);
            }
            delegatesList.add(new IElementCompareRequest() {
                public IMemento getMemento() { return update.getMemento(); }
                public void setEqual(boolean equal) { update.setEqual(equal);}
                public IPresentationContext getPresentationContext() { return update.getPresentationContext(); }
                public Object getElement() { return bp; }
                public TreePath getElementPath() { return update.getElementPath().getParentPath().createChildPath(bp); }
                public Object getViewerInput() { return update.getViewerInput(); }
                public void setStatus(IStatus status) { update.setStatus(status); }
                public IStatus getStatus() { return update.getStatus(); }
                public void done() { update.done(); }
                public void cancel() { update.cancel(); }
                public boolean isCanceled() { return update.isCanceled(); }
            });
        }
        
        for (IElementMementoProvider provider : delegatesMap.keySet()) {
            List<IElementCompareRequest> updatesList = delegatesMap.get(provider);
            provider.compareElements(updatesList.toArray(new IElementCompareRequest[updatesList.size()]));
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
