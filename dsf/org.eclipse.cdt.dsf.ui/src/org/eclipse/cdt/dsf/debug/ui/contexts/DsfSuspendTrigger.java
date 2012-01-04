/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.contexts;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.contexts.ISuspendTrigger;
import org.eclipse.debug.ui.contexts.ISuspendTriggerListener;

/**
 * DSF implementation of the ISuspendTrigger interface.  The suspend trigger
 * is used by the IDE to trigger activation of the debug perspective when 
 * the debugger suspends.
 *   
 * @see ISuspendTrigger
 * 
 * @since 1.0
 */
@ConfinedToDsfExecutor("fSession.getExecutor()")
public class DsfSuspendTrigger implements ISuspendTrigger {

    private final DsfSession fSession;
    private final ILaunch fLaunch;
    private volatile boolean fDisposed = false;
    private boolean fEventListenerRegisterd = false;
    private final DsfServicesTracker fServicesTracker;

    @ThreadSafe
    private final ListenerList fListeners = new ListenerList();

    @ThreadSafe
    public DsfSuspendTrigger(DsfSession session, ILaunch launch) {
        fSession = session;
        fLaunch = launch;
        fServicesTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), fSession.getId());
        try {
            fSession.getExecutor().execute(new DsfRunnable() {
                @Override
                public void run() {
                    if (!fDisposed) {
                        fSession.addServiceEventListener(DsfSuspendTrigger.this, null);
                        fEventListenerRegisterd = true;
                    }
                }
            });
        } catch(RejectedExecutionException e) {}
    }
    
    @ThreadSafe
    @Override
    public void addSuspendTriggerListener(final ISuspendTriggerListener listener) {
        fListeners.add(listener);

        // Check if an execution context in the model is already suspended.  
        // If so notify the listener.
        getIsLaunchSuspended(new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(), null) {
            @Override
            protected void handleSuccess() {
                if (!fDisposed && getData().booleanValue()) {
                    listener.suspended(fLaunch, null);
                }
            }
            @Override
            protected void handleErrorOrWarning() {
                // Ignore expected race condition and not supported error.
                // Log other errors.
                if (getStatus().getCode() > IDsfStatusConstants.NOT_SUPPORTED) {
                    super.handleErrorOrWarning();
                }
            }
        });
    }

    @ThreadSafe
    @Override
    public void removeSuspendTriggerListener(ISuspendTriggerListener listener) { 
        fListeners.remove(listener);
    }
    
    @ThreadSafe
    public void dispose() {
        try {
            fSession.getExecutor().execute(new DsfRunnable() {
                @Override
                public void run() {
                    if (fEventListenerRegisterd) {
                        fSession.removeServiceEventListener(DsfSuspendTrigger.this);
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            // Session already gone.
        }
            
        fServicesTracker.dispose();
        fDisposed = true;
    }

    @DsfServiceEventHandler 
    public void eventDispatched(IRunControl.ISuspendedDMEvent e) {
        fireSuspended(null);        
    }

    /**
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler 
    public void eventDispatched(DataModelInitializedEvent e) {
        getIsLaunchSuspended(new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(), null) {
            @Override
            protected void handleSuccess() {
                if (!fDisposed && getData().booleanValue()) {
                    fireSuspended(null);
                }
            }
            
            @Override
            protected void handleErrorOrWarning() {
                // Ignore expected race condition and not supported error.
                // Log other errors.
                if (getStatus().getCode() > IDsfStatusConstants.NOT_SUPPORTED) {
                    super.handleErrorOrWarning();
                }
            }
        });
    }   
    
    /**
     * Returns the services tracker used by the suspend trigger.
     * @since 2.1
     */
    protected DsfServicesTracker getServicesTracker() {
        return fServicesTracker;
    }

    /**
     * Returns the launch for this suspend trigger.
     * @since 2.1
     */
    @ThreadSafe
    protected ILaunch getLaunch() {
        return fLaunch;
    }

    /**
     * Returns the DSF session for this suspend trigger.
     * @since 2.1
     */
    @ThreadSafe
    protected DsfSession getSession() {
        return fSession;
    }
    
    /**
     * Notifies the listeners that a suspend event was received.
     * 
     * @param context
     * 
     * @since 2.1
     */
    @ThreadSafe
    protected void fireSuspended(final Object context) {
        final Object[] listeners = fListeners.getListeners();
        if (listeners.length != 0) {
            new Job("DSF Suspend Trigger Notify") { //$NON-NLS-1$
                {
                    setSystem(true);
                }
                
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    final MultiStatus status = new MultiStatus(DsfUIPlugin.PLUGIN_ID, 0, "DSF Suspend Trigger Notify Job Status", null); //$NON-NLS-1$
                    for (int i = 0; i < listeners.length; i++) {
                        final ISuspendTriggerListener listener = (ISuspendTriggerListener) listeners[i];
                        SafeRunner.run(new ISafeRunnable() {
                            @Override
                           public void run() throws Exception {
                                listener.suspended(fLaunch, context);
                            }
                        
                            @Override
                            public void handleException(Throwable exception) {
                                status.add(new Status(
                                    IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, "Exception while calling suspend trigger listeners", exception)); //$NON-NLS-1$
                            }
                        
                        });             
                    }        
                    return status;
                }
            }.schedule();
        }
    }

    /**
     * Retrieves the top-level containers for this launch.  This method should 
     * be overriden by specific debugger integrations. 
     * @param rm
     * 
     * @since 2.1
     */
    @ThreadSafe
    protected void getLaunchTopContainers(DataRequestMonitor<IContainerDMContext[]> rm) {
        rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "Not implemented.", null)); //$NON-NLS-1$
        rm.done();
    }

    /**
     * Checks if the given launch is currently suspended.  
     * 
     * @param rm Request monitor.
     * 
     * @since 2.1
     */
    @ThreadSafe
    private void getIsLaunchSuspended(final DataRequestMonitor<Boolean> rm) {
        getLaunchTopContainers(new DataRequestMonitor<IContainerDMContext[]>(fSession.getExecutor(), rm) {
            @Override
            protected void handleSuccess() {
                final CountingRequestMonitor crm = new CountingRequestMonitor(fSession.getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        if (rm.getData() == null) {
                            rm.setData(Boolean.FALSE);
                        }
                        rm.done();
                    };
                };
                int count = 0;
                for (final IContainerDMContext containerCtx : getData()) {
                    getIsContainerSuspended(containerCtx, new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(), crm) {
                        @Override
                        protected void handleSuccess() {
                            if (getData().booleanValue()) {
                                rm.setData(Boolean.TRUE);
                            }
                            crm.done();
                        };
                    });
                    count++;
                }
                crm.setDoneCount(count);
            }
        });
    }
    
    /**
     * Recursively checks if the given container or any of its execution 
     * contexts are suspended.
     * 
     * @param container Container to check.
     * @param rm Request monitor.
     * 
     * @since 2.1
     */
    @ConfinedToDsfExecutor("fSession.getExecutor()")
    private void getIsContainerSuspended(final IContainerDMContext container, final DataRequestMonitor<Boolean> rm) {
        // Check if run control service is still available.
        IRunControl rc = fServicesTracker.getService(IRunControl.class);
        if (rc == null) {
            rm.setData(Boolean.FALSE);
            rm.done();
            return;
        }

        // Check if container is suspended.  If so, stop searching.
        if (rc.isSuspended(container)) {
            rm.setData(Boolean.TRUE);
            rm.done();
            return;
        }
        
        // Retrieve the execution contexts and check if any of them are suspended.
        rc.getExecutionContexts(
            container, 
            new DataRequestMonitor<IExecutionDMContext[]>(fSession.getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    // Check if run control service is still available.
                    IRunControl rc = fServicesTracker.getService(IRunControl.class);
                    if (rc == null) {
                        rm.setData(Boolean.FALSE);
                        rm.done();
                        return;
                    }                    
                        
                    // If any of the execution contexts are suspended, stop searching
                    boolean hasContainers = false;
                    for (IExecutionDMContext execCtx : getData()) {
                        if (rc.isSuspended(execCtx)) {
                            rm.setData(Boolean.TRUE);
                            rm.done();
                            return;
                        }
                        hasContainers = hasContainers || execCtx instanceof IContainerDMContext; 
                    }
                    
                    // If any of the returned contexts were containers, check them recursively.
                    if (hasContainers) {
                        final CountingRequestMonitor crm = new CountingRequestMonitor(fSession.getExecutor(), rm) {
                            @Override
                            protected void handleSuccess() {
                                if (rm.getData() == null) {
                                    rm.setData(Boolean.FALSE);
                                }
                                rm.done();
                            };
                        };
                        int count = 0;
                        for (IExecutionDMContext execCtx : getData()) {
                            if (execCtx instanceof IContainerDMContext) {
                                getIsContainerSuspended(
                                    (IContainerDMContext)execCtx, 
                                    new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(), crm) {
                                        @Override
                                        protected void handleSuccess() {
                                            if (getData().booleanValue()) {
                                                rm.setData(Boolean.TRUE);
                                            }
                                            crm.done();
                                        };
                                    });
                                count++;
                            }
                        }
                        crm.setDoneCount(count);
                    } else {
                        rm.setData(Boolean.FALSE);
                        rm.done();
                    }
                }
            });
    }

}
