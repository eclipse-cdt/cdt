/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import java.util.Collections;
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
 *     multiReqMon.requireDoneAdding();
 *     
 *     for (int i = 0; i < 10; i++) {
 *         service.call(i, multiRequestMon.add(
 *             new RequestMonitor(fExecutor, null) {
 *                 public void handleCompleted() {
 *                     System.out.println(Integer.toString(i) + " complete");
 *                     multiRequestMon.requestMonitorDone(this);
 *                }
 *             }));
 *     }
 *     
 *     multiReqMon.doneAdding();
 * </pre>
 * 
 * @since 1.0
 */
public class MultiRequestMonitor<V extends RequestMonitor> extends RequestMonitor {
    private List<V> fRequestMonitorList = Collections.synchronizedList(new LinkedList<V>());
    private Map<V,Boolean> fStatusMap = Collections.synchronizedMap(new HashMap<V,Boolean>());
    private int fDoneCounter;

	/**
	 * Has client called {@link #requireDoneAdding()}? Should be set right
	 * after construction so no need to synchronize
	 */
    private boolean fRequiresDoneAdding;
    
	/**
	 * Has client called {@link #doneAdding()}?
	 */
	private boolean fDoneAdding;

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
    public synchronized <T extends V> T add(T rm) {
        assert !fStatusMap.containsKey(rm);
        if (fDoneAdding) {
        	throw new IllegalStateException("Can't add a monitor after having called doneAdding()"); //$NON-NLS-1$
        }
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
    public void requestMonitorDone(V requestMonitor) {
    	// Avoid holding object lock while calling our super's done()
    	boolean callSuperDone = false;

    	synchronized (this) {
	        if (getStatus() instanceof MultiStatus) {
	            ((MultiStatus)getStatus()).merge(requestMonitor.getStatus());
	        }
	        
	        if (!fStatusMap.containsKey(requestMonitor)) {
	        	throw new IllegalArgumentException("Unknown monitor."); //$NON-NLS-1$
	        }
	        
	        fStatusMap.put(requestMonitor, true);
	        assert fDoneCounter > 0;
	        fDoneCounter--;
	        if (fDoneCounter == 0 && (fDoneAdding || !fRequiresDoneAdding)) {
	            assert !fStatusMap.containsValue(false);
	            callSuperDone = true;
	        }
    	}

    	if (callSuperDone) {
    		super.done();
    	}
    }

	/**
	 * Returns the list of requested monitors, sorted in order as they were
	 * added. The returned list is a copy.
	 */
    public List<V> getRequestMonitors() {
    	synchronized (fRequestMonitorList) { // needed while copying, even when list is a synchronized collection
    		return new LinkedList<V>(fRequestMonitorList);
    	}
    }

    /**
     * Returns true if given monitor is finished.
     */
    public boolean isRequestMonitorDone(V rm) {
        return fStatusMap.get(rm);
    }

	/**
	 * In order to avoid a theoretical (but unlikely) race condition failure,
	 * clients should call this method immediately after creating the monitor.
	 * Doing so will require the client to use {@link #doneAdding()} to indicate
	 * it has finished adding monitors via {@link #add(RequestMonitor)}
	 * 
	 * @since 2.1
	 */
    public void requireDoneAdding() {
    	fRequiresDoneAdding = true;
    }

	/**
	 * Client should call this after it has finished adding monitors to this
	 * MultiRequestMonitor. The client must have first called
	 * {@link #requireDoneAdding()}, otherwise an {@link IllegalStateException}
	 * is thrown
	 * 
	 * @since 2.1
	 */
    public void doneAdding() {
    	// Avoid holding object lock while calling our super's done().
    	boolean callSuperDone = false;
    	
    	synchronized (this) {
	    	if (!fRequiresDoneAdding) {
	    		throw new IllegalStateException("The method requiresDoneAdding() must be called first"); //$NON-NLS-1$
	    	}
	    	fDoneAdding = true;
	    	
	    	// In theory, the added monitors may have already completed.
	        if (fDoneCounter == 0) {
	            assert !fStatusMap.containsValue(false);
	            callSuperDone = true;
	        }
    	}
    	
    	if (callSuperDone) {
    		super.done();
    	}
    }
    
    @Override
    public String toString() {
        return "Multi-RequestMonitor: " + getStatus().toString(); //$NON-NLS-1$
    }
}
