/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.composite.CompositeScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPNamespaceScope extends CompositeScope implements ICPPNamespaceScope {
	ICPPNamespace[] namespaces;
	
	public CompositeCPPNamespaceScope(ICompositesFactory cf, ICPPNamespace[] namespaces) {
		super(cf, (IIndexFragmentBinding) namespaces[0]);
		this.namespaces = namespaces;
	}
	
	public void addUsingDirective(ICPPUsingDirective directive) throws DOMException {
		fail();
	}

	public ICPPUsingDirective[] getUsingDirectives() throws DOMException {
		return new ICPPUsingDirective[0]; // same behavior as PDOMCPPNamespace
	}

	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet)
	throws DOMException {
		IBinding preresult = null;
		for(int i=0; preresult==null && i<namespaces.length; i++) {
			preresult = namespaces[i].getNamespaceScope().getBinding(name, resolve, fileSet);
		}
		return processUncertainBinding(preresult);
	}
	
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet)
			throws DOMException {
		IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[namespaces.length][];
		for(int i=0; i<namespaces.length; i++) {
			IBinding[] raw = namespaces[i].getNamespaceScope().getBindings(name, resolve, prefixLookup, fileSet);
			preresult[i] = new IIndexFragmentBinding[raw.length];
			System.arraycopy(raw, 0, preresult[i], 0, raw.length);
		}
		return cf.getCompositeBindings(preresult);
	}
	
	final public IBinding[] find(String name) throws DOMException {
		IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[namespaces.length][];
		for(int i=0; i<namespaces.length; i++) {
			IBinding[] raw = namespaces[i].getNamespaceScope().find(name);
			preresult[i] = new IIndexFragmentBinding[raw.length];
			System.arraycopy(raw, 0, preresult[i], 0, raw.length);
		}
		return cf.getCompositeBindings(preresult);
	}
	
	public IIndexBinding getScopeBinding() {
		return cf.getCompositeBinding(rbinding);
	}
	
	@Override
	public IIndexName getScopeName() {
		for(int i=0; i<namespaces.length; i++) {
			if(namespaces[i] instanceof IIndexScope) {
				IIndexScope s= (IIndexScope) namespaces[i];
				IIndexName nm= s.getScopeName();
				if(nm!=null)
					return nm;
			}
		}
		return null;
	}
}
