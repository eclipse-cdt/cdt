/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.CompositeScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPClassScope extends CompositeScope implements ICPPClassScope {
	public CompositeCPPClassScope(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	public ICPPClassType getClassType() {
		return (ICPPClassType) cf.getCompositeBinding(rbinding);
	}

	public ICPPMethod[] getImplicitMethods() {
		try {
			ICPPClassScope rscope = (ICPPClassScope) ((ICPPClassType)rbinding).getCompositeScope();
			ICPPMethod[] result = rscope.getImplicitMethods();
			for(int i=0; i<result.length; i++) {
				result[i] = (ICPPMethod) cf.getCompositeBinding((IIndexFragmentBinding)result[i]);
			}
			return result;
		} catch (DOMException de) {
			CCorePlugin.log(de);
		}
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		IBinding binding = ((ICPPClassType)rbinding).getCompositeScope().getBinding(name, resolve);
		return processUncertainBinding(binding);
	}
	
	public IBinding[] find(String name, boolean prefixLookup)
	throws DOMException {
		IBinding[] preresult = ((ICPPClassType)rbinding).getCompositeScope().find(name, prefixLookup);
		return processUncertainBindings(preresult);
	}

	public IBinding[] find(String name) throws DOMException {
		IBinding[] preresult = ((ICPPClassType)rbinding).getCompositeScope().find(name);
		return processUncertainBindings(preresult);	
	}
	
	public IIndexBinding getScopeBinding() {
		return (IIndexBinding) getClassType();
	}
}
