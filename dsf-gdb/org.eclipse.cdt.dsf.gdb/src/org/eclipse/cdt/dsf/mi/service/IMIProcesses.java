/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * This interface provides a method for creating execution contexts.
 * @since 1.1
 */
public interface IMIProcesses extends IProcesses {
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
	 * Create an execution context.
	 *
	 * @param containerDmc The parent process debugging context
	 * @param threadDmc The parent thread context
	 * @param threadId The thread  id of the thread
	 */
	IMIExecutionDMContext createExecutionContext(IContainerDMContext containerDmc, IThreadDMContext threadDmc,
			String threadId);

	/**
	 * Create a container context.
	 *
	 * @param processDmc The parent process context of this context
	 * @param groupId The thread group id of the process
	 */
	IMIContainerDMContext createContainerContext(IProcessDMContext processDmc, String groupId);

	/**
	 * Create a container context based on a threadId.  This implies knowledge
	 * of which threads belong to which container.  This method can only be used
	 * if the threadId has been already created.
	 *
	 * @param controlDmc The parent command control context of this context
	 * @param threadId The thread id belonging to the container we want to create
	 */
	IMIContainerDMContext createContainerContextFromThreadId(ICommandControlDMContext controlDmc, String threadId);

	/**
	 * Create a container context based on a groupId.  This implies knowledge
	 * of which pid is represented by the groupId.  This method can only be used
	 * if the groupId has been already created.
	 *
	 * @param controlDmc The parent command control context of this context
	 * @param groupId The thread-group id of the container we want to create
	 * @since 4.0
	 */
	IMIContainerDMContext createContainerContextFromGroupId(ICommandControlDMContext controlDmc, String groupId);
}
