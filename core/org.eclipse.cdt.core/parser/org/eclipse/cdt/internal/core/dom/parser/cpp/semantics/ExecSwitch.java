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

import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class ExecSwitch implements ICPPExecution {
	private final ICPPExecution initStmtExec;
	private final ICPPEvaluation controllerExprEval;
	private final ExecSimpleDeclaration controllerDeclExec;
	private final ICPPExecution[] bodyStmtExecutions;

	public ExecSwitch(ICPPExecution initStmtExec, ICPPEvaluation controllerExprEval,
			ExecSimpleDeclaration controllerDeclExec, ICPPExecution[] bodyStmtExecutions) {
		this.initStmtExec = initStmtExec;
		this.controllerExprEval = controllerExprEval;
		this.controllerDeclExec = controllerDeclExec;
		this.bodyStmtExecutions = bodyStmtExecutions;
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		EvalUtil.executeStatement(initStmtExec, record, context);
		final int caseIndex = getMatchingCaseIndex(record, context);
		for (int i = caseIndex; i < bodyStmtExecutions.length; ++i) {
			ICPPExecution stmtExec = bodyStmtExecutions[i];
			ICPPExecution result = EvalUtil.executeStatement(stmtExec, record, context);
			if (result instanceof ExecReturn || result instanceof ExecContinue) {
				return result;
			} else if (result instanceof ExecBreak) {
				break;
			}
		}
		return null;
	}

	private int getMatchingCaseIndex(ActivationRecord record, ConstexprEvaluationContext context) {
		IValue controllerValue = null;
		if (controllerExprEval != null) {
			controllerValue = EvalUtil.getConditionExprValue(controllerExprEval, record, context);
		} else if (controllerDeclExec != null) {
			controllerValue = EvalUtil.getConditionDeclValue(controllerDeclExec, record, context);
		}

		for (int i = 0; i < bodyStmtExecutions.length; ++i) {
			if (isSatisfiedCaseStatement(bodyStmtExecutions[i], controllerValue, record, context)) {
				return i;
			}
		}
		return bodyStmtExecutions.length;
	}

	private boolean isSatisfiedCaseStatement(ICPPExecution stmtExec, IValue controllerValue, ActivationRecord record,
			ConstexprEvaluationContext context) {
		if (stmtExec instanceof ExecCase) {
			ExecCase caseStmtExec = (ExecCase) stmtExec;
			caseStmtExec = (ExecCase) caseStmtExec.executeForFunctionCall(record, context);
			Number caseVal = caseStmtExec.getCaseExpressionEvaluation().getValue().numberValue();
			Number controllerVal = controllerValue.numberValue();
			return caseVal != null && controllerVal != null && caseVal.equals(controllerVal);
		}
		return stmtExec instanceof ExecDefault;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPExecution newInitStmtExec = initStmtExec != null ? initStmtExec.instantiate(context, maxDepth) : null;
		ICPPEvaluation newControllerExprEval = controllerExprEval != null
				? controllerExprEval.instantiate(context, maxDepth)
				: null;
		ExecSimpleDeclaration newControllerDeclExec = controllerDeclExec != null
				? (ExecSimpleDeclaration) controllerDeclExec.instantiate(context, maxDepth)
				: null;
		ICPPExecution[] newBodyStmtExecutions = new ICPPExecution[bodyStmtExecutions.length];
		boolean executionsDidChange = false;
		for (int i = 0; i < bodyStmtExecutions.length; i++) {
			ICPPExecution bodyStmtExec = bodyStmtExecutions[i];
			ICPPExecution newBodyStmtExec = bodyStmtExec.instantiate(context, maxDepth);
			if (newBodyStmtExec != bodyStmtExec) {
				executionsDidChange = true;
			}
			newBodyStmtExecutions[i] = newBodyStmtExec;
		}

		if (newInitStmtExec == initStmtExec && newControllerExprEval == controllerExprEval
				&& newControllerDeclExec == controllerDeclExec && !executionsDidChange) {
			return this;
		}
		return new ExecSwitch(newInitStmtExec, newControllerExprEval, newControllerDeclExec, newBodyStmtExecutions);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_SWITCH);
		buffer.marshalExecution(initStmtExec, includeValue);
		buffer.marshalEvaluation(controllerExprEval, includeValue);
		buffer.marshalExecution(controllerDeclExec, includeValue);
		buffer.putInt(bodyStmtExecutions.length);
		for (ICPPExecution execution : bodyStmtExecutions) {
			buffer.marshalExecution(execution, includeValue);
		}
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPExecution initStmtExec = buffer.unmarshalExecution();
		ICPPEvaluation controllerExprEval = buffer.unmarshalEvaluation();
		ExecSimpleDeclaration controllerDeclExec = (ExecSimpleDeclaration) buffer.unmarshalExecution();
		int len = buffer.getInt();
		ICPPExecution[] bodyStmtExecutions = new ICPPExecution[len];
		for (int i = 0; i < bodyStmtExecutions.length; i++) {
			bodyStmtExecutions[i] = buffer.unmarshalExecution();
		}
		return new ExecSwitch(initStmtExec, controllerExprEval, controllerDeclExec, bodyStmtExecutions);
	}
}
