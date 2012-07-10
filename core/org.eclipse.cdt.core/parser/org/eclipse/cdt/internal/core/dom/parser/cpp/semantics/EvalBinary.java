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

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.glvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueTypeWithResolvedTypedefs;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
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
		return Value.create(this, point);
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
		case IASTBinaryExpression.op_assign:
		case IASTBinaryExpression.op_binaryAndAssign:
		case IASTBinaryExpression.op_binaryOrAssign:
		case IASTBinaryExpression.op_binaryXorAssign:
		case IASTBinaryExpression.op_divideAssign:
		case IASTBinaryExpression.op_minusAssign:
		case IASTBinaryExpression.op_moduloAssign:
		case IASTBinaryExpression.op_multiplyAssign:
		case IASTBinaryExpression.op_plusAssign:
		case IASTBinaryExpression.op_shiftLeftAssign:
		case IASTBinaryExpression.op_shiftRightAssign:
			return LVALUE;

		case IASTBinaryExpression.op_pmdot:
			if (!(getTypeOrFunctionSet(point) instanceof ICPPFunctionType))
				return fArg1.getValueCategory(point);
			break;

		case IASTBinaryExpression.op_pmarrow:
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

    	case IASTBinaryExpression.op_lessEqual:
    	case IASTBinaryExpression.op_lessThan:
    	case IASTBinaryExpression.op_greaterEqual:
    	case IASTBinaryExpression.op_greaterThan:
    	case IASTBinaryExpression.op_logicalAnd:
    	case IASTBinaryExpression.op_logicalOr:
    	case IASTBinaryExpression.op_equals:
    	case IASTBinaryExpression.op_notequals:
    		return CPPBasicType.BOOLEAN;

    	case IASTBinaryExpression.op_plus:
    		if (type1 instanceof IPointerType) {
        		return ExpressionTypes.restoreTypedefs(type1, originalType1);
    		}
    		if (type2 instanceof IPointerType) {
        		return ExpressionTypes.restoreTypedefs(type2, originalType2);
    		}
    		break;

    	case IASTBinaryExpression.op_minus:
    		if (type1 instanceof IPointerType) {
    			if (type2 instanceof IPointerType) {
    				return CPPVisitor.getPointerDiffType(point);
    			}
    			return originalType1;
    		}
    		break;

    	case ICPPASTBinaryExpression.op_pmarrow:
    	case ICPPASTBinaryExpression.op_pmdot:
    		if (type2 instanceof ICPPPointerToMemberType) {
    			IType t= ((ICPPPointerToMemberType) type2).getType();
    			if (t instanceof ICPPFunctionType)
    				return t;
    			if (fOperator == ICPPASTBinaryExpression.op_pmdot && fArg1.getValueCategory(point) == PRVALUE) {
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
}
