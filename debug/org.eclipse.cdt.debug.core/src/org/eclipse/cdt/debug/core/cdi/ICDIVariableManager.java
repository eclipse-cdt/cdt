/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.*;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;

/**
 * Auto update is on by default.
 */
public interface ICDIVariableManager extends ICDIManager {

	/**
	 * Method getVariableObject.
	 * you can specify a static/global variable in a particular function or file,
	 * filename or/and function is the context for the static ICDIVariableObject.
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
	 * @return ICDIVariableObject
	 * @throws CDIException
	 */
	ICDIVariableObject getVariableObject(String filename, String function, String name) throws CDIException;

	/**
	 * Use the stackframe to return an ICDIVariableObject for name.
	 *
	 * @param stack
	 * @param name
	 * @return ICDIVariableObject
	 * @throws CDIException
	 */
	ICDIVariableObject getVariableObject(ICDIStackFrame stack, String name) throws CDIException;

	/**
	 * Consider the variable object as an Array of type and range[start, end]
	 * @param stack
	 * @param name
	 * @return ICDIVariableObject
	 * @throws CDIException
	 */
	ICDIVariableObject getVariableObjectAsArray(ICDIVariableObject var, String type, int start, int end) throws CDIException;

	/**
	 * Consider the variable object as type.
	 * @param stack
	 * @param name
	 * @return ICDIVariableObject
	 * @throws CDIException
	 */
	ICDIVariableObject getVariableObjectAsType(ICDIVariableObject var, String type) throws CDIException;

	/**
	 * Method getVariableObjects.
	 * Returns all the local variable objects of that stackframe.
	 * @param stack
	 * @return ICDIVariableObject[]
	 * @throws CDIException
	 */
	ICDIVariableObject[] getLocalVariableObjects(ICDIStackFrame stack) throws CDIException;

	/**
	 * Method getVariableObjects.
	 * Returns all the local variable objects of that stackframe.
	 * @param stack
	 * @return ICDIVariableObject[]
	 * @throws CDIException
	 */
	ICDIVariableObject[] getVariableObjects(ICDIStackFrame stack) throws CDIException;

	/**
	 * Method createVariable.
	 * Create a Variable for evaluation.  A CreatedEvent will be trigger and
	 * ChangedEvent will also be trigger when the variable is assign a new value.
	 * DestroyedEvent is fired when the variable is out of scope and automatically
	 * removed from the manager list.
	 * @param var
	 * @return ICDIVariable
	 * @throws CDIException
	 */
	ICDIVariable createVariable(ICDIVariableObject var) throws CDIException;

	/**
	 * Method getArgumentObject.
	 * Returns a argument Object that will hold the name and the stackframe.
	 * @param stack
	 * @param name
	 * @return ICDIArgumentObject
	 * @throws CDIException
	 */
	ICDIArgumentObject getArgumentObject(ICDIStackFrame stack, String name) throws CDIException;

	/**
	 * Method getArgumentObjects.
	 * Returns all the local arguments of that stackframe.
	 * @param stack
	 * @return ICDIArgumentObject[]
	 * @throws CDIException
	 */
	ICDIArgumentObject[] getArgumentObjects(ICDIStackFrame stack) throws CDIException;

	/**
	 * Method createArgument.
	 * Create a Variable for evaluation.  CreatedEvent will be trigger and
	 * ChangedEvent will also be trigger when the variable is assign a new value.
	 * DestroyedEvent is fired when the variable is out of scope and automatically
	 * from th manager list.
	 * @param var
	 * @return ICDIArgument
	 * @throws CDIException
	 */
	ICDIArgument createArgument(ICDIArgumentObject var) throws CDIException;

	/**
	 * Remove the variable from the manager list.
	 * @param var
	 * @return ICDIArgument
	 * @throws CDIException
	 */
	void destroyVariable(ICDIVariable var) throws CDIException;

}
