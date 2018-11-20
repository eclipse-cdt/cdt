/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmljs;

/**
 * A JavaScript logical expression from the <a href="https://github.com/estree/estree/blob/master/spec.md#logicalexpression">ESTree
 * Specification</a>
 */
public interface IJSLogicalExpression extends IJSExpression {
	/**
	 * An Enumeration covering the two logical operators in JavaScript
	 */
	enum LogicalOperator {
		Or("||"), //$NON-NLS-1$
		And("&&"); //$NON-NLS-1$

		public static LogicalOperator fromObject(Object obj) {
			if (obj instanceof String) {
				for (LogicalOperator op : LogicalOperator.values()) {
					if (obj.equals(op.toString())) {
						return op;
					}
				}
			}
			return null;
		}

		private final String op;

		private LogicalOperator(String op) {
			this.op = op;
		}

		@Override
		public String toString() {
			return this.op;
		}
	}

	@Override
	default String getType() {
		return "LogicalExpression"; //$NON-NLS-1$
	}

	public LogicalOperator getOperator();

	public IJSExpression getLeft();

	public IJSExpression getRight();
}
