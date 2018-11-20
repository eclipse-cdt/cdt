/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeOfUnknownMember;

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
	public boolean isConstexpr() {
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
		return new TypeOfUnknownMember(this);
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}

	@Override
	public int getFieldPosition() {
		return -1;
	}
}
