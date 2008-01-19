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

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.DsfUIPlugin;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;


/**
 * View model node based on a single Data Model Context type.  
 * The assumption in this implementation is that elements of this node have
 * a single IDMContext associated with them, and all of these contexts 
 * are of the same class type.   
 */
@SuppressWarnings("restriction")
abstract public class AbstractDMVMNode extends AbstractVMNode implements IVMNode {

    /**
     * IVMContext implementation used for this schema node.
     */
    @Immutable
    protected class DMVMContext extends AbstractVMContext implements IDMVMContext {
        private final IDMContext fDmc;
        
        public DMVMContext(IDMContext dmc) {
            super(getVMProvider().getVMAdapter(), AbstractDMVMNode.this);
            fDmc = dmc;
        }
        
        public IDMContext getDMContext() { return fDmc; }
        
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
            if (!(other instanceof AbstractDMVMNode.DMVMContext)) return false;
            DMVMContext otherVmc = (DMVMContext)other;
            return AbstractDMVMNode.this.equals(otherVmc.getVMNode()) &&
                   fDmc.equals(otherVmc.fDmc);
        }
        
        @Override
        public int hashCode() {
            return AbstractDMVMNode.this.hashCode() + fDmc.hashCode(); 
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
     * @see #setChildNodes(IVMNode[])
     */
    public AbstractDMVMNode(AbstractDMVMProvider provider, DsfSession session, Class<? extends IDMContext> dmcClassType) {
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
    
    @Override
    public IVMContext getContextFromEvent(Object event) {
        if (event instanceof IDMEvent<?>) {
            IDMEvent<?> dmEvent = (IDMEvent<?>)event;
            IDMContext dmc = DMContexts.getAncestorOfType(dmEvent.getDMContext(), fDMCClassType);
            if (dmc != null) {
                return createVMContext(dmc);
            }
        }
        return null;
    }

    protected AbstractDMVMProvider getDMVMProvider() {
        return (AbstractDMVMProvider)getVMProvider();
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
        if (element instanceof IDMVMContext) {
            // If update element is a DMC, check if session is still alive.
            IDMContext dmc = ((IDMVMContext)element).getDMContext();
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
    
    public void update(final IHasChildrenUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    for (IHasChildrenUpdate update : updates) {
                        if (!checkUpdate(update)) continue;
                        updateHasElementsInSessionThread(update);
                    }
                }});
        } catch (RejectedExecutionException e) {
            for (IViewerUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }

    protected void updateHasElementsInSessionThread(final IHasChildrenUpdate update) {
        update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfService.NOT_SUPPORTED, "Not implemented, clients should call to update all children instead.", null)); //$NON-NLS-1$
        update.done();
    }

    public void update(final IChildrenCountUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    for (IChildrenCountUpdate update : updates) {
                        if (!checkUpdate(update)) continue;
                        updateElementCountInSessionThread(update);                        
                    }
                }});
        } catch (RejectedExecutionException e) {
            for (IViewerUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }
    
    protected void updateElementCountInSessionThread(final IChildrenCountUpdate update) {
        update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfService.NOT_SUPPORTED, "Not implemented, clients should call to update all children instead.", null)); //$NON-NLS-1$
        update.done();
    }
        
    public void update(final IChildrenUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    // After every dispatch, must check if update still valid. 
                    for (IChildrenUpdate update : updates) {
                        if (!checkUpdate(update)) continue;
                        updateElementsInSessionThread(update);                        
                    }
                }});
        } catch (RejectedExecutionException e) {
            for (IViewerUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }

    abstract protected void updateElementsInSessionThread(IChildrenUpdate update);

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
            vmContexts[i] = createVMContext(dmcs[i]);
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
    
    protected IDMVMContext createVMContext(IDMContext dmc) {
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
    public <T extends IDMContext> T findDmcInPath(Object inputObject, TreePath path, Class<T> dmcType) {
        T retVal = null;
        for (int i = path.getSegmentCount() - 1; i >= 0; i--) {
            if (path.getSegment(i) instanceof IDMVMContext) {
                IDMContext dmc = ((IDMVMContext)path.getSegment(i)).getDMContext();
                if ( dmc.getSessionId().equals(getSession().getId()) ) {
                    retVal = DMContexts.getAncestorOfType(dmc, dmcType);
                    if (retVal != null) break;
                }
            }
        }
        // Search the root object of the layout hierarchy.
        if (retVal == null) {
            if (inputObject instanceof ITreeSelection) {
                ITreeSelection inputSelection = (ITreeSelection)inputObject;
                if (inputSelection.getPaths().length == 1) {
                    retVal = findDmcInPath(null, inputSelection.getPaths()[0], dmcType);
                }
            } else if (inputObject instanceof IStructuredSelection) {
                Object rootElement = ((IStructuredSelection)inputObject).getFirstElement();
                if (rootElement instanceof IDMVMContext) {
                    retVal = DMContexts.getAncestorOfType(((IDMVMContext)rootElement).getDMContext(), dmcType);
                }
            } else if (inputObject instanceof IDMVMContext) {
                retVal = DMContexts.getAncestorOfType(((IDMVMContext)inputObject).getDMContext(), dmcType);
            }
        }
            
        return retVal;
    }
}
