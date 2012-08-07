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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromReturnType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromReturnType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics.LookupMode;
import org.eclipse.core.runtime.CoreException;

public class EvalFunctionCall extends CPPEvaluation {
	private final ICPPEvaluation[] fArguments;
	private ICPPFunction fOverload= CPPFunction.UNINITIALIZED_FUNCTION;
	private IType fType;

	public EvalFunctionCall(ICPPEvaluation[] args) {
		fArguments= args;
	}

	/**
	 * Returns arguments of the function call. The first argument is the function name, the rest
	 * are arguments passed to the function.
	 */
	public ICPPEvaluation[] getArguments() {
		return fArguments;
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
		for (ICPPEvaluation arg : fArguments) {
			if (arg.isTypeDependent())
				return true;
		}
		return false;
	}

	@Override
	public boolean isValueDependent() {
		for (ICPPEvaluation arg : fArguments) {
			if (arg.isValueDependent())
				return true;
		}
		return false;
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

		IType t= SemanticUtil.getNestedType(fArguments[0].getTypeOrFunctionSet(point), TDEF|REF|CVTYPE);
		if (t instanceof ICPPClassType) {
	    	return CPPSemantics.findOverloadedOperator(point, fArguments, t, OverloadableOperator.PAREN, LookupMode.NO_GLOBALS);
		}
		return null;
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


		final ICPPEvaluation arg0 = fArguments[0];
		IType t= SemanticUtil.getNestedType(arg0.getTypeOrFunctionSet(point), TDEF|REF|CVTYPE);
		if (t instanceof ICPPClassType) {
			return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
		}

		if (t instanceof IPointerType) {
			t= SemanticUtil.getNestedType(((IPointerType) t).getType(), TDEF | REF | CVTYPE);
		}
		if (t instanceof IFunctionType) {
			t = typeFromReturnType(((IFunctionType) t).getReturnType());
			if (arg0 instanceof EvalMemberAccess) {
				t= ExpressionTypes.restoreTypedefs(t, ((EvalMemberAccess) arg0).getOwnerType());
			}
			return t;
		}
		return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
	}

	@Override
	public IValue getValue(IASTNode point) {
		// TODO(sprigogin): Simulate execution of a function call if the value is not dependent.
		return Value.create(this);
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		ICPPFunction overload = getOverload(point);
    	if (overload != null)
    		return valueCategoryFromFunctionCall(overload);


		IType t= fArguments[0].getTypeOrFunctionSet(point);
		if (t instanceof IPointerType) {
			t= SemanticUtil.getNestedType(((IPointerType) t).getType(), TDEF | REF | CVTYPE);
		}
		if (t instanceof IFunctionType) {
			return valueCategoryFromReturnType(((IFunctionType) t).getReturnType());
		}
		return ValueCategory.PRVALUE;
    }

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putByte(ITypeMarshalBuffer.EVAL_FUNCTION_CALL);
		buffer.putShort((short) fArguments.length);
		for (ICPPEvaluation arg : fArguments) {
			buffer.marshalEvaluation(arg, includeValue);
		}
	}

	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		int len= buffer.getShort();
		ICPPEvaluation[] args = new ICPPEvaluation[len];
		for (int i = 0; i < args.length; i++) {
			args[i]= (ICPPEvaluation) buffer.unmarshalEvaluation();
		}
		return new EvalFunctionCall(args);
	}

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		ICPPEvaluation[] args = fArguments;
		for (int i = 0; i < fArguments.length; i++) {
			ICPPEvaluation arg = fArguments[i].instantiate(tpMap, packOffset, within, maxdepth, point);
			if (arg != fArguments[i]) {
				if (args == fArguments) {
					args = new ICPPEvaluation[fArguments.length];
					System.arraycopy(fArguments, 0, args, 0, fArguments.length);
				}
				args[i] = arg;
			}
		}
		if (args == fArguments)
			return this;

		if (args[0] instanceof EvalFunctionSet && getOverload(point) == null) {
			// Resolve the function using the parameters of the function call.
			args[0] = ((EvalFunctionSet) args[0]).resolveFunction(Arrays.copyOfRange(args, 1, args.length), point);
		}
		return new EvalFunctionCall(args);
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = CPPTemplates.PACK_SIZE_NOT_FOUND;
		for (ICPPEvaluation arg : fArguments) {
			r = CPPTemplates.combinePackSize(r, arg.determinePackSize(tpMap));
		}
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		for (ICPPEvaluation arg : fArguments) {
			if (arg.referencesTemplateParameter())
				return true;
		}
		return false;
	}
}
