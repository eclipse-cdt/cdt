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
 *******************************************************************************/

package org.eclipse.dd.gdb.internal.provisional.service;


import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.internal.GdbPlugin;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControlDMContext;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.dd.mi.service.IMIRunControl;
import org.eclipse.dd.mi.service.MIRunControl;
import org.eclipse.dd.mi.service.command.commands.CLIInfoThreads;
import org.eclipse.dd.mi.service.command.events.MIEvent;
import org.eclipse.dd.mi.service.command.events.MIThreadExitEvent;
import org.eclipse.dd.mi.service.command.output.CLIInfoThreadsInfo;

public class GDBRunControl extends MIRunControl implements IGDBRunControl {

	/**
     * Implement a custom execution data for threads in order to provide additional 
     * information.  This object can be made separate from IExecutionDMData after
     * the deprecated method: IDMService.getModelData() is no longer used.  
     */
    public static class GDBThreadData implements IGDBThreadData {
        private final String fId;
        private final String fName;

        GDBThreadData(String id, String name) {
            fId = id;
            fName = name;
        }
        
        public String getName() {
            return fName; 
        }
        public String getId() { return fId; } 

        public boolean isDebuggerAttached() { return true; }
    }

    /**
     * Implement a custom execution data the process in order to provide additional 
     * information.  This object can be made separate from IExecutionDMData after
     * the deprecated method: IDMService.getModelData() is no longer used.  
     */
    public static class GDBProcessData implements IGDBProcessData {
        private final String fName;
        
        GDBProcessData(String name) {
            fName = name;
        }
        
        public String getName() {
            return fName;
        }
    }

    private GDBControl fGdb;
    
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
    	
        fGdb = getServicesTracker().getService(GDBControl.class);
        register(new String[]{IRunControl.class.getName(), 
        		IMIRunControl.class.getName(),  MIRunControl.class.getName(), 
        		IGDBRunControl.class.getName(), GDBRunControl.class.getName()}, new Hashtable<String,String>());
        requestMonitor.done();
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        unregister();
        super.shutdown(requestMonitor);
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
    public void getExecutionContexts(IContainerDMContext c, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		DataRequestMonitor<IExecutionDMContext[]> rm1 = new DataRequestMonitor<IExecutionDMContext[]>(
				getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				raiseExitEvents(getData());
				fOldExecutionCtxts = getData();
				rm.setData(fOldExecutionCtxts);
				rm.done();
			}
		};
		super.getExecutionContexts(c, rm1);
    }

	public void getProcessData(GDBControlDMContext gdbDmc, DataRequestMonitor<IGDBProcessData> rm) {
        rm.setData( new GDBProcessData(fGdb.getExecutablePath().lastSegment()) );
        rm.done();
	}
	
	public void getThreadData(final IMIExecutionDMContext execDmc, final DataRequestMonitor<IGDBThreadData> rm) {
        IContainerDMContext containerDmc = DMContexts.getAncestorOfType(execDmc, IContainerDMContext.class);
        assert containerDmc != null; // Every exec context should have a container as an ancestor.
        getCache().execute(new CLIInfoThreads(containerDmc),
                new DataRequestMonitor<CLIInfoThreadsInfo>(getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        rm.setData( createThreadInfo(execDmc, getData()) );
                        rm.done();
                    }
                });
	}

    private GDBThreadData createThreadInfo(IMIExecutionDMContext dmc, CLIInfoThreadsInfo info){
        for (CLIInfoThreadsInfo.ThreadInfo thread : info.getThreadInfo()) {
            if(Integer.parseInt(thread.getId()) == dmc.getThreadId()){
                //fMapThreadIds.put(thread.getId(), String.valueOf(dmc.getId()));
                return new GDBThreadData(thread.getOsId(), thread.getName());       
            }
        }
        return  new GDBThreadData("","");  //$NON-NLS-1$ //$NON-NLS-2$
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
                MIEvent<?> e =  new MIThreadExitEvent(containerDmc, ((IMIExecutionDMContext)ctxt).getThreadId());
                // Dispatch DsfMIThreadExitEvent
                getSession().dispatchEvent(e, getProperties());
			}
		}
	}
	
	
}
