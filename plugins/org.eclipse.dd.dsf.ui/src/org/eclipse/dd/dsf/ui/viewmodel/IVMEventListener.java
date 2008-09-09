/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.viewmodel;

import java.util.concurrent.Executor;

import org.eclipse.dd.dsf.concurrent.RequestMonitor;

/**
 * A listener participating in event notifications sent out from VM adapter.
 * 
 * @since 1.1
 */
public interface IVMEventListener {

    /**
     * Returns the executor that needs to be used to access this event listener. 
     */
    public Executor getExecutor();
    
	/**
	 * Process the given event and indicate completion with request monitor.
	 */
	public void handleEvent(final Object event, RequestMonitor rm);
	
	/**
	 * Returns whether the event handling manager should wait for this listener
	 * to complete handling this event, or whether the event listener can process
	 * the event asynchronously.  
	 */
	public boolean shouldWaitHandleEventToComplete();
}
