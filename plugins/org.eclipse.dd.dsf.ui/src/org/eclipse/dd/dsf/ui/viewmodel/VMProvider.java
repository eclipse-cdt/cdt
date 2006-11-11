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

import java.util.concurrent.atomic.AtomicReference;

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
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditor;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditorFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentationFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IContainerRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;

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
    private final DsfSession fSession;
    private final ModelProxy fModelProxy = new ModelProxy(); 

    /**
     * It is theoretically possible for a VMProvider to be disposed before it 
     * has a chance to register itself as event listener.  This flag is used
     * to avoid removing itself as listener in such situation.
     */
    private boolean fRegisteredAsEventListener = false;

    /**
     * The root node for this model provider.  The root layout node could be 
     * null when first created, to allow sub-classes to prorperly configure the 
     * root node in the sub-class constructor.  
     */
    private AtomicReference<IVMRootLayoutNode> fRootLayoutNodeRef = new AtomicReference<IVMRootLayoutNode>();
    
    /**
     * Constructs the view model provider for given DSF session.  The 
     * constructor is thread-safe to allow VM provider to be constructed
     * synchronously when a call to getAdapter() is made on an element 
     * in a view.
     */
    @ThreadSafe
    public VMProvider(DsfSession session, IVMRootLayoutNode rootLayoutNode) {
        fSession = session;
        fRootLayoutNodeRef.set(rootLayoutNode);
        // Add ourselves as listener for DM events events.
        session.getExecutor().execute(new Runnable() {
            public void run() {
                if (DsfSession.isSessionActive(getSession().getId())) {
                    getSession().addServiceEventListener(VMProvider.this, null);
                    fRegisteredAsEventListener = true;
                }
            }
        });
    }    

    /** 
     * Sets the layout nodes.  This method is thread-safe, because it might
     * be called fromthe constructor, which itself is thread-safe. 
     */
    @ThreadSafe
    public void setRootLayoutNode(IVMRootLayoutNode rootLayoutNode) {
        final IVMRootLayoutNode oldRootLayoutNode = fRootLayoutNodeRef.getAndSet(rootLayoutNode); 
        if (oldRootLayoutNode != null) {
            // IVMLayoutNode has to be called on dispatch thread... for now at least.
            getSession().getExecutor().execute( new Runnable() {
                public void run() {
                    oldRootLayoutNode.dispose();
                }
            });
        }
    }
    
    public IVMRootLayoutNode getRootLayoutNode() {
        return fRootLayoutNodeRef.get();
    }
    
    /** Called to dispose the provider. */ 
    public void dispose() {
        if (fRegisteredAsEventListener) {
            fSession.removeServiceEventListener(this);
        }
        if (fRootLayoutNodeRef != null) {
            fRootLayoutNodeRef.get().dispose();
        }
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
     * Retrieves the label information for given object.  
     * The implementation converts the object into a VM-Context, then delegates 
     * to the context's layout node.
     * Note: this method must be called on the provider's dispatch thread.

     * @see IAsynchronousLabelAdapter#retrieveLabel(Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, ILabelRequestMonitor)
     */
    public void retrieveLabel(Object object, ILabelRequestMonitor result, String[] columns) 
    {
        IVMContext vmc = getVmcForObject(object);
        if (vmc == null) {
            result.done();
            return;
        }        

        vmc.getLayoutNode().retrieveLabel(vmc, result, columns);
    }

    public ModelProxy getModelProxy() {
        return fModelProxy;
    }

    
    /**
     * Creates the column presentation for the given object.  This method is meant
     * to be overriden by deriving class to provide view-specific functionality.
     * The default is to return null, meaning no columns. 
     * <p>
     * The viewer only reads the column presentation for the root/input element of 
     * the tree/table, so the VMProvider must be configured to own the root element 
     * in the view in order for this setting to be effective.   
     * <p>
     * Note: since the IColumnEditorFactoryAdapter interface is synchronous, and since
     * column info is fairly static, this method is thread-safe, and it will
     * not be called on the executor thread.
     * 
     * @see IColumnPresentationFactoryAdapter#createColumnPresentation(IPresentationContext, Object)
     */
    @ThreadSafe
    public IColumnPresentation createColumnPresentation(Object element) {
        return null;
    }
    
    /**
     * Returns the ID of the column presentation for the given object.  This method 
     * is meant to be overriden by deriving class to provide view-specific 
     * functionality. The default is to return null, meaning no columns. 
     * <p>
     * The viewer only reads the column presentation for the root/input element of 
     * the tree/table, so the VMProvider must be configured to own the root element 
     * in the view in order for this setting to be effective.   
     * <p>
     * Note: since the IColumnEditorFactoryAdapter interface is synchronous, and since
     * column info is fairly static, this method is thread-safe, and it will
     * not be called on the executor thread.
     * 
     * @see IColumnEditorFactoryAdapter#getColumnEditorId(IPresentationContext, Object)
     */
    @ThreadSafe
    public String getColumnPresentationId(Object element) {
        return null;
    }

    public IColumnEditor createColumnEditor(Object element) {
        IVMContext vmc = getVmcForObject(element);
        if (vmc == null) {
            return null;
        }        

        return vmc.getLayoutNode().createColumnEditor(vmc);
    }
    
    public String getColumnEditorId(Object element) {
        IVMContext vmc = getVmcForObject(element);
        if (vmc == null) {
            return null;
        }        

        return vmc.getLayoutNode().getColumnEditorId(vmc);
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
        IVMRootLayoutNode rootLayoutNode = getRootLayoutNode();
        if (rootLayoutNode == null) {
            return null;
        } 
        else if (parent.equals(rootLayoutNode.getRootVMC().getInputObject())) {
            return rootLayoutNode.getRootVMC();
        } 
        else if (parent instanceof IVMContext){
            /*
             * The parent is a VMC.  Check to make sure that the VMC 
             * originated from a node in this ViewModelProvider.  If it didn't
             * it is most likely a result of a change in view layout, and this 
             * request is a stale request.  So just ignore it.
             */
            if (isOurLayoutNode( ((IVMContext)parent).getLayoutNode(), 
                                 new IVMLayoutNode[] { rootLayoutNode } )) 
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
     * Handle "data model changed" event by generating a delta object for each 
     * view and passing it to the corresponding view model provider.  The view
     * model provider is then responsible for filling-in and sending the delta
     * to the viewer.
     * @param e
     */
    @DsfServiceEventHandler
    public void eventDispatched(final IDMEvent<?> event) {
        IVMRootLayoutNode rootLayoutNode = getRootLayoutNode();
        
        if (rootLayoutNode != null && rootLayoutNode.hasDeltaFlags(event)) {
            rootLayoutNode.createDelta(event, new GetDataDone<IModelDelta>() {
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
}
