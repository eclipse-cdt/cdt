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
public interface ICDIThread extends ICDIObject {
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
	 * Returns the depth of the stack frames 
	 * @return  depth of stack frames
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	int getStackFrameCount() throws CDIException;

	/**
	 * Set the current Stack for the thread.
	 * @param - ICDIStackFrame
	 */
	void setCurrentStackFrame(ICDIStackFrame current) throws CDIException;

	/**
	 * Set the current frame whithout generation any events, for example
	 * registers changed events.
	 * @param frame
	 * @param b
	 */
	void setCurrentStackFrame(ICDIStackFrame frame, boolean doUpdate) throws CDIException;

	/**
	 * Set the current stackframe.
	 * @return ICDIStackFrame
	 */
	ICDIStackFrame getCurrentStackFrame() throws CDIException;
	/**
	 * Returns whether this thread is currently suspended.
	 *
	 * @return whether this thread is currently suspended
	 */
	boolean isSuspended();

	/**
	 * Causes this thread to resume its execution. 
	 * Has no effect on a thread that is not suspended.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void resume() throws CDIException;

	/**
	 * Causes this thread to suspend its execution. 
	 * Has no effect on an already suspended thread.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void suspend() throws CDIException;

	/**
	 * Equivalent to stepReturn(true)
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepReturn() throws CDIException;

	/**
	 * If execute is true, continue running until just after function. if
	 * If execute is false, cancel execution of the function and stop the
	 * program after the function.
	 * Can  only be called when the associated target is suspended.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepReturn(boolean execute) throws CDIException;

	/**
	 * Steps over the current source line. Can only be called
	 * when the associated thread is suspended. 
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepOver() throws CDIException;

	/**
	 * Steps into the current source line. Can only be called
	 * when the associated thread is suspended. 
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepInto() throws CDIException;

	/**
	 * Steps over the current machine instruction. Can only be called
	 * when the associated thread is suspended. 
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepOverInstruction() throws CDIException;

	/**
	 * Steps into the current machine instruction. Can only be called
	 * when the associated thread is suspended. 
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepIntoInstruction() throws CDIException;

	/**
	 * Continues running until location is reached.
	 * Can only be called when the associated thread is suspended.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void runUntil(ICDILocation location) throws CDIException;

	/**
	 * Resume execution at location. Note the jump() does not change stackframe.
	 * The result is undefined if jump outside of the stacframe i.e function.
	 * Can  only be called when the associated target is suspended.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void jump(ICDILocation location) throws CDIException;

	/**
	 * Method signal, resume execution without giving a signal.
	 * @throws CDIException
	 */
	void signal() throws CDIException;

	/**
	 * Resume execution where the program stopped but immediately give the
	 * signal.
	 * 
	 * @param signal
	 * @throws CDIException
	 */
	void signal(ICDISignal signal) throws CDIException;

	/**
	 * Returns true if the threads are the same.
	 */
	boolean equals(ICDIThread thead);

}
