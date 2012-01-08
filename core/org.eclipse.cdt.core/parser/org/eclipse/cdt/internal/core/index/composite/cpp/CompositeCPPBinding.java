/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
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
			return ((ICPPBinding)rbinding).getQualifiedName();
		} catch(DOMException de) {
			CCorePlugin.log(de);
			return new String[0];
		}
	}
	
	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		return ((ICPPBinding)rbinding).getQualifiedNameCharArray();
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		return ((ICPPBinding)rbinding).isGloballyQualified();
	}
}
