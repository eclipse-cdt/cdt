/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ICreatedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.pty.PTY;

/**
 * This class processes events related to the creation/start/deletion of
 * inferior processes in GDB.  Based on these events it prepares the console.
 * 
 * @since 5.1
 */
public class MIInferiorEventProcessor implements IEventProcessor {
	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;
	
    private ICommandControlService fCommandControl;
    private CommandFactory fCommandFactory;
    private IGDBBackend fBackend;
    
    public MIInferiorEventProcessor(ICommandControlService controlService) {
    	fCommandControl = controlService;
    	fSession = controlService.getSession();
        fServicesTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSession.getId());
    	
        fBackend = fServicesTracker.getService(IGDBBackend.class);
        fCommandFactory = ((IMICommandControl)fCommandControl).getCommandFactory();
    	
        fSession.addServiceEventListener(this, null);
    }
    
	@Override
	public void dispose() {
        fSession.removeServiceEventListener(this);
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
    public void eventDispatched(ICreatedDMEvent e) {
    	if (e.getDMContext() instanceof IMIContainerDMContext) {
    		// Whenever an inferior is added to GDB (=thread-group-added), we prepare it
    		initializeInferior((IMIContainerDMContext)e.getDMContext(), new RequestMonitor(fSession.getExecutor(), null));
    	}
	}

    @DsfServiceEventHandler
    public void eventDispatched(IStartedDMEvent e) {
    	if (e.getDMContext() instanceof IMIContainerDMContext) {
    		// Whenever an inferior starts to run (=thread-group-started), everything is
    		// already setup for it, so we can setup right away in case the instance is
    		// restarted.
    		// Since the restart can be triggered from the GDB console by typing 'run, 
    		// we won't know it happened until the new process is running, by which time we 
    		// already have to have the PTY set in GDB.
    		// That is why we do it right here, even if a restart may never happen.
    		initializeInferior((IMIContainerDMContext)e.getDMContext(), new RequestMonitor(fSession.getExecutor(), null));
    	}
    }

	@Override
	public void eventReceived(Object output) {
	}

	@Override
	public void commandQueued(ICommandToken token) {
	}

	@Override
	public void commandSent(ICommandToken token) {
	}

	@Override
	public void commandRemoved(ICommandToken token) {
	}

	@Override
	public void commandDone(ICommandToken token, ICommandResult result) {
	}
}
