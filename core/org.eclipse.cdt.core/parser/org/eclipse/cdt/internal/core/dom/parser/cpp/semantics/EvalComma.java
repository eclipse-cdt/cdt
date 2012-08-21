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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromFunctionCall;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

public class EvalComma extends CPPEvaluation {
	private static final ICPPFunction[] NO_FUNCTIONS = {};

	private final ICPPEvaluation[] fArguments;
	private ICPPFunction[] fOverloads;

	private IType fType;

	public EvalComma(ICPPEvaluation[] evals) {
		fArguments= evals;
	}

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
		if (fType != null)
			return fType instanceof TypeOfDependentExpression;

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

	public ICPPFunction[] getOverloads(IASTNode point) {
		if (fOverloads == null) {
			fOverloads= computeOverloads(point);
		}
		return fOverloads;
	}

	private ICPPFunction[] computeOverloads(IASTNode point) {
		if (fArguments.length < 2)
			return NO_FUNCTIONS;

		if (isTypeDependent())
			return NO_FUNCTIONS;

		ICPPFunction[] overloads = new ICPPFunction[fArguments.length - 1];
		ICPPEvaluation e1= fArguments[0];
		for (int i = 1; i < fArguments.length; i++) {
			ICPPEvaluation e2 = fArguments[i];
			ICPPFunction overload = CPPSemantics.findOverloadedOperatorComma(point, e1, e2);
			if (overload == null) {
				e1= e2;
			} else {
				overloads[i - 1] = overload;
				e1= new EvalFixed(typeFromFunctionCall(overload), valueCategoryFromFunctionCall(overload), Value.UNKNOWN);
				if (e1.getTypeOrFunctionSet(point) instanceof ISemanticProblem) {
					e1= e2;
				}
			}
		}
		return overloads;
	}

	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		if (fType == null) {
			fType= computeType(point);
		}
		return fType;
	}

	private IType computeType(IASTNode point) {
		if (isTypeDependent()) {
			return new TypeOfDependentExpression(this);
		}
		ICPPFunction[] overloads = getOverloads(point);
		if (overloads.length > 0) {
			ICPPFunction last = overloads[overloads.length - 1];
			if (last != null) {
				return typeFromFunctionCall(last);
			}
		}
		return fArguments[fArguments.length - 1].getTypeOrFunctionSet(point);
	}

	@Override
	public IValue getValue(IASTNode point) {
		ICPPFunction[] overloads = getOverloads(point);
		if (overloads.length > 0) {
			// TODO(sprigogin): Simulate execution of a function call.
			return Value.create(this);
		}

		return fArguments[fArguments.length - 1].getValue(point);
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		ICPPFunction[] overloads = getOverloads(point);
		if (overloads.length > 0) {
			ICPPFunction last = overloads[overloads.length - 1];
			if (last != null) {
				return valueCategoryFromFunctionCall(last);
			}
		}
		return fArguments[fArguments.length - 1].getValueCategory(point);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putByte(ITypeMarshalBuffer.EVAL_COMMA);
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
		return new EvalComma(args);
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
		return new EvalComma(args);
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
