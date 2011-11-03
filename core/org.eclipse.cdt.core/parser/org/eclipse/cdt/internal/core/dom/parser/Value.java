/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator.SizeAndAlignment;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator.EvalException;
import org.eclipse.cdt.internal.core.pdom.db.TypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents values of variables, enumerators or expressions. The primary purpose of the representation
 * is to support instantiation of templates with non-type template parameters. 
 */
public class Value implements IValue {
	public static final int MAX_RECURSION_DEPTH = 25;
	public final static IValue UNKNOWN= new Value("<unknown>".toCharArray(), ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY); //$NON-NLS-1$
	public final static IValue NOT_INITIALIZED= new Value("<__>".toCharArray(), ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY); //$NON-NLS-1$
	private static final int[] NO_INT = {};
	
	private static final String SCOPE_OP = "::"; //$NON-NLS-1$
	private static final char UNIQUE_CHAR = '_';
	private static final char TEMPLATE_PARAM_CHAR = '#';
	private static final char TEMPLATE_PARAM_PACK_CHAR = '`';
	private static final char REFERENCE_CHAR = '&';
	private static final char UNARY_OP_CHAR = '$';
	private static final char BINARY_OP_CHAR = '@';
	private static final char CONDITIONAL_CHAR= '?';
	
	private static final char SEPARATOR = ',';

	private final static IValue[] TYPICAL= {
		new Value(new char[] {'0'}, ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY), 
		new Value(new char[] {'1'}, ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY), 
		new Value(new char[] {'2'}, ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY), 
		new Value(new char[] {'3'}, ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY), 
		new Value(new char[] {'4'}, ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY), 
		new Value(new char[] {'5'}, ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY), 
		new Value(new char[] {'6'}, ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY)};


	private static class Reevaluation {
		public final char[] fExpression;
		private int fPackOffset;
		public int pos=0;
		public final Map<String, Integer> fUnknownSigs;
		public final List<ICPPUnknownBinding> fUnknowns;
		public final IBinding[] fResolvedUnknown;
		public final ICPPTemplateParameterMap fMap;

		public Reevaluation(char[] expr, int packOffset, Map<String, Integer> unknownSigs,
				List<ICPPUnknownBinding> unknowns, IBinding[] resolvedUnknowns, ICPPTemplateParameterMap map) {
			fExpression= expr;
			fPackOffset= packOffset;
			fUnknownSigs= unknownSigs;
			fUnknowns= unknowns;
			fResolvedUnknown= resolvedUnknowns;
			fMap= map;
		}
		public void nextSeperator() throws UnknownValueException {
			final char[] expression = fExpression;
			final int len = expression.length;
			int idx = pos;
			while(idx < len) {
				if (expression[idx++] == SEPARATOR)
					break;
			}
			pos= idx;
		}
	}

	private static class UnknownValueException extends Exception {}
	private static UnknownValueException UNKNOWN_EX= new UnknownValueException();
	private static int sUnique=0;

	private final char[] fExpression;
	private final ICPPUnknownBinding[] fUnknownBindings;
	private char[] fSignature;
	
	private Value(char[] rep, ICPPUnknownBinding[] unknown) {
		assert rep != null;
		fExpression= rep;
		fUnknownBindings= unknown;
	}
	
	@Override
	public char[] getInternalExpression() {
		return fExpression;
	}

	@Override
	public IBinding[] getUnknownBindings() {
		return fUnknownBindings;
	}
	
	@Override
	public char[] getSignature() {
		if (fSignature == null) {
			if (fUnknownBindings.length == 0) {
				fSignature= fExpression;
			} else {
				StringBuilder buf= new StringBuilder();
				buf.append(fExpression);
				buf.append('[');
				for (int i = 0; i < fUnknownBindings.length; i++) {
					if (i > 0)
						buf.append(',');
					buf.append(getSignatureForUnknown(fUnknownBindings[i]));
				}
				buf.append(']');
				final int end= buf.length();
				fSignature= new char[end];
				buf.getChars(0, end, fSignature, 0);
			}
		}
		return fSignature;
	}
	
	@Override
	public Long numericalValue() {
		return parseLong(fExpression);
	}
	
	public void marshall(TypeMarshalBuffer buf) throws CoreException {
		if (UNKNOWN == this) {
			buf.putByte((byte) (ITypeMarshalBuffer.VALUE | ITypeMarshalBuffer.FLAG1));
		} else {
			Long num= numericalValue();
			if (num != null) {
				long lv= num;
				if (lv >= Integer.MIN_VALUE && lv <= Integer.MAX_VALUE) {
					buf.putByte((byte) (ITypeMarshalBuffer.VALUE | ITypeMarshalBuffer.FLAG2));
					buf.putInt((int) lv);
				} else {
					buf.putByte((byte) (ITypeMarshalBuffer.VALUE | ITypeMarshalBuffer.FLAG3));
					buf.putLong(lv);
				}
			} else {
				buf.putByte((ITypeMarshalBuffer.VALUE));
				buf.putCharArray(fExpression);
				buf.putShort((short) fUnknownBindings.length);
				for (ICPPUnknownBinding b : fUnknownBindings) {
					buf.marshalBinding(b);
				}
			}
		}
	}
	
	public static IValue unmarshal(TypeMarshalBuffer buf) throws CoreException {
		int firstByte= buf.getByte();
		if (firstByte == TypeMarshalBuffer.NULL_TYPE)
			return null;
		if ((firstByte & ITypeMarshalBuffer.FLAG1) != 0) 
			return Value.UNKNOWN;
		if ((firstByte & ITypeMarshalBuffer.FLAG2) != 0) {
			int val= buf.getInt();
			return Value.create(val);
		}
		if ((firstByte & ITypeMarshalBuffer.FLAG3) != 0) {
			long val= buf.getLong();
			return Value.create(val);
		}
		
		char[] expr = buf.getCharArray();
		final int len= buf.getShort();
		ICPPUnknownBinding[] unknowns= new ICPPUnknownBinding[len];
		for (int i = 0; i < unknowns.length; i++) {
			final ICPPUnknownBinding unknown = (ICPPUnknownBinding) buf.unmarshalBinding();
			if (unknown == null) {
				return Value.UNKNOWN;
			}
			unknowns[i]= unknown;
		}
		return new Value(expr, unknowns);
	}

	@Override
	public int hashCode() {
		return CharArrayUtils.hash(fExpression);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IValue)) {
			return false;
		}
		final IValue rhs = (IValue) obj;
		if (!CharArrayUtils.equals(fExpression, rhs.getInternalExpression()))
			return false;
		
		IBinding[] rhsUnknowns= rhs.getUnknownBindings();
		if (fUnknownBindings.length != rhsUnknowns.length)
			return false;
		
		for (int i = 0; i < rhsUnknowns.length; i++) {
			final IBinding rhsUnknown = rhsUnknowns[i];
			if (rhsUnknown instanceof ICPPUnknownBinding) {
				if (!getSignatureForUnknown((ICPPUnknownBinding) rhsUnknown).equals(getSignatureForUnknown(fUnknownBindings[i]))) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		return new String(getSignature());
	}

	/**
	 * Creates a value representing the given number.
	 */
	public static IValue create(long value) {
		if (value >=0 && value < TYPICAL.length)
			return TYPICAL[(int) value];
		return new Value(toCharArray(value), ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY);
	}
	
	/**
	 * Creates a value representing the given template parameter.
	 */
	public static IValue create(ICPPTemplateNonTypeParameter tntp) {
		final String expr = createTemplateParamExpression(tntp.getParameterID(), tntp.isParameterPack());
		return new Value(expr.toCharArray(), ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY);
	}

	private static String createTemplateParamExpression(int id, boolean isPack) {
		StringBuilder buf= new StringBuilder();
		buf.append(isPack ? TEMPLATE_PARAM_PACK_CHAR : TEMPLATE_PARAM_CHAR);
		buf.append(Integer.toHexString(id));
		return buf.toString();
	}

	/**
	 * Tests whether the value is a template parameter (or parameter pack), 
	 * returns the parameter id of the parameter, or <code>-1</code> if it is not a template parameter.
	 */
	public static int isTemplateParameter(IValue tval) {
		final char[] rep= tval.getInternalExpression();
		if (rep.length > 0) {
			final char c = rep[0];
			if (c == TEMPLATE_PARAM_CHAR || c == TEMPLATE_PARAM_PACK_CHAR) {
				for (int i = 1; i < rep.length; i++) {
					if (rep[i] == SEPARATOR)
						return -1;
				}
				try {
					return parseHex(rep, 1);
				} catch (UnknownValueException e) {
				}
			}
		}
		return -1;
	}
	
	/**
	 * Tests whether the value directly references some template parameter.
	 */
	public static boolean referencesTemplateParameter(IValue tval) {
		final char[] rep= tval.getInternalExpression();
		for (char element : rep) {
			if (element == TEMPLATE_PARAM_CHAR || element == TEMPLATE_PARAM_PACK_CHAR)
				return true;
		}
		return false;
	}

	/**
	 * Tests whether the value depends on a template parameter.
	 */
	public static boolean isDependentValue(IValue nonTypeValue) {
		final char[] rep= nonTypeValue.getInternalExpression();
		for (final char c : rep) {
			if (c == REFERENCE_CHAR || c == TEMPLATE_PARAM_CHAR || c == TEMPLATE_PARAM_PACK_CHAR)
				return true;
		}
		return false;
	}

	/**
	 * Collects all references to parameter packs.
	 */
	public static int[] getParameterPackReferences(IValue value) {
		final char[] rep= value.getInternalExpression();
		int result= -1;
		List<Integer> array= null;
		for (int i=0; i<rep.length-1; i++) {
			if (rep[i] == TEMPLATE_PARAM_PACK_CHAR) {
				int ref;
				try {
					ref = parseHex(rep, i + 1);
					if (result < 0) {
						result = ref;
					} else {
						if (array == null) {
							array = new ArrayList<Integer>(2);
							array.add(result);
						}
						array.add(ref);
					}
				} catch (UnknownValueException e) {
				}
			}
		}
		if (array != null) {
			int[] ra= new int[array.size()];
			for (int i = 0; i < ra.length; i++) {
				ra[i]= array.get(i);
			}
			return ra;
		}
		if (result != -1)
			return new int[] {result};
		
		return NO_INT;
	}

	/**
	 * Creates the value for an expression.
	 */
	public static IValue create(IASTExpression expr, int maxRecursionDepth) {
		try {
			Map<String, Integer> unknownSigs= new HashMap<String, Integer>();
			List<ICPPUnknownBinding> unknown= new ArrayList<ICPPUnknownBinding>();
			Object obj= evaluate(expr, unknownSigs, unknown, maxRecursionDepth);
			if (obj instanceof Number)
				return create(((Number) obj).longValue());
			
			ICPPUnknownBinding[] ua;
			if (unknown.isEmpty()) {
				ua= ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY;
			} else {
				ua= unknown.toArray(new ICPPUnknownBinding[unknown.size()]);
			}
			return new Value(((String)obj).toCharArray(), ua);
		} catch (UnknownValueException e) {
		}
		return UNKNOWN;
	}
	
	/**
	 * Creates a value off its canonical representation.
	 */
	public static IValue fromInternalRepresentation(char[] rep, ICPPUnknownBinding[] unknown) {
		if (CharArrayUtils.equals(rep, UNKNOWN.getInternalExpression()))
			return UNKNOWN;
		
		Long l= parseLong(rep);
		if (l != null) 
			return create(l.longValue());
	
		return new Value(rep, unknown);
	}

	/**
	 * Creates a unique value needed during template instantiation.
	 */
	public static IValue unique() {
		StringBuilder buf= new StringBuilder(10);
		buf.append(UNIQUE_CHAR);
		buf.append(++sUnique);
		return new Value(extractChars(buf), ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY); 
	}

	/**
	 * Computes the canonical representation of the value of the expression. 
	 * Returns a {@code Number} for numerical values or a {@code String}, otherwise.
	 * @throws UnknownValueException
	 */
	private static Object evaluate(IASTExpression e, Map<String, Integer> unknownSigs, List<ICPPUnknownBinding> unknowns, int maxdepth) throws UnknownValueException {
		if (maxdepth < 0 || e == null)
			throw UNKNOWN_EX;
		
		if (e instanceof IASTArraySubscriptExpression) {
			throw UNKNOWN_EX;
		}
		if (e instanceof IASTBinaryExpression) {
			return evaluateBinaryExpression((IASTBinaryExpression) e, unknownSigs, unknowns, maxdepth);
		}
		if (e instanceof IASTCastExpression) { // must be ahead of unary
			return evaluate(((IASTCastExpression) e).getOperand(), unknownSigs, unknowns, maxdepth);
		}
		if (e instanceof IASTUnaryExpression) {
			return evaluateUnaryExpression((IASTUnaryExpression) e, unknownSigs, unknowns, maxdepth);
		}
		if (e instanceof IASTConditionalExpression) {
			IASTConditionalExpression cexpr= (IASTConditionalExpression) e;
			Object o= evaluate(cexpr.getLogicalConditionExpression(), unknownSigs, unknowns, maxdepth);
			if (o instanceof Number) {
				Number v= (Number) o;
				if (v.longValue() == 0) {
					return evaluate(cexpr.getNegativeResultExpression(), unknownSigs, unknowns, maxdepth);
				}
				final IASTExpression pe = cexpr.getPositiveResultExpression();
				if (pe == null) // gnu-extension allows to omit the positive expression.
					return o;
				return evaluate(pe, unknownSigs, unknowns, maxdepth);
			}

			final IASTExpression pe = cexpr.getPositiveResultExpression();
			Object po= pe == null ? o : evaluate(pe, unknownSigs, unknowns, maxdepth);
			Object neg= evaluate(cexpr.getNegativeResultExpression(), unknownSigs, unknowns, maxdepth);
			return "" + CONDITIONAL_CHAR + SEPARATOR + o.toString() + SEPARATOR + po.toString() + SEPARATOR + neg.toString(); //$NON-NLS-1$
		}
		if (e instanceof IASTIdExpression) {
			IBinding b= ((IASTIdExpression) e).getName().resolvePreBinding();
			return evaluateBinding(b, unknownSigs, unknowns, maxdepth);
		}
		if (e instanceof IASTLiteralExpression) {
			IASTLiteralExpression litEx= (IASTLiteralExpression) e;
			switch (litEx.getKind()) {
			case IASTLiteralExpression.lk_false:
				return 0;
			case IASTLiteralExpression.lk_true:
				return 1;
			case IASTLiteralExpression.lk_integer_constant:
				try {
					return ExpressionEvaluator.getNumber(litEx.getValue());
				} catch (EvalException e1) {
					throw UNKNOWN_EX;
				}
			case IASTLiteralExpression.lk_char_constant:
				try {
					final char[] image= litEx.getValue();
					if (image.length > 1 && image[0] == 'L') 
						return ExpressionEvaluator.getChar(image, 2);
					return ExpressionEvaluator.getChar(image, 1);
				} catch (EvalException e1) {
					throw UNKNOWN_EX;
				}
			}
		}
		if (e instanceof IASTTypeIdExpression) {
			IASTTypeIdExpression typeIdEx = (IASTTypeIdExpression) e;
			switch (typeIdEx.getOperator()) {
			case IASTTypeIdExpression.op_sizeof:
				final IType type;
				ASTTranslationUnit ast = (ASTTranslationUnit) typeIdEx.getTranslationUnit();
				type = ast.createType(typeIdEx.getTypeId());
				SizeofCalculator calculator = ast.getSizeofCalculator();
				SizeAndAlignment info = calculator.sizeAndAlignment(type);
				if (info == null)
					throw UNKNOWN_EX;
				return info.size;
			}
		}
		throw UNKNOWN_EX;
	}
	
	/**
	 * Extract a value off a binding.
	 */
	private static Object evaluateBinding(IBinding b, Map<String, Integer> unknownSigs, List<ICPPUnknownBinding> unknowns, int maxdepth) throws UnknownValueException {
		if (b instanceof IType) {
			throw UNKNOWN_EX;
		}
		if (b instanceof ICPPTemplateNonTypeParameter) {
			final ICPPTemplateNonTypeParameter tp = (ICPPTemplateNonTypeParameter) b;
			return createTemplateParamExpression(tp.getParameterID(), tp.isParameterPack());
		}
		
		if (b instanceof ICPPUnknownBinding) {
			return createReference((ICPPUnknownBinding) b, unknownSigs, unknowns);
		}
			
		IValue value= null;
		if (b instanceof IInternalVariable) {
			value= ((IInternalVariable) b).getInitialValue(maxdepth - 1);
		} else if (b instanceof IVariable) {
			value= ((IVariable) b).getInitialValue();
		} else if (b instanceof IEnumerator) {
			value= ((IEnumerator) b).getValue();
		} 
		if (value != null)
			return evaluateValue(value, unknownSigs, unknowns);
		
		throw UNKNOWN_EX;
	}

	private static Object createReference(ICPPUnknownBinding unknown, Map<String, Integer> unknownSigs, List<ICPPUnknownBinding> unknowns) {
		String sig= getSignatureForUnknown(unknown);
		Integer idx= unknownSigs.get(sig);
		if (idx == null) {
			idx= unknownSigs.size();
			unknownSigs.put(sig, idx);
			unknowns.add(unknown);
		}
		return "" + REFERENCE_CHAR + idx.toString();  //$NON-NLS-1$
	}
	
	private static Object evaluateValue(IValue cv, Map<String, Integer> unknownSigs, List<ICPPUnknownBinding> unknowns) throws UnknownValueException {
		if (cv == Value.UNKNOWN) 
			throw UNKNOWN_EX;
		
		Long lv= cv.numericalValue();
		if (lv != null)
			return lv;
		
		final IBinding[] oldUnknowns = cv.getUnknownBindings();
		final char[] expr= cv.getInternalExpression();
		if (oldUnknowns.length == 0)
			return new String(expr);
		
		StringBuilder buf= new StringBuilder(expr.length);
		boolean skipToSeparator= false;
		for (int i = 0; i < expr.length; i++) {
			final char c= expr[i];
			switch (c) {
			case REFERENCE_CHAR: {
				int idx= parseNonNegative(expr, i + 1);
				if (idx >= oldUnknowns.length)
					throw UNKNOWN_EX;
				final IBinding old = oldUnknowns[idx];
				if (!(old instanceof ICPPUnknownBinding)) 
					throw UNKNOWN_EX;

				buf.append(createReference((ICPPUnknownBinding) old, unknownSigs, unknowns));
				skipToSeparator= true;
				break;
			}
			case SEPARATOR:
				skipToSeparator= false;
				buf.append(c);
				break;
			default:
				if (!skipToSeparator)
					buf.append(c);
				break;
			}
		}
		return buf.toString();
	}
	
	private static Object evaluateUnaryExpression(IASTUnaryExpression ue, Map<String, Integer> unknownSigs, List<ICPPUnknownBinding> unknowns, int maxdepth) throws UnknownValueException {
		final int unaryOp= ue.getOperator();

		if (unaryOp == IASTUnaryExpression.op_sizeof) {
			IType type = ue.getExpressionType();
			ASTTranslationUnit ast = (ASTTranslationUnit) ue.getTranslationUnit();
			SizeofCalculator calculator = ast.getSizeofCalculator();
			SizeAndAlignment info = calculator.sizeAndAlignment(type);
			if (info == null)
				throw UNKNOWN_EX;
			return info.size;
		}

		if (unaryOp == IASTUnaryExpression.op_amper || unaryOp == IASTUnaryExpression.op_star ||
				unaryOp == IASTUnaryExpression.op_sizeofParameterPack) {
			throw UNKNOWN_EX;
		}
			
		final Object value= evaluate(ue.getOperand(), unknownSigs, unknowns, maxdepth);
		return combineUnary(unaryOp, value); 
	}
	
	private static Object combineUnary(final int unaryOp, final Object value) throws UnknownValueException {
		switch (unaryOp) {
		case IASTUnaryExpression.op_bracketedPrimary:
		case IASTUnaryExpression.op_plus:
			return value;
		}

		if (value instanceof Number) {
			long v= ((Number) value).longValue();
			switch (unaryOp) {
			case IASTUnaryExpression.op_prefixIncr:
			case IASTUnaryExpression.op_postFixIncr:
				return ++v;
			case IASTUnaryExpression.op_prefixDecr:
			case IASTUnaryExpression.op_postFixDecr:
				return --v;
			case IASTUnaryExpression.op_minus:
				return -v;
			case IASTUnaryExpression.op_tilde:
				return ~v;
			case IASTUnaryExpression.op_not:
				return v == 0 ? 1 : 0;
			}
			throw UNKNOWN_EX;
		} 
		
		switch (unaryOp) {
		case IASTUnaryExpression.op_prefixIncr:
		case IASTUnaryExpression.op_postFixIncr:
		case IASTUnaryExpression.op_prefixDecr:
		case IASTUnaryExpression.op_postFixDecr:
		case IASTUnaryExpression.op_minus:
		case IASTUnaryExpression.op_tilde:
		case IASTUnaryExpression.op_not:
			return "" + UNARY_OP_CHAR + unaryOp + SEPARATOR + value.toString();  //$NON-NLS-1$
		}
		throw UNKNOWN_EX;
	}

	private static Object evaluateBinaryExpression(IASTBinaryExpression be, 
			Map<String, Integer> unknownSigs, List<ICPPUnknownBinding> unknowns, int maxdepth) throws UnknownValueException {
		final Object o1= evaluate(be.getOperand1(), unknownSigs, unknowns, maxdepth);
		final Object o2= evaluate(be.getOperand2(), unknownSigs, unknowns, maxdepth);

		final int op= be.getOperator();
		return combineBinary(op, o1, o2);
	}
	
	private static Object combineBinary(final int op, final Object o1, final Object o2) throws UnknownValueException {
		if (o1 instanceof Number && o2 instanceof Number) {
			long v1= ((Number) o1).longValue();
			long v2= ((Number) o2).longValue();
			switch (op) {
			case IASTBinaryExpression.op_multiply:
				return v1 * v2;
			case IASTBinaryExpression.op_divide:
				if (v2 == 0)
					throw UNKNOWN_EX;
				return v1 / v2;
			case IASTBinaryExpression.op_modulo:
				if (v2 == 0)
					throw UNKNOWN_EX;
				return v1 % v2;
			case IASTBinaryExpression.op_plus:
				return v1 + v2;
			case IASTBinaryExpression.op_minus:
				return v1 - v2;
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
				return v1 & v2;
			case IASTBinaryExpression.op_binaryXor:
				return v1 ^ v2;
			case IASTBinaryExpression.op_binaryOr:
				return v1 | v2;
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
			throw UNKNOWN_EX;
		}
		switch (op) {
		case IASTBinaryExpression.op_multiply:
		case IASTBinaryExpression.op_divide:
		case IASTBinaryExpression.op_modulo:
		case IASTBinaryExpression.op_plus:
		case IASTBinaryExpression.op_minus:
		case IASTBinaryExpression.op_shiftLeft:
		case IASTBinaryExpression.op_shiftRight:
		case IASTBinaryExpression.op_lessThan:
		case IASTBinaryExpression.op_greaterThan:
		case IASTBinaryExpression.op_lessEqual:
		case IASTBinaryExpression.op_greaterEqual:
		case IASTBinaryExpression.op_binaryAnd:
		case IASTBinaryExpression.op_binaryXor:
		case IASTBinaryExpression.op_binaryOr:
		case IASTBinaryExpression.op_logicalAnd:
		case IASTBinaryExpression.op_logicalOr:
        case IASTBinaryExpression.op_max:
        case IASTBinaryExpression.op_min:
			break;
		case IASTBinaryExpression.op_equals:
			if (o1.equals(o2))
				return 1;
			break;
		case IASTBinaryExpression.op_notequals:
			if (o1.equals(o2))
				return 0;
			break;
		default:
			throw UNKNOWN_EX;
		}
		
		return "" + BINARY_OP_CHAR + op + SEPARATOR + o1.toString() + SEPARATOR + o2.toString(); //$NON-NLS-1$
	}
	
	public static IValue reevaluate(IValue val, int packOffset, IBinding[] resolvedUnknowns, ICPPTemplateParameterMap map, int maxdepth) {
		try {
			Map<String, Integer> unknownSigs= new HashMap<String, Integer>();
			List<ICPPUnknownBinding> unknown= new ArrayList<ICPPUnknownBinding>();
			Reevaluation reeval= new Reevaluation(val.getInternalExpression(), packOffset,
					unknownSigs, unknown,
					resolvedUnknowns, map);
			Object obj= reevaluate(reeval, maxdepth);
			if (reeval.pos != reeval.fExpression.length)
				return UNKNOWN;
			
			if (obj instanceof Number)
				return create(((Number) obj).longValue());
			
			ICPPUnknownBinding[] ua;
			if (unknown.isEmpty()) {
				ua= ICPPUnknownBinding.EMPTY_UNKNOWN_BINDING_ARRAY;
			} else {
				ua= unknown.toArray(new ICPPUnknownBinding[unknown.size()]);
			}
			return new Value(((String)obj).toCharArray(), ua);
		} catch (UnknownValueException e) {
		}
		return UNKNOWN;
	}

	private static Object reevaluate(Reevaluation reeval, int maxdepth) 
			throws UnknownValueException {
		if (maxdepth < 0)
			throw UNKNOWN_EX;

		final int idx= reeval.pos;
		final char[] buf= reeval.fExpression;
		final int length = buf.length;
		if (idx >= length)
			throw UNKNOWN_EX;
		
		final char c= buf[idx];
		switch (c) {
		case BINARY_OP_CHAR: 
			int op= parseNonNegative(buf, idx + 1);
			reeval.nextSeperator();
			Object o1= reevaluate(reeval, maxdepth);
			Object o2= reevaluate(reeval, maxdepth);
			return combineBinary(op, o1, o2);
		case UNARY_OP_CHAR: 
			op= parseNonNegative(buf, idx + 1);
			reeval.nextSeperator();
			o1= reevaluate(reeval, maxdepth);
			return combineUnary(op, o1);
		case CONDITIONAL_CHAR:
			reeval.nextSeperator();
			Object cond= reevaluate(reeval, maxdepth);
			Object po= reevaluate(reeval, maxdepth);
			Object neg= reevaluate(reeval, maxdepth);
			if (cond instanceof Number) {
				Number v= (Number) cond;
				if (v.longValue() == 0) {
					return neg;
				}
				return po;
			}
			return "" + CONDITIONAL_CHAR + SEPARATOR + cond.toString() + SEPARATOR + po.toString() + SEPARATOR + neg.toString(); //$NON-NLS-1$
		case REFERENCE_CHAR: 
			int num= parseNonNegative(buf, idx + 1);
			final IBinding[] resolvedUnknowns= reeval.fResolvedUnknown;
			if (num >= resolvedUnknowns.length)
				throw UNKNOWN_EX;
			reeval.nextSeperator();
			return evaluateBinding(resolvedUnknowns[num], reeval.fUnknownSigs, reeval.fUnknowns, maxdepth);

		case TEMPLATE_PARAM_CHAR:
			num= parseHex(buf, idx + 1);
			reeval.nextSeperator();
			ICPPTemplateArgument arg = reeval.fMap.getArgument(num);
			if (arg != null) {
				IValue val= arg.getNonTypeValue();
				if (val == null)
					throw UNKNOWN_EX;
				return evaluateValue(val, reeval.fUnknownSigs, reeval.fUnknowns);
			}
			return createTemplateParamExpression(num, false);
			
		case TEMPLATE_PARAM_PACK_CHAR:
			num= parseHex(buf, idx + 1);
			reeval.nextSeperator();
			arg= null;
			if (reeval.fPackOffset >= 0) {
				ICPPTemplateArgument[] args= reeval.fMap.getPackExpansion(num);
				if (args != null && reeval.fPackOffset < args.length) {
					arg= args[reeval.fPackOffset];
				}
			}
			if (arg != null) {
				IValue val= arg.getNonTypeValue();
				if (val == null)
					throw UNKNOWN_EX;
				return evaluateValue(val, reeval.fUnknownSigs, reeval.fUnknowns);
			}
			return createTemplateParamExpression(num, true);
			
		default:
			reeval.nextSeperator();
			return parseLong(buf, idx);
		}
	}

	/**
	 * Parses a non negative int.
	 */
	private static int parseNonNegative(char[] value, int offset) throws UnknownValueException {
		final long maxvalue= Integer.MAX_VALUE/10;
		final int len= value.length;
		int result = 0;
		boolean ok= false;
		for(; offset < len; offset++) {
			final int digit= (value[offset] - '0');
			if (digit < 0 || digit > 9)
				break;
			if (result > maxvalue)
				return -1;
			
			result= result * 10 + digit;
			ok= true;
		}
		if (!ok)
			throw UNKNOWN_EX;
		return result;
	}

	/**
	 * Parses a a hex value.
	 */
	private static int parseHex(char[] value, int offset) throws UnknownValueException {
		int result = 0;
		boolean ok= false;
		final int len= value.length;
		for(; offset < len; offset++) {
			int digit= (value[offset] - '0');
			if (digit < 0 || digit > 9) {
				digit += '0' - 'a' + 10;
				if (digit < 10 || digit > 15) {
					digit += 'a' - 'A';
					if (digit < 10 || digit > 15) {
						break;
					}
				}
			}
			if ((result & 0xf0000000) != 0)
				throw UNKNOWN_EX;
			
			result= (result << 4) + digit;
			ok= true;
		}
		if (!ok)
			throw UNKNOWN_EX;
		
		return result;
	}

	/**
	 * Parses a long.
	 */
	private static long parseLong(char[] value, int offset) throws UnknownValueException {
		final long maxvalue= Long.MAX_VALUE/10;
		final int len= value.length;
		boolean negative= false;
		long result = 0;
		
		boolean ok= false;
		if (offset < len && value[offset] == '-') {
			negative = true;
			offset++;
		}
		for(; offset < len; offset++) {
			final int digit= (value[offset] - '0');
			if (digit < 0 || digit > 9)
				break;
			
			if (result > maxvalue)
				throw UNKNOWN_EX;
			
			result= result * 10 + digit;
			ok= true;
		}
		if (!ok)
			throw UNKNOWN_EX;
		
		return negative ? -result : result;
	}

	/**
	 * Parses a long, returns <code>null</code> if not possible
	 */
	private static Long parseLong(char[] value) {
		final long maxvalue= Long.MAX_VALUE/10;
		final int len= value.length;
		boolean negative= false;
		long result = 0;
		int i= 0;
		
		if (len > 0 && value[0] == '-') {
			negative = true;
			i++;
		}
		if (i == len)
			return null;
		
		for(; i < len; i++) {
			if (result > maxvalue)
				return null;
			
			final int digit= (value[i] - '0');
			if (digit < 0 || digit > 9)
				return null;
			result= result * 10 + digit;
		}
		return negative ? -result : result;
	}

	/**
	 * Computes a signature for an unknown binding.
	 */
	private static String getSignatureForUnknown(ICPPUnknownBinding binding) {
		IBinding owner= binding.getOwner();
		if (owner instanceof IType) {
			StringBuilder buf= new StringBuilder();
			ASTTypeUtil.appendType((IType) owner, true, buf);
			return buf.append(SCOPE_OP).append(binding.getName()).toString();
		}
		return binding.getName();
	}

	/**
	 * Converts long to a char array
	 */
	private static char[] toCharArray(long value) {
		StringBuilder buf= new StringBuilder();
		buf.append(value);
		return extractChars(buf);
	}

	private static char[] extractChars(StringBuilder buf) {
		final int len = buf.length();
		char[] result= new char[len];
		buf.getChars(0, len, result, 0);
		return result;
	}
}
