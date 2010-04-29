/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfCommandRunnable;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.IRestartHandler;
import org.eclipse.debug.core.model.IProcess;

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

    public void canExecute(final IEnabledStateRequest request) {
        if (request.getElements().length != 1) {
            request.setEnabled(false);
            request.done();
            return;
        }
    	
        fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) {
            @Override public void doExecute() {
    			IGDBControl gdbControl = fTracker.getService(IGDBControl.class);
				if (gdbControl != null) {
					request.setEnabled(gdbControl.canRestart());
				} else {
                    request.setEnabled(false);
				}			
				request.done();
            }
        });        
    }
    
    private class UpdateLaunchJob extends Job {
    	private final AtomicReference<IPath> fExecPathRef;
    	
    	UpdateLaunchJob(IPath path) {
			super(""); //$NON-NLS-1$
			setSystem(true);
			fExecPathRef = new AtomicReference<IPath>(path);
    	}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
	        // Now that we restarted the inferior, we must add it to our launch
	        // we must do this here because we cannot do it in the executor, or else
	        // it deadlocks
	        // We must first remove the old inferior from our launch (since it uses
	        // the same name and we use that name to find the old one)
	        //
	        // Remove
	        String inferiorLabel = fExecPathRef.get().lastSegment();

	        IProcess[] launchProcesses = fLaunch.getProcesses();
	        for (IProcess p : launchProcesses) {
	        	if (p.getLabel().equals(inferiorLabel)) {
	            	fLaunch.removeProcess(p);
	            	break;
	        	}
	        }
	        // Add
	        try {
	            fLaunch.addInferiorProcess(inferiorLabel);
	        } catch (CoreException e) {
	        }
	        return Status.OK_STATUS;
		}
    }
    
    public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length != 1) {
            request.done();
            return false;
        }

        fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) {
            @Override public void doExecute() {
    			final IGDBControl gdbControl = fTracker.getService(IGDBControl.class);
    			final IGDBBackend backend = fTracker.getService(IGDBBackend.class);
				if (gdbControl != null && backend != null) {
                    gdbControl.initInferiorInputOutput(new RequestMonitor(fExecutor, null) {
                    	@Override
                    	protected void handleCompleted() {
                    		if (isSuccess()) {
                        		gdbControl.createInferiorProcess();
                        		gdbControl.restart(fLaunch, new RequestMonitor(fExecutor, null));
                        		
                        		// Update the launch outside the executor
            					new UpdateLaunchJob(backend.getProgramPath()).schedule();
                    		}
                    	}
                    });
				}
			}
        });
        return false;
    }
}

