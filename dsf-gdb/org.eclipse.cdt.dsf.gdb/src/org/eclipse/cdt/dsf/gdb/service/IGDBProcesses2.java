/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator.IGroupDMContext;

/**
 * This interface provides a method for creating execution contexts.
 * @since 4.8
 */
public interface IGDBProcesses2 extends IGDBProcesses
{
	/**
	 * Create a process context whose parent is a group context.
	 * 
	 * @param pid The OS Id of the process
	 */
    IProcessDMContext createProcessContext(IGroupDMContext groupDmc, String pid);
    
}

