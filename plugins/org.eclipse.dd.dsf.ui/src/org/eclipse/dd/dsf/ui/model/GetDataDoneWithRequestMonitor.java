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
package org.eclipse.dd.dsf.ui.model;

import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;

/**
 * Convenience extension to GetDataDone, which handles notifying the 
 * IAsynchronousRequestMonitor when the done is executed.  
 * @param <V> Class type of data.
 */
@SuppressWarnings("restriction")
public abstract class GetDataDoneWithRequestMonitor<V> extends GetDataDone<V> {
    /** Monitor to be posted when this done is executed. */
    private IAsynchronousRequestMonitor fMonitor;
    
    /** Constructor requires the monitor */
    public GetDataDoneWithRequestMonitor(IAsynchronousRequestMonitor monitor) {
        fMonitor = monitor;
    }
    
    /**
     * Run method checks the request monitor for cancellation and checks this 
     * done's status before calling doRun().  Finally it takes care of 
     * notifying the request montior that request is finished.
     */
    public final void run() {
        if (fMonitor.isCanceled()) return;
        if (!getStatus().isOK()) {
            fMonitor.setStatus(getStatus());
        } else {
            doRun();
        }
        fMonitor.done();
    }

    /**
     * Method to perform the actual work.  It should not call monitor.done(),
     * because it will be called by this class in run().
     */
    protected abstract void doRun();
}