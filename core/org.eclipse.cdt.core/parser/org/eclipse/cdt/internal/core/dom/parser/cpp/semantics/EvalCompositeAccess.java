/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFunctionCall.ParameterPackType;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents an access to a sub-value of a composite value, identified by an index.
 * Composite values can include arrays, structures, and parameter packs (see {@code CompositeValue}).
 */
public final class EvalCompositeAccess implements ICPPEvaluation {
	private final ICPPEvaluation parent;  // The composite value being accessed
	private final int elementId;          // The index of the sub-value being accessed

	public EvalCompositeAccess(ICPPEvaluation parent, int elementId) {
		Assert.isNotNull(parent);
		this.parent = parent;
		this.elementId = elementId;
	}

	public void update(ICPPEvaluation newValue) {
		parent.getValue(null).setSubValue(elementId, newValue);
	}

	@Override
	public boolean isInitializerList() {
		return getTargetEvaluation().isInitializerList();
	}

	private ICPPEvaluation getTargetEvaluation() {
		return parent.getValue(null).getSubValue(elementId);
	}

	@Override
	public boolean isFunctionSet() {
		return getTargetEvaluation().isFunctionSet();
	}

	@Override
	public boolean isTypeDependent() {
		return getTargetEvaluation().isTypeDependent();
	}

	@Override
	public boolean isValueDependent() {
		return getTargetEvaluation().isValueDependent();
	}

	@Override
	public boolean isConstantExpression(IASTNode point) {
		return getTargetEvaluation().isConstantExpression(point);
	}

	@Override
	public IType getType(IASTNode point) {
		IType type = getParent().getType(point);
		type = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);

		if (type instanceof IArrayType) {
			IArrayType arrayType = (IArrayType) type;
			return arrayType.getType();
		} else if (type instanceof InitializerListType) {
			InitializerListType initListType = (InitializerListType) type;
			ICPPEvaluation[] clauses = initListType.getEvaluation().getClauses();
			if (elementId >= 0 && elementId < clauses.length) {
				return clauses[elementId].getType(point);
			} else {
				return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
			}
		} else if (type instanceof ICPPClassType) {
			ICPPClassType classType = (ICPPClassType) type;
			IField[] fields = ClassTypeHelper.getFields(classType, point);
			if (elementId >= 0 && elementId < fields.length) {
				return fields[elementId].getType();
			} else {
				return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
			}
		} else if (type instanceof ParameterPackType) {
			ParameterPackType parameterPackType = (ParameterPackType) type;
			return parameterPackType.getTypes()[elementId];
		} else if (type instanceof ICPPBasicType) {
			return type;
		}
		return null;
	}

	@Override
	public IValue getValue(IASTNode point) {
		return getTargetEvaluation().getValue(point);
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return getTargetEvaluation().getValueCategory(point);
	}

	@Override
	public char[] getSignature() {
		return getTargetEvaluation().getSignature();
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		if (getTargetEvaluation() != EvalFixed.INCOMPLETE) {
			return getTargetEvaluation().computeForFunctionCall(record, context);
		} else {
			ICPPEvaluation evaluatedComposite = parent.computeForFunctionCall(record, context);
			return evaluatedComposite.getValue(context.getPoint()).getSubValue(elementId).computeForFunctionCall(record, context);
		}
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
	public IBinding getTemplateDefinition() {
		return parent.getTemplateDefinition();
	}

	public ICPPEvaluation getParent() {
		return parent;
	}

	public int getElementId() {
		return elementId;
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		return getTargetEvaluation().instantiate(context, maxDepth);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_COMPOSITE_ACCESS);
		buffer.marshalEvaluation(parent, includeValue);
		buffer.putInt(elementId);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation parent = buffer.unmarshalEvaluation();
		int elementId = buffer.getInt();
		return new EvalCompositeAccess(parent, elementId);
	}
}
