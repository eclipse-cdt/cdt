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
package org.eclipse.dd.dsf.ui.model;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.DoneTracker;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.DMCs;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;


/**
 * View Model Schema Node based on a single Data Model Context (DMC) type.  
 * The assumption in this implementation is that elements of this node have
 * a single IDataModelContext associated with them, and all of these DMCs 
 * are of the same class type.   
 */
@SuppressWarnings("restriction")
abstract public class DataViewModelSchemaNode implements IViewModelSchemaNode {

    /**
     * IViewModelContext implementation used for this schema node.
     */
    public class DataVMC implements IViewModelContext {
        IViewModelContext fParent;
        IDataModelContext fDmc;
        
        public DataVMC(IViewModelContext parent, IDataModelContext dataModelContext) {
            fParent = parent;
            fDmc = dataModelContext;
        }
        
        public IDataModelContext getDataModelContext() { return fDmc; }
        public IViewModelContext getParent() { return fParent; }
        public IViewModelSchemaNode getSchemaNode() { return DataViewModelSchemaNode.this; }
        
        public Object getAdapter(Class adapter) {
            return fDmc.getAdapter(adapter);
        }
        
        public boolean equals(Object other) {
            if (!(other instanceof DataVMC)) return false;
            DataVMC otherVmc = (DataVMC)other;
            return DataViewModelSchemaNode.this.equals(otherVmc.getSchemaNode()) &&
                   fParent.equals(otherVmc.fParent) && 
                   fDmc.equals(otherVmc.fDmc);
        }
        
        public int hashCode() {
            return DataViewModelSchemaNode.this.hashCode() + fParent.hashCode() + fDmc.hashCode(); 
        }
     
        public String toString() {
            return fParent.toString() + "\n" + fDmc.toString();
        }
    }

    /** Executor to use when posting done tokens. */
    private DsfExecutor fExecutor;
    
    /** Service tracker to be used by sub-classes */
    private DsfServicesTracker fServices;
    
    /** Child schema nodes of this node. */
    private IViewModelSchemaNode[] fChildNodes = new IViewModelSchemaNode[0];
    
    /** Class type that the elements of this schema node are based on. */
    private Class<? extends IDataModelContext> fDMCClassType;

    /** 
     * Constructor initializes instance data, except for the child nodes.  
     * Child nodes must be initialized by calling setChildNodes()
     * @param session
     * @param dmcClassType
     * @see #setChildNodes(IViewModelSchemaNode[])
     */
    public DataViewModelSchemaNode(DsfSession session, Class<? extends IDataModelContext> dmcClassType) {
        fServices = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());        
        fExecutor = session.getExecutor();
        fDMCClassType = dmcClassType;
    }
    
    /**
     * Sets the child schema nodes of this node.  Needs to be configured 
     * before the schema node is used. 
     */
    public void setChildNodes(IViewModelSchemaNode[] childNodes) {
        fChildNodes = childNodes;
    }
    
    public IViewModelSchemaNode[] getChildNodes() {
        return fChildNodes;
    }

    /** 
     * If any of the children nodes have delta flags, that means that this 
     * node has to generate a delta as well. 
     */
    public boolean hasDeltaFlags(IDataModelEvent e) {
        for (IViewModelSchemaNode childNode : getChildNodes()) {
            if (childNode.hasDeltaFlags(e)) return true;
        }
        return false;
    }

    /**
     * Handles calling child schema nodes to build the model delta.  If child 
     * schema nodes have deltas, this schema node has to provide the 
     * IModelDelta objects that the child shema node can build on.
     */
    public void buildDelta(final IDataModelEvent e, final ViewModelDelta parent, final Done done) {
        DoneTracker childNodesDoneTracker = new DoneTracker() { public void run() {
            fExecutor.execute(done);
        }};

        // TODO: Note this is pretty inefficient: for one the below loop could 
        // potentially retrieve the elements for this node several times, but 
        // beyond that it may be possible to optimize this code based on what's
        // visible in the view.
        for (final IViewModelSchemaNode childNode : getChildNodes()) {
            if (childNode.hasDeltaFlags(e)) {
                // Take the IDataModelContext (DMC) that the event is based on, 
                // and search its ancestors.  Look for the DMC class typs that 
                // this schema node is based on.  If its found, then only one
                // IModelDelta needs to be generated for this schema node.
                // Otherwise, a IModelDelta needs to be created for every 
                // element in this schema node.  And for every element from 
                // this node, the child schema node needs to be called. 
                final Done childNodeDone = childNodesDoneTracker.addNoActionDone();
                IDataModelContext dmc = DMCs.getAncestorOfType(e.getDMC(), fDMCClassType);
                if (dmc != null) {
                    childNode.buildDelta(
                        e, 
                        parent.addNode(new DataVMC(parent.getVMC(), dmc), IModelDelta.NO_CHANGE), 
                        childNodeDone);
                } else {
                    getElements(
                        parent.getVMC(), 
                        new GetDataDone<IViewModelContext[]>() { public void run() {
                            if (!getStatus().isOK()) {
                                fExecutor.execute(done);
                                return;
                            } else {
                                // For each element in this schema node, create 
                                // a separate IModelDelta, and call the child
                                // schema node with it.
                                final DoneTracker doneTracker = new DoneTracker() { public void run() {
                                    fExecutor.execute(childNodeDone);                                
                                }};
                                for (IViewModelContext element : getData()) {
                                    childNode.buildDelta(
                                        e, 
                                        parent.addNode(element, IModelDelta.NO_CHANGE),
                                        doneTracker.addNoActionDone());
                                }
                            }
                        }});
                }
            }
        }
        
        // Check if there was anything to be done.  If not, invoke the client 
        // done.
        if (childNodesDoneTracker.getDones().isEmpty()) {
            fExecutor.execute(done);
        }            
    }
    
    /**
     * Returns the services tracker for sub-class use.
     */
    protected DsfServicesTracker getServicesTracker() {
        return fServices;
    }

    /**
     * Returns the executor for sub-class use.
     */
    protected DsfExecutor getExecutor() {
        return fExecutor;
    }
    
    /**
     * Utility method that takes an array of DMC object and creates a 
     * corresponding array of IViewModelContext elements base on that.   
     * @param parent The parent for generated IViewModelContext elements. 
     * @param dmContexts Array of DMC objects to build return array on.
     * @return Array of IViewModelContext objects.
     */
    protected IViewModelContext[] wrapDMContexts(IViewModelContext parent, IDataModelContext[] dmContexts) {
        IViewModelContext[] vmContexts = new IViewModelContext[dmContexts.length];
        for (int i = 0; i < dmContexts.length; i++) {
            vmContexts[i] = new DataVMC(parent, dmContexts[i]);
        }
        return vmContexts;
    }

    @SuppressWarnings("unchecked")
    public static <V extends IDataModelContext> V findDMContext(IViewModelContext vmc, Class<V> dmcType) {
        if (vmc instanceof DataViewModelSchemaNode.DataVMC &&
            dmcType.isAssignableFrom( ((DataViewModelSchemaNode.DataVMC)vmc).getDataModelContext().getClass() ))
        {
            return (V)((DataViewModelSchemaNode.DataVMC)vmc).getDataModelContext();
        } else if (vmc.getParent() != null) {
            return findDMContext(vmc.getParent(), dmcType);
        }
        return null;
    }
    
    public void sessionDispose() {
        // FIXME: should track when disposed and avoid issuing model deltas
        fServices.dispose();
        for (IViewModelSchemaNode childNode : getChildNodes()) {
            childNode.sessionDispose();
        }
    }
}
