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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;

public class DsfTerminateCommand implements ITerminateHandler {

	private class WaitForTerminationJob extends Job implements SessionEndedListener {

		final private IDebugCommandRequest fRequest;
		final private String fSessionId;
		final private Lock fLock = new ReentrantLock();
		final private Condition fTerminated = fLock.newCondition();

		public WaitForTerminationJob(String sessionId, IDebugCommandRequest request) {
			super("Wait for termination job"); //$NON-NLS-1$
			setUser(false);
			setSystem(true);
			fSessionId = sessionId;
			fRequest = request;
			DsfSession.addSessionEndedListener(WaitForTerminationJob.this);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			// Wait for all processes associated with the launch 
			// and the shutdown sequence to be completed.
			// The wait time is restricted to stop the job in case  
			// of termination error.
			boolean result = !DsfSession.isSessionActive(fSessionId);
			if (!result) {
				fLock.lock();
				try {
					result = fTerminated.await(1, TimeUnit.MINUTES);
				}
				catch(InterruptedException e) {
				}
				finally {
					fLock.unlock();
				}
			}
			// Marking the request as cancelled will prevent the removal of 
			// the launch from the Debug view in case of "Terminate and Remove". 
			fRequest.setStatus(result ? Status.OK_STATUS : Status.CANCEL_STATUS);
			fRequest.done();
			DsfSession.removeSessionEndedListener(WaitForTerminationJob.this);
			return Status.OK_STATUS;
		}

		@Override
		public void sessionEnded(DsfSession session) {
			if (fSessionId.equals(session.getId())) {
				fLock.lock();
				try {
					fTerminated.signal();
				}
				finally {
					fLock.unlock();
				}
			}
		}
	}

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

    // Run control may not be available after a connection is terminated and shut down.
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
	                            	WaitForTerminationJob job = new WaitForTerminationJob(fSession.getId(), request);
	                            	job.schedule();
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
	                            	WaitForTerminationJob job = new WaitForTerminationJob(fSession.getId(), request);
	                            	job.schedule();
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
}
