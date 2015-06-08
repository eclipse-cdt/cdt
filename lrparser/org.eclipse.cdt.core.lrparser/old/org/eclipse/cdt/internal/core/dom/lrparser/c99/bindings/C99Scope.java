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

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;

/**
 * @author Mike Kucera
 *
 */
@SuppressWarnings({"restriction","unused"})
public class C99Scope implements IC99Scope, IASTInternalScope {
	private IScope parent;
	private IASTNode physicalNode;
	private IName scopeName;
	private final EScopeKind kind;
	
	public C99Scope(EScopeKind eKind) {
		kind= eKind;
	}
	
	@Override
	public IScope getParent() {
		return parent;
	}

	@Override
	public void setParent(IScope parent) {
		this.parent = parent;
	}
	
	@Override
	public IASTNode getPhysicalNode() {
		return physicalNode;
	}

	@Override
	public void setPhysicalNode(IASTNode physicalNode) {
		this.physicalNode = physicalNode;
	}
	
	@Override
	public final EScopeKind getKind() {
		return kind;
	}

	@Override
	public IName getScopeName() {
		return scopeName;
	}

	@Override
	public void setScopeName(IName scopeName) {
		this.scopeName = scopeName;
	}
	
	@Override
	public IBinding[] find(String name, IASTTranslationUnit tu) {
		throw new UnsupportedOperationException();
	}

	@Override @Deprecated
	public IBinding[] find(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void addBinding(IBinding binding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addName(IASTName name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void populateCache() {
	}

	@Override
	public void removeNestedFromCache(IASTNode container) {
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings) {
		return null;
	}

	/**
	 * @deprecated Use {@link #getBindings(ScopeLookupData)} instead
	 */
	@Override
	@Deprecated
	public IBinding[] getBindings(IASTName name, boolean resolve,
			boolean prefixLookup, IIndexFileSet acceptLocalBindings) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}
}
