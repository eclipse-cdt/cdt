/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui.actions;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.examples.dsf.pda.service.PDACommandControl;
import org.eclipse.cdt.examples.dsf.pda.service.PDAVirtualMachineDMContext;
import org.eclipse.cdt.examples.dsf.pda.ui.PDAUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;

/**
 * The terminate command is specialized for the PDA debugger.  Currently there
 * is no standard interface for terminating a debug session in DSF, because the 
 * details of initiating and shutting down a debug session vary greatly in 
 * different debuggers.
 */
public class PDATerminateCommand implements ITerminateHandler {
    // The executor and the services tracker, both initialized from a DSF session.
    private final DsfSession fSession;
    private final DsfServicesTracker fTracker;
    
    public PDATerminateCommand(DsfSession session) {
        fSession = session;
        fTracker = new DsfServicesTracker(PDAUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        // DSF services tracker always has to be disposed, because the OSGi services
        // use reference counting.
        fTracker.dispose();
    }

    // Run control may not be available after a connection is terminated and shut down.
    public void canExecute(final IEnabledStateRequest request) {
        // Terminate can only operate on a single element.
        if (request.getElements().length != 1 || 
            !(request.getElements()[0] instanceof IDMVMContext) ) 
        {
            request.setEnabled(false);
            request.done();
            return;
        }

        // Find the PDA program context in the selected element.  If one is not found, 
        // the action should be disabled.
        IDMVMContext vmc = (IDMVMContext)request.getElements()[0];
        final PDAVirtualMachineDMContext pdaProgramCtx = DMContexts.getAncestorOfType(vmc.getDMContext(), PDAVirtualMachineDMContext.class);
        if (pdaProgramCtx == null) {
            request.setEnabled(false);
            request.done();
            return;
        }            
        
        try {
            fSession.getExecutor().execute(
                new DsfRunnable() { 
                    public void run() {
                        // Get the processes service and the exec context.
                        PDACommandControl commandControl = fTracker.getService(PDACommandControl.class);
                        if (commandControl == null || pdaProgramCtx == null) {
                            // Context or service already invalid.
                            request.setEnabled(false);
                            request.done();
                        } else {
                            // Check whether the control is terminated.
                            request.setEnabled(!commandControl.isTerminated());
                            request.done();
                        }
                    }
                });
        } catch (RejectedExecutionException e) {
            // The DSF session for this context is no longer active.  It's possible to check 
            // for this condition before calling fSession.getExecutor().execute(), but 
            // since this method is executing in a different thread than the session control, 
            // there would still be a chance for a race condition leading to this exception. 
            request.setEnabled(false);
            request.done();
        }
    }

    public boolean execute(final IDebugCommandRequest request) {
        // Skip the checks and assume that this method is called only if the action
        // was enabled.

        try {
            fSession.getExecutor().submit(new DsfRunnable() { 
                public void run() {
                    // If the command control service is available, attempt to terminate the program.
                    PDACommandControl commandControl = fTracker.getService(PDACommandControl.class);
                    if (commandControl != null) {
                        
                        commandControl.terminate(
                            new RequestMonitor(ImmediateExecutor.getInstance(), null) {
                                @Override
                                protected void handleCompleted() {
                                    request.setStatus(getStatus());
                                    request.done();
                                }
                            });
                    }
                 }
            });
        } catch (RejectedExecutionException e) {
            request.setStatus(new Status(IStatus.ERROR, PDAUIPlugin.PLUGIN_ID, "PDA debug session is shut down."));
            request.done();
        }
        return false;
    }
    
}
