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
 * A JavaScript update expression from the <a href="https://github.com/estree/estree/blob/master/spec.md#updateexpression">ESTree
 * Specification</a>
 */
public interface IJSUpdateExpression extends IQmlASTNode {
	/**
	 * An Enumeration covering the two update operators in JavaScript
	 */
	enum UpdateOperator {
		Decrement("--"), //$NON-NLS-1$
		Increment("++"); //$NON-NLS-1$

		public static UpdateOperator fromObject(Object obj) {
			if (obj instanceof String) {
				for (UpdateOperator op : UpdateOperator.values()) {
					if (obj.equals(op.toString())) {
						return op;
					}
				}
			}
			return null;
		}

		private final String op;

		private UpdateOperator(String op) {
			this.op = op;
		}

		@Override
		public String toString() {
			return this.op;
		}
	}

	@Override
	default String getType() {
		return "UpdateExpression"; //$NON-NLS-1$
	}

	public UpdateOperator getOperator();

	public IJSExpression getArgument();

	public boolean isPrefix();
}
