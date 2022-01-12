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

import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class ExecWhile implements ICPPExecution {
	private final ICPPEvaluation conditionExprEval;
	private final ExecSimpleDeclaration conditionDeclExec;
	private final ICPPExecution bodyExec;

	public ExecWhile(ICPPEvaluation conditionExprEval, ExecSimpleDeclaration conditionDeclExec,
			ICPPExecution bodyExec) {
		this.conditionExprEval = conditionExprEval;
		this.conditionDeclExec = conditionDeclExec;
		this.bodyExec = bodyExec;
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		while (conditionSatisfied(record, context)) {
			if (context.getStepsPerformed() >= ConstexprEvaluationContext.MAX_CONSTEXPR_EVALUATION_STEPS) {
				return ExecIncomplete.INSTANCE;
			}

			ICPPExecution result = EvalUtil.executeStatement(bodyExec, record, context);
			if (result instanceof ExecReturn) {
				return result;
			} else if (result instanceof ExecBreak) {
				break;
			} else if (result instanceof ExecContinue) {
				continue;
			}
		}
		return null;
	}

	private boolean conditionSatisfied(ActivationRecord record, ConstexprEvaluationContext context) {
		if (conditionExprEval != null) {
			return EvalUtil.conditionExprSatisfied(conditionExprEval, record, context);
		} else if (conditionDeclExec != null) {
			return EvalUtil.conditionDeclSatisfied(conditionDeclExec, record, context);
		}
		return false;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation newConditionExprEval = conditionExprEval != null
				? conditionExprEval.instantiate(context, maxDepth)
				: null;
		ExecSimpleDeclaration newConditionDeclExec = conditionDeclExec != null
				? (ExecSimpleDeclaration) conditionDeclExec.instantiate(context, maxDepth)
				: null;
		ICPPExecution newBodyExec = bodyExec.instantiate(context, maxDepth);
		if (newConditionExprEval == conditionExprEval && newConditionDeclExec == conditionDeclExec
				&& newBodyExec == bodyExec) {
			return this;
		}
		return new ExecWhile(newConditionExprEval, newConditionDeclExec, newBodyExec);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_WHILE);
		buffer.marshalEvaluation(conditionExprEval, includeValue);
		buffer.marshalExecution(conditionDeclExec, includeValue);
		buffer.marshalExecution(bodyExec, includeValue);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation conditionExprEval = buffer.unmarshalEvaluation();
		ExecSimpleDeclaration conditionDeclExec = (ExecSimpleDeclaration) buffer.unmarshalExecution();
		ICPPExecution bodyExec = buffer.unmarshalExecution();
		return new ExecWhile(conditionExprEval, conditionDeclExec, bodyExec);
	}
}
