package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.service.IDsfService;

/**
 * This service builds on top of standard run control service to provide 
 * step queuing functionality.  Step queuing essentially allows user to press
 * and hold the step key and achieve maximum stepping speed.  If this service
 * is used, other service implementations, such as stack and expressions, can 
 * use it to avoid requesting data from debugger back end if another step is 
 * about to be executed. 
 */
public interface IStepQueueManager extends IDsfService {
    
    void setStepQueueDepth(int depth);
    int getStepQueueDepth();
    
    /** 
     * Returns the number of step commands that are queued for given execution
     * context.
     */
    int getPendingStepCount(IRunControl.IExecutionDMC execCtx);

    /**
     * Checks whether a step command can be queued up for given context.
     */
    void canEnqueueStep(IRunControl.IExecutionDMC execCtx);
    
    /**
     * Adds a step command to the execution queue for given context.
     * @param execCtx Execution context that should perform the step. 
     * @param stepType Type of step to execute.
     */
    void enqueueStep(IRunControl.IExecutionDMC execCtx, IRunControl.StepType stepType);
    
    /**
     * Adds an instruction step command to the execution queue for given 
     * context.
     * @param execCtx Execution context that should perform the step. 
     * @param stepType Type of step to execute.
     */
    void enqueueInstructionStep(IRunControl.IExecutionDMC execCtx, IRunControl.StepType stepType);
}
