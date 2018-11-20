/*******************************************************************************
 * Copyright (c) 2007, 2015 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
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

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eNamespace;
	}

	@Override
	public void addUsingDirective(ICPPUsingDirective directive) {
		fail();
	}

	@Override
	public ICPPUsingDirective[] getUsingDirectives() {
		return new ICPPUsingDirective[0]; // same behavior as PDOMCPPNamespace
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		IBinding preresult = null;
		for (int i = 0; preresult == null && i < namespaces.length; i++) {
			preresult = namespaces[i].getNamespaceScope().getBinding(name, resolve, fileSet);
		}
		return processUncertainBinding(preresult);
	}

	@Deprecated
	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[namespaces.length][];
		for (int i = 0; i < namespaces.length; i++) {
			IBinding[] raw = namespaces[i].getNamespaceScope().getBindings(lookup);
			preresult[i] = new IIndexFragmentBinding[raw.length];
			System.arraycopy(raw, 0, preresult[i], 0, raw.length);
		}
		return cf.getCompositeBindings(preresult);
	}

	@Override
	final public IBinding[] find(String name, IASTTranslationUnit tu) {
		IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[namespaces.length][];
		for (int i = 0; i < namespaces.length; i++) {
			IBinding[] raw = namespaces[i].getNamespaceScope().find(name, tu);
			preresult[i] = new IIndexFragmentBinding[raw.length];
			System.arraycopy(raw, 0, preresult[i], 0, raw.length);
		}
		return cf.getCompositeBindings(preresult);
	}

	@Override
	@Deprecated
	final public IBinding[] find(String name) {
		IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[namespaces.length][];
		for (int i = 0; i < namespaces.length; i++) {
			IBinding[] raw = namespaces[i].getNamespaceScope().find(name);
			preresult[i] = new IIndexFragmentBinding[raw.length];
			System.arraycopy(raw, 0, preresult[i], 0, raw.length);
		}
		return cf.getCompositeBindings(preresult);
	}

	@Override
	public IIndexBinding getScopeBinding() {
		return cf.getCompositeBinding(rbinding);
	}

	@Override
	public IIndexName getScopeName() {
		for (ICPPNamespace namespace : namespaces) {
			if (namespace instanceof IIndexScope) {
				IIndexScope s = (IIndexScope) namespace;
				IIndexName nm = s.getScopeName();
				if (nm != null)
					return nm;
			}
		}
		return null;
	}

	@Override
	public ICPPNamespaceScope[] getInlineNamespaces() {
		IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[namespaces.length][];
		for (int i = 0; i < namespaces.length; i++) {
			ICPPNamespaceScope[] raw = namespaces[i].getNamespaceScope().getInlineNamespaces();
			IIndexFragmentBinding[] arr = preresult[i] = new IIndexFragmentBinding[raw.length];
			for (int j = 0; j < raw.length; j++) {
				arr[j] = (IIndexFragmentBinding) ((IIndexScope) raw[j]).getScopeBinding();
			}
		}
		IIndexBinding[] compBinding = cf.getCompositeBindings(preresult);
		ICPPNamespaceScope[] result = new ICPPNamespaceScope[compBinding.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = ((ICPPNamespace) compBinding[i]).getNamespaceScope();
		}
		return result;
	}
}
