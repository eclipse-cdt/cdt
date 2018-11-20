/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;

/**
 * C/C++ specific extension of <code>IStackFrame</code>.
 */
public interface ICStackFrame
		extends IRunToLine, IRunToAddress, IResumeAtLine, IResumeAtAddress, IStackFrame, ICDebugElement {

	/**
	 * Returns the address of this stack frame.
	 *
	 * @return the address of this stack frame
	 */
	public IAddress getAddress();

	/**
	 * Returns the source file of this stack frame or <code>null</code>
	 * if the source file is unknown.
	 *
	 * @return the source file of this stack frame
	 */
	public String getFile();

	/**
	 * Returns the function of this stack frame or <code>null</code>
	 * if the function is unknown.
	 *
	 * @return the function of this stack frame
	 */
	public String getFunction();

	/**
	 * Returns the line number of this stack frame or <code>0</code>
	 * if the line number is unknown.
	 *
	 * @return the line number of this stack frame
	 */
	public int getFrameLineNumber();

	/**
	 * Returns the level of this stack frame.
	 *
	 * @return the level of this stack frame
	 */
	public int getLevel();

	/**
	 * Evaluates the given expression in the context of this stack frame.
	 *
	 * @param expression expression to evaluate
	 * @return the evaluation result
	 * @throws DebugException if this method fails.
	 */
	public IValue evaluateExpression(String expression) throws DebugException;

	/**
	 * Evaluates the specified expression in the context of this stack frame
	 * and returns the evaluation result as a string.
	 *
	 * @param expression the expression to evaluate
	 * @return the evaluation result
	 * @throws DebugException on failure. Reasons include:
	 */
	public String evaluateExpressionToString(String expression) throws DebugException;

	/**
	 * Returns whether this stack frame can currently evaluate an expression.
	 *
	 * @return whether this stack frame can currently evaluate an expression
	 */
	boolean canEvaluate();
}
