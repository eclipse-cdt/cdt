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
package org.eclipse.dd.dsf.ui.viewmodel.dm;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.DsfUIPlugin;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;


/**
 * View model layout node based on a single Data Model Context type.  
 * The assumption in this implementation is that elements of this node have
 * a single IDMContext associated with them, and all of these contexts 
 * are of the same class type.   
 */
@SuppressWarnings("restriction")
abstract public class AbstractDMVMLayoutNode extends AbstractVMLayoutNode 
    implements IElementLabelProvider
{

    /**
     * IVMContext implementation used for this schema node.
     */
    @Immutable
    public class DMVMContext extends AbstractVMContext {
        private final IDMContext fDmc;
        
        public DMVMContext(IDMContext dmc) {
            super(getVMProvider().getVMAdapter(), AbstractDMVMLayoutNode.this);
            fDmc = dmc;
        }
        
        public IDMContext getDMC() { return fDmc; }
        
        /**
         * The IAdaptable implementation.  If the adapter is the DM context, 
         * return the context, otherwise delegate to IDMContext.getAdapter().
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            Object superAdapter = super.getAdapter(adapter);
            if (superAdapter != null) {
                return superAdapter;
            } else if (adapter.isInstance(fDmc)) {
                return fDmc;
            } else {
                return fDmc.getAdapter(adapter);
            }
        }
        
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof AbstractDMVMLayoutNode.DMVMContext)) return false;
            DMVMContext otherVmc = (DMVMContext)other;
            return AbstractDMVMLayoutNode.this.equals(otherVmc.getLayoutNode()) &&
                   fDmc.equals(otherVmc.fDmc);
        }
        
        @Override
        public int hashCode() {
            return AbstractDMVMLayoutNode.this.hashCode() + fDmc.hashCode(); 
        }
     
        @Override
        public String toString() {
            return fDmc.toString();
        }
    }

    private DsfSession fSession;
    
    private DsfServicesTracker fServicesTracker;
    
    /** 
     * Concrete class type that the elements of this schema node are based on.  
     * Even though the data model type is a parameter the DMContextVMLayoutNode, 
     * this type is erased at runtime, so a concrete class typs of the DMC
     * is needed for instanceof chacks.  
     */
    private Class<? extends IDMContext> fDMCClassType;

    /** 
     * Constructor initializes instance data, except for the child nodes.  
     * Child nodes must be initialized by calling setChildNodes()
     * @param session
     * @param dmcClassType
     * @see #setChildNodes(IVMLayoutNode[])
     */
    public AbstractDMVMLayoutNode(AbstractVMProvider provider, DsfSession session, Class<? extends IDMContext> dmcClassType) {
        super(provider);
        fSession = session;
        fServicesTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
        fDMCClassType = dmcClassType;
    }
     
    
    @Override
    public void dispose() {
        fServicesTracker.dispose();
        super.dispose();
    }
    
    protected DsfSession getSession() {
        return fSession;
    }

    protected DsfServicesTracker getServicesTracker() {
        return fServicesTracker; 
    }
    
    @Override
    protected boolean checkUpdate(IViewerUpdate update) {
        if (!super.checkUpdate(update)) return false;

        // Extract the VMC from the update (whatever the update sub-class. 
        Object element = update.getElement(); 
        if (element instanceof AbstractDMVMLayoutNode.DMVMContext) {
            // If update element is a DMC, check if session is still alive.
            IDMContext dmc = ((DMVMContext)element).getDMC();
            if (dmc.getSessionId() != getSession().getId() || !DsfSession.isSessionActive(dmc.getSessionId())) {
                handleFailedUpdate(update);
                return false;
            }
        }        
        return true;
    }

    /** 
     * Convenience method that checks whether the given dmc context is null.  If it is null, an 
     * appropriate error message is set in the update.
     * @param dmc Data Model Context (DMC) to check.
     * @param update Update to handle in case the DMC is null.
     * @return true if the DMC is NOT null, indicating that it's OK to proceed.  
     */
    protected boolean checkDmc(IDMContext dmc, IViewerUpdate update) {
        if (dmc == null) {
            update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfService.INVALID_STATE, 
                                        "No valid context found.", null)); //$NON-NLS-1$
            handleFailedUpdate(update);
            return false;
        }
        return true;
    }    

    /**
     * A convenience method that checks whether a given service exists.  If the service does not
     * exist, the update is filled in with the appropriate error message. 
     * @param serviceClass Service class to find.
     * @param filter Service filter to use in addition to the service class name.
     * @param update Update object to fill in.
     * @return true if service IS found, indicating that it's OK to proceed.  
     */
    protected boolean checkService(Class<? extends IDsfService> serviceClass, String filter, IViewerUpdate update) {
        if (getServicesTracker().getService(serviceClass, filter) == null) {
            update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfService.INVALID_STATE, 
                                        "Service " + serviceClass.getName() + " not available.", null)); //$NON-NLS-1$ //$NON-NLS-2$
            handleFailedUpdate(update);
            return false;
        }
        return true;
    }
    
    public void updateHasElements(final IHasChildrenUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    updateHasElementsInSessionThread(updates);                        
                }});
        } catch (RejectedExecutionException e) {
            for (IHasChildrenUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }

    protected void updateHasElementsInSessionThread(IHasChildrenUpdate[] updates) {
        for (final IHasChildrenUpdate update : updates) {
            if (!checkUpdate(update)) continue;
            
            updateElementsInSessionThread(
                new ElementsUpdate( 
                    new DataRequestMonitor<List<Object>>(getSession().getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (!checkUpdate(update)) return;
                            if (getStatus().isOK()) {
                                update.setHasChilren(getData().size() != 0);
                            } else {
                                update.setHasChilren(false);
                            }
                            update.done();
                        }
                    }, 
                    update.getElementPath())
                );
        }
    }

    public void updateElementCount(final IChildrenCountUpdate update) {
        if (!checkUpdate(update)) return;

        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    // After every dispatch, must check if update still valid. 
                    if (!checkUpdate(update)) return;
                    updateElementCountInSessionThread(update);                        
                }});
        } catch (RejectedExecutionException e) {
            handleFailedUpdate(update);
        }
    }
    
    protected void updateElementCountInSessionThread(final IChildrenCountUpdate update) {
        updateElementsInSessionThread(
            new ElementsUpdate( 
                new DataRequestMonitor<List<Object>>(getSession().getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!checkUpdate(update)) return;
                        if (getStatus().isOK()) {
                            update.setChildCount(getData().size());
                        } else {
                            update.setChildCount(0);
                        }
                        update.done();
                    }
                }, 
                update.getElementPath())
            );
    }
        
    public void updateElements(final IChildrenUpdate update) {
        if (!checkUpdate(update)) return;

        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    // After every dispatch, must check if update still valid. 
                    if (!checkUpdate(update)) return;
                    updateElementsInSessionThread(update);                        
                }});
        } catch (RejectedExecutionException e) {
            handleFailedUpdate(update);
        }
    }

    abstract protected void updateElementsInSessionThread(IChildrenUpdate update);

    public void update(final ILabelUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    // After every dispatch, must check if update still valid. 
                    updateLabelInSessionThread(updates);
                }});
        } catch (RejectedExecutionException e) {
            for (ILabelUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }
    
    abstract protected void updateLabelInSessionThread(ILabelUpdate[] updates);

    @Override
    public int getDeltaFlags(Object e) {
        int flags = 0;
        if (e instanceof IDMEvent) {
            flags = getNodeDeltaFlagsForDMEvent((IDMEvent<?>)e);
        } 
        return flags | super.getDeltaFlags(e);
    }
    
    protected int getNodeDeltaFlagsForDMEvent(@SuppressWarnings("unused") IDMEvent<?> e) {
        return IModelDelta.NO_CHANGE;
    }
    
    @Override
    public void buildDelta(final Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
        if (e instanceof IDMEvent) {
            // Call handler for Data Model events.  But check to make sure 
            // that session is still active.
            if (DsfSession.isSessionActive(getSession().getId())) {
                getSession().getExecutor().execute(new DsfRunnable() {
                    public void run() {
                        buildDeltaForDMEvent((IDMEvent<?>)e, parentDelta, nodeOffset, requestMonitor);
                    }
                });
            } else {
                if (isDisposed()) return;
                requestMonitor.done();
            }
        } else {
            super.buildDelta(e, parentDelta, nodeOffset, requestMonitor);
        }
    }
    
    /**
     * Handle all Data Model events.  If a DM context in the event contains 
     * a context of the type tracked by this node, then this base implementation
     * will only create a delta node for this one element.  
     */
    protected void buildDeltaForDMEvent(final IDMEvent<?> event, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
        IDMContext dmc = DMContexts.getAncestorOfType(event.getDMContext(), fDMCClassType);
        
        if (dmc != null) {
            // Create the VM context based on the DM context from the DM event.
            final IVMContext vmc = createVMContext(DMContexts.getAncestorOfType(event.getDMContext(), fDMCClassType));
    
            final Map<IVMLayoutNode,Integer> childNodeDeltas = getChildNodesWithDeltaFlags(event);
            if (childNodeDeltas.size() == 0) {
                // There are no child nodes with deltas, just return to parent.
                requestMonitor.done();
                return;
            }            
    
            // Check if any of the child nodes are will generate IModelDelta.SELECT  or 
            // IModelDelta.EXPAND flags.  If so, we must calcuate the index for this 
            // VMC.
            boolean calculateIndex = false;
            for (int childDelta : childNodeDeltas.values()) {
                if ( (childDelta & (IModelDelta.SELECT | IModelDelta.EXPAND)) != 0 ) {
                    calculateIndex = true;
                    break;
                }
            }
            
            if (calculateIndex) {
                // Calculate the index of this node by retrieving all the 
                // elements and then finding the DMC that the event is for.  
                updateElements(new ElementsUpdate(
                    new DataRequestMonitor<List<Object>>(getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (isDisposed()) return;
    
                            // Check for an empty list of elements.  If it's empty then we 
                            // don't have to call the children nodes, so return here.
                            // No need to propagate error, there's no means or need to display it.
                            if (!getStatus().isOK() || getData().isEmpty()) {
                                requestMonitor.done();
                                return;
                            }
                            
                            // Find the index.
                            int i;
                            for (i = 0; i < getData().size(); i++) {
                                if (vmc.equals(getData().get(i))) break;
                            }                            
                            if (i == getData().size()) {
                                // Element not found, no need to generate the delta.
                                requestMonitor.done();
                                return;
                            }
                            
                            VMDelta delta = parentDelta.addNode(vmc, nodeOffset + i, IModelDelta.NO_CHANGE);
                            callChildNodesToBuildDelta(childNodeDeltas, delta, event, requestMonitor);
                        }
                    }, 
                    parentDelta));        
            } else {
                VMDelta delta = parentDelta.addNode(vmc, IModelDelta.NO_CHANGE);
                callChildNodesToBuildDelta(childNodeDeltas, delta, event, requestMonitor);
            }            
        } else {
            // The DMC for this node was not found in the event.  Call the 
            // super-class to resort to the default behavior which may add a 
            // delta for every element in this node.
            super.buildDelta(event, parentDelta, nodeOffset, requestMonitor);
        }
    }
    
    /**
     * Utility method that takes an array of DMC object and creates a 
     * corresponding array of IVMContext elements base on that.   
     * @param parent The parent for generated IVMContext elements. 
     * @param dmcs Array of DMC objects to build return array on.
     * @return Array of IVMContext objects.
     */
    protected IVMContext[] dmcs2vmcs(IDMContext[] dmcs) {
        IVMContext[] vmContexts = new IVMContext[dmcs.length];
        for (int i = 0; i < dmcs.length; i++) {
            vmContexts[i] = new DMVMContext(dmcs[i]);
        }
        return vmContexts;
    }
    
    protected void fillUpdateWithVMCs(IChildrenUpdate update, IDMContext[] dmcs) {
        int startIdx = update.getOffset() != -1 ? update.getOffset() : 0;
        int endIdx = update.getLength() != -1 ? startIdx + update.getLength() : dmcs.length;
        // Ted: added bounds limitation of dmcs.length
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=202109
        for (int i = startIdx; i < endIdx && i < dmcs.length; i++) {
            update.setChild(createVMContext(dmcs[i]), i);
        }
    }
    
    protected IVMContext createVMContext(IDMContext dmc) {
        return new DMVMContext(dmc);
    }

    /**
     * Searches for a DMC of given type in the tree patch contained in given 
     * VMC.  Only a DMC in the same session will be returned.
     * @param <V> Type of the DMC that will be returned.
     * @param vmc VMC element to search.
     * @param dmcType Class object for matching the type.
     * @return DMC, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public <T extends IDMContext> T findDmcInPath(TreePath path, Class<T> dmcType) {
        T retVal = null;
        for (int i = path.getSegmentCount() - 1; i >= 0; i--) {
            if (path.getSegment(i) instanceof AbstractDMVMLayoutNode.DMVMContext) {
                IDMContext dmc = ((DMVMContext)path.getSegment(i)).getDMC();
                if ( dmc.getSessionId().equals(getSession().getId()) ) {
                    retVal = DMContexts.getAncestorOfType(dmc, dmcType);
                    if (retVal != null) break;
                }
            }
        }
        // Search the root object of the layout hierarchy.
        if (retVal == null) {
            Object inputObject = getVMProvider().getRootElement();
            if (inputObject instanceof ITreeSelection) {
                ITreeSelection inputSelection = (ITreeSelection)inputObject;
                if (inputSelection.getPaths().length == 1) {
                    retVal = findDmcInPath(inputSelection.getPaths()[0], dmcType);
                }
            } else if (inputObject instanceof IStructuredSelection) {
                Object rootElement = ((IStructuredSelection)inputObject).getFirstElement();
                if (rootElement instanceof AbstractDMVMLayoutNode.DMVMContext) {
                    retVal = DMContexts.getAncestorOfType(((DMVMContext)rootElement).getDMC(), dmcType);
                }
            } else if (inputObject instanceof AbstractDMVMLayoutNode.DMVMContext) {
                retVal = DMContexts.getAncestorOfType(((DMVMContext)inputObject).getDMC(), dmcType);
            }
        }
            
        return retVal;
    }
}
