/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;

public class EvalPointer extends EvalReference {
	private int position;

	public EvalPointer(ActivationRecord record, EvalCompositeAccess referredSubvalue, IASTNode point) {
		this(record, referredSubvalue, findEnclosingTemplate(point));
	}	
	
	public EvalPointer(ActivationRecord record, EvalCompositeAccess referredSubValue, IBinding iBinding) {
		this(record, referredSubValue, iBinding, referredSubValue.getElementId());
	}
	
	public EvalPointer(ActivationRecord record, EvalCompositeAccess referredSubValue, IBinding iBinding, int offset) {
		super(record, referredSubValue, iBinding);
		setPosition(offset);
	}
	
	public EvalPointer(ActivationRecord record, IBinding referredBinding, IBinding templateDefinition) {
		super(record, referredBinding, templateDefinition);
		setPosition(0);
	}
	
	public EvalReference dereference() {
		if (referredSubValue != null) {
			final EvalCompositeAccess pointedToValue = new EvalCompositeAccess(referredSubValue.getParent(), getPosition());
			return new EvalReference(owningRecord, pointedToValue, getTemplateDefinition());
		} else  {
			return new EvalReference(owningRecord,referredBinding, getTemplateDefinition());
		}
	}
	
	@Override
	public IType getType(IASTNode point) {
		IType valueType = getValue().getType(point);
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
		} 
	}

	private boolean isPositionOutOfRange() {
		return subValuePositionOutOfrange() || (this.referredBinding != null && position != 0);
	}

	private boolean subValuePositionOutOfrange() {
		return referredSubValue != null && (position - referredSubValue.getParent().getValue(null).numberOfValues() > 0 || position < 0);
	}
	
	@Override
	public IValue getValue(IASTNode point) {
		return IntegralValue.create(position);
	}
	
	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		return this;
	}
	
	public EvalPointer copy() {
		if (referredSubValue != null) {
			return new EvalPointer(owningRecord, referredSubValue, getTemplateDefinition(), position);
		} else  {
			return new EvalPointer(owningRecord, referredBinding, getTemplateDefinition());
		}
	}
	
	public static EvalPointer createFromAddress(EvalReference reference) {
		if (reference.referredSubValue != null) {
			return new EvalPointer(reference.owningRecord, reference.referredSubValue, reference.getTemplateDefinition());
		} else  {
			return new EvalPointer(reference.owningRecord, reference.referredBinding, reference.getTemplateDefinition());
		}
	}
}
