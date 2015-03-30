/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Ericsson - added support for core-awareness
 *     Marc Khouzam (Ericsson) - Support for exited processes in the debug view (bug 407340)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;

public interface IGDBProcesses extends IMIProcesses {
	
    /**
     * This interface extends the DSF ThreadDMData to provide
     * the cores on which a process or a thread is located as well
     * as the owner of the process.
     * 
	 * @since 4.0
	 */
    interface IGdbThreadDMData extends IThreadDMData {
    	/**
    	 * @return The list of identifiers of the cores on which the thread
    	 *         or process is currently located.  A thread will typically
    	 *         be located on a single core at a time, while a process will
    	 *         be located on all cores on which one of the process' threads
    	 *         is located.  Returns null if the information is not available.
    	 */
        String[] getCores();
        
        /**
         * @return The owner of the process, usually a user ID.  Returns null if the
         *         information is not available.  For threads, this method can return
         *         null or the owner of the parent process, if available.    
         */
        String getOwner();
    }
    
    /**
     * This interface describes an exited thread/process.
     * 
	 * @since 4.7
	 */
    interface IGdbThreadExitedDMData extends IThreadDMData {
		/** 
		 * @return The exit code of this process.
		 *         Returns null if the exit code is not known.
		 */
		Integer getExitCode();
    }
    
    /**
     * Get a list of all execution contexts belonging to a container.  This call is synchronous,
     * unlike the call to getProcessesBeingDebugged().  However, some services may not be able
     * to fulfill this request synchronously and will have to rely on getProcessesBeingDebugged().
     *
     * @param containerDmc The container for which we want to get the execution contexts
     */
    IMIExecutionDMContext[] getExecutionContexts(IMIContainerDMContext containerDmc);
    
    /**
     * Returns whether the specified process can be restarted.
     *  
     * @param containerDmc The process that should be restarted
     * @param rm The requestMonitor that returns if a restart is allowed on the specified process.
     * 
     * @since 4.0
     */
    void canRestart(IContainerDMContext containerDmc, DataRequestMonitor<Boolean> rm);
    
    /**
     * Request that the specified process be restarted.
     * 
     * @param containerDmc The process that should be restarted
     * @param attributes Different attributes that affect the restart operation.  This is 
     *                   usually the launch configuration attributes
     * @param rm The requetMonitor that indicates that the restart request has been completed.  It will
     *           contain the IContainerDMContext fully filled with the data of the restarted process.
     *           
     * @since 4.0
     */
	void restart(IContainerDMContext containerDmc, Map<String, Object> attributes, DataRequestMonitor<IContainerDMContext> rm);
	
    /**
     * Request that the specified process be started.
     * 
     * @param containerDmc The process that should be started.
     * @param attributes Different attributes that affect the start operation.  This is 
     *                   usually the launch configuration attributes
     * @param rm The requestMonitor that indicates that the start request has been completed.  It will
     *           contain the IContainerDMContext fully filled with the data of the newly started process.
     *           
     * @since 4.0
     */
	void start(IContainerDMContext containerDmc, Map<String, Object> attributes, DataRequestMonitor<IContainerDMContext> rm);
	
    /**
     * Attaches debugger to the given process.     
     * When attaching to a process, a debugging context can now be used to characterize the process.
     * This method can optionally choose to return this IDMContext inside the DataRequestMonitor.  
     * This can be useful for backends that do not have the ability to obtain the different 
     * debugging IDMContexts through {@link #getProcessesBeingDebugged(IDMContext, DataRequestMonitor)
     * 
     * @param file Binary to use for the process.
     * @since 4.0
     */    
    void attachDebuggerToProcess(IProcessDMContext procCtx, String file, DataRequestMonitor<IDMContext> rm);

}
