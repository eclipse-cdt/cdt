/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * A convenience class that allows a client to retrieve data from services 
 * synchronously from a non-dispatch thread.  This class is different from
 * a Callable<V> in that it allows the implementation code to calculate
 * the result in several dispatches, rather than requiring it to return the 
 * data at end of Callable#call method.
 * <p>
 * Usage:<br/>
 * <pre>
 *     class DataQuery extends Query<Data> {
 *         protected void execute(DataRequestMonitor<Data> rm) {
 *             rm.setData(fSlowService.getData());
 *             rm.done();
 *         }
 *     }
 *     
 *     DsfExecutor executor = getExecutor();
 *     DataQuery query = new DataQuery();
 *     executor.submit(query);
 *     
 *     try {
 *         Data data = query.get();
 *     }
 *     
 * </pre>
 * <p> 
 * @see java.util.concurrent.Callable
 * 
 * @since 1.0
 */
@ThreadSafe
abstract public class Query<V> extends DsfRunnable 
    implements Future<V> 
{
    private class QueryRm extends DataRequestMonitor<V> {

        boolean fExecuted = false;
        
        boolean fCompleted = false;
        
        private QueryRm() { 
            super(ImmediateExecutor.getInstance(), null);
        }
        
        @Override
        public synchronized void handleCompleted() {
            fCompleted = true;
            notifyAll();
        }
    
        public synchronized boolean isCompleted() {
            return fCompleted;
        }
        
        public synchronized boolean setExecuted() {
            if (fExecuted || isCanceled()) {
                // already executed or canceled
                return false;
            } 
            fExecuted = true;
            return true;
        }
        
        public synchronized boolean isExecuted() {
            return fExecuted;
        }
    };
    
    private final QueryRm fRm = new QueryRm();
    
    /** 
     * The no-argument constructor 
     */
    public Query() {}

    public V get() throws InterruptedException, ExecutionException { 
        IStatus status;
        V data;
        synchronized (fRm) {
            while (!isDone()) {
                fRm.wait();
            }
            status = fRm.getStatus();
            data = fRm.getData();
        }
        
        if (status.getSeverity() == IStatus.CANCEL) {
            throw new CancellationException();
        } else if (status.getSeverity() != IStatus.OK) {
            throw new ExecutionException(new CoreException(status));
        }
        return data;
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long timeLeft = unit.toMillis(timeout);
        long timeoutTime = System.currentTimeMillis() + unit.toMillis(timeout);

        IStatus status;
        V data;
        synchronized (fRm) {
            while (!isDone()) {
                if (timeLeft <= 0) {
                    throw new TimeoutException();
                }
                fRm.wait(timeLeft);
                timeLeft = timeoutTime - System.currentTimeMillis();
            }
            status = fRm.getStatus();
            data = fRm.getData();
        }
        
        if (status.getSeverity() == IStatus.CANCEL) {
            throw new CancellationException();
        } else if (status.getSeverity() != IStatus.OK) {
            throw new ExecutionException(new CoreException(status));
        }
        return data;
    }

    /**
     * Don't try to interrupt the DSF executor thread, just ignore the request 
     * if set.
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean completed = false;
        synchronized (fRm) {
            completed = fRm.isCompleted();
            if (!completed) {
                fRm.cancel();
                fRm.notifyAll();
            }
        }
        return !completed; 
    }

    public boolean isCancelled() { return fRm.isCanceled(); }

    public boolean isDone() {
        synchronized (fRm) {
            // If future is canceled, return right away.
            return fRm.isCompleted() || fRm.isCanceled();
        }
    }
    
    
    abstract protected void execute(DataRequestMonitor<V> rm);
    
    public void run() {
        if (fRm.setExecuted()) {
            execute(fRm);
        }
    }

    /**
     * Completes the query with the given exception.
     * 
     * @deprecated Query implementations should call the request monitor to 
     * set the exception status directly.
     */
    @Deprecated
    protected void doneException(Throwable t) {
        fRm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Exception", t)); //$NON-NLS-1$
        fRm.done();
    }        

    
}

