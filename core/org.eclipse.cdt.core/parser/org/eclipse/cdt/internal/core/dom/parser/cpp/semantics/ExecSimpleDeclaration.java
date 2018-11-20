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
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class ExecSimpleDeclaration implements ICPPExecution {
	private final ICPPExecution[] declaratorExecutions;

	public ExecSimpleDeclaration(ICPPExecution[] declaratorExecutions) {
		this.declaratorExecutions = declaratorExecutions;
	}

	public ICPPExecution[] getDeclaratorExecutions() {
		return declaratorExecutions;
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPExecution[] newDeclaratorExecutions = new ICPPExecution[declaratorExecutions.length];
		for (int i = 0; i < declaratorExecutions.length; i++) {
			newDeclaratorExecutions[i] = declaratorExecutions[i].executeForFunctionCall(record, context);
		}
		return new ExecSimpleDeclaration(newDeclaratorExecutions);
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPExecution[] newDeclaratorExecutions = new ICPPExecution[declaratorExecutions.length];
		for (int i = 0; i < declaratorExecutions.length; i++) {
			newDeclaratorExecutions[i] = declaratorExecutions[i].instantiate(context, maxDepth);
		}
		return new ExecSimpleDeclaration(newDeclaratorExecutions);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_SIMPLE_DECLARATION);
		buffer.putInt(declaratorExecutions.length);
		for (ICPPExecution execution : declaratorExecutions) {
			buffer.marshalExecution(execution, includeValue);
		}
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int len = buffer.getInt();
		ICPPExecution[] declaratorExecutions = new ICPPExecution[len];
		for (int i = 0; i < declaratorExecutions.length; i++) {
			declaratorExecutions[i] = buffer.unmarshalExecution();
		}
		return new ExecSimpleDeclaration(declaratorExecutions);
	}
}
