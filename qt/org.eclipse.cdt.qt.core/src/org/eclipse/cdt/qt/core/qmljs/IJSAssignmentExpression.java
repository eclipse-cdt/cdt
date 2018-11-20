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
 * A JavaScript assignment expression from the
 * <a href="https://github.com/estree/estree/blob/master/spec.md#assignmentexpression">ESTree Specification</a>
 */
public interface IJSAssignmentExpression extends IJSExpression {
	/**
	 * An Enumeration covering the 12 assignment operators in JavaScript
	 */
	enum AssignmentOperator {
		Assign("="), //$NON-NLS-1$
		AssignAdd("+="), //$NON-NLS-1$
		AssignSubtract("-="), //$NON-NLS-1$
		AssignMultiply("*="), //$NON-NLS-1$
		AssignDivide("/="), //$NON-NLS-1$
		AssignModulus("%="), //$NON-NLS-1$
		AssignLeftShift("<<="), //$NON-NLS-1$
		AssignRightShift(">>="), //$NON-NLS-1$
		AssignUnsignedRightShift(">>>="), //$NON-NLS-1$
		AssignOr("|="), //$NON-NLS-1$
		AssignExclusiveOr("^"), //$NON-NLS-1$
		AssignAnd("&="); //$NON-NLS-1$

		public static AssignmentOperator fromObject(Object obj) {
			if (obj instanceof String) {
				for (AssignmentOperator op : AssignmentOperator.values()) {
					if (obj.equals(op.toString())) {
						return op;
					}
				}
			}
			return null;
		}

		private final String op;

		private AssignmentOperator(String op) {
			this.op = op;
		}

		@Override
		public String toString() {
			return this.op;
		}
	}

	@Override
	default String getType() {
		return "AssignmentExpression"; //$NON-NLS-1$
	}

	public AssignmentOperator getOperator();

	public IJSExpression getLeft();

	public IJSExpression getRight();
}
