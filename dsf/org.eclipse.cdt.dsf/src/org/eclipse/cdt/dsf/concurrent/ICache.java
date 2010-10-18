/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import org.eclipse.core.runtime.IStatus;

/** 
 * The interface for a general purpose cache that caches the result of a single 
 * request.  Implementations need to provide the logic to fetch data from an
 * asynchronous data source.
 * <p>
 * This cache requires an executor to use.  The executor is used to synchronize 
 * access to the cache state and data.   
 * </p>
 * @since 2.2
 */ 
@ConfinedToDsfExecutor("getExecutor()")
public interface ICache<V> {
    
    /**
     * The executor that must be used to access this cache.
     */
    public DsfExecutor getExecutor();
        
    /**
     * Returns the current data value held by this cache.  Clients should first
     * call isValid() to determine if the data is up to date.
     */
    public V getData();
    
    /**
     * Returns the status of the source request held by this cache.  Clients 
     * should first call isValid() to determine if the data is up to date.
     */    
    public IStatus getStatus();

    /**
     * Wait for the cache to become valid.  If the cache is valid already, the
     * request returns immediately, otherwise data will first be retrieved from the 
     * source. 
     * 
     * @param rm RequestMonitor that is called when cache becomes valid.   
     */
    public void wait(RequestMonitor rm);
    
    /** 
     * Request data from the cache.  The cache is valid, it will complete the 
     * request immediately, otherwise data will first be retrieved from the 
     * source. 
     * 
     * @param rm RequestMonitor that is called when cache becomes valid.   
     */ 
    public void request(DataRequestMonitor<V> rm);
    
    /**
     * Returns <code>true</code> if the cache is currently valid.  I.e. 
     * whether the cache can return a value immediately without first 
     * retrieving it from the data source. 
     */
    public boolean isValid();
}
