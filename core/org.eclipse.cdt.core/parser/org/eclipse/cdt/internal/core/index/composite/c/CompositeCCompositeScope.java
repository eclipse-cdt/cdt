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
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
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

	@Override
	public IBinding getBinding(char[] name) {
		fail();
		return null;
	}

	@Override
	public ICompositeType getCompositeType() {
		return (ICompositeType) cf.getCompositeBinding(rbinding);
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		IBinding binding = ((ICompositeType) rbinding).getCompositeScope().getBinding(name, resolve, fileSet);
		return processUncertainBinding(binding);
	}

	@Override
	@Deprecated
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		IBinding[] bindings = ((ICompositeType) rbinding).getCompositeScope().getBindings(lookup);
		return processUncertainBindings(bindings);
	}

	@Override
	public IBinding[] find(String name, IASTTranslationUnit tu) {
		IBinding[] preresult = ((ICompositeType) rbinding).getCompositeScope().find(name, tu);
		return processUncertainBindings(preresult);
	}

	@Override
	@Deprecated
	public IBinding[] find(String name) {
		IBinding[] preresult = ((ICompositeType) rbinding).getCompositeScope().find(name);
		return processUncertainBindings(preresult);
	}

	@Override
	public IIndexBinding getScopeBinding() {
		return (IIndexBinding) getCompositeType();
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}
}
