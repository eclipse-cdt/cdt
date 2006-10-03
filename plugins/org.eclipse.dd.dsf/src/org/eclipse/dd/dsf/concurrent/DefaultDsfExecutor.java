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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.DsfPlugin;

/**
 * Default implementation of a DSF executor interfaces, based on the 
 * standard java.util.concurrent.ThreadPoolExecutor.
 */

public class DefaultDsfExecutor extends ScheduledThreadPoolExecutor 
    implements DsfExecutor 
{
    /** Thread factory that creates the single thread to be used for this executor */
    static class DsfThreadFactory implements ThreadFactory {
        Thread fThread;
        public Thread newThread(Runnable r) {
            assert fThread == null;  // Should be called only once.
            fThread = new Thread(new ThreadGroup("DSF Thread Group"), r, "DSF Dispatch Thread", 0); 
            return fThread;
        }
    }

    public DefaultDsfExecutor() {
        super(1, new DsfThreadFactory());
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            // If tracing, pre-start the dispatch thread, and add it to the map.
            prestartAllCoreThreads();
            fThreadToExecutorMap.put(((DsfThreadFactory)getThreadFactory()).fThread, DefaultDsfExecutor.this);
        }
    }
    
    public boolean isInExecutorThread() {
        return Thread.currentThread().equals( ((DsfThreadFactory)getThreadFactory()).fThread );
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (r instanceof Future) {
            Future future = (Future)r;
            try {
                future.get();
            } catch (InterruptedException e) { // Ignore
            } catch (CancellationException e) { // Ignore also
            } catch (ExecutionException e) {
                if (e.getCause() != null) {
                    DsfPlugin.getDefault().getLog().log(new Status(
                        IStatus.ERROR, DsfPlugin.PLUGIN_ID, -1, "Uncaught exception in DSF executor thread", e.getCause()));
                    
                    // Print out the stack trace to console if assertions are enabled. 
                    if(ASSERTIONS_ENABLED) {
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream(512);
                        PrintStream printStream = new PrintStream(outStream);
                        try {
                            printStream.write("Uncaught exception in session executor thread: ".getBytes());
                        } catch (IOException e2) {}
                        e.getCause().printStackTrace(new PrintStream(outStream));
                        System.err.println(outStream.toString());
                    }
                }
            }
        }
    }

    
    //
    // Utilities used for tracing.
    //
    static boolean DEBUG_EXECUTOR = false;
    static boolean ASSERTIONS_ENABLED = false;
    static {
        DEBUG_EXECUTOR = DsfPlugin.DEBUG && "true".equals( //$NON-NLS-1$
            Platform.getDebugOption("org.eclipse.dd.dsf/debug/executor")); //$NON-NLS-1$
        assert ASSERTIONS_ENABLED = true;
    }  

    /** 
     * This map is used by DsfRunnable/DsfQuery/DsfCallable to track by which executor
     * an executable object was created.
     * <br>Note: Only used when tracing. 
     */
    static Map<Thread, DefaultDsfExecutor> fThreadToExecutorMap = new HashMap<Thread, DefaultDsfExecutor>();
    
    /** 
     * Currently executing runnable/callable.
     * <br>Note: Only used when tracing. 
     */
    TracingWrapper fCurrentlyExecuting;
    
    /** 
     * Counter number saved by each tracing runnable when executed 
     * <br>Note: Only used when tracing. 
     */
    int fSequenceCounter;

    /** 
     * Wrapper for runnables/callables, is used to store tracing information 
     * <br>Note: Only used when tracing. 
     */
    abstract class TracingWrapper {
        /** Sequence number of this runnable/callable */
        int fSequenceNumber = -1; 
        
        /** Trace of where the runnable/callable was submitted to the executor */
        StackTraceElement[] fSubmittedAt = null; 
        
        /** Reference to the runnable/callable that submitted this runnable/callable to the executor */
        TracingWrapper fSubmittedBy = null;

        /**
         * @param offset the number of items in the stack trace not to be printed
         */
        TracingWrapper(int offset) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            fSubmittedAt = new StackTraceElement[stackTrace.length - offset]; 
            System.arraycopy(stackTrace, offset - 1, fSubmittedAt, 0, fSubmittedAt.length);
            if (isInExecutorThread() &&  fCurrentlyExecuting != null) {
                fSubmittedBy = fCurrentlyExecuting;
            }
        }
        
        void traceExecution() {
            fSequenceNumber = fSequenceCounter++;
            fCurrentlyExecuting = this;

            // Write to console only if tracing is enabled (as opposed to tracing or assertions).
            if (DEBUG_EXECUTOR) {
                StringBuilder traceBuilder = new StringBuilder();
    
                // Record the time
                long time = System.currentTimeMillis();
                long seconds = (time / 1000) % 1000;
                if (seconds < 100) traceBuilder.append('0');
                if (seconds < 10) traceBuilder.append('0');
                traceBuilder.append(seconds);
                traceBuilder.append(',');
                long millis = time % 1000;
                if (millis < 100) traceBuilder.append('0');
                if (millis < 10) traceBuilder.append('0');
                traceBuilder.append(millis);
                traceBuilder.append(' ');
    
                // Record the executor #
                traceBuilder.append('#');
                traceBuilder.append(fSequenceNumber);
                traceBuilder.append(' ');
    
                // Append executable class name
                traceBuilder.append(getExecutable().getClass().getName());
                if (getExecutable() instanceof DsfExecutable) {
                    DsfExecutable dsfExecutable = (DsfExecutable)getExecutable(); 
                    if (dsfExecutable.fCreatedAt != null || dsfExecutable.fCreatedBy != null) {
                        traceBuilder.append("\n        Created  ");
                        if (dsfExecutable.fCreatedBy != null) {
                            traceBuilder.append(" by #");
                            traceBuilder.append(dsfExecutable.fCreatedBy.fSequenceNumber);
                        }
                        if (dsfExecutable.fCreatedAt != null) {
                            traceBuilder.append(" at ");
                            traceBuilder.append(dsfExecutable.fCreatedAt[0].toString());
                        }   
                    }
                }
    
                // Submitted info
                traceBuilder.append("\n        ");
                traceBuilder.append("Submitted");
                if (fSubmittedBy != null) {
                    traceBuilder.append(" by #");
                    traceBuilder.append(fSubmittedBy.fSequenceNumber);
                }
                traceBuilder.append(" at ");
                traceBuilder.append(fSubmittedAt[0].toString());
                
                // Finally, the executable's toString().
                traceBuilder.append("\n        ");
                traceBuilder.append(getExecutable().toString());
                
                // Finally write out to console
                DsfPlugin.debug(traceBuilder.toString());
            }
        }

        abstract protected Object getExecutable();
    }
    
    class TracingWrapperRunnable extends TracingWrapper implements Runnable {
        final Runnable fRunnable;
        public TracingWrapperRunnable(Runnable runnable, int offset) {
            super(offset);
            fRunnable = runnable;
        }

        protected Object getExecutable() { return fRunnable; }
        
        public void run() {
            traceExecution();
            fRunnable.run(); 
        }
    }
    
    public class TracingWrapperCallable<T> extends TracingWrapper implements Callable<T> {
        final Callable<T> fCallable;
        public TracingWrapperCallable(Callable<T> callable, int offset) {
            super(offset);
            fCallable = callable;
        }

        protected Object getExecutable() { return fCallable; }

        public T call() throws Exception {
            traceExecution();
            return fCallable.call();
        }
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            if ( !(callable instanceof TracingWrapper) ) {
                callable = new TracingWrapperCallable<V>(callable, 6);
            }
        }
        return super.schedule(callable, delay, unit);
    }
     @Override
     public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
         if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
             if ( !(command instanceof TracingWrapper) ) {
                 command = new TracingWrapperRunnable(command, 6);
             }
         }
         return super.schedule(command, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            command = new TracingWrapperRunnable(command, 6);
        }
        return super.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            command = new TracingWrapperRunnable(command, 6);
        }
        return super.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public void execute(Runnable command) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            command = new TracingWrapperRunnable(command, 6);
        }
        super.execute(command);
    }     
    
    @Override
    public Future<?> submit(Runnable command) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            command = new TracingWrapperRunnable(command, 6);
        }
        return super.submit(command);
    }
    
    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            callable = new TracingWrapperCallable<T>(callable, 6);
        }
        return super.submit(callable);
    }
    
    @Override
    public <T> Future<T> submit(Runnable command, T result) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            command = new TracingWrapperRunnable(command, 6);
        }
        return super.submit(command, result);
    }
}
