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
	public final static short BUILTIN_FFS = 0, BUILTIN_FFSL = 1, BUILTIN_FFSLL = 2, BUILTIN_CTZ = 3, BUILTIN_CTZL = 4,
			BUILTIN_CTZLL = 5, BUILTIN_POPCOUNT = 6, BUILTIN_POPCOUNTL = 7, BUILTIN_POPCOUNTLL = 8, BUILTIN_PARITY = 9,
			BUILTIN_PARITYL = 10, BUILTIN_PARITYLL = 11, BUILTIN_ABS = 12, BUILTIN_LABS = 13, BUILTIN_LLABS = 14;

	private static IType intType = new CPPBasicType(Kind.eInt, 0);
	private static IType longType = new CPPBasicType(Kind.eInt, CPPBasicType.IS_LONG);
	private static IType longlongType = new CPPBasicType(Kind.eInt, CPPBasicType.IS_LONG_LONG);

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
		case BUILTIN_FFSL:
		case BUILTIN_FFSLL:
			return executeBuiltinFfs(record, context);
		case BUILTIN_CTZ:
		case BUILTIN_CTZL:
		case BUILTIN_CTZLL:
			return executeBuiltinCtz(record, context);
		case BUILTIN_POPCOUNT:
		case BUILTIN_POPCOUNTL:
		case BUILTIN_POPCOUNTLL:
			return executeBuiltinPopcount(record, context);
		case BUILTIN_PARITY:
		case BUILTIN_PARITYL:
		case BUILTIN_PARITYLL:
			return executeBuiltinParity(record, context);
		case BUILTIN_ABS:
		case BUILTIN_LABS:
		case BUILTIN_LLABS:
			return executeBuiltinAbs(record, context);
		}
		return null;
	}

	/*
	 * Return an execution representing __builtin_ffs or __builtin_ctz
	 */
	private ICPPExecution executeBuiltinFfsCtz(ActivationRecord record, ConstexprEvaluationContext context,
			boolean isCtz) {
		ICPPEvaluation arg0 = record.getVariable(new CPPBuiltinParameter(null, 0));

		IValue argValue = arg0.getValue();
		if (!(argValue instanceof IntegralValue))
			return null;

		// __builtin_ffs returns 0 if arg is 0, or 1+count where count is the number of trailing 0 bits
		// __builtin_ctz is undefined if arg is 0, or returns count
		long arg = argValue.numberValue().longValue();
		if (arg == 0) {
			if (isCtz) {
				return null;
			} else {
				return new ExecReturn(new EvalFixed(intType, ValueCategory.PRVALUE, IntegralValue.create(0)));
			}
		}
		int count = 0;
		while ((arg & 1) == 0) {
			arg >>= 1;
			count++;
		}
		int increment = isCtz ? 0 : 1;
		return new ExecReturn(new EvalFixed(intType, ValueCategory.PRVALUE, IntegralValue.create(count + increment)));
	}

	private ICPPExecution executeBuiltinFfs(ActivationRecord record, ConstexprEvaluationContext context) {
		return executeBuiltinFfsCtz(record, context, false /* ffs */);
	}

	private ICPPExecution executeBuiltinCtz(ActivationRecord record, ConstexprEvaluationContext context) {
		return executeBuiltinFfsCtz(record, context, true /* ctz */);
	}

	/*
	 * Return an execution representing __builtin_popcount
	 */
	private ICPPExecution executeBuiltinPopcountParity(ActivationRecord record, ConstexprEvaluationContext context,
			boolean isParity) {
		ICPPEvaluation arg0 = record.getVariable(new CPPBuiltinParameter(null, 0));

		IValue argValue = arg0.getValue();
		if (!(argValue instanceof IntegralValue))
			return null;

		long arg = argValue.numberValue().longValue();
		int count = 0;
		while (arg != 0) {
			if ((arg & 1) != 0)
				count++;
			arg >>>= 1;
		}
		if (isParity) {
			count = count & 1;
		}
		return new ExecReturn(new EvalFixed(intType, ValueCategory.PRVALUE, IntegralValue.create(count)));
	}

	private ICPPExecution executeBuiltinPopcount(ActivationRecord record, ConstexprEvaluationContext context) {
		return executeBuiltinPopcountParity(record, context, false);
	}

	private ICPPExecution executeBuiltinParity(ActivationRecord record, ConstexprEvaluationContext context) {
		return executeBuiltinPopcountParity(record, context, true);
	}

	private ICPPExecution executeBuiltinAbs(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPEvaluation arg0 = record.getVariable(new CPPBuiltinParameter(null, 0));

		IValue argValue = arg0.getValue();
		if (!(argValue instanceof IntegralValue))
			return null;

		long arg = argValue.numberValue().longValue();
		long result = Math.abs(arg);

		IType resultType = null;
		switch (funcId) {
		case BUILTIN_ABS:
			resultType = intType;
			break;
		case BUILTIN_LABS:
			resultType = longType;
			break;
		case BUILTIN_LLABS:
			resultType = longlongType;
			break;
		}

		return new ExecReturn(new EvalFixed(resultType, ValueCategory.PRVALUE, IntegralValue.create(result)));
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
