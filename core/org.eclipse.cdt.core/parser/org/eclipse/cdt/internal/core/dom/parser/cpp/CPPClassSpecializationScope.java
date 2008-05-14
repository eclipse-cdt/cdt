/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Bryan Wilkinson (QNX)
 * Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * @author aniefer
 */
public class CPPClassSpecializationScope implements ICPPClassSpecializationScope, IASTInternalScope {
	private ObjectMap instanceMap = ObjectMap.EMPTY_MAP;
	final private ICPPSpecialization specialization;

	public CPPClassSpecializationScope(ICPPSpecialization specialization) {
		this.specialization = specialization;
	}

	public ICPPClassType getOriginalClassType() {
		return (ICPPClassType) specialization.getSpecializedBinding();
	}
	
	public IBinding getInstance(IBinding binding) {
		if (instanceMap.containsKey(binding)) {
			return (IBinding) instanceMap.get(binding);
		} else if (!(binding instanceof ICPPClassTemplatePartialSpecialization)) {
			IBinding spec = CPPTemplates.createSpecialization(this, binding, specialization.getArgumentMap());
			if (instanceMap == ObjectMap.EMPTY_MAP)
				instanceMap = new ObjectMap(2);
			instanceMap.put(binding, spec);
			return spec;
		}
		return null;
	}
	
	public final IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) throws DOMException {
		return getBindings(name, resolve, prefix, IIndexFileSet.EMPTY);
	}

	public IBinding getBinding(IASTName name, boolean forceResolve, IIndexFileSet fileSet) throws DOMException {
		char[] c = name.toCharArray();
		
	    if (CharArrayUtils.equals(c, specialization.getNameCharArray()) &&
	    		!CPPClassScope.isConstructorReference(name)) {
	    	return specialization;
	    }

		ICPPClassType specialized = (ICPPClassType) specialization.getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		IBinding[] bindings = classScope != null ? classScope.getBindings(name, forceResolve, false) : null;
		
		if (bindings == null)
			return null;
    	
		IBinding[] specs = new IBinding[0];
		for (IBinding binding : bindings) {
			specs = (IBinding[]) ArrayUtil.append(IBinding.class, specs, getInstance(binding));
		}
		specs = (IBinding[]) ArrayUtil.trim(IBinding.class, specs);
    	return CPPSemantics.resolveAmbiguities(name, specs);
	}

	public IBinding[] getBindings(IASTName name, boolean forceResolve, boolean prefixLookup,
			IIndexFileSet fileSet) throws DOMException {
		char[] c = name.toCharArray();
		IBinding[] result = null;
		
	    if ((!prefixLookup && CharArrayUtils.equals(c, specialization.getNameCharArray())) ||
	    		(prefixLookup && CharArrayUtils.equals(specialization.getNameCharArray(), 0, c.length, c, true))) {
	    	result = new IBinding[] { specialization };
	    }

		ICPPClassType specialized = (ICPPClassType) specialization.getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		IBinding[] bindings = classScope != null ?
				classScope.getBindings(name, forceResolve, prefixLookup, fileSet) : null;
		
		if (bindings != null) {
			for (IBinding binding : bindings) {
				result = (IBinding[]) ArrayUtil.append(IBinding.class, result, getInstance(binding));
			}
		}

		return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}
	
	public ICPPClassType getClassType() {
		return (ICPPClassType) specialization;
	}

	public ICPPMethod[] getImplicitMethods() {
		// Implicit methods shouldn't have implicit specializations
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public IName getScopeName() {
		if (specialization instanceof ICPPInternalBinding)
			return (IASTName) ((ICPPInternalBinding) specialization).getDefinition();
		//TODO: get the scope name for non-internal bindings
		return null;
	}

	protected ICPPConstructor[] getConstructors() throws DOMException {
		ICPPClassType specialized = (ICPPClassType) specialization.getSpecializedBinding();
		ICPPConstructor[] bindings = specialized.getConstructors();
		
		if (bindings == null) return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		
    	ICPPConstructor[] specs = new ICPPConstructor[0];
		for (ICPPConstructor binding : bindings) {
			specs = (ICPPConstructor[]) ArrayUtil.append(ICPPConstructor.class, specs, getInstance(binding));
		}
		return (ICPPConstructor[]) ArrayUtil.trim(ICPPConstructor.class, specs);
	}
	
	protected ICPPMethod[] getDeclaredMethods() throws DOMException {
		ICPPClassType specialized = (ICPPClassType) specialization.getSpecializedBinding();
		ICPPMethod[] bindings = specialized.getDeclaredMethods();
		
		if (bindings == null) return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		
		ICPPMethod[] specs = new ICPPMethod[0];
		for (ICPPMethod binding : bindings) {
			specs = (ICPPMethod[]) ArrayUtil.append(ICPPMethod.class, specs, getInstance(binding));
		}
		return (ICPPMethod[]) ArrayUtil.trim(ICPPMethod.class, specs);
	}

	public IScope getParent() throws DOMException {
		ICPPClassType cls = getOriginalClassType();
		ICPPClassScope scope = (ICPPClassScope)cls.getCompositeScope();
		if (scope != null)
			return scope.getParent();
		if (cls instanceof ICPPInternalBinding) {
			IASTNode[] nds = ((ICPPInternalBinding)cls).getDeclarations();
			if (nds != null && nds.length > 0)
				return CPPVisitor.getContainingScope(nds[0]);
		}
		return null;
	}

	public IBinding[] find(String name) throws DOMException {
		return CPPSemantics.findBindings(this, name, false);
	}

	public boolean isFullyCached() throws DOMException {
		ICPPScope origScope = (ICPPScope) getOriginalClassType().getCompositeScope();
		if (!ASTInternal.isFullyCached(origScope)) {
			try {
				CPPSemantics.lookupInScope(null, origScope, null);
			} catch (DOMException e) {
			}
		}
		return true;
	}
	
	// This scope does not cache its own names
	public void setFullyCached(boolean b) {}
	public void flushCache() {}
	public void addName(IASTName name) {}
	public IASTNode getPhysicalNode() { return null; }
	public void removeBinding(IBinding binding) {}
	public void addBinding(IBinding binding) {}

	@Override
	public String toString() {
		IName name = getScopeName();
		return name != null ? name.toString() : String.valueOf(specialization);
	}
}
