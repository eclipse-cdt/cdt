/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.CompositeIndexBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

abstract class CompositeCPPBinding extends CompositeIndexBinding implements ICPPBinding {
	public CompositeCPPBinding(ICompositesFactory cf, IBinding rbinding) {
		super(cf, (IIndexFragmentBinding) rbinding);
	}

	@Override
	public String[] getQualifiedName() {
		try {
			return ((ICPPBinding) rbinding).getQualifiedName();
		} catch (DOMException e) {
			CCorePlugin.log(e);
			return new String[0];
		}
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		return ((ICPPBinding) rbinding).getQualifiedNameCharArray();
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		return ((ICPPBinding) rbinding).isGloballyQualified();
	}
}
