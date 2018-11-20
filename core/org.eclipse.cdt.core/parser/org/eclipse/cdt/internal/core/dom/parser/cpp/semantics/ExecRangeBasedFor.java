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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ALLCVQ;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class ExecRangeBasedFor implements ICPPExecution {
	private final ExecSimpleDeclaration declarationExec;
	private final ICPPEvaluation initClauseEval;
	private final ICPPFunction begin;
	private final ICPPFunction end;
	private final ICPPExecution bodyExec;

	public ExecRangeBasedFor(ExecSimpleDeclaration declarationExec, ICPPEvaluation initClauseEval, ICPPFunction begin,
			ICPPFunction end, ICPPExecution bodyExec) {
		this.declarationExec = declarationExec;
		this.initClauseEval = initClauseEval;
		this.begin = begin;
		this.end = end;
		this.bodyExec = bodyExec;
	}

	private ICPPExecution loopOverArray(IVariable rangeVar, ICPPEvaluation valueRange, ActivationRecord record,
			ConstexprEvaluationContext context) {
		ICPPEvaluation[] range = valueRange.getValue().getAllSubValues();
		for (int i = 0; i < range.length; i++) {
			ICPPEvaluation value = new EvalFixed(range[i].getType(), range[i].getValueCategory(), range[i].getValue());
			if (rangeVar.getType() instanceof ICPPReferenceType) {
				value = new EvalReference(record, new EvalCompositeAccess(valueRange, i),
						value.getTemplateDefinition());
			}
			record.update(rangeVar, value);

			ICPPExecution result = EvalUtil.executeStatement(bodyExec, record, context);
			if (result instanceof ExecReturn) {
				return result;
			} else if (result instanceof ExecBreak) {
				break;
			} else if (result instanceof ExecContinue) {
				continue;
			}
		}
		return null;
	}

	private ICPPExecution loopOverObject(IVariable rangeVar, ICPPEvaluation rangeEval, boolean rangeIsConst,
			ICPPClassType classType, ActivationRecord record, ConstexprEvaluationContext context) {
		if (begin != null && end != null) {
			ICPPEvaluation beginEval = callFunction(classType, begin, rangeEval, record, context);
			ICPPEvaluation endEval = callFunction(classType, end, rangeEval, record, context);
			boolean isRef = rangeVar.getType() instanceof ICPPReferenceType;

			for (; !isEqual(beginEval, endEval); beginEval = inc(record, beginEval, context)) {
				record.update(rangeVar, deref(beginEval, isRef, record, context));

				ICPPExecution result = EvalUtil.executeStatement(bodyExec, record, context);
				if (result instanceof ExecReturn) {
					return result;
				} else if (result instanceof ExecBreak) {
					break;
				} else if (result instanceof ExecContinue) {
					continue;
				}
			}
			return null;
		}
		return ExecIncomplete.INSTANCE;
	}

	private static boolean isEqual(ICPPEvaluation a, ICPPEvaluation b) {
		Number result = new EvalBinary(IASTBinaryExpression.op_equals, a, b, a.getTemplateDefinition()).getValue()
				.numberValue();
		return result != null && result.longValue() != 0;
	}

	private static ICPPEvaluation deref(ICPPEvaluation ptr, boolean isRef, ActivationRecord record,
			ConstexprEvaluationContext context) {
		ICPPEvaluation derefEval = new EvalUnary(ICPPASTUnaryExpression.op_star, ptr, null, ptr.getTemplateDefinition())
				.computeForFunctionCall(record, context);
		if (isRef) {
			return derefEval;
		} else {
			return new EvalFixed(derefEval.getType(), derefEval.getValueCategory(), derefEval.getValue());
		}
	}

	private static ICPPEvaluation inc(ActivationRecord record, ICPPEvaluation ptr, ConstexprEvaluationContext context) {
		return new EvalUnary(IASTUnaryExpression.op_prefixIncr, ptr, null, ptr.getTemplateDefinition())
				.computeForFunctionCall(record, context);
	}

	private static ICPPEvaluation callFunction(ICPPClassType classType, ICPPFunction func, ICPPEvaluation rangeEval,
			ActivationRecord record, ConstexprEvaluationContext context) {
		EvalFunctionCall call = null;
		IBinding templateDefinition = rangeEval.getTemplateDefinition();
		if (func instanceof ICPPMethod) {
			EvalMemberAccess memberAccess = new EvalMemberAccess(classType, ValueCategory.LVALUE, func, rangeEval,
					false, templateDefinition);
			ICPPEvaluation[] args = new ICPPEvaluation[] { memberAccess };
			call = new EvalFunctionCall(args, rangeEval, templateDefinition);
		} else {
			EvalBinding op = new EvalBinding(func, func.getType(), templateDefinition);
			ICPPEvaluation[] args = new ICPPEvaluation[] { op, rangeEval };
			call = new EvalFunctionCall(args, null, templateDefinition);
		}
		return call.computeForFunctionCall(record, context);
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		if (context.getStepsPerformed() >= ConstexprEvaluationContext.MAX_CONSTEXPR_EVALUATION_STEPS) {
			return ExecIncomplete.INSTANCE;
		}

		ICPPEvaluation valueRange = initClauseEval.computeForFunctionCall(record, context.recordStep());
		ExecDeclarator declaratorExec = (ExecDeclarator) declarationExec.getDeclaratorExecutions()[0];
		IVariable rangeVar = (IVariable) declaratorExec.getDeclaredBinding();
		if (rangeVar == null) {
			return ExecIncomplete.INSTANCE;
		}

		boolean rangeIsConst = SemanticUtil.isConst(initClauseEval.getType());
		IType type = SemanticUtil.getNestedType(valueRange.getType(), ALLCVQ | TDEF | REF);
		if (type instanceof IArrayType || type instanceof InitializerListType) {
			return loopOverArray(rangeVar, valueRange, record, context);
		} else if (type instanceof ICPPClassType) {
			ICPPClassType classType = (ICPPClassType) type;
			return loopOverObject(rangeVar, initClauseEval, rangeIsConst, classType, record, context);
		}
		return ExecIncomplete.INSTANCE;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ExecSimpleDeclaration newDeclarationExec = (ExecSimpleDeclaration) declarationExec.instantiate(context,
				maxDepth);
		ICPPEvaluation newInitClauseEval = initClauseEval.instantiate(context, maxDepth);
		// TODO: This isn't the correct way to instantiate the 'begin' and 'end' functions, because
		//       if the range type is dependent, ADL may find different functions to call after instantiation.
		//       The correct way would be:
		//         - Construct EvalFunctionCalls representing the invocations of begin and end with the
		//           range as argument.
		//         - Instantiate the EvalFunctionCalls. This will perform the ADL if appropriate.
		//         - Query the instantiated function bindings from the instantiated EvalFunctionCalls.
		//       Alternatively, we could lower the range-based for loop into a regular for loop as
		//       described in the standard (by constructing the corresponding executions and evaluations),
		//       and the above instantiations will fall out of that automatically.
		ICPPSpecialization owner = context.getContextSpecialization();
		ICPPFunction newBegin = null;
		ICPPFunction newEnd = null;
		if (owner instanceof ICPPClassSpecialization) {
			if (begin != null) {
				newBegin = (ICPPFunction) ((ICPPClassSpecialization) owner).specializeMember(begin);
			}
			if (end != null) {
				newEnd = (ICPPFunction) ((ICPPClassSpecialization) owner).specializeMember(end);
			}
		}
		ICPPExecution newBodyExec = bodyExec.instantiate(context, maxDepth);

		if (newDeclarationExec == declarationExec && newInitClauseEval == initClauseEval && newBegin == begin
				&& newEnd == end && newBodyExec == bodyExec) {
			return this;
		}
		return new ExecRangeBasedFor(newDeclarationExec, newInitClauseEval, newBegin, newEnd, newBodyExec);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_RANGE_BASED_FOR);
		buffer.marshalExecution(declarationExec, includeValue);
		buffer.marshalEvaluation(initClauseEval, includeValue);
		buffer.marshalBinding(begin);
		buffer.marshalBinding(end);
		buffer.marshalExecution(bodyExec, includeValue);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ExecSimpleDeclaration declarationExec = (ExecSimpleDeclaration) buffer.unmarshalExecution();
		ICPPEvaluation initClauseEval = buffer.unmarshalEvaluation();
		ICPPFunction begin = (ICPPFunction) buffer.unmarshalBinding();
		ICPPFunction end = (ICPPFunction) buffer.unmarshalBinding();
		ICPPExecution bodyExec = buffer.unmarshalExecution();
		return new ExecRangeBasedFor(declarationExec, initClauseEval, begin, end, bodyExec);
	}
}
