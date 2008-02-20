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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.ImmediateExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
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
    
    private Sequence fInitializationSequence = null;
    private boolean fInitialized = false;
    private boolean fShutDown = false;
    
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

    @ConfinedToDsfExecutor("getSession().getExecutor()")
    public void initializeServices(String program, int requestPort, int eventPort, final RequestMonitor rm)
    {
        fTracker = new DsfServicesTracker(PDAPlugin.getBundleContext(), fSession.getId());
        fSession.addServiceEventListener(PDALaunch.this, null);
        
        synchronized(this) {
            fInitializationSequence = new PDAServicesInitSequence(
                getSession(), this, program, requestPort, eventPort, 
                new RequestMonitor(ImmediateExecutor.getInstance(), rm) {
                    @Override
                    protected void handleCompleted() {
                        boolean doShutdown = false;
                        synchronized (this)
                        { 
                            fInitialized = true;
                            fInitializationSequence = null;
                            if (fShutDown) {
                                doShutdown = true;
                            }
                        }
                        if (doShutdown) {
                            doShutdown(rm);
                        } else {
                            if (getStatus().getSeverity() == IStatus.ERROR) {
                                rm.setStatus(getStatus());
                            }
                            rm.done();
                        }
                        fireChanged();
                    }
                });
        }
        getSession().getExecutor().execute(fInitializationSequence);
    }

    @DsfServiceEventHandler 
    public void eventDispatched(PDATerminatedEvent event) {
        shutdownServices(new RequestMonitor(ImmediateExecutor.getInstance(), null));
    }

    public synchronized boolean isInitialized() {
        return fInitialized;
    }
    
    public synchronized boolean isShutDown() {
        return fShutDown;
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
    public void shutdownServices(final RequestMonitor rm) {
        boolean doShutdown = false;
        synchronized (this) {
            if (!fInitialized && fInitializationSequence != null) {
                fInitializationSequence.cancel(false);
            } else {
                doShutdown = !fShutDown && fInitialized;
            }
            fShutDown = true;
        }

        if (doShutdown) {
            doShutdown(rm);
        } else {
            rm.done();
        }
    }
    
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    private void doShutdown(final RequestMonitor rm) {
        fExecutor.execute( new PDAServicesShutdownSequence(
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
            }) );
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
