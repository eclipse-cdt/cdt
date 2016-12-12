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

public class ExecReturn implements ICPPExecution {
	private ICPPEvaluation retVal;

	public ExecReturn(ICPPEvaluation exprEval) {
		this.retVal = exprEval;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation newRetVal = retVal.instantiate(context, maxDepth);
		if (newRetVal == retVal) {
			return this;
		}
		return new ExecReturn(newRetVal);
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPEvaluation newRetVal = retVal.computeForFunctionCall(record, context);
		if (newRetVal == retVal) {
			return this;
		}
		return new ExecReturn(newRetVal);
	}

	public ICPPEvaluation getReturnValueEvaluation() {
		return retVal;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_RETURN);
		buffer.marshalEvaluation(retVal, includeValue);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation retVal = buffer.unmarshalEvaluation();
		return new ExecReturn(retVal);
	}
}