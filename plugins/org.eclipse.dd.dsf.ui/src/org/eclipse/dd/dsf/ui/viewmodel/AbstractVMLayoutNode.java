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
package org.eclipse.dd.dsf.ui.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/**
 * Base implementation of the View Model layout node.  
 * The main functionality implemented here is for building the view model 
 * deltas (IModelDelta), based on the flags returned by child nodes. 
 */
@SuppressWarnings("restriction")
abstract public class AbstractVMLayoutNode implements IVMLayoutNode {

    private final AbstractVMProvider fProvider;
    private boolean fDisposed = false;
    
    /** Child schema nodes of this node. */
    private IVMLayoutNode[] fChildNodes = new IVMLayoutNode[0];

    
    public AbstractVMLayoutNode(AbstractVMProvider provider) {
        fProvider = provider;
    }

    /**
     * Accessor method for sub-classes.
     */
    protected DsfExecutor getExecutor() {
        return fProvider.getExecutor();
    }

    public IVMProvider getVMProvider() {
        return fProvider;
    }
    
    public void setChildNodes(IVMLayoutNode[] childNodes) {
        fChildNodes = childNodes;
    }
    
    public IVMLayoutNode[] getChildLayoutNodes() {
        return fChildNodes;
    }

    public void dispose() {
        fDisposed = true;
        for (IVMLayoutNode childNode : getChildLayoutNodes()) {
            childNode.dispose();
        }
    }
    
    /** 
     * If any of the children nodes have delta flags, that means that this 
     * node has to generate a delta as well. 
     * <p> Note: If a child node has a <code>IModelDelta.CONTENT</code> delta 
     * flag, it means that this flag will be added to this node's element.  
     * To allow for optimization change the child's <code>IModelDelta.CONTENT</code>
     * flag into a <code>IModelDelta.STATE</code> flag.
     */
    public int getDeltaFlags(Object e) {
        int retVal = 0;
        for (IVMLayoutNode childNode : getChildLayoutNodes()) {
            int childNodeDeltaFlags = childNode.getDeltaFlags(e);
            if ((childNodeDeltaFlags | IModelDelta.CONTENT) != 0) {
                childNodeDeltaFlags &= ~IModelDelta.CONTENT;
                childNodeDeltaFlags |= IModelDelta.STATE;
            }
            retVal |= childNode.getDeltaFlags(e);
        }
        return retVal;
    }

    /** 
     * Base implementation that handles calling child layout nodes to build 
     * the model delta.  The child nodes are called with all the elements 
     * in this node, which could be very inefficient.  In order to build delta
     * only for specific elements in this node, the class extending 
     * <code>AbstractVMLayoutNode</code> should override this method. 
     * @see IVMLayoutNode#buildDelta(Object, VMDelta, int, RequestMonitor)
     */
    public void buildDelta(final Object event, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
        // Find the child nodes that have deltas for the given event. 
        final Map<IVMLayoutNode,Integer> childNodesWithDeltaFlags = getChildNodesWithDeltaFlags(event);

        // If no child layout nodes have deltas we can stop here. 
        if (childNodesWithDeltaFlags.size() == 0) {
            requestMonitor.done();
            return;
        }            
        
        // Check if the child delta only has an IModelDelta.CONTENT or 
        // IModelDelta.STATE flags.  If that's the case, we can skip 
        // creating a delta for this node, because the TreeUpdatePolicy does not 
        // use the full path from the delta to handle these flags.
        // Similarly, the index argument is not necessary either.
        boolean mustGetElements = false;
        for (int childDelta : childNodesWithDeltaFlags.values()) {
            if ((childDelta & ~IModelDelta.STATE) != 0) {
                mustGetElements = true;
            }
        }

        if (!mustGetElements) {
            callChildNodesToBuildDelta(childNodesWithDeltaFlags, parentDelta, event, requestMonitor);
        } else {
            // The given child layout nodes have deltas potentially for all elements
            // from this node.  Retrieve all elements and call the child nodes with
            // each element as the parent of their delta.
            updateElements(new ElementsUpdate(
                new DataRequestMonitor<List<Object>>(getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (fDisposed) return;
                        
                        // Check for an empty list of elements.  If it's empty then we 
                        // don't have to call the children nodes, so return here.
                        // No need to propagate error, there's no means or need to display it.
                        if (!getStatus().isOK() || getData().size() == 0) {
                            requestMonitor.done();
                            return;
                        }
                        
                        final MultiRequestMonitor<RequestMonitor> elementsDeltasMultiRequestMon = 
                            new MultiRequestMonitor<RequestMonitor>(getExecutor(), null) { 
                                @Override
                                protected void handleCompleted() {
                                    if (isDisposed()) return;
                                    requestMonitor.done();
                                }
                            };

                        // For each element from this node, create a new delta, 
                        // and then call all the child nodes to build their delta. 
                        for (int i = 0; i < getData().size(); i++) {
                            VMDelta delta = parentDelta.addNode(getData().get(i), nodeOffset + i, IModelDelta.NO_CHANGE);
                            callChildNodesToBuildDelta(
                                childNodesWithDeltaFlags, delta, event, 
                                elementsDeltasMultiRequestMon.add(new RequestMonitor(getExecutor(), null) { 
                                    @Override
                                    protected void handleCompleted() {
                                        elementsDeltasMultiRequestMon.requestMonitorDone(this);
                                    }
                                }));
                        }
                    }
                }, 
                parentDelta));
        }
    }
    
    /**
     * Calls the specified child layout nodes to build the delta for the given event.
     * @param nodes Map of layout nodes to be invoked, and the corresponding delta 
     * flags that they will generate.  This map is generated with a call to 
     * {@link #getChildNodesWithDeltaFlags(Object)}.  
     * @param delta The delta object to build on.  This delta should have been 
     * gerated by this node, unless the full delta path is not being calculated
     * due to an optimization.
     * @param event The event object that the delta is being built for.
     * @param requestMonitor The result token to invoke when the delta is completed.
     */
    protected void callChildNodesToBuildDelta(final Map<IVMLayoutNode,Integer> nodes, final VMDelta delta, final Object event, final RequestMonitor requestMonitor) {
        assert nodes.size() != 0;

        // Check if any of the child nodes are will generate IModelDelta.SELECT  or 
        // IModelDelta.EXPAND flags.  If so, we must calcuate the index for this 
        // VMC.
        boolean calculateOffsets = false;
        for (int childDelta : nodes.values()) {
            if ( (childDelta & (IModelDelta.SELECT | IModelDelta.EXPAND)) != 0 ) {
                calculateOffsets = true;
                break;
            }
        }

        getChildNodesElementOffsets(
            delta, !calculateOffsets, 
            new DataRequestMonitor<Map<IVMLayoutNode, Integer>>(getExecutor(), null) {
                @Override
                protected void handleCompleted() {
                    if (isDisposed()) return;
                    
                    final MultiRequestMonitor<RequestMonitor> childrenBuildDeltaDoneCollector = 
                        new MultiRequestMonitor<RequestMonitor>(getExecutor(), null) { 
                            @Override
                            protected void handleCompleted() {
                                if (isDisposed()) return;
                                requestMonitor.done();
                            }
                        };
    
                    // Set the total count of number of children in the parent delta.
                    delta.setChildCount(getData().get(null));
                    
                    for (IVMLayoutNode node : nodes.keySet()) {
                        node.buildDelta(
                            event, delta, getData().get(node),
                            childrenBuildDeltaDoneCollector.add(new RequestMonitor(getExecutor(), null) {
                                @Override
                                protected void handleCompleted() {
                                    childrenBuildDeltaDoneCollector.requestMonitorDone(this);
                                }})
                            );
                    }
                }
            });
    }

    /**
     * Calculates the indexes at which the elements of each of the child 
     * layout nodes begin.  These indexes are necessary to correctly 
     * calculate the deltas for elements in the child nodes.
     * @param delta The delta object to build on.  This delta should have been 
     * gerated by this node, unless the full delta path is not being calculated
     * due to an optimization.
     * @param fakeIt If true, it causes this method to fill the return data
     * structure with dummy values.  The dummy values indicate that the indexes
     * are not known and are acceptable in the delta if the delta flags being 
     * generated do not require full index information. 
     * @param rm Return token containing the results.  The result data is a 
     * mapping between the child nodes and the indexes at which the child nodes' 
     * elements begin.  There is a special value in the map with a <code>null</code>
     * key, which contains the full element count for all the nodes. 
     */
    private void getChildNodesElementOffsets(IModelDelta delta, boolean fakeIt, final DataRequestMonitor<Map<IVMLayoutNode, Integer>> rm) {
        assert getChildLayoutNodes().length != 0;
        
        if (!fakeIt) {
            final Integer[] counts = new Integer[getChildLayoutNodes().length]; 
            final MultiRequestMonitor<RequestMonitor> childrenCountMultiRequestMon = 
                new MultiRequestMonitor<RequestMonitor>(getExecutor(), rm) { 
                    @Override
                    protected void handleCompleted() {
                        if (isDisposed()) return;
                        super.handleCompleted();
                    }
                    @Override
                    protected void handleOK() {
                        Map<IVMLayoutNode, Integer> data = new HashMap<IVMLayoutNode, Integer>();
                        int offset = 0;
                        for (int i = 0; i < getChildLayoutNodes().length; i++) {
                            data.put(getChildLayoutNodes()[i], offset);
                            offset += counts[i];
                        }
                        // As the final value, put the total count in the return map, with null key.
                        data.put(null, offset);
                        rm.setData(data);
                        rm.done();
                    }
                };
            
            for (int i = 0; i < getChildLayoutNodes().length; i++) {
                final int nodeIndex = i;
                getChildLayoutNodes()[i].updateElementCount(
                    new ElementsCountUpdate(
                        childrenCountMultiRequestMon.add(
                            new DataRequestMonitor<Integer>(getExecutor(), null) {
                                @Override
                                protected void handleCompleted() {
                                    counts[nodeIndex] = getData();
                                    childrenCountMultiRequestMon.requestMonitorDone(this);
                                }
                            }), 
                        delta)
                    );
            }
        } else {
            Map<IVMLayoutNode, Integer> data = new HashMap<IVMLayoutNode, Integer>();
            for (int i = 0; i < getChildLayoutNodes().length; i++) {
                data.put(getChildLayoutNodes()[i], -1);
            }
            data.put(null, -1);
            rm.setData(data);
            rm.done();
        }            
    }
        
    protected boolean isDisposed() { 
        return fDisposed;
    }
    
    /**
     * Convenience method that returns the child layout nodes which return
     * <code>true</code> to the <code>hasDeltaFlags()</code> test for the given
     * event.   
     */
    protected Map<IVMLayoutNode, Integer> getChildNodesWithDeltaFlags(Object e) {
        Map<IVMLayoutNode, Integer> nodes = new HashMap<IVMLayoutNode, Integer>(); 
        for (final IVMLayoutNode childNode : getChildLayoutNodes()) {
            int delta = childNode.getDeltaFlags(e);
            if (delta != IModelDelta.NO_CHANGE) {
                nodes.put(childNode, delta);
            }
        }
        return nodes;
    }
    
    /**
     * Convenience method that returns a token value in case when the services
     * that the layout node depends on, are not available.
     */
    protected boolean checkUpdate(IViewerUpdate update) {
        if (update.isCanceled()) {
            update.done();
            return false;
        }
        if (fDisposed) {
            handleFailedUpdate(update);
            return false;
        }
        return true;
    }
    
    /**
     * A convenience method that completes update object in case of an error.
     * Different types of update need to have some data configured to exhibit
     * desired behavior in the viewer.
     * @param update Update to handle.
     */
    protected void handleFailedUpdate(IViewerUpdate update) {
        if (update instanceof IHasChildrenUpdate) {
            ((IHasChildrenUpdate)update).setHasChilren(false);
        } else if (update instanceof IChildrenCountUpdate) {
            ((IChildrenCountUpdate)update).setChildCount(0);            
        } else if (update instanceof ILabelUpdate) {
            ILabelUpdate labelUpdate = (ILabelUpdate)update;
            String[] columns = labelUpdate.getPresentationContext().getColumns();
            for (int i = 0; i < (columns != null ? columns.length : 1); i++) {
                labelUpdate.setLabel("...", i); //$NON-NLS-1$
            }
        }
        update.done();
    }
    
    /**
     * Implementation of basic View Model Context node functionality.  The main
     * purpose of the VMC wrapper is to re-direct adapter queries to the IVMAdapter
     * and the layout node that the given context was created by.
     * <p> 
     * Note: Deriving classes must override the Object.equals/hashCode methods.
     * This is because the VMC objects are just wrappers that are created
     * by the view model on demand, so the equals methods must use the object
     * being wrapped by the VMC to perform a meaningful comparison.    
     */
    abstract public static class AbstractVMContext implements IVMContext {
        protected final IVMAdapter fVMAdapter;
        protected final IVMLayoutNode fLayoutNode;
        
        public AbstractVMContext(IVMAdapter adapter, IVMLayoutNode node) {
            fVMAdapter = adapter;
            fLayoutNode = node;
        }
        
        public IVMLayoutNode getLayoutNode() { return fLayoutNode; }

        /**
         * IAdapter implementation returns the IVMAdapter instance for the 
         * interfaces that are actually implemented by the VM Adapter.  These
         * should at least include {@link IElementContentProvider}, 
         * {@link IModelProxyFactory}, and 
         * {@link IColumnPresentationFactory}.
         */
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            if (adapter.isInstance(fVMAdapter)) {
                return fVMAdapter;
            } else if (adapter.isInstance(fLayoutNode)) {
                return fLayoutNode;
            }
            return null;
        }

        /** Deriving classes must override. */
        @Override
        abstract public boolean equals(Object obj);
        
        /** Deriving classes must override. */
        @Override
        abstract public int hashCode();
    }
    
    protected TreePath getTreePathFromDelta(IModelDelta delta) {
        List<Object> elementList = new LinkedList<Object>();
        IModelDelta listDelta = delta;
        elementList.add(0, listDelta.getElement());
        while (listDelta.getParentDelta() != null) {
            elementList.add(0, listDelta.getElement());
            listDelta = listDelta.getParentDelta();
        }
        return new TreePath(elementList.toArray());
    }
    
    protected class ViewerUpdate implements IViewerUpdate {
        
		final private RequestMonitor fRequestMonitor;
        final private TreePath fTreePath;
        private IStatus fStatus;
        private boolean fCancelled = false;
        
        public ViewerUpdate(RequestMonitor requestMonitor, IModelDelta delta) {
            fRequestMonitor = requestMonitor;
            List<Object> elementList = new LinkedList<Object>();
            IModelDelta listDelta = delta;
            elementList.add(0, listDelta.getElement());
            while (listDelta.getParentDelta() != null) {
                elementList.add(0, listDelta.getElement());
                listDelta = listDelta.getParentDelta();
            }
            fTreePath = new TreePath(elementList.toArray());
        }

        public ViewerUpdate(RequestMonitor requestMonitor, TreePath path) {
            fRequestMonitor = requestMonitor;
            fTreePath = path;
        }

        public Object getElement(TreePath path) { 
            // If not asking for root, just return the last segment in path.
            if (path.getSegmentCount() > 0) {
                return path.getLastSegment();
            }
            // Calculate the root of the viewer.
            return getVMProvider().getRootElement();
        }

        public Object getElement() {
            return getElementPath().getLastSegment();
        }
        
        public IPresentationContext getPresentationContext() { return getVMProvider().getPresentationContext(); }
        public TreePath getElementPath() { return fTreePath; }
        public IStatus getStatus() { return fStatus; }
        public void setStatus(IStatus status) { fStatus = status; }
        public boolean isCanceled() { return fCancelled; }
        public void cancel() { fCancelled = true; }
        
        public void done() {
            try {
                fRequestMonitor.done();
            } catch (RejectedExecutionException e) {
            }
        }
    }
    
    protected class ElementsCountUpdate extends ViewerUpdate implements IChildrenCountUpdate {
        private final DataRequestMonitor<Integer> fElementCountRequestMonitor;
        
        public ElementsCountUpdate(DataRequestMonitor<Integer> rm, IModelDelta delta) {
            super(rm, delta);
            fElementCountRequestMonitor = rm;
        }

        public void setChildCount(int numChildren) {
            fElementCountRequestMonitor.setData(numChildren);
        }
    }

    protected class ElementsUpdate extends ViewerUpdate implements IChildrenUpdate {
        private final List<Object> fChildren = new ArrayList<Object>();
        private DataRequestMonitor<List<Object>> fElementUpdateRequestMonitor;
        
        public ElementsUpdate(DataRequestMonitor<List<Object>> rm, IModelDelta delta) {
            super(rm, delta);
            fElementUpdateRequestMonitor = rm;
            fElementUpdateRequestMonitor.setData(fChildren);
        }

        public ElementsUpdate(DataRequestMonitor<List<Object>> rm, TreePath path) {
            super(rm, path);
            fElementUpdateRequestMonitor = rm;
            fElementUpdateRequestMonitor.setData(fChildren);
        }

        public int getOffset() {
            return -1;
        }

        public int getLength() {
            return -1;
        }

        public void setChild(Object child, int offset) {
            fChildren.add(offset, child);
        }
        
        @Override
        public String toString() {
            return "VMElementsUpdate for all elements under parent = " + getElement(); //$NON-NLS-1$
        }
    }

}
