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

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.dd.dsf.DsfPlugin;

/**
 * Utility class to collect multiple request monitor results of commands
 * that are initiated simultaneously.  The usage is as follows:
 * <pre>
 *     final MultiRequestMonitor multiRequestMon = new MultiRequestMonitor(fExecutor, null) { 
 *         public void handleCompleted() {
 *             System.out.println("All complete, errors=" + !getStatus().isOK());
 *         }
 *     };
 *     
 *     for (int i = 0; i < 10; i++) {
 *         service.call(i, multiRequestMon.addRequestMonitor(
 *             new RequestMonitor(fExecutor, null) {
 *                 public void handleCompleted() {
 *                     System.out.println(Integer.toString(i) + " complete");
 *                     multiRequestMon.requestMonitorDone(this);
 *                }
 *             }));
 *     }
 * </pre>
 */
public class CountingRequestMonitor extends RequestMonitor {
    private int fDoneCounter;

    public CountingRequestMonitor(DsfExecutor executor, RequestMonitor parentRequestMonitor) {
        super(executor, parentRequestMonitor);
        setStatus(new MultiStatus(DsfPlugin.PLUGIN_ID, 0, "Collective status for set of sub-operations.", null)); //$NON-NLS-1$
    }

    public void setCount(int count) {
        fDoneCounter = count;
    }

    @Override
    public synchronized void done() {
        fDoneCounter--;
        if (fDoneCounter <= 0) {
            super.done();
        }
    }
    
    @Override
    public String toString() {
        return "Multi-RequestMonitor: " + getStatus().toString(); //$NON-NLS-1$
    }
}
