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
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousLabelAdapter;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;

/**
 * View model provider implements the asynchronous view model functionality for 
 * a single view.  This provider is just a holder which further delegates the
 * model provider functionality to the view model layout nodes that need
 * to be configured with each provider.
 * <p>
 * The view model provider, often does not provide the model for the entire 
 * view.  Rather, it needs to be able to plug in at any level in the viewer's
 * content model and provide data for a sub-tree.
 * 
 * @see IAsynchronousContentAdapter
 * @see IAsynchronousLabelAdapter
 * @see IModelProxy
 * @see IVMLayoutNode
 */
@ConfinedToDsfExecutor("getVMAdapter#getExecutor")
@SuppressWarnings("restriction")
abstract public class AbstractVMProvider implements IVMProvider
{
    private final AbstractVMAdapter fVMAdapter;
    private final IPresentationContext fPresentationContext;
    private final ModelProxy fModelProxy = new ModelProxy();
    private boolean fDisposed = false;

    /**
     * The root node for this model provider.  The root layout node could be 
     * null when first created, to allow sub-classes to prorperly configure the 
     * root node in the sub-class constructor.  
     */
    private AtomicReference<IVMRootLayoutNode> fRootLayoutNodeRef = new AtomicReference<IVMRootLayoutNode>();
    
    /**
     * Constructs the view model provider for given DSF session.  The 
     * constructor is thread-safe to allow VM provider to be constructed
     * synchronously when a call to getAdapter() is made on an element 
     * in a view.
     */
    public AbstractVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext) {
        fVMAdapter = adapter;
        fPresentationContext = presentationContext;
    }    

    public IPresentationContext getPresentationContext() {
        return fPresentationContext;
    }

    public AbstractVMAdapter getVMAdapter() {
        return fVMAdapter;
    }

    /**
     * Sets the root node for this provider.  
     */
    @ThreadSafe
    protected void setRootLayoutNode(IVMRootLayoutNode rootLayoutNode) {
        final IVMRootLayoutNode oldRootLayoutNode = fRootLayoutNodeRef.getAndSet(rootLayoutNode); 
        if (oldRootLayoutNode != null) {
            oldRootLayoutNode.dispose();
        }
    }
    
    protected ModelProxy getModelProxy() {
        return fModelProxy;
    }
    
    @ThreadSafe
    public IVMRootLayoutNode getRootLayoutNode() {
        return fRootLayoutNodeRef.get();
    }
    
    /** Called to dispose the provider. */ 
    public void dispose() {
        fDisposed = true;
        if (fRootLayoutNodeRef.get() != null) {
            fRootLayoutNodeRef.get().dispose();
        }
    }

    protected boolean isDisposed() {
        return fDisposed;
    }
    
    /**
     * Convenience method to access the View Model's executor.
     */
    public DsfExecutor getExecutor() { return fVMAdapter.getExecutor(); }
    

    public void update(IHasChildrenUpdate[] updates) {
        // Sort the updates by the layout node.
        Map<IVMLayoutNode,List<IHasChildrenUpdate>> nodeUpdatesMap = new HashMap<IVMLayoutNode,List<IHasChildrenUpdate>>();
        for (IHasChildrenUpdate update : updates) {
            // Get the VM Context for last element in path.  
            IVMLayoutNode layoutNode = getLayoutNodeObject(update.getElement());
            if (layoutNode == null) {
                // Stale update, most likely as a result of the layout nodes being
                // changed.  Just ignore it.
                if (!update.isCanceled()) update.done();
                continue;
            }        
            if (!nodeUpdatesMap.containsKey(layoutNode)) {
                nodeUpdatesMap.put(layoutNode, new ArrayList<IHasChildrenUpdate>());
            }
            nodeUpdatesMap.get(layoutNode).add(update);
        }
        
        // Iterate through the nodes in the sorted map.
        for (IVMLayoutNode node :  nodeUpdatesMap.keySet()) {
            updateNode(node, nodeUpdatesMap.get(node).toArray(new IHasChildrenUpdate[nodeUpdatesMap.get(node).size()])); 
        }
    }

    private void updateNode(IVMLayoutNode node, final IHasChildrenUpdate[] updates) {
        // If parent element's layout node has no children, just set the 
        // result and coninue to next element.
        if (node.getChildLayoutNodes().length == 0) {
            for (IHasChildrenUpdate update : updates) {
                update.setHasChilren(false);
            }
            return;
        }

        HasElementsUpdate[][] elementsUpdates = 
            new HasElementsUpdate[node.getChildLayoutNodes().length][updates.length];
        for (int i = 0; i < updates.length; i ++) 
        {
            final IHasChildrenUpdate update = updates[i];
            for (int j = 0; j < node.getChildLayoutNodes().length; j++) 
            {
                final MultiRequestMonitor<DataRequestMonitor<Boolean>> hasChildrenMultiRequestMon = 
                    new MultiRequestMonitor<DataRequestMonitor<Boolean>>(getExecutor(), null) { 
                        @Override
                        protected void handleCompleted() {
                            // Status is OK, only if all request monitors are OK. 
                            if (getStatus().isOK()) { 
                                boolean isContainer = false;
                                for (DataRequestMonitor<Boolean> hasElementsDone : getRequestMonitors().keySet()) {
                                    isContainer |= hasElementsDone.getStatus().isOK() &&
                                                   hasElementsDone.getData().booleanValue();
                                }
                                update.setHasChilren(isContainer);
                                update.done();
                            }
                        }
                    };
                
                elementsUpdates[j][i] = new HasElementsUpdate(
                    update,
                    hasChildrenMultiRequestMon.add(
                        new DataRequestMonitor<Boolean>(getExecutor(), null) {
                            @Override
                            protected void handleCompleted() {
                                hasChildrenMultiRequestMon.requestMonitorDone(this);
                            }
                        }));
            }
        }
            
        for (int j = 0; j < node.getChildLayoutNodes().length; j++) {
            node.getChildLayoutNodes()[j].updateHasElements(elementsUpdates[j]);
        }
    }
    

    public void update(final IChildrenCountUpdate[] updates) {
        for (final IChildrenCountUpdate update : updates) {
            if (update.isCanceled()) continue;

            getChildrenCountsForNode(
                update, update.getElementPath(),
                new DataRequestMonitor<Integer[]>(getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (getStatus().isOK()) {
                            int numChildren = 0;
                            for (Integer count : getData()) {
                                numChildren += count.intValue();
                            }
                            update.setChildCount(numChildren);
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
            getChildrenCountsForNode(
                update, update.getElementPath(), 
                new DataRequestMonitor<Integer[]>(getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!getStatus().isOK()) {
                            update.done();
                            return;
                        } 
                        updateChildrenWithCounts(update, getData());
                    }
                });
        }            
    }
    

    private void getChildrenCountsForNode(IViewerUpdate update, TreePath elementPath, final DataRequestMonitor<Integer[]> rm) {
        if (isDisposed()) return;
        
        // Get the VM Context for last element in path.  
        final IVMLayoutNode layoutNode = getLayoutNodeObject(update.getElement());
        if (layoutNode == null) {
            // Stale update. Just ignore.
            rm.setStatus(new Status(
                IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Stale update.", null));   //$NON-NLS-1$
            rm.done();
            return;
        }        

        IVMLayoutNode[] childNodes = layoutNode.getChildLayoutNodes();

        // If parent element's layout node has no children, just mark done and 
        // return.
        if (childNodes.length == 0) {
            rm.setData(new Integer[0]);
            rm.done();
            return;
        }

        
        // Get the mapping of all the counts.
        final Integer[] counts = new Integer[childNodes.length]; 
        final MultiRequestMonitor<RequestMonitor> childrenCountMultiReqMon = 
            new MultiRequestMonitor<RequestMonitor>(getExecutor(), rm) { 
                @Override
                protected void handleCompleted() {
                    if (!fDisposed) super.handleCompleted();
                }

                @Override
                protected void handleOK() {
                    rm.setData(counts);
                    rm.done();
                }
            };
        
        for (int i = 0; i < childNodes.length; i++) {
            final int nodeIndex = i;
            childNodes[i].updateElementCount(
                new ElementsCountUpdate(
                    update,  
                    childrenCountMultiReqMon.add(
                        new DataRequestMonitor<Integer>(getExecutor(), null) {
                            @Override
                            protected void handleOK() {
                                counts[nodeIndex] = getData();
                            }
                            
                            @Override
                            protected void handleCompleted() {
                                super.handleCompleted();
                                childrenCountMultiReqMon.requestMonitorDone(this);
                            }
                        }), 
                    elementPath)
                );
        }
    }
    
    private void updateChildrenWithCounts(final IChildrenUpdate update, Integer[] nodeElementCounts) {
        final IVMLayoutNode layoutNode = getLayoutNodeObject(update.getElement());
        if (layoutNode == null) {
            // Stale update. Just ignore.
            if (!update.isCanceled()) update.done();
            return;
        }        

        // Create the multi request monitor to mark update when querying all 
        // children nodes is finished.
        final MultiRequestMonitor<RequestMonitor> elementsMultiRequestMon = 
            new MultiRequestMonitor<RequestMonitor>(getExecutor(), null) { 
                @Override
                protected void handleCompleted() {
                    if (!update.isCanceled()) update.done();
                }
            };

        // Iterate through all child nodes and if requested range matches, call them to 
        // get their elements.
        int updateStartIdx = update.getOffset();
        int updateEndIdx = update.getOffset() + update.getLength();
        int idx = 0;
        IVMLayoutNode[] layoutNodes = layoutNode.getChildLayoutNodes();
        for (int i = 0; i < layoutNodes.length; i++) {
            final int nodeStartIdx = idx;
            final int nodeEndIdx = idx + nodeElementCounts[i];
            idx = nodeEndIdx;

            // Check if update range overlaps the node's range.
            if (updateStartIdx <= nodeEndIdx && updateEndIdx > nodeStartIdx) {
                final int elementsStartIdx = Math.max(updateStartIdx - nodeStartIdx, 0);
                final int elementsEndIdx = Math.min(updateEndIdx - nodeStartIdx, nodeElementCounts[i]);
                
                layoutNodes[i].updateElements(
                    new ElementsUpdate(
                        update,  
                        elementsMultiRequestMon.add(new RequestMonitor(getExecutor(), null) { 
                            @Override
                            protected void handleCompleted() {
                                elementsMultiRequestMon.requestMonitorDone(this);
                            }
                        }),
                        nodeStartIdx, elementsStartIdx, elementsEndIdx - elementsStartIdx)
                    ); 
            }
        }
        
        // Guard against invalid queries.
        if (elementsMultiRequestMon.getRequestMonitors().isEmpty()) {
            update.done();
        }

    }
    
    public ModelProxy createModelProxy(Object element, IPresentationContext context) {
        /*
         * Model proxy is the object that correlates events from the data model 
         * into view model deltas that the view can process.  We only need to 
         * create a proxy for the root element of the tree.
         */
        if (getRootLayoutNode() != null && 
            element.equals(getRootLayoutNode().getRootObject()))
        {
            return fModelProxy;
        } 
        return null;
    }

    /**
     * Creates the column presentation for the given object.  This method is meant
     * to be overriden by deriving class to provide view-specific functionality.
     * The default is to return null, meaning no columns. 
     * <p>
     * The viewer only reads the column presentation for the root/input element of 
     * the tree/table, so the VMProvider must be configured to own the root element 
     * in the view in order for this setting to be effective.   
     * <p>
     * Note: since the IColumnEditorFactory interface is synchronous, and since
     * column info is fairly static, this method is thread-safe, and it will
     * not be called on the executor thread.
     * 
     * @see IColumnPresentationFactory#createColumnPresentation(IPresentationContext, Object)
     */
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        return null;
    }

    /**
     * Returns the ID of the column presentation for the given object.  This method 
     * is meant to be overriden by deriving class to provide view-specific 
     * functionality. The default is to return null, meaning no columns. 
     * <p>
     * The viewer only reads the column presentation for the root/input element of 
     * the tree/table, so the VMProvider must be configured to own the root element 
     * in the view in order for this setting to be effective.   
     * <p>
     * Note: since the IColumnEditorFactory interface is synchronous, and since
     * column info is fairly static, this method is thread-safe, and it will
     * not be called on the executor thread.
     * 
     * @see IColumnEditorFactory#getColumnEditorId(IPresentationContext, Object)
     */
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return null;
    }


    /**
     * Convenience method that finds the VMC corresponding to given parent 
     * argument given to isContainer() or retrieveChildren().  
     * @param object Object to find the VMC for.
     * @return parent VMC, if null it indicates that the object did not originate 
     * from this view or is stale.
     */
    private IVMLayoutNode getLayoutNodeObject(Object element) {
        /*
         * First check to see if the parent object is the root object of the 
         * hierarchy.  If that's the case, then retrieve the correcponding
         * root VMC from the root node, and pass this root vmc to the root's 
         * child layout nodes.
         */
        IVMRootLayoutNode rootLayoutNode = getRootLayoutNode();
        if (rootLayoutNode == null) {
            return null;
        } 
        else if (element.equals(rootLayoutNode.getRootObject())) {
            return rootLayoutNode;
        } 
        else if (element instanceof IVMContext){
            /*
             * The parent is a VMC.  Check to make sure that the VMC 
             * originated from a node in this ViewModelProvider.  If it didn't
             * it is most likely a result of a change in view layout, and this 
             * request is a stale request.  So just ignore it.
             */
            if (isOurLayoutNode( ((IVMContext)element).getLayoutNode(), 
                                 new IVMLayoutNode[] { rootLayoutNode } )) 
            {
                return ((IVMContext)element).getLayoutNode();
            }
        } 
        return null;
    }
    
    /**
     * Convenience method which checks whether given layout node is a node 
     * that is configured in this ViewModelProvider.  Implementation 
     * recursively walks the layout hierarchy, and returns true if it finds
     * the node. 
     */
    private boolean isOurLayoutNode(IVMLayoutNode layoutNode, IVMLayoutNode[] nodesToSearch) {
        for (IVMLayoutNode node : nodesToSearch) {
            if (node == layoutNode) return true;
            if (isOurLayoutNode(layoutNode, node.getChildLayoutNodes())) return true;
        }
        return false;
    }
        
    
    @ThreadSafe
    protected class ModelProxy extends AbstractModelProxy {
        /**
         * Counter for whether the model proxy is currently installed in the viewer.
         * Data model events are processed only if the model proxy is active.   
         */
        private int fProxyActive = 0;
        
        /** 
         * Scheduling rule for running the update jobs.  
         */
        private ISchedulingRule fModelChangeRule = new ISchedulingRule() {
            public boolean contains(ISchedulingRule rule) { return this == rule; }
            public boolean isConflicting(ISchedulingRule rule) { return rule == this; }
        };

        @Override
        public void installed(Viewer viewer) {
            fProxyActive++;
        }
        
        @Override
        public void dispose() {
            fProxyActive--;
            super.dispose();
        }
        
        /**
         * Fires given delta using a job.  Processing the delta on the dispatch
         * thread can lead to dead-locks.
         * @param delta
         */
        public void fireModelChangedNonDispatch(final IModelDelta delta) {
            if (fProxyActive <= 0) return;
            
            Job job = new Job("Processing view model delta.") { //$NON-NLS-1$
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    fireModelChanged(delta);
                    return Status.OK_STATUS;
                }
            };
            job.setPriority(Job.INTERACTIVE);
            job.setRule(fModelChangeRule);
            job.schedule();
        }

    }
    
    class ViewerUpdate implements IViewerUpdate {
        
		private IStatus fStatus;
        final private RequestMonitor fRequestMonitor;
        final protected IViewerUpdate fClientUpdate;
        
        public ViewerUpdate(IViewerUpdate clientUpdate, RequestMonitor requestMonitor) {
            fRequestMonitor = requestMonitor;
            fClientUpdate = clientUpdate;
        }

        public Object getElement() { return fClientUpdate.getElement(); }
        public TreePath getElementPath() { return fClientUpdate.getElementPath(); }
        public IPresentationContext getPresentationContext() { return fClientUpdate.getPresentationContext(); }

        public IStatus getStatus() { return fStatus; }
        public void setStatus(IStatus status) { fStatus = status; }
        public boolean isCanceled() { return fClientUpdate.isCanceled(); }
        public void cancel() {
            fClientUpdate.cancel();
        }

        public void done() { 
            try {
                fRequestMonitor.done();
            } catch (RejectedExecutionException e) { // Ignore
            }
        }

    }

    class HasElementsUpdate extends ViewerUpdate implements IHasChildrenUpdate {

        final private DataRequestMonitor<Boolean> fHasElemsRequestMonitor;
        
        HasElementsUpdate(IHasChildrenUpdate clientUpdate, DataRequestMonitor<Boolean> rm) {
            super(clientUpdate, rm);
            fHasElemsRequestMonitor = rm;
        }
        
        @Override
        public TreePath getElementPath() {
            return ((IHasChildrenUpdate)fClientUpdate).getElementPath();
        }

        public void setHasChilren(boolean hasChildren) {
            fHasElemsRequestMonitor.setData(hasChildren);
        }

        @Override
        public void done() {
            assert fHasElemsRequestMonitor.getData() != null || !fHasElemsRequestMonitor.getStatus().isOK();
            super.done();            
        }
    }

    class ElementsCountUpdate extends ViewerUpdate implements IChildrenCountUpdate {
        final private DataRequestMonitor<Integer> fCountRequestMonitor;
        final private TreePath fElementPath;
        
        ElementsCountUpdate(IViewerUpdate clientUpdate, DataRequestMonitor<Integer> rm, TreePath elementPath) {
            super(clientUpdate, rm);
            fElementPath = elementPath;
            fCountRequestMonitor = rm;
        }

        @Override
        public TreePath getElementPath() {
            return fElementPath;
        }

        public void setChildCount(int count) {
            fCountRequestMonitor.setData(count);
        }
        
        @Override
        public void done() {
            assert fCountRequestMonitor.getData() != null || !fCountRequestMonitor.getStatus().isOK();
            super.done();
        }

    }
    
    class ElementsUpdate extends ViewerUpdate implements IChildrenUpdate {
        private final int fClientOffset;
        private final int fOffset;
        private final int fLength;
        
        ElementsUpdate(IChildrenUpdate clientUpdate, RequestMonitor requestMonitor, int clientOffset, int offset, int length) {
            super(clientUpdate, requestMonitor);
            fClientOffset = clientOffset;
            fOffset = offset;
            fLength = length;
        }

        public int getOffset() {
            return fOffset;
        }

        public int getLength() {
            return fLength;
        }

        @Override
        public TreePath getElementPath() {
            return ((IChildrenUpdate)fClientUpdate).getElementPath();
        }

        public void setChild(Object child, int offset) {
            if (offset >= fOffset && offset < (fOffset + fLength)) {
                ((IChildrenUpdate)fClientUpdate).setChild(child, fClientOffset + offset);
            }
        }

        @Override
        public String toString() {
            return "ElementsUpdate for elements under parent = " + getElement() + ", in range " + getOffset() + " -> " + (getOffset() + getLength());  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
        }

    }
}
