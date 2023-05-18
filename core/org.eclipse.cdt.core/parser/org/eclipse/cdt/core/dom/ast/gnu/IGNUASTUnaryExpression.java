/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu;

import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

/**
 * @noreference This interface is not intended to be referenced by clients.
 */
public interface IGNUASTUnaryExpression extends IASTUnaryExpression {

	/**
	 * For GCC parsers, only. {@code op_labelReference} is used for &amp;&amp;label type expressions.
	 * @since 8.2
	 */
	public static final int op_labelReference = 18;

	/**
	 * For GCC parsers in C++ mode, only: '__integer_pack ( expression )'
	 * @since 8.2
	 */
	public static final int op_integerPack = 19;

	/**
	 * @since 5.1
	 */
	@Override
	public IGNUASTUnaryExpression copy();
}
