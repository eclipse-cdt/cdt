/*******************************************************************************
 * Copyright (c) 2013, 2014 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Evaluation for a pack expansion expression.
 */
public class EvalPackExpansion extends CPPDependentEvaluation {
	private ICPPEvaluation fExpansionPattern;
	private IType fType;

	public EvalPackExpansion(ICPPEvaluation expansionPattern, IASTNode pointOfDefinition) {
		this(expansionPattern, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalPackExpansion(ICPPEvaluation expansionPattern, IBinding templateDefinition) {
		super(templateDefinition);
		fExpansionPattern = expansionPattern;
	}

	public ICPPEvaluation getExpansionPattern() {
		return fExpansionPattern;
	}

	@Override
	public boolean isInitializerList() {
		return fExpansionPattern.isInitializerList();
	}

	@Override
	public boolean isFunctionSet() {
		return fExpansionPattern.isFunctionSet();
	}

	@Override
	public boolean isTypeDependent() {
		return fExpansionPattern.isTypeDependent();
	}

	@Override
	public boolean isValueDependent() {
		return fExpansionPattern.isValueDependent();
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalPackExpansion)) {
			return false;
		}
		EvalPackExpansion o = (EvalPackExpansion) other;
		return fExpansionPattern.isEquivalentTo(o.fExpansionPattern);
	}

	@Override
	public IType getType() {
		if (fType == null) {
			IType type = fExpansionPattern.getType();
			if (type == null) {
				fType = ProblemType.UNKNOWN_FOR_EXPRESSION;
			} else {
				fType = new CPPParameterPackType(type);
			}
		}
		return fType;
	}

	@Override
	public IValue getValue() {
		return DependentValue.create(fExpansionPattern);
	}

	@Override
	public ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation expansionPattern = fExpansionPattern.instantiate(context, maxDepth);
		if (expansionPattern == fExpansionPattern)
			return this;
		return new EvalPackExpansion(expansionPattern, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPEvaluation expansionPattern = fExpansionPattern.computeForFunctionCall(record, context.recordStep());
		if (expansionPattern == fExpansionPattern) {
			return this;
		}

		EvalPackExpansion evalParamPack = new EvalPackExpansion(expansionPattern, getTemplateDefinition());
		return evalParamPack;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return CPPTemplates.PACK_SIZE_NOT_FOUND;
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fExpansionPattern.referencesTemplateParameter();
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_PACK_EXPANSION);
		buffer.marshalEvaluation(fExpansionPattern, includeValue);
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation expansionPattern = buffer.unmarshalEvaluation();
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalPackExpansion(expansionPattern, templateDefinition);
	}

	@Override
	public boolean isNoexcept() {
		assert false; // Shouldn't exist outside of a dependent context
		return true;
	}
}