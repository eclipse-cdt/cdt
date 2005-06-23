/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTCastExpression;

/**
 * C++ adds in additional cast-style expressions.
 * 
 * @author jcamelon
 */
public interface ICPPASTCastExpression extends IASTCastExpression {

	/**
	 * <code>op_dynamic_cast</code> is used for dynamic_cast<>'s.
	 */
	public static final int op_dynamic_cast = IASTCastExpression.op_last + 1;

	/**
	 * <code>op_static_cast</code> is used for static_cast<>'s.
	 */
	public static final int op_static_cast = IASTCastExpression.op_last + 2;

	/**
	 * <oode>op_reinterpret_cast</code> is used for reinterpret_cast<>'s.
	 */
	public static final int op_reinterpret_cast = IASTCastExpression.op_last + 3;

	/**
	 * <code>op_const_cast</code> is used for const_cast<>'s.
	 */
	public static final int op_const_cast = IASTCastExpression.op_last + 4;

	/**
	 * <code>op_last</code> is for subinterfaces to extend.
	 */
	public static final int op_last = op_const_cast;
}
