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

public class ExecDo implements ICPPExecution {
	private final ICPPEvaluation conditionEval;
	private final ICPPExecution bodyExec;

	public ExecDo(ICPPEvaluation conditionEval, ICPPExecution bodyExec) {
		this.conditionEval = conditionEval;
		this.bodyExec = bodyExec;
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		do {
			if (context.getStepsPerformed() >= ConstexprEvaluationContext.MAX_CONSTEXPR_EVALUATION_STEPS) {
				return ExecIncomplete.INSTANCE;
			}

			ICPPExecution result = EvalUtil.executeStatement(bodyExec, record, context);
			if (result instanceof ExecReturn) {
				return result;
			} else if (result instanceof ExecBreak) {
				break;
			}
		} while (EvalUtil.conditionExprSatisfied(conditionEval, record, context));
		return null;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation newConditionEval = conditionEval.instantiate(context, maxDepth);
		ICPPExecution newBodyExec = bodyExec.instantiate(context, maxDepth);
		if (newConditionEval == conditionEval && newBodyExec == bodyExec) {
			return this;
		}
		return new ExecDo(newConditionEval, newBodyExec);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_DO);
		buffer.marshalEvaluation(conditionEval, includeValue);
		buffer.marshalExecution(bodyExec, includeValue);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation conditionEval = buffer.unmarshalEvaluation();
		ICPPExecution bodyExec = buffer.unmarshalExecution();
		return new ExecDo(conditionEval, bodyExec);
	}
}
