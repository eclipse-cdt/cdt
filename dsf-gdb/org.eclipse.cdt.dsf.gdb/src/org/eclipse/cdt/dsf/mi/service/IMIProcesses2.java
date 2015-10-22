/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * This interface provides a method for creating execution contexts.
 * @since 4.9
 */
public interface IMIProcesses2 extends IMIProcesses
{
	/**
	 * Create a process context.
	 * 
	 * @param dmc The parent context
	 * @param pid The OS Id of the process
	 */
    IProcessDMContext createProcessContext(IDMContext dmc, String pid);

    /**
     * Create a container context based on a threadId.  This implies knowledge
     * of which threads belong to which container.  This method can only be used
     * if the threadId has been already created.
     * 
     * @param dmc The parent context
     * @param threadId The thread id belonging to the container we want to create
     */
    IMIContainerDMContext createContainerContextFromThreadId(IDMContext dmc, String threadId);

    /**
     * Create a container context based on a groupId.  This implies knowledge
     * of which pid is represented by the groupId.  This method can only be used
     * if the groupId has been already created.
     * 
     * @param dmc The parent context
     * @param goupId The thread-group id of the container we want to create
     */
    IMIContainerDMContext createContainerContextFromGroupId(IDMContext dmc, String groupId);

}

