/*******************************************************************************
 * Copyright (c) 2017 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
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
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Evaluate the return expression depending on a constexpr if condition
 */
public class EvalConstexprIfReturn extends CPPDependentEvaluation {
	private final ICPPEvaluation fCondition;
	private ICPPEvaluation fThenEval;
	private ICPPEvaluation fElseEval;
	private ICPPEvaluation fFinalEval;
	private boolean fIsForDecltype;

	private int fEvalDepth = IntegralValue.MAX_RECURSION_DEPTH;
	private ValueCategory fValueCategory;
	private IType fType;

	public EvalConstexprIfReturn(ICPPEvaluation condition, ICPPEvaluation thenEval, ICPPEvaluation elseEval, ICPPEvaluation finalEval, IASTNode pointOfDefinition, boolean isForDecltype) {
		this(condition, thenEval, elseEval, finalEval, findEnclosingTemplate(pointOfDefinition), isForDecltype);
	}

	public EvalConstexprIfReturn(ICPPEvaluation condition, ICPPEvaluation thenEval, ICPPEvaluation elseEval,  ICPPEvaluation finalEval, IBinding templateDefinition, boolean isForDecltype) {
		super(templateDefinition);
		fCondition = condition;
		fThenEval = thenEval;
		fElseEval = elseEval;
		setFinalEval(finalEval);
		fIsForDecltype = isForDecltype;
	}

	public ICPPEvaluation getCondition() {
		return fCondition;
	}

	public ICPPEvaluation getThenEval() {
		return fThenEval;
	}

	public ICPPEvaluation getElseEval() {
		return fElseEval;
	}

	public ICPPEvaluation getFinalEval() {
		return fFinalEval;
	}

	public boolean isForDecltype() {
		return fIsForDecltype;
	}

	public void setThenEval(ICPPEvaluation thenEval) {
		fThenEval = thenEval;
	}

	public void setElseEval(ICPPEvaluation elseEval) {
		fElseEval = elseEval;
	}

	public void setFinalEval(ICPPEvaluation finalEval) {
		if (fFinalEval == null) {
			fFinalEval = finalEval;
		} else if (fFinalEval instanceof EvalConstexprIfReturn) {
			replaceFinalEval((EvalConstexprIfReturn) fFinalEval, finalEval);
		} else if (finalEval instanceof EvalConstexprIfReturn) {
			replaceFinalEval((EvalConstexprIfReturn) finalEval, fFinalEval);			
		} else if (finalEval != null && !fFinalEval.getType().isSameType(finalEval.getType())) {
			cannotDeduceType();
		}
	}

	private void replaceFinalEval(EvalConstexprIfReturn oldFinalEval, ICPPEvaluation currentFinalEval) {
		fFinalEval = new EvalConstexprIfReturn(
				oldFinalEval.fCondition,
				oldFinalEval.fThenEval,
				oldFinalEval.fElseEval,
				currentFinalEval,
				oldFinalEval.getTemplateDefinition(),
				oldFinalEval.fIsForDecltype
			);
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
		evaluate();
		return fType;
	}

	@Override
	public IValue getValue() {
		if (fCondition == null) {
			return IntegralValue.UNKNOWN;
		}
		IValue condValue = fCondition.getValue();
		if (condValue == IntegralValue.UNKNOWN) {			
			return IntegralValue.UNKNOWN;
		}
		if (condValue.numberValue() != null) {
			ICPPEvaluation evalToConsider = (condValue.numberValue().longValue() != 0) ? fThenEval : fElseEval;
			if (evalToConsider == null) {
				return (fFinalEval == null) ? IntegralValue.UNKNOWN : fFinalEval.getValue();
			}
			return evalToConsider.getValue();
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
		return (fThenEval != null && fThenEval.isTypeDependent()) || (fElseEval != null && fElseEval.isTypeDependent());
	}

	@Override
	public boolean isValueDependent() {
		return (fCondition != null && fCondition.isValueDependent())
				|| (fThenEval != null && fThenEval.isValueDependent())
				|| (fElseEval != null && fElseEval.isValueDependent());
	}

	@Override
	public boolean isConstantExpression() {
		return fCondition.isConstantExpression();
	}

	private void evaluate() {
    	if (fValueCategory != null && fType != null) {
    		return;
    	}
		if (fCondition != null && fCondition.getValue().numberValue() != null) {
			ICPPEvaluation evalToConsider = (fCondition.getValue().numberValue().longValue() != 0) ? fThenEval : fElseEval;
			if (evalToConsider == null || evalToConsider.getType() == CPPSemantics.VOID_TYPE) {
				if (fFinalEval == null) {
					fValueCategory = ValueCategory.PRVALUE;
					fType = CPPSemantics.VOID_TYPE;
				} else {
					updateType(fFinalEval);
				}
			} else if (fFinalEval == null || fFinalEval.getType() == CPPSemantics.VOID_TYPE || isSameTypeNoCV(fFinalEval.getType(), evalToConsider.getType())) {
				updateType(evalToConsider);
			} else if (fFinalEval != null && evalToConsider.getType() == CPPSemantics.VOID_TYPE) {
				updateType(fFinalEval);
			} else {
				cannotDeduceType();
			}
		}
	}

	private void updateType(ICPPEvaluation evaluation) {
		if (evaluation == null || evaluation.getType() instanceof ProblemType) {
			cannotDeduceType();
		} else {
			fValueCategory = evaluation.getValueCategory();
			fType = evaluation.getType();
		}
	}

	private boolean isSameTypeNoCV(IType typeA, IType typeB) {
		int options = SemanticUtil.PTR | SemanticUtil.ARRAY | SemanticUtil.CVTYPE;
		return SemanticUtil.getNestedType(typeA, options).isSameType(SemanticUtil.getNestedType(typeB, options));
	}

	private void cannotDeduceType() {
		fValueCategory = ValueCategory.PRVALUE;
		fType = fIsForDecltype ? ProblemType.CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE : ProblemType.CANNOT_DEDUCE_AUTO_TYPE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EVAL_CONSTEXPR_IF_RETURN_TYPE;
		buffer.putShort(firstBytes);
		buffer.marshalEvaluation(fCondition, includeValue);
		buffer.marshalEvaluation(fThenEval, includeValue);
		buffer.marshalEvaluation(fElseEval, includeValue);
		buffer.marshalEvaluation(fFinalEval, includeValue);
		buffer.putByte((byte) (fIsForDecltype ? 1 : 0));
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation condition = buffer.unmarshalEvaluation();
		ICPPEvaluation thenEval = buffer.unmarshalEvaluation();
		ICPPEvaluation elseEval = buffer.unmarshalEvaluation();
		ICPPEvaluation finalEval = buffer.unmarshalEvaluation();
		IBinding templateDefinition = buffer.unmarshalBinding();
		boolean isForDecltype = buffer.getByte() == 1;
		return new EvalConstexprIfReturn(condition, thenEval, elseEval, finalEval, templateDefinition, isForDecltype);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		// We need our own depth counting since maxDepth will again be reset
		// in outside of this class which leads to a StackOverflowException
		if (fEvalDepth-- < 0) {
			return EvalFixed.INCOMPLETE;
		}
		ICPPEvaluation condition = fCondition.instantiate(context, maxDepth);
		// If the condition can be evaluated we only instaniate the
		// branch which is taken. This avoids inifnite recursion.
		Number conditionValue = condition.getValue().numberValue();
		ICPPEvaluation thenEval = null;
		ICPPEvaluation elseEval = null;
		if (conditionValue != null) {
			if (conditionValue.longValue() != 0) {
				thenEval = (fThenEval == null) ? null : fThenEval.instantiate(context, maxDepth);
			} else {
				elseEval = (fElseEval == null) ? null : fElseEval.instantiate(context, maxDepth);
			}
		} else {
			thenEval = (fThenEval == null) ? null : fThenEval.instantiate(context, maxDepth);
			elseEval = (fElseEval == null) ? null : fElseEval.instantiate(context, maxDepth);
		}
		ICPPEvaluation finalEval = (fFinalEval == null) ? null : fFinalEval.instantiate(context, maxDepth);
		if (condition == fCondition && thenEval == fThenEval && elseEval == fElseEval && finalEval == fFinalEval) {
			return this;
		}
		return new EvalConstexprIfReturn(condition, thenEval, elseEval, finalEval, getTemplateDefinition(), fIsForDecltype);
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
			ICPPEvaluation evalToConsider = (conditionValue.longValue() != 0) ? fThenEval : fElseEval;
			if (evalToConsider == null) {
				return (fFinalEval != null) ? fFinalEval.computeForFunctionCall(record, context) : EvalFixed.INCOMPLETE;
			}
			return evalToConsider.computeForFunctionCall(record, context.recordStep());
		}

		ICPPEvaluation thenEval = (fThenEval == null) ? null : fThenEval.computeForFunctionCall(record, context.recordStep());
		ICPPEvaluation elseEval = (fElseEval == null) ? null : fElseEval.computeForFunctionCall(record, context.recordStep());
		ICPPEvaluation finalEval = (fFinalEval == null) ? null : fElseEval.computeForFunctionCall(record, context.recordStep());
		if (condition == fCondition && thenEval == fThenEval && elseEval == fElseEval && finalEval == fFinalEval) {
			return this;
		}

		return new EvalConstexprIfReturn(condition, thenEval, elseEval, finalEval, getTemplateDefinition(), fIsForDecltype);
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return fCondition.determinePackSize(tpMap);
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fCondition.referencesTemplateParameter()
				|| fThenEval.referencesTemplateParameter()
				|| fElseEval.referencesTemplateParameter();
	}
}
