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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
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
			fDelegate= new AbstractCPPClassSpecializationScope(specialization()) {};
		}
	}

	@Override
	public ICPPMethod[] getImplicitMethods() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getImplicitMethods(null);
	}

	@Override
	public ICPPMethod[] getImplicitMethods(IASTNode point) {
		createDelegate();
		return fDelegate.getImplicitMethods(point);
	}

	@Override
	public IBinding[] find(String name) {
		createDelegate();
		return fDelegate.find(name);
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings) {
		createDelegate();
		return fDelegate.getBinding(name, resolve, acceptLocalBindings);
	}

	@Deprecated	@Override 
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
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getConstructors(null);
	}

	@Override
	public ICPPConstructor[] getConstructors(IASTNode point) {
		createDelegate();
		return fDelegate.getConstructors(point);
	}

	@Override
	public ICPPMethod[] getDeclaredMethods(IASTNode point) {
		createDelegate();
		return fDelegate.getDeclaredMethods(point);
	}

	@Override
	public ICPPBase[] getBases(IASTNode point) {
		createDelegate();
		return fDelegate.getBases(point);
	}

	@Override
	public ICPPField[] getDeclaredFields(IASTNode point) {
		createDelegate();
		return fDelegate.getDeclaredFields(point);
	}

	@Override
	public IBinding[] getFriends(IASTNode point) {
		createDelegate();
		return fDelegate.getFriends(point);
	}

	@Override
	public ICPPClassType[] getNestedClasses(IASTNode point) {
		createDelegate();
		return fDelegate.getNestedClasses(point);
	}
}
