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
import org.eclipse.cdt.internal.core.dom.parser.CompositeValue;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of an expression.
 */
public class EvalInitList extends CPPDependentEvaluation {
	private final ICPPEvaluation[] fClauses;
	private boolean fCheckedIsValueDependent;
	private boolean fIsValueDependent;
	private boolean fCheckedIsConstantExpression;
	private boolean fIsConstantExpression;

	public EvalInitList(ICPPEvaluation[] clauses, IASTNode pointOfDefinition) {
		this(clauses, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalInitList(ICPPEvaluation[] clauses, IBinding templateDefinition) {
		super(templateDefinition);
		fClauses = clauses;
	}

	public ICPPEvaluation[] getClauses() {
		return fClauses;
	}

	@Override
	public boolean isInitializerList() {
		return true;
	}

	@Override
	public boolean isFunctionSet() {
		return false;
	}

	@Override
	public boolean isTypeDependent() {
		return containsDependentType(fClauses);
	}

	@Override
	public boolean isValueDependent() {
		if (!fCheckedIsValueDependent) {
			fCheckedIsValueDependent = true;
			fIsValueDependent = containsDependentValue(fClauses);
		}
		return fIsValueDependent;
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
		return areAllConstantExpressions(fClauses);
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalInitList)) {
			return false;
		}
		EvalInitList o = (EvalInitList) other;
		return areEquivalentEvaluations(fClauses, o.fClauses);
	}

	@Override
	public IType getType() {
		return new InitializerListType(this);
	}

	@Override
	public IValue getValue() {
		if (isValueDependent()) {
			return DependentValue.create(this);
		}
		return CompositeValue.create(this);
	}

	@Override
	public ValueCategory getValueCategory() {
		return PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_INIT_LIST);
		buffer.putInt(fClauses.length);
		for (ICPPEvaluation arg : fClauses) {
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
		return new EvalInitList(args, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation[] clauses = instantiateExpressions(fClauses, context, maxDepth);
		if (clauses == fClauses)
			return this;
		return new EvalInitList(clauses, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPEvaluation[] clauses = fClauses;
		for (int i = 0; i < fClauses.length; i++) {
			ICPPEvaluation clause = fClauses[i].computeForFunctionCall(record, context.recordStep());
			if (clause != fClauses[i]) {
				if (clauses == fClauses) {
					clauses = new ICPPEvaluation[fClauses.length];
					System.arraycopy(fClauses, 0, clauses, 0, fClauses.length);
				}
				clauses[i] = clause;
			}
		}
		if (clauses == fClauses) {
			return this;
		}
		return new EvalInitList(clauses, this.getTemplateDefinition());
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = CPPTemplates.PACK_SIZE_NOT_FOUND;
		for (ICPPEvaluation arg : fClauses) {
			r = CPPTemplates.combinePackSize(r, arg.determinePackSize(tpMap));
		}
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		for (ICPPEvaluation clause : fClauses) {
			if (clause.referencesTemplateParameter())
				return true;
		}
		return false;
	}

	@Override
	public boolean isNoexcept() {
		for (ICPPEvaluation eval : getClauses()) {
			if (!eval.isNoexcept())
				return false;
		}
		return true;
	}
}
