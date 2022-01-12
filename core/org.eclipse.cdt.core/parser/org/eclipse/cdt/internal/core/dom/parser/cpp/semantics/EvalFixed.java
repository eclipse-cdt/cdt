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

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.XVALUE;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of an expression.
 */
public final class EvalFixed extends CPPEvaluation {
	public static final ICPPEvaluation INCOMPLETE = new EvalFixed(ProblemType.UNKNOWN_FOR_EXPRESSION, PRVALUE,
			IntegralValue.ERROR);

	private final IType fType;
	private final IValue fValue;
	private final ValueCategory fValueCategory;
	private boolean fIsTypeDependent;
	private boolean fCheckedIsTypeDependent;
	private boolean fIsValueDependent;
	private boolean fCheckedIsValueDependent;
	private boolean fCheckedIsConstantExpression;
	private boolean fIsConstantExpression;

	public EvalFixed(IType type, ValueCategory cat, IValue value) {
		// Avoid nesting EvalFixed's as nesting causes the signature to be different.
		if (value.getEvaluation() instanceof EvalFixed) {
			EvalFixed inner = (EvalFixed) value.getEvaluation();
			type = inner.fType;
			cat = inner.fValueCategory;
			value = inner.fValue;
		}

		if (type instanceof CPPBasicType) {
			Number num = value.numberValue();
			if (num != null) {
				CPPBasicType t = (CPPBasicType) type.clone();
				t.setAssociatedNumericalValue(num.longValue());
				type = t;
			}
		}
		fType = type;
		fValueCategory = cat;
		fValue = value;
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
		if (!fCheckedIsTypeDependent) {
			fCheckedIsTypeDependent = true;
			fIsTypeDependent = CPPTemplates.isDependentType(fType);
		}
		return fIsTypeDependent;
	}

	@Override
	public boolean isValueDependent() {
		if (!fCheckedIsValueDependent) {
			fCheckedIsValueDependent = true;
			fIsValueDependent = IntegralValue.isDependentValue(fValue);
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
		return (fType instanceof ICPPClassType && TypeTraits.isEmpty(fType)) || isConstexprValue(fValue);
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalFixed)) {
			return false;
		}
		EvalFixed o = (EvalFixed) other;
		return fType.isSameType(o.fType) && fValue.isEquivalentTo(o.fValue);
	}

	@Override
	public IType getType() {
		return fType;
	}

	@Override
	public IValue getValue() {
		return fValue;
	}

	@Override
	public ValueCategory getValueCategory() {
		return fValueCategory;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		includeValue = includeValue && fValue != IntegralValue.UNKNOWN;
		short firstBytes = ITypeMarshalBuffer.EVAL_FIXED;
		if (includeValue)
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		switch (fValueCategory) {
		case PRVALUE:
			firstBytes |= ITypeMarshalBuffer.FLAG2;
			break;
		case LVALUE:
			firstBytes |= ITypeMarshalBuffer.FLAG3;
			break;
		default:
			break;
		}

		buffer.putShort(firstBytes);
		buffer.marshalType(fType);
		if (includeValue) {
			buffer.marshalValue(fValue);
		}
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		final boolean readValue = (firstBytes & ITypeMarshalBuffer.FLAG1) != 0;
		IValue value;
		ValueCategory cat;
		switch (firstBytes & (ITypeMarshalBuffer.FLAG2 | ITypeMarshalBuffer.FLAG3)) {
		case ITypeMarshalBuffer.FLAG2:
			cat = PRVALUE;
			break;
		case ITypeMarshalBuffer.FLAG3:
			cat = LVALUE;
			break;
		default:
			cat = XVALUE;
			break;
		}

		IType type = buffer.unmarshalType();
		value = readValue ? buffer.unmarshalValue() : IntegralValue.UNKNOWN;
		return new EvalFixed(type, cat, value);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		IType type = CPPTemplates.instantiateType(fType, context);
		IValue value = CPPTemplates.instantiateValue(fValue, context, maxDepth);
		if (type == fType && value == fValue)
			return this;
		// If an error occurred while instantiating the value (such as a substitution failure),
		// propagate that error.
		if (value == IntegralValue.ERROR)
			return EvalFixed.INCOMPLETE;
		// Resolve the parameter pack type to the underlying type if the instantiated value is not dependent.
		if (type instanceof ICPPParameterPackType && value.numberValue() != null)
			type = ((ICPPParameterPackType) type).getType();
		return new EvalFixed(type, fValueCategory, value);
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPEvaluation eval = fValue.getEvaluation();
		if (eval == null) {
			return this;
		}
		eval = eval.computeForFunctionCall(record, context.recordStep());
		if (eval == fValue.getEvaluation()) {
			return this;
		}
		EvalFixed evalFixed = new EvalFixed(fType, fValueCategory, DependentValue.create(eval));
		return evalFixed;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return CPPTemplates.determinePackSize(fValue, tpMap);
	}

	@Override
	public boolean referencesTemplateParameter() {
		return false;
	}

	@Override
	public String toString() {
		return fType.toString() + ": " + fValue.toString(); //$NON-NLS-1$
	}

	@Override
	public boolean isNoexcept() {
		return true;
	}
}