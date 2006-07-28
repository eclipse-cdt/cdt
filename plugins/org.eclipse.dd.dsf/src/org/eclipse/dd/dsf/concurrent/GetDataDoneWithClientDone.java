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
package org.eclipse.dd.dsf.concurrent;

import org.eclipse.core.runtime.IStatus;

/**
 * Convenience extension to GetDataDone, which handles posting of the client's
 * <code>Done</code> upon the completion of this <code>GetDataDone</code>.
 * @param <V> Class type of data.
 */
public abstract class GetDataDoneWithClientDone<V> extends GetDataDone<V> {
    private DsfExecutor fExecutor;
    private Done fClientDone;
    
    /** 
     * Constructor requires the Done to be posted as well as the executor to
     * post it with.
     */
    public GetDataDoneWithClientDone(DsfExecutor executor, Done clientDone) {
        fExecutor = executor;
        fClientDone = clientDone;
    }

    /**
     * The run method checks the client done for cancellation, and this done
     * for errors.  It calls doRun() for the sub-class execution, and posts
     * the client done when finished. 
     */
    public final void run() {
        if (fClientDone.getStatus().getSeverity() == IStatus.CANCEL) return;
        if (!getStatus().isOK()) {
            fClientDone.setStatus(getStatus());
        } else {
            doRun();
        }
        fExecutor.execute(fClientDone);
    }

    /**
     * Method to perform the actual work.  It should not post the client done
     * because it will be posted by this class in run().
     */
    protected abstract void doRun();
}