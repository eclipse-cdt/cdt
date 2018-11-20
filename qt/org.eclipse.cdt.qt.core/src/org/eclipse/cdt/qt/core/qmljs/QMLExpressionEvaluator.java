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

public final class QMLExpressionEvaluator {
	private QMLExpressionEvaluator() {
	}

	public static class InvalidExpressionException extends Exception {

		private static final long serialVersionUID = 4803923632666457229L;

		private IJSExpression offendingExpression;

		public InvalidExpressionException(String msg, IJSExpression expr) {
			super(msg);
			this.offendingExpression = expr;
		}

		public IJSExpression getOffendingExpression() {
			return offendingExpression;
		}
	}

	/**
	 * Evaluates the given {@link IJSExpression} as a constant expression and returns the result. At the moment this only supports
	 * very simple expressions involving unary operators and literals alone. Support for more complex expressions will be added as
	 * needed.
	 *
	 * @param expr
	 *            the expression to be evaluated
	 *
	 * @throws InvalidExpressionException
	 *             if the given expression can't be reduced to a single constant
	 */
	public static Object evaluateConstExpr(IJSExpression expr) throws InvalidExpressionException {
		if (expr instanceof IJSLiteral) {
			return ((IJSLiteral) expr).getValue();
		} else if (expr instanceof IJSUnaryExpression) {
			IJSUnaryExpression unary = (IJSUnaryExpression) expr;
			Object arg = evaluateConstExpr(unary.getArgument());
			switch (unary.getOperator()) {
			case Plus:
				return unaryPlus(arg, unary.getArgument());
			case Negation:
				return unaryNegate(arg, unary.getArgument());
			case BitwiseNot:
				return unaryBitwiseNot(arg, unary.getArgument());
			case Not:
				return unaryNot(arg, unary.getArgument());
			default:
			}
		}
		throw new InvalidExpressionException("Cannot reduce '" + expr + "' to a constant", expr); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static Object unaryPlus(Object object, IJSExpression expr) throws InvalidExpressionException {
		if (object instanceof Byte) {
			return +((Byte) object);
		} else if (object instanceof Short) {
			return +((Short) object);
		} else if (object instanceof Integer) {
			return +((Integer) object);
		} else if (object instanceof Long) {
			return +((Long) object);
		} else if (object instanceof Float) {
			return +((Float) object);
		} else if (object instanceof Double) {
			return +((Double) object);
		}
		throw new InvalidExpressionException("Cannot perform unary plus operation on a non-number", expr); //$NON-NLS-1$
	}

	private static Object unaryNegate(Object object, IJSExpression expr) throws InvalidExpressionException {
		if (object instanceof Byte) {
			return -((Byte) object);
		} else if (object instanceof Short) {
			return -((Short) object);
		} else if (object instanceof Integer) {
			return -((Integer) object);
		} else if (object instanceof Long) {
			return -((Long) object);
		} else if (object instanceof Float) {
			return -((Float) object);
		} else if (object instanceof Double) {
			return -((Double) object);
		}
		throw new InvalidExpressionException("Cannot perform unary negation operation on a non-number", expr); //$NON-NLS-1$
	}

	private static Object unaryBitwiseNot(Object object, IJSExpression expr) throws InvalidExpressionException {
		if (object instanceof Byte) {
			return ~((Byte) object);
		} else if (object instanceof Short) {
			return ~((Short) object);
		} else if (object instanceof Integer) {
			return ~((Integer) object);
		} else if (object instanceof Long) {
			return ~((Long) object);
		} else if (object instanceof Float || object instanceof Double) {
			return ~((Number) object).longValue();
		}
		throw new InvalidExpressionException("Cannot perform binary not operation on a non-number", expr); //$NON-NLS-1$
	}

	private static Object unaryNot(Object object, IJSExpression expr) throws InvalidExpressionException {
		if (object instanceof Boolean) {
			return !((Boolean) object);
		}
		throw new InvalidExpressionException("Cannot perform unary not operation on a non-boolean", expr); //$NON-NLS-1$
	}
}
