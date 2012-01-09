/*******************************************************************************
 * Copyright (c) 2007, 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPUsingDeclaration extends CompositeCPPBinding implements ICPPUsingDeclaration {
	public CompositeCPPUsingDeclaration(ICompositesFactory cf, ICPPUsingDeclaration using) {
		super(cf, using);
	}
	
	public IBinding[] getMemberBindings() throws DOMException {
		fail(); return null;
	}

	@Override
	public IBinding[] getDelegates() {
		IBinding[] delegates = ((ICPPUsingDeclaration) rbinding).getDelegates();
		IBinding[] composites = new IBinding[delegates.length];
		int j = 0;
		for (int i = 0; i < delegates.length; i++) {
			IBinding binding = delegates[i];
			if (binding instanceof IIndexFragmentBinding) {
				composites[j++] = cf.getCompositeBinding((IIndexFragmentBinding) binding);
			}
		}
		return ArrayUtil.trim(IBinding.class, composites);
	}
}
