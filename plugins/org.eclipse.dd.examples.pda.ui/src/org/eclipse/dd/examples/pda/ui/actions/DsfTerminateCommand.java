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
package org.eclipse.dd.examples.pda.ui.actions;

import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.dd.examples.pda.service.command.PDACommandControl;
import org.eclipse.dd.examples.pda.ui.PDAUIPlugin;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;

public class DsfTerminateCommand implements ITerminateHandler {
    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    public DsfTerminateCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(PDAUIPlugin.getBundleContext(), session.getId());
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
        
        fExecutor.execute(
            new DsfRunnable() { 
                public void run() {
                    // Get the processes service and the exec context.
                    PDACommandControl commandControl = fTracker.getService(PDACommandControl.class);
                    if (commandControl == null || dmc == null) {
                        // Context or service already invalid.
                        request.setEnabled(false);
                        request.done();
                    } else {
                        // Check the terminate.
                        request.setEnabled(!commandControl.isTerminated());
                        request.done();
                    }
                }
            });
    }

    public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length != 1) {
            request.done();
            return false;
        }

        fExecutor.submit(new DsfRunnable() { 
            public void run() {
                PDACommandControl commandControl = fTracker.getService(PDACommandControl.class);
                if (commandControl != null) {
                    commandControl.shutdown(new RequestMonitor(fExecutor, null));
                }
             }
        });
        return false;
    }
    
}
