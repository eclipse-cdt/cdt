/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;

/**
 * C/C++ specific extension of <code>IStackFrame</code>.
 */
public interface ICStackFrame extends IStackFrame, ICDebugElement {

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
	public IValue evaluateExpression( String expression ) throws DebugException;
}
