/*******************************************************************************
 * Copyright (c) 2011 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     William R. Swanson (Tilera) - initial API and implementation (Bug 365966)
 *******************************************************************************/

package org.eclipse.cdt.dsf.concurrent;

/**
 * Convenience extension of CountingRequestMonitor that uses the ImmediateExecutor.
 * The handleCompleted() method is immediately executed in same thread as done() call.
 * @since 2.3
 */
public class ImmediateCountingRequestMonitor extends CountingRequestMonitor
{
	/**
	 * Constructor without a parent monitor and using ImmediateExecutor.
	 */
	public ImmediateCountingRequestMonitor() {
		super(ImmediateExecutor.getInstance(), null);
	}
	
	/**
	 * Constructor with an optional parent monitor and using ImmediateExecutor.
	 */
	public ImmediateCountingRequestMonitor(RequestMonitor parentMonitor) {
		super(ImmediateExecutor.getInstance(), parentMonitor);
	}	
}
