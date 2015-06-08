/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.CompositeScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPEnumScope extends CompositeScope implements ICPPEnumScope {
	public CompositeCPPEnumScope(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eEnumeration;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		IBinding binding = ((ICPPEnumeration)rbinding).asScope().getBinding(name, resolve, fileSet);
		return processUncertainBinding(binding);
	}

	@Deprecated	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		IBinding[] bindings = ((ICPPEnumeration) rbinding).asScope().getBindings(lookup);
		return processUncertainBindings(bindings);
	}

	@Override
	public IBinding[] find(String name, IASTTranslationUnit tu) {
		IBinding[] preresult = ((ICPPEnumeration) rbinding).asScope().find(name, tu);
		return processUncertainBindings(preresult);	
	}

	@Override @Deprecated
	public IBinding[] find(String name) {
		IBinding[] preresult = ((ICPPEnumeration) rbinding).asScope().find(name);
		return processUncertainBindings(preresult);	
	}

	@Override
	public IIndexBinding getScopeBinding() {
		return cf.getCompositeBinding(rbinding);
	}

	@Override
	public ICPPEnumeration getEnumerationType() {
		return (ICPPEnumeration) getScopeBinding();
	}
}
