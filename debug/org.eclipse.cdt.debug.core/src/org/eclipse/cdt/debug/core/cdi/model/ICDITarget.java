/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterObject;
import org.eclipse.cdt.debug.core.cdi.ICDISession;

/**
 * 
 * Represents a debuggable process. This is a root object of the CDI
 * model.
 * 
 * @since Jul 8, 2002
 */
public interface ICDITarget extends ICDIObject {
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
	 * Returns the shared libraries loaded in this target. 
	 * An empty collection is returned if no shared libraries
	 * are loaded in this target. 
	 * 
	 * @return a collection of shared libraries
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDISharedLibrary[] getSharedLibraries() throws CDIException;

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
	 * Returns a memory block that starts at the specified memory 
	 * address, with the specified length.
	 * 
	 * @param - starting address
	 * @param - length of the memory block in bytes 
	 * @return a memory block that starts at the specified memory address, 
	 * with the specified length 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIMemoryBlock getCMemoryBlock(long startAddress, long length)
		throws CDIException;

	/**
	 * Returns the register Object associated with this target.
	 * 
	 * @return a collection of register object.
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIRegisterObject[] getRegisterObjects() throws CDIException;

	/**
	 * Returns the register associated with this target.
	 * @return a collection of register.
	 * @throws CDIException if this method fails.
	 */
	ICDIRegister[] getRegisters(ICDIRegisterObject[] regObjects) throws CDIException;

	/**
	 * Returns a collection of global variables associated with 
	 * this target.
	 * 
	 * @return a collection of global variables 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIGlobalVariable[] getGlobalVariables() throws CDIException;

	/**
	 * Evaluates the expression specified by the given string.
	 * Returns the evaluation result as an ICDIValue.
	 * 
	 * @param - expression string to be evaluated
	 * @return the result of the evaluation
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIValue evaluateExpressionToValue(String expressionText)
		throws CDIException;

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
	 * Steps to the next return statement in the current scope. Can 
	 * only be called when the associated thread is suspended. 
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepReturn() throws CDIException;

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
	 * Continues running until just after function in the current 
	 * stack frame returns. Can only be called when the associated 
	 * target is suspended.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void finish() throws CDIException;
	
	/**
	 * Returns the currently selected thread.
	 * 
	 * @return the currently selected thread
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIThread getCurrentThread() throws CDIException;
}
