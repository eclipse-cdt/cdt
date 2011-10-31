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
	
	public IScope getParent() {
		return parent;
	}

	public void setParent(IScope parent) {
		this.parent = parent;
	}
	
	public IASTNode getPhysicalNode() {
		return physicalNode;
	}

	public void setPhysicalNode(IASTNode physicalNode) {
		this.physicalNode = physicalNode;
	}
	
	public final EScopeKind getKind() {
		return kind;
	}

	public IName getScopeName() {
		return scopeName;
	}

	public void setScopeName(IName scopeName) {
		this.scopeName = scopeName;
	}
	
	
	public IBinding[] find( String name) {
		throw new UnsupportedOperationException();
	}

	public IBinding getBinding(IASTName name, boolean resolve) {
		throw new UnsupportedOperationException();
	}

	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) {
		throw new UnsupportedOperationException();
	}

	
	
	
	public void addBinding(IBinding binding) {
		throw new UnsupportedOperationException();
	}

	public void addName(IASTName name) {
		throw new UnsupportedOperationException();
	}

	public void populateCache() {
	}

	public void removeNestedFromCache(IASTNode container) {
	}

	public IBinding getBinding(IASTName name, boolean resolve,
			IIndexFileSet acceptLocalBindings) {
		// TODO Auto-generated method stub
		return null;
	}

	public IBinding[] getBindings(IASTName name, boolean resolve,
			boolean prefixLookup, IIndexFileSet acceptLocalBindings) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
