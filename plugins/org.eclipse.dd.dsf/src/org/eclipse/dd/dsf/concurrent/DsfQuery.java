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

import java.util.concurrent.Future;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.DsfPlugin;

/**
 * A convenience class that allows a client to retrieve data from services 
 * synchronously from a non-dispatch thread.  This class is different from
 * a Callable<V> in that it allows the implementation code to calculate
 * the result in several dispatches, rather than requiring it to return the 
 * data at end of Callable#call method.
 * 
 * @see java.util.concurrent.Callable
 * FIXME: make this class implement the Future<V> interface.
 */
abstract public class DsfQuery {
    
    private Object fResult;
    private boolean fValid;
    private DsfExecutor fExecutor;
    private Future fFuture;
    private boolean fWaiting;
    private IStatus fStatus = Status.OK_STATUS;
    
    public DsfQuery(DsfExecutor executor) {
        fExecutor = executor;
    }
    
    /**
     * Start data retrieval.
     * Client must implement this method to do whatever is needed to retrieve data.
     * Retrieval can be (but does not have to be) asynchronious - it meas this method can return
     * before data is retrieved. When data is ready Proxy must be notified by calling done() method.
     */ 
    protected abstract void execute();
    
    /**
     * Allows deriving classes to implement their own snipped additional 
     * cancellation code. 
     */
    protected void revokeChildren(Object result) {};
        
    /**
     * Get data associated with this proxy. This method is thread safe and 
     * it will block until data is ready.  Because it's a blocking call and it waits
     * for commands to be processed on the dispatch thread, this methods itself 
     * CANNOT be called on the dispatch thread.
     */
    public synchronized Object get() {
        assert !fExecutor.isInExecutorThread();
        if(!fValid) {
            if (!fWaiting) {
                fFuture = fExecutor.submit(new DsfRunnable() {
                    public void run() {
                        // TODO: not sure if this try-catch is desirable.  It might encourage
                        // implementors to not catch its own exceptions.  If the query code takes
                        // more than one dispatch, then this code will not be helpful anyway.
                        try {
                            DsfQuery.this.execute();
                        } catch (Throwable t) {
                            doneException(t);
                        }
                    }
                });
            } 

            fWaiting = true;
            try {
                while(fWaiting) {
                    wait();
                }
            } catch (InterruptedException e) {
                fStatus = new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, -1,
                                     "Interrupted exception while waiting for result.", e);
                fValid = true;
            }
            assert fValid;
        }
        return fResult;
    }

    /**
     * Same as get(), but with code to automatically re-threw the exception if one
     * was reported by the run() method.
     */
    public Object getWithThrows() throws CoreException {
    	Object retVal = get();
        if (!getStatus().isOK()) {
            throw new CoreException(getStatus());
        }
        return retVal;
    }

    public IStatus getStatus() { return fStatus; }
    
    /** Abort current operation and keep old proxy data */
    public synchronized void cancel() {
        assert fExecutor.isInExecutorThread();
        assert !fWaiting || !fValid;
        if (fWaiting) {
            fFuture.cancel(false);
            fWaiting = false;
            notifyAll();
        } else if (fValid) {
            revokeChildren(fResult);
        }
        fValid = true;
    }
    
    /** Abort current operation and set proxy data to 'result' */
    public synchronized void cancel(Object newResult) {
        fResult = newResult;
        cancel();
    }

    public Object getCachedResult() {
        return fResult;
    }
    
    public boolean isValid() { return fValid; }
    
    public synchronized void done(Object result) {
        // Valid could be true if request was cancelled while data was 
        // being retrieved, and then done() was called.
        if (fValid) return;

        fResult = result;
        fValid = true;
        if (fWaiting) {
            fWaiting = false;
            notifyAll();
        }
    }
    
    public synchronized void doneError(IStatus errorStatus) {
        if (fValid) return;
        fStatus = errorStatus;
        done(null);
    }
    
    public synchronized void doneException(Throwable t) {
        if (fValid) return;
        doneError(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, -1,
                             "Exception while computing result.", t));
    }        
}

