/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Navid Mehregani (TI) - Bug 289526 - Migrate the Restart feature to the new one, as supported by the platform
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfCommandRunnable;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.IRestartHandler;

public class GdbRestartCommand implements IRestartHandler {
    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    private final GdbLaunch fLaunch;
    
    public GdbRestartCommand(DsfSession session, GdbLaunch launch) {
        fExecutor = session.getExecutor();
        fLaunch = launch;
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

    @Override
    public void canExecute(final IEnabledStateRequest request) {
        if (request.getElements().length != 1) {
            request.setEnabled(false);
            request.done();
            return;
        }

        fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) { 
            @Override public void doExecute() {
                IContainerDMContext containerDmc = DMContexts.getAncestorOfType(getContext(), IContainerDMContext.class);
            	IGDBProcesses procService = fTracker.getService(IGDBProcesses.class);

                if (procService != null) {
                	procService.canRestart(
                			containerDmc,
                			new DataRequestMonitor<Boolean>(fExecutor, null) {
                				@Override
                				protected void handleCompleted() {
                					request.setEnabled(isSuccess() && getData());
                					request.done();
                				}
                			});
                } else {
                	request.setEnabled(false);
					request.done();
       			}
            }
        });
	}
    
    @Override
    public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length != 1) {
            request.done();
            return false;
        }
        
        Object element = request.getElements()[0];
        if (!(element instanceof IDMVMContext)) {
            request.done();
            return false;
        }
        
        final IContainerDMContext containerDmc = DMContexts.getAncestorOfType(((IDMVMContext)element).getDMContext(), 
        																	  IContainerDMContext.class);
        
        fExecutor.submit(new DsfRunnable() {
        	@SuppressWarnings("unchecked")
            @Override
			public void run() {
            	IGDBProcesses procService = fTracker.getService(IGDBProcesses.class);

                if (procService != null) {
                	Map<String, Object> attributes = null;
					try {
						attributes = fLaunch.getLaunchConfiguration().getAttributes();
					} catch (CoreException e) {}
					
                	procService.restart(containerDmc, attributes, 
                						new DataRequestMonitor<IContainerDMContext>(fExecutor, null) {
                							@Override
                							protected void handleCompleted() {
                								request.done();
                							};
                						});
                } else {
                	request.done();
       			}
			}
        });
        return false;
    }
}

