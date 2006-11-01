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

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
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
    public interface IProcessDMContext extends IThreadDMContext {}

    /**
     * Interface for thread and process object data.  This data provides a link
     * to the lower level debugger services, in form of symbol, memory, and
     * execution contexts.
     */
    public interface IThreadDMData extends IDMData {
        String getName();
        String getId();
        boolean isDebuggerAttached();
        IRunControl.IExecutionDMContext getExecutionContext();
        IMemory.IMemoryContext getMemoryContext();
        IModules.ISymbolDMContext getSymbolContext();
    }
    
    /**
     * Event indicating that process data has changed.
     */
    public interface ProcessChangedDMEvent extends IDMEvent<IProcessDMContext> {}

    /**
     * Retrieves the current list of processes running on target.
     */
    void getRunningProcesses(GetDataDone<IProcessDMContext[]> done);
    
    /**
     * Attaches debugger to the given process.
     */
    void attachDebuggerToProcess(IProcessDMContext procCtx, Done done);
    
    /**
     * Starts a new process.
     * @param file Process image to use for the new process.
     * @param done Return token with the process context.
     */
    void runNewProcess(String file, GetDataDone<IProcessDMContext> done);
    
    /**
     * Starts a new process with debugger attached.
     * @param file Process image to use for the new process.
     * @param done Return token with the process context.
     */
    void debugNewProcess(String file, GetDataDone<IProcessDMContext> done);

    /**
     * Retrieves the list of processes which are currently under
     * debugger control.
     */
    void getProcessesBeingDebugged(GetDataDone<IProcessDMContext[]> done);
    
    /** 
     * Returns a thread context for given run control execution context.
     * @param execCtx Execution context to return thread for.
     * @return Corresponding thread context.
     */
    IThreadDMContext getThreadForExecutionContext(IRunControl.IExecutionDMContext execCtx); 
    
    /**
     * Checks whether the given process or thread can be terminated.
     * @param thread Thread or process to terminate.
     * @param done Return token.
     */
    void canTerminate(IThreadDMContext thread, GetDataDone<Boolean> done);

    /**
     * Terminates the selected process or thread.
     * @param thread Thread or process to terminate.
     * @param done Return token.
     */
    void terminate(IThreadDMContext thread, Done done);
}
