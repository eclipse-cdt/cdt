/*******************************************************************************
 * Copyright (c) 2008, 2015 Symbian Software Systems and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.AbstractCPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.CompositeScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPClassSpecializationScope extends CompositeScope implements ICPPClassSpecializationScope {
	private ICPPClassSpecializationScope fDelegate;

	public CompositeCPPClassSpecializationScope(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	private ICPPClassSpecialization specialization() {
		return (ICPPClassSpecialization) cf.getCompositeBinding(rbinding);
	}

	@Override
	public ICPPClassType getOriginalClassType() {
		return specialization().getSpecializedBinding();
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

	@Override
	public ICPPClassSpecialization getClassType() {
		return (ICPPClassSpecialization) cf.getCompositeBinding(rbinding);
	}

	@Override
	public IIndexBinding getScopeBinding() {
		return (IIndexBinding) getClassType();
	}

	private void createDelegate() {
		if (fDelegate == null) {
			fDelegate = new AbstractCPPClassSpecializationScope(specialization()) {
			};
		}
	}

	@Override
	public ICPPMethod[] getImplicitMethods() {
		createDelegate();
		return fDelegate.getImplicitMethods();
	}

	@Override
	public IBinding[] find(String name, IASTTranslationUnit tu) {
		createDelegate();
		return fDelegate.find(name, tu);
	}

	@Override
	@Deprecated
	public IBinding[] find(String name) {
		createDelegate();
		return fDelegate.find(name);
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings) {
		createDelegate();
		return fDelegate.getBinding(name, resolve, acceptLocalBindings);
	}

	@Deprecated
	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup,
			IIndexFileSet acceptLocalBindings) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		createDelegate();
		return fDelegate.getBindings(lookup);
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		createDelegate();
		return fDelegate.getConstructors();
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		createDelegate();
		return fDelegate.getDeclaredMethods();
	}

	@Override
	public ICPPBase[] getBases() {
		createDelegate();
		return fDelegate.getBases();
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		createDelegate();
		return fDelegate.getDeclaredFields();
	}

	@Override
	public IBinding[] getFriends() {
		createDelegate();
		return fDelegate.getFriends();
	}

	@Override
	public ICPPClassType[] getNestedClasses() {
		createDelegate();
		return fDelegate.getNestedClasses();
	}

	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations() {
		createDelegate();
		return fDelegate.getUsingDeclarations();
	}
}
