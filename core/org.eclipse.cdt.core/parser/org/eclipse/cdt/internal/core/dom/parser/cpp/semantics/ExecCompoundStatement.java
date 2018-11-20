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

import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class ExecCompoundStatement implements ICPPExecution {
	private ICPPExecution[] executions;

	private ExecCompoundStatement(ICPPExecution[] executions) {
		this.executions = executions;
	}

	public ExecCompoundStatement(IASTStatement[] statements) {
		this(createExecutionsFromStatements(statements));
	}

	private static ICPPExecution[] createExecutionsFromStatements(IASTStatement[] statements) {
		ICPPExecution[] executions = new ICPPExecution[statements.length];
		for (int i = 0; i < executions.length; i++) {
			executions[i] = EvalUtil.getExecutionFromStatement(statements[i]);
		}
		return executions;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPExecution[] newExecutions = new ICPPExecution[executions.length];
		for (int i = 0; i < executions.length; i++) {
			if (executions[i] == null) {
				continue;
			}
			newExecutions[i] = executions[i].instantiate(context, maxDepth);
		}
		return new ExecCompoundStatement(newExecutions);
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		for (ICPPExecution execution : executions) {
			if (execution == null) {
				continue;
			}

			ICPPExecution result = EvalUtil.executeStatement(execution, record, context);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_COMPOUND_STATEMENT);
		buffer.putInt(executions.length);
		for (ICPPExecution execution : executions) {
			buffer.marshalExecution(execution, includeValue);
		}
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int len = buffer.getInt();
		ICPPExecution[] executions = new ICPPExecution[len];
		for (int i = 0; i < executions.length; i++) {
			executions[i] = buffer.unmarshalExecution();
		}
		return new ExecCompoundStatement(executions);
	}
}
