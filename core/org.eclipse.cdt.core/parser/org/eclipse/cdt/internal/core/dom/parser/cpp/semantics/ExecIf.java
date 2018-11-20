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

public class ExecIf implements ICPPExecution {
	private final boolean isConstexpr;
	private final ICPPExecution initStmtExec;
	private final ICPPEvaluation conditionExprEval;
	private final ExecSimpleDeclaration conditionDeclExec;
	private final ICPPExecution thenClauseExec;
	private final ICPPExecution elseClauseExec;

	public ExecIf(boolean isConstexpr, ICPPExecution initStmtExec, ICPPEvaluation conditionExprEval,
			ExecSimpleDeclaration conditionDeclExec, ICPPExecution thenClauseExec, ICPPExecution elseClauseExec) {
		this.isConstexpr = isConstexpr;
		this.initStmtExec = initStmtExec;
		this.conditionExprEval = conditionExprEval;
		this.conditionDeclExec = conditionDeclExec;
		this.thenClauseExec = thenClauseExec;
		this.elseClauseExec = elseClauseExec;
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
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		EvalUtil.executeStatement(initStmtExec, record, context);
		if (conditionSatisfied(record, context)) {
			return EvalUtil.executeStatement(thenClauseExec, record, context);
		} else if (elseClauseExec != null) {
			return EvalUtil.executeStatement(elseClauseExec, record, context);
		}
		return null;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPExecution newInitStmtExec = initStmtExec != null ? initStmtExec.instantiate(context, maxDepth) : null;
		ICPPEvaluation newConditionExprEval = conditionExprEval != null
				? conditionExprEval.instantiate(context, maxDepth)
				: null;
		ExecSimpleDeclaration newConditionDeclExec = conditionDeclExec != null
				? (ExecSimpleDeclaration) conditionDeclExec.instantiate(context, maxDepth)
				: null;

		ICPPExecution newThenClauseExec = null;
		ICPPExecution newElseClauseExec = null;

		if (isConstexpr && newConditionExprEval != null && newConditionExprEval.getValue().numberValue() != null) {
			if (newConditionExprEval.getValue().numberValue().intValue() != 0) {
				/*
				 * We can't just "return newThenClauseExec" here, because the condition
				 * might have side effects so it needs to be preserved in the instantiated
				 * execution even if one of its branch has become null
				 */
				newThenClauseExec = thenClauseExec.instantiate(context, maxDepth);
			} else {
				newElseClauseExec = elseClauseExec != null ? elseClauseExec.instantiate(context, maxDepth) : null;
			}
		} else {
			newThenClauseExec = thenClauseExec.instantiate(context, maxDepth);
			newElseClauseExec = elseClauseExec != null ? elseClauseExec.instantiate(context, maxDepth) : null;
		}

		if (newInitStmtExec == initStmtExec && newConditionExprEval == conditionExprEval
				&& newConditionDeclExec == conditionDeclExec && newThenClauseExec == thenClauseExec
				&& newElseClauseExec == elseClauseExec) {
			return this;
		}
		return new ExecIf(isConstexpr, newInitStmtExec, newConditionExprEval, newConditionDeclExec, newThenClauseExec,
				newElseClauseExec);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EXEC_IF;
		if (isConstexpr) {
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		}
		buffer.putShort(firstBytes);
		buffer.marshalExecution(initStmtExec, includeValue);
		buffer.marshalEvaluation(conditionExprEval, includeValue);
		buffer.marshalExecution(conditionDeclExec, includeValue);
		buffer.marshalExecution(thenClauseExec, includeValue);
		buffer.marshalExecution(elseClauseExec, includeValue);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		boolean isConstexpr = (firstBytes & ITypeMarshalBuffer.FLAG1) != 0;
		ICPPExecution initStmtExec = buffer.unmarshalExecution();
		ICPPEvaluation conditionExprEval = buffer.unmarshalEvaluation();
		ExecSimpleDeclaration conditionDeclExec = (ExecSimpleDeclaration) buffer.unmarshalExecution();
		ICPPExecution thenClauseExec = buffer.unmarshalExecution();
		ICPPExecution elseClauseExec = buffer.unmarshalExecution();
		return new ExecIf(isConstexpr, initStmtExec, conditionExprEval, conditionDeclExec, thenClauseExec,
				elseClauseExec);
	}
}
