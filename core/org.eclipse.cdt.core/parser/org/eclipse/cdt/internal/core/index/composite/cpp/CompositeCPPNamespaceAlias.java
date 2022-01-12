/*******************************************************************************
 * Copyright (c) 2007, 2013 Symbian Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

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

	@Override
	public IBinding[] getMemberBindings() {
		IBinding[] result = ((ICPPNamespaceAlias) rbinding).getMemberBindings();
		for (int i = 0; i < result.length; i++) {
			result[i] = cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	@Override
	public ICPPNamespaceScope getNamespaceScope() {
		return (ICPPNamespaceScope) cf
				.getCompositeScope((IIndexScope) ((ICPPNamespaceAlias) rbinding).getNamespaceScope());
	}

	@Override
	public IBinding getBinding() {
		IIndexFragmentBinding ns = (IIndexFragmentBinding) ((ICPPNamespaceAlias) rbinding).getBinding();
		return cf.getCompositeBinding(ns);
	}

	@Override
	public boolean isInline() {
		return false;
	}
}
