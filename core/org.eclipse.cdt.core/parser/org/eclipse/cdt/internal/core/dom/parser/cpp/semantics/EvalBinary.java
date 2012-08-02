/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_assign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryAndAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryOrAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryXorAssign;
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
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_moduloAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_multiplyAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_notequals;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_plus;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_plusAssign;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_pmarrow;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_pmdot;
import static org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_shiftLeftAssign;
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

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArithmeticConversion;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of an expression.
 */
public class EvalBinary extends CPPEvaluation {
	public final static int op_arrayAccess= Byte.MAX_VALUE;
	private final int fOperator;

	private final ICPPEvaluation fArg1;
	private final ICPPEvaluation fArg2;

	private ICPPFunction fOverload= CPPFunction.UNINITIALIZED_FUNCTION;
	private IType fType;

	public EvalBinary(int operator, ICPPEvaluation arg1, ICPPEvaluation arg2) {
		fOperator= operator;
		fArg1= arg1;
		fArg2= arg2;
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
	public IType getTypeOrFunctionSet(IASTNode point) {
		if (fType == null) {
			if (isTypeDependent()) {
				fType= new TypeOfDependentExpression(this);
			} else {
				ICPPFunction overload = getOverload(point);
				if (overload != null) {
					fType= ExpressionTypes.restoreTypedefs(
							ExpressionTypes.typeFromFunctionCall(overload),
							fArg1.getTypeOrFunctionSet(point), fArg2.getTypeOrFunctionSet(point));
				} else {
					fType= computeType(point);
				}
			}
		}
		return fType;
	}

	@Override
	public IValue getValue(IASTNode point) {
		if (getOverload(point) != null) {
			// TODO(sprigogin): Simulate execution of a function call.
			return Value.create(this);
		}

		IValue v1 = fArg1.getValue(point);
		if (v1 == Value.UNKNOWN)
			return Value.UNKNOWN;
		IValue v2 = fArg2.getValue(point);
		if (v2 == Value.UNKNOWN)
			return Value.UNKNOWN;

		switch (fOperator) {
		case op_equals:
			if (v1.equals(v2))
				return Value.create(1);
			break;
		case op_notequals:
			if (v1.equals(v2))
				return Value.create(0);
			break;
		}

		Long num1 = v1.numericalValue();
		if (num1 != null) {
			if (num1 == 0) {
				if (fOperator == op_logicalAnd)
					return v1;
			} else if (fOperator == op_logicalOr) {
				return v1;
			}
			Long num2 = v2.numericalValue();
			if (num2 != null) {
				return Value.evaluateBinaryExpression(fOperator, num1, num2);
			}
		}
		return Value.create(this);
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
	public ValueCategory getValueCategory(IASTNode point) {
		if (isTypeDependent())
			return ValueCategory.PRVALUE;

		ICPPFunction overload = getOverload(point);
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
			if (!(getTypeOrFunctionSet(point) instanceof ICPPFunctionType))
				return fArg1.getValueCategory(point);
			break;

		case op_pmarrow:
			if (!(getTypeOrFunctionSet(point) instanceof ICPPFunctionType))
				return LVALUE;
			break;
		}

		return ValueCategory.PRVALUE;
	}

	public ICPPFunction getOverload(IASTNode point) {
		if (fOverload == CPPFunction.UNINITIALIZED_FUNCTION) {
			fOverload= computeOverload(point);
		}
		return fOverload;
	}

	private ICPPFunction computeOverload(IASTNode point) {
		if (isTypeDependent())
			return null;

		if (fOperator == op_arrayAccess) {
			IType type = fArg1.getTypeOrFunctionSet(point);
			type= SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
    		if (type instanceof ICPPClassType) {
    			return CPPSemantics.findOverloadedBinaryOperator(point, OverloadableOperator.BRACKET, fArg1, fArg2);
    		}
		} else {
			final OverloadableOperator op = OverloadableOperator.fromBinaryExpression(fOperator);
			if (op != null) {
				return CPPSemantics.findOverloadedBinaryOperator(point, op, fArg1, fArg2);
			}
		}
    	return null;
	}

	public IType computeType(IASTNode point) {
		// Check for overloaded operator.
		ICPPFunction o= getOverload(point);
		if (o != null)
			return typeFromFunctionCall(o);

		final IType originalType1 = fArg1.getTypeOrFunctionSet(point);
		final IType type1 = prvalueTypeWithResolvedTypedefs(originalType1);
		if (type1 instanceof ISemanticProblem) {
			return type1;
		}

    	final IType originalType2 = fArg2.getTypeOrFunctionSet(point);
		final IType type2 = prvalueTypeWithResolvedTypedefs(originalType2);
		if (type2 instanceof ISemanticProblem) {
			return type2;
		}

    	IType type= CPPArithmeticConversion.convertCppOperandTypes(fOperator, type1, type2);
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
    				return CPPVisitor.getPointerDiffType(point);
    			}
    			return originalType1;
    		}
    		break;

    	case op_pmarrow:
    	case op_pmdot:
    		if (type2 instanceof ICPPPointerToMemberType) {
    			IType t= ((ICPPPointerToMemberType) type2).getType();
    			if (t instanceof ICPPFunctionType)
    				return t;
    			if (fOperator == op_pmdot && fArg1.getValueCategory(point) == PRVALUE) {
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
		buffer.putByte(ITypeMarshalBuffer.EVAL_BINARY);
		buffer.putByte((byte) fOperator);
		buffer.marshalEvaluation(fArg1, includeValue);
		buffer.marshalEvaluation(fArg2, includeValue);
	}

	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		int op= buffer.getByte();
		ICPPEvaluation arg1= (ICPPEvaluation) buffer.unmarshalEvaluation();
		ICPPEvaluation arg2= (ICPPEvaluation) buffer.unmarshalEvaluation();
		return new EvalBinary(op, arg1, arg2);
	}

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		ICPPEvaluation arg1 = fArg1.instantiate(tpMap, packOffset, within, maxdepth, point);
		ICPPEvaluation arg2 = fArg2.instantiate(tpMap, packOffset, within, maxdepth, point);
		if (arg1 == fArg1 && arg2 == fArg2)
			return this;
		return new EvalBinary(fOperator, arg1, arg2);
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return CPPTemplates.combinePackSize(fArg1.determinePackSize(tpMap), fArg2.determinePackSize(tpMap));
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fArg1.referencesTemplateParameter() || fArg2.referencesTemplateParameter();
	}
}
