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
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 * Provides the ability to resume a thread or debug target.
 */
interface ICDIExecuteResume {

	/**
	 * Causes this target to resume its execution. 
	 * if passSignal is <code>fase</code> and the target was
	 * suspended by a signal when resuming the signal will be discarded
	 * Has no effect on a target that is not suspended.
	 * 
	 * @param passSignal whether to discar the signal
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void resume(boolean passSignal) throws CDIException;

	/**
	 * Resume execution at location. Note the method does not change stackframe.
	 * The result is undefined if it jumps outside of the stacframe.
	 * Can  only be called when the associated target is suspended.
	 * 
	 * @param location
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void resume(ICDILocation location) throws CDIException;

	/**
	 * Resume execution where the program stopped but immediately give the
	 * signal.
	 * 
	 * @param signal
	 * @throws CDIException
	 */
	void resume(ICDISignal signal) throws CDIException;

}
