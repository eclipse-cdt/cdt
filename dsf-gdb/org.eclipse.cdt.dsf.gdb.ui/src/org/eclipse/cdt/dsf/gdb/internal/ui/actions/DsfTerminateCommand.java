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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IMultiTerminate;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IStatus;
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
        if (request.getElements().length == 0) {
            request.setEnabled(false);
            request.done();
            return;
        }

        getProcessDMContexts(request.getElements(), new DataRequestMonitor<IProcessDMContext[]>(fExecutor, null) {
        	@Override
        	protected void handleCompleted() {
        		if (!isSuccess()) {
        			request.setEnabled(false);
        			request.done();
        		}
        		else {
        			canTerminate(getData(), new ImmediateDataRequestMonitor<Boolean>() {
        				@Override
						protected void handleCompleted() {
            				if (!isSuccess()) {
            					request.setEnabled(false);
            				}
            				else {
            					request.setEnabled(getData());
            				}
            				request.done();
        				}
					});
        		}
        	}
        });
    }

    @Override
    public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length == 0) {
        	request.done();
        	return false;
        }
        
        getProcessDMContexts(request.getElements(), new DataRequestMonitor<IProcessDMContext[]>(fExecutor, null) {
        	@Override
        	protected void handleCompleted() {
        		if (!isSuccess()) {
        			request.setStatus(getStatus());
        			request.done();
        		}
        		else if (getData().length == 0){
        			request.done();
        		}
        		else {
        			terminate(getData(), new ImmediateRequestMonitor() {
        				@Override
						protected void handleCompleted() {
            				if (!isSuccess()) {
            					request.setStatus(getStatus());
            					request.done();
            				}
            				else {
            					waitForTermination(request);
            				}
        				}
        			});
        		}
        	}
        });
        
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

    private void getProcessDMContexts(Object[] elements, final DataRequestMonitor<IProcessDMContext[]> rm) {
    	GdbLaunch launch = null;
    	final Set<IProcessDMContext> procDmcs = new HashSet<IProcessDMContext>();
    	for (Object obj : elements) {
    		if (obj instanceof GdbLaunch) {
    			launch = (GdbLaunch)obj;
    			break;
    		}
    		if (obj instanceof IDMVMContext) {
				IProcessDMContext procDmc = 
					DMContexts.getAncestorOfType(((IDMVMContext)obj).getDMContext(), IProcessDMContext.class);
				if (procDmc != null) {
					procDmcs.add(procDmc);
				}
    		}
    	}
    	if (launch == null) {
    		rm.setData(procDmcs.toArray(new IProcessDMContext[procDmcs.size()]));
    		rm.done();
    	}
    	else {
            try {
            	fExecutor.execute(new DsfRunnable() { 
                    @Override
                    public void run() {
                    	ICommandControlService commandControl = fTracker.getService(ICommandControlService.class);
                    	final IProcesses procService = fTracker.getService(IProcesses.class);
                        if (commandControl != null && procService != null) {
                        	procService.getProcessesBeingDebugged(
                        		commandControl.getContext(), 
                        		new ImmediateDataRequestMonitor<IDMContext[]>() {
                        			@Override
									protected void handleCompleted() {
                        				if (!isSuccess()) {
                        					rm.setStatus(getStatus());
                        				}
                        				else {
                        					for (IDMContext ctx : getData()) {
                        						IProcessDMContext procDmc = DMContexts.getAncestorOfType(ctx, IProcessDMContext.class);
                        						if (procDmc != null) {
                        							procDmcs.add(procDmc);
                        						}
                        					}
                        		    		rm.setData(procDmcs.toArray(new IProcessDMContext[procDmcs.size()]));
                        				}
                    					rm.done();
                        			};
                        		});
                        } 
                        else {
                        	rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, "Service is not available.")); //$NON-NLS-1$
                        	rm.done();
                        }
                     }
                });
            } catch (RejectedExecutionException e) {
            	rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, e.getLocalizedMessage()));
                rm.done();
            }    		
    	}    	
    }

    private void canTerminate(IProcessDMContext[] procDmcs, DataRequestMonitor<Boolean> rm) {
    	if (procDmcs.length == 0) {
    		rm.setData(false);
    		rm.done();
    		return;
    	}
    	
    	IMultiTerminate multiTerminate = fTracker.getService(IMultiTerminate.class);
		if (multiTerminate != null) {
			multiTerminate.canTerminateSome(procDmcs, rm); 
		}
		else {
			IProcesses procService = fTracker.getService(IProcesses.class);
			if (procService != null && procDmcs.length == 1) {
				procService.canTerminate(procDmcs[0], rm);
			}
			else {
				rm.setData(false);;
				rm.done();
			}
		}    	
    }
    
    private void terminate(IProcessDMContext[] procDmcs, RequestMonitor rm) {
    	if (procDmcs.length == 0) {
    		rm.done();
    		return;
    	}

    	IMultiTerminate multiTerminate = fTracker.getService(IMultiTerminate.class);
    	if (multiTerminate != null) {
    		multiTerminate.terminate(procDmcs, rm);
    	}
    	else {
    		IProcesses procService = fTracker.getService(IProcesses.class);
    		if (procService != null && procDmcs.length == 1) {
    			procService.terminate(procDmcs[0], rm);
    		}
    		else {
    			rm.done();
    		}
    	}
    }
}
