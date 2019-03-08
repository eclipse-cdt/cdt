/*******************************************************************************
 * Copyright (c) 2012, 2016 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.dom.ast.IASTBinaryTypeIdExpression.Operator;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Evaluation for a binary type-id expression.
 */
public class EvalBinaryTypeId extends CPPDependentEvaluation {
	private final Operator fOperator;
	private final IType fType1, fType2;

	private boolean fCheckedValueDependent;
	private boolean fIsValueDependent;

	public EvalBinaryTypeId(Operator kind, IType type1, IType type2, IASTNode pointOfDefinition) {
		this(kind, type1, type2, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalBinaryTypeId(Operator kind, IType type1, IType type2, IBinding templateDefinition) {
		super(templateDefinition);
		fOperator = kind;
		fType1 = type1;
		fType2 = type2;
	}

	public Operator getOperator() {
		return fOperator;
	}

	public IType getType1() {
		return fType1;
	}

	public IType getType2() {
		return fType2;
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
	public IType getType() {
		switch (fOperator) {
		case __is_base_of:
		case __is_trivially_assignable:
			return CPPBasicType.BOOLEAN;
		}
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	@Override
	public IValue getValue() {
		if (isValueDependent())
			return DependentValue.create(this);

		return ValueFactory.evaluateBinaryTypeIdExpression(fOperator, fType1, fType2);
	}

	@Override
	public boolean isTypeDependent() {
		return false;
	}

	@Override
	public boolean isValueDependent() {
		if (!fCheckedValueDependent) {
			fIsValueDependent = CPPTemplates.isDependentType(fType1) || CPPTemplates.isDependentType(fType2);
			fCheckedValueDependent = true;
		}
		return fIsValueDependent;
	}

	@Override
	public boolean isConstantExpression() {
		return true;
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalBinaryTypeId)) {
			return false;
		}
		EvalBinaryTypeId o = (EvalBinaryTypeId) other;
		return fOperator == o.fOperator && fType1.isSameType(o.fType1) && fType2.isSameType(o.fType2);
	}

	@Override
	public ValueCategory getValueCategory() {
		return PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_BINARY_TYPE_ID);
		buffer.putByte((byte) fOperator.ordinal());
		buffer.marshalType(fType1);
		buffer.marshalType(fType2);
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int op = buffer.getByte();
		IType arg1 = buffer.unmarshalType();
		IType arg2 = buffer.unmarshalType();
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalBinaryTypeId(Operator.values()[op], arg1, arg2, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		IType type1 = CPPTemplates.instantiateType(fType1, context);
		IType type2 = CPPTemplates.instantiateType(fType2, context);
		if (type1 == fType1 && type2 == fType2)
			return this;
		return new EvalBinaryTypeId(fOperator, type1, type2, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		return this;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return CPPTemplates.combinePackSize(CPPTemplates.determinePackSize(fType1, tpMap),
				CPPTemplates.determinePackSize(fType2, tpMap));
	}

	@Override
	public boolean referencesTemplateParameter() {
		return isValueDependent();
	}

	@Override
	public boolean isNoexcept() {
		return true;
	}
}
