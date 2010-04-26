/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Nokia - create and use backend service. 
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfCommandRunnable;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;

public class DsfTerminateCommand implements ITerminateHandler {
    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    public DsfTerminateCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

    // Run control may not be avilable after a connection is terminated and shut down.
    public void canExecute(final IEnabledStateRequest request) {
        if (request.getElements().length != 1 || 
            !(request.getElements()[0] instanceof IDMVMContext) ) 
        {
            request.setEnabled(false);
            request.done();
            return;
        }

        // Javac doesn't like the cast to "(AbstractDMVMLayoutNode<?>.DMVMContext)" need to use the 
        // construct below and suppress warnings.
        IDMVMContext vmc = (IDMVMContext)request.getElements()[0];
        final IExecutionDMContext dmc = DMContexts.getAncestorOfType(vmc.getDMContext(), IExecutionDMContext.class);
        if (dmc == null) {
            request.setEnabled(false);
            request.done();
            return;
        }            
        
        try {
            fExecutor.execute(
                new DsfRunnable() { 
                    public void run() {
                        // Get the processes service and the exec context.
                    	IGDBBackend gdbBackend = fTracker.getService(IGDBBackend.class);
                        if (gdbBackend == null || dmc == null) {
                            // Context or service already invalid.
                            request.setEnabled(false);
                            request.done();
                        } else {
                            // Check the terminate.
                            request.setEnabled(gdbBackend.getState() == IMIBackend.State.STARTED);
                            request.done();
                        }
                    }
                });
        } catch (RejectedExecutionException e) {
            request.setEnabled(false);
            request.done();
        }
    }

    public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length != 1) {
            request.done();
            return false;
        }

        try {
            fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) { 
                @Override public void doExecute() {
                    IGDBControl gdbControl = fTracker.getService(IGDBControl.class);
                    if (gdbControl != null) {
                        gdbControl.terminate(new RequestMonitor(ImmediateExecutor.getInstance(), null) {
                            @Override
                            protected void handleCompleted() {
                                request.setStatus(getStatus());
                                request.done();
                            };
                        });
                    } else {
                    	request.done();
                    }
                 }
            });
        } catch (RejectedExecutionException e) {
            request.done();
        }
        return false;
    }
    
}
