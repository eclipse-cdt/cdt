/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNaryTypeIdExpression.Operator;
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
 * Evaluation for a n-ary type-id expression.
 */
public class EvalNaryTypeId extends CPPDependentEvaluation {
	private final Operator fOperator;
	private final IType[] fOperands;

	private boolean fCheckedValueDependent;
	private boolean fIsValueDependent;

	public EvalNaryTypeId(Operator operator, IType[] operands, IASTNode pointOfDefinition) {
		this(operator, operands, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalNaryTypeId(Operator operator, IType[] operands, IBinding templateDefinition) {
		super(templateDefinition);
		fOperator = operator;
		fOperands = operands;
	}

	public Operator getOperator() {
		return fOperator;
	}

	public IType[] getOperands() {
		return fOperands;
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
		return false;
	}

	@Override
	public boolean isValueDependent() {
		if (!fCheckedValueDependent) {
			for (IType operand : fOperands) {
				if (CPPTemplates.isDependentType(operand)) {
					fIsValueDependent = true;
				}
			}
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
		if (!(other instanceof EvalNaryTypeId)) {
			return false;
		}
		EvalNaryTypeId o = (EvalNaryTypeId) other;
		return fOperator == o.fOperator && areEquivalentTypes(fOperands, o.fOperands);
	}

	@Override
	public IType getType() {
		switch (fOperator) {
		case __is_trivially_constructible:
		case __is_constructible:
			return CPPBasicType.BOOLEAN;
		}
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	@Override
	public IValue getValue() {
		if (isValueDependent()) {
			return DependentValue.create(this);
		}

		return ValueFactory.evaluateNaryTypeIdExpression(fOperator, fOperands, getTemplateDefinition());
	}

	@Override
	public ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		IType[] operands = CPPTemplates.instantiateTypes(fOperands, context);
		if (operands == fOperands) {
			return this;
		}
		return new EvalNaryTypeId(fOperator, operands, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		return this;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int result = 0;
		for (int i = 0; i < fOperands.length; i++) {
			result = CPPTemplates.combinePackSize(result, CPPTemplates.determinePackSize(fOperands[i], tpMap));
		}
		return result;
	}

	@Override
	public boolean referencesTemplateParameter() {
		return isValueDependent();
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_NARY_TYPE_ID);
		buffer.putByte((byte) fOperator.ordinal());
		buffer.putInt(fOperands.length);
		for (IType operand : fOperands) {
			buffer.marshalType(operand);
		}
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int op = buffer.getByte();
		int len = buffer.getInt();
		IType[] operands = new IType[len];
		for (int i = 0; i < len; i++) {
			operands[i] = buffer.unmarshalType();
		}
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalNaryTypeId(Operator.values()[op], operands, templateDefinition);
	}

	@Override
	public boolean isNoexcept() {
		return true;
	}
}
