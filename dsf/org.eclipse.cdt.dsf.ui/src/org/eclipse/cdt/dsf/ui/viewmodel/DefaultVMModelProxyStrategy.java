/*******************************************************************************
 *  Copyright (c) 2005, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
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
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
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
 * 
 * @since 1.0
 */
@ConfinedToDsfExecutor("#getProvider()#getExecutor()")
@SuppressWarnings("restriction")
public class DefaultVMModelProxyStrategy implements IVMModelProxy {

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
        return getEventDeltaFlags(event) != IModelDelta.NO_CHANGE; 
    }

    public int getEventDeltaFlags(Object event) {
        IRootVMNode rootNode = getVMProvider().getRootVMNode();
        if (rootNode != null && 
            rootNode.isDeltaEvent(getRootElement(), event)) 
        {
            return getDeltaFlags(rootNode, null, event);
        }
        return IModelDelta.NO_CHANGE; 
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
	 * Recursively calls the VM nodes in the hierarchy of the given node to
	 * determine how elements of those types may be (or are) affected by
	 * [event], the answer being a collection of IModelDelta flags.
	 * 
	 * A response of IModeDelta.CONTENT has a special meaning. If we return that
	 * flag for an IVMNode, it means the <i>collection</i> of elements of that
	 * type are affected. It is not a statement on the elements themselves.
	 * 
	 * Optimization 1: If the first-level child node does not have
	 * <code>IModelDelta.CONTENT</code> but one of its descendants does, then
	 * for optimization reasons we return <code>IModelDelta.STATE</code>
	 * instead. See {@link DefaultVMModelProxyStrategy#buildChildDeltasForAllContexts(IVMNode, Object, VMDelta, int, RequestMonitor)}
	 * 
	 * Optimization 2: If the parent delta contains
	 * <code>IModelDelta.CONTENT</code>, we do not need to specify it for its
	 * children. This can shorten delta processing considerably.
	 * 
	 * @param node
	 *            the IVMNode whose delta flags (and those of its descendants)
	 *            are being queried
	 * @param parentDelta
	 *            the base portion of the delta the caller is trying to
	 *            construct; this delta node is specifically the deepest node in
	 *            that chain (i.e., has no children, but may have ancestors)
	 * @param event
	 *            the event the caller is trying to produce a delta for
	 * @return the collective set of IModelDelta flags that reflect how [node]
	 *         and its descendants may be (or are) affected by [event]
	 */
    protected int getDeltaFlags(IVMNode node, ModelDelta parentDelta, Object event) {
        int flags = node.getDeltaFlags(event);
        for (IVMNode childNode : getVMProvider().getChildVMNodes(node)) {
            if (!childNode.equals(node)) {
                int childNodeDeltaFlags = getDeltaFlags(childNode, parentDelta, event);
                
                // optimization 1; see above
                if ((childNodeDeltaFlags & IModelDelta.CONTENT) != 0) {
                    childNodeDeltaFlags &= ~IModelDelta.CONTENT;
                    childNodeDeltaFlags |= IModelDelta.STATE;
                }
                
                flags |= childNodeDeltaFlags;
            }
        }
        
        // optimization 2; see above
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
					// The resulting delta will have parents if our
					// VMProvider is registered to populate only a sub-tree
					// of the viewer. Get the root node of the chain--i.e.,
					// the delta for the root element of the entire viewer.
                    final IModelDelta viewRootDelta = getRootDelta(getData());
                   
                    // Find the child nodes that (may) have deltas for the given event. 
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
                                // Get rid of redundant CONTENT and STATE flags in delta and prune 
                                // nodes without flags
                                rm.setData(pruneDelta((VMDelta)viewRootDelta));
                                rm.done();
                            }
                        });
                }
            });
    }

    protected VMDelta pruneDelta(VMDelta delta) {
        delta.accept(new IModelDeltaVisitor() {
            public boolean visit(IModelDelta deltaNode, int depth) {
                if ((deltaNode.getFlags() & (IModelDelta.CONTENT | IModelDelta.STATE)) != 0) {
                    VMDelta parent = (VMDelta)deltaNode.getParentDelta();
                    while (parent != null) {
                        if ((parent.getFlags() & IModelDelta.CONTENT) != 0) {
                            ((VMDelta)deltaNode).setFlags(deltaNode.getFlags() & ~(IModelDelta.CONTENT | IModelDelta.STATE));
                            break;
                        }
                        parent = parent.getParentDelta();
                    }
                }
                return true;
            }
        });
        return delta;
    }
    
    /** 
     * Base implementation that handles calling child nodes to build 
     * the model delta.  This method delegates to two other methods:
     * {@link #buildChildDeltasForEventContext(IVMContext[], IVMNode, Object, VMDelta, int, RequestMonitor)} 
     * and {@link #buildChildDeltasForAllContexts(IVMNode, Object, VMDelta, int, RequestMonitor)}, 
     * depending on the result of calling{@link IVMNode#getContextsForEvent(VMDelta, Object, DataRequestMonitor)}.
     * @see IVMNode#buildDelta(Object, ModelDelta, int, RequestMonitor)
     */
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
                    } 
                    else if (getStatus().getCode() == IDsfStatusConstants.NOT_SUPPORTED) {
                    	// The DMC for this node was not found in the event.  Call the 
                    	// super-class to resort to the default behavior which will add a 
                    	// delta for every element in this node.
                    	buildChildDeltasForAllContexts(node, event, parentDelta, nodeOffset, rm);
                    } 
                    else {
                        super.handleCompleted();
                    }
                }
            });
    }
    
    /** 
     * Base implementation that handles calling child nodes to build 
     * the model delta.  This method is called with the context obtained  
     * by calling{@link IVMNode#getContextsForEvent(VMDelta, Object, DataRequestMonitor)}.
     * @see IVMNode#buildDelta(Object, ModelDelta, int, RequestMonitor)
     */
    protected void buildChildDeltasForEventContext(final IVMContext[] vmcs, final IVMNode node, final Object event, 
        final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) 
    {
        final Map<IVMNode,Integer> childNodesWithDeltaFlags = getChildNodesWithDeltaFlags(node, parentDelta, event);
        if (childNodesWithDeltaFlags.size() == 0) {
            // There are no child nodes with deltas, just return to parent.
            rm.done();
            return;
        }            
        
        // Check if any of the child nodes are will generate IModelDelta.SELECT  or 
        // IModelDelta.EXPAND flags.  If so, we must calculate the index for this 
        // VMC.
        boolean calculateIndex = false;
        if (nodeOffset >= 0) {
            for (int childDelta : childNodesWithDeltaFlags.values()) {
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
                    new DataRequestMonitor<List<Object>>(getVMProvider().getExecutor(), rm) {
                        @Override
                        protected void handleSuccess() {
                            // Check for an empty list of elements.  If it's empty then we 
                            // don't have to call the children nodes, so return here.
                            // No need to propagate error, there's no means or need to display it.
                            if (getData().isEmpty()) {
                                rm.done();
                                return;
                            }

                            CountingRequestMonitor countingRm = 
                                new CountingRequestMonitor(getVMProvider().getExecutor(), rm);
                            
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
                                
                                callChildNodesToBuildDelta(
                                    node, childNodesWithDeltaFlags, delta, event, countingRm);
                                count++;
                            }
                            countingRm.setDoneCount(count);
                        }
                    }));
        } else {
            CountingRequestMonitor countingRm = 
                new CountingRequestMonitor(getVMProvider().getExecutor(), rm);
            int count = 0;
            for (IVMContext vmc : vmcs) {
                // Optimization: Try to find a delta with a matching element, if found use it.  
                // Otherwise create a new delta for the event element.    
                VMDelta delta = parentDelta.getChildDelta(vmc);
                if (delta == null) {
                    delta = parentDelta.addNode(vmc, IModelDelta.NO_CHANGE);
                }
                callChildNodesToBuildDelta(node, childNodesWithDeltaFlags, delta, event, rm);
                count++;
            }
            countingRm.setDoneCount(count);
        }            
    }

    /** 
     * Base implementation that handles calling child nodes to build 
     * the model delta.  The child nodes are called with all the elements 
     * in this node, which could be very inefficient.  This method is only 
     * called if {@link IVMNode#getContextsForEvent(VMDelta, Object, DataRequestMonitor)}
     * is not supported by the node for the given element.
     * @see IVMNode#buildDelta(Object, ModelDelta, int, RequestMonitor)
     */
    protected void buildChildDeltasForAllContexts(final IVMNode node, final Object event, final VMDelta parentDelta,
        final int nodeOffset, final RequestMonitor rm) 
    {
        final Map<IVMNode,Integer> childNodesWithDeltaFlags = getChildNodesWithDeltaFlags(node, parentDelta, event);
        if (childNodesWithDeltaFlags.size() == 0) {
            // There are no child nodes with deltas, just return to parent.
            rm.done();
            return;
        }            

		// Check if the child delta only has an IModelDelta.STATE flag. If
		// that's the case, we can skip creating a delta for this node, because
		// the Debug Platform's handling of that flag does not require ancestor
		// deltas (doesn't need to know the path to the element)
        //
		// We can skip the delta for this node even if a deeper IVMNode child
		// calls for an IModelDelta.CONTENT flag, since what we do in that case
		// is have the CONTENT flag applied to the first ancestor delta that has
		// something other than IModelDelta.STATE.
        //
        // The main benefit of this optimization is that the viewer is left 
        // to retrieve the elements that need to be refreshed, rather 
        // than having this proxy do it.  Since the viewer is lazy loading
        // it may not need to retrieve as many elements in the hierarchy.
        //
        // For example: suppose the model looks like:
        // A-
        // |-1
        //   |-I
        //   | |-a
        //   | |-b
        //   |-II
        //   | |-c
        //   |-III
        //     |-d
        //
		// And if VM Node responsible for element a, b, c, d needs a CONTENT
		// update, then the delta may look like this:
        //
        // Element: A
        //        Flags: CONTENT
        //        Index: 0 Child Count:-1
        //
        // Instead of:
        //
        //    Element: A
        //        Flags: NO_CHANGE
        //        Index: 0 Child Count: 1
        //        Element: 1
        //            Flags: NO_CHANGE
        //            Index: 0 Child Count: 3
        //            Element: I
        //                Flags: CONTENT
        //                Index: 0 Child Count: 2
        //            Element: II
        //                Flags: CONTENT
        //                Index: 1 Child Count: 1
        //            Element: III
        //                Flags: CONTENT
        //                Index: 2 Child Count: 1
        boolean mustGetElements = false;
        boolean _updateFlagsOnly = true;
        for (int childDelta : childNodesWithDeltaFlags.values()) {
            if ((childDelta & ~IModelDelta.STATE) != 0) {
                mustGetElements = true;
            }
            if ((childDelta & ~(IModelDelta.STATE | IModelDelta.CONTENT)) != 0) {
                _updateFlagsOnly = false;
            }
        }
        final boolean updateFlagsOnly = _updateFlagsOnly; 

        if (!mustGetElements) {
            callChildNodesToBuildDelta(
                node, childNodesWithDeltaFlags, parentDelta, event, 
                new RequestMonitor(getVMProvider().getExecutor(), rm) {
                    @Override
                    protected void handleError() {
                        super.handleError();
                    }
                });
        } else {
            // The given child nodes have deltas potentially for all elements
            // from this node.  Retrieve all elements and call the child nodes with
            // each element as the parent of their delta.
            getVMProvider().updateNode(
                node, 
                new VMChildrenUpdate(
                    parentDelta, getVMProvider().getPresentationContext(), -1, -1,
                    new DataRequestMonitor<List<Object>>(getVMProvider().getExecutor(), rm) {
                        @Override
                        protected void handleCompleted() {
                            if (fDisposed) return;
                            
                            final List<Object> childElements = getData();
                            
                            // Check for an empty list of elements.  If the list of elements is empty
                            // still call the child nodes using the parent delta only.  Do this only if
                            // an optimization was used to build the delta, so that the child node can  
                            // adds the optimized delta flags without the full delta (bug 280770).
                            if (childElements == null || childElements.size() == 0) {
                                if (updateFlagsOnly) {
                                    callChildNodesToBuildDelta(
                                        node, childNodesWithDeltaFlags, parentDelta, event, rm);
                                } else {
                                    rm.done();
                                    return;
                                }
                            } else {
                                final CountingRequestMonitor countingRM = new CountingRequestMonitor(getVMProvider().getExecutor(), rm);
                                int rmCount = 0;
        
                                // For each element from this node, create a new delta, 
                                // and then call all the child nodes to build their delta. 
                                for (int i = 0; i < childElements.size(); i++) {
                                    int elementIndex = nodeOffset >= 0 ? nodeOffset + i : -1;
                                    VMDelta delta= parentDelta.getChildDelta(childElements.get(i));
                                    if (delta == null) {
                                        delta= parentDelta.addNode(childElements.get(i), elementIndex, IModelDelta.NO_CHANGE);
                                    }
                                    callChildNodesToBuildDelta(node, childNodesWithDeltaFlags, delta, event, countingRM);
                                    rmCount++;
                                }
                                countingRM.setDoneCount(rmCount);
                            }
                        }
                    })
                );
        }
    }

	/**
	 * Calls the specified child nodes (of [node]) to build the delta for the
	 * given event.
	 * 
	 * @param childNodes
	 *            Map of nodes to be invoked, and the corresponding delta flags
	 *            that they may (or will) generate. This map is generated with a
	 *            call to {@link #getChildNodesWithDeltaFlags(Object)}.
	 * @param delta
	 *            The delta object to build on. This delta should have been
	 *            generated by this node, unless the full delta path is not
	 *            being calculated due to an optimization.
	 * @param event
	 *            The event object that the delta is being built for.
	 * @param rm
	 *            The result monitor to invoke when the delta is completed.
	 */
    protected void callChildNodesToBuildDelta(final IVMNode node, final Map<IVMNode,Integer> childNodes, final VMDelta delta, 
        final Object event, final RequestMonitor rm) 
    {
        assert childNodes.size() != 0;

		// Check if any of the child nodes might generate a delta that requires
		// us to calculate the index for this VMC.
        boolean calculateOffsets = false;
        for (int childDelta : childNodes.values()) {
            if ( (childDelta & (IModelDelta.SELECT | IModelDelta.EXPAND | IModelDelta.INSERTED | IModelDelta.REMOVED)) != 0 ) {
                calculateOffsets = true;
                break;
            }
        }

        getChildNodesElementOffsets(
            node, delta, calculateOffsets, 
            new DataRequestMonitor<Map<IVMNode, Integer>>(getVMProvider().getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    final CountingRequestMonitor multiRm = new CountingRequestMonitor(getVMProvider().getExecutor(), rm);
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
                                        childNode, event, delta, nodeOffset, 
                                        new RequestMonitor(getVMProvider().getExecutor(), multiRm) );
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
            final CountingRequestMonitor crm = new CountingRequestMonitor(getVMProvider().getExecutor(), rm) { 
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
            int countRM = 0;
            
            for (int i = 0; i < childNodes.length; i++) {
                final int nodeIndex = i;
                getVMProvider().updateNode(
                    childNodes[i], 
                    new VMChildrenCountUpdate(
                        delta, getVMProvider().getPresentationContext(),
                            new DataRequestMonitor<Integer>(getVMProvider().getExecutor(), crm) {
                                @Override
                                protected void handleCompleted() {
                                    counts[nodeIndex] = getData();
                                    crm.done();
                                }
                            } 
                        )
                    );
                countRM++;
            }
            crm.setDoneCount(countRM);
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
	 * Convenience method that returns what each of the child nodes returns from
	 * {@link #getDeltaFlags(IVMNode, ModelDelta, Object)}. Children that return
	 * IModelDelta.NO_CHANGE are omitted.
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
