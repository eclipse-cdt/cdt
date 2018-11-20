/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTCastExpression;

/**
 * C++ adds in additional cast-style expressions.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTCastExpression extends IASTCastExpression, ICPPASTExpression {
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

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTCastExpression copy();

	/**
	 * @since 5.4
	 */
	@Override
	public ICPPASTCastExpression copy(CopyStyle style);
}
