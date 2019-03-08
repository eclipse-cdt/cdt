/*******************************************************************************
 * Copyright (c) 2012, 2017 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_assign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryAnd;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryAndAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryOr;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryOrAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryXor;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryXorAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_divide;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_divideAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_equals;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_greaterEqual;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_greaterThan;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_lessEqual;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_lessThan;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_logicalAnd;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_logicalOr;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_minus;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_minusAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_modulo;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_moduloAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_multiply;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_multiplyAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_notequals;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_plus;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_plusAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_pmarrow;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_pmdot;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_shiftLeft;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_shiftLeftAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_shiftRight;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_shiftRightAssign;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.glvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueTypeWithResolvedTypedefs;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArithmeticConversion;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil.Pair;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * Performs evaluation of an expression.
 */
public class EvalBinary extends CPPDependentEvaluation {
	public final static int op_arrayAccess = Byte.MAX_VALUE;
	private final int fOperator;

	private final ICPPEvaluation fArg1;
	private final ICPPEvaluation fArg2;

	private ICPPFunction fOverload = CPPFunction.UNINITIALIZED_FUNCTION;
	private ICPPEvaluation fOverloadCall;
	private IType fType;
	private boolean fCheckedIsConstantExpression;
	private boolean fIsConstantExpression;

	public EvalBinary(int operator, ICPPEvaluation arg1, ICPPEvaluation arg2, IASTNode pointOfDefinition) {
		this(operator, arg1, arg2, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalBinary(int operator, ICPPEvaluation arg1, ICPPEvaluation arg2, IBinding templateDefinition) {
		super(templateDefinition);
		fOperator = operator;
		fArg1 = arg1;
		fArg2 = arg2;
	}

	public int getOperator() {
		return fOperator;
	}

	public ICPPEvaluation getArg1() {
		return fArg1;
	}

	public ICPPEvaluation getArg2() {
		return fArg2;
	}

	@Override
	public boolean isInitializerList() {
		return false;
	}

	@Override
	public boolean isFunctionSet() {
		return false;
	}

	@Override
	public IType getType() {
		if (fType == null) {
			if (isTypeDependent()) {
				fType = new TypeOfDependentExpression(this);
			} else {
				ICPPFunction overload = getOverload();
				if (overload != null) {
					fType = ExpressionTypes.restoreTypedefs(ExpressionTypes.typeFromFunctionCall(overload),
							fArg1.getType(), fArg2.getType());
				} else {
					fType = computeType();
				}
			}
		}
		return fType;
	}

	private ICPPEvaluation createOperatorOverloadEvaluation(ICPPFunction overload, ICPPEvaluation arg1,
			ICPPEvaluation arg2) {
		EvalFunctionCall operatorCall;
		IASTNode point = CPPSemantics.getCurrentLookupPoint();
		if (overload instanceof ICPPMethod) {
			EvalMemberAccess opAccess = new EvalMemberAccess(arg1.getType(), ValueCategory.LVALUE, overload, arg1,
					false, point);
			ICPPEvaluation[] args = new ICPPEvaluation[] { opAccess, arg2 };
			operatorCall = new EvalFunctionCall(args, arg1, point);
		} else {
			EvalBinding op = new EvalBinding(overload, overload.getType(), point);
			ICPPEvaluation[] args = new ICPPEvaluation[] { op, arg1, arg2 };
			operatorCall = new EvalFunctionCall(args, null, point);
		}
		return operatorCall;
	}

	private boolean operatorAllowsContextualConversion() {
		return fOperator == op_logicalAnd || fOperator == op_logicalOr;
	}

	@Override
	public IValue getValue() {
		ICPPEvaluation arg1 = fArg1;
		ICPPEvaluation arg2 = fArg2;
		ICPPFunction overload = getOverload();
		if (overload != null) {
			IType[] parameterTypes = SemanticUtil.getParameterTypesIncludingImplicitThis(overload);
			if (parameterTypes.length >= 2) {
				boolean allowContextualConversion = operatorAllowsContextualConversion();
				arg1 = maybeApplyConversion(fArg1, parameterTypes[0], allowContextualConversion, true);
				arg2 = maybeApplyConversion(fArg2, parameterTypes[1], allowContextualConversion, true);
			} else {
				CCorePlugin.log(IStatus.ERROR, "Unexpected overload for binary operator " + fOperator //$NON-NLS-1$
						+ ": '" + overload.getName() + "'"); //$NON-NLS-1$//$NON-NLS-2$
			}

			if (!(overload instanceof CPPImplicitFunction)) {
				if (!overload.isConstexpr()) {
					return IntegralValue.ERROR;
				}
				if (fOverloadCall == null) {
					fOverloadCall = createOperatorOverloadEvaluation(overload, arg1, arg2);
				}
				return fOverloadCall.getValue();
			}
		}

		IValue v1 = arg1.getValue();
		if (v1 == null || v1 == IntegralValue.UNKNOWN)
			return IntegralValue.UNKNOWN;
		IValue v2 = arg2.getValue();
		if (v2 == null || v2 == IntegralValue.UNKNOWN)
			return IntegralValue.UNKNOWN;

		switch (fOperator) {
		case op_equals:
			if (v1.equals(v2))
				return IntegralValue.create(true);
			break;
		case op_notequals:
			if (v1.equals(v2))
				return IntegralValue.create(false);
			break;
		case op_assign:
			return v2;
		}

		Number num1 = v1.numberValue();
		if (num1 != null) {
			if (num1 instanceof Long && num1.longValue() == 0) {
				if (fOperator == op_logicalAnd) {
					return v1;
				}
			} else if (fOperator == op_logicalOr) {
				return v1;
			}

			Number num2 = v2.numberValue();
			if (num2 != null) {
				return ValueFactory.evaluateBinaryExpression(fOperator, v1, v2);
			}
		}
		return DependentValue.create(this);
	}

	@Override
	public boolean isTypeDependent() {
		if (fType != null) {
			return fType instanceof TypeOfDependentExpression;
		}
		return fArg1.isTypeDependent() || fArg2.isTypeDependent();
	}

	@Override
	public boolean isValueDependent() {
		return fArg1.isValueDependent() || fArg2.isValueDependent();
	}

	@Override
	public boolean isConstantExpression() {
		if (!fCheckedIsConstantExpression) {
			fCheckedIsConstantExpression = true;
			fIsConstantExpression = computeIsConstantExpression();
		}
		return fIsConstantExpression;
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalBinary)) {
			return false;
		}
		EvalBinary o = (EvalBinary) other;
		return fOperator == o.fOperator && fArg1.isEquivalentTo(o.fArg1) && fArg2.isEquivalentTo(o.fArg2);
	}

	private boolean computeIsConstantExpression() {
		return fArg1.isConstantExpression() && fArg2.isConstantExpression() && isNullOrConstexprFunc(getOverload());
	}

	@Override
	public ValueCategory getValueCategory() {
		if (isTypeDependent())
			return ValueCategory.PRVALUE;

		ICPPFunction overload = getOverload();
		if (overload != null)
			return ExpressionTypes.valueCategoryFromFunctionCall(overload);

		switch (fOperator) {
		case op_arrayAccess:
		case op_assign:
		case op_binaryAndAssign:
		case op_binaryOrAssign:
		case op_binaryXorAssign:
		case op_divideAssign:
		case op_minusAssign:
		case op_moduloAssign:
		case op_multiplyAssign:
		case op_plusAssign:
		case op_shiftLeftAssign:
		case op_shiftRightAssign:
			return LVALUE;

		case op_pmdot:
			if (!(getType() instanceof ICPPFunctionType))
				return fArg1.getValueCategory();
			break;

		case op_pmarrow:
			if (!(getType() instanceof ICPPFunctionType))
				return LVALUE;
			break;
		}

		return ValueCategory.PRVALUE;
	}

	public ICPPFunction getOverload() {
		if (fOverload == CPPFunction.UNINITIALIZED_FUNCTION) {
			fOverload = computeOverload();
		}
		return fOverload;
	}

	private ICPPFunction computeOverload() {
		if (isTypeDependent())
			return null;

		if (fOperator == op_arrayAccess) {
			IType type = fArg1.getType();
			type = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
			if (type instanceof ICPPClassType) {
				return CPPSemantics.findOverloadedBinaryOperator(getTemplateDefinitionScope(),
						OverloadableOperator.BRACKET, fArg1, fArg2);
			}
		} else {
			final OverloadableOperator op = OverloadableOperator.fromBinaryExpression(fOperator);
			if (op != null) {
				return CPPSemantics.findOverloadedBinaryOperator(getTemplateDefinitionScope(), op, fArg1, fArg2);
			}
		}
		return null;
	}

	public IType computeType() {
		// Check for overloaded operator.
		ICPPFunction o = getOverload();
		if (o != null)
			return typeFromFunctionCall(o);

		final IType originalType1 = fArg1.getType();
		final IType type1 = prvalueTypeWithResolvedTypedefs(originalType1);
		if (type1 instanceof ISemanticProblem) {
			return type1;
		}

		final IType originalType2 = fArg2.getType();
		final IType type2 = prvalueTypeWithResolvedTypedefs(originalType2);
		if (type2 instanceof ISemanticProblem) {
			return type2;
		}

		IType type = CPPArithmeticConversion.convertCppOperandTypes(fOperator, type1, type2);
		if (type != null) {
			return ExpressionTypes.restoreTypedefs(type, originalType1, originalType2);
		}

		switch (fOperator) {
		case op_arrayAccess:
			if (type1 instanceof IPointerType) {
				return glvalueType(((IPointerType) type1).getType());
			}
			if (type2 instanceof IPointerType) {
				return glvalueType(((IPointerType) type2).getType());
			}
			return ProblemType.UNKNOWN_FOR_EXPRESSION;

		case op_lessEqual:
		case op_lessThan:
		case op_greaterEqual:
		case op_greaterThan:
		case op_logicalAnd:
		case op_logicalOr:
		case op_equals:
		case op_notequals:
			return CPPBasicType.BOOLEAN;

		case op_plus:
			if (type1 instanceof IPointerType) {
				return ExpressionTypes.restoreTypedefs(type1, originalType1);
			}
			if (type2 instanceof IPointerType) {
				return ExpressionTypes.restoreTypedefs(type2, originalType2);
			}
			break;

		case op_minus:
			if (type1 instanceof IPointerType) {
				if (type2 instanceof IPointerType) {
					return CPPVisitor.getPointerDiffType();
				}
				return originalType1;
			}
			break;

		case op_pmarrow:
		case op_pmdot:
			if (type2 instanceof ICPPPointerToMemberType) {
				IType t = ((ICPPPointerToMemberType) type2).getType();
				if (t instanceof ICPPFunctionType)
					return t;
				if (fOperator == op_pmdot && fArg1.getValueCategory() == PRVALUE) {
					return prvalueType(t);
				}
				return glvalueType(t);
			}
			return ProblemType.UNKNOWN_FOR_EXPRESSION;
		}
		return type1;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_BINARY);
		buffer.putByte((byte) fOperator);
		buffer.marshalEvaluation(fArg1, includeValue);
		buffer.marshalEvaluation(fArg2, includeValue);
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int op = buffer.getByte();
		ICPPEvaluation arg1 = buffer.unmarshalEvaluation();
		ICPPEvaluation arg2 = buffer.unmarshalEvaluation();
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalBinary(op, arg1, arg2, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation arg1 = fArg1.instantiate(context, maxDepth);
		ICPPEvaluation arg2 = fArg2.instantiate(context, maxDepth);
		if (arg1 == fArg1 && arg2 == fArg2)
			return this;
		return new EvalBinary(fOperator, arg1, arg2, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPFunction overload = getOverload();
		if (overload != null) {
			ICPPEvaluation operatorCall = createOperatorOverloadEvaluation(overload, fArg1, fArg2);
			return operatorCall.computeForFunctionCall(record, context);
		}

		Pair<ICPPEvaluation, ICPPEvaluation> vp1 = EvalUtil.getValuePair(fArg1, record, context);
		final ICPPEvaluation updateable1 = vp1.getFirst();
		final ICPPEvaluation fixed1 = vp1.getSecond();
		Pair<ICPPEvaluation, ICPPEvaluation> vp2 = EvalUtil.getValuePair(fArg2, record, context);
		final ICPPEvaluation fixed2 = vp2.getSecond();
		ICPPEvaluation eval = fixed1 == fArg1 && fixed2 == fArg2 ? this
				: new EvalBinary(fOperator, fixed1, fixed2, getTemplateDefinition());

		if (isBinaryOperationWithAssignment(fOperator)) {
			if (isPointerToArray(fixed1) && hasIntType(fixed2)) {
				EvalPointer evalPointer = (EvalPointer) fixed1;
				int currentPos = evalPointer.getPosition();
				int rhs = fixed2.getValue().numberValue().intValue();

				if (fOperator == op_plusAssign) {
					evalPointer.setPosition(currentPos + rhs);
				} else if (fOperator == op_minusAssign) {
					evalPointer.setPosition(currentPos - rhs);
				} else {
					return EvalFixed.INCOMPLETE;
				}
			} else {
				if (updateable1 == EvalFixed.INCOMPLETE)
					return EvalFixed.INCOMPLETE;
				int binaryOperator = getBinaryOperatorWithoutAssignment(fOperator);
				EvalBinary binaryOpEval = new EvalBinary(binaryOperator, fixed1, fixed2, getTemplateDefinition());
				EvalBinary assignmentEval = new EvalBinary(op_assign, updateable1, binaryOpEval,
						getTemplateDefinition());
				return assignmentEval.computeForFunctionCall(record, context);
			}
		} else if (fOperator == op_assign) {
			ICPPEvaluation newValue = null;
			if (fixed2 instanceof EvalPointer) {
				newValue = fixed2;
			} else {
				newValue = new EvalFixed(fixed2.getType(), fixed2.getValueCategory(), fixed2.getValue());
			}

			if (updateable1 instanceof EvalReference && !(updateable1 instanceof EvalPointer)) {
				EvalReference evalRef = (EvalReference) updateable1;
				evalRef.update(newValue);
			} else if (updateable1 instanceof EvalCompositeAccess) {
				EvalCompositeAccess evalCompAccess = (EvalCompositeAccess) updateable1;
				evalCompAccess.update(newValue);
			} else if (updateable1 instanceof EvalBinding) {
				EvalBinding evalBinding = (EvalBinding) updateable1;
				record.update(evalBinding.getBinding(), newValue);
			}
			return updateable1;
		} else if (fOperator == op_arrayAccess) {
			Number numericValue = fixed2.getValue().numberValue();
			if (numericValue == null)
				return EvalFixed.INCOMPLETE;
			ICPPEvaluation composite = fixed1;
			int arrayIndex = numericValue.intValue();
			if (fixed1 instanceof EvalPointer) {
				ICPPEvaluation elementEval = ((EvalPointer) fixed1).getTargetEvaluation();
				if (elementEval instanceof EvalCompositeAccess) {
					// 'composite' will now be the underlying array that the pointer points into.
					// Since the pointer may not point at the beginning of the array, the array
					// index needs to be shifted by the pointer's position.
					composite = ((EvalCompositeAccess) elementEval).getParent();
					arrayIndex += ((EvalPointer) fixed1).getPosition();
				}
			}
			return new EvalCompositeAccess(composite, arrayIndex);
		} else if ((isArray(fixed1) || isArray(fixed2)) && (hasIntType(fixed1) || hasIntType(fixed2))) {
			int offset = hasIntType(fixed1) ? fixed1.getValue().numberValue().intValue()
					: fixed2.getValue().numberValue().intValue();
			EvalCompositeAccess evalCompositeAccess = new EvalCompositeAccess(isArray(fixed1) ? fixed1 : fixed2,
					offset);
			return new EvalPointer(record, evalCompositeAccess, evalCompositeAccess.getTemplateDefinition());
		} else if ((isPointerToArray(fixed1) || isPointerToArray(fixed2))
				&& (hasIntType(fixed1) || hasIntType(fixed2))) {
			final EvalPointer pointer = isPointerToArray(fixed1) ? ((EvalPointer) fixed1).copy()
					: ((EvalPointer) fixed2).copy();
			pointer.setPosition(eval.getValue().numberValue().intValue());
			return pointer;
		}
		return eval;
	}

	private boolean hasIntType(ICPPEvaluation arg2) {
		IType type = arg2.getType();
		return (type instanceof IBasicType && ((IBasicType) type).getKind() == IBasicType.Kind.eInt);
	}

	private boolean isArray(ICPPEvaluation eval) {
		return eval.getType() instanceof IArrayType;
	}

	private boolean isPointerToArray(ICPPEvaluation argument) {
		if (argument instanceof EvalPointer) {
			EvalPointer evalPointer = (EvalPointer) argument;
			ICPPEvaluation pointerValue = evalPointer.dereference().getTargetEvaluation();
			// TODO(nathanridge): What if the composite being accessed is not an array but a structure?
			return pointerValue instanceof EvalCompositeAccess;
		}
		return false;
	}

	private static boolean isBinaryOperationWithAssignment(int operator) {
		switch (operator) {
		case op_binaryAndAssign:
		case op_binaryOrAssign:
		case op_binaryXorAssign:
		case op_divideAssign:
		case op_plusAssign:
		case op_minusAssign:
		case op_multiplyAssign:
		case op_moduloAssign:
		case op_shiftLeftAssign:
		case op_shiftRightAssign:
			return true;
		default:
			return false;
		}
	}

	private static int getBinaryOperatorWithoutAssignment(int operator) {
		switch (operator) {
		case op_binaryAndAssign:
			return op_binaryAnd;
		case op_binaryOrAssign:
			return op_binaryOr;
		case op_binaryXorAssign:
			return op_binaryXor;
		case op_divideAssign:
			return op_divide;
		case op_plusAssign:
			return op_plus;
		case op_minusAssign:
			return op_minus;
		case op_multiplyAssign:
			return op_multiply;
		case op_moduloAssign:
			return op_modulo;
		case op_shiftLeftAssign:
			return op_shiftLeft;
		case op_shiftRightAssign:
			return op_shiftRight;
		default:
			throw new IllegalArgumentException("Operator must be binary operation with assignment"); //$NON-NLS-1$
		}
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return CPPTemplates.combinePackSize(fArg1.determinePackSize(tpMap), fArg2.determinePackSize(tpMap));
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fArg1.referencesTemplateParameter() || fArg2.referencesTemplateParameter();
	}

	@Override
	public String toString() {
		return fArg1.toString() + " <op> " + fArg2.toString(); //$NON-NLS-1$
	}

	@Override
	public boolean isNoexcept() {
		ICPPFunction overload = getOverload();
		if (overload != null) {
			if (!EvalUtil.evaluateNoexceptSpecifier(overload.getType().getNoexceptSpecifier()))
				return false;
		}
		return fArg1.isNoexcept() && fArg2.isNoexcept();
	}
}
