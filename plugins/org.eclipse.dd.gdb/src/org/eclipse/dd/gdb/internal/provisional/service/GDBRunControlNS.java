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


import java.util.Hashtable;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControlDMContext;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.dd.mi.service.IMIRunControl;
import org.eclipse.dd.mi.service.MIRunControlNS;
import org.eclipse.dd.mi.service.command.commands.MIThreadInfo;
import org.eclipse.dd.mi.service.command.output.MIThreadInfoInfo;

public class GDBRunControlNS extends MIRunControlNS implements IGDBRunControl 
{
	/**
     * Implement a custom execution data for threads in order to provide additional 
     * information.  This object can be made separate from IExecutionDMData after
     * the deprecated method: IDMService.getModelData() is no longer used.  
     */
	private static class GDBThreadData implements IGDBThreadData {
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
    private static class GDBProcessData implements IGDBProcessData {
        private final String fName;
        
        GDBProcessData(String name) {
            fName = name;
        }
        
        public String getName() {
            return fName;
        }
    }

    private GDBControl fGdb;
    
    public GDBRunControlNS(DsfSession session) {
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
        register(new String[]{IRunControl.class.getName(), IMIRunControl.class.getName(), IGDBRunControl.class.getName()}, new Hashtable<String,String>());

        requestMonitor.done();
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        unregister();
        super.shutdown(requestMonitor);
    }

	public void getProcessData(GDBControlDMContext gdbDmc, DataRequestMonitor<IGDBProcessData> rm) {
        rm.setData( new GDBProcessData(fGdb.getExecutablePath().lastSegment()) );
        rm.done();
	}
	
	public void getThreadData(final IMIExecutionDMContext execDmc, final DataRequestMonitor<IGDBThreadData> rm) {
		IContainerDMContext containerDmc =  DMContexts.getAncestorOfType(execDmc, IContainerDMContext.class);
        getCache().execute(new MIThreadInfo(containerDmc, execDmc.getThreadId()),
                new DataRequestMonitor<MIThreadInfoInfo>(getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        rm.setData(createThreadInfo(execDmc, getData()));
                        rm.done();
                    }
                });
	}

	private GDBThreadData createThreadInfo(IMIExecutionDMContext dmc, MIThreadInfoInfo info) {
		// There should be only 1 thread in the result, but just in case...
		for (MIThreadInfoInfo.ThreadInfo thread : info.getThreadInfoList()) {
			if (Integer.parseInt(thread.getGdbId()) == dmc.getThreadId()){
				return new GDBThreadData(thread.getOsId(), "");        //$NON-NLS-1$
			}
		}
		return  new GDBThreadData("", "");  //$NON-NLS-1$ //$NON-NLS-2$
	}

}
