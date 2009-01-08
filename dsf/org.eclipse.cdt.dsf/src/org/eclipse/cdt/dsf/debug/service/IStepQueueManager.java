/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * @since 1.1
 */
@ConfinedToDsfExecutor("getSession().getExecutor()")
public interface IStepQueueManager {
    
    /**
     * Returns the session for which this step queue manager is used.
     */
    public DsfSession getSession();
    
	/**
	 * Checks whether a step command can be queued up for given context.
	 */
	public abstract void canEnqueueStep(IExecutionDMContext execCtx, StepType stepType, DataRequestMonitor<Boolean> rm);

	/**
	 * Adds a step command to the execution queue for given context.
	 * @param execCtx Execution context that should perform the step. 
	 * @param stepType Type of step to execute.
	 */
	public abstract void enqueueStep(final IExecutionDMContext execCtx, final StepType stepType);

}
