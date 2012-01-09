/*******************************************************************************
 * Copyright (c) 2006, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPNamespace extends CompositeCPPBinding implements ICPPNamespace {
	ICPPNamespace[] namespaces;
	public CompositeCPPNamespace(ICompositesFactory cf, ICPPNamespace[] namespaces) {
		super(cf, namespaces[0]);
		this.namespaces = namespaces;
	}

	@Override
	public IBinding[] getMemberBindings() {
		IIndexFragmentBinding[][] memberBindings = new IIndexFragmentBinding[namespaces.length][];
		for(int i=0; i<namespaces.length; i++) {
			IBinding[] bindings = namespaces[i].getMemberBindings();
			memberBindings[i] = new IIndexFragmentBinding[bindings.length];
			System.arraycopy(bindings, 0, memberBindings[i], 0, bindings.length);
		}
		return cf.getCompositeBindings(memberBindings);
	}

	@Override
	public ICPPNamespaceScope getNamespaceScope() {
		return new CompositeCPPNamespaceScope(cf, namespaces);
	}

	@Override
	public boolean isInline() {
		for (ICPPNamespace namespace : namespaces) {
			if (namespace.isInline())
				return true;
		}
		return false;
	}
}
