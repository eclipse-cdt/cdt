/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.*;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.glvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArithmeticConversion;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics.LookupMode;
import org.eclipse.core.runtime.CoreException;

public class EvalUnary implements ICPPEvaluation {
	private static final ICPPEvaluation ZERO_EVAL = new EvalFixed(CPPSemantics.INT_TYPE, PRVALUE, Value.create(0));
	
	private final int fOperator;
	private final ICPPEvaluation fArgument;
	private ICPPFunction fOverload= CPPFunction.UNINITIALIZED_FUNCTION;
	private IType fType;
	
	public EvalUnary(int operator, ICPPEvaluation operand) {
		fOperator= operator;
		fArgument= operand;
	}

	public int getOperator() {
		return fOperator;
	}

	public ICPPEvaluation getArgument() {
		return fArgument;
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
	public boolean isTypeDependent() {
		if (fType != null) 
			return fType instanceof TypeOfDependentExpression;

		switch(fOperator) {
		case op_alignOf:
		case op_not:
		case op_sizeof:
		case op_sizeofParameterPack:
		case op_throw:
		case op_typeid:
			return false;
		default:
			return fArgument.isTypeDependent();
		}
	}

	@Override
	public boolean isValueDependent() {
		switch(fOperator) {
		case op_alignOf:
		case op_sizeof:
		case op_sizeofParameterPack:
		case op_typeid:
			return fArgument.isTypeDependent();
		case op_throw:
			return false;
		default:
			return fArgument.isValueDependent();
		}
	}

	public ICPPFunction getOverload(IASTNode point) {
		if (fOverload == CPPFunction.UNINITIALIZED_FUNCTION) {
			fOverload= computeOverload(point);
		}
		return fOverload;
	}

	private ICPPFunction computeOverload(IASTNode point) {
    	OverloadableOperator op = OverloadableOperator.fromUnaryExpression(fOperator);
		if (op == null)
			return null;
		
		if (fArgument.isTypeDependent())
			return null;
		
    	IType type = fArgument.getTypeOrFunctionSet(point);
		type = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
		if (!CPPSemantics.isUserDefined(type))
			return null;

		ICPPEvaluation[] args;
	    if (fOperator == IASTUnaryExpression.op_postFixDecr || fOperator == IASTUnaryExpression.op_postFixIncr) {
	    	args = new ICPPEvaluation[] { fArgument, ZERO_EVAL };
	    } else {
	    	args = new ICPPEvaluation[] { fArgument };
	    }
    	return CPPSemantics.findOverloadedOperator(point, args, type, op, LookupMode.LIMITED_GLOBALS);
	}
	
	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		if (fType == null) 
			fType= computeType(point);
		return fType;
	}
	
	private IType computeType(IASTNode point) {
		if (isTypeDependent()) 
			return new TypeOfDependentExpression(this);
		
		ICPPFunction overload = getOverload(point);
		if (overload != null) 
			return ExpressionTypes.typeFromFunctionCall(overload);

    	switch (fOperator) {
		case op_sizeof:
		case op_sizeofParameterPack:
			return CPPVisitor.get_SIZE_T(point);
		case op_typeid:
			return CPPVisitor.get_type_info(point);
		case op_throw:
			return CPPSemantics.VOID_TYPE;
		case op_amper:
			return new CPPPointerType(fArgument.getTypeOrFunctionSet(point));
		case op_star:
			IType type= fArgument.getTypeOrFunctionSet(point);
			type = prvalueType(type);
	    	if (type instanceof IPointerType) {
	    		return glvalueType(((IPointerType) type).getType());
			} 
	    	if (type instanceof ISemanticProblem) {
	    		return type;
	    	}
			return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
		case op_not:
			return CPPBasicType.BOOLEAN;
		case op_postFixDecr:
		case op_postFixIncr:
			return prvalueType(fArgument.getTypeOrFunctionSet(point));
		case op_minus:
		case op_plus:
		case op_tilde:
	    	final IType t1 = prvalueType(fArgument.getTypeOrFunctionSet(point));
			final IType t2= CPPArithmeticConversion.promoteCppType(t1);
			return t2 != null ? t2 : t1;
		}
		return fArgument.getTypeOrFunctionSet(point);
	}

	@Override
	public IValue getValue(IASTNode point) {
		return Value.create(this, point);
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		ICPPFunction overload = getOverload(point);
    	if (overload != null)
    		return valueCategoryFromFunctionCall(overload);

    	switch (fOperator) {
		case op_typeid:
    	case op_star:
    	case op_prefixDecr:
    	case op_prefixIncr:
			return LVALUE;
    	default:
			return PRVALUE;
    	}
    }

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putByte(ITypeMarshalBuffer.EVAL_UNARY);
		buffer.putByte((byte) fOperator);
		buffer.marshalEvaluation(fArgument, includeValue);
	}
	
	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		int op= buffer.getByte();
		ICPPEvaluation arg= (ICPPEvaluation) buffer.unmarshalEvaluation();
		return new EvalUnary(op, arg);
	}
}
