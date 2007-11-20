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

import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
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
    /**
     * Amount of time in miliseconds, that it takes the ISteppingTimedOutEvent 
     * event to be issued after a step is started. 
     * @see ISteppingTimedOutEvent  
     */
    public final static int STEPPING_TIMEOUT = 500;
    
    /**
     * Indicates that the given context has been stepping for some time, 
     * and the UI (views and actions) may need to be updated accordingly. 
     */
    public interface ISteppingTimedOutEvent extends IDMEvent<IExecutionDMContext> {
    }


    void setStepQueueDepth(int depth);
    int getStepQueueDepth();
    
    /** 
     * Returns the number of step commands that are queued for given execution
     * context.
     */
    int getPendingStepCount(IExecutionDMContext ctx);

    /**
     * Checks whether a step command can be queued up for given context.
     */
    boolean canEnqueueStep(IExecutionDMContext execCtx);

    boolean canEnqueueInstructionStep(IExecutionDMContext ctx);

    /**
     * Adds a step command to the execution queue for given context.
     * @param execCtx Execution context that should perform the step. 
     * @param stepType Type of step to execute.
     */
    void enqueueStep(IExecutionDMContext ctx, IRunControl.StepType stepType);
    
    /**
     * Adds an instruction step command to the execution queue for given 
     * context.
     * @param execCtx Execution context that should perform the step. 
     * @param stepType Type of step to execute.
     */
    void enqueueInstructionStep(IExecutionDMContext ctx, IRunControl.StepType stepType);

    boolean isSteppingTimedOut(IExecutionDMContext context);
}
