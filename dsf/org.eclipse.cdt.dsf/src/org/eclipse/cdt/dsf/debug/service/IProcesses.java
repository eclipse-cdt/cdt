/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson           - Updated for latest DSF version
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * This interface provides access to the OS's process 
 * information, manipulation methods, and debugging methods.
 * This service provides a relatively simple interface for 
 * manipulating processes as compared with a full-blown
 * remote target debugger. 
 * 
 * @since 1.1
 */
public interface IProcesses extends IDsfService {
    
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
     * Retrieves the debugging context that characterizes the specified thread
     * or process context.  
     * 
     * @param dmc The thread or process dmc for which we want the debugging context
     * @param rm The request monitor that will contain the debugging context.
     *           null if no such context exists
     */
    public void getDebuggingContext(IThreadDMContext dmc, DataRequestMonitor<IDMContext> rm);
    
    /**
     * Retrieves the current list of processes running on target.
     * @param dmc The processor or core for which to list all processes
     * @param rm Request completion monitor, to be filled in with array of process contexts.
     */
    void getRunningProcesses(IDMContext dmc, DataRequestMonitor<IProcessDMContext[]> rm);
    
    /**
     * Checks whether it is possible to attach the debugger to a new process.
     * @param dmc The processor or core on which we want to attach to a process.
     * @param rm Return if it is possible to attach.
     */
    void isDebuggerAttachSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm);

    /**
     * Attaches debugger to the given process.     
     * When attaching to a process, a debugging context can now be used to characterize the process.
     * This method can optionally choose to return this IDMContext inside the DataRequestMonitor.  
     * This can be useful for backends that do not have the ability to obtain the different 
     * debugging IDMContexts through {@link #getProcessesBeingDebugged(IDMContext, DataRequestMonitor)
     */    
    void attachDebuggerToProcess(IProcessDMContext procCtx, DataRequestMonitor<IDMContext> rm);

    /**
     * Checks whether it is possible to detach the debugger from the specified process.
     * @param dmc The debugging context from which we want to detach.  This context
     *            should have IProcessDMContext as an ancestor.
     * @param rm Return if it is possible to detach.
     */
    void canDetachDebuggerFromProcess(IDMContext dmc, DataRequestMonitor<Boolean> rm);

    /**
     * Detaches debugger from the given process.
     * @param dmc The debugging context from which we want to detach.  This context
     *            should have IProcessDMContext as an ancestor.
     */
    void detachDebuggerFromProcess(IDMContext dmc, RequestMonitor requestMonitor);

    /**
     * Checks whether it is possible to run a new process.
     * @param dmc The processor or core on which we want to run a new process.
     * @param rm Return if it is possible to run a new process.
     */
    void isRunNewProcessSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm);

    /**
     * Starts a new process.
     * @param dmc The processor or core on which we want to run a new process.
     * @param file Process image to use for the new process.
     * @param attributes Attributes that give information on the process to be debugged
     * @param rm Request completion monitor, to be filled in with the process context.
     */
    void runNewProcess(IDMContext dmc, 
    		           String file,
    		           Map<String, Object> attributes,
    		           DataRequestMonitor<IProcessDMContext> rm);
    
    /**
     * Checks whether it is possible to start a new process with the debugger attached
     * @param dmc The processor or core on which we want to start and debug the new process.
     * @param rm Return if it is possible to start a new process with the debugger attached.
     */
    void isDebugNewProcessSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm);

    /**
     * Starts a new process with the debugger attached.
     * @param dmc The processor or core on which we want to start and debug the new process.
     * @param file Process image to use for the new process.
     * @param attributes Attributes that give information on the process to be debugged
     * @param rm Request completion monitor, to be filled in with the 
     *           debugging context that can now be used to characterize the process
     */
    void debugNewProcess(IDMContext dmc, 
    		             String file, 
    		             Map<String, Object> attributes,
    		             DataRequestMonitor<IDMContext> rm);

    /**
     * Retrieves the list of processes which are currently under debugger control.
     * 
     * @param dmc The processor or core for which to list processes being debugged
     * @param rm Request completion monitor which contains all debugging contexts representing
     *           the processes being debugged.  Note that each of these contexts should also have
     *           IProcessDMContext as a parent.
     */
    void getProcessesBeingDebugged(IDMContext dmc, DataRequestMonitor<IDMContext[]> rm);
    
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
