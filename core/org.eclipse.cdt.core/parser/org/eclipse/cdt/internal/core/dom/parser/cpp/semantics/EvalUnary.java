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
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_alignOf;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_amper;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_minus;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_noexcept;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_not;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_plus;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_postFixDecr;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_postFixIncr;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_prefixDecr;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_prefixIncr;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_sizeof;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_sizeofParameterPack;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_star;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_throw;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_tilde;
import static org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_typeid;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.glvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueTypeWithResolvedTypedefs;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator.SizeAndAlignment;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArithmeticConversion;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics.LookupMode;
import org.eclipse.core.runtime.CoreException;

public class EvalUnary extends CPPEvaluation {
	private static final ICPPEvaluation ZERO_EVAL = new EvalFixed(CPPSemantics.INT_TYPE, PRVALUE, Value.create(0));

	private final int fOperator;
	private final ICPPEvaluation fArgument;
	private final IBinding fAddressOfQualifiedNameBinding;
	private ICPPFunction fOverload= CPPFunction.UNINITIALIZED_FUNCTION;
	private IType fType;

	public EvalUnary(int operator, ICPPEvaluation operand, IBinding addressOfQualifiedNameBinding) {
		fOperator= operator;
		fArgument= operand;
		fAddressOfQualifiedNameBinding= addressOfQualifiedNameBinding;
	}

	public int getOperator() {
		return fOperator;
	}

	public ICPPEvaluation getArgument() {
		return fArgument;
	}

	public IBinding getAddressOfQualifiedNameBinding() {
		return fAddressOfQualifiedNameBinding;
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
		switch (fOperator) {
		case op_alignOf:
		case op_sizeof:
		case op_sizeofParameterPack:
		case op_typeid:
			return fArgument.isTypeDependent();
		case op_noexcept:
			return fArgument.referencesTemplateParameter();
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

		if (fAddressOfQualifiedNameBinding instanceof ICPPMember) {
			ICPPMember member= (ICPPMember) fAddressOfQualifiedNameBinding;
			if (!member.isStatic()) 
				return null;
		}

    	IType type = fArgument.getTypeOrFunctionSet(point);
		type = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
		if (!CPPSemantics.isUserDefined(type))
			return null;

		ICPPEvaluation[] args;
	    if (fOperator == op_postFixDecr || fOperator == op_postFixIncr) {
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
			if (fAddressOfQualifiedNameBinding instanceof ICPPMember) {
				ICPPMember member= (ICPPMember) fAddressOfQualifiedNameBinding;
				if (!member.isStatic()) {
					try {
						return new CPPPointerToMemberType(member.getType(), member.getClassOwner(), false, false, false);
					} catch (DOMException e) {
						return e.getProblem();
					}
				}
			}
			return new CPPPointerType(fArgument.getTypeOrFunctionSet(point));
		case op_star:
			IType type= fArgument.getTypeOrFunctionSet(point);
			type = prvalueTypeWithResolvedTypedefs(type);
	    	if (type instanceof IPointerType) {
	    		return glvalueType(((IPointerType) type).getType());
			}
	    	if (type instanceof ISemanticProblem) {
	    		return type;
	    	}
			return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
		case op_noexcept:
		case op_not:
			return CPPBasicType.BOOLEAN;
		case op_postFixDecr:
		case op_postFixIncr:
			return prvalueType(fArgument.getTypeOrFunctionSet(point));
		case op_minus:
		case op_plus:
		case op_tilde:
	    	final IType t1 = prvalueType(fArgument.getTypeOrFunctionSet(point));
			final IType t2 = SemanticUtil.getNestedType(t1, TDEF);
			final IType t3= CPPArithmeticConversion.promoteCppType(t2);
			return (t3 == null || t3 == t2) ? t1 : t3;
		}
		return fArgument.getTypeOrFunctionSet(point);
	}

	@Override
	public IValue getValue(IASTNode point) {
		if (isValueDependent())
			return Value.create(this);
			
		if (getOverload(point) != null) {
			// TODO(sprigogin): Simulate execution of a function call.
			return Value.create(this);
		}

		switch (fOperator) {
			case op_sizeof: {
				SizeAndAlignment info = SizeofCalculator.getSizeAndAlignment(fArgument.getTypeOrFunctionSet(point), point);
				return info == null ? Value.UNKNOWN : Value.create(info.size);
			}
			case op_alignOf: {
				SizeAndAlignment info = SizeofCalculator.getSizeAndAlignment(fArgument.getTypeOrFunctionSet(point), point);
				return info == null ? Value.UNKNOWN : Value.create(info.alignment);
			}
			case op_noexcept:
				return Value.UNKNOWN;  // TODO(sprigogin): Implement
			case op_sizeofParameterPack:
				return Value.UNKNOWN;  // TODO(sprigogin): Implement
			case op_typeid:
				return Value.UNKNOWN;  // TODO(sprigogin): Implement
			case op_throw:
				return Value.UNKNOWN;  // TODO(sprigogin): Implement
		}

		IValue val = fArgument.getValue(point);
		if (val == null)
			return Value.UNKNOWN;
		
		Long num = val.numericalValue();
		if (num != null) {
			return Value.evaluateUnaryExpression(fOperator, num);
		}
		return Value.create(this);
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
		buffer.marshalBinding(fAddressOfQualifiedNameBinding);
	}

	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		int op= buffer.getByte();
		ICPPEvaluation arg= (ICPPEvaluation) buffer.unmarshalEvaluation();
		IBinding binding= buffer.unmarshalBinding();
		return new EvalUnary(op, arg, binding);
	}

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		ICPPEvaluation argument = fArgument.instantiate(tpMap, packOffset, within, maxdepth, point);
		IBinding aoqn = fAddressOfQualifiedNameBinding;
		if (aoqn instanceof ICPPUnknownBinding) {
			try {
				aoqn= CPPTemplates.resolveUnknown((ICPPUnknownBinding) aoqn, tpMap, packOffset, within, point);
			} catch (DOMException e) {
			}
		}
		if (argument == fArgument && aoqn == fAddressOfQualifiedNameBinding)
			return this;
		
		return new EvalUnary(fOperator, argument, aoqn);
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return fArgument.determinePackSize(tpMap);
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fArgument.referencesTemplateParameter();
	}
}
