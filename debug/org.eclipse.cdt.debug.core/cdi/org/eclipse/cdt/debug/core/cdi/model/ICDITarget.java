/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;

/**
 * 
 * Represents a debuggable process. This is a root object of the CDI
 * model.
 * 
 * @since Jul 8, 2002
 */
public interface ICDITarget extends ICDIThreadGroup, ICDIExpressionManagement, 
	ICDISourceManagement, ICDISharedLibraryManagement, ICDIMemoryBlockManagement, ICDISessionObject {

	/**
	 * Gets the target process.
	 *
	 * @return  the output stream connected to the normal input of the
	 *          target process.
	 */
	Process getProcess();

	/**
	 * Returns the configuration description of this debug session.
	 * 
	 * @return the configuration description
	 */
	ICDITargetConfiguration getConfiguration();

	/**
	 * Evaluates the expression specified by the given string.
	 * Returns the evaluation result as a String.
	 * 
	 * @param - expression string to be evaluated
	 * @return the result of the evaluation
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String evaluateExpressionToString(ICDIStackFrame context, String expressionText)
		throws CDIException;

	/**
	 * A static/global variable in a particular function or file,
	 * filename or/and function is the context for the static ICDIVariableDescriptor.
	 * <pre>
	 * hello.c:
	 *   int bar;
	 *   int main() {
	 *   	static int bar;
	 *   }
	 * file.c:
	 *   int foo() {
	 *   	static int bar;
	 *   }
	 * getVariableObject(null, null, "bar");
	 * getVariableObject(null, "main", "bar");
	 * getVariableObject("file.c", "foo", "bar");
	 * </pre>
	 * @param filename
	 * @param function
	 * @param name
	 * @return ICDIGlobalVariableDescriptor
	 * @throws CDIException
	 */
	ICDIGlobalVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws CDIException;

	/**
	 * Create a variable from the descriptor for evaluation.  A CreatedEvent will be trigger and
	 * ChangedEvent will also be trigger when the variable is assign a new value.
	 * DestroyedEvent is fired when the variable is out of scope and automatically
	 * removed from the manager list.
	 * 
	 * @param varDesc ICDIGlobalVariableDescriptor
	 * @return ICDIGlobalVariable
	 * @throws CDIException
	 */
	ICDIGlobalVariable createGlobalVariable(ICDIGlobalVariableDescriptor varDesc) throws CDIException;

	/**
	 * Return the register groups.
	 * 
	 * @return ICDIRegisterGroup[]
	 */
	ICDIRegisterGroup[] getRegisterGroups() throws CDIException;

	/**
	 * Create a variable from the descriptor for evaluation.  A CreatedEvent will be trigger and
	 * ChangedEvent will also be trigger when the variable is assign a new value.
	 * DestroyedEvent is fired when the variable is out of scope and automatically
	 * removed from the manager list.
	 * @param varDesc ICDThreadStorageDesc
	 * @return
	 * @throws CDIException
	 */
	ICDIRegister createRegister(ICDIRegisterDescriptor varDesc) throws CDIException;

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
	 * Equivalent to resume(false)
	 * 
	 * @deprecated 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	@Deprecated
	void resume() throws CDIException;

	/**
	 * Equivalent to stepOver(1)
	 * 
	 * @deprecated
	 * @see #stepOver(int)
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	@Deprecated
	void stepOver() throws CDIException;

	/**
	 * Equivalent to stepInto(1)
	 * 
	 * @deprecated
	 * @see #stepInto(int) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	@Deprecated
	void stepInto() throws CDIException;

	/**
	 * Equivalent to stepOverInstruction(1)
	 * 
	 * @deprecated
	 * @see stepOverInstruction(int) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	@Deprecated
	void stepOverInstruction() throws CDIException;

	/**
	 * Equivalent to stepIntoInstruction(1)
	 * 
	 * @deprecated
	 * @see #stepIntoInstruction(int) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	@Deprecated
	void stepIntoInstruction() throws CDIException;

	/**
	 * Equivaltent to stepUntil(location)
	 * 
	 * @deprecated
	 * @see #stepUntil(ICDILocation) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	@Deprecated
	void runUntil(ICDILocation location) throws CDIException;

	/**
	 * Equivalent to resume(location
	 * 
	 * @deprecated
	 * @see #resume(ICDLocation) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	@Deprecated
	void jump(ICDILocation location) throws CDIException;
	
	/**
	 * Equivalent to resume(false)
	 * 
	 * @deprecated
	 * @throws CDIException
	 */
	@Deprecated
	void signal() throws CDIException;

	/**
	 * Equivalent to resume(signal)
	 * 
	 * @deprecated
	 * @see #resume(ICDISignal) 
	 * @param signal
	 * @throws CDIException
	 */
	@Deprecated
	void signal(ICDISignal signal) throws CDIException;

	/**
	 * Returns the Runtime options for this target debug session.
	 * 
	 * @return the configuration description
	 */
	ICDIRuntimeOptions getRuntimeOptions();

	/**
	 * Return a ICDICondition
	 */
	ICDICondition createCondition(int ignoreCount, String expression);

	/**
	 * Return a ICDICondition
	 */
	ICDICondition createCondition(int ignoreCount, String expression, String[] threadIds);

	/**
	 * Returns a ICDILineLocation
	 */
	ICDILineLocation createLineLocation(String file, int line);

	/**
	 * Returns a ICDIFunctionLocation
	 */
	ICDIFunctionLocation createFunctionLocation(String file, String function);

	/**
	 * Returns a ICDIAddressLocation
	 */
	ICDIAddressLocation createAddressLocation(BigInteger address);

}
