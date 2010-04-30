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

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
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
    	
        fExecutor.submit(new DsfRunnable() {
            public void run() {
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
    	IDebugCommandRequest fRequest;
    	
    	UpdateLaunchJob(IDebugCommandRequest request) {
			super(""); //$NON-NLS-1$
			setSystem(true);
			fRequest = request;
    	}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
	        // Before restarting the inferior, we must add it to our launch
	        // we must do this here because we cannot do it in the executor, or else
	        // it deadlocks
	        // We must first remove the old inferior from our launch so that we
			// can re-use its name
			
	        // Remove
	        String inferiorLabel = null;

	        IProcess[] launchProcesses = fLaunch.getProcesses();
	        for (IProcess p : launchProcesses) {
	        	if ((p instanceof GDBProcess) == false) {
	        		// We have to processes in our launches, GDB and the inferior
	        		// We can tell this is the inferior because it is not GDB.
	        		// If we don't have an inferior at all, we just won't find it.
	        		inferiorLabel = p.getLabel();
	            	fLaunch.removeProcess(p);
	            	break;
	        	}
	        }
	        // Add
	        if (inferiorLabel != null) {
	        	try {
	        		fLaunch.addInferiorProcess(inferiorLabel);
	        	} catch (CoreException e) {
	        	}
	        }
	        
	        // Now that we have added the new inferior to the launch,
	        // which creates its console, we can perform the restart safely.
	        fExecutor.submit(new DsfRunnable() {
	        	public void run() {
	        		final IGDBControl gdbControl = fTracker.getService(IGDBControl.class);
	        		if (gdbControl != null) {
	        			gdbControl.restart(fLaunch, new RequestMonitor(fExecutor, null) {
	        				@Override
	        				protected void handleCompleted() {
	        					fRequest.done();
	        				};
	        			});
	        		} else {
    					fRequest.done();
	        		}
	        	}
	        });
	        
	        return Status.OK_STATUS;
		}
    }
    
    public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length != 1) {
            request.done();
            return false;
        }

        fExecutor.submit(new DsfRunnable() {
        	public void run() {
    			final IGDBControl gdbControl = fTracker.getService(IGDBControl.class);
				if (gdbControl != null) {
                    gdbControl.initInferiorInputOutput(new RequestMonitor(fExecutor, null) {
                    	@Override
                    	protected void handleCompleted() {
                    		if (isSuccess()) {                    			
                        		gdbControl.createInferiorProcess();
                        		
                        		// Update the launch outside the executor.
                        		// Also, we must have created the new inferior first to create
                        		// the new streams.
                        		// Finally, we should only do the actual restart after
                        		// we have updated the launch, to make sure our consoles
                        		// are ready to process any output from the new inferior (bug 223154)
                        		new UpdateLaunchJob(request).schedule();
                    		} else {
                    			request.done();
                    		}
                    	}
                    });
				} else {
					request.done();
				}
			}
        });
        return false;
    }
}

