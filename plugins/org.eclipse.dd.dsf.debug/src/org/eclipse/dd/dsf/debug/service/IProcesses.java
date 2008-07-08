/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson           - Updated for latest DSF version
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.service;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;

/**
 * This interface provides access to the OS's process 
 * information, manipulation methods, and debugging methods.
 * This service provides a relatively simple interface for 
 * manipulating processes as compared with a full-blown
 * remote target debugger. 
 */
public interface IProcesses extends IDMService {
    
	/**
	 *  A thread as known by the OS.
	 *  This context is kept different than {@link IRunControl.IExecutionDMContext}
	 *  because the OS id of a thread may not be the same as the thread id used by
	 *  the debugger when doing run control operations.
	 */
    public interface IThreadDMContext extends IDMContext {}
	
    /**
	 *  A process as known by the OS.
	 *  This context is kept different than {@link IRunControl.IContainerDMContext}
	 *  because the OS id of a process may not be the same as the process id used by
	 *  the debugger when doing run control operations.
	 */
    public interface IProcessDMContext extends IThreadDMContext {}

    /**
     * Interface for thread and process object data.  This data provides a link
     * to the lower level debugger services, in form of execution contexts.
     */
    public interface IThreadDMData extends IDMData {
        String getName();
        String getId();
        boolean isDebuggerAttached();
        IDMContext getDebuggingContext();
    }
    
    /**
     * Event indicating that process data has changed.
     */
    public interface ProcessChangedDMEvent extends IDMEvent<IProcessDMContext> {}

    /**
     * Retrieves thread or process data for given context.
     * @param dmc Context to retrieve data for.
     * @param rm Request completion monitor.
     */
    public void getExecutionData(IThreadDMContext dmc, DataRequestMonitor<IThreadDMData> rm);
    
    /**
     * Retrieves the current list of processes running on target.
     * @param containerDmc The processor or core for which to list all processes
     * @param rm Request completion monitor, to be filled in with array of process contexts.
     */
    void getRunningProcesses(IContainerDMContext containerDmc, DataRequestMonitor<IProcessDMContext[]> rm);
    
    /**
     * Attaches debugger to the given process.     
     * When attaching to a process, a container context can now be used to characterize the process.
     * IContainerDMContext has IProcessDMContext as a parent.  This method can optionally choose
     * to return the IContainerDMContext inside the DataRequestMonitor.  This can be useful for
     * backends that do not have the ability to obtain the different IContainerDMContexts through 
     * {@link getProcessesBeingDebugged}
     */    
    void attachDebuggerToProcess(IProcessDMContext procCtx, DataRequestMonitor<IContainerDMContext> rm);

    /**
     * Detaches debugger from the given process.
     */
    void detachDebuggerFromProcess(IProcessDMContext procCtx, RequestMonitor requestMonitor);

    /**
     * Starts a new process.
     * @param file Process image to use for the new process.
     * @param rm Request completion monitor, to be filled in with the process context.
     */
    void runNewProcess(String file, DataRequestMonitor<IProcessDMContext> rm);
    
    /**
     * Starts a new process with debugger attached.
     * @param file Process image to use for the new process.
     * @param rm Request completion monitor, to be willed in with the process context.
     */
    void debugNewProcess(String file, DataRequestMonitor<IProcessDMContext> rm);

    /**
     * Retrieves the list of processes which are currently under
     * debugger control.
     * @param containerDmc The processor or core for which to list processes being debugged
     * @param rm Request completion monitor which contains all container contexts representing
     *           the processes being debugged.  Note that each of these containers also has
     *           IProcessDMContext as a parent.
     */
    void getProcessesBeingDebugged(IContainerDMContext containerDmc, DataRequestMonitor<IContainerDMContext[]> rm);
    
    /**
     * Checks whether the given process or thread can be terminated.
     * @param thread Thread or process to terminate.
     * @param rm Return token.
     */
    void canTerminate(IThreadDMContext thread, DataRequestMonitor<Boolean> rm);

    /**
     * Terminates the selected process or thread.
     * @param thread Thread or process to terminate.
     * @param rm Request completion monitor, indicates success or failure.
     */
    void terminate(IThreadDMContext thread, RequestMonitor requestMonitor);
}
