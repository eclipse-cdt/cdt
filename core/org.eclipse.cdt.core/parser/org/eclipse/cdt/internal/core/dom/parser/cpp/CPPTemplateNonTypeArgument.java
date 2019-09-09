/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalPackExpansion;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;
import org.eclipse.core.runtime.Assert;

/**
 * Implementation of non-type template argument, used by AST and index.
 */
public final class CPPTemplateNonTypeArgument implements ICPPTemplateArgument {
	private final ICPPEvaluation fEvaluation;

	public CPPTemplateNonTypeArgument(ICPPEvaluation evaluation) {
		Assert.isNotNull(evaluation);
		if (evaluation instanceof EvalFixed || evaluation.isTypeDependent() || evaluation.isValueDependent()) {
			fEvaluation = evaluation;
		} else {
			IValue value = evaluation.getValue();
			if (value == IntegralValue.ERROR) {
				fEvaluation = EvalFixed.INCOMPLETE;
			} else {
				fEvaluation = new EvalFixed(evaluation.getType(), evaluation.getValueCategory(), value);
			}
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
		return fEvaluation.getValue();
	}

	@Override
	public IType getTypeOfNonTypeValue() {
		return fEvaluation.getType();
	}

	@Override
	public boolean isPackExpansion() {
		return fEvaluation.getType() instanceof ICPPParameterPackType;
	}

	@Override
	public ICPPTemplateArgument getExpansionPattern() {
		IType type = fEvaluation.getType();
		if (type instanceof ICPPParameterPackType) {
			IType t = ((ICPPParameterPackType) type).getType();
			if (t != null) {
				ICPPEvaluation evaluation;
				if (fEvaluation instanceof EvalPackExpansion) {
					evaluation = ((EvalPackExpansion) fEvaluation).getExpansionPattern();
				} else {
					evaluation = new EvalTypeId(t, fEvaluation.getTemplateDefinition(), false, fEvaluation);
				}
				return new CPPTemplateNonTypeArgument(evaluation);
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
