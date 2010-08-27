/*******************************************************************************
 * Copyright (c) 2008, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
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

	public ICPPClassType getOriginalClassType() {
		return specialization().getSpecializedBinding();
	}

	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

	public ICPPClassSpecialization getClassType() {
		return (ICPPClassSpecialization) cf.getCompositeBinding(rbinding);
	}

	public IIndexBinding getScopeBinding() {
		return (IIndexBinding) getClassType();
	}

	private void createDelegate() {
		if (fDelegate == null) {
			fDelegate= new AbstractCPPClassSpecializationScope(specialization()) {};
		}
	}

	public ICPPMethod[] getImplicitMethods() {
		createDelegate();
		return fDelegate.getImplicitMethods();
	}

	public IBinding[] find(String name) {
		createDelegate();
		return fDelegate.find(name);
	}

	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings) {
		createDelegate();
		return fDelegate.getBinding(name, resolve, acceptLocalBindings);
	}

	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup,
			IIndexFileSet acceptLocalBindings) {
		createDelegate();
		return fDelegate.getBindings(name, resolve, prefixLookup, acceptLocalBindings);
	}

	public ICPPConstructor[] getConstructors() {
		createDelegate();
		return fDelegate.getConstructors();
	}

	public ICPPMethod[] getDeclaredMethods() {
		createDelegate();
		return fDelegate.getDeclaredMethods();
	}

	public ICPPBase[] getBases() {
		createDelegate();
		return fDelegate.getBases();
	}

	public ICPPField[] getDeclaredFields() {
		createDelegate();
		return fDelegate.getDeclaredFields();
	}

	public IBinding[] getFriends() {
		createDelegate();
		return fDelegate.getFriends();
	}

	public ICPPClassType[] getNestedClasses() {
		createDelegate();
		return fDelegate.getNestedClasses();
	}
}
