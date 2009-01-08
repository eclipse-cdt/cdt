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
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * Stack service extension.
 * <p>
 * Adds the capability to retrieve a limited number of stack frames.
 * </p>
 * 
 * @since DSF 1.1
 */
public interface IStack2 extends IStack {

	/**
	 * Convenience constant for use with {@link #getFrames(IDMContext, int, int, DataRequestMonitor)}
	 * to retrieve all stack frames.
	 */
	public final static int ALL_FRAMES = -1;

	/**
	 * Retrieves list of stack frames for the given execution context.  Request
	 * will fail if the stack frame data is not available. 
	 * <p>The range of stack frames can be limited by the <code>startIndex</code> and <code>endIndex</code> arguments. 
	 * It is no error to specify an <code>endIndex</code> exceeding the number of available stack frames.
	 * A negative value for <code>endIndex</code> means to retrieve all stack frames. <code>startIndex</code> must be a non-negative value.
	 * </p>
	 * 
	 * @param execContext  the execution context to retrieve stack frames for
	 * @param startIndex  the index of the first frame to retrieve
	 * @param endIndex  the index of the last frame to retrieve (inclusive) or {@link #ALL_FRAMES}
	 * @param rm  the request monitor
	 * 
	 * @see #getFrames(IDMContext, DataRequestMonitor)
	 */
	public abstract void getFrames(IDMContext execContext, int startIndex, int endIndex, DataRequestMonitor<IFrameDMContext[]> rm);

}
