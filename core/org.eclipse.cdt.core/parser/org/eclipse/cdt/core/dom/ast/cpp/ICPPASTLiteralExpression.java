/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;

/**
 * C++ adds additional literal types to primary expression.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTLiteralExpression extends IASTLiteralExpression, ICPPASTExpression, IASTImplicitNameOwner {
	/**
	 * <code>lk_this</code> represents the 'this' keyword.
	 */
	public static final int lk_this = IASTLiteralExpression.lk_this;

	/**
	 * <code>lk_true</code> represents the 'true' keyword.
	 */
	public static final int lk_true = IASTLiteralExpression.lk_true;

	/**
	 * <code>lk_false</code> represents the 'false' keyword.
	 */
	public static final int lk_false = IASTLiteralExpression.lk_false;

	/**
	 * @deprecated All constants must be defined in {@link IASTLiteralExpression}.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final int lk_last = lk_false;

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTLiteralExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTLiteralExpression copy(CopyStyle style);
}
