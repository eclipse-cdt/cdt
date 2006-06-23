/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * Provides the ability to suspend a thread or debug target.
*/
public interface ICDISuspend {

	/**
	 * Causes this target/thread to suspend its execution. 
	 * Has no effect on an already suspended thread.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void suspend() throws CDIException;

	/**
	 * Returns whether this target/thread is currently suspended.
	 *
	 * @return whether this target/thread is currently suspended
	 */
	boolean isSuspended();
}
