/*******************************************************************************
 * Copyright (c) 2012, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.Context;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.UDCMode;
import org.eclipse.core.runtime.CoreException;

public abstract class CPPEvaluation implements ICPPEvaluation {

	CPPEvaluation() {
	}

	@Override
	public IBinding getTemplateDefinition() {
		return null;
	}

	@Override
	public char[] getSignature() {
		SignatureBuilder buf = new SignatureBuilder();
		try {
			marshal(buf, true);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[] { '?' };
		}
		return buf.getSignature();
	}

	public static IType getType(ICPPASTExpression expr) {
		CPPSemantics.pushLookupPoint(expr);
		try {
			return expr.getEvaluation().getType();
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	public static ValueCategory getValueCategory(ICPPASTExpression expr) {
		CPPSemantics.pushLookupPoint(expr);
		try {
			return expr.getEvaluation().getValueCategory();
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	protected static IBinding resolveUnknown(ICPPUnknownBinding unknown, InstantiationContext context) {
		try {
			return CPPTemplates.resolveUnknown(unknown, context);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return unknown;
	}

	protected static ICPPTemplateArgument[] instantiateArguments(ICPPTemplateArgument[] args,
			InstantiationContext context, boolean strict) {
		try {
			return CPPTemplates.instantiateArguments(args, context, strict);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return args;
	}

	protected static IBinding instantiateBinding(IBinding binding, InstantiationContext context, int maxDepth) {
		try {
			return CPPTemplates.instantiateBinding(binding, context, maxDepth);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return binding;
	}

	protected static boolean containsDependentType(ICPPEvaluation[] evaluations) {
		for (ICPPEvaluation eval : evaluations) {
			if (eval.isTypeDependent())
				return true;
		}
		return false;
	}

	protected static boolean containsDependentValue(ICPPEvaluation[] evaluations) {
		for (ICPPEvaluation eval : evaluations) {
			if (eval.isValueDependent())
				return true;
		}
		return false;
	}

	/**
	 * Checks if all evaluations contained in the given array are constant expressions.
	 *
	 * @param evaluations the evaluations to check
	 */
	protected static boolean areAllConstantExpressions(ICPPEvaluation[] evaluations) {
		return areAllConstantExpressions(evaluations, 0, evaluations.length);
	}

	/**
	 * Checks if all evaluations contained in a range of the given array are constant expressions.
	 *
	 * @param evaluations the evaluations to check
	 * @param from the initial index of the range to be checked, inclusive
	 * @param to the final index of the range to be checked, exclusive
	 */
	protected static boolean areAllConstantExpressions(ICPPEvaluation[] evaluations, int from, int to) {
		for (int i = from; i < to; i++) {
			if (!evaluations[i].isConstantExpression()) {
				return false;
			}
		}
		return true;
	}

	protected static boolean isConstexprValue(IValue value) {
		if (value == null) {
			return false;
		}
		ICPPEvaluation innerEval = value.getEvaluation();
		if (innerEval == null) {
			if (value instanceof IntegralValue) {
				return value.numberValue() != null;
			} else {
				return true;
			}
		}
		return innerEval.isConstantExpression();
	}

	protected static boolean isNullOrConstexprFunc(ICPPFunction function) {
		return function == null || function.isConstexpr();
	}

	/**
	 * If a user-defined conversion is required to convert 'argument' to type 'targetType',
	 * returns 'argument' wrapped in an evaluation representing the conversion.
	 * Otherwise, returns 'argument' unmodified.
	 *
	 * @param argument the evaluation to convert
	 * @param targetType the type to convert to
	 * @param allowContextualConversion enable/disable explicit contextual conversion
	 * @param onlyConstexprConversion allow only constexpr conversion operators
	 */
	protected static ICPPEvaluation maybeApplyConversion(ICPPEvaluation argument, IType targetType,
			boolean allowContextualConversion, boolean onlyConstexprConversion) {
		if (targetType == null) {
			return argument;
		}

		IType type = argument.getType();

		// Types match - don't bother to check for conversions.
		if (targetType.isSameType(type)) {
			return argument;
		}

		try {
			// Source type is class type - check for conversion operator.
			IType uqType = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
			ValueCategory valueCategory = argument.getValueCategory();
			if (uqType instanceof ICPPClassType) {
				Cost cost = Conversions.initializationByConversion(valueCategory, type, (ICPPClassType) uqType,
						targetType, false, allowContextualConversion);
				ICPPFunction conversion = cost.getUserDefinedConversion();
				if (conversion != null) {
					if (onlyConstexprConversion && !conversion.isConstexpr()) {
						return EvalFixed.INCOMPLETE;
					}
					ICPPEvaluation eval = new EvalMemberAccess(uqType, valueCategory, conversion, argument, false,
							CPPSemantics.getCurrentLookupPoint());
					return new EvalFunctionCall(new ICPPEvaluation[] { eval }, null, (IBinding) null);
				}
			}

			// Source type is not a class type, or is but a conversion operator wasn't used.
			// Check for standard conversions.
			if (!Conversions.checkImplicitConversionSequence(targetType, type, valueCategory, UDCMode.FORBIDDEN,
					Context.ORDINARY).converts()) {
				return EvalFixed.INCOMPLETE;
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return argument;
	}

	protected static boolean areEquivalentOrNull(ICPPEvaluation a, ICPPEvaluation b) {
		if (a == null) {
			return (b == null);
		}
		return a.isEquivalentTo(b);
	}

	protected static boolean areEquivalentEvaluations(ICPPEvaluation[] a, ICPPEvaluation[] b) {
		if (a == null) {
			return (b == null);
		}
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; i++) {
			if (!a[i].isEquivalentTo(b[i])) {
				return false;
			}
		}
		return true;
	}

	protected static boolean areEquivalentArguments(ICPPTemplateArgument[] a, ICPPTemplateArgument[] b) {
		if (a == null) {
			return (b == null);
		}
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; i++) {
			if (!a[i].isSameValue(b[i])) {
				return false;
			}
		}
		return true;
	}

	protected static boolean areEquivalentTypes(IType[] a, IType[] b) {
		if (a == null) {
			return (b == null);
		}
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; i++) {
			if (!a[i].isSameType(b[i])) {
				return false;
			}
		}
		return true;
	}

	protected static <T extends IBinding> boolean areEquivalentBindings(T[] a, T[] b) {
		if (a == null) {
			return (b == null);
		}
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; i++) {
			if (!a[i].equals(b[i])) {
				return false;
			}
		}
		return true;
	}

}