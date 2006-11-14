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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.DoneCollector;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.DsfUIPlugin;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode.IRootVMC;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentationFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;


/**
 * View model layout node based on a single Data Model Context type.  
 * The assumption in this implementation is that elements of this node have
 * a single IDMContext associated with them, and all of these contexts 
 * are of the same class type.   
 */
@SuppressWarnings("restriction")
abstract public class DMContextVMLayoutNode<V extends IDMData> extends AbstractVMLayoutNode {

    /**
     * IVMContext implementation used for this schema node.
     */
    @Immutable
    public class DMContextVMContext implements IVMContext {
        private final IVMContext fParent;
        private final IDMContext<V> fDmc;
        
        public DMContextVMContext(IVMContext parent, IDMContext<V> dmc) {
            fParent = parent;
            fDmc = dmc;
        }
        
        public IDMContext<V> getDMC() { return fDmc; }
        public IVMContext getParent() { return fParent; }
        public IVMLayoutNode getLayoutNode() { return DMContextVMLayoutNode.this; }
        
        /**
         * The IAdaptable implementation.  If the adapter is the DM context, 
         * return the context, otherwise delegate to IDMContext.getAdapter().
         */
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            if (adapter.isInstance(fDmc)) {
                return fDmc;
            } else {
                return fDmc.getAdapter(adapter);
            }
        }
        
        public boolean equals(Object other) {
            if (!(other instanceof DMContextVMLayoutNode.DMContextVMContext)) return false;
            DMContextVMLayoutNode.DMContextVMContext otherVmc = (DMContextVMLayoutNode.DMContextVMContext)other;
            return DMContextVMLayoutNode.this.equals(otherVmc.getLayoutNode()) &&
                   fParent.equals(otherVmc.fParent) && 
                   fDmc.equals(otherVmc.fDmc);
        }
        
        public int hashCode() {
            return DMContextVMLayoutNode.this.hashCode() + fParent.hashCode() + fDmc.hashCode(); 
        }
     
        public String toString() {
            return fParent.toString() + "->" + fDmc.toString(); //$NON-NLS-1$
        }
    }

    /** Service tracker to be used by sub-classes */
    private DsfServicesTracker fServices;
    
    private DsfSession fSession;
    
    /** 
     * Concrete class type that the elements of this schema node are based on.  
     * Even though the data model type is a parameter the DMContextVMLayoutNode, 
     * this type is erased at runtime, so a concrete class typs of the DMC
     * is needed for instanceof chacks.  
     */
    private Class<? extends IDMContext<V>> fDMCClassType;

    /** 
     * Constructor initializes instance data, except for the child nodes.  
     * Child nodes must be initialized by calling setChildNodes()
     * @param session
     * @param dmcClassType
     * @see #setChildNodes(IVMLayoutNode[])
     */
    public DMContextVMLayoutNode(DsfSession session, Class<? extends IDMContext<V>> dmcClassType) {
        super(session.getExecutor());
        fSession = session;
        fServices = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());        
        fDMCClassType = dmcClassType;
    }
     
    /** 
     * Returns the session for use by sub-classes.
     */
    protected DsfSession getSession() {
        return fSession;
    }
    
    /**
     * Returns the services tracker for sub-class use.
     */
    protected DsfServicesTracker getServicesTracker() {
        return fServices;
    }

    /**
     * The default implementation of the retrieve label method.  It acquires 
     * the service, using parameters in the DMC, then it fetches the model 
     * data from the service, and then it calls the protected method 
     * fillColumnLabel() for each column.  The deriving classes should override
     * this method if a different method of computing the label is needed.
     * 
     * @see #fillColumnLabel(IDMData, String, int, String[], ImageDescriptor[], FontData[], RGB[], RGB[])
     */
    @SuppressWarnings("unchecked")
    public void retrieveLabel(IVMContext vmc, final ILabelRequestMonitor result, final String[] columns) {
        /*
         *  Extract the DMContext from the VMContext, see DMContextVMContext.getAdapter().
         *  Since the VMContext is supplied by this node, the DMContext should never be null.
         *  Note: had to suppress type cast warnings here, because getAdapter() does not support 
         *  generics, and even if it did, I'm not sure it would help.
         */
        final IDMContext<V> dmc = (IDMContext<V>)(vmc).getAdapter(IDMContext.class);
        if (dmc == null) {
            assert false;
            result.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "Invalid VMC type", null)); //$NON-NLS-1$
            result.done();
            return;
        }
        
        /* 
         * Get the instance of the service using the service filter in the DMContext
         * If null it could mean that the service already shut down, and the view 
         * is holding stale elements which will be cleaned up shortly.
         */
        IDMService dmService  = (IDMService)getServicesTracker().getService(null, dmc.getServiceFilter());
        if (dmService == null) {
            handleFailedRetrieveLabel(result);
            return;
        }
        
        dmService.getModelData(
            dmc, 
            new GetDataDone<V>() { 
                public void run() {
                    /*
                     * Check that the request was evaluated and data is still
                     * valid.  The request could fail if the state of the 
                     * service changed during the request, but the view model
                     * has not been updated yet.
                     */ 
                    if (!getStatus().isOK() || !getData().isValid()) {
                        assert getStatus().isOK() || 
                               getStatus().getCode() != IDsfService.INTERNAL_ERROR || 
                               getStatus().getCode() != IDsfService.NOT_SUPPORTED; 
                        handleFailedRetrieveLabel(result);
                        return;
                    }
                    
                    /*
                     * If columns are configured, call the protected methods to 
                     * fill in column values.  
                     */
                    String[] localColumns = columns;
                    if (localColumns == null) localColumns = new String[] { null };
                    
                    String[] text = new String[localColumns.length];
                    ImageDescriptor[] image = new ImageDescriptor[localColumns.length];
                    FontData[] fontData = new FontData[localColumns.length];
                    RGB[] foreground = new RGB[localColumns.length];
                    RGB[] background = new RGB[localColumns.length];
                    for (int i = 0; i < localColumns.length; i++) {
                        fillColumnLabel(dmc, getData(), localColumns[i], i, text, image, fontData, foreground, background);
                    }
                    result.setLabels(text);
                    result.setImageDescriptors(image);
                    result.setFontDatas(fontData);
                    result.setBackgrounds(background);
                    result.setForegrounds(foreground);
                    result.done();
                }
            });
    }

    /**
     * Fills in label information for given column.  This method is intended to 
     * be overriden by deriving classes, to supply label information specific
     * to the node. <br>  
     * The implementation should fill in the correct value in each array at the 
     * given index.
     * @param dmContext Data Model Context object for which the label is generated.
     * @param dmData Data Model Data object retrieved from the model service.
     * for the DM Context supplied to the retrieveLabel() call.
     * @param columnId Name of the column to fill in, null if no columns specified.
     * @param idx Index to fill in in the label arrays.
     * @param text 
     * @param image
     * @param fontData
     * @param foreground
     * @param background
     * 
     * @see IAsynchronousLabelAdapter
     * @see IColumnPresentationFactoryAdapter
     */
    protected void fillColumnLabel(IDMContext<V> dmContext, V dmData, String columnId, int idx, String[] text, 
                                   ImageDescriptor[] image, FontData[] fontData, RGB[] foreground, RGB[] background ) 
    {
        text[idx] = ""; //$NON-NLS-1$
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
    protected boolean hasDeltaFlagsForDMEvent(IDMEvent<?> e) {
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
    public void buildDeltaForDMEvent(final IDMEvent<?> e, final VMDelta parent, final Done done) {
        /* 
         * Take the IDMContext (DMC) that the event is based on, and 
         * search its ancestors.  Look for the DMC class typs that this schema 
         * node is based on.  If its found, then only one IModelDelta needs to 
         * be generated for this schema node. Otherwise, resort to the default 
         * behavior and generate a IModelDelta for every element in this schema 
         * node. 
         */
        IDMContext<V> dmc = DMContexts.getAncestorOfType(e.getDMContext(), fDMCClassType);
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
    protected IVMContext[] dmcs2vmcs(IVMContext parent, IDMContext<V>[] dmcs) {
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
    
    public void dispose() {
        fServices.dispose();
        super.dispose();
    }
}
