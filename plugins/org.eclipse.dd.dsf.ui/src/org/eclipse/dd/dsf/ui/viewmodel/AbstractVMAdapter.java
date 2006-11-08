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
               IModelProxyFactoryAdapter
{
    private final DsfSession fSession;

    @ConfinedToDsfExecutor("getSession().getExecutor()")
    private Map<IPresentationContext, VMProvider> fViewModelProviders = 
        new HashMap<IPresentationContext, VMProvider>(); 

    public AbstractVMAdapter(DsfSession session) {
        fSession = session;
        // regieterModelAdapter() is thread safe, so we're OK calling it from here.
        fSession.registerModelAdapter(IAsynchronousLabelAdapter.class, this);
        fSession.registerModelAdapter(IAsynchronousContentAdapter.class, this);
        fSession.registerModelAdapter(IModelProxyFactoryAdapter.class, this);
    }    

    @ConfinedToDsfExecutor("getSession().getExecutor()")    
    abstract protected VMProvider createViewModelProvider(IPresentationContext context);
            
    protected DsfSession getSession() { return fSession; }
    
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    private VMProvider getViewModelProvider(IPresentationContext context) {
        VMProvider provider = fViewModelProviders.get(context);
        if (provider == null) {
            provider = createViewModelProvider(context);
            if (provider != null) {
                fViewModelProviders.put(context, provider);
            }
        }
        return provider;
    }

    @ConfinedToDsfExecutor("getSession().getExecutor()")
    public void install(IPresentationContext context) {
    }

    @ConfinedToDsfExecutor("getSession().getExecutor()")
    public void dispose() {
        assert getSession().getExecutor().isInExecutorThread();
        
        fSession.unregisterModelAdapter(IAsynchronousLabelAdapter.class);
        fSession.unregisterModelAdapter(IAsynchronousContentAdapter.class);
        fSession.unregisterModelAdapter(IModelProxyFactoryAdapter.class);
        
        for (VMProvider provider : fViewModelProviders.values()) {
            provider.sessionDispose();
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
                    provider.retrieveLabel(object, result);
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
        if (provider != null && element.equals(provider.getRootLayoutNode().getRootVMC().getInputObject())) {
            return provider.getModelProxy();
        }
        return null;
    }
}
