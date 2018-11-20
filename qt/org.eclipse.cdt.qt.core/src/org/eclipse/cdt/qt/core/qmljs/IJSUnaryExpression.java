/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmljs;

/**
 * A JavaScript unary expression from the <a href="https://github.com/estree/estree/blob/master/spec.md#unaryexpression">ESTree
 * Specification</a>
 */
public interface IJSUnaryExpression extends IJSExpression {
	/**
	 * An Enumeration covering the 7 unary operators in JavaScript
	 */
	enum UnaryOperator {
		Negation("-"), //$NON-NLS-1$
		Plus("+"), //$NON-NLS-1$
		Not("!"), //$NON-NLS-1$
		BitwiseNot("~"), //$NON-NLS-1$
		Typeof("typeof"), //$NON-NLS-1$
		Void("void"), //$NON-NLS-1$
		Delete("delete"); //$NON-NLS-1$

		public static UnaryOperator fromObject(Object obj) {
			if (obj instanceof String) {
				for (UnaryOperator op : UnaryOperator.values()) {
					if (obj.equals(op.toString())) {
						return op;
					}
				}
			}
			return null;
		}

		private final String op;

		private UnaryOperator(String op) {
			this.op = op;
		}

		@Override
		public String toString() {
			return this.op;
		}
	}

	@Override
	default String getType() {
		return "UnaryExpression"; //$NON-NLS-1$
	}

	public UnaryOperator getOperator();

	public boolean isPrefix();

	public IJSExpression getArgument();
}
