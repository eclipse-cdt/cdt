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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.PlatformObject;

@SuppressWarnings("restriction")
public class C99Enumeration extends PlatformObject implements IC99Binding, IEnumeration, ITypeable {

	private List<IEnumerator> enumerators = new ArrayList<>();
	private String name;

	private IScope scope;

	public C99Enumeration() {
	}

	public C99Enumeration(String name) {
		this.name = name;
	}

	public void addEnumerator(IEnumerator e) {
		enumerators.add(e);
	}

	@Override
	public IEnumerator[] getEnumerators() {
		return enumerators.toArray(new IEnumerator[enumerators.size()]);
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public char[] getNameCharArray() {
		return name.toCharArray();
	}

	@Override
	public IType getType() {
		return this;
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef)
			return type.isSameType(this);

		return false;
	}

	@Override
	public C99Enumeration clone() {
		try {
			C99Enumeration clone = (C99Enumeration) super.clone();
			clone.enumerators = new ArrayList<>();
			for (IEnumerator e : enumerators) {
				// TODO this is wrong,
				// IEnumerator is not Cloneable so we are not returning a deep copy here
				clone.addEnumerator(e);
			}
			return clone;
		} catch (CloneNotSupportedException e1) {
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
	public long getMinValue() {
		return SemanticUtil.computeMinValue(this);
	}

	@Override
	public long getMaxValue() {
		return SemanticUtil.computeMaxValue(this);
	}
}
