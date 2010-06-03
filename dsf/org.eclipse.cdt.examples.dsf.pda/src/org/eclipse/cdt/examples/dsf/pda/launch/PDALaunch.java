/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.launch;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.model.DsfLaunch;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.service.PDATerminatedEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.ITerminate;

/**
 * The PDA launch object. In general, a DSF-based debugger has to override 
 * the base launch class in order to supply its own content providers for the 
 * debug view.  Additionally, the PDA launch is used to monitor the state of the
 * PDA debugger and to shutdown the DSF services and session belonging to the 
 * launch.
 * <p>
 * The PDA launch class mostly contains methods and fields that can be accessed
 * on any thread.  However, some fields and methods used for managing the DSF
 * session need to be synchronized using the DSF executor.
 * </p>
 */
@ThreadSafe
public class PDALaunch extends DsfLaunch
implements ITerminate
{   
    // DSF executor and session.  Both are created and shutdown by the launch. 
    private final DefaultDsfExecutor fExecutor;
    private final DsfSession fSession;

    // Objects used to track the status of the DSF session.
    private boolean fInitialized = false;
    private boolean fShutDown = false;
    
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    private Sequence fInitializationSequence = null;

    /**
     * Launch constructor creates the launch for given parameters.  The
     * constructor also creates a DSF session and an executor, so that 
     * {@link #getSession()} returns a valid value, however no services 
     * are initialized yet. 
     * 
     * @see Launch
     */
    public PDALaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
        super(launchConfiguration, mode, locator);

        // Create the dispatch queue to be used by debugger control and services 
        // that belong to this launch
        final DefaultDsfExecutor dsfExecutor = new DefaultDsfExecutor(PDAPlugin.ID_PDA_DEBUG_MODEL);
        dsfExecutor.prestartCoreThread();
        fExecutor = dsfExecutor;
        fSession = DsfSession.startSession(fExecutor, PDAPlugin.ID_PDA_DEBUG_MODEL);

        // Register the launch as an adapter This ensures that the launch,
        // and debug model ID will be associated with all DMContexts from this
        // session.
        fSession.registerModelAdapter(ILaunch.class, this);
    }

    /**
     * Returns the DSF services session that belongs to this launch.  This 
     * method will always return a DsfSession object, however if the debugger 
     * is shut down, the session will no longer active.
     */
    public DsfSession getSession() { return fSession; }

    /**
     * Initializes the DSF services using the specified parameters.  This 
     * method has to be called on the executor thread in order to avoid 
     * synchronization issues.  
     */
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    public void initializeServices(String program, final RequestMonitor rm)
    {
        // Double-check that we're being called in the correct thread.
        assert fExecutor.isInExecutorThread();

        // Check if shutdownServices() was called already, which would be 
        // highly unusual, but if so we don't need to do anything except set 
        // the initialized flag.
        synchronized(this) {
            if (fShutDown) {
                fInitialized = true;
                return;
            }
        }

        // Register the launch as listener for services events.
        fSession.addServiceEventListener(PDALaunch.this, null);

        // The initialization sequence is stored in a field to allow it to be 
        // canceled if shutdownServices() is called before the sequence 
        // completes.
        fInitializationSequence = new PDAServicesInitSequence(
            getSession(), this, program, 
            new RequestMonitor(ImmediateExecutor.getInstance(), rm) {
                @Override
                protected void handleCompleted() {
                    // Set the initialized flag and check whether the 
                    // shutdown flag is set.  Access the flags in a 
                    // synchronized section as these flags can be accessed
                    // on any thread.
                    boolean doShutdown = false;
                    synchronized (this) { 
                        fInitialized = true;
                        fInitializationSequence = null;
                        if (fShutDown) {
                            doShutdown = true;
                        }
                    }

                    if (doShutdown) {
                        // If shutdownServices() was already called, start the 
                        // shutdown sequence now.
                        doShutdown(rm);
                    } else {
                        // If there was an error in the startup sequence, 
                        // report the error to the client.
                        if (getStatus().getSeverity() == IStatus.ERROR) {
                            rm.setStatus(getStatus());
                        }
                        rm.done();
                    }
                    fireChanged();
                }
            });

        // Finally, execute the sequence. 
        getSession().getExecutor().execute(fInitializationSequence);
    }

    /**
     * Event handler for a debugger terminated event.    
     */
    @DsfServiceEventHandler 
    public void eventDispatched(PDATerminatedEvent event) {
        shutdownServices(new RequestMonitor(ImmediateExecutor.getInstance(), null));
    }

    /**
     * Returns whether the DSF service initialization sequence has completed yet.
     */
    public synchronized boolean isInitialized() {
        return fInitialized;
    }

    /**
     * Returns whether the DSF services have been set to shut down.
     * @return
     */
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
        // Check initialize and shutdown flags to determine if the shutdown
        // sequence can be called yet.
        boolean doShutdown = false;
        synchronized (this) {
            if (!fInitialized && fInitializationSequence != null) {
                // Launch has not yet initialized, try to cancel the 
                // shutdown sequence.
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
            fExecutor, fSession.getId(),
            new RequestMonitor(fSession.getExecutor(), rm) { 
                @Override
                public void handleCompleted() {
                    fSession.removeServiceEventListener(PDALaunch.this);
                    if (!isSuccess()) {
                        PDAPlugin.getDefault().getLog().log(new MultiStatus(
                            PDAPlugin.PLUGIN_ID, -1, new IStatus[]{getStatus()}, "Session shutdown failed", null)); //$NON-NLS-1$
                    }
                    // Last order of business, shutdown the dispatch queue.
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
