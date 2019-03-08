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

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of a compound statement expression. Most but not all methods
 * delegate to the evaluation of the last expression in the compound one.
 */
public class EvalCompoundStatementExpression extends CPPDependentEvaluation {
	// fDelegate is the expression inside the expression-statement which is the
	// last statement inside the statement-expression.
	// TODO: Store the executions of the statements that come before the last one,
	//       and simulate their execution in computeForFunctionCall().
	private final ICPPEvaluation fDelegate;

	public EvalCompoundStatementExpression(ICPPEvaluation delegate, IASTNode pointOfDefinition) {
		this(delegate, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalCompoundStatementExpression(ICPPEvaluation delegate, IBinding templateDefinition) {
		super(templateDefinition);
		fDelegate = delegate;
	}

	public ICPPEvaluation getLastEvaluation() {
		return fDelegate;
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
		return fDelegate.isTypeDependent();
	}

	@Override
	public boolean isValueDependent() {
		return fDelegate.isValueDependent();
	}

	@Override
	public boolean isConstantExpression() {
		return fDelegate.isConstantExpression();
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalCompoundStatementExpression)) {
			return false;
		}
		EvalCompoundStatementExpression o = (EvalCompoundStatementExpression) other;
		return fDelegate.isEquivalentTo(o.fDelegate);
	}

	@Override
	public IType getType() {
		return fDelegate.getType();
	}

	@Override
	public IValue getValue() {
		return fDelegate.getValue();
	}

	@Override
	public ValueCategory getValueCategory() {
		return PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_COMPOUND);
		buffer.marshalEvaluation(fDelegate, includeValue);
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation arg = buffer.unmarshalEvaluation();
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalCompoundStatementExpression(arg, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation delegate = fDelegate.instantiate(context, maxDepth);
		if (delegate == fDelegate)
			return this;
		return new EvalCompoundStatementExpression(delegate, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPEvaluation delegate = fDelegate.computeForFunctionCall(record, context.recordStep());
		if (delegate == fDelegate) {
			return this;
		} else {
			EvalCompoundStatementExpression evalCompound = new EvalCompoundStatementExpression(delegate,
					getTemplateDefinition());
			return evalCompound;
		}
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return fDelegate.determinePackSize(tpMap);
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fDelegate.referencesTemplateParameter();
	}

	@Override
	public boolean isNoexcept() {
		return fDelegate.isNoexcept();
	}
}
