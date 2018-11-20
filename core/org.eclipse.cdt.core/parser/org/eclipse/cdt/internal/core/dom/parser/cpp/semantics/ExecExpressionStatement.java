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

public class ExecExpressionStatement implements ICPPExecution {
	private ICPPEvaluation exprEval;

	public ExecExpressionStatement(ICPPEvaluation exprEval) {
		this.exprEval = exprEval;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation newExprEval = exprEval.instantiate(context, maxDepth);
		if (newExprEval == exprEval) {
			return this;
		}
		return new ExecExpressionStatement(newExprEval);
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPEvaluation newExprEval = exprEval.computeForFunctionCall(record, context);
		if (newExprEval == exprEval) {
			return this;
		}
		return new ExecExpressionStatement(newExprEval);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_EXPRESSION_STATEMENT);
		buffer.marshalEvaluation(exprEval, includeValue);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation exprEval = buffer.unmarshalEvaluation();
		return new ExecExpressionStatement(exprEval);
	}
}
