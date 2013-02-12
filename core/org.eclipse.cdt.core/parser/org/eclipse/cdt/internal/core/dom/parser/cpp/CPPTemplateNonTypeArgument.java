/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupContext;
import org.eclipse.core.runtime.Assert;

/**
 * Implementation of non-type template argument, used by AST and index.
 */
public class CPPTemplateNonTypeArgument implements ICPPTemplateArgument {
	private final ICPPEvaluation fEvaluation;

	public CPPTemplateNonTypeArgument(ICPPEvaluation evaluation, LookupContext context) {
		Assert.isNotNull(evaluation);
		if (evaluation instanceof EvalFixed || context == null || context.getPointOfInstantiation() == null ||
				evaluation.isTypeDependent() || evaluation.isValueDependent()) {
			fEvaluation= evaluation;
		} else {
			fEvaluation= new EvalFixed(evaluation.getTypeOrFunctionSet(context),
					evaluation.getValueCategory(context), evaluation.getValue(context));
		}
	}

	public CPPTemplateNonTypeArgument(IValue value, IType type) {
		fEvaluation = new EvalFixed(type, PRVALUE, value);
	}

	@Override
	public boolean isTypeValue() {
		return false;
	}

	@Override
	public IType getOriginalTypeValue() {
		return null;
	}

	@Override
	public boolean isNonTypeValue() {
		return true;
	}

	@Override
	public IType getTypeValue() {
		return null;
	}

	@Override
	public ICPPEvaluation getNonTypeEvaluation() {
		return fEvaluation;
	}

	@Override
	public IValue getNonTypeValue() {
		return fEvaluation.getValue(null);
	}

	@Override
	public IType getTypeOfNonTypeValue() {
		return fEvaluation.getTypeOrFunctionSet((IASTNode)null);
	}

	@Override
	public boolean isPackExpansion() {
		return fEvaluation.getTypeOrFunctionSet((IASTNode)null) instanceof ICPPParameterPackType;
	}

	@Override
	public ICPPTemplateArgument getExpansionPattern() {
		IType type = fEvaluation.getTypeOrFunctionSet((IASTNode)null);
		if (type instanceof ICPPParameterPackType) {
			IType t= ((ICPPParameterPackType) type).getType();
			if (t != null) {
				ICPPEvaluation evaluation;
				if (fEvaluation instanceof EvalFixed) {
					EvalFixed fixed = (EvalFixed) fEvaluation;
					evaluation = new EvalFixed(t, fixed.getValueCategory(), fixed.getValue());
				} else {
					evaluation = new EvalTypeId(t, fEvaluation);
				}
				return new CPPTemplateNonTypeArgument(evaluation, null);
			}
		}
		return null;
	}

	@Override
	public boolean isSameValue(ICPPTemplateArgument arg) {
		return getNonTypeValue().equals(arg.getNonTypeValue());
	}

	@Override
	public String toString() {
		return getNonTypeValue().toString();
	}
}
