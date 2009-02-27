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
package org.eclipse.cdt.dsf.debug.ui.contexts;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
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
    private boolean fDisposed = false;
    private boolean fEventListenerRegisterd = false;

    @ThreadSafe
    private final ListenerList fListeners = new ListenerList();

    @ThreadSafe
    public DsfSuspendTrigger(DsfSession session, ILaunch launch) {
        fSession = session;
        fLaunch = launch;
        try {
            fSession.getExecutor().execute(new DsfRunnable() {
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
    public void addSuspendTriggerListener(ISuspendTriggerListener listener) {
        if (fListeners != null) {
            fListeners.add(listener);
        }
    }

    @ThreadSafe
    public void removeSuspendTriggerListener(ISuspendTriggerListener listener) { 
        if (fListeners != null) {
            fListeners.remove(listener);
        }
    }
    
    public void dispose() {
        if (fEventListenerRegisterd) {
            fSession.removeServiceEventListener(this);
        }
        fDisposed = true;
    }

    @DsfServiceEventHandler 
    public void eventDispatched(IRunControl.ISuspendedDMEvent e) {
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
                            public void run() throws Exception {
                                listener.suspended(fLaunch, null);
                            }
                        
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
    
}
