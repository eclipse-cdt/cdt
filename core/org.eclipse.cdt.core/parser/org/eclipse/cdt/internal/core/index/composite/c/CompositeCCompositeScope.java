/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.CompositeScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCCompositeScope extends CompositeScope implements ICCompositeTypeScope {

	public CompositeCCompositeScope(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	public IBinding getBinding(char[] name) {
		fail(); return null;
	}

	public ICompositeType getCompositeType() {
		return (ICompositeType) cf.getCompositeBinding(rbinding);
	}

	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		IBinding binding = ((ICompositeType)rbinding).getCompositeScope().getBinding(name, resolve, fileSet);
		return processUncertainBinding(binding);
	}
	
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		IBinding[] bindings = ((ICompositeType)rbinding).getCompositeScope().getBindings(name, resolve, prefixLookup, fileSet);
		return processUncertainBindings(bindings);
	}
	
	public IBinding[] find(String name) {
		IBinding[] preresult = ((ICompositeType)rbinding).getCompositeScope().find(name);
		return processUncertainBindings(preresult);	
	}
	
	public IIndexBinding getScopeBinding() {
		return (IIndexBinding) getCompositeType();
	}

	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}
}
