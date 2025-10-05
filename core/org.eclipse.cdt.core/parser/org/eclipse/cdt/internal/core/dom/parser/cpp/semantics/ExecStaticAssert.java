/*******************************************************************************
 * Copyright (c) 2025 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class ExecStaticAssert implements ICPPExecution {
	public static final ICPPEvaluation FAILED = new EvalFixed(ProblemType.STATIC_ASSERT_FAILED, PRVALUE,
			IntegralValue.STATIC_ASSERT_FAILED_ERROR);

	public static final ExecStaticAssert FAILED_INSTANCE = new ExecStaticAssert(null);

	private final ICPPEvaluation fCondition;

	public ExecStaticAssert(ICPPEvaluation condition) {
		// null condition is reused for static FAILED_INSTANCE
		fCondition = condition;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation condition = fCondition == null ? null : fCondition.instantiate(context, maxDepth);
		if (condition == fCondition) {
			return this;
		}

		return new ExecStaticAssert(condition);
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		if (fCondition == null || !EvalUtil.conditionExprSatisfied(fCondition, record, context)) {
			//return ExecIncomplete.INSTANCE;
			//return this;
			return FAILED_INSTANCE;
		} else {
			return null;
		}
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EXEC_STATIC_ASSERT;
		buffer.putShort(firstBytes);
		buffer.marshalEvaluation(fCondition, includeValue);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPEvaluation condition = buffer.unmarshalEvaluation();
		return new ExecStaticAssert(condition);
	}
}
