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

import java.util.Map;

import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * Default implementation of a root layout node.  This class may be sub-classed
 * to implement model-specific event handling.
 */
@SuppressWarnings("restriction")
public class VMRootLayoutNode extends AbstractVMLayoutNode implements IVMRootLayoutNode {

    public VMRootLayoutNode(AbstractVMProvider provider) {
        super(provider);
    }

    /**
     * This implementation only fulfills the requirements of the super-interface.
     * There is no use case for a root node implementing this method, but its 
     * easier to just implement it for sake of uniformity of model.
     */
    public void updateElements(IChildrenUpdate update) {
        // Ignore startIdx, endIdx, since there's only one element to be had.
        update.setChild(getVMProvider().getRootElement(), 0);
        update.done();
    }
    
    /**
     * This implementation only fulfills the requirements of the super-interface.
     * There is no use case for a root node implementing this method, but its 
     * easier to just impelemnt it for sake of uniformity of model.
     */
    public void updateElementCount(IChildrenCountUpdate update) {
        update.setChildCount(1);
        update.done();
    }

    /**
     * This implementation only fulfills the requirements of the super-interface.
     * There is no use case for a root node implementing this method, but its 
     * easier to just implement it for sake of uniformity of model.
     */
    public void updateHasElements(IHasChildrenUpdate[] updates) {
        for (IHasChildrenUpdate update : updates) {
            update.setHasChilren(true); 
            update.done();
        }
    }

    /**
     * This implementation only fulfills the requirements of the super-interface.
     * There is no use case for a root node implementing this method, but its 
     * easier to just implement it for sake of uniformity of model.
     */
    public void updateLabel(@SuppressWarnings("unused")
    IVMContext vmc, ILabelUpdate update) {
        update.done();
    }
    
    /**
     * Default implementation creates a delta assuming that the root layout node
     * is the input object into the view.  
     */
    public void createDelta(Object event, final DataRequestMonitor<IModelDelta> rm) {
        final Map<IVMLayoutNode,Integer> childNodeDeltas = getChildNodesWithDeltaFlags(event);
        assert childNodeDeltas.size() != 0 : "Caller should make sure that there are deltas for given event."; //$NON-NLS-1$

        // Always create the rootDelta, no matter what delta flags the child nodes have.
        final VMDelta rootDelta = new VMDelta(getVMProvider().getRootElement(), IModelDelta.NO_CHANGE);

        callChildNodesToBuildDelta(
            childNodeDeltas, rootDelta, event, 
            new RequestMonitor(getExecutor(), rm) { 
                @Override
                protected void handleCompleted() {
                    if (!isDisposed()) super.handleCompleted();
                }

                @Override
                public void handleOK() {
                    rm.setData(rootDelta);
                    rm.done();
                }
            });
    }
    
    public Object getRootObject() {
        return getVMProvider().getRootElement();
    }
}
