/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICSession;

/**
 * 
 * Represents a debuggable process. This is a root object of the CDI
 * model.
 * 
 * @since Jul 8, 2002
 */
public interface ICTarget extends ICObject
{	
	/**
	 * Returns the debug session this target is contained in.
	 * 
	 * @return the debug session this target is contained in
	 */
	ICSession getSession();

    /**
     * Gets the output stream of the target process.
     *
     * @return  the output stream connected to the normal input of the
     *          target process.
     */
    OutputStream getOutputStream();

    /**
     * Gets the input stream of the target process.
     *
     * @return  the input stream connected to the normal output of the
     *          target process.
     */
    InputStream getInputStream();

    /**
     * Gets the error stream of the target process.
     *
     * @return  the input stream connected to the error stream of the
     *          target process.
     */
    InputStream getErrorStream();

	/**
	 * Returns the shared libraries loaded in this target. 
	 * An empty collection is returned if no shared libraries
	 * are loaded in this target. 
	 * 
	 * @return a collection of shared libraries
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICSharedLibrary[] getSharedLibraries() throws CDIException;

	/**
	 * Returns the threads contained in this target. 
	 * An empty collection is returned if this target contains no 
	 * threads.
	 * 
	 * @return a collection of threads
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICThread[] getThreads() throws CDIException;
	
	/**
	 * Returns the thread associated with the given id.
	 * 
	 * @param id - the thread id
	 * @return the thread associated with the given id
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICThread getThread( String id ) throws CDIException;
	
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
	ICMemoryBlock getCMemoryBlock( long startAddress, long length ) throws CDIException;

	/**
	 * Returns the register groups associated with this target.
	 * 
	 * @return a collection of register groups 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICRegisterGroup[] getRegisterGroups() throws CDIException;

	/**
	 * Returns a collection of global variables associated with 
	 * this target.
	 * 
	 * @return a collection of global variables 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICGlobalVariable[] getGlobalVariables() throws CDIException;	

	/**
	 * Evaluates the expression specified by the given string.
	 * 
	 * @param - expression string to be evaluated
	 * @return an expression object
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICExpression evaluateExpression( String expressionText ) throws CDIException; 

	/**
	 * Evaluates the given expression.
	 * 
	 * @param - expression to be evaluated
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void evaluateExpression( ICExpression expression ) throws CDIException;
	
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
	 * Returns whether this target is is currently stepping.
	 *
	 * @return whether this target is currently stepping
	 */
	boolean isStepping();
	
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
}
