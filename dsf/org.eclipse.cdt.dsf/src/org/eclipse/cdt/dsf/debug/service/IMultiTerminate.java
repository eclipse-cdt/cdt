/*******************************************************************************
 * Copyright (c) 2014 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;

/**
 * This interface provides the ability to perform terminate on multiple contexts.
 * 
 * @since 2.6
 */
public interface IMultiTerminate {

    /**
     * Checks whether it is possible to terminate at least one of the specified threads 
     * or processes.
     * 
     * @param dmcs The contexts of the threads to terminate
     * @param rm Request monitor returning whether there is at least one thread or process can be terminated.
     */
    void canTerminateSome(IThreadDMContext[] dmcs, DataRequestMonitor<Boolean> rm);

    /**
     * Checks whether it is possible to terminate all of the specified threads or processes.
     * 
     * @param dmcs The contexts of the threads or processes to terminate
     * @param rm Request monitor returning whether all of the threads can be terminated.
     */
    void canTerminateAll(IThreadDMContext[] dmcs, DataRequestMonitor<Boolean> rm);

    /**
     * Request to terminate the specified threads or processes. Only threads and processes
     * that can be terminated will be affected, others will be ignored.
     * 
     * @param dmc The contexts of the threads or processes to terminate.
     */
    void terminate(IThreadDMContext[] dmcs, RequestMonitor rm);
}