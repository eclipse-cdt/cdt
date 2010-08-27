/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
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

import org.eclipse.cdt.core.dom.ast.EScopeKind;
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
	
	public EScopeKind getKind() {
		return EScopeKind.eNamespace;
	}

	public void addUsingDirective(ICPPUsingDirective directive) {
		fail();
	}

	public ICPPUsingDirective[] getUsingDirectives() {
		return new ICPPUsingDirective[0]; // same behavior as PDOMCPPNamespace
	}

	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		IBinding preresult = null;
		for(int i=0; preresult==null && i<namespaces.length; i++) {
			preresult = namespaces[i].getNamespaceScope().getBinding(name, resolve, fileSet);
		}
		return processUncertainBinding(preresult);
	}
	
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[namespaces.length][];
		for(int i=0; i<namespaces.length; i++) {
			IBinding[] raw = namespaces[i].getNamespaceScope().getBindings(name, resolve, prefixLookup, fileSet);
			preresult[i] = new IIndexFragmentBinding[raw.length];
			System.arraycopy(raw, 0, preresult[i], 0, raw.length);
		}
		return cf.getCompositeBindings(preresult);
	}
	
	final public IBinding[] find(String name) {
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
		for (ICPPNamespace namespace : namespaces) {
			if(namespace instanceof IIndexScope) {
				IIndexScope s= (IIndexScope) namespace;
				IIndexName nm= s.getScopeName();
				if(nm!=null)
					return nm;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope#getInlineNamespaces()
	 */
	public ICPPNamespaceScope[] getInlineNamespaces() {
		IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[namespaces.length][];
		for(int i=0; i<namespaces.length; i++) {
			ICPPNamespaceScope[] raw = namespaces[i].getNamespaceScope().getInlineNamespaces();
			IIndexFragmentBinding[] arr = preresult[i] = new IIndexFragmentBinding[raw.length];
			for (int j=0; j<raw.length; j++) {
				arr[j]= (IIndexFragmentBinding) ((IIndexScope) raw[j]).getScopeBinding();
			}
		}
		IIndexBinding[] compBinding = cf.getCompositeBindings(preresult);
		ICPPNamespaceScope[] result = new ICPPNamespaceScope[compBinding.length];
		for(int i=0; i<result.length; i++) {
			result[i]= ((ICPPNamespace) compBinding[i]).getNamespaceScope();
		}
		return result;
	}
}
