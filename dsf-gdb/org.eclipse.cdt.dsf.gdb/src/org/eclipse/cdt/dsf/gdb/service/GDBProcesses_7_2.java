/*******************************************************************************
 * Copyright (c) 2010, 2011 TUBITAK BILGEM-ITI and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIAddInferiorInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Adding support for multi-process with GDB 7.2
 * 
 * @since 4.0
 */
public class GDBProcesses_7_2 extends GDBProcesses_7_1 {
    
    private CommandFactory fCommandFactory;
    private IGDBControl fCommandControl;
    
	public GDBProcesses_7_2(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
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
	private void doInitialize(RequestMonitor requestMonitor) {
		fCommandControl = getServicesTracker().getService(IGDBControl.class);
        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
    	requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		super.shutdown(requestMonitor);
	}
	
	@Override
    public void attachDebuggerToProcess(final IProcessDMContext procCtx, final DataRequestMonitor<IDMContext> rm) {
		if (procCtx instanceof IMIProcessDMContext) {
	    	if (!doIsDebuggerAttachSupported()) {
	            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Attach not supported.", null)); //$NON-NLS-1$
	            rm.done();    		
	    		return;
	    	}
	    	
	    	ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(procCtx, ICommandControlDMContext.class);
	        fCommandControl.queueCommand(
	        		fCommandFactory.createMIAddInferior(controlDmc),
	        		new DataRequestMonitor<MIAddInferiorInfo>(getExecutor(), rm) {
	        			@Override
	        			protected void handleSuccess() {
	        				final String groupId = getData().getGroupId();
	        				if (groupId == null || groupId.trim().length() == 0) {
     				           rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid gdb group id.", null)); //$NON-NLS-1$
    				           rm.done();
    				           return;
        					}
	        				
	        				final IMIContainerDMContext containerDmc = createContainerContext(procCtx, groupId);
	        				fCommandControl.queueCommand(
	        						fCommandFactory.createMITargetAttach(containerDmc, ((IMIProcessDMContext)procCtx).getProcId()),
	        						new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
	        							@Override
	        							protected void handleSuccess() {
	        								rm.setData(containerDmc);
	        								
	        								// Start tracking this process' breakpoints.
	        								MIBreakpointsManager bpmService = getServicesTracker().getService(MIBreakpointsManager.class);
	        								bpmService.startTrackingBreakpoints(containerDmc, rm);
	        							}
	        						});
	        			}
	        		});
	    } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}
	
	@Override
    public void detachDebuggerFromProcess(IDMContext dmc, final RequestMonitor rm) {
    	
		ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		final IMIContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
		
    	if (controlDmc != null && containerDmc != null) {
        	if (!doCanDetachDebuggerFromProcess()) {
                rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Detach not supported.", null)); //$NON-NLS-1$
                rm.done();
                return;
        	}

        	fCommandControl.queueCommand(
        			fCommandFactory.createMITargetDetach(controlDmc, containerDmc.getGroupId()),
    				new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    					@Override
    					protected void handleCompleted() {
    						if (isSuccess()) {
    						// Bug in GDB 7.2 where removing an inferior will lead to a crash when running other processes.
    						// I'm hoping it will be fixed in 7.2.1
//    			        	fCommandControl.queueCommand(
//    			        			fCommandFactory.createMIRemoveInferior(fCommandControl.getContext(), containerDmc.getGroupId()),
//    			    				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
    						} else {
    							// This command fails with GDB 7.2 because of a GDB bug, which was fixed with GDB 7.2.1
    							// In case we get here, we assume we are using GDB 7.2 (although we should not) and we work
    							// around it.
    							// Also, with GDB 7.2, removing the inferior does not work because of another bug, so we just don't do it.
    					       	fCommandControl.queueCommand(
    				        			fCommandFactory.createMITargetDetach(containerDmc),
    				    				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
    						}
    					}
    				});
    	} else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}
}

