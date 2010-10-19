package org.eclipse.cdt.dsf.concurrent;

/******************************************************************************* 
 * Copyright (c) 2008 Wind River Systems, Inc. and others. 
 * All rights reserved. This program and the accompanying materials  
 * are made available under the terms of the Eclipse Public License v1.0  
 * which accompanies this distribution, and is available at  
 * http://www.eclipse.org/legal/epl-v10.html  
 *   
 * Contributors: 
 *     Wind River Systems - initial API and implementation 
 *******************************************************************************/ 
 
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
 * @since 2.2
 */ 
@ConfinedToDsfExecutor("fExecutor") 
public abstract class RequestCache<V> extends AbstractCache<V> { 

    protected DataRequestMonitor<V> fRm;

    
    public RequestCache(ImmediateInDsfExecutor executor) {
        super(executor);
    }
    
    @Override
    protected final void retrieve() {
        // Make sure to cancel the previous rm.  This may lead to the rm being 
        // canceled twice, but that's not harmful.
        if (fRm != null) {
            fRm.cancel();
        }
        
        fRm = new DataRequestMonitor<V>(getImmediateInDsfExecutor(), null) {
            
            private IStatus fRawStatus = Status.OK_STATUS;
            
            @Override 
            protected void handleCompleted() {
                if (this == fRm) {
                    fRm = null;
                    IStatus status;
                    synchronized (this) {
                        status = fRawStatus;
                    }
                    set(getData(), status); 
                }
            } 
            
            @Override
            public synchronized void setStatus(IStatus status) {
                fRawStatus = status;
            };
            
            @Override
            public boolean isCanceled() {
                return super.isCanceled() || RequestCache.this.isCanceled();
            };
        }; 
        retrieve(fRm);
    }     

    /** 
     * Sub-classes should override this method to retrieve the cache data 
     * from its source. 
     * 
     * @param rm Request monitor for completion of data retrieval.
     */ 
    protected abstract void retrieve(DataRequestMonitor<V> rm); 

    @Override
    protected synchronized void canceled() {
        if (fRm != null) { 
            fRm.cancel(); 
        } 
    }    

    @Override
    protected void reset(V data, IStatus status) { 
        if (fRm != null) { 
            fRm.cancel(); 
            fRm = null; 
        } 
        super.reset(data, status);
    } 

    @Override
    protected void set(V data, IStatus status) {
        if (fRm != null) { 
            fRm.cancel(); 
            fRm = null; 
        } 
        super.set(data, status);
    }
} 
