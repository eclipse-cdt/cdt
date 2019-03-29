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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecutionOwner;

public class EvalUtil {
	/**
	 * The set of ICPPVariable objects for which initial value computation is in progress on each thread.
	 * This is used to guard against recursion during initial value computation.
	 */
	private static final ThreadLocal<Set<ICPPVariable>> fInitialValueInProgress = new ThreadLocal<Set<ICPPVariable>>() {
		@Override
		protected Set<ICPPVariable> initialValue() {
			return new HashSet<>();
		}
	};

	public static IValue getConditionExprValue(ICPPEvaluation conditionExprEval, ActivationRecord record,
			ConstexprEvaluationContext context) {
		return conditionExprEval.computeForFunctionCall(record, context.recordStep()).getValue();
	}

	public static IValue getConditionDeclValue(ExecSimpleDeclaration conditionDeclExec, ActivationRecord record,
			ConstexprEvaluationContext context) {
		ICPPBinding declaredBinding = ((ExecDeclarator) conditionDeclExec.getDeclaratorExecutions()[0])
				.getDeclaredBinding();
		conditionDeclExec.executeForFunctionCall(record, context.recordStep());
		return record.getVariable(declaredBinding).computeForFunctionCall(record, context).getValue();
	}

	public static boolean conditionExprSatisfied(ICPPEvaluation conditionExprEval, ActivationRecord record,
			ConstexprEvaluationContext context) {
		Number result = getConditionExprValue(conditionExprEval, record, context).numberValue();
		return result != null && result.longValue() != 0;
	}

	public static boolean conditionDeclSatisfied(ExecSimpleDeclaration conditionDeclExec, ActivationRecord record,
			ConstexprEvaluationContext context) {
		Number result = getConditionDeclValue(conditionDeclExec, record, context).numberValue();
		return result != null && result.longValue() != 0;
	}

	public static ICPPExecution getExecutionFromStatement(IASTStatement stmt) {
		if (stmt instanceof ICPPExecutionOwner) {
			ICPPExecutionOwner execOwner = (ICPPExecutionOwner) stmt;
			return execOwner.getExecution();
		}
		return null;
	}

	// A return value != null means that there was a return, break or continue in that statement.
	public static ICPPExecution executeStatement(ICPPExecution exec, ActivationRecord record,
			ConstexprEvaluationContext context) {
		if (exec instanceof ExecExpressionStatement || exec instanceof ExecDeclarationStatement
				|| exec instanceof ExecCase || exec instanceof ExecDefault) {
			exec.executeForFunctionCall(record, context.recordStep());
			return null;
		}

		if (exec instanceof ExecCompoundStatement || exec instanceof ExecWhile || exec instanceof ExecFor
				|| exec instanceof ExecRangeBasedFor || exec instanceof ExecDo || exec instanceof ExecIf
				|| exec instanceof ExecSwitch) {
			ICPPExecution innerResult = exec.executeForFunctionCall(record, context.recordStep());
			if (innerResult instanceof ExecReturn || innerResult instanceof ExecBreak
					|| innerResult instanceof ExecContinue) {
				return innerResult;
			} else if (innerResult != null) {
				return ExecIncomplete.INSTANCE;
			}
			return null;
		}

		return exec;
	}

	private static boolean isUpdateable(ICPPEvaluation eval) {
		return eval instanceof EvalBinding || (eval instanceof EvalReference && !(eval instanceof EvalPointer))
				|| eval instanceof EvalCompositeAccess;
	}

	/**
	 * Returns a pair of evaluations, each representing the value of 'eval'.
	 * The first, "updateable", is an lvalue (EvalBinding, EvalReference, or EvalCompositeAccess).
	 * The second, "fixed", is a value (usually EvalFixed or EvalPointer).
	 * We return both because, depending on the operation, we may need one representation or another.
	 */
	public static Pair<ICPPEvaluation, ICPPEvaluation> getValuePair(ICPPEvaluation eval, ActivationRecord record,
			ConstexprEvaluationContext context) {
		ICPPEvaluation updateable = null;
		if (isUpdateable(eval)) {
			updateable = eval;
		}
		ICPPEvaluation fixed = eval.computeForFunctionCall(record, context.recordStep());
		if (fixed == EvalFixed.INCOMPLETE) {
			updateable = fixed;
		} else if (isUpdateable(fixed)) {
			updateable = fixed;
			if (!(fixed instanceof EvalCompositeAccess)) {
				fixed = fixed.computeForFunctionCall(record, context);
			}
		}
		return new Pair<>(updateable, fixed);
	}

	public static class Pair<T1, T2> {
		private final T1 first;
		private final T2 second;

		public Pair(T1 first, T2 second) {
			this.first = first;
			this.second = second;
		}

		public T1 getFirst() {
			return first;
		}

		public T2 getSecond() {
			return second;
		}
	}

	public static boolean isCompilerGeneratedCtor(IBinding ctor) {
		if (ctor instanceof ICPPSpecialization) {
			ICPPSpecialization ctorSpec = (ICPPSpecialization) ctor;
			return isCompilerGeneratedCtor(ctorSpec.getSpecializedBinding());
		}
		return ctor instanceof ICPPConstructor && ((ICPPConstructor) ctor).isImplicit();
	}

	/**
	 * Returns the initial value of the given variable, evaluated in the context of
	 * the given activation record.
	 */
	public static ICPPEvaluation getVariableInitialValue(ICPPVariable variable, ActivationRecord record) {
		Set<ICPPVariable> recursionProtectionSet = fInitialValueInProgress.get();
		if (!recursionProtectionSet.add(variable)) {
			return EvalFixed.INCOMPLETE;
		}
		try {
			IType type = variable.getType();
			IType nestedType = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
			IValue initialValue = variable.getInitialValue();
			ICPPEvaluation valueEval = null;

			if ((initialValue != null && initialValue.getEvaluation() != null)
					|| (initialValue == null && nestedType instanceof ICPPClassType)) {
				final ICPPEvaluation initializerEval = initialValue == null ? null : initialValue.getEvaluation();
				if (initializerEval == EvalFixed.INCOMPLETE) {
					return null;
				}
				ExecDeclarator declaratorExec = new ExecDeclarator(variable, initializerEval);

				ConstexprEvaluationContext context = new ConstexprEvaluationContext();
				if (declaratorExec.executeForFunctionCall(record, context) != ExecIncomplete.INSTANCE) {
					valueEval = record.getVariable(declaratorExec.getDeclaredBinding());
				}
			} else if (initialValue != null) {
				valueEval = new EvalFixed(type, ValueCategory.LVALUE, initialValue);
			}

			if (valueEval != null
					&& (valueEval == EvalFixed.INCOMPLETE || valueEval.getValue() == IntegralValue.UNKNOWN)) {
				return null;
			}
			return valueEval;
		} finally {
			recursionProtectionSet.remove(variable);
		}
	}

	public static boolean isDefaultConstructor(ICPPConstructor constructor) {
		return constructor.getRequiredArgumentCount() == 0;
	}

	public static boolean evaluateNoexceptSpecifier(ICPPEvaluation noexceptSpecifier) {
		if (noexceptSpecifier != null && noexceptSpecifier.getValue() instanceof IntegralValue) {
			IntegralValue v = (IntegralValue) noexceptSpecifier.getValue();
			if (v.numberValue() != null)
				return v.numberValue().longValue() == 1;
		}
		return false;
	}
}