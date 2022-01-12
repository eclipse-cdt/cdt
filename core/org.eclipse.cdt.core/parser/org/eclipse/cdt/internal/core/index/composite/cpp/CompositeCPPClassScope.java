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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.CompositeScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPClassScope extends CompositeScope implements ICPPClassScope {
	public CompositeCPPClassScope(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

	@Override
	public ICPPClassType getClassType() {
		return (ICPPClassType) cf.getCompositeBinding(rbinding);
	}

	@Override
	public ICPPMethod[] getImplicitMethods() {
		ICPPClassScope rscope = (ICPPClassScope) ((ICPPClassType) rbinding).getCompositeScope();
		ICPPMethod[] result = rscope.getImplicitMethods();
		for (int i = 0; i < result.length; i++) {
			result[i] = (ICPPMethod) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		ICPPClassScope rscope = (ICPPClassScope) ((ICPPClassType) rbinding).getCompositeScope();
		ICPPConstructor[] result = rscope.getConstructors();
		for (int i = 0; i < result.length; i++) {
			result[i] = (ICPPConstructor) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		IBinding binding = ((ICPPClassType) rbinding).getCompositeScope().getBinding(name, resolve, fileSet);
		return processUncertainBinding(binding);
	}

	@Override
	@Deprecated
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		IBinding[] bindings = ((ICPPClassType) rbinding).getCompositeScope().getBindings(lookup);
		return processUncertainBindings(bindings);
	}

	@Override
	public IBinding[] find(String name, IASTTranslationUnit tu) {
		IBinding[] preresult = ((ICPPClassType) rbinding).getCompositeScope().find(name, tu);
		return processUncertainBindings(preresult);
	}

	@Override
	@Deprecated
	public IBinding[] find(String name) {
		IBinding[] preresult = ((ICPPClassType) rbinding).getCompositeScope().find(name);
		return processUncertainBindings(preresult);
	}

	@Override
	public IIndexBinding getScopeBinding() {
		return (IIndexBinding) getClassType();
	}
}
