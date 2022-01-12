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
 *     Mike Kucera (IBM)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;

/**
 * C++ adds a few more binary expressions over C.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTBinaryExpression extends IASTBinaryExpression, ICPPASTExpression, IASTImplicitNameOwner {
	/**
	 * <code>op_pmdot</code> pointer-to-member field dereference.
	 */
	public static final int op_pmdot = IASTBinaryExpression.op_pmdot;

	/**
	 * <code>op_pmarrow</code> pointer-to-member pointer dereference.
	 */
	public static final int op_pmarrow = IASTBinaryExpression.op_pmarrow;

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTBinaryExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTBinaryExpression copy(CopyStyle style);

	/**
	 * Returns the function binding for the overloaded operator, or <code>null</code> if
	 * the operator is not overloaded.
	 * @since 5.3
	 */
	public ICPPFunction getOverload();
}
