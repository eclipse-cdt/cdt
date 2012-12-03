/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;

/**
 * Represents a reference to a (member) function (instance), which cannot be resolved because 
 * an argument depends on a template parameter. A compiler would resolve it during instantiation.
 */
public class CPPDeferredFunction extends CPPUnknownBinding implements ICPPFunction {
	private static final ICPPFunctionType FUNCTION_TYPE=
			new CPPFunctionType(ProblemType.UNKNOWN_FOR_EXPRESSION, IType.EMPTY_TYPE_ARRAY);

	public static ICPPFunction createForSample(IFunction sample) throws DOMException {
		if (sample instanceof ICPPConstructor)
			return new CPPUnknownConstructor(((ICPPConstructor) sample).getClassOwner());
		
		final IBinding owner = sample.getOwner();
		return new CPPDeferredFunction(owner, sample.getNameCharArray());
	}

	private final IBinding fOwner;

	public CPPDeferredFunction(IBinding owner, char[] name) {
		super(name);
		fOwner= owner;
	}

	@Override
	public IType[] getExceptionSpecification() {
		return null;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isInline() {
		return false;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public IScope getFunctionScope() {
		return asScope();
	}

	@Override
	public ICPPParameter[] getParameters() {
		return ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
	}

	@Override
	public ICPPFunctionType getType() {
		return FUNCTION_TYPE;
	}

	@Override
	public boolean isAuto() {
		return false;
	}

	@Override
	public boolean isExtern() {
		return false;
	}

	@Override
	public boolean isRegister() {
		return false;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean takesVarArgs() {
		return false;
	}

	@Override
	public boolean isNoReturn() {
		return false;
	}

	@Override
	public int getRequiredArgumentCount() {
		return 0;
	}

	@Override
	public boolean hasParameterPack() {
		return false;
	}
	
	@Override
	public IBinding getOwner() {
		return fOwner;
	}
}
