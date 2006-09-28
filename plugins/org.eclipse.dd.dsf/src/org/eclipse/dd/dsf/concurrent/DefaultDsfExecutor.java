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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

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
    // debug tracing flags
    public static boolean DEBUG_EXECUTOR = false;
    static {
        DEBUG_EXECUTOR = DsfPlugin.DEBUG && "true".equals( //$NON-NLS-1$
            Platform.getDebugOption("org.eclipse.dd.dsf/debug/executor")); //$NON-NLS-1$
    }  

    
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
    }
    
    public boolean isInExecutorThread() {
        return Thread.currentThread().equals( ((DsfThreadFactory)getThreadFactory()).fThread );
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) { 
        System.out.println("");
        
        
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
                    if (DEBUG_EXECUTOR) {
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream(512);
                        PrintStream printStream = new PrintStream(outStream);
                        try {
                            printStream.write("Uncaught exception in session executor thread: ".getBytes());
                        } catch (IOException e2) {}
                        e.getCause().printStackTrace(new PrintStream(outStream));
                        DsfPlugin.debug(outStream.toString());
                    }
                }
            }
        }
    }
    
}
