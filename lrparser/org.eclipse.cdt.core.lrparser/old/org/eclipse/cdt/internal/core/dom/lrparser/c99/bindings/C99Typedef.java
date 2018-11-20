/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.core.runtime.PlatformObject;

@SuppressWarnings("restriction")
public class C99Typedef extends PlatformObject implements IC99Binding, ITypedef, ITypeContainer, ITypeable {
	private IType type;
	private String name;

	private IScope scope;

	public C99Typedef() {
	}

	public C99Typedef(IType type) {
		this.type = type;
	}

	public C99Typedef(IType type, String name) {
		this.type = type;
		this.name = name;
	}

	@Override
	public IType getType() {
		return type;
	}

	@Override
	public void setType(IType type) {
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public char[] getNameCharArray() {
		return name.toCharArray();
	}

	@Override
	public boolean isSameType(IType t) {
		if (t == this)
			return true;

		if (t instanceof ITypedef)
			return type.isSameType(((ITypedef) t).getType());
		return type.isSameType(t);
	}

	@Override
	public C99Typedef clone() {
		try {
			C99Typedef clone = (C99Typedef) super.clone();
			clone.type = (IType) type.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			assert false;
			return null;
		}
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	@Override
	public IScope getScope() {
		return scope;
	}

	@Override
	public void setScope(IScope scope) {
		this.scope = scope;
	}

	@Override
	public IBinding getOwner() {
		if (scope != null) {
			return CVisitor.findEnclosingFunction((IASTNode) scope.getScopeName()); // local or global
		}
		return null;
	}

	@Override
	public String toString() {
		return getName() + " -> " + ASTTypeUtil.getType(this, true); //$NON-NLS-1$
	}
}
