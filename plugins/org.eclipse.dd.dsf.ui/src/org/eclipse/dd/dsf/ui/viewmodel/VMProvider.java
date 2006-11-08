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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.DoneCollector;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IChildrenRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IContainerRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy;

/**
 * View model provider implements the asynchronous view model functionality for 
 * a single view.  This provider is just a holder which further delegates the
 * model provider functionality to the view model layout nodes that need
 * to be configured with each provider.
 * <p>
 * The view model provider, often does not provide the model for the entire 
 * view.  Rather, it needs to be able to plug in at any level in the viewer's
 * content model and provide data for a sub-tree.
 * 
 * @see IAsynchronousContentAdapter
 * @see IAsynchronousLabelAdapter
 * @see IModelProxy
 * @see IVMLayoutNode
 */
@ConfinedToDsfExecutor("fSession#getExecutor")
@SuppressWarnings("restriction")
public class VMProvider 
{
    @ThreadSafe
    public class ModelProxy extends AbstractModelProxy {
        /**
         * Counter for whether the model proxy is currently installed in the viewer.
         * Data model events are processed only if the model proxy is active.   
         */
        private int fProxyActive = 0;
        
        /** 
         * Scheduling rule for running the update jobs.  
         */
        private ISchedulingRule fModelChangeRule = new ISchedulingRule() {
            public boolean contains(ISchedulingRule rule) { return this == rule; }
            public boolean isConflicting(ISchedulingRule rule) { return rule == this; }
        };

        public void installed() {
            fProxyActive++;
        }
        
        public void dispose() {
            fProxyActive--;
            super.dispose();
        }
        
        @Override
        public void removeModelChangedListener(IModelChangedListener listener) {
            // TODO Auto-generated method stub
            super.removeModelChangedListener(listener);
        }
        
        @Override
        public void addModelChangedListener(IModelChangedListener listener) {
            // TODO Auto-generated method stub
            super.addModelChangedListener(listener);
        }

        /**
         * Fires given delta using a job.  Processing the delta on the dispatch
         * thread can lead to dead-locks.
         * @param delta
         */
        public void fireModelChangedNonDispatch(final IModelDelta delta) {
            if (fProxyActive <= 0) return;
            
            Job job = new Job("Processing view model delta.") { //$NON-NLS-1$
                protected IStatus run(IProgressMonitor monitor) {
                    fireModelChanged(delta);
                    return Status.OK_STATUS;
                }
            };
            job.setPriority(Job.INTERACTIVE);
            job.setRule(fModelChangeRule);
            job.schedule();
        }

    }
    
    private final DsfSession fSession;
    private final ModelProxy fModelProxy = new ModelProxy(); 

    private IVMRootLayoutNode fRootLayoutNode;
    
    /**
     * Constructs the view model provider for given DSF session.
     */
    public VMProvider(DsfSession session, IVMRootLayoutNode rootLayoutNode) {
        fSession = session;
        setRootLayoutNode(rootLayoutNode);
        // Add ourselves as listener for DM events events.
        session.addServiceEventListener(this, null);
    }    

    /** Sets the layout nodes */
    public void setRootLayoutNode(IVMRootLayoutNode rootLayoutNode) {
        if (fRootLayoutNode != null) {
            fRootLayoutNode.sessionDispose();
        }
        fRootLayoutNode = rootLayoutNode;
    }
    
    public IVMRootLayoutNode getRootLayoutNode() {
        return fRootLayoutNode;
    }
    
    /** Called to dispose the provider. */ 
    public void sessionDispose() {
        fSession.removeServiceEventListener(this);
        fRootLayoutNode.sessionDispose();
    }

    protected DsfSession getSession() { return fSession; }

    /** 
     * Performs the query to determine if given VNC is a container. 
     * Note: this method must be called on the provider's dispatch thread.
     * @see IAsynchronousContentAdapter#isContainer(Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, IContainerRequestMonitor)
     */
    @SuppressWarnings("unchecked")
    public void isContainer(Object parent, final IContainerRequestMonitor monitor) 
    {
        assert fSession.getExecutor().isInExecutorThread();

        IVMContext parentVmc = getVmcForObject(parent);
        if (parentVmc == null) {
            monitor.done();
            return;
        }        

        /*
         * If the element's model node has no child layout nodes, it's not a 
         * container.
         */
        if (parentVmc.getLayoutNode().getChildLayoutNodes().length == 0) {
            monitor.setIsContainer(false);
            monitor.done();
            return;
        }

        /*
         * For each child layout node, retrieve the list of elements.  When
         * all are done, If any of the child nodes have elements, notify the 
         * monitor that there are children.
         */
        final DoneCollector doneCollector = new DoneCollector(fSession.getExecutor()) { 
            public void run() {
                if (monitor.isCanceled()) return;
                
                boolean isContainer = false;
                for (Done hasElementsDone : getDones().keySet()) {
                    isContainer |= hasElementsDone.getStatus().isOK() &&
                                   ((GetDataDone<Boolean>)hasElementsDone).getData().booleanValue();
                }
                monitor.setIsContainer(isContainer);
                monitor.done();
            }
        };
        for (IVMLayoutNode childNode : parentVmc.getLayoutNode().getChildLayoutNodes()) {
            childNode.hasElements(
                parentVmc, 
                doneCollector.add( new GetDataDone<Boolean>() { public void run() {
                    doneCollector.doneDone(this);
                }}));
        }
    }

    /** 
     * Performs the query to retrieve children for the given VMC.
     * Note: this method must be called on the provider's dispatch thread.
     * @see IAsynchronousContentAdapter#retrieveChildren(Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, IChildrenRequestMonitor)
     */
    @SuppressWarnings("unchecked")
    public void retrieveChildren(final Object parent, final IChildrenRequestMonitor monitor) 
    {
        assert fSession.getExecutor().isInExecutorThread();

        IVMContext parentVmc = getVmcForObject(parent);
        if (parentVmc == null) {
            monitor.done();
            return;
        }        
        
        /*
         * If the element's model node has no child layout nodes.  There is 
         * nothing to do, just mark the monitor done. 
         */
        if (parentVmc.getLayoutNode().getChildLayoutNodes().length == 0) {
            assert false : "We should never get here, because isContainer() should have returned false"; //$NON-NLS-1$
            monitor.done();
            return;
        }

        /*
         * Iterate through the child layout nodes, and request their elements.
         * Requests are async, so use a tracker for the results. 
         */
        final DoneCollector doneCollector = new DoneCollector(fSession.getExecutor()) { 
            public void run() {
                if (monitor.isCanceled()) return;
                monitor.done(); 
            }
        };
        for (IVMLayoutNode childNode : parentVmc.getLayoutNode().getChildLayoutNodes()) {
            childNode.getElements(
                parentVmc, 
                doneCollector.add( new GetDataDone<IVMContext[]>() { 
                    public void run() {
                        if (getStatus().isOK()) {
                            monitor.addChildren(getData());
                        }
                        doneCollector.doneDone(this);
                    }
                }));
        }
    }

    /**
     * Convenience method that finds the VMC corresponding to given parent 
     * argument given to isContainer() or retrieveChildren().  
     * @param object Object to find the VMC for.
     * @return parent VMC, if null it indicates that the object did not originate 
     * from this view or is stale.
     */
    private IVMContext getVmcForObject(Object parent) {
        /*
         * First check to see if the parent object is the root object of the 
         * hierarchy.  If that's the case, then retrieve the correcponding
         * root VMC from the root node, and pass this root vmc to the root's 
         * child layout nodes.
         */
        if (parent.equals(fRootLayoutNode.getRootVMC().getInputObject())) {
            return fRootLayoutNode.getRootVMC();
        } else if (parent instanceof IVMContext){
            /*
             * The parent is a VMC.  Check to make sure that the VMC 
             * originated from a node in this ViewModelProvider.  If it didn't
             * it is most likely a result of a change in view layout, and this 
             * request is a stale request.  So just ignore it.
             */
            if (isOurLayoutNode( ((IVMContext)parent).getLayoutNode(), 
                                 new IVMLayoutNode[] { fRootLayoutNode } )) 
            {
                return (IVMContext)parent;
            }
        } 
        return null;
    }
    
    /**
     * Convenience method which checks whether given layout node is a node 
     * that is configured in this ViewModelProvider.  Implementation 
     * recursively walks the layout hierarchy, and returns true if it finds
     * the node. 
     */
    private boolean isOurLayoutNode(IVMLayoutNode layoutNode, IVMLayoutNode[] nodesToSearch) {
        for (IVMLayoutNode node : nodesToSearch) {
            if (node == layoutNode) return true;
            if (isOurLayoutNode(layoutNode, node.getChildLayoutNodes())) return true;
        }
        return false;
    }
    

    /**
     * Retrieves the label information for given VMC.
     * Note: this method must be called on the provider's dispatch thread.
     * @see IAsynchronousLabelAdapter#retrieveLabel(Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, ILabelRequestMonitor)
     */
    public void retrieveLabel(Object object, final ILabelRequestMonitor result) 
    {
        IVMContext vmc = getVmcForObject(object);
        if (vmc == null) {
            result.done();
            return;
        }        

        vmc.getLayoutNode().retrieveLabel(vmc, result);
    }

    public ModelProxy getModelProxy() {
        return fModelProxy;
    }
    
    /**
     * Handle "data model changed" event by generating a delta object for each 
     * view and passing it to the corresponding view model provider.  The view
     * model provider is then responsible for filling-in and sending the delta
     * to the viewer.
     * @param e
     */
    @DsfServiceEventHandler
    public void eventDispatched(final IDMEvent<?> event) {
        if (fRootLayoutNode.hasDeltaFlags(event)) {
            fRootLayoutNode.createDelta(event, new GetDataDone<IModelDelta>() {
                public void run() {
                    if (getStatus().isOK()) {
                        fModelProxy.fireModelChangedNonDispatch(getData());
                    }
                }
                @Override public String toString() {
                    return "Result of a delta for event: '" + event.toString() + "' in VMP: '" + VMProvider.this + "'";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            });
        }
    }
}
