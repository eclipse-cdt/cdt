package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.dd.dsf.model.IDataModelService;

/**
 * This interface provides access to the native OS's process 
 * information, manipulation methods, and debugging methods.
 * This service provides a relatively simple interface for 
 * manipulating processes as compared with a full-blown
 * remote target debugger. 
 */
public interface INativeProcesses extends IDataModelService {
    
    public interface IThreadDMC extends IDataModelContext<IThreadData> {}
    public interface IProcessDMC extends IThreadDMC {}

    /**
     * Interface for thread and process object data.  This data provides a link
     * to the lower level debugger services, in form of symbol, memory, and
     * execution contexts.
     */
    public interface IThreadData extends IDataModelData {
        String getName();
        String getId();
        boolean isDebuggerAttached();
        IRunControl.IExecutionDMC getExecutionContext();
        IMemory.IMemoryContext getMemoryContext();
        IModules.ISymbolDMC getSymbolContext();
    }
    
    /**
     * Event indicating that process data has changed.
     */
    public interface ProcessChangedEvent extends IDataModelEvent<IProcessDMC> {}

    /**
     * Retrieves the current list of processes running on target.
     */
    void getRunningProcesses(GetDataDone<IProcessDMC[]> done);
    
    /**
     * Attaches debugger to the given process.
     */
    void attachDebuggerToProcess(IProcessDMC procCtx, Done done);
    
    /**
     * Starts a new process.
     * @param file Process image to use for the new process.
     * @param done Return token with the process context.
     */
    void runNewProcess(String file, GetDataDone<IProcessDMC> done);
    
    /**
     * Starts a new process with debugger attached.
     * @param file Process image to use for the new process.
     * @param done Return token with the process context.
     */
    void debugNewProcess(String file, GetDataDone<IProcessDMC> done);
    
    /** 
     * Returns a thread context for given run control execution context.
     * @param execCtx Execution context to return thread for.
     * @return Corresponding thread context.
     */
    IThreadDMC getThreadForExecutionContext(IRunControl.IExecutionDMC execCtx); 
}
