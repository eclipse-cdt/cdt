/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service;

import org.eclipse.dd.dsf.debug.service.IProcesses;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * This interface provides a method for creating execution contexts.
 */
public interface IMIProcesses extends IProcesses
{
	/**
	 * Create a thread context.
	 * 
	 * @param processDmc The parent process context
	 * @param threadId The OS Id of the thread
	 */
    IThreadDMContext createThreadContext(IProcessDMContext processDmc, String threadId);

	/**
	 * Create a process context.
	 * 
	 * @param pid The OS Id of the process
	 */
    IProcessDMContext createProcessContext(ICommandControlDMContext controlDmc, String pid);
    
    /**
     * Create a execution context.
     * 
     * @param containerDmc The parent process debugging context
     * @param threadDmc The parent thread context
     * @param threadId The thread  id of the thread
     */
    IMIExecutionDMContext createExecutionContext(IContainerDMContext containerDmc, 
                                                 IThreadDMContext threadDmc, 
                                                 String threadId);

    /**
     * Create a executionGroup context.
     * 
     * @param processDmc The parent process context of this context
     * @param groupId The thread group id of the process
     */
    IMIExecutionGroupDMContext createExecutionGroupContext(IProcessDMContext processDmc,
    												       String groupId);
    
    String getExecutionGroupIdFromThread(String threadId);
}

