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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IChildrenRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentationFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IContainerRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxyFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;

/** 
 * Base implementation for DSF-based view model adapters.
 */
@ThreadSafe
@SuppressWarnings("restriction")
abstract public class AbstractVMAdapter
    implements IAsynchronousLabelAdapter,
               IAsynchronousContentAdapter,
               IModelProxyFactoryAdapter,
               IColumnPresentationFactoryAdapter
{
    private final DsfSession fSession;

    @ConfinedToDsfExecutor("getSession().getExecutor()")
    private final Map<IPresentationContext, VMProvider> fViewModelProviders = 
        Collections.synchronizedMap( new HashMap<IPresentationContext, VMProvider>() ); 

    /**
     * Constructor for the View Model session.  It is tempting to have the 
     * adapter register itself here with the session as the model adapter, but
     * that would mean that the adapter might get accessed on another thread
     * even before the deriving class is fully constructed.  So it it better
     * to have the owner of this object register it with the session.
     * @param session
     */
    public AbstractVMAdapter(DsfSession session) {
        fSession = session;
    }    

    @ThreadSafe    
    abstract protected VMProvider createViewModelProvider(IPresentationContext context);
            
    protected DsfSession getSession() { return fSession; }
    
    @ThreadSafe
    private VMProvider getViewModelProvider(IPresentationContext context) {
        assert DsfSession.isSessionActive(getSession().getId());
        
        synchronized(fViewModelProviders) {
            VMProvider provider = fViewModelProviders.get(context);
            if (provider == null) {
                provider = createViewModelProvider(context);
                if (provider != null) {
                    fViewModelProviders.put(context, provider);
                }
            }
            return provider;
        }
    }

    @ConfinedToDsfExecutor("getSession().getExecutor()")
    public void install(IPresentationContext context) {
    }

    @ConfinedToDsfExecutor("getSession().getExecutor()")
    public void dispose() {
        assert getSession().getExecutor().isInExecutorThread();
        
        for (VMProvider provider : fViewModelProviders.values()) {
            provider.dispose();
        }
        fViewModelProviders.clear();
    }
    
    public void retrieveLabel(final Object object, final IPresentationContext context, final ILabelRequestMonitor result) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() { 
                public void run() {
                    if (result.isCanceled()) return;
                    
                    VMProvider provider = getViewModelProvider(context);
                    if (provider == null) {
                        result.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "No model provider for object: " + object.toString(), null)); //$NON-NLS-1$
                        result.done();
                    }
                    provider.retrieveLabel(object, result, context.getColumns());
                }
                @Override
                public String toString() { return "Switch to dispatch thread to execute retrieveLabel()"; } //$NON-NLS-1$
            });
        } catch(RejectedExecutionException e) {
            // This can happen if session is being shut down.
            result.done();
        }
    }

    public void isContainer(final Object element, final IPresentationContext context, final IContainerRequestMonitor result) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    if (result.isCanceled()) return;
                    
                    VMProvider provider = getViewModelProvider(context);
                    if (provider == null) {
                        result.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "No model provider for object: " + element.toString(), null)); //$NON-NLS-1$
                        result.done();
                    }
                    provider.isContainer(element, result);
                }
                public String toString() { return "Switch to dispatch thread to execute isContainer()"; } //$NON-NLS-1$
            });
        } catch(RejectedExecutionException e) {
            // This can happen if session is being shut down.
            result.done();
        }
    }

    public void retrieveChildren(final Object element, final IPresentationContext context, final IChildrenRequestMonitor result) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() { 
                public void run() {
                    if (result.isCanceled()) return;
                    
                    VMProvider provider = getViewModelProvider(context);
                    if (provider == null) {
                        result.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "No model provider for object: " + element.toString(), null)); //$NON-NLS-1$
                        result.done();
                    }
                    provider.retrieveChildren(element, result);
                }
                public String toString() { return "Switch to dispatch thread to execute retrieveChildren()"; } //$NON-NLS-1$
            });
        } catch(RejectedExecutionException e) {
            // This can happen if session is being shut down.
            result.done();
        }
    }

    public IModelProxy createModelProxy(Object element, IPresentationContext context) {
        /*
         * Model proxy is the object that correlates events from the data model 
         * into view model deltas that the view can process.  We only need to 
         * create a proxy for the root element of the tree.
         */
        VMProvider provider = getViewModelProvider(context);
        if (provider != null &&
            provider.getRootLayoutNode() != null && 
            element.equals(provider.getRootLayoutNode().getRootVMC().getInputObject()))
        {
            return provider.getModelProxy();
        }
        return null;
    }
    
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        VMProvider provider = getViewModelProvider(context);
        if (provider != null) {
            return provider.getColumnPresentationId(element);
        }
        return null;
    }
    
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        VMProvider provider = getViewModelProvider(context);
        if (provider != null) {
            return provider.createColumnPresentation(element);
        }
        return null;
    }
}
