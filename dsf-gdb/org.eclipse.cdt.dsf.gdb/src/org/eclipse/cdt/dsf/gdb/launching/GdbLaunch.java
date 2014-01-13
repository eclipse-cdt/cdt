/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Marc Khouzam (Ericsson) - Fix NPE for partial launches (Bug 368597)
 *     Marc Khouzam (Ericsson) - Create the gdb process through the process factory (Bug 210366)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Each memory context needs a different MemoryRetrieval (Bug 250323)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.model.IConnectHandler;
import org.eclipse.cdt.debug.core.model.IDebugNewExecutableHandler;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.debug.internal.provisional.model.IMemoryBlockRetrievalManager;
import org.eclipse.cdt.dsf.debug.model.DsfLaunch;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.memory.GdbMemoryBlockRetrievalManager;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.AbstractCLIProcess;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.ITerminate;

/**
 * The only object in the model that implements the traditional interfaces.
 */
@ThreadSafe
public class GdbLaunch extends DsfLaunch
    implements ITerminate, IDisconnect, ITracedLaunch
{
    private DefaultDsfExecutor fExecutor;
    private DsfSession fSession;
    private DsfServicesTracker fTracker;
    private boolean fInitialized = false;
    private boolean fShutDown = false;
    private IMemoryBlockRetrievalManager fMemRetrievalManager;
    private IDsfDebugServicesFactory fServiceFactory;
    
    public GdbLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
        super(launchConfiguration, mode, locator);

        // Create the dispatch queue to be used by debugger control and services 
        // that belong to this launch
        final DefaultDsfExecutor dsfExecutor = new DefaultDsfExecutor(GdbLaunchDelegate.GDB_DEBUG_MODEL_ID);        
        dsfExecutor.prestartCoreThread();
        fExecutor = dsfExecutor;
        fSession = DsfSession.startSession(fExecutor, GdbLaunchDelegate.GDB_DEBUG_MODEL_ID);
    }

    public DsfExecutor getDsfExecutor() { return fExecutor; }
    public IDsfDebugServicesFactory getServiceFactory() { return fServiceFactory; }
    
    public void initialize()
    {
        /*
         * Registering the launch as an adapter.  This ensures that this launch
         * will be associated with all DMContexts from this session.
         * We do this here because we want to have access to the launch even
         * if we run headless, but when we run headless, GdbAdapterFactory is
         * not initialized.
         */
    	fSession.registerModelAdapter(ILaunch.class, this);

        Runnable initRunnable = new DsfRunnable() { 
        	@Override
            public void run() {
                fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSession.getId());
                fSession.addServiceEventListener(GdbLaunch.this, null);
    
                fInitialized = true;
                fireChanged();
            }
        };
        
        // Invoke the execution code and block waiting for the result.
        try {
            fExecutor.submit(initRunnable).get();
        } catch (InterruptedException e) {
            new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Error initializing launch", e); //$NON-NLS-1$
        } catch (ExecutionException e) {
            new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Error initializing launch", e); //$NON-NLS-1$
        }
    }

    public void initializeControl()
    throws CoreException
    {
        // Create a memory retrieval manager and register it with the session
    	// To maintain a mapping of memory contexts to the corresponding memory retrieval in this session
        try {
            fExecutor.submit( new Callable<Object>() {
            	@Override
                public Object call() throws CoreException {
                    fMemRetrievalManager = new GdbMemoryBlockRetrievalManager(GdbLaunchDelegate.GDB_DEBUG_MODEL_ID, getLaunchConfiguration(), fSession);
                    fSession.registerModelAdapter(IMemoryBlockRetrievalManager.class, fMemRetrievalManager);
                    fSession.addServiceEventListener(fMemRetrievalManager, null);
                    return null;
                }
            }).get();
        } catch (InterruptedException e) {
            throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, 0, "Interrupted while waiting for get process callable.", e)); //$NON-NLS-1$
        } catch (ExecutionException e) {
            throw (CoreException)e.getCause();
        } catch (RejectedExecutionException e) {
            throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, 0, "Debugger shut down before launch was completed.", e)); //$NON-NLS-1$
        }
    }

    public DsfSession getSession() { return fSession; }
    
    @ThreadSafeAndProhibitedFromDsfExecutor("getDsfExecutor()")
    public void addCLIProcess(String label) throws CoreException {
        try {
            // Add the CLI process object to the launch.
    		AbstractCLIProcess cliProc =
    			getDsfExecutor().submit( new Callable<AbstractCLIProcess>() {
    				@Override
    				public AbstractCLIProcess call() throws CoreException {
    					IGDBControl gdb = fTracker.getService(IGDBControl.class);
    					if (gdb != null) {
    						return gdb.getCLIProcess();
    					}
    					return null;
    				}
    			}).get();

			// Need to go through DebugPlugin.newProcess so that we can use 
			// the overrideable process factory to allow others to override.
			// First set attribute to specify we want to create the gdb process.
			// Bug 210366
			Map<String, String> attributes = new HashMap<String, String>();
		    attributes.put(IGdbDebugConstants.PROCESS_TYPE_CREATION_ATTR, 
		    		       IGdbDebugConstants.GDB_PROCESS_CREATION_VALUE);
		    DebugPlugin.newProcess(this, cliProc, label, attributes);
        } catch (InterruptedException e) {
            throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, 0, "Interrupted while waiting for get process callable.", e)); //$NON-NLS-1$
        } catch (ExecutionException e) {
            throw (CoreException)e.getCause();
        } catch (RejectedExecutionException e) {
            throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, 0, "Debugger shut down before launch was completed.", e)); //$NON-NLS-1$
        } 
    }
    
    public void setServiceFactory(IDsfDebugServicesFactory factory) {
    	fServiceFactory = factory;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // IServiceEventListener
    @DsfServiceEventHandler public void eventDispatched(ICommandControlShutdownDMEvent event) {
        shutdownSession(new ImmediateRequestMonitor());
    }

    ///////////////////////////////////////////////////////////////////////////
    // ITerminate
    @Override
    public boolean canTerminate() {
        return fInitialized && super.canTerminate();
    }
    // ITerminate
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // IDisconnect
    @Override
    public boolean canDisconnect() {
        return canTerminate();
    }

    @Override
    public boolean isDisconnected() {
        return isTerminated();
    }

    @Override
    public void disconnect() throws DebugException {
    	terminate();
    }
    // IDisconnect
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Terminates the gdb session, shuts down the services, the session and 
     * the executor associated with this launch.  
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
        if (fShutDown) {
            rm.done();
            return;
        }
        fShutDown = true;
            
        final Sequence shutdownSeq = new ShutdownSequence(
            getDsfExecutor(), fSession.getId(),
            new RequestMonitor(fSession.getExecutor(), rm) { 
                @Override
                public void handleCompleted() {
                	if (fMemRetrievalManager != null) {
                		fSession.removeServiceEventListener(fMemRetrievalManager);
                		fMemRetrievalManager.dispose();
                	}

                    fSession.removeServiceEventListener(GdbLaunch.this);
                    if (!isSuccess()) {
                        GdbPlugin.getDefault().getLog().log(new MultiStatus(
                            GdbPlugin.PLUGIN_ID, -1, new IStatus[]{getStatus()}, "Session shutdown failed", null)); //$NON-NLS-1$
                    }
                    // Last order of business, shutdown the dispatch queue.
                    fTracker.dispose();
                    fTracker = null;
                    DsfSession.endSession(fSession);

                    // 'fireTerminate()' removes this launch from the list of 'DebugEvent' 
                    // listeners. The launch may not be terminated at this point: the inferior 
                    // and gdb processes are monitored in separate threads. This will prevent
                    // updating of some of the Debug view actions.
                    // 'DebugEvent.TERMINATE' will be fired when each of the corresponding processes 
                    // exits and handled by 'handleDebugEvents()' method.
                    if (isTerminated())
                        fireTerminate();
                    
                    rm.setStatus(getStatus());
                    rm.done();
                }
            });
        
        final Step[] steps = new Step[] {
            	new Step() {        		
                    @Override
    				public void execute(RequestMonitor rm) {
                    	IGDBControl control = fTracker.getService(IGDBControl.class);
                    	if (control == null) {
                    		rm.done();
                    		return;
                    	}
                    	control.terminate(rm);
                    }
    			},
    			
            	new Step() {        		
                    @Override
    				public void execute(RequestMonitor rm) {
                    	fExecutor.execute(shutdownSeq);
                    }
    			}	
            };

            fExecutor.execute(new Sequence(fExecutor) {

    			@Override
    			public Step[] getSteps() {
    				return steps;
    			}
            });
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
    	// We replace the standard terminate handler by DsfTerminateHandler
    	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=377447.
    	if (adapter.equals(ITerminateHandler.class))
    		return getSession().getModelAdapter(adapter);

    	// Allow to call the connect handler when the launch is selected
    	if (adapter.equals(IConnectHandler.class))
    		return getSession().getModelAdapter(adapter);
    	
    	if (adapter.equals(IDebugNewExecutableHandler.class))
    		return getSession().getModelAdapter(adapter);

        // Must force adapters to be loaded.
        Platform.getAdapterManager().loadAdapter(this, adapter.getName());
        return super.getAdapter(adapter);
    }
    
    @Override
	public void launchRemoved(ILaunch launch) {
		if (this.equals(launch)) {
    		fExecutor.shutdown();
    		fExecutor = null;
    	}
    	super.launchRemoved(launch);
    }
}
