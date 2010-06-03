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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.Platform;

/**
 * Instrumented base class for
 * <ul>
 * <li>Runnable/Callable objects that are to be submitted to a DsfExecutor
 * <li>objects that have a primary execution method (resembling
 * <code>Runnable.run</code>) and that tend to be exercised from a DSF Executor
 * and/or that submit work to a DSF executor.
 * </ul>
 * 
 * <p>
 * Derivative classes benefit from additional fields that can be of help when
 * debugging a DSF session. Derivatives that implement Runnable/Callable and are
 * fed to DSF executors additionally benefit from tracing (when turned on by the
 * user). A trace message is generated when the Runnable/Callable is submitted
 * to the DsfExecutor.
 * 
 * <p>
 * Note that DSF executors need not be fed instances of this type. It is
 * perfectly fine for clients to call the DSF executor with a plain vanilla
 * Runnable/Callable, but such objects will obviously not benefit from the
 * instrumentation.
 * 
 * <p>
 * When this base class is used to instrument a Runnable/Callable that is
 * destined for a DSF executor, no additional work is imposed on the derived
 * class. In all other cases, the subclass is responsible for calling
 * {@link #setSubmitted()} from its primary execution method (e.g.,
 * {@link RequestMonitor#done()}
 * 
 * All fields and methods in this class are for tracing and debugging purposes
 * only.
 * 
 * @since 1.0
 */
@ThreadSafe
public class DsfExecutable {
    /** 
     * Flag indicating that tracing of the DSF executor is enabled.  It enables
     * storing of the "creator" information as well as tracing of disposed
     * runnables that have not been submitted to the executor.  
     */
    static boolean DEBUG_EXECUTOR = false;

	/**
	 * Flag indicating that monitor objects should be instrumented. A monitor is
	 * an object that is usually constructed as an anonymous inner classes and
	 * is used when making an asynchronous call--one that needs to return some
	 * result or at least notify its caller when it has completed. These objects
	 * usually end up getting chained together at runtime, forming what is
	 * effectively a very disjointed code path. When this trace option is
	 * enabled, these objects are given a String field at construction time that
	 * contains the instantiation backtrace. This turns out to be a fairly
	 * dependable alternative to the standard program stack trace, which is of
	 * virtually no help when debugging asynchronous, monitor-assisted code.
	 */
    static boolean DEBUG_MONITORS = false;

    /** 
     * Flag indicating that assertions are enabled.  It enables storing of the
     * "creator" executable for debugging purposes.
     */
    static boolean ASSERTIONS_ENABLED = false;

    static {
        assert (ASSERTIONS_ENABLED = true) == true;
        DEBUG_EXECUTOR = DsfPlugin.DEBUG && "true".equals( //$NON-NLS-1$
                Platform.getDebugOption("org.eclipse.cdt.dsf/debug/executor")); //$NON-NLS-1$
        
        DEBUG_MONITORS = DsfPlugin.DEBUG && "true".equals( //$NON-NLS-1$
                Platform.getDebugOption("org.eclipse.cdt.dsf/debug/monitors")); //$NON-NLS-1$          
    }

	/**
	 * Stack trace indicating where this object was created.
	 */
    final StackTraceWrapper fCreatedAt;

	/**
	 * If this object was created by a runnable/callable that was submitted to a
	 * DsfExecutor, this field holds a reference to its tracing wrapper.
	 * Otherwise, this field is null.
	 */
    final DefaultDsfExecutor.TracingWrapper fCreatedBy;

	/**
	 * If this object is a Runnable/Callable, this flag indicates whether this
	 * object was ever submitted to a DsfExecutor for execution. If this is not
	 * a Runnable/Callable, then this indicates whether the primary execution
	 * method of the object was ever invoked. The subclass is required to
	 * explicitly call this method at the start of that primary method.
	 */
    private volatile boolean fSubmitted = false;
    
    @SuppressWarnings("unchecked")
    public DsfExecutable() {
        // Use assertion flag (-ea) to jre to avoid affecting performance when not debugging.
        if (ASSERTIONS_ENABLED || DEBUG_EXECUTOR || DEBUG_MONITORS) {
            // Find the runnable/callable that is currently running.
            DefaultDsfExecutor executor = DefaultDsfExecutor.fThreadToExecutorMap.get(Thread.currentThread()); 
            if (executor != null) {
                fCreatedBy = executor.fCurrentlyExecuting;
            } else {
                fCreatedBy = null;
            }
            
            // Get the stack trace and find the first method that is not a 
            // constructor of this object. 
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            Class thisClass = getClass();
            Set<String> classNamesSet = new HashSet<String>();
            while(thisClass != null) {
                classNamesSet.add(thisClass.getName());
                thisClass = thisClass.getSuperclass();
            }
            int i;
            for (i = 3; i < stackTrace.length; i++) {
                if ( !classNamesSet.contains(stackTrace[i].getClassName()) ) break;
            }
            fCreatedAt = new StackTraceWrapper(new StackTraceElement[stackTrace.length - i]); 
            System.arraycopy(stackTrace, i, fCreatedAt.fStackTraceElements, 0, fCreatedAt.fStackTraceElements.length);
        } else {
            fCreatedAt = null;
            fCreatedBy = null;
        }
    }

	/**
	 * Was this object submitted for execution?
	 * 
	 * See {@link #setSubmitted()}
	 */
    public boolean getSubmitted() {
        return fSubmitted;
    }

	/**
	 * Mark that this object was submitted for execution.
	 * 
	 * <p>
	 * More specifically, if this object is a runnable/callable, this method is
	 * called right before the execute/call method is invoked by a DSF executor.
	 * If the object is not a runnable/callable, then this method is called
	 * right before a DsfExecutor invokes a callable/runnable that will invoke
	 * the target method of this object.
	 */
    public void setSubmitted() {
        fSubmitted = true;
    }

	/**
	 * Returns whether this object is always expected to be executed.
	 * We output a trace message if we are garbage collected without having been
	 * executed...that is unless this method returns false. 
	 * 
	 * Subclasses should override this method and return false if instances of
	 * it aren't meant to always be executed, thus avoiding unnecessary trace
	 * output.
	 * 
	 * @return true if this object should always be executed
	 */
    protected boolean isExecutionRequired() {
        return true;
    }

// Bug 306982
//  Disable the use of finalize() method in DSF runnables tracing to avoid 
//  a performance penalty in garbage colleciton.
//
//	/**
//	 * Checks to see if the object was executed before being garbage collected.
//	 * If not, and it's expected to have been, then output a trace message to
//	 * that effect.
//	 * 
//	 * @see java.lang.Object#finalize()
//	 */
//    @Override
//    protected void finalize() {
//        if (DEBUG_EXECUTOR && !fSubmitted && isExecutionRequired()) {
//            StringBuilder traceBuilder = new StringBuilder();
//
//            // Record the time
//            traceBuilder.append(DsfPlugin.getDebugTime());
//            traceBuilder.append(' ');
//            
//            final String refstr = LoggingUtils.toString(this, false);
//            traceBuilder.append("DSF executable was never executed: " + refstr); //$NON-NLS-1$
//            final String tostr = LoggingUtils.trimTrailingNewlines(this.toString());
//            if (!tostr.equals(refstr)) {
//            	traceBuilder.append(" ["); //$NON-NLS-1$
//            	traceBuilder.append(tostr);
//            	traceBuilder.append(']');
//            }
//            traceBuilder.append("\nCreated at:\n"); //$NON-NLS-1$
//            traceBuilder.append(fCreatedAt);
//            
//            DsfPlugin.debug(traceBuilder.toString());
//        }
//    }
}
