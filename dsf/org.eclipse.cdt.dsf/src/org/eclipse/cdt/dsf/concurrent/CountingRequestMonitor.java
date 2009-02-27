/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
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

import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * Utility class to collect multiple request monitor results of commands
 * that are initiated simultaneously.  The usage is as follows:
 * <code><pre>
 *     final CountingRequestMonitor countingRm = new CountingRequestMonitor(fExecutor, null) { 
 *         public void handleCompleted() {
 *             System.out.println("All complete, errors=" + !getStatus().isOK());
 *         }
 *     };
 *     
 *     int count = 0;
 *     for (int i : elements) {
 *         service.call(i, countingRm);
 *         count++;
 *     }
 *     
 *     countingRm.setDoneCount(count);
 * </pre></code>
 * 
 * @since 1.0
 */
public class CountingRequestMonitor extends RequestMonitor {
    /**
     * Counter tracking the remaining number of times that the done() method
     * needs to be called before this request monitor is actually done.
     */
    private int fDoneCounter;
    
    /**
     * Flag indicating whether the initial count has been set on this monitor.
     */
    private boolean fInitialCountSet = false;

    public CountingRequestMonitor(Executor executor, RequestMonitor parentRequestMonitor) {
        super(executor, parentRequestMonitor);
        super.setStatus(new MultiStatus(DsfPlugin.PLUGIN_ID, 0, "Collective status for set of sub-operations.", null)); //$NON-NLS-1$
    }

    /**
     * Sets the number of times that this request monitor needs to be called 
     * before this monitor is truly considered done.  This method must be called
     * exactly once in the life cycle of each counting request monitor.
     * @param count Number of times that done() has to be called to mark the request
     * monitor as complete.  If count is '0', then the counting request monitor is 
     * marked as done immediately.
     */
    public synchronized void setDoneCount(int count) {
        assert !fInitialCountSet;
        fInitialCountSet = true;
        fDoneCounter += count;
        if (fDoneCounter <= 0) {
            assert fDoneCounter == 0; // Mismatch in the count.
            super.done();
        }
    }

    /**
     * Called to indicate that one of the calls using this monitor is finished.
     * Only when <code>done</done> is called the number of times corresponding to the 
     * count, the request monitor will be considered complete.  This method can be 
     * called before {@link #setDoneCount(int)}.
     */
    @Override
    public synchronized void done() {
        fDoneCounter--;
        if (fInitialCountSet && fDoneCounter <= 0) {
            assert fDoneCounter == 0; // Mismatch in the count.
            super.done();
        }
    }
    
    @Override
    public String toString() {
        return "CountingRequestMonitor: " + getStatus().toString(); //$NON-NLS-1$
    }
    
    @Override
    public synchronized void setStatus(IStatus status) {
        if ((getStatus() instanceof MultiStatus)) {
            ((MultiStatus)getStatus()).add(status);
        }
    };
}
