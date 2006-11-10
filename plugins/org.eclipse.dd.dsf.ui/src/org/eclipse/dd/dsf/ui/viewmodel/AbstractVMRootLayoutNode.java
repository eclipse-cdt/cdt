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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dd.dsf.concurrent.DoneCollector;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;

/**
 * 
 */
@SuppressWarnings("restriction")
abstract public class AbstractVMRootLayoutNode extends AbstractVMLayoutNode implements IVMRootLayoutNode {

    protected static class RootVMC<V> implements IRootVMC {
        private final V fInputObject;
        private final IVMRootLayoutNode fVMRootLayoutNode;
        
        public RootVMC(IVMRootLayoutNode vmRootLayoutNode, V inputObject) {
            fVMRootLayoutNode = vmRootLayoutNode;
            fInputObject = inputObject;
        }
        
        /** Returns the ILaunch object belonging to this launch VMC. */
        public V getInputObject() { return fInputObject; }
        
        public IVMContext getParent() { return null; }
        
        public IVMLayoutNode getLayoutNode() { return fVMRootLayoutNode; }
        
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            if (fInputObject instanceof IAdaptable) {
                return ((IAdaptable)fInputObject).getAdapter(adapter);
            }
            return null; 
        }
        
        public boolean equals(Object other) {
            return getClass().equals( other.getClass() ) &&
                   fInputObject.equals( ((RootVMC)other).getInputObject() );
        }
        
        public int hashCode() {
            return fInputObject.hashCode();
        }
        
        public String toString() { return "Root VMC for " + fInputObject.toString(); } //$NON-NLS-1$
    }
        
    public AbstractVMRootLayoutNode(DsfExecutor executor) {
        super(executor);
    }

    /**
     * This implementation only fulfils the requirements of the super-interface.
     * There is no use case for a root node implementing this method, but its 
     * easier to just impelemnt it for sake of uniformity of model.
     */
    public void getElements(IVMContext parentVmc, GetDataDone<IVMContext[]> done) {
        done.setData(new IVMContext[] { getRootVMC() });
        getExecutor().execute(done);
    }

    /**
     * This implementation only fulfils the requirements of the super-interface.
     * There is no use case for a root node implementing this method, but its 
     * easier to just impelemnt it for sake of uniformity of model.
     */
    public void hasElements(IVMContext parentVmc, GetDataDone<Boolean> done) {
        done.setData(true);
        getExecutor().execute(done);
    }

    /**
     * This implementation only fulfils the requirements of the super-interface.
     * There is no use case for a root node implementing this method, but its 
     * easier to just impelemnt it for sake of uniformity of model.
     */
    public void retrieveLabel(IVMContext vmc, ILabelRequestMonitor result, String[] columns) {
        result.done();
    }
    
    /**
     * Default implementation creates a delta assuming that the root layout node
     * is the input object into the view.  
     */
    public void createDelta(Object event, final GetDataDone<IModelDelta> done) {
        final VMDelta rootDelta = new VMDelta(getRootVMC().getInputObject(), getRootVMC());

        final IVMLayoutNode[] childNodes = getChildNodesWithDeltas(event);
        if (childNodes.length == 0) {
            done.setData(rootDelta);
            getExecutor().execute(done);
            return;
        }            

        /* 
         * The execution for this node is not done until all the child nodes
         * are done.  Use the tracker to wait for all children to complete. 
         */
        final DoneCollector doneCollector = new DoneCollector(getExecutor()) { 
            public void run() {
                if (propagateError(getExecutor(), done, "Failed to generate child deltas.")) return; //$NON-NLS-1$
                done.setData(rootDelta);            
                getExecutor().execute(done);                                
            }
        };
        for (final IVMLayoutNode childNode : childNodes) {
            childNode.buildDelta(event, rootDelta, doneCollector.addNoActionDone());
        }
    }
}
