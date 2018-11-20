/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Represents a C++ class, declaration of which is not yet available.
 */
public class CPPUnknownMemberClass extends CPPUnknownMember implements ICPPUnknownMemberClass {
	public CPPUnknownMemberClass(IType owner, char[] name) {
		super(owner, name);
	}

	@Override
	public ICPPBase[] getBases() {
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	@Override
	public IField[] getFields() {
		return IField.EMPTY_FIELD_ARRAY;
	}

	@Override
	public IField findField(String name) {
		return null;
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	@Override
	public ICPPMethod[] getMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	@Override
	public IBinding[] getFriends() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public int getKey() {
		return 0;
	}

	@Override
	public final IScope getCompositeScope() {
		return asScope();
	}

	@Override
	public boolean isSameType(IType type) {
		if (this == type)
			return true;

		if (type instanceof ITypedef)
			return type.isSameType(this);

		if (type instanceof ICPPUnknownMemberClass && !(type instanceof ICPPUnknownMemberClassInstance)) {
			ICPPUnknownMemberClass rhs = (ICPPUnknownMemberClass) type;
			if (CharArrayUtils.equals(getNameCharArray(), rhs.getNameCharArray())) {
				final IType lhsContainer = getOwnerType();
				final IType rhsContainer = rhs.getOwnerType();
				if (lhsContainer != null && rhsContainer != null) {
					return lhsContainer.isSameType(rhsContainer);
				}
			}
		}
		return false;
	}

	@Override
	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations() {
		return ICPPUsingDeclaration.EMPTY_USING_DECL_ARRAY;
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public int getVisibility(IBinding member) {
		throw new IllegalArgumentException(member.getName() + " is not a member of " + getName()); //$NON-NLS-1$
	}
}
