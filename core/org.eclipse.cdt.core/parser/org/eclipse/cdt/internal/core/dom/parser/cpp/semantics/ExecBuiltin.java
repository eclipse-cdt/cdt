/*******************************************************************************
* Copyright (c) 2022 Davin McCall and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Davin McCall - initial API and implementation
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Constexpr-evaluation for compiler builtin functions.
 */
public class ExecBuiltin implements ICPPExecution {
	public final static short BUILTIN_FFS = 0;

	private static IType intType = new CPPBasicType(Kind.eInt, 0);

	private short funcId;

	public ExecBuiltin(short funcId) {
		this.funcId = funcId;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		return this;
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {

		switch (funcId) {
		case BUILTIN_FFS:
			return executeBuiltinFfs(record, context);
		}
		return null;
	}

	private ICPPExecution executeBuiltinFfs(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPEvaluation arg0 = record.getVariable(new CPPBuiltinParameter(null, 0));

		IValue argValue = arg0.getValue();
		if (!(argValue instanceof IntegralValue))
			return null;

		// __builtin_ffs returns 0 if arg is 0, or 1+count where count is the number of trailing 0 bits
		long arg = argValue.numberValue().longValue();
		if (arg == 0) {
			return new ExecReturn(new EvalFixed(intType, ValueCategory.PRVALUE, IntegralValue.create(0)));
		}
		int count = 0;
		while ((arg & 1) == 0) {
			arg >>= 1;
			count++;
		}
		return new ExecReturn(new EvalFixed(intType, ValueCategory.PRVALUE, IntegralValue.create(count + 1)));
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_BUILTIN);
		buffer.putShort(funcId);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		short funcId = buffer.getShort();
		return new ExecBuiltin(funcId);
	}
}
