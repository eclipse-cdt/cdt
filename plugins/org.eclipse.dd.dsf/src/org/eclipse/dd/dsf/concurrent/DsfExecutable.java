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

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for DSF-instrumented alternative to the Runnable/Callable interfaces.
 * <p>
 * While it is perfectly fine for clients to call the DSF executor with
 * an object only implementing the Runnable/Callable interface, the DsfExecutable
 * contains fields and methods that used for debugging and tracing when 
 * tracing is enabled.
 */
@Immutable
public class DsfExecutable {
    final StackTraceElement[]  fCreatedAt;
    final DefaultDsfExecutor.TracingWrapper fCreatedBy;

    @SuppressWarnings("unchecked")
    public DsfExecutable() {
        // Use assertion flag (-ea) to jre to avoid affecting performance when not debugging.
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled || DefaultDsfExecutor.DEBUG_EXECUTOR) {
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
            fCreatedAt = new StackTraceElement[stackTrace.length - i]; 
            System.arraycopy(stackTrace, i, fCreatedAt, 0, fCreatedAt.length);
        } else {
            fCreatedAt = null;
            fCreatedBy = null;
        }
    }        
}
