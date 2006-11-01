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

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.DoneCollector;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.DsfUIPlugin;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode.IRootVMC;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;


/**
 * View model layout node based on a single Data Model Context type.  
 * The assumption in this implementation is that elements of this node have
 * a single IDMContext associated with them, and all of these contexts 
 * are of the same class type.   
 */
@SuppressWarnings("restriction")
abstract public class DMContextVMLayoutNode extends AbstractVMLayoutNode {

    /**
     * IVMContext implementation used for this schema node.
     */
    @Immutable
    public class DMContextVMContext implements IVMContext {
        private final IVMContext fParent;
        private final IDMContext fDmc;
        
        public DMContextVMContext(IVMContext parent, IDMContext dmc) {
            fParent = parent;
            fDmc = dmc;
        }
        
        public IDMContext getDMC() { return fDmc; }
        public IVMContext getParent() { return fParent; }
        public IVMLayoutNode getLayoutNode() { return DMContextVMLayoutNode.this; }
        
        public Object getAdapter(Class adapter) {
            return fDmc.getAdapter(adapter);
        }
        
        public boolean equals(Object other) {
            if (!(other instanceof DMContextVMContext)) return false;
            DMContextVMContext otherVmc = (DMContextVMContext)other;
            return DMContextVMLayoutNode.this.equals(otherVmc.getLayoutNode()) &&
                   fParent.equals(otherVmc.fParent) && 
                   fDmc.equals(otherVmc.fDmc);
        }
        
        public int hashCode() {
            return DMContextVMLayoutNode.this.hashCode() + fParent.hashCode() + fDmc.hashCode(); 
        }
     
        public String toString() {
            return fParent.toString() + "->" + fDmc.toString();
        }
    }

    /** Service tracker to be used by sub-classes */
    private DsfServicesTracker fServices;
    
    
    /** Class type that the elements of this schema node are based on. */
    private Class<? extends IDMContext> fDMCClassType;

    /** 
     * Constructor initializes instance data, except for the child nodes.  
     * Child nodes must be initialized by calling setChildNodes()
     * @param session
     * @param dmcClassType
     * @see #setChildNodes(IVMLayoutNode[])
     */
    public DMContextVMLayoutNode(DsfSession session, Class<? extends IDMContext> dmcClassType) {
        super(session.getExecutor());
        fServices = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());        
        fDMCClassType = dmcClassType;
    }
    

    
    /**
     * Returns the services tracker for sub-class use.
     */
    protected DsfServicesTracker getServicesTracker() {
        return fServices;
    }

    @Override
    public boolean hasDeltaFlags(Object e) {
        if (e instanceof IDMEvent) {
            return hasDeltaFlagsForDMEvent((IDMEvent)e);
        } else {
            return super.hasDeltaFlags(e);
        }
    }
    
    /**
     * DMC-specific version of {@link IVMLayoutNode#hasDeltaFlags(Object)}.
     * By default, it falls back on the super-class implementation.
     */
    protected boolean hasDeltaFlagsForDMEvent(IDMEvent e) {
        return super.hasDeltaFlags(e);
    }
    
    @Override
    public void buildDelta(Object e, VMDelta parent, Done done) {
        if (e instanceof IDMEvent) {
            buildDeltaForDMEvent((IDMEvent)e, parent, done);
        } else {
            super.buildDelta(e, parent, done);
        }
    }
    
    /**
     * Adds an optimization (over the AbstractViewModelLayoutNode) which 
     * narrows down the list of children based on the DMC within the event. 
     */
    public void buildDeltaForDMEvent(final IDMEvent e, final VMDelta parent, final Done done) {
        /* 
         * Take the IDMContext (DMC) that the event is based on, and 
         * search its ancestors.  Look for the DMC class typs that this schema 
         * node is based on.  If its found, then only one IModelDelta needs to 
         * be generated for this schema node. Otherwise, resort to the default 
         * behavior and generate a IModelDelta for every element in this schema 
         * node. 
         */
        IDMContext dmc = DMContexts.getAncestorOfType(e.getDMContext(), fDMCClassType);
        if (dmc != null) {
            IVMLayoutNode[] childNodes = getChildNodesWithDeltas(e);
            if (childNodes.length == 0) {
                // There are no child nodes with deltas, just return to parent.
                getExecutor().execute(done);
                return;
            }            

            /* 
             * This execution for this node is not done until all the child nodes
             * are done.  Use the tracker to wait for all children to complete. 
             */
            DoneCollector childDoneTracker = new DoneCollector(getExecutor()) { 
                public void run() {
                    getExecutor().execute(done);
                }
            };                
            for (final IVMLayoutNode childNode : getChildLayoutNodes()) {
                /*
                 * Create a delta corresponding to the DMC from the event and pass 
                 * it as parent VMC to the child node.  The child node will build 
                 * its delta on top of this delta. 
                 */
                childNode.buildDelta(
                    e, 
                    parent.addNode(new DMContextVMContext(parent.getVMC(), dmc), IModelDelta.NO_CHANGE),
                    childDoneTracker.addNoActionDone());
            }
        } else {
            super.buildDelta(e, parent, done);
        }
    }
    
    /**
     * Utility method that takes an array of DMC object and creates a 
     * corresponding array of IVMContext elements base on that.   
     * @param parent The parent for generated IVMContext elements. 
     * @param dmcs Array of DMC objects to build return array on.
     * @return Array of IVMContext objects.
     */
    protected IVMContext[] dmcs2vmcs(IVMContext parent, IDMContext[] dmcs) {
        IVMContext[] vmContexts = new IVMContext[dmcs.length];
        for (int i = 0; i < dmcs.length; i++) {
            vmContexts[i] = new DMContextVMContext(parent, dmcs[i]);
        }
        return vmContexts;
    }

    /**
     * Searches for a DMC of given type in the tree patch contained in given 
     * VMC.  VMCs keep a reference to the parent node that contain them in the 
     * tree.  This method recursively looks compares the parent until root is 
     * reached, or the DMC is found.  If the root is reached, and the root's 
     * input is also a VMC (which comes from another view), then the hierarchy
     * of the input object will be searched as well.
     * @param <V> Type of the DMC that will be returned.
     * @param vmc VMC element to search.
     * @param dmcType Class object for matching the type.
     * @return DMC, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static <V extends IDMContext> V findDmcInVmc(IVMContext vmc, Class<V> dmcType) {
        if (vmc instanceof IRootVMC && ((IRootVMC)vmc).getInputObject() instanceof IVMContext) {
            vmc = (IVMContext)((IRootVMC)vmc).getInputObject();
        }
        
        if (vmc instanceof DMContextVMLayoutNode.DMContextVMContext &&
            dmcType.isAssignableFrom( ((DMContextVMLayoutNode.DMContextVMContext)vmc).getDMC().getClass() ))
        {
            return (V)((DMContextVMLayoutNode.DMContextVMContext)vmc).getDMC();
        } else if (vmc.getParent() != null) {
            return findDmcInVmc(vmc.getParent(), dmcType);
        }
        return null;
    }
    
    public void sessionDispose() {
        fServices.dispose();
        super.sessionDispose();
    }
}
