/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;

/**
 * Represents a reference to a (member) function (instance), which cannot be resolved because 
 * it depends on a template parameter. A compiler would resolve it during instantiation.
 */
public class CPPUnknownFunction extends CPPUnknownBinding implements ICPPFunction {

	public static IFunction createForSample(IFunction sample, IASTName name) throws DOMException {
		return new CPPUnknownFunction(sample.getOwner(), name.getLastName());
	}

	private ICPPFunctionType fType;

	public CPPUnknownFunction(IBinding owner, IASTName name) {
		super(owner, name);
	}

	public IType[] getExceptionSpecification() throws DOMException {
		return null;
	}

	public boolean isExternC() throws DOMException {
		return false;
	}

	public boolean isInline() throws DOMException {
		return false;
	}

	public boolean isMutable() throws DOMException {
		return false;
	}

	public IScope getFunctionScope() throws DOMException {
		return asScope();
	}

	public IParameter[] getParameters() throws DOMException {
		return IParameter.EMPTY_PARAMETER_ARRAY;
	}

	public ICPPFunctionType getType() throws DOMException {
		if (fType == null) {
			fType= new CPPUnknownFunctionType();
		}
		return fType;
	}

	public boolean isAuto() throws DOMException {
		return false;
	}

	public boolean isExtern() throws DOMException {
		return false;
	}

	public boolean isRegister() throws DOMException {
		return false;
	}

	public boolean isStatic() throws DOMException {
		return false;
	}

	public boolean takesVarArgs() throws DOMException {
		return false;
	}
}
