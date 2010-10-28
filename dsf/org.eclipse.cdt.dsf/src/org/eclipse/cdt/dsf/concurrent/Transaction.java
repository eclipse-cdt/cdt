/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;

/**
 * @since 2.2
 */
public abstract class Transaction<V> {

	/**
	 * The exception we throw when the client transaction logic asks us to
	 * validate a cache object that is stale (or has never obtained a value from
	 * the source)
	 */
    private static final InvalidCacheException INVALID_CACHE_EXCEPTION = new InvalidCacheException();
    
	/** The request object we've been given to set the transaction results in */
    private DataRequestMonitor<V> fRm;
    
    public static class InvalidCacheException extends Exception {
        private static final long serialVersionUID = 1L;
    }

	/**
	 * Kicks off the transaction. We'll either complete the request monitor
	 * immediately if all the data points the transaction needs are cached and
	 * valid, or we'll end up asynchronously completing the monitor if and when
	 * either (a) all the data points are available and up-to-date, or (b)
	 * obtaining them from the source encountered an error. Note that there is
	 * potential in (b) for us to never complete the monitor. If one or more
	 * data points are perpetually becoming stale, then we'll indefinitely wait
	 * for them to stabilize. The caller should cancel its request monitor in
	 * order to get us to stop waiting.
	 * 
	 * @param rm Request completion monitor.
	 */
    public void request(DataRequestMonitor<V> rm) {
        if (fRm != null) {
            assert fRm.isCanceled();
            fRm.done();
        }
        fRm = rm;
        assert fRm != null;
        execute();
    }

	/**
	 * The transaction logic--code that tries to synchronously make use of,
	 * usually, multiple data points that are normally obtained asynchronously.
	 * Each data point is represented by a cache object. The transaction logic
	 * must check the validity of each cache object just prior to using it
	 * (calling its getData()). It should do that check by calling one of our
	 * validate() methods. Those methods will throw InvalidCacheException if the
	 * cached data is invalid (stale, e.g.,) or CoreException if an error was
	 * encountered the last time it got data form the source. The exception will
	 * abort the transaction, but in the case of InvalidCacheException, we
	 * schedule an asynchronous call that will re-invoke the transaction
	 * logic once the cache object has been updated from the source.
	 * 
	 * @return the cached data if it's valid, otherwise an exception is thrown
	 * @throws InvalidCacheException
	 * @throws CoreException
	 */
    abstract protected V process() throws InvalidCacheException, CoreException;

	/**
	 * Method which invokes the transaction logic and handles any exception that
	 * may result. If that logic encounters a stale/unset cache object, then we
	 * simply do nothing. This method will be called again once the cache
	 * objects tell us it has obtained an updated value form the source.
	 */
    private void execute() {
        if (fRm.isCanceled()) {
            fRm.done();
            fRm = null;
            return;
        }
        
        try {
        	// Execute the transaction logic
            V data = process();
            
			// No exception means all cache objects used by the transaction
			// were valid and up to date. Complete the request
            fRm.setData(data);
            fRm.done();
            fRm = null;
        }
        catch (CoreException e) {
			// At least one of the cache objects encountered a failure obtaining
			// the data from the source. Complete the request.
            fRm.setStatus(e.getStatus());
            fRm.done();
            fRm = null;
        }
        catch (InvalidCacheException e) {
			// At least one of the cache objects was stale/unset. Keep the
			// request monitor in the incomplete state, thus leaving our client
			// "waiting" (asynchronously). We'll get called again once the cache
			// objects are updated, thus re-starting the whole transaction
			// attempt.
        }
    }

	/**
	 * Clients must call one of our validate methods prior to using (calling
	 * getData()) on data cache object.
	 * 
	 * @param cache
	 *            the object being validated
	 * @throws InvalidCacheException
	 *             if the data is stale/unset
	 * @throws CoreException
	 *             if an error was encountered getting the data from the source
	 */
    public void validate(ICache<?> cache) throws InvalidCacheException, CoreException {
        if (cache.isValid()) {
            if (!cache.getStatus().isOK()) {
                throw new CoreException(cache.getStatus());
            }
        } else {
			// Throw the invalid cache exception, but first ask the cache to
			// update itself from its source, and schedule a re-attempt of the
			// transaction logic to occur when the stale/unset cache has been
			// updated
            cache.update(new RequestMonitor(ImmediateExecutor.getInstance(), fRm) {
                @Override
                protected void handleCompleted() {
                    execute();
                }
            });
            throw INVALID_CACHE_EXCEPTION;
        }
    }

    /**
     * See {@link #validate(RequestCache)}. This variant simply validates
     * multiple cache objects.
     */
    public void validate(ICache<?> ... caches) throws InvalidCacheException, CoreException {
        validate(Arrays.asList(caches));
    }
    
	/**
	 * See {@link #validate(RequestCache)}. This variant simply validates
	 * multiple cache objects.
	 */
    public void validate(Iterable<ICache<?>> caches) throws InvalidCacheException, CoreException {
        // Check if any of the caches have errors:
        boolean allValid = true;
        
        for (ICache<?> cache : caches) {
            if (cache.isValid()) {
                if (!cache.getStatus().isOK()) {
                    throw new CoreException(cache.getStatus());
                }
            } else {
                allValid = false;
            }
        }
        if (!allValid) {
			// Throw the invalid cache exception, but first schedule a
			// re-attempt of the transaction logic, to occur when the
			// stale/unset cache objects have been updated
            CountingRequestMonitor countringRm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), fRm) {
                @Override
                protected void handleCompleted() {
                    execute();
                }
            };
            int count = 0;
            for (ICache<?> cache : caches) {
                if (!cache.isValid()) {
                    cache.update(countringRm);
                    count++;
                }
            }
            countringRm.setDoneCount(count);
            throw INVALID_CACHE_EXCEPTION;
        }        
    }

}
