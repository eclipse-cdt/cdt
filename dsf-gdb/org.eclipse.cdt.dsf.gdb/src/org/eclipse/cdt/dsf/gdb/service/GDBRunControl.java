/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson AB		  - Modified for additional functionality	
 *     Nokia - create and use backend service. 
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;


import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.events.MIEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadExitEvent;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class GDBRunControl extends MIRunControl {
    private IGDBBackend fGdb;
	private IMIProcesses fProcService;

	// Record list of execution contexts
	private IExecutionDMContext[] fOldExecutionCtxts;

	
    public GDBRunControl(DsfSession session) {
        super(session);
    }
    
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
            new RequestMonitor(getExecutor(), requestMonitor) { 
                @Override
                public void handleSuccess() {
                    doInitialize(requestMonitor);
                }});
    }

    private void doInitialize(final RequestMonitor requestMonitor) {
    	
        fGdb = getServicesTracker().getService(IGDBBackend.class);
        fProcService = getServicesTracker().getService(IMIProcesses.class);

        register(new String[]{IRunControl.class.getName(), 
        		MIRunControl.class.getName(), 
        		GDBRunControl.class.getName()}, new Hashtable<String,String>());
        requestMonitor.done();
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        unregister();
        super.shutdown(requestMonitor);
    }
    
    @Override
	public IMIExecutionDMContext createMIExecutionContext(IContainerDMContext container, int threadId) {
        IProcessDMContext procDmc = DMContexts.getAncestorOfType(container, IProcessDMContext.class);
        
        IThreadDMContext threadDmc = null;
        if (procDmc != null) {
        	// For now, reuse the threadId as the OSThreadId
        	threadDmc = fProcService.createThreadContext(procDmc, Integer.toString(threadId));
        }

        return fProcService.createExecutionContext(container, threadDmc, Integer.toString(threadId));
    }

    @Override
    public void suspend(IExecutionDMContext context, final RequestMonitor rm){
        canSuspend(
            context, 
            new DataRequestMonitor<Boolean>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    if (getData()) {
                        fGdb.interrupt();
                    } else {
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Context cannot be suspended.", null)); //$NON-NLS-1$
                    }
                    rm.done();
                }
            });
    }

    
    /*
	 * This is a HACK. Remove this method when GDB starts to account exited threads id in -thread-list-id command.
	 * Exited threads are reported in -thread-list-id command even after an exit event is raised by GDB
	 * Hence, this method needs a special handling in case of GDB.
	 * Raises ExitEvent when a thread really exits from the system. This is done by comparing the execution contexts list
	 * See bug 200615 for details.
	 */
	@Override
    public void getExecutionContexts(IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		fProcService.getProcessesBeingDebugged(
				containerDmc,
				new DataRequestMonitor<IDMContext[]>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						if (getData() instanceof IExecutionDMContext[]) {
							IExecutionDMContext[] execDmcs = (IExecutionDMContext[])getData();
							raiseExitEvents(execDmcs);
							fOldExecutionCtxts = execDmcs;
							rm.setData(fOldExecutionCtxts);
						} else {
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid contexts", null)); //$NON-NLS-1$
						}
						rm.done();
					}
				});
    }

	private void raiseExitEvents(IExecutionDMContext[] ctxts){
		if(ctxts == null || fOldExecutionCtxts == null)
			return;
		List<IExecutionDMContext> list = Arrays.asList(ctxts);
		List<IExecutionDMContext> oldThreadList = Arrays.asList(fOldExecutionCtxts);
		Iterator<IExecutionDMContext> iterator = oldThreadList.iterator();
		while(iterator.hasNext()){
			IExecutionDMContext ctxt = iterator.next();
			if(! list.contains(ctxt)){
			    IContainerDMContext containerDmc = DMContexts.getAncestorOfType(ctxt, IContainerDMContext.class); 
                MIEvent<?> e =  new MIThreadExitEvent(containerDmc, Integer.toString(((IMIExecutionDMContext)ctxt).getThreadId()));
                // Dispatch DsfMIThreadExitEvent
                getSession().dispatchEvent(e, getProperties());
			}
		}
	}
	
    @Override
	public void canStep(final IExecutionDMContext context, StepType stepType, final DataRequestMonitor<Boolean> rm) {
    	if (context instanceof IContainerDMContext) {
    		rm.setData(false);
    		rm.done();
    		return;
    	}
    	
    	if (stepType == StepType.STEP_RETURN) {
    		// A step return will always be done in the top stack frame.
    		// If the top stack frame is the only stack frame, it does not make sense
    		// to do a step return since GDB will reject it.
    		
    		// Until bug 256798 is fixed, the service tracker could be null
    		if (getServicesTracker() == null) {
    			// service is shutdown
    			rm.setData(false);
    			rm.done();
    			return;
    		}
            MIStack stackService = getServicesTracker().getService(MIStack.class);
            if (stackService != null) {
            	// Check that the stack is at least two deep.
            	stackService.getStackDepth(context, 2, new DataRequestMonitor<Integer>(getExecutor(), rm) {
            		@Override
            		public void handleCompleted() {
            			if (isSuccess() && getData() == 1) {
            				rm.setData(false);
            				rm.done();
            			} else {
            	    		canResume(context, rm);
            			}
            		}
            	});
            	return;
            }
    	}
    	
    	canResume(context, rm);
    }
	
}
