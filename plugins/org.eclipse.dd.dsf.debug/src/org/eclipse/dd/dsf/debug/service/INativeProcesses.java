/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.service;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;

/**
 * This interface provides access to the native OS's process 
 * information, manipulation methods, and debugging methods.
 * This service provides a relatively simple interface for 
 * manipulating processes as compared with a full-blown
 * remote target debugger. 
 */
public interface INativeProcesses extends IDMService {
    
    public interface IThreadDMContext extends IDMContext<IThreadDMData> {}
    public interface IProcessDMContext extends IDMContext<IProcessDMData> {}

    /**
     * Interface for thread and process object data.  
     */
    public interface IThreadDMData extends IDMData {
        String getName();
        String getId();
        boolean isDebuggerAttached();
        IDMContext<?> getDebugContext();
    }

    /**
     * Interface for thread and process object data.  
     */
    public interface IProcessDMData extends IDMData {
        String getName();
        String getId();
        boolean isDebuggerAttached();
        IDMContext<?> getDebugContext();
    }

    /**
     * Event indicating that process data has changed.
     */
    public interface IProcessChangedDMEvent extends IDMEvent<IProcessDMContext> {}


    public interface IProcessStartedEvent extends IDMEvent<IDMContext<INativeProcesses>> {
        IProcessDMContext getProcess();
    } 

    public interface IProcessExitedEvent extends IDMEvent<IDMContext<INativeProcesses>> {
        IProcessDMContext getProcess();
    } 
    
    /**
     * Returns a thread for the corresponding context.  <code>null</code> if no corresponding
     * thread exists.
     * @param execCtx
     * @return
     */
    public IThreadDMContext getThreadForDebugContext(IDMContext<?> execCtx);

    /**
     * Returns a process context corresponding to the given context. <code>null</code> if no 
     * corresponding process exists.    
     */
    public IProcessDMContext getProcessForDebugContext(IDMContext<?> execCtx);
    
    /**
     * Retrieves the current list of processes running on target.
     * @param rm Request completion monitor, to be filled in with array of process contexts.
     */
    void getRunningProcesses(DataRequestMonitor<IProcessDMContext[]> rm);
    
    /**
     * Attaches debugger to the given process.
     */
    void attachDebuggerToProcess(IProcessDMContext procCtx, RequestMonitor requestMonitor);
    
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
     * @param rm Request completion monitor.
     */
    void getProcessesBeingDebugged(DataRequestMonitor<IProcessDMContext[]> rm);
    
    /**
     * Checks whether the given process or thread can be terminated.
     * @param thread Thread or process to terminate.
     * @param rm Return token.
     */
    void canTerminate(IDMContext<?> ctx, DataRequestMonitor<Boolean> rm);

    /**
     * Terminates the selected process or thread.
     * @param thread Thread or process to terminate.
     * @param rm Request completion monitor, indicates success or failure.
     */
    void terminate(IDMContext<?> ctx, RequestMonitor requestMonitor);

}
