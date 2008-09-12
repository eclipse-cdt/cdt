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
    
    /**
     * Retrieve the groupId to which this threadId belongs
     * 
     * @param threadId The ID of the thread
     * @return The ID of the group to which the specified thread belongs
     */
    String getExecutionGroupIdFromThread(String threadId);
    
    /**
     * This method should be called when a new thread is created.  It allows
     * to keep track of the thread to group relationship.
     * 
     * @param threadId The ID of the new thread
     * @param groupId The ID of the group to which the new thread belongs
     */
    void addThreadId(String threadId, String groupId);

    /**
     * This method should be called when a thread exits.  It is meant
     * to remove the thread to group entry.
     * 
     * @param threadId The ID of the thread that exited
     */
    void removeThreadId(String threadId);
}

