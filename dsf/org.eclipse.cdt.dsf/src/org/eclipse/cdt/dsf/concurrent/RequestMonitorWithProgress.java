/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import java.util.concurrent.Executor;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A request monitor which uses a progress monitor as a parent.  When the parent
 * progress monitor is canceled, the request monitor will also be canceled, 
 * although the cancellation listeners will not be called.
 * 
 * @since 1.1
 */
public class RequestMonitorWithProgress extends RequestMonitor {

    private final IProgressMonitor fProgressMonitor;
    
    public RequestMonitorWithProgress(Executor executor, IProgressMonitor progressMonitor) {
        super(executor, null);
        fProgressMonitor = progressMonitor;
    }

    public IProgressMonitor getProgressMonitor() {
        return fProgressMonitor;
    }
    
    @Override
    public synchronized boolean isCanceled() {
        return super.isCanceled() || fProgressMonitor.isCanceled();
    }
}
