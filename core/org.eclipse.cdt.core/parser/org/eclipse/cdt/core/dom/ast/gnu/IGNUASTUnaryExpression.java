/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast.gnu;

import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

/**
 * There are GNU language extensions that apply to both GCC and G++. Unary
 * expressions for _alignOf() and typeof() along the lines of sizeof().
 * 
 * @author jcamelon
 */
public interface IGNUASTUnaryExpression extends IASTUnaryExpression {

	/**
	 * <code>op_typeof</code> is used for typeof( unaryExpression ) type
	 * expressions.
	 */
	public static final int op_typeof = IASTUnaryExpression.op_last + 1;

	/**
	 * <code>op_alignOf</code> is used for __alignOf( unaryExpression ) type
	 * expressions.
	 */
	public static final int op_alignOf = IASTUnaryExpression.op_last + 2;

	/**
	 * <code>op_last</code> is available for sub-interfaces.
	 */
	public static final int op_last = op_alignOf;
}
