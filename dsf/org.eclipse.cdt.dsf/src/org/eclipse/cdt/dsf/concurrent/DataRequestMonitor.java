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

import java.util.concurrent.Executor;


/**
 * Request monitor that allows data to be returned to the request initiator.
 * 
 * @param V The type of the data object that this monitor handles. 
 * 
 * @since 1.0
 */
public class DataRequestMonitor<V> extends RequestMonitor {

    /** Data object reference */
    private V fData; 
    
    public DataRequestMonitor(Executor executor, RequestMonitor parentRequestMonitor) {
        super(executor, parentRequestMonitor);
    }

    /** 
     * Sets the data object to specified value.  To be called by the 
     * asynchronous method implementor.
     * @param data Data value to set.
     */
    public synchronized void setData(V data) { fData = data; }
    
    /**
     * Returns the data value, null if not set.
     */
    public synchronized V getData() { return fData; }
    
    @Override
    public String toString() { 
        if (getData() != null) {
            return getData().toString();
        } else {
            return super.toString();
        }
    }    
}
