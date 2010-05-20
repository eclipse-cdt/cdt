/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.IExpressionVMNode;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMModelProxy;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointOrganizer;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointUIConstants;
import org.eclipse.debug.internal.ui.breakpoints.provisional.OtherBreakpointCategory;
import org.eclipse.debug.internal.ui.elements.adapters.DefaultBreakpointsViewInput;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IBreakpointOrganizerDelegate;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The expression provider is used to populate the contents of the expressions 
 * view.  The node hierarchy in this view is a little different than in a typical 
 * provider: the expression manager node should be registered as the single child
 * of the root node and no nodes should be registered as children of expression node.
 * Instead the top level expression nodes should be registered with a call to 
 * {@link #setExpressionNodes(IExpressionVMNode[])}.  And each expression node can
 * have its own sub-hierarchy of elements as needed.  However all nodes configured
 * with this provider (with the exception of the root and the expression manager) 
 * should implement {@link IExpressionVMNode}.
 * 
 * @since 2.1
 */ 
public class BreakpointVMProvider extends AbstractVMProvider 
{
    private IPropertyChangeListener fPresentationContextListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            handleEventInExecThread(event);
        }        
    };

    private IBreakpointsListener fBreakpointsListener = new IBreakpointsListener() {
        public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
            handleEventInExecThread(new BreakpointsChangedEvent(BreakpointsChangedEvent.Type.REMOVED, breakpoints));
        }
        
        public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
            handleEventInExecThread(new BreakpointsChangedEvent(BreakpointsChangedEvent.Type.CHANGED, breakpoints));
        }
        
        public void breakpointsAdded(IBreakpoint[] breakpoints) {
            handleEventInExecThread(new BreakpointsChangedEvent(BreakpointsChangedEvent.Type.ADDED, breakpoints));
        }
        
    };
 
    private IDebugContextListener fDebugContextListener = new IDebugContextListener() {
        public void debugContextChanged(final DebugContextEvent event) {
            handleEventInExecThread(event);
        }
    };
    
    private class ContainerBreakpointsCache extends DataCache<List<BreakpointOrganizerVMContext>> {
        
        private BreakpointOrganizerVMNode fOrganizerVMNode;
        private TreePath fParentPath; 
        
        public ContainerBreakpointsCache(BreakpointOrganizerVMNode organizerVMNode, TreePath parentPath) {
            super(getExecutor());
            fOrganizerVMNode = organizerVMNode;
            fParentPath = parentPath;
        }
        
        @Override
        protected void retrieve(final DataRequestMonitor<List<BreakpointOrganizerVMContext>> rm) {
            getNestingCategoryBreakpoints(
                fParentPath, 
                new DataRequestMonitor<IBreakpoint[]>(getExecutor(), rm) {
                    @SuppressWarnings({ "cast", "unchecked" })
                    @Override
                    protected void handleSuccess() {
                        Map<IAdaptable, List<IBreakpoint>> bpsLists = new HashMap<IAdaptable, List<IBreakpoint>>();
                        for (IBreakpoint bp : getData()) {
                            IAdaptable[] bpCategories = fOrganizerVMNode.getOrganizer().getCategories(bp);
                            if (bpCategories == null || bpCategories.length == 0) {
                                bpCategories = OtherBreakpointCategory.getCategories(fOrganizerVMNode.getOrganizer());
                            } 
                            
                            for (IAdaptable category : bpCategories) {
                                List<IBreakpoint> categoryBPs = bpsLists.get(category);
                                if (categoryBPs == null) {
                                    categoryBPs = new ArrayList<IBreakpoint>();
                                    bpsLists.put(category, categoryBPs);
                                }
                                categoryBPs.add(bp);
                            }
                        }
                        
                        // Only show the empty containers for the top-level node.
                        if (fParentPath.getSegmentCount() == 0) {
                            final IAdaptable[] independentCategories = fOrganizerVMNode.getOrganizer().getCategories();
                            if (independentCategories != null) {
                                for (IAdaptable category : independentCategories) {
                                    if (!bpsLists.containsKey(category)) {
                                        bpsLists.put(category, (List<IBreakpoint>)Collections.EMPTY_LIST);
                                    }
                                }
                            }
                        }
                        
                        List<BreakpointOrganizerVMContext> vmcs = new ArrayList<BreakpointOrganizerVMContext>(bpsLists.size());
                        for (Map.Entry<IAdaptable, List<IBreakpoint>> entry : bpsLists.entrySet()) {
                            List<IBreakpoint> bpsList = entry.getValue();  
                            IBreakpoint[] bpsArray =  bpsList.toArray(new IBreakpoint[bpsList.size()]);
                            
                            vmcs.add(createBreakpointOrganizerVMContext(fOrganizerVMNode, entry.getKey(), bpsArray));                                
                        }
                        Comparator<Object> comparator = (Comparator<Object>)getPresentationContext().getProperty(
                        		IBreakpointUIConstants.PROP_BREAKPOINTS_ELEMENT_COMPARATOR);
                        if (comparator != null) {
                            Collections.sort(vmcs, comparator);
                        }
                        
                        rm.setData(vmcs);
                        rm.done();
                    }
                });
        }
    };

    private final Map<TreePath, ContainerBreakpointsCache> fContainerBreakpointsCacheMap = 
        new HashMap<TreePath, ContainerBreakpointsCache>();

    private DataCache<IBreakpoint[]> fFilteredBreakpointsCache = new DataCache<IBreakpoint[]>(getExecutor()) {
        @Override
        protected void retrieve(org.eclipse.cdt.dsf.concurrent.DataRequestMonitor<IBreakpoint[]> rm) {
            calcFileteredBreakpoints(rm);
        }
    };
    
    public BreakpointVMProvider(AbstractVMAdapter adapter, IPresentationContext context) {
        super(adapter, context);
        
        // Create the top level node which provides the anchor starting point.
        // This node is referenced by the BreakpointVMInput element so it 
        // should not change when the view layout is updated.
        setRootNode(new RootDMVMNode(this));
        // Configure the rest of the layout nodes.
        configureLayout();
        
        context.addPropertyChangeListener(fPresentationContextListener);
        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(fBreakpointsListener);
        IWorkbenchWindow window = context.getWindow();
        if (window != null) {
            DebugUITools.getDebugContextManager().getContextService(window).addDebugContextListener(
                fDebugContextListener);
        }
        
    }

    @Override
    protected IVMModelProxy createModelProxyStrategy(Object rootElement) {
        return new BreakpointVMModelProxyStrategy(this, rootElement);
    }
    
    protected IVMNode createBreakpointVMNode() {
        return new BreakpointVMNode(this);
    }
    
    /**
     * Configures the nodes of this provider.  This method may be over-ridden by
     * sub classes to create an alternate configuration in this provider.
     */
    protected void configureLayout() {
        IBreakpointOrganizer[] organizers = (IBreakpointOrganizer[])
            getPresentationContext().getProperty(IBreakpointUIConstants.PROP_BREAKPOINTS_ORGANIZERS);
        
        IVMNode parentNode = getRootVMNode();
        if (organizers != null) {
            for (IBreakpointOrganizer organizer : organizers) {
                IVMNode organizerNode = new BreakpointOrganizerVMNode(this, organizer);
                addChildNodes(parentNode, new IVMNode[] {organizerNode});
                parentNode = organizerNode;
            }
        }
        
        IVMNode bpsNode = createBreakpointVMNode();
        addChildNodes(parentNode, new IVMNode[] {bpsNode});
    }
    

    @Override
    public void dispose() {
        getPresentationContext().removePropertyChangeListener(fPresentationContextListener);
        DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(fBreakpointsListener);
        IWorkbenchWindow window = getPresentationContext().getWindow();
        if (window != null) {
            DebugUITools.getDebugContextManager().getContextService(window).removeDebugContextListener(
                fDebugContextListener);
        }        
        super.dispose();
    }
    
    @Override
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        return null;
    }
    
    @Override
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return null;
    }

    @Override
    public void update(IViewerInputUpdate update) {
        IDMContext activeDMContext = null;
        if (update.getElement() instanceof IDMVMContext) {
            activeDMContext = ((IDMVMContext)update.getElement()).getDMContext();
            activeDMContext = DMContexts.getAncestorOfType(activeDMContext, IBreakpointsTargetDMContext.class);
        }
        if (activeDMContext != null) {
            update.setInputElement(new BreakpointVMInput(getRootVMNode(), activeDMContext));
        } else {
            // If no breakpoints target found in active context, delegate the breakpoint
            // presentation to the default: breakpoint manager.
            update.setInputElement(new DefaultBreakpointsViewInput(update.getPresentationContext()));
        }
        update.done();
    }
    
    public void getNestingCategoryBreakpoints(TreePath path, final DataRequestMonitor<IBreakpoint[]> rm) {
        BreakpointOrganizerVMContext nestingOrganizerVmc = null;
        while (path.getSegmentCount() > 0) {
            if (path.getLastSegment() instanceof BreakpointOrganizerVMContext) {
                nestingOrganizerVmc = (BreakpointOrganizerVMContext)path.getLastSegment();
                break;
            }
            path = path.getParentPath();
        }
        if (nestingOrganizerVmc == null) {
            getFileteredBreakpoints(rm);
        } else {
            final BreakpointOrganizerVMContext _nestingOrganizerVmc = nestingOrganizerVmc;
            getBreakpointOrganizerVMCs(
                (BreakpointOrganizerVMNode)_nestingOrganizerVmc.getVMNode(), path.getParentPath(),
                new DataRequestMonitor<List<BreakpointOrganizerVMContext>>(getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        int newVmcIdx = getData().indexOf(_nestingOrganizerVmc);
                        
                        if (newVmcIdx >= 0) {
                            rm.setData(getData().get(newVmcIdx).getBreakpoints());
                        } else {
                            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Breakpoint category not found", null)); //$NON-NLS-1$
                        }
                        rm.done();
                    }
                });
        }
    }
    
    public void getBreakpointOrganizerVMCs(BreakpointOrganizerVMNode organizerVMNode, TreePath path, 
        DataRequestMonitor<List<BreakpointOrganizerVMContext>> rm) 
    {
        ContainerBreakpointsCache cache = fContainerBreakpointsCacheMap.get(path);
        if (cache == null) {
            cache = new ContainerBreakpointsCache(organizerVMNode, path);
            fContainerBreakpointsCacheMap.put(path, cache);
        }
        
        cache.request(rm);
    }


    protected BreakpointOrganizerVMContext createBreakpointOrganizerVMContext(BreakpointOrganizerVMNode node, IAdaptable category, IBreakpoint[] breakpoints) {
        return new BreakpointOrganizerVMContext(node, category, breakpoints);
    }
    

    public void getFileteredBreakpoints(final DataRequestMonitor<IBreakpoint[]> rm) {
        fFilteredBreakpointsCache.request(rm);
    }
    
    protected void calcFileteredBreakpoints(DataRequestMonitor<IBreakpoint[]> rm) {
        rm.setData(DebugPlugin.getDefault().getBreakpointManager().getBreakpoints());
        rm.done();
    }

    public void getBreakpointsForDebugContext(ISelection debugContext, DataRequestMonitor<IBreakpoint[]> rm) {
        rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
        rm.done();
    }
    
    public void handleEventInExecThread(final Object event) {
        getExecutor().execute(new DsfRunnable() {
            public void run() {
                handleEvent(event);
            }
        });
    }

    @Override
    public void handleEvent(Object event, RequestMonitor rm) {
        if (isPresentationContextEvent(event)) {
            PropertyChangeEvent propertyEvent = (PropertyChangeEvent)event;
            if (IBreakpointUIConstants.PROP_BREAKPOINTS_ORGANIZERS.equals(propertyEvent.getProperty())) 
            {
                clearNodes(false);
                configureLayout();
            } 
        } 
        super.handleEvent(event, rm);
    }

    public static boolean isPresentationContextEvent(Object event) {
        return event instanceof PropertyChangeEvent && ((PropertyChangeEvent)event).getSource() instanceof IPresentationContext;
    }

    public static boolean isBreakpointOrganizerEvent(Object event) {
        return event instanceof PropertyChangeEvent && ((PropertyChangeEvent)event).getSource() instanceof IBreakpointOrganizerDelegate;
    }

    private static final int MODEL_DELTA_CHANGE_FLAGS = IModelDelta.STATE | IModelDelta.CONTENT | IModelDelta.ADDED | IModelDelta.REMOVED | IModelDelta.REPLACED | IModelDelta.INSERTED; 
    
    @Override
    protected void handleEvent(IVMModelProxy proxyStrategy, Object event, RequestMonitor rm) {
        // Before generating a delta, flush the caches.
        int deltaFlags = proxyStrategy.getEventDeltaFlags(event);
        if ((deltaFlags & MODEL_DELTA_CHANGE_FLAGS) != 0) {
            flushCaches();
        }
        
        super.handleEvent(proxyStrategy, event, rm);
    }
    
    private void flushCaches() {
        fFilteredBreakpointsCache.reset();
        for (DataCache<?> cache : fContainerBreakpointsCacheMap.values()) {
            cache.reset();
        }
        fContainerBreakpointsCacheMap.clear();
    }
}
