/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

/**
 * Represents a reference to a (member) function (instance), which cannot be resolved because 
 * it depends on a template parameter. A compiler would resolve it during instantiation.
 */
public class CPPUnknownFunction extends CPPUnknownBinding implements ICPPFunction {

	public static ICPPFunction createForSample(IFunction sample) throws DOMException {
		if (sample instanceof ICPPConstructor)
			return new CPPUnknownConstructor(((ICPPConstructor) sample).getClassOwner());
		
		return new CPPUnknownFunction(sample.getOwner(), sample.getNameCharArray());
	}

	private ICPPFunctionType fType;

	public CPPUnknownFunction(IBinding owner, char[] name) {
		super(owner, name);
	}

	public IType[] getExceptionSpecification() {
		return null;
	}

	public boolean isDeleted() {
		return false;
	}

	public boolean isExternC() {
		return false;
	}

	public boolean isInline() {
		return false;
	}

	public boolean isMutable() {
		return false;
	}

	public IScope getFunctionScope() throws DOMException {
		return asScope();
	}

	public ICPPParameter[] getParameters() {
		return ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
	}

	public ICPPFunctionType getType() {
		if (fType == null) {
			fType= new CPPUnknownFunctionType();
		}
		return fType;
	}

	public boolean isAuto() {
		return false;
	}

	public boolean isExtern() {
		return false;
	}

	public boolean isRegister() {
		return false;
	}

	public boolean isStatic() {
		return false;
	}

	public boolean takesVarArgs() {
		return false;
	}

	public int getRequiredArgumentCount() {
		return 0;
	}

	public boolean hasParameterPack() {
		return false;
	}
}
