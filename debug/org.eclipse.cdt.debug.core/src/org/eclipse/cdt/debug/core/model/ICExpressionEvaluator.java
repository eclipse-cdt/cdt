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
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * 
 * Supports the evaluation of C/C++ expressions.
 * 
 * @since Sep 13, 2002
 */
public interface ICExpressionEvaluator
{
	/**
	 * Evaluates the specified expression and returns evaluation result 
	 * as a string.
	 * 
	 * @param expression the expression to evaluate
	 * @return the evaluation result
	 * @throws DebugException on failure. Reasons include:
	 */
	String evaluateExpressionToString( String expression ) throws DebugException;
	
	/**
	 * Returns whether this object can currently evaluate an expression.
	 * 
	 * @return whether this object can currently evaluate an expression
	 */
	boolean canEvaluate();
}
