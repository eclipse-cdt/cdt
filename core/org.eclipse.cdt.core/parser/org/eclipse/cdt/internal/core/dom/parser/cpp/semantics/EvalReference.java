/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

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

public class EvalReference extends CPPDependentEvaluation {
	protected final ActivationRecord owningRecord;

	// The following invariant must be true for instances of this class:
	// (referredBinding == null) != (referredSubValue == null)
	protected IBinding referredBinding;
	protected EvalCompositeAccess referredSubValue;

	public EvalReference(ActivationRecord owningRecord, IBinding referredBinding, IBinding templateDefinition) {
		super(templateDefinition);
		this.owningRecord = owningRecord;
		this.referredBinding = referredBinding;
	}

	EvalReference(ActivationRecord owningRecord, IBinding referredBinding, IASTNode point) {
		this(owningRecord, referredBinding, findEnclosingTemplate(point));
	}

	public EvalReference(ActivationRecord owningRecord, EvalCompositeAccess referredSubValue,
			IBinding templateDefinition) {
		super(templateDefinition);
		this.owningRecord = owningRecord;
		this.referredSubValue = referredSubValue;
		this.referredBinding = null;
	}

	EvalReference(ActivationRecord owningRecord, EvalCompositeAccess referredSubValue, IASTNode point) {
		this(owningRecord, referredSubValue, findEnclosingTemplate(point));
	}

	public ActivationRecord getOwningRecord() {
		return owningRecord;
	}

	public EvalCompositeAccess getReferredSubValue() {
		return referredSubValue;
	}

	@Override
	public final boolean isInitializerList() {
		return getTargetEvaluation().isInitializerList();
	}

	@Override
	public final boolean isFunctionSet() {
		return getTargetEvaluation().isFunctionSet();
	}

	@Override
	public final boolean isTypeDependent() {
		return getTargetEvaluation().isTypeDependent();
	}

	@Override
	public final boolean isValueDependent() {
		return getTargetEvaluation().isValueDependent();
	}

	@Override
	public final boolean isConstantExpression() {
		return getTargetEvaluation().isConstantExpression();
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		// This probably doesn't need to be implemented as this evaluation type
		// only arises as an intermediate artifact during constexpr processing.
		return false;
	}

	@Override
	public IType getType() {
		return getTargetEvaluation().getType();
	}

	@Override
	public IValue getValue() {
		return getTargetEvaluation().getValue();
	}

	public final ICPPEvaluation getTargetEvaluation() {
		if (referredSubValue != null) {
			return referredSubValue;
		}
		ICPPEvaluation targetValue = owningRecord.getVariable(referredBinding);
		return targetValue == null ? EvalFixed.INCOMPLETE : targetValue;
	}

	public void update(ICPPEvaluation eval) {
		if (referredBinding != null) {
			ICPPEvaluation oldValue = owningRecord.getVariable(referredBinding);
			if (oldValue instanceof EvalReference) {
				((EvalReference) oldValue).update(eval);
			} else {
				owningRecord.update(referredBinding, eval);
			}
		} else {
			referredSubValue.update(eval);
		}
	}

	public final IBinding getReferredBinding() {
		return referredBinding;
	}

	@Override
	public ValueCategory getValueCategory() {
		// An EvalReference always refers to a named variable, so its value category is lvalue.
		return ValueCategory.LVALUE;
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		if (referredSubValue != null) {
			return referredSubValue.computeForFunctionCall(record, context);
		}
		ICPPEvaluation referredEval = owningRecord.getVariable(referredBinding);
		if (referredEval instanceof EvalReference) {
			// TODO(nathanridge): Why are we doing this for EvalReference only?
			return referredEval.computeForFunctionCall(record, context);
		}
		return referredEval;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return getTargetEvaluation().determinePackSize(tpMap);
	}

	@Override
	public boolean referencesTemplateParameter() {
		return getTargetEvaluation().referencesTemplateParameter();
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		// TODO(nathanridge): Why are we losing the EvalReference wrapper here?
		return getTargetEvaluation().instantiate(context, maxDepth);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EVAL_REFERENCE;
		if (referredSubValue != null) {
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		}
		buffer.putShort(firstBytes);
		if (referredSubValue != null) {
			buffer.marshalEvaluation(referredSubValue, includeValue);
		} else {
			buffer.marshalBinding(referredBinding);
			buffer.marshalEvaluation(owningRecord.getVariable(referredBinding), includeValue);
		}
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		boolean subValue = (firstBytes & ITypeMarshalBuffer.FLAG1) != 0;
		if (subValue) {
			EvalCompositeAccess referredSubValue = (EvalCompositeAccess) buffer.unmarshalEvaluation();
			IBinding templateDefinition = buffer.unmarshalBinding();
			return new EvalReference(new ActivationRecord(), referredSubValue, templateDefinition);
		} else {
			IBinding referredBinding = buffer.unmarshalBinding();
			ICPPEvaluation value = buffer.unmarshalEvaluation();
			ActivationRecord record = new ActivationRecord();
			record.update(referredBinding, value);
			IBinding templateDefinition = buffer.unmarshalBinding();
			return new EvalReference(record, referredBinding, templateDefinition);
		}
	}

	@Override
	public boolean isNoexcept() {
		assert false;
		return true;
	}
}
