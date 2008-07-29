/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.ui.actions;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.dd.dsf.debug.ui.actions.DsfCommandRunnable;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

public class GdbDisconnectCommand implements IDisconnectHandler {
    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    public GdbDisconnectCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

    public void canExecute(final IEnabledStateRequest request) {
        if (request.getElements().length != 1) {
            request.setEnabled(false);
            request.done();
            return;
        }

        fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) { 
            @Override public void doExecute() {
                IProcessDMContext procDmc = DMContexts.getAncestorOfType(getContext(), IProcessDMContext.class);

                getProcessService().canDetachDebuggerFromProcess(
                		procDmc,
                		new DataRequestMonitor<Boolean>(fExecutor, null) {
                			@Override
                			protected void handleCompleted() {
                				request.setEnabled(isSuccess() && getData());
                				request.done();
                			}
                		});
            }
        });
	}

	public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length != 1) {
            request.done();
            return false;
        }

    	fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) { 
            @Override public void doExecute() {
                IProcessDMContext procDmc = DMContexts.getAncestorOfType(getContext(), IProcessDMContext.class);
            	getProcessService().detachDebuggerFromProcess(procDmc, new RequestMonitor(fExecutor, null));
            }
        });
		return false;
	}    
}
