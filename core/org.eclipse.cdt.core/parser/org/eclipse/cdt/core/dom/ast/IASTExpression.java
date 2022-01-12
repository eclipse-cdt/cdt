/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the root class of expressions.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTExpression extends IASTInitializerClause {
	/**
	 * @since 5.3
	 */
	public enum ValueCategory {
		/**
		 * Traditional lvalue
		 */
		LVALUE,
		/**
		 * Expiring value as introduced by c++11.
		 */
		XVALUE,
		/**
		 * Pure rvalue.
		 */
		PRVALUE;

		/**
		 * Both prvalues and xvalues are rvalues.
		 */
		public boolean isRValue() {
			return this != LVALUE;
		}

		/**
		 * A generalized lvalue is either an lvalue or an xvalue.
		 */
		public boolean isGLValue() {
			return this != PRVALUE;
		}
	}

	/**
	 * Empty expression array.
	 */
	public static final IASTExpression[] EMPTY_EXPRESSION_ARRAY = {};

	/**
	 * Returns the type of the value the expression evaluates to.
	 */
	public IType getExpressionType();

	/**
	 * Returns whether this expression is an lvalue. LValues are for instance required on
	 * the left hand side of an assignment expression.
	 * @since 5.2
	 */
	public boolean isLValue();

	/**
	 * Returns the value category of this expression.
	 * @since 5.3
	 */
	ValueCategory getValueCategory();

	/**
	 * @since 5.1
	 */
	@Override
	public IASTExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTExpression copy(CopyStyle style);
}
