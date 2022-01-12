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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class ExecConstructorChain implements ICPPExecution {
	private final Map<IBinding, ICPPEvaluation> ccInitializers;

	public ExecConstructorChain(Map<IBinding, ICPPEvaluation> ccInitializers) {
		this.ccInitializers = ccInitializers;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		Map<IBinding, ICPPEvaluation> instantiatedInitializers = new HashMap<>();
		for (Entry<IBinding, ICPPEvaluation> initializer : ccInitializers.entrySet()) {
			instantiatedInitializers.put(CPPEvaluation.instantiateBinding(initializer.getKey(), context, maxDepth),
					initializer.getValue().instantiate(context, maxDepth));
		}
		return new ExecConstructorChain(instantiatedInitializers);
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		return this;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_CONSTRUCTOR_CHAIN);
		buffer.putInt(ccInitializers.size());
		for (Entry<IBinding, ICPPEvaluation> ccInitializer : ccInitializers.entrySet()) {
			buffer.marshalBinding(ccInitializer.getKey());
			buffer.marshalEvaluation(ccInitializer.getValue(), includeValue);
		}
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int len = buffer.getInt();
		Map<IBinding, ICPPEvaluation> ccInitializers = new HashMap<>();
		for (int i = 0; i < len; i++) {
			IBinding member = buffer.unmarshalBinding();
			ICPPEvaluation memberEval = buffer.unmarshalEvaluation();
			ccInitializers.put(member, memberEval);
		}
		return new ExecConstructorChain(ccInitializers);
	}

	public Map<IBinding, ICPPEvaluation> getConstructorChainInitializers() {
		return ccInitializers;
	}
}
