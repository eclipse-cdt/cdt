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
 * 
 * A thread in a debug target.
 * A thread contains stack frames.  Stack frames are only available 
 * when the thread is suspended, and are returned in top-down order.
 * 
 * @since Jul 8, 2002
 */
public interface ICDIThread extends ICDIExecuteStep, ICDIExecuteResume, ICDISuspend, ICDIObject {

	/**
	 * Returns the stack frames contained in this thread. An
	 * empty collection is returned if this thread contains
	 * no stack frames, or is not currently suspended. Stack frames
	 * are returned in top down order.
	 * 
	 * @return  a collection of stack frames
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIStackFrame[] getStackFrames() throws CDIException;

	/**
	 * Returns the stack frames contained in this thread whose levels
	 * are between the two arguments(inclusive).
	 * An empty collection is returned if this thread contains
	 * no stack frames, or is not currently suspended. Stack frames
	 * are returned in top down order.
	 * 
	 * @return  a collection of stack frames
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIStackFrame[] getStackFrames(int lowFrame, int highFrame) throws CDIException;

	/**
	 * Returns the depth of the stack frames.
	 *  
	 * @return  depth of stack frames
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	int getStackFrameCount() throws CDIException;

	/**
	 * Return thread local storage variables descriptor.
	 * 
	 * @return
	 * @throws CDIException
	 */
	ICDIThreadStorageDescriptor[] getThreadStorageDescriptors() throws CDIException;

	/**
	 * Equivalent to resume(false)
	 * 
	 * @deprecated 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void resume() throws CDIException;

	/**
	 * Equivalent to stepOver(1)
	 * 
	 * @deprecated 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepOver() throws CDIException;

	/**
	 * Equivalent to stepInto(1)
	 * 
	 * @deprecated 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepInto() throws CDIException;

	/**
	 * Equivalent to stepOverInstruction(1)
	 * 
	 * @deprecated 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepOverInstruction() throws CDIException;

	/**
	 * Equivalent to stepIntoInstruction(1)
	 * 
	 * @deprecated 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepIntoInstruction() throws CDIException;

	/**
	 * This method is deprecated and will only be available
	 * on the stackframe
	 * 
	 * @deprecated
	 * @see ICDIStackFrame.stepReturn()
	 * @throws CDIException
	 */
	void stepReturn() throws CDIException;

	/**
	 * Equivalent to stepUntil(location)
	 * 
	 * @deprecated
	 * @see #stepUntil(ICDILocation) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void runUntil(ICDILocation location) throws CDIException;

	/**
	 * Equivalent to resume(location)
	 * 
	 * @deprecated
	 * @see #resume(ICDILocation) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void jump(ICDILocation location) throws CDIException;

	/**
	 * Equivalent to resume(false)
	 * 
	 * @deprecated
	 * @see #resume(boolean)
	 * @throws CDIException
	 */
	void signal() throws CDIException;

	/**
	 * Equivalent to resume(signal)
	 * 
	 * @deprecated
	 * @see #resume(ICDISignal) 
	 * @param signal
	 * @throws CDIException
	 */
	void signal(ICDISignal signal) throws CDIException;

	/**
	 * Returns true if the threads are the same.
	 */
	boolean equals(ICDIThread thead);

}
