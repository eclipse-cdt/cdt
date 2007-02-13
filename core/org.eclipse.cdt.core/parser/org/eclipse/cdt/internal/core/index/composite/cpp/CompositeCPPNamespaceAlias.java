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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPNamespaceAlias extends CompositeCPPBinding implements ICPPNamespaceAlias {
	public CompositeCPPNamespaceAlias(ICompositesFactory cf, ICPPNamespaceAlias alias) {
		super(cf, alias);
	}
	
	public IBinding[] getMemberBindings() throws DOMException {
		fail(); return null;
	}

	public ICPPNamespaceScope getNamespaceScope() throws DOMException {
		fail(); return null;
	}

	public IBinding getBinding() {
		IIndexFragmentBinding ns = (IIndexFragmentBinding) ((ICPPNamespaceAlias)rbinding).getBinding();
		return cf.getCompositeBinding(ns);
	}

	public int getDelegateType() {
		fail(); return 0;
	}
}
