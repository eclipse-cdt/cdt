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
package org.eclipse.cdt.dsf.ui.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.internal.LoggingUtils;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.concurrent.SimpleDisplayExecutor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.update.UserEditEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.swt.widgets.Display;

/**
 * View model provider implements the asynchronous view model functionality for 
 * a single view.  This provider is just a holder which further delegates the
 * model provider functionality to the view model nodes that need
 * to be configured with each provider.
 * 
 * <p/>
 * The view model provider, often does not provide the model for the entire 
 * view.  Rather, it needs to be able to plug in at any level in the viewer's
 * content model and provide data for a sub-tree.
 * 
 * <p/>
 * Clients are intended to extend this class.
 * 
 * @see IModelProxy
 * @see IVMNode
 * 
 * @since 1.0
 */
abstract public class AbstractVMProvider implements IVMProvider, IVMEventListener
{
    // debug flags
    /** @since 1.1 */
    public static String DEBUG_PRESENTATION_ID = null;

    /** @since 1.1 */
    public static boolean DEBUG_CONTENT_PROVIDER = false;

    /** @since 1.1 */
    public static boolean DEBUG_DELTA = false;

    static {
        DEBUG_PRESENTATION_ID = Platform.getDebugOption("org.eclipse.cdt.dsf.ui/debug/vm/presentationId"); //$NON-NLS-1$
        if (!DsfUIPlugin.DEBUG || "".equals(DEBUG_PRESENTATION_ID)) { //$NON-NLS-1$
            DEBUG_PRESENTATION_ID = null;
        }
        DEBUG_CONTENT_PROVIDER = DsfUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
         Platform.getDebugOption("org.eclipse.cdt.dsf.ui/debug/vm/contentProvider")); //$NON-NLS-1$

        DEBUG_DELTA = DsfUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
            Platform.getDebugOption("org.eclipse.cdt.dsf.ui/debug/vm/delta")); //$NON-NLS-1$
    }   

    /** Reference to the VM adapter that owns this provider */
    private final AbstractVMAdapter fVMAdapter;
    
    /** The presentation context that this provider is associated with */
    private final IPresentationContext fPresentationContext;

    /**
     * The executor that this VM provider operates in.  This executor will be 
     * initialized properly when we can access the display from the 
     * IPresentationContext object (bug 213629).  For now utilize the 
     * assumption that there is only one display. 
     */
    private final Executor fExecutor = SimpleDisplayExecutor.getSimpleDisplayExecutor(Display.getDefault());

    /**
     * The element content provider implementation that this provider delegates to.
     * Sub-classes may override the content strategy used for custom functionality.   
     */
    private final IElementContentProvider fContentStrategy;
    
    /**
     * The list of active model proxies in this provider.  A new model
     * proxy is created when a viewer has a new input element 
     * (see {@link #createModelProxy(Object, IPresentationContext)}).  
     * Typically there will be only one active model proxy in a given
     * provider.  However, if a view model provider fills only a sub-tree
     * in a viewer, and there are several sub-trees active in the same viewer
     * at the same time, each of these sub-trees will have it's own model 
     * proxy.
     */
    private List<IVMModelProxy> fActiveModelProxies = new LinkedList<IVMModelProxy>();

    /**
     * Convencience constant.
     */
    private static final IVMNode[] EMPTY_NODES_ARRAY = new IVMNode[0];
    
    
    /**
     * The mapping of parent to child nodes.  
     */
    private Map<IVMNode,IVMNode[]> fChildNodesMap = 
        new HashMap<IVMNode,IVMNode[]>();
        
    /** 
     * Cached array of all the configued view model nodes.  It is generated 
     * based on the child nodes map.
     */
    private IVMNode[] fNodesListCache = null;

    /**
     * Flag indicating that the provider is disposed.
     */
    private boolean fDisposed = false;

    /**
     * The root node for this model provider.  The root layout node could be 
     * null when first created, to allow sub-classes to prorperly configure the 
     * root node in the sub-class constructor.  
     */
    private IRootVMNode fRootNode;
    
    private class EventInfo {
        EventInfo(Object event, RequestMonitor rm) {
            fEvent = event;
            fClientRm = rm;
        }
        Object fEvent;
        RequestMonitor fClientRm;
    }
    
    private class ModelProxyEventQueue {
        /** The event actively being handled */
        EventInfo fCurrentEvent;

		/**
		 * The request monitor we created to handle fCurrentEvent. It is
		 * responsible for calling <code>done</code> on the client RM of that
		 * event.
		 */
        RequestMonitor fCurrentRm;
        
        /** The queue */
        List<EventInfo> fEventQueue = new LinkedList<EventInfo>();
    }
    
    private Map<IVMModelProxy, ModelProxyEventQueue> fProxyEventQueues = new HashMap<IVMModelProxy, ModelProxyEventQueue>();
    
    /**
     * Constructs the view model provider for given DSF session.  The 
     * constructor is thread-safe to allow VM provider to be constructed
     * synchronously when a call to getAdapter() is made on an element 
     * in a view.
     */
    public AbstractVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext) {
        fVMAdapter = adapter;
        fPresentationContext = presentationContext;
        fContentStrategy = createContentStrategy();
    }    

    public IPresentationContext getPresentationContext() {
        return fPresentationContext;
    }

    public AbstractVMAdapter getVMAdapter() {
        return fVMAdapter;
    }

    /**
     * Creates the strategy class that will be used to implement the content 
     * provider interface of this view model provider.  This method can be 
     * overridden by sub-classes to provider custom content provider strategy.
     * <p/>
     * Note this method can be called by the base class constructor, therefore 
     * it should not reference any fields initialized in the sub-class.
     * 
     * @return New content provider implementation.
     */
    protected IElementContentProvider createContentStrategy() {
        return new DefaultVMContentProviderStrategy(this);
    }

    /**
     * Access method for the content provider strategy.
     * 
     * @return Content provider implementation currently being used by this 
     * class.
     */
    protected IElementContentProvider getContentStrategy() {
        return fContentStrategy;
    }
    
    /**
     * Creates the strategy class that will be used to implement the content 
     * model proxy of this view model provider.  It is normally called by 
     * {@link #createModelProxy(Object, IPresentationContext)} every time the 
     * input in the viewer is updated. This method can be overridden by 
     * sub-classes to provider custom model proxy strategy.
     * 
     * @return New model proxy implementation.
     */
    protected IVMModelProxy createModelProxyStrategy(Object rootElement) {
        return new DefaultVMModelProxyStrategy(this, rootElement);
    }
    
    /**
     * Returns the list of active proxies in this provider.  The returned
     * list is not a copy and if a sub-class modifies this list, it will
     * modify the current list of active proxies.  This allows the 
     * sub-classes to change how the active proxies are managed and 
     * retained.  
     */
    protected List<IVMModelProxy> getActiveModelProxies() {
        return fActiveModelProxies;
    }
    
    /**
     * Processes the given event in the given provider, sending model 
     * deltas if necessary.
     */
	public void handleEvent(final Object event) {
    	handleEvent(event, null);
    }
	
    /**
     * {@inheritDoc}
	 * @since 1.1
	 */
    public void handleEvent(final Object event, RequestMonitor rm) {
        if (DEBUG_DELTA && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
        	trace(event, null, null, EventHandlerAction.received);
        }

    	CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), rm);
        final List<IVMModelProxy> activeModelProxies= new ArrayList<IVMModelProxy>(getActiveModelProxies());
    	crm.setDoneCount(activeModelProxies.size());

    	for (final IVMModelProxy proxyStrategy : activeModelProxies) {
    	    // If the event is generated by the model proxy, only process it for the proxy that created it.
    	    if ( event instanceof ModelProxyInstalledEvent &&
    	         !((ModelProxyInstalledEvent)event).getModelProxy().equals(proxyStrategy) ) 
    	    {
    	        crm.done();
    	        continue;
    	    }

    	    // Process the event only if there are potential delta flags that may be generated.
    	    // Also, process the event if it is a result of the user modifying something
    	    // so that the cache is properly updated. 
            if (proxyStrategy.isDeltaEvent(event) || event instanceof UserEditEvent) {
                if (!fProxyEventQueues.containsKey(proxyStrategy)) {
                    fProxyEventQueues.put(proxyStrategy, new ModelProxyEventQueue());
                }
                // If the event queue is empty, directly handle the new event. Otherwise queue it. 
                final ModelProxyEventQueue queue = fProxyEventQueues.get(proxyStrategy);
                if (queue.fCurrentEvent != null) {
                    assert queue.fCurrentRm != null;
                    // Iterate through the events in the queue and check if 
                    // they can be skipped.  If they can be skipped, then just 
                    // mark their RM as done.  Stop iterating through the queue
                    // if an event that cannot be skipped is encountered.
                    while (!queue.fEventQueue.isEmpty()) {
                        EventInfo eventToSkipInfo = queue.fEventQueue.get(queue.fEventQueue.size() - 1);
                        
                        if (canSkipHandlingEvent(event, eventToSkipInfo.fEvent)) {
                            if (DEBUG_DELTA && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                            	trace(event, eventToSkipInfo.fEvent, proxyStrategy, EventHandlerAction.skipped);
                            }
                            queue.fEventQueue.remove(queue.fEventQueue.size() - 1);
                            eventToSkipInfo.fClientRm.done();
                        } else {
                            break;
                        }
                    }
                    // If the queue is empty check if the current event
                    // being processed can be skipped.  If so, cancel its
                    // processing 
                    if (queue.fEventQueue.isEmpty() && canSkipHandlingEvent(event, queue.fCurrentEvent.fEvent)) {
                        if (DEBUG_DELTA && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                        	trace(event, queue.fCurrentEvent.fEvent, proxyStrategy, EventHandlerAction.canceled);
                        }
                        queue.fCurrentRm.cancel();
                    }

                    if (DEBUG_DELTA && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                    	trace(event, null, proxyStrategy, EventHandlerAction.queued);
                    }
                    queue.fEventQueue.add(new EventInfo(event, crm));
                } else {
                    doHandleEvent(queue, proxyStrategy, new EventInfo(event, crm));
                }
            } else {
            	crm.done();
            }
        }

        // Discard the event queues of proxies that have been removed
        List<IVMModelProxy> activeProxies = getActiveModelProxies();
        for (Iterator<IVMModelProxy> itr = fProxyEventQueues.keySet().iterator(); itr.hasNext();) {
            if (!activeProxies.contains(itr.next())) {
                itr.remove();
            }
        }
    }

    private void doHandleEvent(final ModelProxyEventQueue queue, final IVMModelProxy proxyStrategy, final EventInfo eventInfo) {
        // Do handle event is a sort of a recursive asynchronous method.  It 
        // calls the asynchronous handleEvent() to process the event from the 
        // eventInfo argument.  When handleEvent() completes, this method 
        // (doHandleEvent) checks whether there is any more events in the queue
        // that should be handled.  If there are, doHandleEvent calls itself
        // to process the next event in the queue.
        assert queue.fCurrentEvent == null && queue.fCurrentRm == null;
        
        queue.fCurrentEvent = eventInfo;
        queue.fCurrentRm = new RequestMonitor(getExecutor(), eventInfo.fClientRm) {
            @Override
            protected void handleCompleted() {
                eventInfo.fClientRm.done();
                queue.fCurrentEvent = null;
                queue.fCurrentRm = null;
                if (!queue.fEventQueue.isEmpty() && !fDisposed) {
                    EventInfo nextEventInfo = queue.fEventQueue.remove(0);
                    doHandleEvent(queue, proxyStrategy, nextEventInfo);
                } 
            }
        };
        handleEvent(proxyStrategy, eventInfo.fEvent, queue.fCurrentRm);
    }

    /**
     * Handles the given event for the given proxy strategy.  
     * <p>
     * This method is called by the base {@link #handleEvent(Object)} 
     * implementation to handle the given event using the given model proxy.
     * The default implementation of this method checks whether the given
     * proxy is active and if the proxy is active, it is called to generate the 
     * delta which is then sent to the viewer. 
     * </p>
     * @param proxyStrategy Model proxy strategy to use to process this event.
     * @param event Event to process.
     * @param rm Request monitor to call when processing the event is 
     * completed.
     */
    protected void handleEvent(final IVMModelProxy proxyStrategy, final Object event, final RequestMonitor rm) {   
        if (!proxyStrategy.isDisposed()) {
            if (DEBUG_DELTA && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
            	trace(event, null, proxyStrategy, EventHandlerAction.processing);
            }
            proxyStrategy.createDelta(
                event, 
                new DataRequestMonitor<IModelDelta>(getExecutor(), rm) {
                    @Override
                    public void handleSuccess() {
                        proxyStrategy.fireModelChanged(getData());
                        if (DEBUG_DELTA && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                        	trace(event, null, proxyStrategy, EventHandlerAction.firedDeltaFor);
                        }
                        rm.done();
                    }
                    @Override public String toString() {
                        return "Result of a delta for event: '" + event.toString() + "' in VMP: '" + AbstractVMProvider.this + "'" + "\n" + getData().toString();  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }   
                });
        } else {
            rm.done();
        }
    }

    /**
     * Determines whether processing of a given event can be skipped.  This 
     * method is called when there are multiple events waiting to be processed
     * by the provider.  As new events are received from the model, they are
     * compared with the events in the queue using this method, events at the 
     * end of the queue are tested for removal.  If this method returns that a 
     * given event can be skipped in favor of the new event, the skipped event
     * is removed from the queue.  This process is repeated with the new event
     * until an event which cannot be stopped is found or the queue goes empty.
     * <p>
     * This method may be overriden by specific view model provider 
     * implementations extending this abstract class. 
     * </p>
     * @param newEvent New event that was received from the model.
     * @param eventToSkip Event which is currently at the end of the queue.  
     * @return True if the event at the end of the queue can be skipped in 
     * favor of the new event.
     */
    protected boolean canSkipHandlingEvent(Object newEvent, Object eventToSkip) {
        return false;
    }
    
    /** @since 1.1 */
    public boolean shouldWaitHandleEventToComplete() {
        return false;
    }
    
    public IRootVMNode getRootVMNode() {
        return fRootNode;
    }

    public IVMNode[] getAllVMNodes() {
        if (fNodesListCache != null) {
            return fNodesListCache;
        }
        List<IVMNode> list = new ArrayList<IVMNode>();
        for (IVMNode node : fChildNodesMap.keySet()) {
            if (node != null) {
                list.add(node);
            }
        }
        fNodesListCache = list.toArray(new IVMNode[list.size()]);; 
        return fNodesListCache; 
    }
        
    public IVMNode[] getChildVMNodes(IVMNode node) {
        IVMNode[] retVal = fChildNodesMap.get(node);
        if (retVal != null) {
            return retVal;
        }
        return EMPTY_NODES_ARRAY;
    }    

    /**
     * Configures the given array of nodes as children of the given parent node.
     * Sub-classes should call this method to define the hierarchy of nodes.
     */
    protected void addChildNodes(IVMNode parentNode, IVMNode[] childNodes) {
        // Add to the child nodes array.
        IVMNode[] existingChildNodes = fChildNodesMap.get(parentNode);
        if (existingChildNodes == null) {
            fChildNodesMap.put(parentNode, childNodes);
        } else {
            IVMNode[] newNodes = new IVMNode[existingChildNodes.length + childNodes.length];
            System.arraycopy(existingChildNodes, 0, newNodes, 0, existingChildNodes.length);
            System.arraycopy(childNodes, 0, newNodes, existingChildNodes.length, childNodes.length);
            fChildNodesMap.put(parentNode, newNodes);
        }
        
        // Make sure that each new expression node has an entry of its own.
        for (IVMNode childNode : childNodes) {
            addNode(childNode);
        }
        
        fNodesListCache = null;
    }
    
    /**
     * Adds the given node to configured nodes, without creating any 
     * parent-child relationship for it.  It is useful for providers which do have 
     * a strict tree hierarchy of ndoes.
     */
    protected void addNode(IVMNode node) {
        if (!fChildNodesMap.containsKey(node)) {
            fChildNodesMap.put(node, EMPTY_NODES_ARRAY);
        }
    }

    /**
     * Clears all configured nodes, including the root node.  This allows a 
     * subclass to reset and reconfigure its nodes.
     */
    protected void clearNodes() {
        clearNodes(true);
        for (IVMNode node : fChildNodesMap.keySet()) {
            node.dispose();
        }
        fChildNodesMap.clear();
        fRootNode = null;
    }

    /**
     * Clears all configured nodes.  This allows a subclass to reset and 
     * reconfigure its nodes.
     * 
     * @param clearRootNode Flag indicating whether to also clear the root node.
     * @since 2.1
     */
    protected void clearNodes(boolean clearRootNode) {
        for (IVMNode node : fChildNodesMap.keySet()) {
            if ( !clearRootNode || !node.equals(getRootVMNode()) ) { 
                node.dispose();
            }
        }
        fChildNodesMap.clear();
        if (clearRootNode) {
            fRootNode = null;
        } else {
            fChildNodesMap.put(getRootVMNode(), EMPTY_NODES_ARRAY);
        }
    }
    
    /**
     * Sets the root node for this provider.  
     */
    protected void setRootNode(IRootVMNode rootNode) {
        fRootNode = rootNode;
    }
    
    /** Called to dispose the provider. */ 
    public void dispose() {
        clearNodes();
        fRootNode = null;
        fDisposed = true;
    }
    
    public void update(final IHasChildrenUpdate[] updates) {
        fContentStrategy.update(updates);
    }
    
    public void update(final IChildrenCountUpdate[] updates) {
        fContentStrategy.update(updates);
    }
    
    public void update(final IChildrenUpdate[] updates) {
        fContentStrategy.update(updates);
    }

	/**
	 * Calls the given view model node to perform the given updates. This method
	 * is called by view model provider and its helper classes instead of
	 * calling the IVMNode method directly, in order to allow additional
	 * processing of the update. For example the AbstractCachingVMProvider
	 * overrides this method to optionally return the results for an update from
	 * a cache.
	 * 
	 * [node] represents the type of the child element, not of the parent. In
	 * other words, the update requests are asking if one or more model elements
	 * of a particular type (thread, e.g.) have children. But [node] does not
	 * represent that type. It represents the type of the potential children
	 * (frame, e.g.)
	 */
    public void updateNode(final IVMNode node, IHasChildrenUpdate[] updates) {
        IHasChildrenUpdate[] updateProxies = new IHasChildrenUpdate[updates.length];
        for (int i = 0; i < updates.length; i++) {
            final IHasChildrenUpdate update = updates[i];
            if (DEBUG_CONTENT_PROVIDER && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                DsfUIPlugin.debug("updateNodeHasChildren(node = " + node + ", update = " + update + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            updateProxies[i] = new VMHasChildrenUpdate(
                update,
                new ViewerDataRequestMonitor<Boolean>(getExecutor(), update) {
                    @Override
                    protected void handleSuccess() {
                        update.setHasChilren(getData());
                        update.done();
                    }
                    
                    @Override
                    protected void handleErrorOrWarning() {
                        if (getStatus().getCode() == IDsfStatusConstants.NOT_SUPPORTED) {
                            if (DEBUG_CONTENT_PROVIDER && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                                DsfUIPlugin.debug("not-supported:updateNodeHasChildren(node = " + node + ", update = " + update + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            }
                            updateNode(
                                node, 
                                new VMChildrenUpdate(
                                    update, -1, -1, 
                                    new ViewerDataRequestMonitor<List<Object>>(getExecutor(), update) {
                                        @Override
                                        protected void handleSuccess() {
                                            update.setHasChilren( !getData().isEmpty() );
                                            update.done();
                                        }
                                    })
                                );
                                    
                        } else {
                            update.setStatus(getStatus());
                            update.done();
                        }
                    }
                    
                });
        }
        node.update(updateProxies);
    }

    /**
     * Calls the given view model node to perform the given updates.  This 
     * method is called by view model provider and it's helper classes instead
     * of calling the IVMNode method directly, in order to allow additional
     * processing of the udpate.  For example the AbstractCachingVMProvider 
     * overrides this method to optionally return the results for an update from
     * a cache. 
     */
    public void updateNode(final IVMNode node, final IChildrenCountUpdate update) {
        if (DEBUG_CONTENT_PROVIDER && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
            DsfUIPlugin.debug("updateNodeChildCount(node = " + node + ", update = " + update + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        node.update(new IChildrenCountUpdate[] { 
            new VMChildrenCountUpdate(
                update,
                new ViewerDataRequestMonitor<Integer>(getExecutor(), update) {
                    @Override
                    protected void handleSuccess() {
                        update.setChildCount(getData());
                        update.done();
                    }
                    
                    @Override
                    protected void handleErrorOrWarning() {
                        if (getStatus().getCode() == IDsfStatusConstants.NOT_SUPPORTED) {
                            if (DEBUG_CONTENT_PROVIDER && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                                DsfUIPlugin.debug("not-supported:updateNodeChildCount(node = " + node + ", update = " + update + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            }
                            updateNode(
                                node, 
                                new VMChildrenUpdate(
                                    update, -1, -1, 
                                    new ViewerDataRequestMonitor<List<Object>>(getExecutor(), update) {
                                        @Override
                                        protected void handleSuccess() {
                                            update.setChildCount( getData().size() );
                                            update.done();
                                        }
                                    })
                                );
                        } else {
                        	super.handleErrorOrWarning();
                        }
                    }
                    
                })
        });
    }

    /**
     * Calls the given view model node to perform the given updates.  This 
     * method is called by view model provider and it's helper classes instead
     * of calling the IVMNode method directly, in order to allow additional
     * processing of the udpate.  For example the AbstractCachingVMProvider 
     * overrides this method to optionally return the results for an update from
     * a cache. 
     */
    public void updateNode(IVMNode node, IChildrenUpdate update) {
        if (DEBUG_CONTENT_PROVIDER && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
            DsfUIPlugin.debug("updateNodeChildren(node = " + node + ", update = " + update + ")");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
        }
        node.update(new IChildrenUpdate[] { update });
    }

    
    /**
     * Returns whether this provider has been disposed.
     */
    protected boolean isDisposed() {
        return fDisposed;
    }

    /**
     * The abstract provider uses a the display-thread executor so that the 
     * provider will operate on the same thread as the viewer.  This way no 
     * synchronization is necessary when the provider is called by the viewer.
     * Also, the display thread is likely to be shut down long after any of the 
     * view models are disposed, so the users of this abstract provider do not 
     * need to worry about the executor throwing the {@link RejectedExecutionException}
     * exception. 
     */
    public Executor getExecutor() { 
        return fExecutor; 
    }
    
    public IModelProxy createModelProxy(Object element, IPresentationContext context) {
        
        // Iterate through the current active proxies to try to find a proxy with the same
        // element and re-use it if found. Only disposed proxies can be re-used because
        // multiple viewers cannot use the same proxy.  Also at this time purge other proxies 
        // that are no longer installed.
        IVMModelProxy proxy = null;
        for (Iterator<IVMModelProxy> itr = getActiveModelProxies().iterator(); itr.hasNext();) {
            IVMModelProxy next = itr.next();
            if (next != null) {
            	if (next.getRootElement().equals(element) && next.isDisposed()) {
            		proxy = next;
            	} else if (next.isDisposed()) {
            		itr.remove();
            	}
            }
        }
        
        if (proxy == null) {
            proxy = createModelProxyStrategy(element);
            getActiveModelProxies().add(proxy);
        } else if (proxy.isDisposed()) {
            // DSF is capable of re-using old proxies which were previously 
            // disposed.  However, the viewer which installs a proxy using
            // a background job to install the proxy calls 
            // IModelProxy.isDisposed(), to check whether the proxy was disposed
            // before it could be installed.  We need to clear the disposed flag
            // of the re-used proxy here, otherwise the proxy will never get used.
            // Calling init here will cause the init() method to be called twice
            // so the IVMModelProxy needs to be prepared for that.
            // See bug 241024.
            proxy.init(context);
        }
        return proxy;
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
     * Note: since the IColumnEditorFactory interface is synchronous, and since
     * column info is fairly static, this method is thread-safe, and it will
     * not be called on the executor thread.
     * 
     * @see IColumnPresentationFactory#createColumnPresentation(IPresentationContext, Object)
     */
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
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
     * Note: since the IColumnEditorFactory interface is synchronous, and since
     * column info is fairly static, this method is thread-safe, and it will
     * not be called on the executor thread.
     * 
     * @see IColumnEditorFactory#getColumnEditorId(IPresentationContext, Object)
     */
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return null;
    }

    /**
     * Calculates the proxy input object to be used for the given input in the given
     * viewer.  By default no proxy object is used an the given element is used
     * as the input into the view. 
     * <p>
     * Sub classes can override this method for view-specific behavior.
     * 
     * @see IViewerInputProvider
     */
    public void update(IViewerInputUpdate update) {
        update.setInputElement(update.getElement());
        update.done();
    }
    
    /**
     * Used for tracing event handling
     */
    private enum EventHandlerAction {
    	received,
    	queued,
    	processing,
    	firedDeltaFor,
    	skipped,
    	canceled
    }

	/**
	 * Trace that we've reached a particular phase of the handling of an event
	 * for a particular proxy.
	 * 
	 * @param event
	 *            the event being handled
	 * @param skippedOrCanceledEvent
	 *            for a 'skip' or 'cancel' action, this is the event that is
	 *            being dismissed. Otherwise null
	 * @param proxy
	 *            the target proxy; n/a (null) for a 'received' action.
	 * @param action
	 *            what phased of the event handling has beeb reached
	 */
    private void trace(Object event, Object skippedOrCanceledEvent, IVMModelProxy proxy, EventHandlerAction action) {
    	assert DEBUG_DELTA;
        StringBuilder str = new StringBuilder();
        str.append(DsfPlugin.getDebugTime());
        str.append(' ');
        if (action == EventHandlerAction.skipped || action == EventHandlerAction.canceled) {
	        str.append(LoggingUtils.toString(this) + " " + action.toString() + " event " + LoggingUtils.toString(skippedOrCanceledEvent) + " because of event " + LoggingUtils.toString(event)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$        	
        }
        else {
	        str.append(LoggingUtils.toString(this) + " " + action.toString() + " event " + LoggingUtils.toString(event)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        if (action != EventHandlerAction.received) {
        	str.append(" for proxy " + LoggingUtils.toString(proxy) + ", whose root is " + LoggingUtils.toString(proxy.getRootElement())); //$NON-NLS-1$ //$NON-NLS-2$
        }
        DsfUIPlugin.debug(str.toString());
    }
}
