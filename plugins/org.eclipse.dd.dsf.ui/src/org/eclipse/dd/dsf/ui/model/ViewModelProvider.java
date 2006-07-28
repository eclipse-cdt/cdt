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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.DoneTracker;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IChildrenRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IContainerRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy;

/**
 * View model provider implements the asynchronous view model functionality for 
 * a single view.  This provider is just a holder which further delegates the
 * model provider functionality to the view model schema nodes that need
 * to be configured with each provider.
 * <p>
 * The view model provider, often does not provide the model for the entire 
 * view.  Rather, it needs to be able to plug in at any level in the viewer's
 * content model and provide data for a sub-tree.
 * 
 * @see IAsynchronousContentAdapter
 * @see IAsynchronousLabelAdapter
 * @see IModelProxy
 * @see IViewModelSchemaNode
 */
@SuppressWarnings("restriction")
public class ViewModelProvider extends AbstractModelProxy 
{
    private DsfSession fSession;
    
    /**
     * Counter for whether the model proxy is currently installed in the viewer.
     * Data model events are processed only if the model proxy is active.   
     */
    private int fProxyActive = 0;
    
    /**
     * Schema nodes that define the contents of this view model.
     * They must be initialized with setRootSchemaNodes() before using the
     * model provider.
     */
    private IViewModelSchemaNode[] fRootSchemaNodes = new IViewModelSchemaNode[0];

    /** 
     * Root VMC node for the model.  The devault value may be overriden with
     * an object from a tree, by the data model adapter.
     */
    IViewModelContext fRootVMC = new IViewModelContext() {
        public IViewModelContext getParent() { return null; }
        public IViewModelSchemaNode getSchemaNode() { return null; }
        public Object getAdapter(Class adapter) { 
            return fSession.getModelAdapter(adapter); 
        }
        public String toString() {
            return "Root";
        }
    };

    /**
     * Constructs the view model provider for given DSF session.
     */
    public ViewModelProvider(DsfSession session) {
        fSession = session;
        fRootSchemaNodes = new IViewModelSchemaNode[0];
    }    

    /** Sets the schema nodes */
    public void setRootSchemaNodes(IViewModelSchemaNode[] rootSchemaNodes) {
        for (IViewModelSchemaNode node : fRootSchemaNodes) {
            node.sessionDispose();
        }    
        fRootSchemaNodes = rootSchemaNodes;
    }
    
    /** Sets the root element */
    public void setRootElement(IViewModelContext vmc) {
        fRootVMC = vmc;
    }
    
    /** Retrieves the root element */
    public IViewModelContext getRootElement() { return fRootVMC; }
    
    /** Called to dispose the provider. */ 
    public void sessionDispose() {
        for (IViewModelSchemaNode node : fRootSchemaNodes) {
            node.sessionDispose();
        }    
    }
    
    /** 
     * Performs the query to determine if given VNC is a container. 
     * Note: this method must be called on the provider's dispatch thread.
     * @see IAsynchronousContentAdapter#isContainer(Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, IContainerRequestMonitor)
     */
    @SuppressWarnings("unchecked")
    public void isContainer(IViewModelContext vmc, final IContainerRequestMonitor monitor) 
    {
        assert fSession.getExecutor().isInExecutorThread();

        // If the VMC is the root element, use the root schema nodes to 
        // collect the list of children.  Otherwise, get the child schema nodes 
        // out of VMC's schema node.
        IViewModelSchemaNode[] childSchemaNodes;
        if (vmc == fRootVMC) {
            childSchemaNodes = fRootSchemaNodes;
        } else {
            childSchemaNodes = vmc.getSchemaNode().getChildNodes();
        }
        
        // For each child schema node, retrieve the list of elements.  When 
        // all are done, notify the request monitor.
        final DoneTracker doneTracker = new DoneTracker() { 
            public void run() {
            boolean isContainer = false;
            for (Done hasElementsDone : getDones().keySet()) {
                isContainer |= hasElementsDone.getStatus().isOK() &&
                               ((GetDataDone<Boolean>)hasElementsDone).getData().booleanValue();
            }
            monitor.setIsContainer(isContainer);
            monitor.done();
        }};
        for (IViewModelSchemaNode childNode : childSchemaNodes) {
            childNode.hasElements(
                vmc, 
                doneTracker.add( new GetDataDone<Boolean>() { public void run() {
                    doneTracker.doneDone(this);
                }}));
        }
    }

    /** 
     * Performs the query to retrieve children for the given VMC.
     * Note: this method must be called on the provider's dispatch thread.
     * @see IAsynchronousContentAdapter#retrieveChildren(Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, IChildrenRequestMonitor)
     */
    @SuppressWarnings("unchecked")
    public void retrieveChildren(final IViewModelContext vmc, final IChildrenRequestMonitor monitor) 
    {
        assert fSession.getExecutor().isInExecutorThread();

        // Get the child nodes as in isContainer().
        IViewModelSchemaNode[] childSchemaNodes;
        if (vmc == fRootVMC) {
            childSchemaNodes = fRootSchemaNodes;
        } else {
            childSchemaNodes = vmc.getSchemaNode().getChildNodes();
        }

        // Collect the elements from each child schema node.
        final DoneTracker doneTracker = new DoneTracker() { public void run() {
            monitor.done();
        }};
        for (IViewModelSchemaNode childNode : childSchemaNodes) {
            childNode.getElements(
                vmc, 
                doneTracker.add( new GetDataDone<IViewModelContext[]>() { public void run() {
                    monitor.addChildren(getData());
                    doneTracker.doneDone(this);
                }}));
        }
    }
    
    /**
     * Retrieves the label information for given VMC.
     * Note: this method must be called on the provider's dispatch thread.
     * @see IAsynchronousLabelAdapter#retrieveLabel(Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, ILabelRequestMonitor)
     */
    public void retrieveLabel(IViewModelContext vmc, final ILabelRequestMonitor result) 
    {
        assert fSession.getExecutor().isInExecutorThread();
        vmc.getSchemaNode().retrieveLabel(vmc, result);
    }

    ///////////////////////////////////////////////////////////////////////////
    // IModelProxy
    private ISchedulingRule fModelChangeRule = new ISchedulingRule() {
        public boolean contains(ISchedulingRule rule) { return this == rule; }
        public boolean isConflicting(ISchedulingRule rule) { return rule == this; }
    };

    public void installed() {
        fProxyActive++;
    }
    
    public void dipose() {
        fProxyActive--;
        super.dispose();
    }
    
    /**
     * Called by the async. data model adapter, this method generates and 
     * dispatches the view model delta for the given data model event.
     * Note: the root node in the delta must be supplied to the view model
     * provider, because the root view model provider node may not be at the
     * root of the viewer's tree.
     * @param rootDeltaNode Root node to use for additional delta. 
     * @param e Data model event received.
     */
    public void handleDataModelEvent(final ViewModelDelta rootDeltaNode, IDataModelEvent e) {
        // Go through all the schema nodes and collect delta information for 
        // the received event.

        DoneTracker doneTracker = new DoneTracker() { public void run() {
            if (rootDeltaNode.getFlags() != IModelDelta.NO_CHANGE || rootDeltaNode.getNodes().length != 0) {
                // Fire the delta only if there are changes.
                fireModelChangedNonDispatch(rootDeltaNode);
            }
        }};
        
        for (final IViewModelSchemaNode childNode : fRootSchemaNodes) {
            if (childNode.hasDeltaFlags(e)) {
                childNode.buildDelta(e, rootDeltaNode, doneTracker.addNoActionDone());
            }
        }
    }

    /**
     * Fires given delta using a job.  Processing the delta on the dispatch
     * thread can lead to dead-locks.
     * @param delta
     */
    private void fireModelChangedNonDispatch(final IModelDelta delta) {
        if (fProxyActive <= 0) return;
        
        Job job = new Job("Computing isContainer") { //$NON-NLS-1$
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
