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
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;

/**
 * 
 * Represents a debuggable process. This is a root object of the CDI
 * model.
 * 
 * @since Jul 8, 2002
 */
public interface ICDITarget extends ICDIBreakpointManagement, ICDIObject {
	/**
	 * Returns the debug session this target is contained in.
	 * 
	 * @return the debug session this target is contained in
	 */
	ICDISession getSession();

	/**
	 * Gets the target process.
	 *
	 * @return  the output stream connected to the normal input of the
	 *          target process.
	 */
	Process getProcess();

	/**
	 * Returns the threads contained in this target. 
	 * An empty collection is returned if this target contains no 
	 * threads.
	 * 
	 * @return a collection of threads
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIThread[] getThreads() throws CDIException;

	/**
	 * Set the current thread on the target.
	 * @param - ICDThread
	 */
	void setCurrentThread(ICDIThread current) throws CDIException;

	/**
	 * Evaluates the expression specified by the given string.
	 * Returns the evaluation result as a String.
	 * 
	 * @param - expression string to be evaluated
	 * @return the result of the evaluation
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String evaluateExpressionToString(String expressionText)
		throws CDIException;

	/**
	 * Returns whether this target is terminated.
	 *
	 * @return whether this target is terminated
	 */
	boolean isTerminated();

	/**
	 * Causes this target to terminate.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void terminate() throws CDIException;

	/**
	 * Returns whether this target is disconnected.
	 *
	 * @return whether this target is disconnected
	 */
	boolean isDisconnected();

	/**
	 * Disconnects this target from the debuggable process. Generally, 
	 * disconnecting ends a debug session with this target, but allows 
	 * the debuggable program to continue running.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void disconnect() throws CDIException;

	/**
	 * Restarts the execution of this target.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void restart() throws CDIException;

	/**
	 * Returns whether this target is currently suspended.
	 *
	 * @return whether this target is currently suspended
	 */
	boolean isSuspended();

	/**
	 * Causes this target to resume its execution. 
	 * Has no effect on a target that is not suspended.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void resume() throws CDIException;

	/**
	 * Causes this target to suspend its execution. 
	 * Has no effect on an already suspended target.
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
	 * when the associated target is suspended. 
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepOver() throws CDIException;

	/**
	 * Steps into the current source line. Can only be called
	 * when the associated target is suspended. 
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepInto() throws CDIException;

	/**
	 * Steps over the current machine instruction. Can only be called
	 * when the associated target is suspended. 
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepOverInstruction() throws CDIException;

	/**
	 * Steps into the current machine instruction. Can only be called
	 * when the associated target is suspended. 
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepIntoInstruction() throws CDIException;

	/**
	 * Continues running until location is reached. Can only be called when the associated 
	 * target is suspended.
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
	 * Returns the currently selected thread.
	 * 
	 * @return the currently selected thread
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIThread getCurrentThread() throws CDIException;

	/**
	 * Return a ICDICondition
	 */
	ICDICondition createCondition(int ignoreCount, String expression);

	/**
	 * Returns a ICDILocation
	 */
	ICDILocation createLocation(String file, String function, int line);

	/**
	 * Returns a ICDILocation
	 */
	ICDILocation createLocation(long address);

}
