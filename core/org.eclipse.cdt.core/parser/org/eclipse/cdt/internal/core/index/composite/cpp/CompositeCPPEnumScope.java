/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.CompositeScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPEnumScope extends CompositeScope implements ICPPScope {
	public CompositeCPPEnumScope(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	public EScopeKind getKind() {
		return EScopeKind.eEnumeration;
	}

	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		IBinding binding = ((ICPPEnumeration)rbinding).asScope().getBinding(name, resolve, fileSet);
		return processUncertainBinding(binding);
	}

	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		IBinding[] bindings = ((ICPPEnumeration)rbinding).asScope().getBindings(name, resolve, prefixLookup, fileSet);
		return processUncertainBindings(bindings);
	}
	
	public IBinding[] find(String name) {
		IBinding[] preresult = ((ICPPEnumeration)rbinding).asScope().find(name);
		return processUncertainBindings(preresult);	
	}
	
	public IIndexBinding getScopeBinding() {
		return cf.getCompositeBinding(rbinding);
	}
}
