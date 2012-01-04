/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.concurrent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;

/**
 * A simple executor which uses the display thread to run the submitted 
 * runnables. It only implements the {@link Executor}, and NOT the more
 * sophisticated {@link DsfExecutor} (which extends 
 * {@link java.util.concurrent.ScheduledExecutorService}). However, this 
 * implementation is much more efficient than DisplayDsfExecutor as it does
 * not use a separate thread or maintain its own queue.
 * 
 * @since 1.0
 */
public class SimpleDisplayExecutor implements Executor{
    /**
     * Internal mapping of display objects to executors.
     */
    private static Map<Display, SimpleDisplayExecutor> fExecutors = Collections.synchronizedMap( new HashMap<Display, SimpleDisplayExecutor>() );
    
    /**
     * Factory method for display executors.
     * @param display Display to create an executor for.
     * @return The new (or re-used) executor.
     */
    public static SimpleDisplayExecutor getSimpleDisplayExecutor(Display display) {
        synchronized (fExecutors) {
            SimpleDisplayExecutor executor = fExecutors.get(display);
            if (executor == null) {
                executor = new SimpleDisplayExecutor(display);
                fExecutors.put(display, executor);
            }
            return executor;
        }
    }
    
    /**
     * The display class used by this executor to execute the submitted runnables. 
     */
    private final Display fDisplay;
    
    private SimpleDisplayExecutor(Display display) {
        fDisplay = display;
    }

    @Override
	public void execute(Runnable command) {
        try {
            fDisplay.asyncExec(command);
        } catch (SWTException e) {
            if (e.code == SWT.ERROR_DEVICE_DISPOSED) {
                throw new RejectedExecutionException("Display " + fDisplay + " is disposed", e); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                throw e;
            }
        }
    }
}
