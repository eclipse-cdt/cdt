/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator.EvalException;

/**
 * Represents values of variables, enumerators or expressions. The primary purpose of the representation
 * is to support instantiation of templates with non-type template parameters. 
 */
public class Value implements IValue {
	public static final int MAX_RECURSION_DEPTH = 25;
	public final static IValue UNKNOWN= new Value("<unknown>"); //$NON-NLS-1$
	
	private final static IValue[] TYPICAL= {new Value(String.valueOf(0)), 
		new Value(String.valueOf(1)), new Value(String.valueOf(2)), new Value(String.valueOf(3)), 
		new Value(String.valueOf(4)), new Value(String.valueOf(5)), new Value(String.valueOf(6))};

	
	private static class UnknownValueException extends Exception {}
	private static UnknownValueException UNKNOWN_EX= new UnknownValueException();

	private final String fValue;
	private Value(String rep) {
		fValue= rep;
	}
	public String getCanonicalRepresentation() {
		return fValue;
	}

	public Long numericalValue() {
		try {
			return Long.parseLong(fValue);
		} catch (NumberFormatException e) {
		}
		return null;
	}

	public static IValue create(long value) {
		if (value >=0 && value < TYPICAL.length)
			return TYPICAL[(int) value];
		return new Value(String.valueOf(value));
	}

	public static IValue create(IASTExpression expr, int maxRecursionDepth) {
		try {
			Object obj= evaluate(expr, maxRecursionDepth);
			if (obj instanceof Long)
				return create(((Long) obj).longValue());
			return new Value(obj.toString());
		} catch (UnknownValueException e) {
		}
		return UNKNOWN;
	}
	
	public static IValue fromCanonicalRepresentation(String rep) {
		if (rep.equals(UNKNOWN.getCanonicalRepresentation()))
			return UNKNOWN;
		
		try {
			return create(Long.parseLong(rep));
		} catch (NumberFormatException e) {}
	
		return new Value(rep);
	}

	/**
	 * Computes the canonical representation of the value of the expression. Returns a {@code Long} for
	 * numerical values or a {@code String}, otherwise.
	 * @throws UnknownValueException
	 */
	@SuppressWarnings("nls")
	private static Object evaluate(IASTExpression e, int maxdepth) throws UnknownValueException {
		if (maxdepth < 0 || e == null)
			throw UNKNOWN_EX;
		
		if (e instanceof IASTArraySubscriptExpression) {
			IASTArraySubscriptExpression sub= (IASTArraySubscriptExpression) e;
			return evaluate(sub.getArrayExpression(), maxdepth) + "," + 
					evaluate(sub.getSubscriptExpression(), maxdepth) + ",[]"; 
		}
		if (e instanceof IASTBinaryExpression) {
			return evaluateBinaryExpression((IASTBinaryExpression) e, maxdepth);
		}
		if (e instanceof IASTUnaryExpression) {
			return evaluateUnaryExpression((IASTUnaryExpression) e, maxdepth);
		}
		if (e instanceof IASTCastExpression) {
			return evaluate(((IASTCastExpression) e).getOperand(), maxdepth);
		}
		if (e instanceof IASTConditionalExpression) {
			IASTConditionalExpression cexpr= (IASTConditionalExpression) e;
			Object o= evaluate(cexpr.getLogicalConditionExpression(), maxdepth);
			if (o instanceof Long) {
				Long v= (Long) o;
				if (v.longValue() == 0) {
					return evaluate(cexpr.getNegativeResultExpression(), maxdepth);
				}
				final IASTExpression pe = cexpr.getPositiveResultExpression();
				if (pe == null) // gnu-extension allows to omit the positive expression.
					return o;
				return evaluate(pe, maxdepth);
			}

			final IASTExpression pe = cexpr.getPositiveResultExpression();
			Object po= pe == null ? o : evaluate(pe, maxdepth);
			return o + "," + evaluate(cexpr.getNegativeResultExpression(), maxdepth) + "," + po + '?';
		}
		if (e instanceof IASTIdExpression) {
			IBinding b= ((IASTIdExpression) e).getName().resolveBinding();
			if (b instanceof ICPPTemplateParameter) {
				if (b instanceof ICPPTemplateNonTypeParameter) {
					return evaluate((ICPPTemplateParameter) b);
				}
				throw UNKNOWN_EX;
			}
			IValue cv= null;
			if (b instanceof IInternalVariable) {
				cv= ((IInternalVariable) b).getInitialValue(maxdepth-1);
			} else if (b instanceof IVariable) {
				cv= ((IVariable) b).getInitialValue();
			} else if (b instanceof IEnumerator) {
				cv= ((IEnumerator) b).getValue();
			}
			if (cv != null)
				return toObject(cv);
			
			try {
				if (b instanceof ICPPBinding)
					return ((ICPPBinding) b).getQualifiedName();
				return b.getName();
			} catch (DOMException e1) {
				throw UNKNOWN_EX;
			}
		}
		if (e instanceof IASTLiteralExpression) {
			if (e.getExpressionType() instanceof IBasicType) {
				try {
					return ExpressionEvaluator.getNumber(e.toString().toCharArray());
				} catch (EvalException e1) {
					throw UNKNOWN_EX;
				}
			}
			return e.toString();
		}
		throw UNKNOWN_EX;
	}
	
	private static Object toObject(IValue cv) throws UnknownValueException {
		if (cv == Value.UNKNOWN) 
			throw UNKNOWN_EX;
		
		Long lv= cv.numericalValue();
		if (lv != null)
			return lv;
		return cv.getCanonicalRepresentation();
	}
	
	@SuppressWarnings("nls")
	private static String evaluate(ICPPTemplateParameter param) {
		// mstodo add support for parameter positions first.
		return "#" ;//+Integer.toHexString(param.getParameterPosition());
	}
	
	@SuppressWarnings("nls")
	private static Object evaluateUnaryExpression(IASTUnaryExpression ue, int maxdepth) throws UnknownValueException {
		final int unaryOp= ue.getOperator();
		if (unaryOp == IASTUnaryExpression.op_amper || unaryOp == IASTUnaryExpression.op_star) 
			throw UNKNOWN_EX;
			
		final Object value= evaluate(ue.getOperand(), maxdepth);
		if (value instanceof Long) {
			long v= (Long) value;
			switch(unaryOp) {
			case IASTUnaryExpression.op_prefixIncr:
			case IASTUnaryExpression.op_postFixIncr:
				return ++v;
			case IASTUnaryExpression.op_prefixDecr :
			case IASTUnaryExpression.op_postFixDecr:
				return --v;
			case IASTUnaryExpression.op_bracketedPrimary:
			case IASTUnaryExpression.op_plus:
				return value;
			case IASTUnaryExpression.op_minus:
				return -v;
			case IASTUnaryExpression.op_tilde:
				return ~v;
			case IASTUnaryExpression.op_not:
				return v == 0 ? 1 : 0;
			}
		}
		switch (unaryOp) {
		case IASTUnaryExpression.op_bracketedPrimary:
		case IASTUnaryExpression.op_plus:
			return value;
		}
		
		return value + "," + ASTSignatureUtil.getUnaryOperatorString(ue); 
	}

	@SuppressWarnings("nls")
	private static Object evaluateBinaryExpression(IASTBinaryExpression be, int maxdepth) throws UnknownValueException {
		final Object o1= evaluate(be.getOperand1(), maxdepth);
		final Object o2= evaluate(be.getOperand2(), maxdepth);

		final int op= be.getOperator();
		if (o1 instanceof Long && o2 instanceof Long) {
			long v1= (Long) o1;
			long v2= (Long) o2;
			switch(op) {
			case IASTBinaryExpression.op_multiply:
				return v1*v2;
			case IASTBinaryExpression.op_divide:
				if (v2 == 0)
					throw UNKNOWN_EX;
				return v1/v2;
			case IASTBinaryExpression.op_modulo:
				if (v2 == 0)
					throw UNKNOWN_EX;
				return v1 % v2;
			case IASTBinaryExpression.op_plus:
				return v1+v2;
			case IASTBinaryExpression.op_minus:
				return v1-v2;
			case IASTBinaryExpression.op_shiftLeft:
				return v1 << v2;
			case IASTBinaryExpression.op_shiftRight:
				return v1 >> v2;
			case IASTBinaryExpression.op_lessThan:
				return v1 < v2 ? 1 : 0;
			case IASTBinaryExpression.op_greaterThan:
				return v1 > v2 ? 1 : 0;
			case IASTBinaryExpression.op_lessEqual:
				return v1 <= v2 ? 1 : 0;
			case IASTBinaryExpression.op_greaterEqual:
				return v1 >= v2 ? 1 : 0;
			case IASTBinaryExpression.op_binaryAnd:
				return v1&v2;
			case IASTBinaryExpression.op_binaryXor:
				return v1^v2;
			case IASTBinaryExpression.op_binaryOr:
				return v1|v2;
			case IASTBinaryExpression.op_logicalAnd:
				return v1 != 0 && v2 != 0 ? 1 : 0;
			case IASTBinaryExpression.op_logicalOr:
				return v1 != 0 || v2 != 0 ? 1 : 0;
			case IASTBinaryExpression.op_equals:
				return v1 == v2 ? 1 : 0;
			case IASTBinaryExpression.op_notequals:
				return v1 != v2 ? 1 : 0;
            case IASTBinaryExpression.op_max:
				return Math.max(v1, v2);
            case IASTBinaryExpression.op_min:
				return Math.min(v1, v2);
			}
		}
		switch (op) {
		case IASTBinaryExpression.op_equals:
			return o1.equals(o2) ? 1 : 0;
		case IASTBinaryExpression.op_notequals:
			return !o1.equals(o2) ? 1 : 0;
		}
		
		return o1 + "," + o2 + "," + ASTSignatureUtil.getBinaryOperatorString(be);
	}
}
