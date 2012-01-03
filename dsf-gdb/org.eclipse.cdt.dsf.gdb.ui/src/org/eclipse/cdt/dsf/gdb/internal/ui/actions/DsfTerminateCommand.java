/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
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
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
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
    @Override
    public void canExecute(final IEnabledStateRequest request) {
        if (request.getElements().length != 1 || 
            !(request.getElements()[0] instanceof IDMVMContext) ) 
        {
            request.setEnabled(false);
            request.done();
            return;
        }

        IDMVMContext vmc = (IDMVMContext)request.getElements()[0];
        
        // First check if there is an ancestor process to terminate.  This is the smallest entity we can terminate
        final IProcessDMContext processDmc = DMContexts.getAncestorOfType(vmc.getDMContext(), IProcessDMContext.class);
        if (processDmc == null) {
            request.setEnabled(false);
            request.done();
            return;
        }
        
        try {
            fExecutor.execute(
                new DsfRunnable() { 
                    @Override
                    public void run() {
                        // Get the processes service and the exec context.
                    	IProcesses procService = fTracker.getService(IProcesses.class);
                        if (procService == null) {
                            // Service already invalid.
                            request.setEnabled(false);
                            request.done();
                        } else {
                        	procService.canTerminate(processDmc, new ImmediateDataRequestMonitor<Boolean>() {
                        		@Override
                        		protected void handleCompleted() {
                        			request.setEnabled(isSuccess() && getData());
                        			request.done();
                        		}
                        	});
                        }
                    }
                });
        } catch (RejectedExecutionException e) {
            request.setEnabled(false);
            request.done();
        }
    }

    @Override
    public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length != 1 || 
        	!(request.getElements()[0] instanceof IDMVMContext)) {
        	request.done();
        	return false;
        }

        IDMVMContext vmc = (IDMVMContext)request.getElements()[0];

        // First check if there is an ancestor process to terminate.  This is the smallest entity we can terminate
        final IProcessDMContext processDmc = DMContexts.getAncestorOfType(vmc.getDMContext(), IProcessDMContext.class);
        if (processDmc == null) {
        	request.done();
        	return false;
        }

        try {
            fExecutor.execute(new DsfRunnable() { 
                @Override
                public void run() {
                	IProcesses procService = fTracker.getService(IProcesses.class);
                    if (procService != null) {
                    	procService.terminate(processDmc, new ImmediateRequestMonitor() {
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
