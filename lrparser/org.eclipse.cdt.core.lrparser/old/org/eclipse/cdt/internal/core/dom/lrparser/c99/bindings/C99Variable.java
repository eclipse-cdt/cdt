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
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.core.runtime.PlatformObject;

@SuppressWarnings("restriction")
public class C99Variable extends PlatformObject implements IC99Binding, IVariable, ITypeable {
	private boolean isAuto;
	private boolean isExtern;
	private boolean isRegister;
	private boolean isStatic;

	private String name;
	private IType type;

	private IScope scope;

	public C99Variable() {
	}

	public C99Variable(String name) {
		this.name = name;
	}

	public void setType(IType type) {
		this.type = type;
	}

	@Override
	public IType getType() {
		return type;
	}

	public void setAuto(boolean auto) {
		this.isAuto = auto;
	}

	@Override
	public boolean isAuto() {
		return isAuto;
	}

	public void setExtern(boolean extern) {
		this.isExtern = extern;
	}

	@Override
	public boolean isExtern() {
		return isExtern;
	}

	public void setRegister(boolean isRegister) {
		this.isRegister = isRegister;
	}

	@Override
	public boolean isRegister() {
		return isRegister;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
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
		if (scope == null)
			return null;

		return CVisitor.findDeclarationOwner((IASTNode) scope.getScopeName(), true);
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}
}
