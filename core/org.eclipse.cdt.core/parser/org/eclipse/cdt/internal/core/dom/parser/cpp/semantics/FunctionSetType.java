/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.Rank;

/**
 * Used during overload resolution as a place-holder for function sets.
 */
public class FunctionSetType implements IType {
	private final CPPFunctionSet fFunctionSet;
	private final boolean fPointerType;

	public FunctionSetType(CPPFunctionSet set, boolean addressOf) {
		fFunctionSet = set;
		fPointerType = addressOf;
	}

	@Override
	public boolean isSameType(IType type) {
		return type instanceof FunctionSetType && fFunctionSet == ((FunctionSetType) type).fFunctionSet;
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}

	public ValueCategory getValueCategory() {
		return fPointerType ? PRVALUE : LVALUE;
	}

	public Cost costForTarget(IType paramType) {
		IBinding result = CPPSemantics.resolveTargetedFunction(paramType, fFunctionSet);
		if (result instanceof ICPPFunction && !(result instanceof IProblemBinding)) {
			Cost c = new Cost(paramType, paramType, Rank.IDENTITY);
			c.setSelectedFunction((ICPPFunction) result);
			return c;
		}
		return Cost.NO_CONVERSION;
	}

	public void applySelectedFunction(ICPPFunction selectedFunction) {
		fFunctionSet.applySelectedFunction(selectedFunction);
	}

	public CPPFunctionSet getFunctionSet() {
		return fFunctionSet;
	}

	public void setToUnknown() {
		fFunctionSet.setToUnknown();
	}
}
