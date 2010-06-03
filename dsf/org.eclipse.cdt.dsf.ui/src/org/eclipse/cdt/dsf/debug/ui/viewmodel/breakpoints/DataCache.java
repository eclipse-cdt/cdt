package org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints;

/******************************************************************************* 
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others. 
 * All rights reserved. This program and the accompanying materials  
 * are made available under the terms of the Eclipse Public License v1.0  
 * which accompanies this distribution, and is available at  
 * http://www.eclipse.org/legal/epl-v10.html  
 *   
 * Contributors: 
 *     Wind River Systems - initial API and implementation 
 *******************************************************************************/ 
 
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/** 
 * A general purpose cache, which caches the result of a single request.
 * Sub classes need to implement {@link #retrieve(DataRequestMonitor)} to fetch
 * data from the data source.  Clients are responsible for calling 
 * {@link #disable()} and {@link #reset()} to manage the state of the cache in 
 * response to events from the data source.
 * <p>
 * This cache requires an executor to use.  The executor is used to synchronize 
 * access to the cache state and data.   
 * </p>
 * <p>
 * This class is intended as a general utility, but it's not quite ready for 
 * API, so it's just private class for now.
 * </p>
 * @since 2.1 
 */ 
@ConfinedToDsfExecutor("fExecutor") 
abstract class DataCache<V> { 

    final private Executor fExecutor; 
     
    private boolean fValid; 
     
    protected DataRequestMonitor<V> fRm;
    private V fData;
    private IStatus fStatus;
 
    private List<DataRequestMonitor<V>> fWaitingList = new LinkedList<DataRequestMonitor<V>>(); 

    public DataCache(Executor executor) { 
        fExecutor = executor; 
    } 
     
    
    /** 
     * Sub-classes should override this method to retrieve the cache data 
     * from its source. 
     * 
     * @param rm Request monitor for completion of data retrieval.
     */ 
    protected abstract void retrieve(DataRequestMonitor<V> rm); 

    /**
     * Returns <code>true</code> if the cache is currently valid.  I.e. 
     * whether the cache can return a value immediately without first 
     * retrieving it from the data source. 
     */
    public boolean isValid() {
        return fValid;
    }

    /**
     * Returns <code>true</code> if the cache is currently waiting for data
     * from the data source.
     */
    public boolean isPending() {
        return fRm != null;
    }
    
    /**
     * Returns the current data value held by this cache.  Clients should first
     * call isValid() to determine if the data is up to date.
     */
    public V getData() {
        return fData;
    }
    
    /**
     * Returns the status of the source request held by this cache.  Clients 
     * should first call isValid() to determine if the data is up to date.
     */
    public IStatus getStatus() {
        return fStatus;
    }
    
    /** 
     * Request data from the cache.  The cache is valid, it will complete the 
     * request immediately, otherwise data will first be retrieved from the 
     * source. 
     * @param req 
     */ 
    public void request(final DataRequestMonitor<V> rm) { 
        if (!fValid) {
            boolean first = fWaitingList.isEmpty();
            fWaitingList.add(rm); 
        	if(first) {
        	    fRm = new DataRequestMonitor<V>(fExecutor, null) { 
	                @Override 
	                protected void handleCompleted() {
	                    if (!isCanceled()) {
    	                    fValid = true;
    	                    fRm = null;
    	                    set(getData(), getStatus()); 
	                    }
	                } 
	            }; 
	            retrieve(fRm);
        	}
        } else { 
            rm.setData(fData); 
            rm.setStatus(fStatus); 
            rm.done(); 
        } 
    } 
     
    
    private void set(V data, IStatus status) { 
        fData = data;
        fStatus = status;
        List<DataRequestMonitor<V>> waitingList = fWaitingList;  
        fWaitingList = new LinkedList<DataRequestMonitor<V>>(); 
 
        for (DataRequestMonitor<V> rm : waitingList) { 
            rm.setData(data); 
            rm.setStatus(status); 
            rm.done(); 
        } 
    } 
     
    /**
     * Resets the cache with a data value <code>null</code> and an error 
     * status with code {@link IDsfStatusConstants#INVALID_STATE}.
     * 
     * @see #reset(Object, IStatus)
     */
    public void reset() {
        reset(null, new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Cache reset", null)); //$NON-NLS-1$
    }
    
    /** 
     * Resets the cache with given data and status.  Resetting the cache 
     * forces the cache to be invalid and cancels any current pending requests
     * from data source.  
     * <p>
     * This method should be called when the data source has issued an event 
     * indicating that the source data has changed but data may still be 
     * retrieved.  Clients may need to re-request data following cache reset.
     * </p>
     * @param data The data that should be returned to any clients currently 
     * waiting for cache data.
     * @status The status that should be returned to any clients currently 
     * waiting for cache data. 
     */ 
    public void reset(V data, IStatus status) { 
        fValid = false; 
        if (fRm != null) { 
            fRm.cancel(); 
            fRm = null; 
        } 
        set(data, status); 
    } 

    /**
     * Disables the cache from retrieving data from the source.  If the cache 
     * is already valid the data and status is retained.  If the cache is not 
     * valid, then data value <code>null</code> and an error status with code 
     * {@link IDsfStatusConstants#INVALID_STATE} are set. 
     * 
     * @see #disable(Object, IStatus)
     */
    public void disable() {
        //TODO: better.
        
        V data;
        IStatus status;
        if (fValid) {
            data = getData();
            status = getStatus();
        } else {
            data = null;
            status = new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Cache disable", null); //$NON-NLS-1$
        }
        disable(data, status);
    }

    /** 
     * Resets the cache then disables it.  When a cache is disabled it means 
     * that it is valid and requests to the data source will not be sent.
     * <p>
     * This method should be called when the data source has issued an event 
     * indicating that the source data has changed and future requests for 
     * data will return the given data and status.  Once the source data 
     * becomes available again, clients should call {@link #reset()}.
     * </p>
     * @param data The data that should be returned to any clients waiting for 
     * cache data and for clients requesting data until the cache is reset again.
     * @status The status that should be returned to any clients waiting for 
     * cache data and for clients requesting data until the cache is reset again.
     * 
     * @see #reset(Object, IStatus)
     */ 
    public void disable(V data, IStatus status) {
        reset(data, status);
        fValid = true;
    }
} 
