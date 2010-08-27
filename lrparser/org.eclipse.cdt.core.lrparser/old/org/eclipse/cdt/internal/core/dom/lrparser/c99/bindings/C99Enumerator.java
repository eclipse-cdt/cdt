/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.cdt.internal.core.dom.parser.Value;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IType getType() {
		return type;
	}

	public void setType(IType type) {
		this.type = type;
	}

	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	public char[] getNameCharArray() {
		return name.toCharArray();
	}

	public IScope getScope() {
		return scope;
	}

	public void setScope(IScope scope) {
		this.scope = scope;
	}

	public IBinding getOwner() {
		if (type instanceof IBinding)
			return (IBinding) type;
		return null;
	}

	public IValue getValue() {
		return Value.UNKNOWN;
	}
}
