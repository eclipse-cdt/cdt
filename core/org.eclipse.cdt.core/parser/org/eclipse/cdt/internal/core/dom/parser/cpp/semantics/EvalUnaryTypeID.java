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
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_alignof;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_nothrow_constructor;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_nothrow_copy;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_trivial_assign;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_trivial_constructor;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_trivial_copy;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_trivial_destructor;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_virtual_destructor;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_abstract;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_class;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_empty;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_enum;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_final;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_literal_type;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_pod;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_polymorphic;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_standard_layout;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_trivial;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_trivially_copyable;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_union;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_sizeof;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_sizeofParameterPack;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_typeid;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_typeof;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class EvalUnaryTypeID extends CPPDependentEvaluation {
	private final int fOperator;
	private final IType fOrigType;
	private IType fType;

	public EvalUnaryTypeID(int operator, IType type, IASTNode pointOfDefinition) {
		this(operator, type, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalUnaryTypeID(int operator, IType type, IBinding templateDefinition) {
		super(templateDefinition);
		fOperator = operator;
		fOrigType = type;
	}

	public int getOperator() {
		return fOperator;
	}

	public IType getArgument() {
		return fOrigType;
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
		if (fOperator == op_typeof)
			return CPPTemplates.isDependentType(fOrigType);
		return false;
	}

	@Override
	public boolean isValueDependent() {
		switch (fOperator) {
		case op_sizeofParameterPack:
			return true;
		case op_sizeof:
		case op_alignof:
		case op_has_nothrow_copy:
		case op_has_nothrow_constructor:
		case op_has_trivial_assign:
		case op_has_trivial_constructor:
		case op_has_trivial_copy:
		case op_has_trivial_destructor:
		case op_has_virtual_destructor:
		case op_is_abstract:
		case op_is_class:
		case op_is_empty:
		case op_is_enum:
		case op_is_final:
		case op_is_literal_type:
		case op_is_pod:
		case op_is_polymorphic:
		case op_is_standard_layout:
		case op_is_trivial:
		case op_is_trivially_copyable:
		case op_is_union:
			return CPPTemplates.isDependentType(fOrigType);

		case op_typeid:
		case op_typeof:
			return false;
		}
		return false;
	}

	@Override
	public boolean isConstantExpression() {
		return true;
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalUnaryTypeID)) {
			return false;
		}
		EvalUnaryTypeID o = (EvalUnaryTypeID) other;
		return fOperator == o.fOperator && fOrigType.isSameType(o.fOrigType);
	}

	@Override
	public IType getType() {
		if (fType == null)
			fType = computeType();
		return fType;
	}

	private IType computeType() {
		switch (fOperator) {
		case op_sizeof:
		case op_sizeofParameterPack:
		case op_alignof:
			IType result = (IType) CPPVisitor.get_SIZE_T().clone();
			if (SemanticUtil.getSimplifiedType(result) instanceof CPPBasicType) {
				result = CPPVisitor.associateTypeWithValue(result, getValue());
			}
			return result;
		case op_typeid:
			return CPPVisitor.get_type_info();
		case op_has_nothrow_copy:
		case op_has_nothrow_constructor:
		case op_has_trivial_assign:
		case op_has_trivial_constructor:
		case op_has_trivial_copy:
		case op_has_trivial_destructor:
		case op_has_virtual_destructor:
		case op_is_abstract:
		case op_is_class:
		case op_is_empty:
		case op_is_enum:
		case op_is_final:
		case op_is_literal_type:
		case op_is_pod:
		case op_is_polymorphic:
		case op_is_standard_layout:
		case op_is_trivial:
		case op_is_trivially_copyable:
		case op_is_union:
			return CPPBasicType.BOOLEAN;
		case op_typeof:
			if (isTypeDependent())
				return new TypeOfDependentExpression(this);
			return fOrigType;
		}
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	@Override
	public IValue getValue() {
		if (isValueDependent())
			return DependentValue.create(this);

		return ValueFactory.evaluateUnaryTypeIdExpression(fOperator, fOrigType);
	}

	@Override
	public ValueCategory getValueCategory() {
		return fOperator == op_typeid ? LVALUE : PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_UNARY_TYPE_ID);
		buffer.putByte((byte) fOperator);
		buffer.marshalType(fOrigType);
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int op = buffer.getByte();
		IType arg = buffer.unmarshalType();
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalUnaryTypeID(op, arg, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		if (fOperator == op_sizeofParameterPack) {
			return instantiateSizeofParameterPack(context);
		}
		return instantiateBySubstitution(context);
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		return this;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return CPPTemplates.determinePackSize(fOrigType, tpMap);
	}

	@Override
	public boolean referencesTemplateParameter() {
		return CPPTemplates.isDependentType(fOrigType);
	}

	private ICPPEvaluation instantiateBySubstitution(InstantiationContext context) {
		IType type = CPPTemplates.instantiateType(fOrigType, context);
		if (type == fOrigType)
			return this;
		return new EvalUnaryTypeID(fOperator, type, getTemplateDefinition());
	}

	private ICPPEvaluation instantiateSizeofParameterPack(InstantiationContext context) {
		if (fOrigType instanceof ICPPTemplateParameter) {
			ICPPTemplateParameter pack = (ICPPTemplateParameter) fOrigType;
			if (pack.isParameterPack()) {
				ICPPTemplateArgument[] args = context.getPackExpansion(pack);
				if (args == null) {
					return this;
				}
				int concreteArgCount = 0;
				boolean havePackExpansion = false;
				for (ICPPTemplateArgument arg : args) {
					if (arg.isPackExpansion()) {
						havePackExpansion = true;
					} else {
						concreteArgCount++;
					}
				}
				if (havePackExpansion) {
					// TODO(bug 530103):
					//   This will only handle correctly the case where there is a single argument
					//   which is a pack expansion, and no concrete arguments.
					//   To correctly handle cases with multiple pack expansions, or a mixture
					//   of concrete arguments and pack expansions, we need to do the following:
					//     - For each pack expansion, find the parameter pack P which it's
					//       expanding (if it's expanding multiple parameter packs, any one
					//       should be sufficient), and construct an EvalUnaryTypeId representing
					//       sizeof...(P).
					//     - Construct an EvalBinary tree representing the sum of |concreteArgCount|
					//       and the EvalUnaryTypeIds from the previous step.
					return instantiateBySubstitution(context);
				} else {
					return new EvalFixed(getType(), getValueCategory(), IntegralValue.create(concreteArgCount));
				}
			}
		}
		return EvalFixed.INCOMPLETE;
	}

	@Override
	public boolean isNoexcept() {
		return true;
	}
}
