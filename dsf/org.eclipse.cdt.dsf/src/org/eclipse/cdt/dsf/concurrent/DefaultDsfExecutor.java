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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Default implementation of a DSF executor interfaces, based on the 
 * standard java.util.concurrent.ThreadPoolExecutor.
 * 
 * @since 1.0
 */

public class DefaultDsfExecutor extends ScheduledThreadPoolExecutor 
    implements DsfExecutor 
{
    /**
     * Instance counter for DSF executors.  Used in the executor's thread name.
     */
    private static int fgInstanceCounter = 0;
    
    /**
     * Name of the executor, used in the executor's thread name.
     */
    private String fName;
    
    /**
     * Instance number of this executor, used with the executor name.
     */
    private int fInstanceNumber;
    
    /** Thread factory that creates the single thread to be used for this executor */
    static class DsfThreadFactory implements ThreadFactory {
        private String fThreadName; 
        DsfThreadFactory(String name) {
            fThreadName = name;
        }
        
        Thread fThread;
        public Thread newThread(Runnable r) {
            assert fThread == null;  // Should be called only once.
            fThread = new Thread(new ThreadGroup(fThreadName), r, fThreadName, 0); 
            return fThread;
        }
    }

    public DefaultDsfExecutor() {
        this("DSF Executor"); //$NON-NLS-1$
    }
    
    /** 
     * Creates a new DSF Executor with the given name.
     * @param name Name used to create executor's thread.
     */
    public DefaultDsfExecutor(String name) {
        super(1, new DsfThreadFactory(name + " - " + fgInstanceCounter)); //$NON-NLS-1$
        fName = name;
        fInstanceNumber = fgInstanceCounter++;
        
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            // If tracing, pre-start the dispatch thread, and add it to the map.
            prestartAllCoreThreads();
            fThreadToExecutorMap.put(((DsfThreadFactory)getThreadFactory()).fThread, DefaultDsfExecutor.this);
        }
    }
    
    public boolean isInExecutorThread() {
        return Thread.currentThread().equals( ((DsfThreadFactory)getThreadFactory()).fThread );
    }

    protected String getName() { 
        return fName;
    }
    
    static void logException(Throwable t) {
        DsfPlugin plugin = DsfPlugin.getDefault();
        if (plugin == null) return;
        
        ILog log = plugin.getLog();
        if (log != null) {
            log.log(new Status(
                IStatus.ERROR, DsfPlugin.PLUGIN_ID, -1, "Uncaught exception in DSF executor thread", t)); //$NON-NLS-1$
        }                   
        // Print out the stack trace to console if assertions are enabled. 
        if(ASSERTIONS_ENABLED) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(512);
            PrintStream printStream = new PrintStream(outStream);
            try {
                printStream.write("Uncaught exception in session executor thread: ".getBytes()); //$NON-NLS-1$
            } catch (IOException e2) {}
            t.printStackTrace(new PrintStream(outStream));
            System.err.println(outStream.toString());
        }
    }
    
    //
    // Utilities used for tracing.
    //
    protected static boolean DEBUG_EXECUTOR = false;
    protected static String DEBUG_EXECUTOR_NAME = ""; //$NON-NLS-1$
    protected static boolean ASSERTIONS_ENABLED = false;
    static {
        DEBUG_EXECUTOR = DsfPlugin.DEBUG && "true".equals( //$NON-NLS-1$
            Platform.getDebugOption("org.eclipse.cdt.dsf/debug/executor")); //$NON-NLS-1$
        DEBUG_EXECUTOR_NAME = DsfPlugin.DEBUG 
            ? Platform.getDebugOption("org.eclipse.cdt.dsf/debug/executorName") : ""; //$NON-NLS-1$ //$NON-NLS-2$
        assert (ASSERTIONS_ENABLED = true) == true;
    }  

    /** 
     * This map is used by DsfRunnable/Query/DsfCallable to track by which executor
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
        StackTraceWrapper fSubmittedAt = null; 
        
        /** Reference to the runnable/callable that submitted this runnable/callable to the executor */
        TracingWrapper fSubmittedBy = null;

		/**
		 * @param stackTrace
		 *            the stack trace to log
		 * @param frameIgnoreCount
		 *            the number of items in [stackTrace] to ignore, starting at
		 *            the topmost frame (where the thread is currently
		 *            executing--stackTrace[0])
		 */
        TracingWrapper(StackTraceElement[] stackTrace, int frameIgnoreCount) {
        	// guard against the offset being greater than the stack trace
        	frameIgnoreCount = Math.min(frameIgnoreCount, stackTrace.length);
        	fSubmittedAt = new StackTraceWrapper(new StackTraceElement[stackTrace.length - frameIgnoreCount]);
            if (fSubmittedAt.fStackTraceElements.length > 0) {
	            System.arraycopy(stackTrace, frameIgnoreCount, fSubmittedAt.fStackTraceElements, 0, fSubmittedAt.fStackTraceElements.length);
            }
            
            if (isInExecutorThread() &&  fCurrentlyExecuting != null) {
                fSubmittedBy = fCurrentlyExecuting;
            }
        }
        
        void traceExecution() {
            fSequenceNumber = fSequenceCounter++;
            fCurrentlyExecuting = this;

            // Write to console only if tracing is enabled (as opposed to tracing or assertions).
            if (DEBUG_EXECUTOR && ("".equals(DEBUG_EXECUTOR_NAME) || fName.equals(DEBUG_EXECUTOR_NAME))) { //$NON-NLS-1$
                StringBuilder traceBuilder = new StringBuilder();
    
                // Record the time
                traceBuilder.append(DsfPlugin.getDebugTime());
                traceBuilder.append(' ');
    
                // Record the executor #
                traceBuilder.append('#');
                traceBuilder.append(fSequenceNumber);

                // Record the executor name
                traceBuilder.append('(');
                traceBuilder.append(fName);
                traceBuilder.append(" - "); //$NON-NLS-1$
                traceBuilder.append(fInstanceNumber);
                traceBuilder.append(')');
                traceBuilder.append(' ');

                // Append executable class name
                traceBuilder.append(getExecutable().getClass().getName());
                
                // Add executable's toString().
                traceBuilder.append("\n        "); //$NON-NLS-1$
                traceBuilder.append(getExecutable().toString());

                // Append "create by" info.
                if (getExecutable() instanceof DsfExecutable) {
                    DsfExecutable dsfExecutable = (DsfExecutable)getExecutable(); 
                    if (dsfExecutable.fCreatedAt != null || dsfExecutable.fCreatedBy != null) {
                        traceBuilder.append("\n            created  "); //$NON-NLS-1$
                        if (dsfExecutable.fCreatedBy != null) {
                            traceBuilder.append(" by #"); //$NON-NLS-1$
                            traceBuilder.append(dsfExecutable.fCreatedBy.fSequenceNumber);
                        }
                        if (dsfExecutable.fCreatedAt != null) {
                            traceBuilder.append("\n                      at "); //$NON-NLS-1$
                            traceBuilder.append(dsfExecutable.fCreatedAt.fStackTraceElements[0].toString());
                            for (int i = 1; i < dsfExecutable.fCreatedAt.fStackTraceElements.length && i < 3; i++) {
                                traceBuilder.append("\n                         "); //$NON-NLS-1$
                                traceBuilder.append(dsfExecutable.fCreatedAt.fStackTraceElements[i].toString());
                            }
                        }   
                    }
                }
    
                // Submitted info
                traceBuilder.append("\n            submitted"); //$NON-NLS-1$
                if (fSubmittedBy != null) {
                    traceBuilder.append(" by #"); //$NON-NLS-1$
                    traceBuilder.append(fSubmittedBy.fSequenceNumber);
                }
                traceBuilder.append("\n                      at "); //$NON-NLS-1$
                traceBuilder.append(fSubmittedAt.fStackTraceElements[0].toString());
                for (int i = 1; i < fSubmittedAt.fStackTraceElements.length && i < 3; i++) {
                    traceBuilder.append("\n                         "); //$NON-NLS-1$
                    traceBuilder.append(fSubmittedAt.fStackTraceElements[i].toString());
                }
                                
                // Finally write out to console
                DsfPlugin.debug(traceBuilder.toString());
            }
        }

        abstract protected Object getExecutable();
    }
    
    
    class TracingWrapperRunnable extends TracingWrapper implements Runnable {
        final Runnable fRunnable;
        
        
        public TracingWrapperRunnable(Runnable runnable, StackTraceElement[] stackTrace, int frameIgnoreCount) {
            super(stackTrace, frameIgnoreCount);
            if (runnable == null) throw new NullPointerException();
            fRunnable = runnable;

            // Check if executable wasn't executed already.
            if (DEBUG_EXECUTOR && fRunnable instanceof DsfExecutable) {
                assert !((DsfExecutable)fRunnable).getSubmitted() : "Executable was previously executed."; //$NON-NLS-1$
                ((DsfExecutable)fRunnable).setSubmitted();
            }
        }

        @Override
        protected Object getExecutable() { return fRunnable; }
        
        public void run() {
            traceExecution();

            // Finally invoke the runnable code.
            try {
                fRunnable.run();
            } catch (RuntimeException e) {
                // If an exception was thrown in the Runnable, trace it.  
                // Because there is no one else to catch it, it is a 
                // programming error.
                logException(e);
                throw e;
            }
        }
    }
    
    public class TracingWrapperCallable<T> extends TracingWrapper implements Callable<T> {
        final Callable<T> fCallable;
        
        /**
         * @deprecated use constructor that takes stack trace and ignore count
         */
        @Deprecated
		public TracingWrapperCallable(Callable<T> callable, int frameIgnoreCount) {
            super(new StackTraceElement[0], 0);
            if (callable == null) throw new NullPointerException();
            fCallable = callable;
        }
        
        /**
		 * @since 2.1
		 */
        public TracingWrapperCallable(Callable<T> callable, StackTraceElement[] stackTrace, int frameIgnoreCount) {
            super(stackTrace, frameIgnoreCount);
            if (callable == null) throw new NullPointerException();
            fCallable = callable;
        }

        @Override
        protected Object getExecutable() { return fCallable; }

        public T call() throws Exception {
            traceExecution();
            
            // Finally invoke the runnable code.
            // Note that callables can throw exceptions that can be caught
            // by clients that invoked them using ExecutionException.
            return fCallable.call();
        }
    }

	/**
	 * When recording a stack crawl to trace who has called a method in this
	 * class, we call {@link Thread#getStackTrace()} and ignore the top three
	 * frames. The two topmost frames are ones related to the
	 * {@link Thread#getStackTrace()} call itself, and the third is the method
	 * in this class that's been called.
	 */
    private static final int TRACE_FRAME_IGNORE_COUNT = 3;

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            if ( !(callable instanceof TracingWrapper) ) {
                callable = new TracingWrapperCallable<V>(callable, Thread.currentThread().getStackTrace(), TRACE_FRAME_IGNORE_COUNT);
            }
        }
        return super.schedule(callable, delay, unit);
    }
     @Override
     public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
         if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
             if ( !(command instanceof TracingWrapper) ) {
                 command = new TracingWrapperRunnable(command, Thread.currentThread().getStackTrace(), TRACE_FRAME_IGNORE_COUNT);
             }
         }
         return super.schedule(command, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            command = new TracingWrapperRunnable(command, Thread.currentThread().getStackTrace(), TRACE_FRAME_IGNORE_COUNT);
        }
        return super.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            command = new TracingWrapperRunnable(command, Thread.currentThread().getStackTrace(), TRACE_FRAME_IGNORE_COUNT);
        }
        return super.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
    
    @Override
    public void execute(Runnable command) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            command = new TracingWrapperRunnable(command, Thread.currentThread().getStackTrace(), TRACE_FRAME_IGNORE_COUNT);
        }
        super.execute(command);
    }     
    
    @Override
    public Future<?> submit(Runnable command) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            command = new TracingWrapperRunnable(command, Thread.currentThread().getStackTrace(), TRACE_FRAME_IGNORE_COUNT);
        }
        return super.submit(command);
    }
    
    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            callable = new TracingWrapperCallable<T>(callable, Thread.currentThread().getStackTrace(), TRACE_FRAME_IGNORE_COUNT);
        }
        return super.submit(callable);
    }
    
    @Override
    public <T> Future<T> submit(Runnable command, T result) {
        if(DEBUG_EXECUTOR || ASSERTIONS_ENABLED) {
            command = new TracingWrapperRunnable(command, Thread.currentThread().getStackTrace(), TRACE_FRAME_IGNORE_COUNT);
        }
        return super.submit(command, result);
    }
}
