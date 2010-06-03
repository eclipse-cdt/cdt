/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.concurrent;

import java.util.concurrent.Executor;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/**
 * Counting multi data request monitor that takes a <code>IViewerUpdate</code> 
 * as a parent. If the IViewerUpdate is canceled, this request monitor becomes 
 * canceled as well. 
 * 
 * @see IViewerUpdate.
 * 
 * @since 1.0
 */
public class ViewerCountingRequestMonitor extends CountingRequestMonitor {

    private final IViewerUpdate fUpdate;
    public ViewerCountingRequestMonitor(Executor executor, IViewerUpdate update) {
        super(executor, null);
        fUpdate = update;
    }
    
    @Override
    public synchronized boolean isCanceled() { 
        // isCanceled() is called implicitly by the super-constructor before fUpdate
        // is initialized.  The fUpdate != null is here to protect against an NPE 
        // from that.
        return (fUpdate != null && fUpdate.isCanceled()) || super.isCanceled();
    }
    
    @Override
    protected void handleSuccess() {
        fUpdate.done();
    }

    @Override
    protected void handleErrorOrWarning() {
        fUpdate.setStatus(getStatus());
        fUpdate.done();
    }
    
    @Override
    protected void handleCancel() {
        fUpdate.setStatus(getStatus());
        fUpdate.done();
    }
}
