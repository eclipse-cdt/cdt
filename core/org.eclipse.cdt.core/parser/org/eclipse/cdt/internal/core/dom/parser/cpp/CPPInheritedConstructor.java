/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType.UNSPECIFIED_TYPE;

import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Binding for a constructor inherited from a base class (12.9).
 */
public class CPPInheritedConstructor extends CPPImplicitMethod implements ICPPConstructor {
	private final ICPPConstructor prototype;

	public CPPInheritedConstructor(ICPPClassScope scope, char[] name, ICPPConstructor prototype,
			ICPPParameter[] params) {
		super(scope, name, createFunctionType(params), params);
		this.prototype = prototype;
    }

	private static ICPPFunctionType createFunctionType(IParameter[] params) {
		return CPPVisitor.createImplicitFunctionType(UNSPECIFIED_TYPE, params, false, false);
	}

	@Override
	public boolean isDestructor() {
		return false;
	}

	@Override
	public boolean isImplicit() {
		return true;
	}

	@Override
	public int getVisibility() {
		return prototype.getVisibility();
	}

	@Override
	public boolean isExplicit() {
		return prototype.isExplicit();
	}

	@Override
	public boolean isDeleted() {
		return prototype.isDeleted();
	}

	@Override
	public boolean isConstexpr() {
		return prototype.isConstexpr();
	}

	@Override
	public IType[] getExceptionSpecification() {
		return prototype.getExceptionSpecification();
	}
}
