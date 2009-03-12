/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
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
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import org.eclipse.core.runtime.CoreException;


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
    /** The synchronization object for this query */
    private final Sync fSync = new Sync();

    /** 
     * The no-argument constructor 
     */
    public Query() {}
    
    public V get() throws InterruptedException, ExecutionException { return fSync.doGet(); }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return fSync.doGet(unit.toNanos(timeout));
    }

    /**
     * Don't try to interrupt the DSF executor thread, just ignore the request 
     * if set.
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        return fSync.doCancel(); 
    }

    public boolean isCancelled() { return fSync.doIsCancelled(); }

    public boolean isDone() { return fSync.doIsDone(); }
    
    
    protected void doneException(Throwable t) {
        fSync.doSetException(t);
    }        
    
    abstract protected void execute(DataRequestMonitor<V> rm);
    
    public void run() {
        if (fSync.doRun()) {
            try {
                /*
                 * Create the executor which is going to handle the completion of the
                 * request monitor.  Normally a DSF executor is supplied here which
                 * causes the request monitor to be invoked in a new dispatch loop.
                 * But since the query is a synchronization object, it can handle
                 * the completion of the request in any thread.  
                 * Avoiding the use of a DSF executor is very useful because queries are
                 * meant to be used by clients calling from non-dispatch thread, and there
                 * is a chance that a client may execute a query just as a session is being
                 * shut down.  In that case, the DSF executor may throw a 
                 * RejectedExecutionException which would have to be handled by the query.
                 */
                execute(new DataRequestMonitor<V>(ImmediateExecutor.getInstance(), null) {
                    @Override
                    public void handleCompleted() {
                        if (isSuccess()) fSync.doSet(getData());
                        else fSync.doSetException(new CoreException(getStatus()));
                    }
                });
            } catch(Throwable t) {
                /*
                 * Catching the exception here will only work if the exception 
                 * happens within the execute.  It will not work in cases when 
                 * the execute submits other runnables, and the other runnables
                 * encounter the exception.
                 */ 
                fSync.doSetException(t);
                
                /*
                 * Since we caught the exception, it will not be logged by 
                 * DefaultDsfExecutable.afterExecution().  So log it here.
                 */
                DefaultDsfExecutor.logException(t);
            }
        }
    }
    
    @SuppressWarnings("serial")
    final class Sync extends AbstractQueuedSynchronizer {
        private static final int STATE_RUNNING = 1;
        private static final int STATE_DONE = 2;
        private static final int STATE_CANCELLED = 4;

        private V fResult;
        private Throwable fException;

        private boolean ranOrCancelled(int state) {
            return (state & (STATE_DONE | STATE_CANCELLED)) != 0;
        }

        @Override
        protected int tryAcquireShared(int ignore) {
            return doIsDone()? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int ignore) {
            return true; 
        }

        boolean doIsCancelled() {
            return getState() == STATE_CANCELLED;
        }
        
        boolean doIsDone() {
            return ranOrCancelled(getState());
        }

        V doGet() throws InterruptedException, ExecutionException {
            acquireSharedInterruptibly(0);
            if (getState() == STATE_CANCELLED) throw new CancellationException();
            if (fException != null) throw new ExecutionException(fException);
            return fResult;
        }

        V doGet(long nanosTimeout) throws InterruptedException, ExecutionException, TimeoutException {
            if (!tryAcquireSharedNanos(0, nanosTimeout)) throw new TimeoutException();                
            if (getState() == STATE_CANCELLED) throw new CancellationException();
            if (fException != null) throw new ExecutionException(fException);
            return fResult;
        }

        void doSet(V v) {
            while(true) {
                int s = getState();
                if (ranOrCancelled(s)) return;
                if (compareAndSetState(s, STATE_DONE)) break;
            }
            fResult = v;
            releaseShared(0);
        }

        void doSetException(Throwable t) {
            while(true) {
                int s = getState();
                if (ranOrCancelled(s)) return;
                if (compareAndSetState(s, STATE_DONE)) break;
            }
            fException = t;
            fResult = null;
            releaseShared(0);
        }

        boolean doCancel() {
            while(true) {
                int s = getState();
                if (ranOrCancelled(s)) return false;
                if (compareAndSetState(s, STATE_CANCELLED)) break;
            }
            releaseShared(0);
            return true;
        }

        boolean doRun() {
            return compareAndSetState(0, STATE_RUNNING); 
        }
    }
}

