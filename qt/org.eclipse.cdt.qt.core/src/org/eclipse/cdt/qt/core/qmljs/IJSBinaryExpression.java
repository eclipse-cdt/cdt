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
 * A JavaScript binary expression from the <a href="https://github.com/estree/estree/blob/master/spec.md#binaryexpression">ESTree
 * Specification</a>
 */
public interface IJSBinaryExpression extends IJSExpression {
	/**
	 * An Enumeration covering the 21 binary operators in JavaScript
	 */
	enum BinaryOperator {
		Equality("=="), //$NON-NLS-1$
		Inequality("!="), //$NON-NLS-1$
		StrictEquality("==="), //$NON-NLS-1$
		LessThan("<"), //$NON-NLS-1$
		LessThanOrEqual("<="), //$NON-NLS-1$
		GreaterThan(">"), //$NON-NLS-1$
		GreaterThanOrEqual(">="), //$NON-NLS-1$
		LeftShift("<<"), //$NON-NLS-1$
		RightShift(">>"), //$NON-NLS-1$
		UnsignedRightShift(">>>"), //$NON-NLS-1$
		Add("+"), //$NON-NLS-1$
		Subtract("-"), //$NON-NLS-1$
		Multiply("*"), //$NON-NLS-1$
		Divide("/"), //$NON-NLS-1$
		Modulus("%"), //$NON-NLS-1$
		Or("|"), //$NON-NLS-1$
		EclusiveOr("^"), //$NON-NLS-1$
		And("&"), //$NON-NLS-1$
		In("in"), //$NON-NLS-1$
		Instanceof("instanceof"); //$NON-NLS-1$

		public static BinaryOperator fromObject(Object obj) {
			if (obj instanceof String) {
				for (BinaryOperator op : BinaryOperator.values()) {
					if (obj.equals(op.toString())) {
						return op;
					}
				}
			}
			return null;
		}

		private final String op;

		private BinaryOperator(String op) {
			this.op = op;
		}

		@Override
		public String toString() {
			return this.op;
		}
	}

	@Override
	default String getType() {
		return "BinaryExpression"; //$NON-NLS-1$
	}

	public BinaryOperator getOperator();

	public IJSExpression getLeft();

	public IJSExpression getRight();
}
