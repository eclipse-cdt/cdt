package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.dd.dsf.model.IDataModelService;

/**
 * This interface provides access to controlling and monitoring the execution 
 * state of a process being debugged.  This interface does not actually 
 * provide methods for creating or destroying execution contexts, it doesn't
 * even have methods for getting labels.  That's because it is expected that
 * higher level services, ones that deal with processes, kernels, or target 
 * features will provide that functionality. 
 */
public interface IRunControl extends IDataModelService
{

    /**
     * Execution context is the object on which run control operations can be
     * performed.  A lot of higher-level services reference this context to build
     * functionality on top of it, e.g. stack, expression evaluation, registers, etc.
     */
    public interface IExecutionDMC extends IDataModelContext<IExecutionData> {}
    
    /**
     * Context representing a process, kernel, or some other logical container 
     * for execution cotnexts, which by itself can perform run-control
     * operations. 
     */
    public interface IContainerDMC extends IExecutionDMC {}

    /** Flag indicating reason context state change. */
    public enum StateChangeReason { USER_REQUEST, STEP, BREAKPOINT, EXCEPTION, CONTAINER };
        
    /**
     * Events signaling a state changes.
     */
    public interface SuspendedEvent extends IDataModelEvent<IExecutionDMC> {
        StateChangeReason getReason();
    }
    public interface ResumedEvent extends IDataModelEvent<IExecutionDMC> {
        StateChangeReason getReason();
    }
    public interface ContainerSuspendedEvent extends IDataModelEvent<IExecutionDMC> {
        StateChangeReason getReason();
    }
    public interface ContainerResumedEvent extends IDataModelEvent<IExecutionDMC> {
        StateChangeReason getReason();
    }
    public interface IStartedEvent extends IDataModelEvent<IExecutionDMC> {
        IExecutionDMC getExecutionContext();
    }
    public interface IExitedEvent extends IDataModelEvent<IExecutionDMC> {
        IExecutionDMC getExecutionContext();
    }
    
    /**
     * Display information for an execution context.
     */
    public interface IExecutionData extends IDataModelData {
        boolean isSuspended();
        StateChangeReason getStateChangeReason();
    }

    /**
     * Returns execution contexts belonging to the given container context.
     */
    public void getExecutionContexts(IContainerDMC c, GetDataDone<IExecutionDMC[]> done);

    /*
     * Run control commands.  They all require the IExecutionContext object on 
     * which they perform the operations.  
     */
    boolean canResume(IExecutionDMC context);
    boolean canSuspend(IExecutionDMC context);
    boolean isSuspended(IExecutionDMC context);
    void resume(IExecutionDMC context, Done done);
    void suspend(IExecutionDMC context, Done done);
    public enum StepType { STEP_OVER, STEP_INTO, STEP_RETURN };
    boolean isStepping(IExecutionDMC context);
    void canStep(IExecutionDMC context);
    void step(IExecutionDMC context, StepType stepType, Done done);
    void instructionStep(IExecutionDMC context, StepType stepType, Done done);
}
