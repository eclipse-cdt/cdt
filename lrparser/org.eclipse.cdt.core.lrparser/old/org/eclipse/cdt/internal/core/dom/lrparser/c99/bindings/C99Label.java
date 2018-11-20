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
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.core.runtime.PlatformObject;

@SuppressWarnings("restriction")
public class C99Label extends PlatformObject implements IC99Binding, ILabel {

	private String name;
	private IScope scope;

	public C99Label() {
	}

	public C99Label(String name) {
		this.name = name;
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
	public IASTLabelStatement getLabelStatement() {
		return null;
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
}
