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
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil.Pair;
import org.eclipse.core.runtime.CoreException;

public class ExecCase implements ICPPExecution {
	private final ICPPEvaluation caseExprEval;

	public ExecCase(ICPPEvaluation caseExprEval) {
		this.caseExprEval = caseExprEval;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation newCaseExprEval = caseExprEval.instantiate(context, maxDepth);
		if (newCaseExprEval == caseExprEval) {
			return this;
		}
		return new ExecCase(newCaseExprEval);
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		Pair<ICPPEvaluation, ICPPEvaluation> vp = EvalUtil.getValuePair(caseExprEval, record, context);
		ICPPEvaluation fixed = vp.getSecond();
		if (fixed == caseExprEval) {
			return this;
		}
		return new ExecCase(fixed);
	}

	public ICPPEvaluation getCaseExpressionEvaluation() {
		return caseExprEval;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_CASE);
		buffer.marshalEvaluation(caseExprEval, includeValue);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation caseExprEval = buffer.unmarshalEvaluation();
		return new ExecCase(caseExprEval);
	}
}
