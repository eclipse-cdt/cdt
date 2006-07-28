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

import java.util.concurrent.ScheduledExecutorService;

/**
 * DSF executor service.  Implementations of this executor must ensure
 * that all runnables and callables are executed in the same thread: the 
 * executor's single dispatch thread.  
 * <br>Note: A DSF executor dispatch thread does not necessarily have 
 * to be exclusive to the executor, it could be shared with 
 * another event dispatch service, such as the SWT display dispatch thread.
 */
public interface DsfExecutor extends ScheduledExecutorService
{
    /**
     * Checks if the thread that this method is called in is the same as the
     * executor's dispatch thread.
     * @return true if in DSF executor's dispatch thread
     */
    public boolean isInExecutorThread();
}
