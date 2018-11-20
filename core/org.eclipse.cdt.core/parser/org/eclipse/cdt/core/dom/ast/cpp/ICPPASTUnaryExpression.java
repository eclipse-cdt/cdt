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
 *     Mike Kucera
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTUnaryExpression extends IASTUnaryExpression, ICPPASTExpression, IASTImplicitNameOwner {
	/**
	 * <code>op_throw</code> throw exp
	 */
	public static final int op_throw = IASTUnaryExpression.op_throw;

	/**
	 * <code>op_typeid</code> = typeid( exp )
	 */
	public static final int op_typeid = IASTUnaryExpression.op_typeid;

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTUnaryExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTUnaryExpression copy(CopyStyle style);

	/**
	 * Returns the function binding for the overloaded operator, or <code>null</code> if
	 * the operator is not overloaded.
	 * @since 5.4
	 */
	public ICPPFunction getOverload();
}
