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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.dd.dsf.DsfPlugin;

/**
 * Utility class to collect multiple done (callback) results of commands
 * that are initiated simultaneously.  The usage is as follows:
 * <pre>
 *     final DoneCollector doneCollector = new DoneCollector() { 
 *         public void run() {
 *             System.out.println("All complete, errors=" + !getStatus().isOK());
 *         }
 *     };
 *     for (int i = 0; i < 10; i++) {
 *         service.call(i, doneCollector.addDone(new Done() {
 *             public void run() {
 *                 System.out.println(Integer.toString(i) + " complete");
 *                 doneCollector.doneDone(this);
 *             }
 *         }));
 *     }
 * </pre>
 */
public abstract class DoneCollector<V extends Done> extends Done {
    private Map<V,Boolean> fDones = new HashMap<V,Boolean>();
    private int fDoneCounter;

    /**
     * No-arg constructor.  
     * <br>
     * Note: this runnable will be executed following 
     * execution of the last done, and in the same dispatch loop. 
     *
     */
    public DoneCollector() {
        setStatus(new MultiStatus(DsfPlugin.PLUGIN_ID, 0, "Collective status for set of sub-operations.", null)); //$NON-NLS-1$
    }
    
    /** 
     * Adds a new Done callback to this tracker's list. 
     * @param <T> Client-specific class of the Done callback, it's used here to avoid an 
     * unnecessary cast by the client.
     * @param done callback object to add to the tracker
     * @return the done that was just added, it allows this method to be used 
     * inlined in service method calls
     */
    public <T extends V> T add(T done) {
        assert !fDones.containsKey(done);
        fDones.put(done, false);
        fDoneCounter++;
        return done;
    }
    
    /**
     * Marks the given Done callback as completed.  Client implementations of 
     * the Done callback have to call this method in order for the tracker
     * to complete.
     * <br>
     * Note: funniest method signature ever! 
     * @param done
     */
    public void doneDone(V done) {
        ((MultiStatus)getStatus()).merge(done.getStatus());
        assert fDones.containsKey(done);
        fDones.put(done, true);
        assert fDoneCounter > 0;
        fDoneCounter--;
        if (fDoneCounter == 0) {
            assert !fDones.containsValue(false);
            run();
        }
    }    
    
    /**
     * Returns the map of Done callbacks.  Access to this data is provided
     * in case overriding classes need access to the collected data in the
     * done callbacks.
     * @return map of the done callbacks.
     */
    public Map<V,Boolean> getDones() { return fDones; }
    
    @Override
    public String toString() {
        return "Done Collector: " + getStatus().toString(); //$NON-NLS-1$
    }
    
    @Override
    protected boolean isExecutionRequired() {
        return false;
    }
}
