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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Cache for retrieving ranges of elements from an asynchronous data source.  
 * Clients of this cache should call {@link #getRange(long, int)} to get a cache
 * for that given range of elements.  Sub-classes must implement {@link #retrieve(long, int, DataRequestMonitor)}
 * to retrieve data from the asynchronous data source.
 * @since 2.2
 */
abstract public class RangeCache<V> {
    
    private class Request extends RequestCache<List<V>> implements Comparable<Request> {
        long fOffset;
        int fCount;
        @Override
        protected void retrieve(DataRequestMonitor<java.util.List<V>> rm) {
            RangeCache.this.retrieve(fOffset, fCount, rm); 
        }
        
        Request(long offset, int count) {
            super(fExecutor);
            fOffset = offset;
            fCount = count;
        }
        
        public int compareTo(RangeCache<V>.Request o) {
            if (fOffset > o.fOffset) {
                return 1;
            } else if (fOffset == o.fOffset) {
                return 0;
            } else /*if (fOffset < o.fOffset)*/ {
                return -1;
            }
        }
        
        @Override
        public boolean equals(Object _o) {
            if (_o instanceof RangeCache<?>.Request) {
                RangeCache<?>.Request o = (RangeCache<?>.Request)_o;
                return fOffset == o.fOffset && fCount == o.fCount;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return (int)fOffset^fCount;
        }
        
        @Override
        public String toString() {
            return "" + fOffset + "(" + fCount + ") -> " + (fOffset + fCount); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    /**
     * This transaction class implements the main logic of the range cache. 
     * It examines the current requests held by the cache and and creates 
     * requests ones as needed.  Once the requests are all valid it returns
     * the completed data to the client.
     */
    private class RangeTransaction extends Transaction<List<V>> { 
        
        long fOffset;
        int fCount;

        RangeTransaction(long offset, int count) {
            fOffset = offset;
            fCount = count;
        }

        @Override
        protected List<V> process() throws InvalidCacheException, CoreException {
            clearCanceledRequests();
            
            List<ICache<?>> transactionRequests = new ArrayList<ICache<?>>(1);
            
            // Create a new request for the data to retrieve.
            Request current = new Request(fOffset, fCount);
            
            current = adjustRequestHead(current, transactionRequests);
            if (current != null) {
                current = adjustRequestTail(current, transactionRequests);
            }
            if (current != null) {
                transactionRequests.add(current);
                fRequests.add(current);
            }
                
            validate(transactionRequests);

            return makeElementsListFromRequests(transactionRequests);
        }
        
        
        // Adjust the beginning of the requested range of data.  If there 
        // is already an overlapping range in front of the requested range, 
        // then use it.
        private Request adjustRequestHead(Request request, List<ICache<?>> transactionRequests) {
            SortedSet<Request> headRequests = fRequests.headSet(request);
            if (!headRequests.isEmpty()) {
                Request headRequest = headRequests.last();
                long headEndOffset = headRequest.fOffset + headRequest.fCount;
                if (headEndOffset > fOffset) {
                    transactionRequests.add(headRequest);
                    request.fCount = (int)(request.fCount - (headEndOffset - fOffset));
                    request.fOffset = headEndOffset;
                }
            }
            if (request.fCount > 0) {
                return request;
            } else {
                return null;
            }
        }

        /**
         * Adjust the end of the requested range of data.
         * @param current
         * @param transactionRequests
         * @return
         */
        private Request adjustRequestTail(Request current, List<ICache<?>> transactionRequests) {
            // Create a duplicate of the tailSet, in order to avoid a concurrent modification exception.
            List<Request> tailSet = new ArrayList<Request>(fRequests.tailSet(current));
            
            // Iterate through the matching requests and add them to the requests list.
            for (Request tailRequest : tailSet) {
                if (tailRequest.fOffset < current.fOffset + fCount) {
                    // found overlapping request add it to list
                    if (tailRequest.fOffset <= current.fOffset) {
                        // next request starts off at the beginning of current request
                        transactionRequests.add(tailRequest);
                        current.fOffset = tailRequest.fOffset + tailRequest.fCount;
                        current.fCount = ((int)(fOffset - current.fOffset)) + fCount ;
                        if (current.fCount <= 0) {
                            return null;
                        }
                    } else {
                        current.fCount = (int)(tailRequest.fOffset - current.fOffset);
                        transactionRequests.add(current);
                        fRequests.add(current);
                        current = null;
                        transactionRequests.add(tailRequest);
                        long tailEndOffset = tailRequest.fOffset + tailRequest.fCount;
                        long rangeEndOffset = fOffset + fCount;
                        if (tailEndOffset >= rangeEndOffset) {
                            return null;
                        } else {
                            current = new Request(tailEndOffset, (int)(rangeEndOffset - tailEndOffset));
                        }
                    }
                } else {
                    break;
                }
            }
            return current;
        }
        
        private List<V> makeElementsListFromRequests(List<ICache<?>> requests) {
            List<V> retVal = new ArrayList<V>(fCount);
            long index = fOffset;
            long end = fOffset + fCount;
            int requestIdx = 0;
            while (index < end ) {
                @SuppressWarnings("unchecked")
                Request request = (Request)requests.get(requestIdx);
                if (index < request.fOffset + request.fCount) {
                    retVal.add( request.getData().get((int)(index - request.fOffset)) );
                    index ++;
                } else {
                    requestIdx++;
                }
            }
            return retVal;
        }
        
        private void clearCanceledRequests() {
            for (Iterator<Request> itr = fRequests.iterator(); itr.hasNext();) {
                Request request = itr.next();
                if (!request.isValid() && request.isCanceled()) {
                    itr.remove();
                }
            }
        }
    }

    private final ImmediateInDsfExecutor fExecutor;

    /**
     * Requests currently held by this cache.  The requests should be for 
     * non-overlapping ranges of elements.  
     */
    
    private SortedSet<Request> fRequests = new TreeSet<Request>(); 
    
    public RangeCache(ImmediateInDsfExecutor executor) {
        fExecutor = executor;
    }

    /**
     * Retrieves data from the data source. 
     * 
     * @param offset Offset in data range where the requested list of data should start.
     * @param count Number of elements requests.
     * @param rm Callback for the data.
     */
    protected abstract void retrieve(long offset, int count, DataRequestMonitor<List<V>> rm);
    
    /**
     * Returns a cache for the range of requested data.
     * 
     * @param offset Offset in data range where the requested list of data should start.
     * @param count Number of elements requests.
     * @return Cache object for the requested data.  
     */
    public ICache<List<V>> getRange(final long offset, final int count) {
        assert fExecutor.getDsfExecutor().isInExecutorThread();
        
        return new RequestCache<List<V>>(fExecutor) {
            @Override
            protected void retrieve(DataRequestMonitor<List<V>> rm) {
                new RangeTransaction(offset, count).request(rm);
            }
        };
    }
    
    /**
     * Sets the given list and status to the cache.  Subsequent range requests 
     * that fall in its the range will return the given data.  Requests outside 
     * of its range will trigger a call to {@link #retrieve(long, int, DataRequestMonitor)}.<br>
     * The given data parameter can be <code>null</code> if the given status 
     * parameter contains an error.  In this case all requests in the given 
     * range will return the error. 
     * 
     * @param offset Offset of the given data to set to cache.
     * @param count Count of the given data to set to cache.
     * @param data List of elements to set to cache.  Can be <code>null</code>. 
     * @param status Status object to set to cache.
     */
    protected void set(long offset, int count, List<V> data, IStatus status) {
        for (Request request : fRequests) {
            if (!request.isValid()) {
                request.set(null, Status.OK_STATUS);
            }
        }
        fRequests.clear();
        Request request = new Request(offset, count);
        request.set(data, status);
        fRequests.add(request);
    }
    
    /**
     * Forces the cache into an invalid state.  If there are any pending 
     * requests, their will continue and their results will be cached. 
     */
    protected void reset() {
        for (Iterator<Request> itr = fRequests.iterator(); itr.hasNext();) {
            Request request = itr.next();
            if (request.isValid()) {
                request.reset();
                itr.remove();
            }
        }
    }
}
