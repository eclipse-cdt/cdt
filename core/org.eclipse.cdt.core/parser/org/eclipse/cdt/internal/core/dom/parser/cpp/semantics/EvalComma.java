/*******************************************************************************
 * Copyright (c) 2012, 2014 Wind River Systems, Inc. and others.
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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromFunctionCall;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class EvalComma extends CPPDependentEvaluation {
	private static final ICPPFunction[] NO_FUNCTIONS = {};

	private final ICPPEvaluation[] fArguments;
	private ICPPFunction[] fOverloads;

	private IType fType;
	private boolean fCheckedIsConstantExpression;
	private boolean fIsConstantExpression;

	public EvalComma(ICPPEvaluation[] evals, IASTNode pointOfDefinition) {
		this(evals, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalComma(ICPPEvaluation[] evals, IBinding templateDefinition) {
		super(templateDefinition);
		fArguments = evals;
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

		return containsDependentType(fArguments);
	}

	@Override
	public boolean isValueDependent() {
		return containsDependentValue(fArguments);
	}

	@Override
	public boolean isConstantExpression() {
		if (!fCheckedIsConstantExpression) {
			fCheckedIsConstantExpression = true;
			fIsConstantExpression = computeIsConstantExpression();
		}
		return fIsConstantExpression;
	}

	private boolean computeIsConstantExpression() {
		if (!areAllConstantExpressions(fArguments)) {
			return false;
		}
		for (ICPPFunction overload : fOverloads) {
			if (!isNullOrConstexprFunc(overload)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalComma)) {
			return false;
		}
		EvalComma o = (EvalComma) other;
		return areEquivalentEvaluations(fArguments, o.fArguments);
	}

	public ICPPFunction[] getOverloads() {
		if (fOverloads == null) {
			fOverloads = computeOverloads();
		}
		return fOverloads;
	}

	private ICPPFunction[] computeOverloads() {
		if (fArguments.length < 2)
			return NO_FUNCTIONS;

		if (isTypeDependent())
			return NO_FUNCTIONS;

		ICPPFunction[] overloads = new ICPPFunction[fArguments.length - 1];
		ICPPEvaluation e1 = fArguments[0];
		for (int i = 1; i < fArguments.length; i++) {
			ICPPEvaluation e2 = fArguments[i];
			ICPPFunction overload = CPPSemantics.findOverloadedOperatorComma(getTemplateDefinitionScope(), e1, e2);
			if (overload == null) {
				e1 = e2;
			} else {
				overloads[i - 1] = overload;
				e1 = new EvalFixed(typeFromFunctionCall(overload), valueCategoryFromFunctionCall(overload),
						IntegralValue.UNKNOWN);
				if (e1.getType() instanceof ISemanticProblem) {
					e1 = e2;
				}
			}
		}
		return overloads;
	}

	@Override
	public IType getType() {
		if (fType == null) {
			fType = computeType();
		}
		return fType;
	}

	private IType computeType() {
		if (isTypeDependent()) {
			return new TypeOfDependentExpression(this);
		}
		ICPPFunction[] overloads = getOverloads();
		if (overloads.length > 0) {
			ICPPFunction last = overloads[overloads.length - 1];
			if (last != null) {
				return typeFromFunctionCall(last);
			}
		}
		return fArguments[fArguments.length - 1].getType();
	}

	@Override
	public IValue getValue() {
		ICPPFunction[] overloads = getOverloads();
		if (overloads.length > 0) {
			// TODO(sprigogin): Simulate execution of a function call.
			return DependentValue.create(this);
		}

		return fArguments[fArguments.length - 1].getValue();
	}

	@Override
	public ValueCategory getValueCategory() {
		ICPPFunction[] overloads = getOverloads();
		if (overloads.length > 0) {
			ICPPFunction last = overloads[overloads.length - 1];
			if (last != null) {
				return valueCategoryFromFunctionCall(last);
			}
		}
		return fArguments[fArguments.length - 1].getValueCategory();
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_COMMA);
		buffer.putInt(fArguments.length);
		for (ICPPEvaluation arg : fArguments) {
			buffer.marshalEvaluation(arg, includeValue);
		}
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int len = buffer.getInt();
		ICPPEvaluation[] args = new ICPPEvaluation[len];
		for (int i = 0; i < args.length; i++) {
			args[i] = buffer.unmarshalEvaluation();
		}
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalComma(args, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation[] args = fArguments;
		for (int i = 0; i < fArguments.length; i++) {
			ICPPEvaluation arg = fArguments[i].instantiate(context, maxDepth);
			if (arg != fArguments[i]) {
				// Propagate instantiation errors for SFINAE purposes.
				if (arg == EvalFixed.INCOMPLETE) {
					return arg;
				}
				if (args == fArguments) {
					args = new ICPPEvaluation[fArguments.length];
					System.arraycopy(fArguments, 0, args, 0, fArguments.length);
				}
				args[i] = arg;
			}
		}
		if (args == fArguments)
			return this;
		return new EvalComma(args, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPEvaluation[] args = fArguments;
		for (int i = 0; i < fArguments.length; i++) {
			ICPPEvaluation arg = fArguments[i].computeForFunctionCall(record, context.recordStep());
			if (arg != fArguments[i]) {
				if (args == fArguments) {
					args = new ICPPEvaluation[fArguments.length];
					System.arraycopy(fArguments, 0, args, 0, fArguments.length);
				}
				args[i] = arg;
			}
		}
		if (args == fArguments) {
			return this;
		}
		EvalComma evalComma = new EvalComma(args, getTemplateDefinition());
		return evalComma;
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

	@Override
	public boolean isNoexcept() {
		if (getOverloads() != null)
			for (ICPPFunction overload : getOverloads()) {
				if (overload != null && !EvalUtil.evaluateNoexceptSpecifier(overload.getType().getNoexceptSpecifier()))
					return false;
			}
		for (ICPPEvaluation arg : fArguments) {
			if (!arg.isNoexcept())
				return false;
		}
		return true;
	}
}
