/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Bryan Wilkinson (QNX)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * Base class for all specialization scopes
 */
public class AbstractCPPClassSpecializationScope implements ICPPClassSpecializationScope {
	final private ICPPClassSpecialization specialClass;

	public AbstractCPPClassSpecializationScope(ICPPClassSpecialization specialization) {
		this.specialClass= specialization;
	}

	public ICPPClassType getOriginalClassType() {
		return specialClass.getSpecializedBinding();
	}
		
	public final IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) throws DOMException {
		return getBindings(name, resolve, prefix, IIndexFileSet.EMPTY);
	}

	public IBinding getBinding(IASTName name, boolean forceResolve, IIndexFileSet fileSet) throws DOMException {
		char[] c = name.toCharArray();
		
	    if (CharArrayUtils.equals(c, specialClass.getNameCharArray()) && !CPPClassScope.isConstructorReference(name)) {
	    	return specialClass;
	    }

		ICPPClassType specialized = specialClass.getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		IBinding[] bindings = classScope != null ? classScope.getBindings(name, forceResolve, false) : null;
		
		if (bindings == null)
			return null;
    	
		IBinding[] specs = new IBinding[0];
		for (IBinding binding : bindings) {
			specs = (IBinding[]) ArrayUtil.append(IBinding.class, specs, specialClass.specializeMember(binding));
		}
		specs = (IBinding[]) ArrayUtil.trim(IBinding.class, specs);
    	return CPPSemantics.resolveAmbiguities(name, specs);
	}

	public IBinding[] getBindings(IASTName name, boolean forceResolve, boolean prefixLookup,
			IIndexFileSet fileSet) throws DOMException {
		char[] c = name.toCharArray();
		IBinding[] result = null;
		
	    if ((!prefixLookup && CharArrayUtils.equals(c, specialClass.getNameCharArray())) ||
	    		(prefixLookup && CharArrayUtils.equals(specialClass.getNameCharArray(), 0, c.length, c, true))) {
	    	result = new IBinding[] { specialClass };
	    }

		ICPPClassType specialized = specialClass.getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		IBinding[] bindings = classScope != null ?
				classScope.getBindings(name, forceResolve, prefixLookup, fileSet) : null;
		
		if (bindings != null) {
			for (IBinding binding : bindings) {
				result = (IBinding[]) ArrayUtil.append(IBinding.class, result, specialClass.specializeMember(binding));
			}
		}

		return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}
	
	public ICPPClassSpecialization getClassType() {
		return specialClass;
	}

	public ICPPMethod[] getImplicitMethods() {
		// Implicit methods shouldn't have implicit specializations
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public IName getScopeName() {
		if (specialClass instanceof ICPPInternalBinding)
			return (IASTName) ((ICPPInternalBinding) specialClass).getDefinition();
		//TODO: get the scope name for non-internal bindings
		return null;
	}

	public ICPPConstructor[] getConstructors() throws DOMException {
		ICPPClassType specialized = specialClass.getSpecializedBinding();
		ICPPConstructor[] bindings = specialized.getConstructors();
		
		if (bindings == null) return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		
    	ICPPConstructor[] specs = new ICPPConstructor[0];
		for (ICPPConstructor binding : bindings) {
			specs = (ICPPConstructor[]) ArrayUtil.append(ICPPConstructor.class, specs, specialClass.specializeMember(binding));
		}
		return (ICPPConstructor[]) ArrayUtil.trim(ICPPConstructor.class, specs);
	}
	
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		ICPPClassType specialized = specialClass.getSpecializedBinding();
		ICPPMethod[] bindings = specialized.getDeclaredMethods();
		
		if (bindings == null) return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		
		ICPPMethod[] specs = new ICPPMethod[0];
		for (ICPPMethod binding : bindings) {
			specs = (ICPPMethod[]) ArrayUtil.append(ICPPMethod.class, specs, specialClass.specializeMember(binding));
		}
		return (ICPPMethod[]) ArrayUtil.trim(ICPPMethod.class, specs);
	}

	public IScope getParent() throws DOMException {
		IBinding binding= specialClass.getOwner();
		if (binding instanceof ICPPClassType) {
			return ((ICPPClassType) binding).getCompositeScope();
		}
		if (binding instanceof ICPPNamespace) {
			return ((ICPPNamespace) binding).getNamespaceScope();
		}
		return getOriginalClassType().getScope();
	}

	public IBinding[] find(String name) throws DOMException {
		return CPPSemantics.findBindings(this, name, false);
	}

	@Override
	public String toString() {
		IName name = getScopeName();
		return name != null ? name.toString() : String.valueOf(specialClass);
	}

	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}
}
