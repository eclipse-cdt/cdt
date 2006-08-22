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
    public enum StateChangeReason { UNKNOWN, USER_REQUEST, STEP, BREAKPOINT, EXCEPTION, CONTAINER };
        
    /**
     * Events signaling a state changes.
     */
    public interface ISuspendedEvent extends IDataModelEvent<IExecutionDMC> {
        StateChangeReason getReason();
    }
    public interface IResumedEvent extends IDataModelEvent<IExecutionDMC> {
        StateChangeReason getReason();
    }
    public interface IContainerSuspendedEvent extends IDataModelEvent<IExecutionDMC> {
        StateChangeReason getReason();
    }
    public interface IContainerResumedEvent extends IDataModelEvent<IExecutionDMC> {
        StateChangeReason getReason();
    }
    
    /**
     * Indicates that a new execution context (thread) was started.  The DMC 
     * for the event is the container of the new exec context.
     */
    public interface IStartedEvent extends IDataModelEvent<IContainerDMC> {
        IExecutionDMC getExecutionContext();
    }

    /**
     * Indicates that an execution context has exited.  As in the started event, 
     * the DMC for the event is the container of the exec context.
     */
    public interface IExitedEvent extends IDataModelEvent<IContainerDMC> {
        IExecutionDMC getExecutionContext();
    }

    /**
     * Display information for an execution context.
     */
    public interface IExecutionData extends IDataModelData {
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
    boolean canStep(IExecutionDMC context);
    void step(IExecutionDMC context, StepType stepType, Done done);
    boolean canInstructionStep(IExecutionDMC context);
    void instructionStep(IExecutionDMC context, StepType stepType, Done done);
}
