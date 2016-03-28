/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableExecution;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil.Pair;
import org.eclipse.core.runtime.CoreException;

public class ExecConstructorChain implements ICPPExecution {
	private final List<Pair<IBinding, ICPPEvaluation>> ccInitializers;
	
	public ExecConstructorChain(List<Pair<IBinding, ICPPEvaluation>> ccInitializers) {
		this.ccInitializers = ccInitializers;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		return this;
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		return this;
	}
	
	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_CONSTRUCTOR_CHAIN);
		buffer.putInt(ccInitializers.size());
		for (Pair<IBinding, ICPPEvaluation> ccInitializer : ccInitializers) {
			buffer.marshalBinding(ccInitializer.getFirst());
			buffer.marshalEvaluation(ccInitializer.getSecond(), includeValue);
		}
	}
	
	public static ISerializableExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int len = buffer.getInt();
		List<Pair<IBinding, ICPPEvaluation>> ccInitializers = new ArrayList<>();
		for (int i = 0; i < len; i++) {
			IBinding member = buffer.unmarshalBinding();
			ICPPEvaluation memberEval = (ICPPEvaluation)buffer.unmarshalEvaluation();
			ccInitializers.add(new Pair<IBinding, ICPPEvaluation>(member, memberEval));
		}
		return new ExecConstructorChain(ccInitializers);
	}
	
	public List<Pair<IBinding, ICPPEvaluation>> getConstructorChainInitializers() {
		return ccInitializers;
	} 
}
