/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -data-evaluate-expression EXPR
 *
 *   Evaluate EXPR as an expression.  The expression could contain an
 *inferior function call.  The function call will execute synchronously.
 *If the expression contains spaces, it must be enclosed in double quotes.
 *
 */
public class MIDataEvaluateExpression extends MICommand 
{
	public MIDataEvaluateExpression(String expr) {
		super("-data-evaluate-expression", new String[]{expr});
	}
}
