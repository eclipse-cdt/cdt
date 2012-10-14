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

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;

/**
 * Represents a reference to a field, which cannot be resolved because the owner is
 * unknown. A compiler would resolve it during instantiation.
 */
public class CPPUnknownField extends CPPUnknownMember implements ICPPField {
	public CPPUnknownField(IType owner, char[] name) {
		super(owner, name);
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isMutable() {
		return false;
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
	public int getVisibility() {
		return v_public;
	}
	@Override
	public ICPPClassType getClassOwner() {
		IType owner = getOwnerType();
		if (owner instanceof ICPPClassType)
			return (ICPPClassType) owner;
		return null;
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		IType owner = getOwnerType();
		if (owner instanceof ICompositeType)
			return (ICompositeType) owner;
		return null;
	}

	@Override
	public IType getType() {
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}
}
