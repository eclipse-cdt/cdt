/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class ExecIf implements ICPPExecution {
	private final ICPPEvaluation conditionExprEval;
	private final ExecSimpleDeclaration conditionDeclExec;
	private final ICPPExecution thenClauseExec;
	private final ICPPExecution elseClauseExec;

	public ExecIf(ICPPEvaluation conditionExprEval, ExecSimpleDeclaration conditionDeclExec, ICPPExecution thenClauseExec, ICPPExecution elseClauseExec) {
		this.conditionExprEval = conditionExprEval;
		this.conditionDeclExec = conditionDeclExec;
		this.thenClauseExec = thenClauseExec;
		this.elseClauseExec = elseClauseExec;
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		boolean conditionSatisfied = false;
		if (conditionExprEval != null) {
			conditionSatisfied = EvalUtil.conditionExprSatisfied(conditionExprEval, record, context);
		} else if (conditionDeclExec != null) {
			conditionSatisfied = EvalUtil.conditionDeclSatisfied(conditionDeclExec, record, context);
		}

		if (conditionSatisfied) {
			return EvalUtil.executeStatement(thenClauseExec, record, context);
		} else if (elseClauseExec != null) {
			return EvalUtil.executeStatement(elseClauseExec, record, context);
		}
		return null;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation newConditionExprEval = conditionExprEval != null ? conditionExprEval.instantiate(context, maxDepth) : null;
		ExecSimpleDeclaration newConditionDeclExec = conditionDeclExec != null ? (ExecSimpleDeclaration) conditionDeclExec.instantiate(context, maxDepth) : null;
		ICPPExecution newThenClauseExec = thenClauseExec.instantiate(context, maxDepth);
		ICPPExecution newElseClauseExec = elseClauseExec != null ? elseClauseExec.instantiate(context, maxDepth) : null;
		if (newConditionExprEval == conditionExprEval && newConditionDeclExec == conditionDeclExec && newThenClauseExec == thenClauseExec && newElseClauseExec == elseClauseExec) {
			return this;
		}
		return new ExecIf(newConditionExprEval, newConditionDeclExec, newThenClauseExec, newElseClauseExec);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_IF);
		buffer.marshalEvaluation(conditionExprEval, includeValue);
		buffer.marshalExecution(conditionDeclExec, includeValue);
		buffer.marshalExecution(thenClauseExec, includeValue);
		buffer.marshalExecution(elseClauseExec, includeValue);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation conditionExprEval = buffer.unmarshalEvaluation();
		ExecSimpleDeclaration conditionDeclExec = (ExecSimpleDeclaration) buffer.unmarshalExecution();
		ICPPExecution thenClauseExec = buffer.unmarshalExecution();
		ICPPExecution elseClauseExec = buffer.unmarshalExecution();
		return new ExecIf(conditionExprEval, conditionDeclExec, thenClauseExec, elseClauseExec);
	}
}
