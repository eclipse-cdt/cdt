/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

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
	 * Set the curretn Stack for the thread.
	 * @param - ICDIStackFrame
	 */
	void setCurrentStackFrame(ICDIStackFrame current) throws CDIException;

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
	 * Steps to the next return statement in the current scope. Can 
	 * only be called when the associated thread is suspended. 
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepReturn() throws CDIException;

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
	 * Continues running until just after function in the current 
	 * stack frame returns. Can only be called when the associated 
	 * thread is suspended.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void finish() throws CDIException;

	/**
	 * Returns true if the threads are the same.
	 */
	boolean equals(ICDIThread thead);
}
