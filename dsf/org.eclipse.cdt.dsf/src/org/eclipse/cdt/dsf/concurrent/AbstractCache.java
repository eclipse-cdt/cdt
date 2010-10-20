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
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A base implementation of a general purpose cache. Sub classes must implement
 * {@link #retrieve(DataRequestMonitor)} to fetch data from the data source.
 * Sub-classes are also responsible for calling {@link #set(Object, IStatus)}
 * and {@link #reset()} to manage the state of the cache in response to events
 * from the data source.
 * <p>
 * This cache requires an executor to use. The executor is used to synchronize
 * access to the cache state and data.
 * </p>
 * @since 2.2
 */ 
@ConfinedToDsfExecutor("fExecutor") 
public abstract class AbstractCache<V> implements ICache<V> { 

    private static final IStatus INVALID_STATUS = new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Cache invalid", null); //$NON-NLS-1$
    
    private class RequestCanceledListener implements RequestMonitor.ICanceledListener {
        public void requestCanceled(final RequestMonitor canceledRm) {
            fExecutor.execute(new Runnable() {
                public void run() {
                    handleCanceledRm(canceledRm);
                }
            });
        }
    };

    private RequestCanceledListener fRequestCanceledListener = new RequestCanceledListener(); 
    
    private boolean fValid; 
    
    private V fData;
    private IStatus fStatus = INVALID_STATUS;
 
    @ThreadSafe
    private Object fWaitingList; 

    private final ImmediateInDsfExecutor fExecutor;
    
    public AbstractCache(ImmediateInDsfExecutor executor) {
        fExecutor = executor;
    }

    public DsfExecutor getExecutor() {
        return fExecutor.getDsfExecutor();
    }
    
    protected ImmediateInDsfExecutor getImmediateInDsfExecutor() {
        return fExecutor;
    }
    
	/**
	 * Sub-classes should override this method to retrieve the cache data from
	 * its source. The implementation should call {@link #set(Object, IStatus)}
	 * to store the newly retrieved data when it arrives (or an error, if one
	 * occurred retrieving the data)
	 * 
	 * @param rm
	 *            Request monitor for completion of data retrieval.
	 */ 
    abstract protected void retrieve();

    
    /**
     * Called to cancel a retrieve request.  This method is called when 
     * clients of the cache no longer need data that was requested. <br>
     * Sub-classes should cancel and clean up requests to the asynchronous 
     * data source. 
     * 
     * <p>
     * Note: Called while holding a lock to "this".  No new request will start until
     * this call returns. 
     * </p> 
     */
    @ThreadSafe
    abstract protected void canceled();
    
    public boolean isValid() {
        return fValid;
    }

    public V getData() {
        if (!fValid) {
            throw new IllegalStateException("Cache is not valid.  Cache data can be read only when cache is valid."); //$NON-NLS-1$
        }
        return fData;
    }
    
    public IStatus getStatus() {
        if (!fValid) {
            throw new IllegalStateException("Cache is not valid.  Cache status can be read only when cache is valid."); //$NON-NLS-1$
        }
        return fStatus;
    }
    
    public void update(RequestMonitor rm) { 
        assert fExecutor.getDsfExecutor().isInExecutorThread();

        if (!fValid) {
            boolean first = false;
            synchronized (this) {
                if (fWaitingList == null) {
                    first = true;
                    fWaitingList = rm;
                } else if (fWaitingList instanceof RequestMonitor[]) {
                    RequestMonitor[] waitingList = (RequestMonitor[])fWaitingList;
                    int waitingListLength = waitingList.length;
                    int i;
                    for (i = 0; i < waitingListLength; i++) {
                        if (waitingList[i] == null) {
                            waitingList[i] = rm;
                            break;
                        }
                    }
                    if (i == waitingListLength) {
                        RequestMonitor[] newWaitingList = new RequestMonitor[waitingListLength + 1];
                        System.arraycopy(waitingList, 0, newWaitingList, 0, waitingListLength);
                        newWaitingList[waitingListLength] = rm;
                        fWaitingList = newWaitingList;
                    }
                } else {
                    RequestMonitor[] newWaitingList = new RequestMonitor[2];
                    newWaitingList[0] = (RequestMonitor)fWaitingList;
                    newWaitingList[1] = rm;
                    fWaitingList = newWaitingList;
                }
            }            
            rm.addCancelListener(fRequestCanceledListener);
            if (first) {
                retrieve();
            }
        } else {
            rm.setStatus(fStatus); 
            rm.done(); 
        }
    } 
    
    private void completeWaitingRm(RequestMonitor rm) {
        rm.setStatus(fStatus); 
        rm.removeCancelListener(fRequestCanceledListener);
        rm.done(); 
    }
    
    private void handleCanceledRm(final RequestMonitor rm) {

        boolean found = false;
        boolean waiting = false;
        synchronized (this) {
            if (rm.equals(fWaitingList)) {
                found = true;
                waiting = false;
                fWaitingList = null;
            } else if(fWaitingList instanceof RequestMonitor[]) {
                RequestMonitor[] waitingList = (RequestMonitor[])fWaitingList;
                for (int i = 0; i < waitingList.length; i++) {
                    if (!found && rm.equals(waitingList[i])) {
                        waitingList[i] = null;
                        found = true;
                    }
                    waiting = waiting || waitingList[i] != null;
                }
            }
            if (found && !waiting) {
                canceled();
            }
        }

        // If we have no clients waiting anymore, cancel the request
        if (found) {
            // We no longer need to listen to cancellations.
            rm.removeCancelListener(fRequestCanceledListener);
            rm.setStatus(Status.CANCEL_STATUS);
            rm.done();
        }
        
    }

    @ThreadSafe
    protected boolean isCanceled() {
        boolean canceled;
        List<RequestMonitor> canceledRms = null;
        synchronized (this) {
            if (fWaitingList instanceof RequestMonitor && ((RequestMonitor)fWaitingList).isCanceled()) {
                canceledRms = new ArrayList<RequestMonitor>(1);
                canceledRms.add((RequestMonitor)fWaitingList); 
                fWaitingList = null;
            } else if(fWaitingList instanceof RequestMonitor[]) {
                boolean waiting = false;
                RequestMonitor[] waitingList = (RequestMonitor[])fWaitingList;
                for (int i = 0; i < waitingList.length; i++) {
                    if (waitingList[i] != null && waitingList[i].isCanceled()) {
                        if (canceledRms == null) {
                            canceledRms = new ArrayList<RequestMonitor>(1);
                        }
                        canceledRms.add( waitingList[i] );
                        waitingList[i] = null;
                    }
                    waiting = waiting || waitingList[i] != null;
                }
                if (!waiting) {
                    fWaitingList = null;
                }
            }            
            canceled = fWaitingList == null;
        }
        if (canceledRms != null) {
            for (RequestMonitor canceledRm : canceledRms) {
                canceledRm.setStatus(Status.CANCEL_STATUS);
                canceledRm.removeCancelListener(fRequestCanceledListener);
                canceledRm.done();
            }
        }
        
        return canceled;
    }
    
    /**
     * Resets the cache with a data value <code>null</code> and an error 
     * status with code {@link IDsfStatusConstants#INVALID_STATE}.
     * 
     * @see #reset(Object, IStatus)
     */
    protected void reset() {
        if (!fValid) {
            throw new IllegalStateException("Cache is not valid.  Cache can be reset only when it's in a valid state"); //$NON-NLS-1$
        }
        fValid = false;
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
    protected void set(V data, IStatus status) {
        assert fExecutor.getDsfExecutor().isInExecutorThread();
        
        fData = data;
        fStatus = status;
        fValid = true;
 
        Object waiting = null;
        synchronized(this) {
            waiting = fWaitingList;
            fWaitingList = null;
        }
        if (waiting != null) { 
            if (waiting instanceof RequestMonitor) {
                completeWaitingRm((RequestMonitor)waiting);
            } else if (waiting instanceof RequestMonitor[]) {
                RequestMonitor[] waitingList = (RequestMonitor[])waiting;
                for (int i = 0; i < waitingList.length; i++) {
                    if (waitingList[i] != null) {
                        completeWaitingRm(waitingList[i]);
                    }
                }
            }
            waiting = null;
        }
    }
} 
