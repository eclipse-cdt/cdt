/*******************************************************************************
 * Copyright (c) 2022 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class EvalFoldExpression extends CPPDependentEvaluation {
	private static final EvalFixed EVAL_TRUE = new EvalFixed(CPPBasicType.BOOLEAN, PRVALUE, IntegralValue.create(true));
	private static final EvalFixed EVAL_FALSE = new EvalFixed(CPPBasicType.BOOLEAN, PRVALUE,
			IntegralValue.create(false));
	/*private static final EvalFixed EVAL_VOID = new EvalFixed(CPPBasicType.VOID, PRVALUE,
			IntegralValue.create(0));*/

	private final int fOperator;
	private final boolean fIsComma;
	private final boolean fIsLeftFold;
	private ICPPEvaluation[] fPackEvals;
	private ICPPEvaluation fInitEval;

	private IType fType;

	private boolean fCheckedIsConstantExpression;
	private boolean fIsConstantExpression;
	private ICPPEvaluation fEvaluation;

	public EvalFoldExpression(int operator, boolean isComma, boolean isLeftFold, ICPPEvaluation[] packEvals,
			ICPPEvaluation initEval, IASTNode pointOfDefinition) {
		this(operator, isComma, isLeftFold, packEvals, initEval, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalFoldExpression(int operator, boolean isComma, boolean isLeftFold, ICPPEvaluation[] packEvals,
			ICPPEvaluation initEval, IBinding templateDefinition) {
		super(templateDefinition);
		fOperator = operator;
		fIsComma = isComma;
		fIsLeftFold = isLeftFold;
		fPackEvals = packEvals;
		fInitEval = initEval;
	}

	public int getOperator() {
		return fOperator;
	}

	public ICPPEvaluation getInitExpression() {
		return fInitEval;
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
		return containsDependentType(fPackEvals) || (fInitEval != null && fInitEval.isTypeDependent());
	}

	@Override
	public boolean isValueDependent() {
		return containsDependentValue(fPackEvals) || (fInitEval != null && fInitEval.isValueDependent());
	}

	@Override
	public boolean isConstantExpression() {
		if (!fCheckedIsConstantExpression) {
			fCheckedIsConstantExpression = true;
			fIsConstantExpression = computeIsConstantExpression();
		}
		return fIsConstantExpression;
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalFoldExpression)) {
			return false;
		}
		EvalFoldExpression o = (EvalFoldExpression) other;
		return fOperator == o.fOperator && fIsComma == o.fIsComma && fIsLeftFold == o.fIsLeftFold
				&& fPackEvals == o.fPackEvals
				&& (fInitEval == null ? o.fInitEval == null : fInitEval.isEquivalentTo(o.fInitEval));
	}

	private boolean computeIsConstantExpression() {
		return areAllConstantExpressions(fPackEvals) && (fInitEval == null || fInitEval.isConstantExpression());
	}

	@Override
	public IType getType() {
		if (fType == null) {
			if (isTypeDependent() || isValueDependent()) {
				fType = new TypeOfDependentExpression(this);
			} else {
				fType = computeEvaluation().getType();
			}
		}
		return fType;
	}

	@Override
	public IValue getValue() {
		ICPPEvaluation evaluation = computeEvaluation();
		return evaluation.getValue();
	}

	private ICPPEvaluation computeEvaluation() {
		if (fEvaluation == null) {
			if (fInitEval == null && fPackEvals.length == 0) {
				// unary fold with empty pack
				if (fIsComma) {
					// expression: void(), cannot evaluate
					fEvaluation = EvalFixed.INCOMPLETE;
				} else if (fOperator == IASTBinaryExpression.op_logicalAnd) {
					// expression: true
					fEvaluation = EVAL_TRUE;
				} else if (fOperator == IASTBinaryExpression.op_logicalOr) {
					// expression: false
					fEvaluation = EVAL_FALSE;
				} else {
					// error, cannot evaluate
					fEvaluation = EvalFixed.INCOMPLETE;
				}
			} else {
				// For right fold the expanded pack array is already reversed by instantiate()
				if (fIsComma) {
					int offset = 0;
					ICPPEvaluation[] evals;

					if (fInitEval != null) {
						evals = new ICPPEvaluation[fPackEvals.length + 1];
						if (fIsLeftFold) {
							evals[0] = fInitEval;
							offset = 1;
						} else {
							evals[fPackEvals.length] = fInitEval;
							offset = 0;
						}
					} else {
						evals = new ICPPEvaluation[fPackEvals.length];
						offset = 0;
					}

					for (ICPPEvaluation packElement : fPackEvals) {
						evals[offset++] = packElement;
					}

					fEvaluation = new EvalComma(evals, getTemplateDefinition());
				} else {

					ICPPEvaluation folded = fInitEval;

					for (ICPPEvaluation packElement : fPackEvals) {
						if (folded == null) {
							folded = packElement;
						} else {
							if (fIsLeftFold) {
								folded = new EvalBinary(fOperator, folded, packElement, getTemplateDefinition());
							} else {
								folded = new EvalBinary(fOperator, packElement, folded, getTemplateDefinition());
							}
						}
					}

					fEvaluation = folded;
				}
			}
		}

		return fEvaluation;
	}

	@Override
	public ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EVAL_FOLD_EXPRESSION;
		if (fIsComma) {
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		}
		if (fIsLeftFold) {
			firstBytes |= ITypeMarshalBuffer.FLAG2;
		}
		buffer.putShort((byte) firstBytes);
		buffer.putInt(fOperator);
		buffer.putInt(fPackEvals.length);
		for (ICPPEvaluation arg : fPackEvals) {
			buffer.marshalEvaluation(arg, includeValue);
		}
		buffer.marshalEvaluation(fInitEval, includeValue);
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		boolean isComma = (firstBytes & ITypeMarshalBuffer.FLAG1) != 0;
		boolean isLeftFold = (firstBytes & ITypeMarshalBuffer.FLAG2) != 0;
		int operator = buffer.getInt();
		int len = buffer.getInt();
		ICPPEvaluation[] packEvals = new ICPPEvaluation[len];
		for (int i = 0; i < packEvals.length; i++) {
			packEvals[i] = buffer.unmarshalEvaluation();
		}
		ICPPEvaluation initEval = buffer.unmarshalEvaluation();
		IBinding templateDefinition = buffer.unmarshalBinding();

		return new EvalFoldExpression(operator, isComma, isLeftFold, packEvals, initEval, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation[] packEvals = instantiateExpressions(fPackEvals, context, maxDepth);
		ICPPEvaluation initEval = fInitEval == null ? null : fInitEval.instantiate(context, maxDepth);

		if (packEvals == fPackEvals && initEval == fInitEval) {
			return this;
		}

		if (!fIsLeftFold) {
			ArrayUtil.reverse(packEvals);
		}

		return new EvalFoldExpression(fOperator, fIsComma, fIsLeftFold, packEvals, initEval, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		if (context.getStepsPerformed() >= ConstexprEvaluationContext.MAX_CONSTEXPR_EVALUATION_STEPS) {
			return EvalFixed.INCOMPLETE;
		}

		ICPPEvaluation[] packEvals = new ICPPEvaluation[fPackEvals.length];

		for (int i = 0; i < fPackEvals.length; i++) {
			ICPPEvaluation arg = fPackEvals[i].computeForFunctionCall(record, context.recordStep());
			packEvals[i] = arg;
		}

		ICPPEvaluation initEval = fInitEval == null ? null
				: fInitEval.computeForFunctionCall(record, context.recordStep());

		if (packEvals == fPackEvals && initEval == fInitEval) {
			return this;
		}

		return new EvalFoldExpression(fOperator, fIsComma, fIsLeftFold, packEvals, initEval, getTemplateDefinition());
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = CPPTemplates.PACK_SIZE_NOT_FOUND;
		for (ICPPEvaluation packElement : fPackEvals) {
			r = CPPTemplates.combinePackSize(r, packElement.determinePackSize(tpMap));
		}
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		for (ICPPEvaluation arg : fPackEvals) {
			if (arg.referencesTemplateParameter()) {
				return true;
			}
		}
		return fInitEval != null && fInitEval.referencesTemplateParameter();
	}

	@Override
	public boolean isNoexcept() {
		for (int i = 0; i < fPackEvals.length; i++) {
			ICPPEvaluation eval = fPackEvals[i];
			if (!eval.isNoexcept()) {
				return false;
			}
		}

		if (fInitEval != null) {
			return fInitEval.isNoexcept();
		}

		return true;
	}
}
