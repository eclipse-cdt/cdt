/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * Provides the ability to step  return from the frame.
 * Implementations must be non-blocking.
 */
public interface ICDIExecuteStepReturn {

	/**
	 * Continue execution until the frame return.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepReturn() throws CDIException;

	/**
	 * Cancel execution of the frame and return with value.
	 * value can be <code>null</code>, if no return value is needed.
	 * Can  only be called when the associated target/thread is suspended.
	 * 
	 * @param value use as the returning value.
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepReturn(ICDIValue value) throws CDIException;

}
