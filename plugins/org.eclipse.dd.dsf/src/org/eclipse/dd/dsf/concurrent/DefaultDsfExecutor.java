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

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.DsfPlugin;

/**
 * Default implementation of a Riverbed executor interfaces, based on the 
 * standard java.util.concurrent.ThreadPoolExecutor.
 */

public class DefaultDsfExecutor extends ScheduledThreadPoolExecutor 
    implements DsfExecutor 
{
    static class DsfThreadFactory implements ThreadFactory {
        Thread fThread;
        public Thread newThread(Runnable r) {
            assert fThread == null;  // Should be called only once.
            fThread = new Thread(new ThreadGroup("Riverbed Thread Group"), r, "Riverbed Dispatch Thread", 0); 
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
    protected void afterExecute(Runnable r, Throwable t) {
        // FIXME: Unfortunately this is not enough to catch runnable exceptions, because 
        // FutureTask implementation swallows exceptions when they're thrown by runnables.
        // Need to override the FutureTask class, and the AbstractExecutorService.submit() 
        // methods in order to provide access to these exceptions.

        super.afterExecute(r, t);
        if (t != null) {
            DsfPlugin.getDefault().getLog().log(new Status(
                IStatus.ERROR, DsfPlugin.PLUGIN_ID, -1, "Uncaught exception in dispatch thread.", t));
        }
    }
}
