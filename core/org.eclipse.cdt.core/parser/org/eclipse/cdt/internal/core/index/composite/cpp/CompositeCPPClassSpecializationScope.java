/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPClassSpecializationScope extends CompositeCPPClassScope {
	private ObjectMap instanceMap = ObjectMap.EMPTY_MAP;
	
	public CompositeCPPClassSpecializationScope(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	private ICPPSpecialization specialization() {
		return (ICPPSpecialization) cf.getCompositeBinding(rbinding);
	}
	
	@Override
	public ICPPMethod[] getImplicitMethods() {
		// Implicit methods shouldn't have implicit specializations
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public IBinding[] find(String name) throws DOMException {
		return CPPSemantics.findBindings(this, name, false);
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings) throws DOMException {
		char[] c = name.toCharArray();
		
	    if (CharArrayUtils.equals(c, specialization().getNameCharArray()) &&
	    		!CPPClassScope.isConstructorReference(name)) {
	    	return specialization();
	    }

		ICPPClassType specialized = (ICPPClassType) specialization().getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		IBinding[] bindings = classScope != null ? classScope.getBindings(name, resolve, false) : null;
		
		if (bindings == null)
			return null;
    	
		IBinding[] specs = new IBinding[0];
		for (int i = 0; i < bindings.length; i++) {
			specs = (IBinding[]) ArrayUtil.append(IBinding.class, specs, getInstance(bindings[i]));
		}
		specs = (IBinding[]) ArrayUtil.trim(IBinding.class, specs);
    	return CPPSemantics.resolveAmbiguities(name, specs);
	}

	@Override
	public IBinding[] getBindings(IASTName name, boolean forceResolve, boolean prefixLookup,
			IIndexFileSet fileSet) throws DOMException {
		char[] c = name.toCharArray();
		IBinding[] result = null;
		
	    if ((!prefixLookup && CharArrayUtils.equals(c, specialization().getNameCharArray())) ||
	    		(prefixLookup && CharArrayUtils.equals(specialization().getNameCharArray(), 0, c.length, c, true))) {
	    	result = new IBinding[] { specialization() };
	    }

		ICPPClassType specialized = (ICPPClassType) specialization().getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		IBinding[] bindings = classScope != null ?
				classScope.getBindings(name, forceResolve, prefixLookup, fileSet) : null;
		
		if (bindings != null) {
			for (int i = 0; i < bindings.length; i++) {
				result = (IBinding[]) ArrayUtil.append(IBinding.class, result, getInstance(bindings[i]));
			}
		}

		return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}
	
	private IBinding getInstance(IBinding binding) {
		if (instanceMap.containsKey(binding)) {
			return (IBinding) instanceMap.get(binding);
		} else if (!(binding instanceof ICPPClassTemplatePartialSpecialization)) {
			IBinding spec = CPPTemplates.createSpecialization(this, binding, specialization().getArgumentMap());
			if (instanceMap == ObjectMap.EMPTY_MAP)
				instanceMap = new ObjectMap(2);
			instanceMap.put(binding, spec);
			return spec;
		}
		return null;
	}
}
