/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel;

import java.util.concurrent.Executor;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;

/**
 * A listener participating in event notifications sent out from VM adapter.
 * 
 * @since 1.1
 */
@ConfinedToDsfExecutor("#getExecutor()")
public interface IVMEventListener {

    /**
     * Returns the executor that needs to be used to access this event listener. 
     */
    @ThreadSafe
    public Executor getExecutor();
    
	/**
	 * Process the given event and indicate completion with request monitor.
	 */
	public void handleEvent(final Object event, RequestMonitor rm);

	/**
	 * Returns whether the event handling manager should wait for this listener
	 * to complete handling an event given to it via
	 * {@link #handleEvent(Object, RequestMonitor)}, or whether the event
	 * listener can process that event asynchronously.
	 */
	public boolean shouldWaitHandleEventToComplete();
}
