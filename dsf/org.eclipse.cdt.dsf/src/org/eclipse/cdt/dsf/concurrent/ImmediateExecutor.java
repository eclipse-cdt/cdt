/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import java.util.concurrent.Executor;

import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.Platform;

/**
 * Executor that executes a runnable immediately (synchronously) when it is
 * submitted (when {@link #execute(Runnable)} is called). The runnable is
 * exercised on the submitter's thread. This executor is useful for clients that
 * need to create <code>RequestMonitor</code> objects, but which do not have
 * their own executor.
 * 
 * @see RequestMonitor
 * 
 * @since 1.0
 */
public class ImmediateExecutor implements Executor {
    
    /**
     * Debug flag used for tracking runnables that were never executed, 
     * or executed multiple times.
     */
    protected static boolean DEBUG_EXECUTOR = false;
    static {
        DEBUG_EXECUTOR = DsfPlugin.DEBUG && "true".equals( //$NON-NLS-1$
            Platform.getDebugOption("org.eclipse.cdt.dsf/debug/executor")); //$NON-NLS-1$
    }  

    private static ImmediateExecutor fInstance = new ImmediateExecutor();
    
    /**
     * The default constructor is hidden. {@link #getInstance()} should be 
     * used instead.
     */
    private ImmediateExecutor() {}
    
    /**
     * Returns the singleton instance of ImmediateExecutor.
     */
    public static Executor getInstance() {
        return fInstance;
    }
    
    public void execute(Runnable command) {
        // Check if executable wasn't executed already.
        if (DEBUG_EXECUTOR && command instanceof DsfExecutable) {
            assert !((DsfExecutable)command).getSubmitted() : "Executable was previously executed."; //$NON-NLS-1$
            ((DsfExecutable)command).setSubmitted();
        }
        command.run();
    }
}
