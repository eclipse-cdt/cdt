/*******************************************************************************
 * Copyright (c) 2012, 2015 Wind River Systems, Inc. and others.
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
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.XVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArithmeticConversion;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.Context;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.UDCMode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.Rank;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of an expression.
 */
public class EvalConditional extends CPPDependentEvaluation {
	private final ICPPEvaluation fCondition, fPositive, fNegative;
	private final boolean fPositiveThrows, fNegativeThrows;

	private ValueCategory fValueCategory;
	private IType fType;
	private ICPPFunction fOverload;
	private boolean fCheckedIsConstantExpression;
	private boolean fIsConstantExpression;

	public EvalConditional(ICPPEvaluation condition, ICPPEvaluation positive, ICPPEvaluation negative,
			boolean positiveThrows, boolean negativeThrows, IASTNode pointOfDefinition) {
		this(condition, positive, negative, positiveThrows, negativeThrows, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalConditional(ICPPEvaluation condition, ICPPEvaluation positive, ICPPEvaluation negative,
			boolean positiveThrows, boolean negativeThrows, IBinding templateDefinition) {
		super(templateDefinition);
		// Gnu-extension: Empty positive expression is replaced by condition.
		fCondition = condition;
		fPositive = positive;
		fNegative = negative;
		fPositiveThrows = positiveThrows;
		fNegativeThrows = negativeThrows;
	}

	public ICPPEvaluation getCondition() {
		return fCondition;
	}

	public ICPPEvaluation getPositive() {
		return fPositive;
	}

	public ICPPEvaluation getNegative() {
		return fNegative;
	}

	public boolean isPositiveThrows() {
		return fPositiveThrows;
	}

	public boolean isNegativeThrows() {
		return fNegativeThrows;
	}

	@Override
	public boolean isInitializerList() {
		return false;
	}

	@Override
	public boolean isFunctionSet() {
		return false;
	}

	public ICPPFunction getOverload() {
		evaluate();
		return fOverload;
	}

	@Override
	public IType getType() {
		evaluate();
		return fType;
	}

	@Override
	public IValue getValue() {
		IValue condValue = fCondition.getValue();
		if (condValue == IntegralValue.UNKNOWN)
			return IntegralValue.UNKNOWN;
		Number cond = condValue.numberValue();
		if (cond != null) {
			if (cond.longValue() != 0) {
				return fPositive == null ? condValue : fPositive.getValue();
			} else {
				return fNegative.getValue();
			}
		}
		return DependentValue.create(this);
	}

	@Override
	public ValueCategory getValueCategory() {
		evaluate();
		return fValueCategory;
	}

	@Override
	public boolean isTypeDependent() {
		final ICPPEvaluation positive = fPositive == null ? fCondition : fPositive;
		return positive.isTypeDependent() || fNegative.isTypeDependent();
	}

	@Override
	public boolean isValueDependent() {
		return fCondition.isValueDependent() || (fPositive != null && fPositive.isValueDependent())
				|| fNegative.isValueDependent();
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
		return fCondition.isConstantExpression() && (fPositive == null || fPositive.isConstantExpression())
				&& fNegative.isConstantExpression();
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalConditional)) {
			return false;
		}
		EvalConditional o = (EvalConditional) other;
		return fCondition.isEquivalentTo(o.fCondition) && areEquivalentOrNull(fPositive, o.fPositive)
				&& fNegative.isEquivalentTo(o.fNegative);
	}

	private void evaluate() {
		if (fValueCategory != null)
			return;

		fValueCategory = PRVALUE;

		final ICPPEvaluation positive = fPositive == null ? fCondition : fPositive;

		IType t2 = positive.getType();
		IType t3 = fNegative.getType();

		final IType uqt2 = getNestedType(t2, TDEF | REF | CVTYPE);
		final IType uqt3 = getNestedType(t3, TDEF | REF | CVTYPE);
		if (uqt2 instanceof ISemanticProblem || uqt2 instanceof ICPPUnknownType) {
			fType = uqt2;
			return;
		}
		if (uqt3 instanceof ISemanticProblem || uqt3 instanceof ICPPUnknownType) {
			fType = uqt3;
			return;
		}

		final boolean void2 = isVoidType(uqt2);
		final boolean void3 = isVoidType(uqt3);

		// Void types: Either both are void or one is a throw expression.
		if (void2 || void3) {
			if (fPositiveThrows) {
				fType = Conversions.lvalue_to_rvalue(t3, false);
			} else if (fNegativeThrows) {
				fType = Conversions.lvalue_to_rvalue(t2, false);
			} else if (void2 && void3) {
				fType = uqt2;
			} else {
				fType = ProblemType.UNKNOWN_FOR_EXPRESSION;
			}
			return;
		}

		final ValueCategory vcat2 = positive.getValueCategory();
		final ValueCategory vcat3 = fNegative.getValueCategory();

		// Same type
		if (t2.isSameType(t3)) {
			if (vcat2 == vcat3) {
				fType = t2;
				fValueCategory = vcat2;
			} else {
				fType = prvalueType(t2);
				fValueCategory = PRVALUE;
			}
			return;
		}

		final boolean isClassType2 = uqt2 instanceof ICPPClassType;
		final boolean isClassType3 = uqt3 instanceof ICPPClassType;

		// Different types with at least one class type
		if (isClassType2 || isClassType3) {
			final Cost cost2 = convertToMatch(t2, vcat2, uqt2, t3, vcat3, uqt3); // sets fType and fValueCategory
			final Cost cost3 = convertToMatch(t3, vcat3, uqt3, t2, vcat2, uqt2); // sets fType and fValueCategory
			if (cost2.converts() || cost3.converts()) {
				if (cost2.converts()) {
					if (cost3.converts() || cost2.isAmbiguousUDC()) {
						fType = ProblemType.UNKNOWN_FOR_EXPRESSION;
					}
				} else if (cost3.isAmbiguousUDC()) {
					fType = ProblemType.UNKNOWN_FOR_EXPRESSION;
				}
				return;
			}
		} else if (vcat2 == vcat3 && vcat2.isGLValue() && uqt2.isSameType(uqt3)) {
			// Two lvalues or two xvalues with same type up to qualification.
			final CVQualifier cv2 = SemanticUtil.getCVQualifier(t2);
			final CVQualifier cv3 = SemanticUtil.getCVQualifier(t3);
			if (cv2.isAtLeastAsQualifiedAs(cv3)) {
				fType = t2;
				fValueCategory = vcat2;
			} else if (cv3.isAtLeastAsQualifiedAs(cv2)) {
				fType = t3;
				fValueCategory = vcat3;
			} else {
				fType = ProblemType.UNKNOWN_FOR_EXPRESSION;
			}
			return;
		}

		// 5.16-5: At least one class type but no conversion
		if (isClassType2 || isClassType3) {
			fOverload = CPPSemantics.findOverloadedConditionalOperator(getTemplateDefinitionScope(), positive,
					fNegative);
			if (fOverload != null) {
				fType = ExpressionTypes.typeFromFunctionCall(fOverload);
			} else {
				fType = ProblemType.UNKNOWN_FOR_EXPRESSION;
			}
			return;
		}

		// 5.16-6
		t2 = Conversions.lvalue_to_rvalue(t2, false);
		t3 = Conversions.lvalue_to_rvalue(t3, false);
		if (t2.isSameType(t3)) {
			fType = t2;
		} else {
			fType = CPPArithmeticConversion.convertCppOperandTypes(IASTBinaryExpression.op_plus, t2, t3);
			if (fType == null) {
				fType = Conversions.compositePointerType(t2, t3);
				if (fType == null) {
					fType = ProblemType.UNKNOWN_FOR_EXPRESSION;
				}
			}
		}
	}

	private Cost convertToMatch(IType t1, ValueCategory vcat1, IType uqt1, IType t2, ValueCategory vcat2, IType uqt2) {
		// E2 is an lvalue or E2 is an xvalue
		try {
			if (vcat2.isGLValue()) {
				IType target = new CPPReferenceType(t2, vcat2 == XVALUE);
				Cost c = Conversions.checkImplicitConversionSequence(target, t1, vcat1, UDCMode.ALLOWED,
						Context.REQUIRE_DIRECT_BINDING);
				if (c.converts()) {
					fType = t2;
					fValueCategory = vcat2;
					return c;
				}
			}
			// Both are class types and one derives from the other
			if (uqt1 instanceof ICPPClassType && uqt2 instanceof ICPPClassType) {
				int dist = SemanticUtil.calculateInheritanceDepth(uqt1, uqt2);
				if (dist >= 0) {
					CVQualifier cv1 = SemanticUtil.getCVQualifier(t1);
					CVQualifier cv2 = SemanticUtil.getCVQualifier(t2);
					if (cv2.isAtLeastAsQualifiedAs(cv1)) {
						fType = t2;
						fValueCategory = PRVALUE;
						return new Cost(t1, t2, Rank.IDENTITY);
					}
					return Cost.NO_CONVERSION;
				}
				if (SemanticUtil.calculateInheritanceDepth(uqt2, uqt1) >= 0)
					return Cost.NO_CONVERSION;
			}
			// Unrelated class types or just one class:
			if (vcat2 != PRVALUE) {
				t2 = Conversions.lvalue_to_rvalue(t2, false);
			}
			Cost c = Conversions.checkImplicitConversionSequence(t2, t1, vcat1, UDCMode.ALLOWED, Context.ORDINARY);
			if (c.converts()) {
				fType = t2;
				fValueCategory = PRVALUE;
				return c;
			}
		} catch (DOMException e) {
		}
		return Cost.NO_CONVERSION;
	}

	private boolean isVoidType(IType t) {
		return t instanceof ICPPBasicType && ((ICPPBasicType) t).getKind() == ICPPBasicType.Kind.eVoid;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EVAL_CONDITIONAL;
		if (fPositiveThrows)
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		if (fNegativeThrows)
			firstBytes |= ITypeMarshalBuffer.FLAG2;

		buffer.putShort(firstBytes);
		buffer.marshalEvaluation(fCondition, includeValue);
		buffer.marshalEvaluation(fPositive, includeValue);
		buffer.marshalEvaluation(fNegative, includeValue);
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		boolean pth = (firstBytes & ITypeMarshalBuffer.FLAG1) != 0;
		boolean nth = (firstBytes & ITypeMarshalBuffer.FLAG2) != 0;
		ICPPEvaluation cond = buffer.unmarshalEvaluation();
		ICPPEvaluation pos = buffer.unmarshalEvaluation();
		ICPPEvaluation neg = buffer.unmarshalEvaluation();
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalConditional(cond, pos, neg, pth, nth, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation condition = fCondition.instantiate(context, maxDepth);
		ICPPEvaluation positive = fPositive == null ? null : fPositive.instantiate(context, maxDepth);
		ICPPEvaluation negative = fNegative.instantiate(context, maxDepth);
		if (condition == fCondition && positive == fPositive && negative == fNegative)
			return this;
		return new EvalConditional(condition, positive, negative, fPositiveThrows, fNegativeThrows,
				getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPEvaluation condition = fCondition.computeForFunctionCall(record, context.recordStep());
		// If the condition can be evaluated, fold the conditional into
		// just the branch that is taken. This avoids infinite recursion
		// when computing a recursive constexpr function where the base
		// case of the recursion is one of the branches of the conditional.
		Number conditionValue = condition.getValue().numberValue();
		if (conditionValue != null) {
			if (conditionValue.longValue() != 0) {
				return fPositive == null ? null : fPositive.computeForFunctionCall(record, context.recordStep());
			} else {
				return fNegative.computeForFunctionCall(record, context.recordStep());
			}
		}
		ICPPEvaluation positive = fPositive == null ? null
				: fPositive.computeForFunctionCall(record, context.recordStep());
		ICPPEvaluation negative = fNegative.computeForFunctionCall(record, context.recordStep());
		if (condition == fCondition && positive == fPositive && negative == fNegative) {
			return this;
		}

		EvalConditional evalConditional = new EvalConditional(condition, positive, negative, fPositiveThrows,
				fNegativeThrows, getTemplateDefinition());
		return evalConditional;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = fCondition.determinePackSize(tpMap);
		r = CPPTemplates.combinePackSize(r, fNegative.determinePackSize(tpMap));
		if (fPositive != null)
			r = CPPTemplates.combinePackSize(r, fPositive.determinePackSize(tpMap));
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fCondition.referencesTemplateParameter()
				|| (fPositive != null && fPositive.referencesTemplateParameter())
				|| fNegative.referencesTemplateParameter();
	}

	@Override
	public boolean isNoexcept() {
		return fCondition.isNoexcept() && fPositive.isNoexcept() && fNegative.isNoexcept();
	}
}
