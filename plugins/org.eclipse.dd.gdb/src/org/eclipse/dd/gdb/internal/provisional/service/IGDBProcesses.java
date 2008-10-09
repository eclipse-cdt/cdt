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
package org.eclipse.dd.gdb.internal.provisional.service;

import org.eclipse.dd.mi.service.IMIContainerDMContext;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.dd.mi.service.IMIProcesses;

public interface IGDBProcesses extends IMIProcesses {
    /**
     * Get a list of all execution contexts belonging to a container.  This call is synchronous,
     * unlike the call to getProcessesBeingDebugged().  However, some services may not be able
     * to fulfill this request synchronously and will have to rely on getProcessesBeingDebugged().
     *
     * @param containerDmc The container for which we want to get the execution contexts
     */
    IMIExecutionDMContext[] getExecutionContexts(IMIContainerDMContext containerDmc);

}
