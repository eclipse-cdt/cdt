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
import org.eclipse.dd.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/** 
 * Base implementation for View Model Adapters.  The implementation uses
 * its own single-thread executor for communicating with providers and 
 * layout nodes. 
 */
@ThreadSafe
@SuppressWarnings("restriction")
abstract public class AbstractVMAdapter implements IVMAdapter
{
    private final DsfExecutor fExecutor;
    private boolean fDisposed;

    private final Map<IPresentationContext, IVMProvider> fViewModelProviders = 
        Collections.synchronizedMap( new HashMap<IPresentationContext, IVMProvider>() ); 

    /**
     * Constructor for the View Model session.  It is tempting to have the 
     * adapter register itself here with the session as the model adapter, but
     * that would mean that the adapter might get accessed on another thread
     * even before the deriving class is fully constructed.  So it it better
     * to have the owner of this object register it with the session.
     * @param session
     */
    public AbstractVMAdapter() {
        fExecutor = new DefaultDsfExecutor();
    }    

    /**
     * Returns the executor that will be used to communicate with the providers
     * and the layout nodes.
     * @return
     */
    public DsfExecutor getExecutor() {
        return fExecutor;
    }
    
    @ThreadSafe
    public IVMProvider getVMProvider(IPresentationContext context) {
        synchronized(fViewModelProviders) {
            if (fDisposed) return null;

            IVMProvider provider = fViewModelProviders.get(context);
            if (provider == null) {
                provider = createViewModelProvider(context);
                if (provider != null) {
                    fViewModelProviders.put(context, provider);
                }
            }
            return provider;
        }
    }

    public void dispose() {
        // Execute the shutdown in adapter's dispatch thread.
        getExecutor().execute(new DsfRunnable() {
            public void run() {
                synchronized(fViewModelProviders) {
                    fDisposed = true;
                    for (IVMProvider provider : fViewModelProviders.values()) {
                        provider.dispose();
                    }
                    fViewModelProviders.clear();
                }
                fExecutor.shutdown();
            }
        });
    }
    
    public void update(IHasChildrenUpdate[] updates) {
        handleUpdates(updates);
    }
    
    public void update(IChildrenCountUpdate[] updates) {
        handleUpdates(updates);
    }
    
    public void update(final IChildrenUpdate[] updates) {
        handleUpdates(updates);
    }
    
    private void handleUpdates(final IViewerUpdate[] updates) {
        try {
            getExecutor().execute(new DsfRunnable() {
                public void run() {
                    IPresentationContext context = null;
                    int firstIdx = 0;
                    int curIdx = 0;
                    for (curIdx = 0; curIdx < updates.length; curIdx++) {
                        if (!updates[curIdx].getPresentationContext().equals(context)) {
                            if (context != null) {
                                callProviderWithUpdate(updates, firstIdx, curIdx);
                            }
                            context = updates[curIdx].getPresentationContext();
                            firstIdx = curIdx;
                        }
                    }
                    callProviderWithUpdate(updates, firstIdx, curIdx);
                }
            });
        } catch(RejectedExecutionException e) {
            for (IViewerUpdate update : updates) {
                update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, "VM adapter executor not available", e)); //$NON-NLS-1$
                update.done();
            }
        }
    }
    
    private void callProviderWithUpdate(IViewerUpdate[] updates, int startIdx, int endIdx) {
        final IVMProvider provider = getVMProvider(updates[0].getPresentationContext());
        if (provider == null) {
            for (IViewerUpdate update : updates) {
                update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, 
                                            "No model provider for update " + update, null)); //$NON-NLS-1$
                update.done();
            }
            return;
        }
        if (startIdx == 0 && endIdx == updates.length) {
            if (updates instanceof IHasChildrenUpdate[]) provider.update((IHasChildrenUpdate[])updates);
            else if (updates instanceof IChildrenCountUpdate[]) provider.update((IChildrenCountUpdate[])updates);
            else if (updates instanceof IChildrenUpdate[]) provider.update((IChildrenUpdate[])updates);
        } else {
            if (updates instanceof IHasChildrenUpdate[]) {
                IHasChildrenUpdate[] providerUpdates = new IHasChildrenUpdate[endIdx - startIdx];
                System.arraycopy(updates, startIdx, providerUpdates, 0, endIdx - startIdx);
                provider.update(providerUpdates);
            }
            else if (updates instanceof IChildrenCountUpdate[]) {
                IChildrenCountUpdate[] providerUpdates = new IChildrenCountUpdate[endIdx - startIdx];
                System.arraycopy(updates, startIdx, providerUpdates, 0, endIdx - startIdx);
                provider.update(providerUpdates);
            }
            else if (updates instanceof IChildrenUpdate[]) {
                IChildrenUpdate[] providerUpdates = new IChildrenUpdate[endIdx - startIdx];
                System.arraycopy(updates, startIdx, providerUpdates, 0, endIdx - startIdx);
                provider.update(providerUpdates);
            }
        }
    }
        
    public IModelProxy createModelProxy(Object element, IPresentationContext context) {
        IVMProvider provider = getVMProvider(context);
        if (provider != null) {
            return provider.createModelProxy(element, context);
        }
        return null;
    }
    
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        final IVMProvider provider = getVMProvider(context);
        if (provider != null) {
            return provider.createColumnPresentation(context, element);
        }
        return null;
    }
    
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        final IVMProvider provider = getVMProvider(context);
        if (provider != null) {
            return provider.getColumnPresentationId(context, element);
        }
        return null;
    }


    public void update(IViewerInputUpdate update) {
        final IVMProvider provider = getVMProvider(update.getPresentationContext());
        if (provider != null) {
            provider.update(update);
        }
    }

    /**
     * Creates a new View Model Provider for given presentation context.  Returns null
     * if the presentation context is not supported.
     */
    @ThreadSafe    
    abstract protected IVMProvider createViewModelProvider(IPresentationContext context);
}
