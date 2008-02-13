/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.launch;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.ImmediateExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.dd.examples.pda.service.command.PDATerminatedEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.ITerminate;

/**
 * A DSF-based debugger has to override the base launch class in order to
 * supply its own content providers for the debug view.
 */
@ThreadSafe
public class PDALaunch extends Launch
    implements ITerminate
{
    private final DefaultDsfExecutor fExecutor;
    private final DsfSession fSession;
    
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    private DsfServicesTracker fTracker;
    
    private AtomicBoolean fInitialized = new AtomicBoolean(false);
    private AtomicBoolean fShutDown = new AtomicBoolean(false);
    
    public PDALaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
        super(launchConfiguration, mode, locator);

        // Create the dispatch queue to be used by debugger control and services 
        // that belong to this launch
        final DefaultDsfExecutor dsfExecutor = new DefaultDsfExecutor(PDAPlugin.ID_PDA_DEBUG_MODEL);
        dsfExecutor.prestartCoreThread();
        fExecutor = dsfExecutor;
        fSession = DsfSession.startSession(fExecutor, PDAPlugin.ID_PDA_DEBUG_MODEL);
    }

    public DsfExecutor getDsfExecutor() { return fExecutor; }
    
    public DsfSession getSession() { return fSession; }

    @ConfinedToDsfExecutor("getExecutor")
    public void initializeControl()
        throws CoreException
    {
        
        Runnable initRunnable = new DsfRunnable() { 
            public void run() {
                fTracker = new DsfServicesTracker(PDAPlugin.getBundleContext(), fSession.getId());
                fSession.addServiceEventListener(PDALaunch.this, null);
                fInitialized.set(true);
                fireChanged();
            }
        };
        
        // Invoke the execution code and block waiting for the result.
        try {
            fExecutor.submit(initRunnable).get();
        } catch (InterruptedException e) {
            throw new CoreException(new Status(
                IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "Error initializing launch", e)); //$NON-NLS-1$
        } catch (ExecutionException e) {
            throw new CoreException(new Status(
                IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "Error initializing launch", e.getCause())); //$NON-NLS-1$
        }
    }

    @DsfServiceEventHandler public void eventDispatched(PDATerminatedEvent event) {
        shutdownSession(new RequestMonitor(ImmediateExecutor.getInstance(), null));
    }

    public boolean isInitialized() {
        return fInitialized.get();
    }
    
    public boolean isShutDown() {
        return fShutDown.get();
    }
    
    @Override
    public boolean canTerminate() {
        return super.canTerminate() && isInitialized() && !isShutDown();
    }

    @Override
    public boolean isTerminated() {
        return super.isTerminated() || isShutDown();
    }


    @Override
    public void terminate() throws DebugException {
        if (isShutDown()) return;
        super.terminate();
    }

    /**
     * Shuts down the services, the session and the executor associated with 
     * this launch.  
     * <p>
     * Note: The argument request monitor to this method should NOT use the
     * executor that belongs to this launch.  By the time the shutdown is 
     * complete, this executor will not be dispatching anymore and the 
     * request monitor will never be invoked.  Instead callers should use
     * the {@link ImmediateExecutor}.
     * </p>
     * @param rm The request monitor invoked when the shutdown is complete.    
     */
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    public void shutdownSession(final RequestMonitor rm) {
        if (fShutDown.getAndSet(true)) {
            rm.done();
            return;
        }
            
        Sequence shutdownSeq = new PDAServicesShutdownSequence(
            getDsfExecutor(), fSession.getId(),
            new RequestMonitor(fSession.getExecutor(), rm) { 
                @Override
                public void handleCompleted() {
                    fSession.removeServiceEventListener(PDALaunch.this);
                    if (!getStatus().isOK()) {
                        PDAPlugin.getDefault().getLog().log(new MultiStatus(
                            PDAPlugin.PLUGIN_ID, -1, new IStatus[]{getStatus()}, "Session shutdown failed", null)); //$NON-NLS-1$
                    }
                    // Last order of business, shutdown the dispatch queue.
                    fTracker.dispose();
                    fTracker = null;
                    DsfSession.endSession(fSession);
                    // endSession takes a full dispatch to distribute the 
                    // session-ended event, finish step only after the dispatch.
                    fExecutor.shutdown();
                    fireTerminate();
                    
                    rm.setStatus(getStatus());
                    rm.done();
                }
            });
        fExecutor.execute(shutdownSeq);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter) {
        // Force adapters to be loaded.  Otherwise the adapter manager may not find
        // the model proxy adapter for DSF-based debug elements.
        Platform.getAdapterManager().loadAdapter(this, adapter.getName());
        return super.getAdapter(adapter);
    }
}
