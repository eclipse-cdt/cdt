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

/**
 * Asynchronous method callback which returns a data object (in addition to
 * the base class's getStatus().  
 * @param <V>
 */
public abstract class GetDataDone<V> extends Done {
    /** Data object reference */
    private V fData; 
    
    /** 
     * Sets the data object to specified value.  To be called by the 
     * asynchronous method implementor.
     * @param data Data value to set.
     */
    public void setData(V data) { fData = data; }
    
    /**
     * Returns the data value, null if not set.
     */
    public V getData() { return fData; }
}
