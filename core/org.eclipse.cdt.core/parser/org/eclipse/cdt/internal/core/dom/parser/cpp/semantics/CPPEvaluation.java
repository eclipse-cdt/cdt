/*******************************************************************************
 * Copyright (c) 2012, 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
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

	protected static IBinding resolveUnknown(ICPPUnknownBinding unknown, InstantiationContext context) {
		try {
			return CPPTemplates.resolveUnknown(unknown, context);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return unknown;
	}

	protected static ICPPTemplateArgument[] instantiateArguments(ICPPTemplateArgument[] args,
			InstantiationContext context) {
		try {
			return CPPTemplates.instantiateArguments(args, context, false);
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

	protected static boolean areAllConstantExpressions(ICPPEvaluation[] evaluations, IASTNode point) {
		for (ICPPEvaluation eval : evaluations) {
			if (!eval.isConstantExpression(point)) {
				return false;
			}
		}
		return true;
	}

	protected static boolean isConstexprValue(IValue value, IASTNode point) {
		if (value == null) {
			return false;
		}
		ICPPEvaluation innerEval = value.getEvaluation();
		if (innerEval == null) {
			return value.numericalValue() != null;
		}
		return innerEval.isConstantExpression(point);
	}

	protected static boolean isNullOrConstexprFunc(ICPPFunction function) {
		return function == null || function.isConstexpr();
	}

	/**
	 * If a user-defined conversion is required to convert 'argument' to type 'targetType',
	 * return 'argument' wrapped in an evaluation representing the conversion.
	 * Otherwise, return 'argument' unmodified.
	 * @param point point of instantiation for name lookups
	 */
	protected static ICPPEvaluation maybeApplyConversion(ICPPEvaluation argument, IType targetType, 
			IASTNode point) {
		IType type = argument.getType(point);
		ValueCategory valueCategory = argument.getValueCategory(point);
		ICPPFunction conversion = null;
		if (type instanceof ICPPClassType) {
			try {
				Cost cost = Conversions.initializationByConversion(valueCategory, type, (ICPPClassType) type, 
						targetType, false, point);
				conversion = cost.getUserDefinedConversion();
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}
		if (conversion != null) {
			if (!conversion.isConstexpr()) {
				return EvalFixed.INCOMPLETE;
			}
			ICPPEvaluation eval = new EvalBinding(conversion, null, (IBinding) null);
			argument = new EvalFunctionCall(new ICPPEvaluation[] {eval, argument}, (IBinding) null);
		}
		return argument;
	}
}