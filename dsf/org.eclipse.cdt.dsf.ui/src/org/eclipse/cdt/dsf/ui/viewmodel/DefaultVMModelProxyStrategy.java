/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - adapted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;


/**
 * This is the default implementation of {@link IModelProxy} interface for 
 * use by a view model provider.  It implements an algorithm to walk the
 * tree hierarchy of nodes configured with a provider in order to compose
 * an {@link IModelDelta} for a given data model event.
 * <p/>
 * This class is closely linked with a view model provider which is required
 * for the constructor.  The view model provider is used to access the correct
 * executor and the node hierarchy. 
 */
@ConfinedToDsfExecutor("#getProvider()#getExecutor()")
@SuppressWarnings("restriction")
public class DefaultVMModelProxyStrategy implements IVMModelProxy, IVMModelProxyExtension {

    private final AbstractVMProvider fProvider;
    private final Object fRootElement;
    private IPresentationContext fContext;
    private Viewer fViewer;
    private boolean fDisposed = false;
    private ListenerList fListeners = new ListenerList();
	private IDoubleClickListener fDoubleClickListener;
    
    /**
     * Creates this model proxy strategy for the given provider. 
     */
    public DefaultVMModelProxyStrategy(AbstractVMProvider provider, Object rootElement) {
        fProvider = provider;
        fRootElement = rootElement;
    }
    
    public boolean isDeltaEvent(Object event) {
        IRootVMNode rootNode = getVMProvider().getRootVMNode();
        return rootNode != null && 
                rootNode.isDeltaEvent(getRootElement(), event) && 
                getDeltaFlags(rootNode, null, event) != 0; 
    }

    
    /**
     * Returns the view model provider that this strategy is configured for.
     * @return
     */
    protected AbstractVMProvider getVMProvider() {
        return fProvider;
    }
    
    
    private Object[] getListeners() {
        return fListeners.getListeners();
    }

    public void addModelChangedListener(IModelChangedListener listener) {
        fListeners.add(listener);
    }

    public void removeModelChangedListener(IModelChangedListener listener) {
        fListeners.remove(listener);
    }

    public Object getRootElement() {
        return fRootElement;
    }
    
    /** @since 1.1 */
   public Object getViewerInput() {
        return fRootElement;
    }
    
   /** @since 1.1 */
    public TreePath getRootPath() {
        return TreePath.EMPTY;
    }
    
    /**
     * Notifies registered listeners of the given delta.
     * 
     * @param delta model delta to broadcast
     */
    public void fireModelChanged(IModelDelta delta) {
        final IModelDelta root = getRootDelta(delta);
        Object[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++) {
            final IModelChangedListener listener = (IModelChangedListener) listeners[i];
            ISafeRunnable safeRunnable = new ISafeRunnable() {
                public void handleException(Throwable exception) {
                    DebugUIPlugin.log(exception);
                }

                public void run() throws Exception {
                    listener.modelChanged(root, DefaultVMModelProxyStrategy.this);
                }

            };
            SafeRunner.run(safeRunnable);
        }
    }
    
    /**
     * Convenience method that returns the root node of the given delta.
     * 
     * @param delta delta node
     * @return returns the root of the given delta
     */
    protected IModelDelta getRootDelta(IModelDelta delta) {
        IModelDelta parent = delta.getParentDelta();
        while (parent != null) {
            delta = parent;
            parent = delta.getParentDelta();
        }
        return delta;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.IModelProxy#dispose()
     */
    public void dispose() {
        fDisposed = true;
        if (fViewer instanceof StructuredViewer && fDoubleClickListener != null) {
        	((StructuredViewer) fViewer).removeDoubleClickListener(fDoubleClickListener);
        	fDoubleClickListener= null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.IModelProxy#init(org.eclipse.debug.internal.ui.viewers.IPresentationContext)
     */
    public void init(IPresentationContext context) {
        fDisposed = false;
        fContext = context;
    }
    
    /**
     * Returns the context this model proxy is installed in.
     * 
     * @return presentation context, or <code>null</code> if this
     *  model proxy has been disposed
     */
    public IPresentationContext getPresentationContext() {
        return fContext;
    }

    /* (non-Javadoc)
     * 
     * Subclasses should override as required.
     * 
     * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy#installed(org.eclipse.jface.viewers.Viewer)
     */
    public void installed(final Viewer viewer) {  
        fViewer = viewer;
        getVMProvider().getExecutor().execute( new DsfRunnable() {
            public void run() {
                fProvider.handleEvent(new ModelProxyInstalledEvent(DefaultVMModelProxyStrategy.this, viewer, fRootElement));
            }
        });
        if (fViewer instanceof StructuredViewer && fDoubleClickListener == null) {
        	((StructuredViewer) fViewer).addDoubleClickListener(fDoubleClickListener= new IDoubleClickListener() {
        		public void doubleClick(DoubleClickEvent e) {
        			handleDoubleClick(e);
        		}
        	});
        }
    }
    
    /**
     * Handle viewer double click.
     * 
	 * @param e  the event
	 * 
	 * @since 1.1
	 */
	protected void handleDoubleClick(final DoubleClickEvent e) {
		final AbstractVMProvider vmProvider= getVMProvider();
		if (!vmProvider.isDisposed()) {
	        ISelection selection = e.getSelection();
			if (!selection.isEmpty() && selection instanceof ITreeSelection) {
	            final TreePath path = ((ITreeSelection)selection).getPaths()[0];
	    	    final Object input = e.getViewer().getInput();
	    	    
	            vmProvider.getExecutor().execute( new DsfRunnable() {
	                public void run() {
	                    Object rootElement = getRootElement();
	                    boolean eventContainsRootElement = rootElement.equals(input);
	                    for (int i = 0; !eventContainsRootElement && i < path.getSegmentCount(); i++) {
	                        eventContainsRootElement = rootElement.equals(path.getSegment(i));
	                    }

	                    if (eventContainsRootElement) {
	                        vmProvider.handleEvent(e);
	                    }
	                }
	            });
	        }
		}
	}

	/**
     * Returns the viewer this proxy is installed in.
     * 
     * @return viewer or <code>null</code> if not installed
     * 
     * @since 1.1
     */
    public Viewer getViewer() {
        return fViewer;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy#isDisposed()
     */
    public boolean isDisposed() {
        return fDisposed;
    }   

    /**
     * Recursively calls the VM nodes in the hierarchy of the given node
     * to calculate the delta flags that are 
     * <p/> 
     * Note: If a child node has a <code>IModelDelta.CONTENT</code> delta 
     * flag, it means that this flag will be added to this node's element.  
     * To allow for optimization change the child's <code>IModelDelta.CONTENT</code>
     * flag into a <code>IModelDelta.STATE</code> flag.
     * 
     * @param node
     * @param event
     * @return
     */
    protected int getDeltaFlags(IVMNode node, ModelDelta parentDelta, Object event) {
        int flags = node.getDeltaFlags(event);
        for (IVMNode childNode : getVMProvider().getChildVMNodes(node)) {
            if (!childNode.equals(node)) {
                int childNodeDeltaFlags = getDeltaFlags(childNode, parentDelta, event);
                if ((childNodeDeltaFlags & IModelDelta.CONTENT) != 0) {
                    childNodeDeltaFlags &= ~IModelDelta.CONTENT;
                    childNodeDeltaFlags |= IModelDelta.STATE;
                }
                flags |= childNodeDeltaFlags;
            }
        }
        // Optimization: If the parent delta contains the "content" flag, we do 
        // not need to add it to the child.  This can shorten delta processing 
        // considerably so check for it.
        while (parentDelta != null) {
            if ( (parentDelta.getFlags() & IModelDelta.CONTENT) != 0 ) {
                flags = flags & ~IModelDelta.CONTENT & ~IModelDelta.STATE;
                break;
            }
            parentDelta = (ModelDelta)parentDelta.getParentDelta();
        }
        return flags;
    }
    
    /**
     * Default implementation creates a delta assuming that the root node
     * is the input object into the view.  
     */
    public void createDelta(final Object event, final DataRequestMonitor<IModelDelta> rm) {
        final IRootVMNode rootNode = getVMProvider().getRootVMNode(); 
        
        // Always create the rootDelta, no matter what delta flags the child nodes have.
        rootNode.createRootDelta(
            getRootElement(), event, 
            new DataRequestMonitor<VMDelta>(getVMProvider().getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    // Find the root delta for the whole view to use when firing the delta. 
                    // Note: the view root is going to be different than the model root
                    // in case when the view model provider is registered to populate only 
                    // a sub-tree of a view.
                    final IModelDelta viewRootDelta = getRootDelta(getData());
                   
                    // Find the child nodes that have deltas for the given event. 
                    final Map<IVMNode,Integer> childNodesWithDeltaFlags = getChildNodesWithDeltaFlags(rootNode, getData(), event);

                    // If no child nodes have deltas we can stop here. 
                    if (childNodesWithDeltaFlags.size() == 0) {
                        rm.setData(viewRootDelta);
                        rm.done();
                        return;
                    }            
                    
                    callChildNodesToBuildDelta(
                        rootNode, 
                        childNodesWithDeltaFlags, getData(), event, 
                        new RequestMonitor(getVMProvider().getExecutor(), rm) {
                            @Override
                            protected void handleSuccess() {
                                rm.setData(viewRootDelta);
                                rm.done();
                            }
                        });
                }
            });
    }

    protected void buildChildDeltas(final IVMNode node, final Object event, final VMDelta parentDelta, 
        final int nodeOffset, final RequestMonitor rm) 
    {
        node.getContextsForEvent(
            parentDelta,
            event, 
            new DataRequestMonitor<IVMContext[]>(getVMProvider().getExecutor(), rm) {
                @Override
                protected void handleCompleted() {
                    if (isSuccess()) {
                        assert getData() != null;
                        buildChildDeltasForEventContext(getData(), node, event, parentDelta, nodeOffset, rm);
                    } else if (getStatus().getCode() == IDsfStatusConstants.NOT_SUPPORTED) {
                        // The DMC for this node was not found in the event.  Call the 
                        // super-class to resort to the default behavior which may add a 
                        // delta for every element in this node.
                        buildChildDeltasForAllContexts(node, event, parentDelta, nodeOffset, rm);
                    } else {
                        super.handleCompleted();
                    }
                }
            });
    }
    
    protected void buildChildDeltasForEventContext(final IVMContext[] vmcs, final IVMNode node, final Object event, 
        final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) 
    {
        final Map<IVMNode,Integer> childNodeDeltas = getChildNodesWithDeltaFlags(node, parentDelta, event);
        if (childNodeDeltas.size() == 0) {
            // There are no child nodes with deltas, just return to parent.
            requestMonitor.done();
            return;
        }            

        // Check if any of the child nodes are will generate IModelDelta.SELECT  or 
        // IModelDelta.EXPAND flags.  If so, we must calculate the index for this 
        // VMC.
        boolean calculateIndex = false;
        if (nodeOffset >= 0) {
            for (int childDelta : childNodeDeltas.values()) {
                if ( (childDelta & (IModelDelta.SELECT | IModelDelta.EXPAND)) != 0 ) {
                    calculateIndex = true;
                    break;
                }
            }
        }
        
        if (calculateIndex) {
            // Calculate the index of this node by retrieving all the 
            // elements and then finding the DMC that the event is for.  
            getVMProvider().updateNode(
                node, 
                new VMChildrenUpdate(
                    parentDelta, getVMProvider().getPresentationContext(), -1, -1,
                    new DataRequestMonitor<List<Object>>(getVMProvider().getExecutor(), requestMonitor) {
                        @Override
                        protected void handleSuccess() {
                            // Check for an empty list of elements.  If it's empty then we 
                            // don't have to call the children nodes, so return here.
                            // No need to propagate error, there's no means or need to display it.
                            if (getData().isEmpty()) {
                                requestMonitor.done();
                                return;
                            }

                            CountingRequestMonitor countingRm = 
                                new CountingRequestMonitor(getVMProvider().getExecutor(), requestMonitor);
                            
                            int count = 0;
                            for (IVMContext vmc : vmcs) {
                                // Find the index of the vmc in the full list of elements.
                                int i;
                                for (i = 0; i < getData().size(); i++) {
                                    if (vmc.equals(getData().get(i))) break;
                                }                            
                                if (i == getData().size()) {
                                    // Element not found, no need to generate the delta.
                                    continue;
                                }
                                
                                // Optimization: Try to find a delta with a matching element, if found use it.  
                                // Otherwise create a new delta for the event element.
                                int elementIndex = nodeOffset + i;
                                VMDelta delta = parentDelta.getChildDelta(vmc);
                                if (delta == null || delta.getIndex() != elementIndex) {
                                    delta = parentDelta.addNode(vmc, elementIndex, IModelDelta.NO_CHANGE);
                                }
                                
                                callChildNodesToBuildDelta(node, childNodeDeltas, delta, event, countingRm);
                                count++;
                            }
                            countingRm.setDoneCount(count);
                        }
                    }));
        } else {
            CountingRequestMonitor countingRm = 
                new CountingRequestMonitor(getVMProvider().getExecutor(), requestMonitor);
            int count = 0;
            for (IVMContext vmc : vmcs) {
                // Optimization: Try to find a delta with a matching element, if found use it.  
                // Otherwise create a new delta for the event element.    
                VMDelta delta = parentDelta.getChildDelta(vmc);
                if (delta == null) {
                    delta = parentDelta.addNode(vmc, IModelDelta.NO_CHANGE);
                }
                callChildNodesToBuildDelta(node, childNodeDeltas, delta, event, requestMonitor);
                count++;
            }
            countingRm.setDoneCount(count);
        }            
    }
    
    /** 
     * Base implementation that handles calling child nodes to build 
     * the model delta.  The child nodes are called with all the elements 
     * in this node, which could be very inefficient.  In order to build delta
     * only for specific elements in this node, the class extending 
     * <code>AbstractVMNode</code> should override this method. 
     * @see IVMNode#buildDelta(Object, ModelDelta, int, RequestMonitor)
     */
    protected void buildChildDeltasForAllContexts(final IVMNode node, final Object event, final VMDelta parentDelta,
        final int nodeOffset, final RequestMonitor requestMonitor) 
    {
        // Find the child nodes that have deltas for the given event. 
        final Map<IVMNode,Integer> childNodesWithDeltaFlags = getChildNodesWithDeltaFlags(node, parentDelta, event);

        // If no child nodes have deltas we can stop here. 
        if (childNodesWithDeltaFlags.size() == 0) {
            requestMonitor.done();
            return;
        }            
        
        // Check if the child delta only has an IModelDelta.STATE flag.  
        // If that's the case, we can skip creating a delta for this node, 
        // because the TreeUpdatePolicy does not use the full path from the 
        // delta to handle these flags. Similarly, the index argument is 
        // not necessary either.
        boolean mustGetElements = false;
        for (int childDelta : childNodesWithDeltaFlags.values()) {
            if ((childDelta & ~IModelDelta.STATE) != 0) {
                mustGetElements = true;
            }
        }

        if (!mustGetElements) {
            callChildNodesToBuildDelta(node, childNodesWithDeltaFlags, parentDelta, event, requestMonitor);
        } else {
            // The given child nodes have deltas potentially for all elements
            // from this node.  Retrieve all elements and call the child nodes with
            // each element as the parent of their delta.
            getVMProvider().updateNode(
                node, 
                new VMChildrenUpdate(
                    parentDelta, getVMProvider().getPresentationContext(), -1, -1,
                    new DataRequestMonitor<List<Object>>(getVMProvider().getExecutor(), requestMonitor) {
                        @Override
                        protected void handleSuccess() {
                            if (fDisposed) return;
                            
                            // Check for an empty list of elements.  If it's empty then we 
                            // don't have to call the children nodes, so return here.
                            if (getData().size() == 0) {
                                requestMonitor.done();
                                return;
                            }
                            
                            final MultiRequestMonitor<RequestMonitor> elementsDeltasMultiRequestMon = 
                                new MultiRequestMonitor<RequestMonitor>(getVMProvider().getExecutor(), requestMonitor);
    
                            // For each element from this node, create a new delta, 
                            // and then call all the child nodes to build their delta. 
                            for (int i = 0; i < getData().size(); i++) {
                                int elementIndex = nodeOffset >= 0 ? nodeOffset + i : -1;
                                VMDelta delta= parentDelta.getChildDelta(getData().get(i));
                                if (delta == null) {
                                    delta= parentDelta.addNode(getData().get(i), elementIndex, IModelDelta.NO_CHANGE);
                                }
                                callChildNodesToBuildDelta(
                                    node, childNodesWithDeltaFlags, delta, event, 
                                    elementsDeltasMultiRequestMon.add(new RequestMonitor(getVMProvider().getExecutor(), null) { 
                                        @Override
                                        protected void handleCompleted() {
                                            elementsDeltasMultiRequestMon.requestMonitorDone(this);
                                        }
                                    }));
                            }
                        }
                    })
                );
        }
    }
    
    /**
     * Calls the specified child nodes to build the delta for the given event.
     * @param childNodes Map of nodes to be invoked, and the corresponding delta 
     * flags that they will generate.  This map is generated with a call to 
     * {@link #getChildNodesWithDeltaFlags(Object)}.  
     * @param delta The delta object to build on.  This delta should have been 
     * generated by this node, unless the full delta path is not being calculated
     * due to an optimization.
     * @param event The event object that the delta is being built for.
     * @param requestMonitor The result token to invoke when the delta is completed.
     */
    protected void callChildNodesToBuildDelta(final IVMNode node, final Map<IVMNode,Integer> childNodes, final VMDelta delta, final Object event, final RequestMonitor requestMonitor) {
        assert childNodes.size() != 0;

        // Check if any of the child nodes are will generate IModelDelta.SELECT  or 
        // IModelDelta.EXPAND flags.  If so, we must calculate the index for this 
        // VMC.
        boolean calculateOffsets = false;
        for (int childDelta : childNodes.values()) {
            if ( (childDelta & (IModelDelta.SELECT | IModelDelta.EXPAND | IModelDelta.INSERTED | IModelDelta.REMOVED)) != 0 ) {
                calculateOffsets = true;
                break;
            }
        }

        getChildNodesElementOffsets(
            node, delta, calculateOffsets, 
            new DataRequestMonitor<Map<IVMNode, Integer>>(getVMProvider().getExecutor(), requestMonitor) {
                @Override
                protected void handleSuccess() {
                    final CountingRequestMonitor multiRm = new CountingRequestMonitor(getVMProvider().getExecutor(), requestMonitor);
                    int multiRmCount = 0;
                    
                    // Set the total count of number of children in the parent delta.
                    delta.setChildCount(getData().get(null));
                    
                    for (final IVMNode childNode : childNodes.keySet()) {
                        // Avoid descending into recursive node hierarchy's when calculating the delta.
                        if (node.equals(childNode)) continue;
                        
                        final int nodeOffset = getData().get(childNode);
                        childNode.buildDelta(
                            event, delta, nodeOffset, 
                            new RequestMonitor(getVMProvider().getExecutor(), multiRm) {
                                @Override
                                protected void handleSuccess() {
                                    buildChildDeltas(
                                        childNode, event, delta, nodeOffset, new RequestMonitor(getVMProvider().getExecutor(), multiRm));
                                }
                            });
                        multiRmCount++;
                    }
                    multiRm.setDoneCount(multiRmCount);
                }
            });
    }

    /**
     * Calculates the indexes at which the elements of each of the child 
     * nodes begin.  These indexes are necessary to correctly 
     * calculate the deltas for elements in the child nodes.
     * @param delta The delta object to build on.  This delta should have been 
     * generated by this node, unless the full delta path is not being calculated
     * due to an optimization.
     * @param doCalculdateOffsets If true, the method calls each node to get its
     * element count. If false, it causes this method to fill the return data
     * structure with dummy values.  The dummy values indicate that the indexes
     * are not known and are acceptable in the delta if the delta flags being 
     * generated do not require full index information. 
     * @param rm Return token containing the results.  The result data is a 
     * mapping between the child nodes and the indexes at which the child nodes' 
     * elements begin.  There is a special value in the map with a <code>null</code>
     * key, which contains the full element count for all the nodes. 
     */
    private void getChildNodesElementOffsets(IVMNode node, IModelDelta delta, boolean calculdateOffsets, final DataRequestMonitor<Map<IVMNode, Integer>> rm) {
        final IVMNode[] childNodes = getVMProvider().getChildVMNodes(node); 
        assert childNodes.length != 0;
        
        if (calculdateOffsets) {
            final Integer[] counts = new Integer[childNodes.length]; 
            final MultiRequestMonitor<RequestMonitor> childrenCountMultiRequestMon = 
                new MultiRequestMonitor<RequestMonitor>(getVMProvider().getExecutor(), rm) { 
                    @Override
                    protected void handleSuccess() {
                        Map<IVMNode, Integer> data = new HashMap<IVMNode, Integer>();
                        int offset = 0;
                        for (int i = 0; i < childNodes.length; i++) {
                            data.put(childNodes[i], offset);
                            offset += counts[i];
                        }
                        // As the final value, put the total count in the return map, with null key.
                        data.put(null, offset);
                        rm.setData(data);
                        rm.done();
                    }
                };
            
            for (int i = 0; i < childNodes.length; i++) {
                final int nodeIndex = i;
                getVMProvider().updateNode(
                    childNodes[i], 
                    new VMChildrenCountUpdate(
                        delta, getVMProvider().getPresentationContext(),
                        childrenCountMultiRequestMon.add(
                            new DataRequestMonitor<Integer>(getVMProvider().getExecutor(), rm) {
                                @Override
                                protected void handleCompleted() {
                                    counts[nodeIndex] = getData();
                                    childrenCountMultiRequestMon.requestMonitorDone(this);
                                }
                            }) 
                        )
                    );
            }
        } else {
            Map<IVMNode, Integer> data = new HashMap<IVMNode, Integer>();
            for (int i = 0; i < childNodes.length; i++) {
                data.put(childNodes[i], -1);
            }
            data.put(null, -1);
            rm.setData(data);
            rm.done();
        }            
    }
        
    /**
     * Convenience method that returns the child nodes which return
     * <code>true</code> to the <code>hasDeltaFlags()</code> test for the given
     * event.   
     */
    protected Map<IVMNode, Integer> getChildNodesWithDeltaFlags(IVMNode node, ModelDelta parentDelta, Object e) {
        Map<IVMNode, Integer> nodes = new HashMap<IVMNode, Integer>(); 
        for (final IVMNode childNode : getVMProvider().getChildVMNodes(node)) {
            if (!childNode.equals(node)) {
                int delta = getDeltaFlags(childNode, parentDelta, e);
                if (delta != IModelDelta.NO_CHANGE) {
                    nodes.put(childNode, delta);
                }
            }
        }
        return nodes;
    }

    
}
