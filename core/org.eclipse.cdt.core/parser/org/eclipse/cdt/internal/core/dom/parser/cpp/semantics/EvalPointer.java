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

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

public final class EvalPointer extends EvalReference {
	// The position will only be nonzero if the EvalReference has a referredSubValue,
	// not a referredBinding.
	private int position;

	public EvalPointer(ActivationRecord record, EvalCompositeAccess referredSubValue, IASTNode point) {
		this(record, referredSubValue, findEnclosingTemplate(point));
	}

	public EvalPointer(ActivationRecord record, EvalCompositeAccess referredSubValue, IBinding templateDefinition) {
		this(record, referredSubValue, templateDefinition, referredSubValue.getElementId());
	}

	public EvalPointer(ActivationRecord record, EvalCompositeAccess referredSubValue, IBinding templateDefinition,
			int offset) {
		super(record, referredSubValue, templateDefinition);
		setPosition(offset);
	}

	public EvalPointer(ActivationRecord record, IBinding referredBinding, IBinding templateDefinition) {
		super(record, referredBinding, templateDefinition);
		setPosition(0);
	}

	public EvalReference dereference() {
		if (referredSubValue != null) {
			final EvalCompositeAccess pointedToValue = new EvalCompositeAccess(referredSubValue.getParent(),
					getPosition());
			return new EvalReference(owningRecord, pointedToValue, getTemplateDefinition());
		} else {
			return new EvalReference(owningRecord, referredBinding, getTemplateDefinition());
		}
	}

	@Override
	public IType getType() {
		IType valueType = getTargetEvaluation().getType();
		return new CPPPointerType(valueType, false, false, false);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
		invalidatePointerIfPositionOutOfRange();
	}

	private void invalidatePointerIfPositionOutOfRange() {
		if (isPositionOutOfRange()) {
			referredSubValue = new EvalCompositeAccess(EvalFixed.INCOMPLETE, 0);
			referredBinding = null;
		}
	}

	private boolean isPositionOutOfRange() {
		return subValuePositionOutOfrange() || (this.referredBinding != null && position != 0);
	}

	private boolean subValuePositionOutOfrange() {
		return referredSubValue != null
				&& (position - referredSubValue.getParent().getValue().numberOfSubValues() > 0 || position < 0);
	}

	@Override
	public IValue getValue() {
		// TODO(nathanridge): Why does it make sense to consider a pointer's value to be its offset
		// into the underlying array?
		return IntegralValue.create(position);
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		return this;
	}

	public EvalPointer copy() {
		if (referredSubValue != null) {
			return new EvalPointer(owningRecord, referredSubValue, getTemplateDefinition(), position);
		} else {
			return new EvalPointer(owningRecord, referredBinding, getTemplateDefinition());
		}
	}

	public static EvalPointer createFromAddress(EvalReference reference) {
		if (reference.referredSubValue != null) {
			return new EvalPointer(reference.owningRecord, reference.referredSubValue,
					reference.getTemplateDefinition());
		} else {
			return new EvalPointer(reference.owningRecord, reference.referredBinding,
					reference.getTemplateDefinition());
		}
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EVAL_POINTER;
		if (referredSubValue != null) {
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		}
		buffer.putShort(firstBytes);
		if (referredSubValue != null) {
			buffer.marshalEvaluation(referredSubValue, includeValue);
			buffer.putInt(position);
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
			int position = buffer.getInt();
			IBinding templateDefinition = buffer.unmarshalBinding();
			return new EvalPointer(new ActivationRecord(), referredSubValue, templateDefinition, position);
		} else {
			IBinding referredBinding = buffer.unmarshalBinding();
			ICPPEvaluation value = buffer.unmarshalEvaluation();
			ActivationRecord record = new ActivationRecord();
			record.update(referredBinding, value);
			IBinding templateDefinition = buffer.unmarshalBinding();
			return new EvalPointer(record, referredBinding, templateDefinition);
		}
	}

}
