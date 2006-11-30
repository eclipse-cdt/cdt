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

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * 
 */
@SuppressWarnings("restriction")
abstract public class AbstractVMRootLayoutNode extends AbstractVMLayoutNode implements IVMRootLayoutNode {

    public AbstractVMRootLayoutNode(AbstractVMProvider provider) {
        super(provider);
    }

    /**
     * This implementation only fulfils the requirements of the super-interface.
     * There is no use case for a root node implementing this method, but its 
     * easier to just impelemnt it for sake of uniformity of model.
     */
    public void updateElements(IChildrenUpdate update) {
        // Ignore startIdx, endIdx, since there's only one element to be had.
        update.setChild(getRootObject(), 0);
        update.done();
    }
    
    /**
     * This implementation only fulfils the requirements of the super-interface.
     * There is no use case for a root node implementing this method, but its 
     * easier to just impelemnt it for sake of uniformity of model.
     */
    public void updateElementCount(IChildrenCountUpdate update) {
        update.setChildCount(1);
        update.done();
    }

    /**
     * This implementation only fulfils the requirements of the super-interface.
     * There is no use case for a root node implementing this method, but its 
     * easier to just impelemnt it for sake of uniformity of model.
     */
    public void updateHasElements(IHasChildrenUpdate[] updates) {
        for (IHasChildrenUpdate update : updates) {
            update.setHasChilren(true); 
            update.done();
        }
    }

    /**
     * This implementation only fulfils the requirements of the super-interface.
     * There is no use case for a root node implementing this method, but its 
     * easier to just impelemnt it for sake of uniformity of model.
     */
    public void updateLabel(IVMContext vmc, ILabelUpdate update) {
        update.done();
    }
    
    /**
     * Default implementation creates a delta assuming that the root layout node
     * is the input object into the view.  
     */
    public void createDelta(Object event, final GetDataDone<IModelDelta> done) {
        final Map<IVMLayoutNode,Integer> childNodeDeltas = getChildNodesWithDeltas(event);
        assert childNodeDeltas.size() != 0 : "Caller should make sure that there are deltas for given event."; //$NON-NLS-1$

        // Always create the rootDelta, no matter what delta flags the child nodes have.
        final VMDelta rootDelta = new VMDelta(getRootObject(), IModelDelta.NO_CHANGE);

        callChildNodesToBuildDelta(
            childNodeDeltas, rootDelta, event, 
            new Done() { 
                public void run() {
                    if (isDisposed()) return;
                    if (propagateError(getExecutor(), done, "Failed to create delta.")); //$NON-NLS-1$
                    done.setData(rootDelta);
                    getExecutor().execute(done);
                }
            });
    }
}
