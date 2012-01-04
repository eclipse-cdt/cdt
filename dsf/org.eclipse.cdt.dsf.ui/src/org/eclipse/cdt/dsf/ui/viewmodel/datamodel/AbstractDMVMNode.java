/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.datamodel;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;

/**
 * View model node based on a single IDMContext type. All the elements created
 * by this node are of that type.
 * 
 * @since 1.0
 */
abstract public class AbstractDMVMNode extends AbstractVMNode implements IVMNode {

    /**
     * IVMContext implementation used for this schema node.
     */
    @Immutable
    protected class DMVMContext extends AbstractVMContext implements IDMVMContext {
        private final IDMContext fDmc;
        
        public DMVMContext(IDMContext dmc) {
            super(AbstractDMVMNode.this);
            assert dmc != null;
            fDmc = dmc;
        }
        
        @Override
		public IDMContext getDMContext() { return fDmc; }
        
        /**
         * The IAdaptable implementation.  If the adapter is the DM context, 
         * return the context, otherwise delegate to IDMContext.getAdapter().
         */
        @Override
        @SuppressWarnings("rawtypes") 
        public Object getAdapter(Class adapter) {
            Object superAdapter = super.getAdapter(adapter);
            if (superAdapter != null) {
                return superAdapter;
            } else {
                // Delegate to the Data Model to find the context.
                if (adapter.isInstance(fDmc)) {
                    return fDmc;
                } else {
                    return fDmc.getAdapter(adapter);
                }
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
     * This type is used by the standard event processing logic to find the 
     * element in the event which is managed by this VM node.
     * 
     * @see #getContextsForEvent(VMDelta, Object, DataRequestMonitor)
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
    public void getContextsForEvent(VMDelta parentDelta, Object event, DataRequestMonitor<IVMContext[]> rm) {
        if (event instanceof IDMEvent<?>) {
            IDMEvent<?> dmEvent = (IDMEvent<?>)event;
            IDMContext dmc = DMContexts.getAncestorOfType(dmEvent.getDMContext(), fDMCClassType);
            if (dmc != null) {
                rm.setData(new IVMContext[] { createVMContext(dmc) });
                rm.done();
                return;
            }
        } 
        super.getContextsForEvent(parentDelta, event, rm);
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

    @Override
	public void update(final IHasChildrenUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                @Override
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

    @ConfinedToDsfExecutor("getSession().getExecutor()")
    protected void updateHasElementsInSessionThread(final IHasChildrenUpdate update) {
        update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "Not implemented, clients should call to update all children instead.", null)); //$NON-NLS-1$
        update.done();
    }

    @Override
	public void update(final IChildrenCountUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                @Override
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
    
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    protected void updateElementCountInSessionThread(final IChildrenCountUpdate update) {
        update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "Not implemented, clients should call to update all children instead.", null)); //$NON-NLS-1$
        update.done();
    }
        
    @Override
	public void update(final IChildrenUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                @Override
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

    @ConfinedToDsfExecutor("getSession().getExecutor()")
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
    
    /**
     * Fill update request with view model contexts based on given data model contexts.
     * Assumes that data model context elements start at index 0.
     * 
     * @param update  the viewer update request
     * @param dmcs  the data model contexts
     */
    protected void fillUpdateWithVMCs(IChildrenUpdate update, IDMContext[] dmcs) {
    	fillUpdateWithVMCs(update, dmcs, 0);
    }

    /**
     * Fill update request with view model contexts based on given data model contexts.
     * 
     * @param update  the viewer update request
     * @param dmcs  the data model contexts
     * @param firstIndex  the index of the first data model context
     * 
     * @since 1.1
     */
    protected void fillUpdateWithVMCs(IChildrenUpdate update, IDMContext[] dmcs, int firstIndex) {
        int updateIdx = update.getOffset() != -1 ? update.getOffset() : 0;
        final int endIdx = updateIdx + (update.getLength() != -1 ? update.getLength() : dmcs.length);
        int dmcIdx = updateIdx - firstIndex;
        if (dmcIdx < 0) {
        	updateIdx -= dmcIdx;
        	dmcIdx = 0;
        }
        while (updateIdx < endIdx && dmcIdx < dmcs.length) {
        	update.setChild(createVMContext(dmcs[dmcIdx++]), updateIdx++);
        }
    }
    
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new DMVMContext(dmc);
    }

    /**
     * Creates a default CompositeDMVMContext which represents the selection.
     * This can be overridden by view model providers which for their own purposes.
     * @param update defines the selection to be updated to
     * @return DM Context which represent the current selection 
     */
    protected IDMContext createCompositeDMVMContext(IViewerUpdate update) {
    	return new CompositeDMVMContext(update);
    }
    
    /**
     * Searches for a DMC of given type in the tree path contained in given 
     * VMC.  Only a DMC in the same session will be returned.
     * @param <V> Type of the DMC that will be returned.
     * @param vmc VMC element to search.
     * @param dmcType Class object for matching the type.
     * @return DMC, or null if not found.
     */
    protected <T extends IDMContext> T findDmcInPath(Object inputObject, TreePath path, Class<T> dmcType) {
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
