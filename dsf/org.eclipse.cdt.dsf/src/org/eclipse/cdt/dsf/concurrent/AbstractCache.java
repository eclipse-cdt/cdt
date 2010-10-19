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
 * 
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
     * Called while holding a lock to "this".  No new request will start until
     * this call returns.
     */
    @ThreadSafe
    abstract protected void canceled();
    
    public boolean isValid() {
        return fValid;
    }

    public V getData() {
        return fData;
    }
    
    public IStatus getStatus() {
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
    
    private void doSet(V data, IStatus status, boolean valid) {
        assert fExecutor.getDsfExecutor().isInExecutorThread();
        
        fData = data;
        fStatus = status;
        fValid = valid;
 
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
     
    private void completeWaitingRm(RequestMonitor rm) {
        if (rm instanceof DataRequestMonitor<?>) {
            @SuppressWarnings("unchecked")
            DataRequestMonitor<V> drm = (DataRequestMonitor<V>)rm;
            drm.setData(fData);
        }
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
            if (/*found && */!waiting) {
                canceled();
            }
        }

        // If we have no clients waiting anymore, cancel the request
        if (found) {
            // We no longer need to listen to cancelations.
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
	 * Resets the cache, setting its data to <code>null</code>, and status to
	 * {@link #INVALID_STATUS}. Equivalent to reset(null, INVALID_STATUS)
	 * 
	 * @see #reset(Object, IStatus)
	 */
    protected void reset() {
        reset(null, INVALID_STATUS);
    }

	/**
	 * Resets the cache, setting its data to [data], and status to [status].
	 * Resetting the cache puts it in the invalid state and cancels any current
	 * pending requests to the data source.
	 * 
	 * <p>
	 * The cache should be reset when the data source has issued an event
	 * indicating that the source data has changed but data may still be
	 * retrieved. Clients may need to re-request data following a cache reset.
	 * 
	 * @param data
	 *            The data that should be returned to any client that calls
	 *            {@link #getData()} despite the invalid state
	 * @status The status that should be returned to any client that calls
	 *         {@link #getStatus()()} despite the invalid state
	 * @see #reset()
	 * @see #set(Object, IStatus)
	 */ 
    protected void reset(V data, IStatus status) { 
        doSet(data, status, false); 
    }

	/**
	 * Puts the cache into the valid state, given it new data and status.
	 * 
	 * This method should be called when the subclass has received a response
	 * for updated data from the source. Note that such a response may be an
	 * error. That does not make the cache invalid. Invalid strictly means that
	 * the cache's data has either gone stale or that it's in the initial unset
	 * state.
	 * 
	 * @param data
	 *            The data that should be returned to any clients waiting for
	 *            cache data and for clients requesting data, until the cache is
	 *            invalidated via one of the reset methods.
	 * @status The status that should be returned to any clients waiting for
	 *         cache data and for clients requesting status, until the cache is
	 *         invalidated via one of the reset methods.
	 * 
	 * @see #reset()
	 * @see #reset(Object, IStatus)
	 */ 
    protected void set(V data, IStatus status) {
        doSet(data, status, true); 
    }
} 
