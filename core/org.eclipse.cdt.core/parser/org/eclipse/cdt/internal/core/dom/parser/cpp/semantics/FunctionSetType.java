/*
 * FunctionSetType.java
 * Created on Sep 14, 2010
 *
 * Copyright 2010 Wind River Systems, Inc. All rights reserved.
 */

package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.Rank;

/**
 * Used during overload resolution as a place-holder for function sets.
 */
public class FunctionSetType implements IType {

	private ICPPFunction[] fFunctionSet;
	private boolean fPointerType;
	private IASTName fName;

	public FunctionSetType(ICPPFunction[] functions, IASTName name, boolean addressOf) {
		fName= name;
		fFunctionSet= functions;
		fPointerType= addressOf;
	}

	@Override
	public boolean isSameType(IType type) {
		return type == this;
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}

	public ValueCategory getValueCategory() {
		return fPointerType ? PRVALUE : LVALUE;
	}

	public Cost costForTarget(IType paramType) {
		IBinding result = CPPSemantics.resolveTargetedFunction(paramType, fName, fFunctionSet);
		if (result instanceof ICPPFunction && !(result instanceof IProblemBinding)) {
			Cost c= new Cost(paramType, paramType, Rank.IDENTITY);
			c.setSelectedFunction((ICPPFunction) result);
			return c;
		}
		return Cost.NO_CONVERSION;
	}

	public void applySelectedFunction(ICPPFunction selectedFunction) {
		if (selectedFunction != null) {
			fName.setBinding(selectedFunction);
		}
	}
	
	public ICPPFunction[] getFunctionSet() {
		return fFunctionSet;
	}

	public void setToUnknown() {
		fName.setBinding(new CPPUnknownFunction(null, fName.toCharArray()));
	}
}
