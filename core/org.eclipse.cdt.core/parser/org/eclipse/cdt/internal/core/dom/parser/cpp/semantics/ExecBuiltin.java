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
			return executeBuiltinFfs(record, context, intType);
		case BUILTIN_FFSL:
			return executeBuiltinFfs(record, context, longType);
		case BUILTIN_FFSLL:
			return executeBuiltinFfs(record, context, longlongType);
		case BUILTIN_CTZ:
			return executeBuiltinCtz(record, context, intType);
		case BUILTIN_CTZL:
			return executeBuiltinCtz(record, context, longType);
		case BUILTIN_CTZLL:
			return executeBuiltinCtz(record, context, longlongType);
		case BUILTIN_POPCOUNT:
			return executeBuiltinPopcount(record, context, intType);
		case BUILTIN_POPCOUNTL:
			return executeBuiltinPopcount(record, context, longType);
		case BUILTIN_POPCOUNTLL:
			return executeBuiltinPopcount(record, context, longlongType);
		case BUILTIN_PARITY:
			return executeBuiltinParity(record, context, intType);
		case BUILTIN_PARITYL:
			return executeBuiltinParity(record, context, longType);
		case BUILTIN_PARITYLL:
			return executeBuiltinParity(record, context, longlongType);
		case BUILTIN_ABS:
			return executeBuiltinAbs(record, context, intType);
		case BUILTIN_LABS:
			return executeBuiltinAbs(record, context, longType);
		case BUILTIN_LLABS:
			return executeBuiltinAbs(record, context, longlongType);
		}
		return null;
	}

	/*
	 * Return an execution representing __builtin_ffs or __builtin_ctz
	 */
	private ICPPExecution executeBuiltinFfsCtz(ActivationRecord record, ConstexprEvaluationContext context,
			boolean isCtz, IType argType) {
		ICPPEvaluation arg0 = record.getVariable(new CPPBuiltinParameter(null, 0));

		IValue argValue = arg0.getValue();
		Number numberVal = argValue.numberValue();
		numberVal = Conversions.narrowNumberValue(numberVal, argType);
		if (numberVal == null)
			return null;

		// __builtin_ffs returns 0 if arg is 0, or 1+count where count is the number of trailing 0 bits
		// __builtin_ctz is undefined if arg is 0, or returns count
		long arg = numberVal.longValue();
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

	private ICPPExecution executeBuiltinFfs(ActivationRecord record, ConstexprEvaluationContext context,
			IType argType) {
		return executeBuiltinFfsCtz(record, context, false /* ffs */, argType);
	}

	private ICPPExecution executeBuiltinCtz(ActivationRecord record, ConstexprEvaluationContext context,
			IType argType) {
		return executeBuiltinFfsCtz(record, context, true /* ctz */, argType);
	}

	/*
	 * Return an execution representing __builtin_popcount
	 */
	private ICPPExecution executeBuiltinPopcountParity(ActivationRecord record, ConstexprEvaluationContext context,
			boolean isParity, IType argType) {
		ICPPEvaluation arg0 = record.getVariable(new CPPBuiltinParameter(null, 0));

		IValue argValue = arg0.getValue();
		Number numberVal = argValue.numberValue();
		numberVal = Conversions.narrowNumberValue(numberVal, argType);
		if (numberVal == null)
			return null;

		long arg = numberVal.longValue();
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

	private ICPPExecution executeBuiltinPopcount(ActivationRecord record, ConstexprEvaluationContext context,
			IType argType) {
		return executeBuiltinPopcountParity(record, context, false, argType);
	}

	private ICPPExecution executeBuiltinParity(ActivationRecord record, ConstexprEvaluationContext context,
			IType argType) {
		return executeBuiltinPopcountParity(record, context, true, argType);
	}

	private ICPPExecution executeBuiltinAbs(ActivationRecord record, ConstexprEvaluationContext context,
			IType argType) {
		ICPPEvaluation arg0 = record.getVariable(new CPPBuiltinParameter(null, 0));

		IValue argValue = arg0.getValue();
		Number argNumber = argValue.numberValue();
		argNumber = Conversions.narrowNumberValue(argNumber, argType);
		if (argNumber == null)
			return null;

		long arg = argNumber.longValue();
		long result = Math.abs(arg);

		return new ExecReturn(new EvalFixed(argType, ValueCategory.PRVALUE, IntegralValue.create(result)));
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
