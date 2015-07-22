/*******************************************************************************
 * Copyright (c) 2015 Texas Instruments, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;

/**
 * Extension to the {@link IRunControl} service to add asynchronous versions of some methods.
 * @since 2.7
 */
public interface IRunControlAsync extends IRunControl {
	
	/**
	 * The asynchronous version of {@link IRunControl#isSuspended(IExecutionDMContext)}
	 */
    void isSuspended(IExecutionDMContext context, DataRequestMonitor<Boolean> rm);

	/**
	 * The asynchronous version of {@link IRunControl#isStepping(IExecutionDMContext)}
	 */
    void isStepping(IExecutionDMContext context, DataRequestMonitor<Boolean> rm);
}
