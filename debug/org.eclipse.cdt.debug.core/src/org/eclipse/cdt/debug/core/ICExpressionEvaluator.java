/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

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
