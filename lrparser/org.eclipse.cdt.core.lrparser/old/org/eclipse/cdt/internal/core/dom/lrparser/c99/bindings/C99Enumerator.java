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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.core.runtime.PlatformObject;

@SuppressWarnings("restriction")
public class C99Enumerator extends PlatformObject implements IC99Binding, IEnumerator, ITypeable {

	private String name;
	private IType type;
	private IScope scope;

	public C99Enumerator() {
	}

	public C99Enumerator(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public IType getType() {
		return type;
	}

	public void setType(IType type) {
		this.type = type;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	@Override
	public char[] getNameCharArray() {
		return name.toCharArray();
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
		if (type instanceof IBinding)
			return (IBinding) type;
		return null;
	}

	@Override
	public IValue getValue() {
		return IntegralValue.UNKNOWN;
	}
}
