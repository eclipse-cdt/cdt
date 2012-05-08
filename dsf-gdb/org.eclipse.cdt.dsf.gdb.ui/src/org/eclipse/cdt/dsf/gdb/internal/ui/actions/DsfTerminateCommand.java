/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems and others.
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
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;

public class DsfTerminateCommand implements ITerminateHandler {
	private final DsfSession fSession;
	private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    public DsfTerminateCommand(DsfSession session) {
    	fSession = session;
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

    @Override
    public void canExecute(final IEnabledStateRequest request) {
        if (request.getElements().length != 1 || 
            !(request.getElements()[0] instanceof IDMVMContext || 
              request.getElements()[0] instanceof GdbLaunch)) {
            request.setEnabled(false);
            request.done();
            return;
        }

        if (request.getElements()[0] instanceof GdbLaunch) {
        	canExecute(((GdbLaunch)request.getElements()[0]), request);
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

        canExecute(processDmc, request);
    }

    @Override
    public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length != 1 || 
        	!(request.getElements()[0] instanceof IDMVMContext || 
              request.getElements()[0] instanceof GdbLaunch)) {
        	request.done();
        	return false;
        }

        if (request.getElements()[0] instanceof GdbLaunch) {
        	return execute(((GdbLaunch)request.getElements()[0]), request);
        }

        IDMVMContext vmc = (IDMVMContext)request.getElements()[0];

        // First check if there is an ancestor process to terminate.  This is the smallest entity we can terminate
        final IProcessDMContext processDmc = DMContexts.getAncestorOfType(vmc.getDMContext(), IProcessDMContext.class);
        if (processDmc == null) {
        	request.done();
        	return false;
        }

        return execute(processDmc, request);
    }

    private void canExecute(GdbLaunch launch, IEnabledStateRequest request) {
    	request.setEnabled(launch.canTerminate());
    	request.done();
    }

    private boolean execute(GdbLaunch launch, final IDebugCommandRequest request) {    	
        try {
            fExecutor.execute(new DsfRunnable() { 
                @Override
                public void run() {
                	final IGDBControl commandControl = fTracker.getService(IGDBControl.class);
                    if (commandControl != null) {
                    	commandControl.terminate(new ImmediateRequestMonitor() {
                            @Override
                            protected void handleCompleted() {
                            	if (!isSuccess()) {
                            		request.setStatus(getStatus());
                            		request.done();
                            	}
                            	else {
                            		waitForTermination(request);
                            	}
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

    private void canExecute(final IProcessDMContext processDmc, final IEnabledStateRequest request) {
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

    private boolean execute(final IProcessDMContext processDmc, final IDebugCommandRequest request) {
        try {
            fExecutor.execute(new DsfRunnable() { 
                @Override
                public void run() {
                	IProcesses procService = fTracker.getService(IProcesses.class);
                    if (procService != null) {
                    	procService.terminate(processDmc, new ImmediateRequestMonitor() {
                            @Override
                            protected void handleCompleted() {
                            	if (!isSuccess()) {
                            		request.setStatus(getStatus());
                            		request.done();
                            	}
                            	else {
                            		waitForTermination(request);
                            	}
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
    
    /**
     * Wait for the debug session to be fully shutdown before reporting
     * that the terminate was completed.  This is important for the
     * 'Terminate and remove' operation.
     * The wait time is limited with a timeout so as to eventually complete the
     * request in the case of termination error, or when terminating
     * a single process in a multi-process session.
     * See bug 377447
     */
    private void waitForTermination(final IDebugCommandRequest request) {
    	// It is possible that the session already had time to terminate
		if (!DsfSession.isSessionActive(fSession.getId())) {
			request.done();
			return;
		}

		// Listener that will indicate when the shutdown is complete
		final SessionEndedListener endedListener = new SessionEndedListener () { 
			@Override
			public void sessionEnded(DsfSession session) {
				if (fSession.equals(session)) {
					DsfSession.removeSessionEndedListener(this);
					request.done(); 
				}
			}
		};

		DsfSession.addSessionEndedListener(endedListener); 

		// Create the timeout
		// For a multi-process session, if a single process is
		// terminated, this timeout will always hit (unless the
		// session is also terminated before the timeout).
		// We haven't found a problem with delaying the completion
		// of the request that way.
		// Note that this timeout is not removed even if we don't
		// need it anymore, once the session has terminated;
		// instead, we let it timeout and ignore it if the session
		// is already terminated.
		fExecutor.schedule(new Runnable() { 
			@Override
			public void run() {
				// Check that the session is still active when the timeout hits.
				// If it is not, then everything has been cleaned up already.
				if (DsfSession.isSessionActive(fSession.getId())) {
					DsfSession.removeSessionEndedListener(endedListener);

					// Marking the request as cancelled will prevent the removal of 
					// the launch from the Debug view in case of "Terminate and Remove".
					// This is important for multi-process sessions when "Terminate and Remove"
					// is applied to one of the running processes. In this case the selected
					// process will be terminated but the associated launch will not be removed 
					// from the Debug view.
					request.setStatus(Status.CANCEL_STATUS);
					request.done();
				}
			}},
			1, TimeUnit.MINUTES);
    }
}
