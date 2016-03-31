/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class EvalReference extends CPPDependentEvaluation {
	protected final ActivationRecord owningRecord;
	protected final IBinding referredBinding;
	protected EvalCompositeAccess referredSubValue;

	EvalReference(ActivationRecord owningRecord, IBinding referredBinding, IBinding iBinding) {
		super(iBinding);
		this.owningRecord = owningRecord;
		this.referredBinding = referredBinding;
	}
	
	EvalReference(ActivationRecord owningRecord, IBinding referredBinding, IASTNode point) {
		this(owningRecord, referredBinding, findEnclosingTemplate(point));
	}
	
	EvalReference(ActivationRecord owningRecord, EvalCompositeAccess referredSubValue, IBinding iBinding) {
		super(iBinding);
		this.owningRecord = owningRecord;
		this.referredSubValue = referredSubValue;
		this.referredBinding = null;
	}
	
	EvalReference(ActivationRecord owningRecord, EvalCompositeAccess referredSubValue, IASTNode point) {
		this(owningRecord, referredSubValue, findEnclosingTemplate(point));
	}

	@Override
	public boolean isInitializerList() {
		return getValue().isInitializerList();
	}

	@Override
	public boolean isFunctionSet() {
		return getValue().isFunctionSet();
	}

	@Override
	public boolean isTypeDependent() {
		return getValue().isTypeDependent();
	}

	@Override
	public boolean isValueDependent() {
		return getValue().isValueDependent();
	}

	@Override
	public boolean isConstantExpression(IASTNode point) {
		return getValue().isConstantExpression(point);
	}

	@Override
	public IType getType(IASTNode point) {
		return getValue().getType(point);
	}

	@Override
	public IValue getValue(IASTNode point) {
		return getValue().getValue(null);
	}
	
	public ICPPEvaluation getValue() {
		if (referredSubValue != null) {
			return referredSubValue;
		}
		return owningRecord.getVariable(referredBinding);
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
	
	public IBinding getReferredBinding() {
		return referredBinding;
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return getValue().getValueCategory(point);
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		if (referredSubValue != null) {
			return referredSubValue.computeForFunctionCall(record, context);
		}
		ICPPEvaluation referredEval = owningRecord.getVariable(referredBinding);
		if (referredEval instanceof EvalReference) {
			return referredEval.computeForFunctionCall(record, context);
		}
		return referredEval;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return getValue().determinePackSize(tpMap);
	}

	@Override
	public boolean referencesTemplateParameter() {
		return getValue().referencesTemplateParameter();
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		return getValue().instantiate(context, maxDepth);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_REFERENCE);
	}

	public static ISerializableEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		return null;
	}
}
