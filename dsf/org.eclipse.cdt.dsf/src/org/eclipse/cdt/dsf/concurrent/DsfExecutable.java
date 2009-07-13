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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.Platform;

/**
 * Base class for DSF-instrumented alternative to the Runnable/Callable interfaces.
 * <p>
 * While it is perfectly fine for clients to call the DSF executor with
 * an object only implementing the Runnable/Callable interface, the DsfExecutable
 * contains fields and methods that used for debugging and tracing when 
 * tracing is enabled.
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
     * Flag indicating that assertions are enabled.  It enables storing of the
     * "creator" executable for debugging purposes.
     */
    static boolean ASSERTIONS_ENABLED = false;

    static {
        assert (ASSERTIONS_ENABLED = true) == true;
        DEBUG_EXECUTOR = DsfPlugin.DEBUG && "true".equals( //$NON-NLS-1$
                Platform.getDebugOption("org.eclipse.cdt.dsf/debug/executor")); //$NON-NLS-1$
    }  
    
    /** 
     * Field that holds the stack trace of where this executable was created.
     * Used for tracing and debugging only.
     */
    final StackTraceWrapper fCreatedAt;
    
    /**
     * Field holding the reference of the executable that created this 
     * executable.  Used for tracing only.
     */
    final DefaultDsfExecutor.TracingWrapper fCreatedBy;

    /**
     * Flag indicating whether this executable was ever executed by an 
     * executor.  Used for tracing only.
     */
    private volatile boolean fSubmitted = false;
    
    @SuppressWarnings("unchecked")
    public DsfExecutable() {
        // Use assertion flag (-ea) to jre to avoid affecting performance when not debugging.
        if (ASSERTIONS_ENABLED || DEBUG_EXECUTOR) {
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
    
    public boolean getSubmitted() {
        return fSubmitted;
    }
    
    /**
     * Marks this executable to indicate that it has been executed by the 
     * executor.  To be invoked only by DsfExecutor.
     */
    public void setSubmitted() {
        fSubmitted = true;
    }
    
    /**
     * Returns whether the runnable/callable is expected to be always executed.  
     * Overriding classes can implement this method and return false, to avoid
     * unnecessary trace output. 
     * @return true if this runnable is expected to run. 
     */
    protected boolean isExecutionRequired() {
        return true;
    }
    
    @Override
    protected void finalize() {
        if (DEBUG_EXECUTOR && !fSubmitted && isExecutionRequired()) {
            StringBuilder traceBuilder = new StringBuilder();

            // Record the time
            traceBuilder.append(DsfPlugin.getDebugTime());
            traceBuilder.append(' ');
            
            // Record the event
            traceBuilder.append("DsfExecutable was never executed:\n        "); //$NON-NLS-1$
            traceBuilder.append(this);
            traceBuilder.append("\nCreated at:"); //$NON-NLS-1$
            traceBuilder.append(fCreatedAt);
            
            DsfPlugin.debug(traceBuilder.toString());
        }
    }
}
