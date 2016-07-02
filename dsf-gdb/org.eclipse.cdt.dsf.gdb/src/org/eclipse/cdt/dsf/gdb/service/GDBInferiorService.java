/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.GDBProcesses_7_0.ContainerCreatedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.pty.PTY;
import org.osgi.framework.BundleContext;

/**
 * @since 5.1
 */
public class GDBInferiorService extends AbstractDsfService 
{
    private IGDBControl fCommandControl;
    private IGDBBackend fBackend;
    private CommandFactory fCommandFactory;
    
    public GDBInferiorService(DsfSession session) {
    	super(session);
    }

    /**
     * This method initializes this service.
     * 
     * @param requestMonitor
     *            The request monitor indicating the operation is finished
     */
    @Override
    public void initialize(final RequestMonitor rm) {
    	super.initialize(new ImmediateRequestMonitor(rm) {
    		@Override
    		protected void handleSuccess() {
    			doInitialize(rm);
			}
		});
	}
	
	/**
	 * This method initializes this service after our superclass's initialize()
	 * method succeeds.
	 * 
	 * @param requestMonitor
	 *            The call-back object to notify when this service's
	 *            initialization is done.
	 */
	private void doInitialize(RequestMonitor rm) {
		fCommandControl = getServicesTracker().getService(IGDBControl.class);
    	fBackend = getServicesTracker().getService(IGDBBackend.class);
        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

        getSession().addServiceEventListener(this, null);

		// Register this service.
		register(new String[] { GDBInferiorService.class.getName() },
				 new Hashtable<String, String>());
        
		// We must handle the very first inferior automatically because
		// we weren't initialized by the time GDB send us the first
		// =thread-group-added event
		// When a thread-group is created we don't have a pid, so we can simply create the container with the UNIQUE_GROUP_ID
		IMIProcesses procService = getServicesTracker().getService(IMIProcesses.class);
		//TODO create a constant for i1
		IMIContainerDMContext containerDmc = procService.createContainerContextFromGroupId(fCommandControl.getContext(), "i1"); //$NON-NLS-1$

    	initializeInferior(containerDmc, rm);
	}

	/**
	 * This method shuts down this service. It unregisters the service, stops
	 * receiving service events, and calls the superclass shutdown() method to
	 * finish the shutdown process.
	 * 
	 * @return void
	 */
	@Override
	public void shutdown(RequestMonitor rm) {
		unregister();
        getSession().removeServiceEventListener(this);
		super.shutdown(rm);
	}
	
	/**
	 * @return The bundle context of the plug-in to which this service belongs.
	 */
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}
		
	private void initializeInferior(IMIContainerDMContext containerDmc, RequestMonitor rm) {
    	if (fBackend.getSessionType() == SessionType.REMOTE) {
    		// The program input and output for a remote session is handled by gdbserver. 
    		// Therefore, no need to create a pty.
    		rm.done();
    	} else {
    		// Every other type of session that can get to this code, is starting a new process
    		// and requires a pty for it.
    		try {
    			PTY pty = new PTY();
				pty.validateSlaveName();

    			// Tell GDB to use this PTY
    			fCommandControl.queueCommand(
    					fCommandFactory.createMIInferiorTTYSet(containerDmc, pty.getSlaveName()), 
    					new ImmediateDataRequestMonitor<MIInfo>(rm) {
    						@Override
    						protected void handleSuccess() {
    							createInferiorProcess(containerDmc, pty);
    							rm.done();
    						}
    						
    						@Override
    						protected void handleFailure() {
    							// We were not able to tell GDB to use the PTY
    							// so we won't use it.
    							createInferiorProcess(containerDmc, fBackend.getMIOutputStream());
    			        		rm.done();
    						}
    					});
    		} catch (IOException e) {
    			// Unable to create a PTY
				createInferiorProcess(containerDmc, fBackend.getMIOutputStream());
        		rm.done();
    		}
    	}
	}
    

	protected MIInferiorProcess createInferiorProcess(IContainerDMContext container, OutputStream outputStream) {
		return new MIInferiorProcess(container, outputStream);
	}

	protected MIInferiorProcess createInferiorProcess(IContainerDMContext container, PTY pty) {
		return new MIInferiorProcess(container, pty);
	}

    @DsfServiceEventHandler
    public void eventDispatched(ContainerCreatedDMEvent e) {
    	initializeInferior(e.getDMContext(), new RequestMonitor(getExecutor(), null));
	}

    @DsfServiceEventHandler
    public void eventDispatched(IStartedDMEvent e) {
    	if (e.getDMContext() instanceof IMIContainerDMContext) {
    		// Initialize a new inferior in case the previous one is restarted
    		// Since the restart can be triggered from the GDB console, we won't know it happened
    		// until the new process is started, by which time we already have to have the PTY set in GDB.
    		// That is why we do it right away.
    		initializeInferior((IMIContainerDMContext)e.getDMContext(), new RequestMonitor(getExecutor(), null));
    	}
    }
}
