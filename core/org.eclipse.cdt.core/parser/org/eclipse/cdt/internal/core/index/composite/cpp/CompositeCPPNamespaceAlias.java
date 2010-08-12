/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPNamespaceAlias extends CompositeCPPBinding implements ICPPNamespaceAlias {
	public CompositeCPPNamespaceAlias(ICompositesFactory cf, ICPPNamespaceAlias alias) {
		super(cf, alias);
	}
	
	public IBinding[] getMemberBindings() throws DOMException {
		IBinding[] result= ((ICPPNamespaceAlias)rbinding).getMemberBindings();
		for(int i=0; i<result.length; i++) {
			result[i]= cf.getCompositeBinding((IIndexFragmentBinding)result[i]);
		}
		return result;
	}

	public ICPPNamespaceScope getNamespaceScope() throws DOMException {
		return (ICPPNamespaceScope) cf.getCompositeScope((IIndexScope) ((ICPPNamespaceAlias)rbinding).getNamespaceScope());
	}

	public IBinding getBinding() {
		IIndexFragmentBinding ns = (IIndexFragmentBinding) ((ICPPNamespaceAlias)rbinding).getBinding();
		return cf.getCompositeBinding(ns);
	}

	public boolean isInline() {
		return false;
	}
}
