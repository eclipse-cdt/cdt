/*******************************************************************************
 * Copyright (c) 2012, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ActivationRecord;
import org.eclipse.core.runtime.CoreException;

/**
 * Assists in evaluating expressions.
 */
public interface ICPPEvaluation {
	public static final ICPPEvaluation[] EMPTY_ARRAY = {};

	boolean isInitializerList();

	boolean isFunctionSet();

	/**
	 * Returns {@code true} if the type of the expression depends on template parameters.
	 */
	boolean isTypeDependent();

	/**
	 * Returns {@code true} if the value of the expression depends on template parameters.
	 */
	boolean isValueDependent();

	/**
	 * Returns {@code true} if the expression is a compile-time constant expression.
	 *
	 * @param point the point of instantiation, determines the scope for name lookups
	 */
	boolean isConstantExpression();

	/**
	 * Return the result of the noexcept-operator applied to the expression.
	 * [expr.unary.noexcept]
	 */
	boolean isNoexcept();

	/**
	 * Returns {@code true} if this expression is equivalent to 'other' for
	 * declaration matching purposes.
	 */
	boolean isEquivalentTo(ICPPEvaluation other);

	/**
	 * Returns the type of the expression.
	 *
	 * If the expression evaluates to a function set, a {@code FunctionSetType} is returned.
	 */
	IType getType();

	/**
	 * Returns the value of the expression.
	 */
	IValue getValue();

	/**
	 * Returns the category of the expression value.
	 * @see ValueCategory
	 */
	ValueCategory getValueCategory();

	/**
	 * Returns a signature uniquely identifying the evaluation. Two evaluations with identical
	 * signatures are guaranteed to produce the same results.
	 */
	char[] getSignature();

	/**
	 * Instantiates the evaluation with the provided template parameter map and pack offset.
	 * The context is used to replace templates with their specialization, where appropriate.
	 *
	 * @return a fully or partially instantiated evaluation, or the original evaluation
	 */
	ICPPEvaluation instantiate(InstantiationContext context, int maxDepth);

	/**
	 * Keeps track of state during a constexpr evaluation.
	 */
	public final class ConstexprEvaluationContext {
		/**
		 * The maximum number of steps allowed in a single constexpr evaluation.
		 * This is used to prevent a buggy constexpr function from causing the
		 * IDE to hang.
		 */
		public static final int MAX_CONSTEXPR_EVALUATION_STEPS = 1024;

		private int fStepsPerformed;

		/**
		 * Constructs a ConstexprEvaluationContext for a new constexpr evaluation.
		 */
		public ConstexprEvaluationContext() {
			fStepsPerformed = 0;
		}

		/**
		 * Records a new step being performed in this constexpr evaluation.
		 *
		 * @return this constexpr evaluation
		 */
		public ConstexprEvaluationContext recordStep() {
			++fStepsPerformed;
			return this;
		}

		/**
		 * Returns the number of steps performed so far in the constexpr evaluation.
		 */
		public int getStepsPerformed() {
			return fStepsPerformed;
		}
	}

	/**
	 * Computes the evaluation produced by substituting function parameters by their values.
	 *
	 * @param record maps function parameters and local variables to their values
	 * @param context the context for the current constexpr evaluation
	 * @return the computed evaluation
	 */
	ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context);

	/**
	 * Searches the evaluation for a usage of a template parameter which is a parameter pack,
	 * and returns the number of arguments bound to that parameter pack in the given
	 * template parameter map.
	 *
	 * Can also return one of the special values CPPTemplates.PACK_SIZE_DEFER,
	 * CPPTemplates.PACK_SIZE_FAIL, and CPPTemplates.PACK_SIZE_NOT_FOUND. See their
	 * declarations for their meanings.
	 *
	 * See also {@code CPPTemplates.determinePackSize()}.
	 */
	int determinePackSize(ICPPTemplateParameterMap tpMap);

	/**
	 * Checks if the evaluation references a template parameter either directly or though nested
	 * evaluations.
	 */
	boolean referencesTemplateParameter();

	/**
	 * If the evaluation is dependent (or instantiated from a dependent evaluation),
	 * returns the template definition in which the evaluation occurs.
	 * Otherwise returns {@code null}.
	 */
	IBinding getTemplateDefinition();

	/**
	 * Marshals an ICPPEvaluation object for storage in the index.
	 *
	 * @param  buffer The buffer that will hold the marshalled ICPPEvaluation object.
	 * @param  includeValue Specifies whether nested IValue objects should be marshalled as well.
	 * */
	void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException;
}
