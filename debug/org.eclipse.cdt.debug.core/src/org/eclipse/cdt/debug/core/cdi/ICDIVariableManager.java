/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;

/**
 */
public interface ICDIVariableManager extends ICDISessionObject {

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
	public ICDIVariableObject getVariableObject(String filename, String function, String name) throws CDIException;

	/**
	 * Method createVariable.
	 * Use the current stackframe to return an ICDIVariable.
	 * A null stack means to use the current stackframe.
	 *
	 * @param stack
	 * @param name
	 * @return ICDIVariableObject
	 * @throws CDIException
	 */
	public ICDIVariableObject getVariableObject(ICDIStackFrame stack, String name) throws CDIException;

	/**
	 * Method getVariableObjects.
	 * Returns all the variable object of that context.
	 * @param stack
	 * @return ICDIVariableObject[]
	 * @throws CDIException
	 */
	public ICDIVariableObject[] getVariableObjects(ICDIStackFrame stack) throws CDIException;

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
	public ICDIVariable createVariable(ICDIVariableObject var) throws CDIException;


	/**
	 * Method getArgumentObject.
	 * Returns a argument Object that will hold the name and the context.
	 * @param stack
	 * @param name
	 * @return ICDIArgumentObject
	 * @throws CDIException
	 */
	public ICDIArgumentObject getArgumentObject(ICDIStackFrame stack, String name) throws CDIException;

	/**
	 * Method getArgumentObjects.
	 * Returns all the arguments of that context.
	 * @param stack
	 * @return ICDIArgumentObject[]
	 * @throws CDIException
	 */
	public ICDIArgumentObject[] getArgumentObjects(ICDIStackFrame stack) throws CDIException;

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
	public ICDIArgument createArgument(ICDIArgumentObject var) throws CDIException;


	/**
	 * Method createExpression.
	 * Create a Variable for evaluation.  A null stack means to use the current
	 * stackframe. A CreatedEvent will be trigger and
	 * ChangedEvent will also be trigger when the variable is assign a new value.
	 * No DestroyedEvent is trigger even when the variable goes out of scope.
	 * @param stack
	 * @param name
	 * @return ICDIExpression
	 * @throws CDIException
	 */
	public ICDIExpression createExpression(ICDIStackFrame stack, String name) throws CDIException;

	/**
	 * Method createRegister.
	 * @param stack
	 * @param reg
	 * @return ICDIRegister
	 * @throws CDIException
	 */
	public ICDIRegister createRegister(ICDIStackFrame stack, ICDIRegisterObject reg) throws CDIException;

	/**
	 * Method getRegisterObjects.
	 * @return ICDIRegisterObject[]
	 * @throws CDIException
	 */
	public ICDIRegisterObject[] getRegisterObjects() throws CDIException;

}
