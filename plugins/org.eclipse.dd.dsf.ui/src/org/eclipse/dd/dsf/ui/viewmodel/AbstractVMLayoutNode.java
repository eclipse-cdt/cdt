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
import java.util.List;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.DoneCollector;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditor;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditorFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;

/**
 * 
 */
@SuppressWarnings("restriction")
abstract public class AbstractVMLayoutNode implements IVMLayoutNode {

    private final DsfExecutor fExecutor;
    
    /** Child schema nodes of this node. */
    private IVMLayoutNode[] fChildNodes = new IVMLayoutNode[0];

    
    public AbstractVMLayoutNode(DsfExecutor executor) {
        fExecutor = executor;
    }

    /**
     * Accessor method for sub-classes.
     */
    protected DsfExecutor getExecutor() {
        return fExecutor;
    }
    
    public void setChildNodes(IVMLayoutNode[] childNodes) {
        fChildNodes = childNodes;
    }
    
    public IVMLayoutNode[] getChildLayoutNodes() {
        return fChildNodes;
    }

    public void dispose() {
        for (IVMLayoutNode childNode : getChildLayoutNodes()) {
            childNode.dispose();
        }
    }
    
    /** 
     * If any of the children nodes have delta flags, that means that this 
     * node has to generate a delta as well. 
     */
    public boolean hasDeltaFlags(Object e) {
        for (IVMLayoutNode childNode : getChildLayoutNodes()) {
            if (childNode.hasDeltaFlags(e)) return true;
        }
        return false;
    }

    /**
     * Handles calling child schema nodes to build the model delta.  If child 
     * schema nodes have deltas, this schema node has to provide the 
     * IModelDelta objects that the child shema node can build on.
     */
    public void buildDelta(final Object e, final VMDelta parent, final Done done) {
        /*
         * Find the child nodes that have deltas for the given event. If no 
         * child layout nodes have deltas, just invoke the done.
         */
        final IVMLayoutNode[] childNodes = getChildNodesWithDeltas(e);
        if (childNodes.length == 0) {
            getExecutor().execute(done);
            return;
        }            


        /*
         * The given child layout nodes have deltas potentially for all elements
         * from this node.  Retrieve all elements and call the child nodes with
         * each element as the parent of their delta.
         */
        getElements(
            parent.getVMC(), 
            new GetDataDone<IVMContext[]>() { 
                public void run() {
                    if (propagateError(getExecutor(), done, "Failed to retrieve elements in layout node " + AbstractVMLayoutNode.this)) return; //$NON-NLS-1$
                    
                    /*
                     * Check for an empty list of elements.  If it's empty then we 
                     * don't have to call the children nodes, so return here.
                     */
                    if (getData().length == 0) {
                        getExecutor().execute(done);
                    }
                    
                    /* 
                     * The execution for this node is not done until all the child nodes
                     * are done.  Use the tracker to wait for all children to complete. 
                     */
                    final DoneCollector doneCollector = new DoneCollector(getExecutor()) { public void run() {
                        getExecutor().execute(done);                                
                    }};
                    for (IVMContext element : getData()) {
                        for (final IVMLayoutNode childNode : childNodes) {
                            childNode.buildDelta(
                                e, 
                                parent.addNode(element, IModelDelta.NO_CHANGE),
                                doneCollector.addNoActionDone());
                        }
                    }
                }
            });
    }
    
    /**
     * Default implementation of the IColumnEditorFactoryAdapter delegate.  It 
     * returns null, which means that no cell editor is configured. 
     * @see IColumnEditorFactoryAdapter#createColumnEditor(IPresentationContext, Object)
     */
    public IColumnEditor createColumnEditor(IVMContext vmc) {
        return null;
    }

    /**
     * Default implementation of the IColumnEditorFactoryAdapter delegate.  It 
     * returns null, which means that no cell editor is configured. 
     * @see IColumnEditorFactoryAdapter#getColumnEditorId(IPresentationContext, Object)
     */
    public String getColumnEditorId(IVMContext vmc) {
        return null;
    }
    
    /**
     * Convenience method that returns the child layout nodes which return
     * <code>true</code> to the <code>hasDeltaFlags()</code> test for the given
     * event.   
     */
    protected IVMLayoutNode[] getChildNodesWithDeltas(Object e) {
        List<IVMLayoutNode> nodes = new ArrayList<IVMLayoutNode>(); 
        for (final IVMLayoutNode childNode : getChildLayoutNodes()) {
            if (childNode.hasDeltaFlags(e)) {
                nodes.add(childNode);
            }
        }
        return nodes.toArray(new IVMLayoutNode[nodes.size()]);
    }

    /**
     * Convenience method that returns a token value in case when the services
     * that the layout node depends on, are not available.
     */
    protected void handleFailedHasElements(GetDataDone<Boolean> done) {
        done.setData(false);
        getExecutor().execute(done);
    }

    /**
     * Convenience method that returns a token value in case when the services
     * that the layout node depends on, are not available.
     */
    protected void handleFailedGetElements(GetDataDone<IVMContext[]> done) {
        done.setData(new IVMContext[0]);
        getExecutor().execute(done);
    }

    /**
     * Convenience method that returns a token value in case when the services
     * that the layout node depends on, are not available.
     */
    protected void handleFailedRetrieveLabel(ILabelRequestMonitor result) {
        result.setLabels(new String[] { "..."} ); //$NON-NLS-1$
        result.done();
    }
    
}
