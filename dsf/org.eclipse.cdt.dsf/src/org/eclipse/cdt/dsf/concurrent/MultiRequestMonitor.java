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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.MultiStatus;

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
public class MultiRequestMonitor<V extends RequestMonitor> extends RequestMonitor {
    private List<V> fRequestMonitorList = new LinkedList<V>();
    private Map<V,Boolean> fStatusMap = new HashMap<V,Boolean>();
    private int fDoneCounter;

    public MultiRequestMonitor(Executor executor, RequestMonitor parentRequestMonitor) {
        super(executor, parentRequestMonitor);
        setStatus(new MultiStatus(DsfPlugin.PLUGIN_ID, 0, "Collective status for set of sub-operations.", null)); //$NON-NLS-1$
    }

    /** 
     * Adds a new RequestMonitor callback to this tracker's list. 
     * @param <T> Client-specific class of the RequestMonitor callback, it's used here to avoid an 
     * unnecessary cast by the client.
     * @param rm Request monitor object to add to the tracker
     * @return The request monitor that was just added, it allows this method to be used 
     * inlined in service method calls
     */
    public <T extends V> T add(T rm) {
        assert !fStatusMap.containsKey(rm);
        fRequestMonitorList.add(rm);
        fStatusMap.put(rm, false);
        fDoneCounter++;
        return rm;
    }
    
    /**
     * Marks the given RequestMonitor callback as completed.  Client implementations of 
     * the RequestMonitor callback have to call this method in order for the tracker
     * to complete.
     * <br>
     * @param requestMonitor
     */
    public synchronized void requestMonitorDone(V requestMonitor) {
        if (getStatus() instanceof MultiStatus) {
            ((MultiStatus)getStatus()).merge(requestMonitor.getStatus());
        }
        assert fStatusMap.containsKey(requestMonitor);
        fStatusMap.put(requestMonitor, true);
        assert fDoneCounter > 0;
        fDoneCounter--;
        if (fDoneCounter == 0) {
            assert !fStatusMap.containsValue(false);
            super.done();
        }
    }    

    /**
     * Returns the list of requested monitors, sorted in order as they were added.
     */
    public List<V> getRequestMonitors() {
        return fRequestMonitorList;
    }

    /**
     * Returns true if given monitor is finished.
     */
    public boolean isRequestMonitorDone(V rm) {
        return fStatusMap.get(rm);
    }
    
    @Override
    public String toString() {
        return "Multi-RequestMonitor: " + getStatus().toString(); //$NON-NLS-1$
    }
}
