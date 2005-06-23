/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.debug;

public interface IDebugEntryRequestor {

	/**
	 * Entering a compilation unit.
	 * @param name
	 * @param address start of address of the cu.
	 */
	void enterCompilationUnit(String name, long address);

	/**
	 * Exit the current compilation unit.
	 * @param address end of compilation unit.
	 */
	void exitCompilationUnit(long address);

	/**
	 * Entering new include file in a compilation unit.
	 * @param name
	 */
	void enterInclude(String name);

	/**
	 * Exit the current include file.
	 */
	void exitInclude();

	/**
	 * Enter a function.
	 * @param name of the function/method
	 * @param type type of the return value.
	 * @param isGlobal return the visiblity of the function.
	 * @param address the start address of the function.
	 */
	void enterFunction(String name, DebugType type, boolean isGlobal, long address);

	/**
	 * Exit the current function.
	 * @param address the address where the function ends.
	 */
	void exitFunction(long address);

	/**
	 * Enter a code block in a function.
	 * @param offset address of the block starts relative to the current function.
	 */
	void enterCodeBlock(long offset);

	/**
	 * Exit of the current code block.
	 * @param offset the address of which the blocks ends relative to the current function.
	 */
	void exitCodeBlock(long offset);

	/**
	 * Statement in the compilation unit with a given address.
	 * @param line lineno of the statement relative to the current compilation unit.
	 * @param offset addres of the statement relative to the current function.
	 */
	void acceptStatement(int line, long address);

	/**
	 * Integer constant.
	 * @param name
	 * @param address.
	 */
	void acceptIntegerConst(String name, int value);

	/**
	 *  floating point constant.
	 * @param name
	 * @param value
	 */
	void acceptFloatConst(String name, double value);

	/**
	 * Type constant: "const b = 0", b is a type enum.
	 * @param name
	 * @param type
	 * @param address
	 */
	void acceptTypeConst(String name, DebugType type, int value);

	/**
	 * Caught Exception.
	 * @param name
	 * @param value
	 */
	void acceptCaughtException(String name, DebugType type, long address);

	/**
	 * Accept a parameter for the current function.
	 * @param name of the parameter
	 * @param type of the parameter
	 * @param kind of the parameter
	 * @param offset address of the parameter relative to the current function.
	 */
	void acceptParameter(String name, DebugType type, DebugParameterKind kind, long offset);

	/**
	 * Record a variable.
	 * @param name
	 * @param type
	 * @param kind
	 * @param address
	 */
	void acceptVariable(String name, DebugType type, DebugVariableKind kind, long address);

	/**
	 * Type definition.
	 * IDebugEntryRequestor
	 * @param name new name
	 * @param type
	 */
	void acceptTypeDef(String name, DebugType type);
	
}
