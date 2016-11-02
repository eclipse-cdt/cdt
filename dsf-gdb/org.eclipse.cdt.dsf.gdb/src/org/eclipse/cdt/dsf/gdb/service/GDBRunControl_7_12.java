/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @since 5.2
 */
public class GDBRunControl_7_12 extends GDBRunControl_7_10 {
	private IMICommandControl fCommandControl;
	private CommandFactory fCommandFactory;

	public GDBRunControl_7_12(DsfSession session) {
		super(session);
	}

	
	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(final RequestMonitor rm) {
        fCommandControl = getServicesTracker().getService(IMICommandControl.class);
        fCommandFactory = fCommandControl.getCommandFactory();
		
		register(new String[]{ GDBRunControl_7_12.class.getName() }, 
		new Hashtable<String,String>());

		rm.done();
	}

    @Override
    public void suspend(IExecutionDMContext context, final RequestMonitor rm){
        canSuspend(
            context, 
            new DataRequestMonitor<Boolean>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    if (getData()) {
							// Thread or Process
							doSuspend(context, rm);
							return;
                    } else {
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Context cannot be suspended.", null)); //$NON-NLS-1$
                        rm.done();
                    }
                }
            });
    }
	
	private void doSuspend(final IExecutionDMContext context, final RequestMonitor rm) {
		// Start the job before sending the interrupt command
		// to make sure we don't miss the *stopped event
		final MonitorSuspendJob monitorJob = new MonitorSuspendJob(0, rm);
		fCommandControl.queueCommand(fCommandFactory.createMIExecInterrupt(context),
				new ImmediateDataRequestMonitor<MIInfo>() {
					@Override
					protected void handleSuccess() {
						// Nothing to do in the case of success, the monitoring job
						// will take care of completing the RM once it gets the
						// *stopped event.
					}

					@Override
					protected void handleFailure() {
						// In case of failure, we must cancel the monitoring job
						// and indicate the failure in the rm.
						monitorJob.cleanAndCancel();
						rm.done(getStatus());
					}
				});
	}

    protected class MonitorSuspendJob extends Job {
    	// Bug 310274.  Until we have a preference to configure timeouts,
    	// we need a large enough default timeout to accommodate slow
    	// remote sessions.
    	private final static int TIMEOUT_DEFAULT_VALUE = 5000;

        private final RequestMonitor fRequestMonitor;

        public MonitorSuspendJob(int timeout, RequestMonitor rm) {
            super("Suspend monitor job."); //$NON-NLS-1$
            setSystem(true);
            fRequestMonitor = rm;
            
            if (timeout <= 0) {
            	timeout = TIMEOUT_DEFAULT_VALUE; // default of 5 seconds
            }
            
            // Register to listen for the stopped event
    		getSession().addServiceEventListener(this, null);

           	schedule(timeout);
        }

        /**
         * Cleanup job and cancel it.
         * This method is required because super.canceling() is only called
         * if the job is actually running.
         */
        public boolean cleanAndCancel() {
        	if (getExecutor().isInExecutorThread()) {
        		getSession().removeServiceEventListener(this); 
        	} else {
        		getExecutor().submit(
       				new DsfRunnable() {
       					@Override
       					public void run() {
       						getSession().removeServiceEventListener(MonitorSuspendJob.this);
       					}
       				});
        	}
        	return cancel();
        }
        
        @DsfServiceEventHandler
    	public void eventDispatched(MIStoppedEvent e) {
    		if (e.getDMContext() != null && e.getDMContext() instanceof IMIExecutionDMContext ) {
    			// For all-stop, this means all threads have stopped
    			if (cleanAndCancel()) {
    				fRequestMonitor.done();
    			}
    		}
    	}
    	
        @Override
        protected IStatus run(IProgressMonitor monitor) {
        	// This will be called when the timeout is hit and no *stopped event was received
        	getExecutor().submit(
                new DsfRunnable() {
                  	@Override
                    public void run() {
                		getSession().removeServiceEventListener(MonitorSuspendJob.this);
                       	fRequestMonitor.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Suspend operation timeout.", null)); //$NON-NLS-1$
                    }
                });
        	return Status.OK_STATUS;
        }
    }
}
