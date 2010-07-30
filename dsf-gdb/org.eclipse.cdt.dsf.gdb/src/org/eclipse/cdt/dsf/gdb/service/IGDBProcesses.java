/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Ericsson - added support for core-awareness
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;

public interface IGDBProcesses extends IMIProcesses {
	
    /**
     * This interface extends the DSF ThreadDMData to provide
     * the cores on which a process or a thread is located as well
     * as the owner of the process.
     * 
	 * @since 4.0
	 */
    public interface IGdbThreadDMData extends IThreadDMData {
    	/**
    	 * @return The list of identifiers of the cores on which the thread
    	 *         or process is currently located.  A thread will typically
    	 *         be located on a single core at a time, while a process will
    	 *         be located on all cores on which one of the process' threads
    	 *         is located.  Returns null if the information is not available.
    	 */
        String[] getCores();
        
        /**
         * @return The owner of the process, usually a user ID.  Returns null if the
         *         information is not available.  For threads, this method can return
         *         null or the owner of the parent process, if available.    
         */
        String getOwner();
    }
    
    /**
     * Get a list of all execution contexts belonging to a container.  This call is synchronous,
     * unlike the call to getProcessesBeingDebugged().  However, some services may not be able
     * to fulfill this request synchronously and will have to rely on getProcessesBeingDebugged().
     *
     * @param containerDmc The container for which we want to get the execution contexts
     */
    IMIExecutionDMContext[] getExecutionContexts(IMIContainerDMContext containerDmc);

}
