/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.core.runtime.CoreException;

public class DeclaredBindingsFilter extends IndexFilter {
	final private String fLinkageID;
	final private boolean fAcceptImplicit;

	public DeclaredBindingsFilter() {
		this(null, false);
	}

	public DeclaredBindingsFilter(String linkageID, boolean acceptImplicit) {
		fLinkageID= linkageID;
		fAcceptImplicit= acceptImplicit;
	}
	
	public boolean acceptLinkage(ILinkage linkage) {
		return fLinkageID == null || fLinkageID.equals(linkage.getID());
	}

	public boolean acceptBinding(IBinding binding) throws CoreException {
		if (binding instanceof IIndexFragmentBinding) {
			return ((IIndexFragmentBinding) binding).hasDeclaration() ||
				(fAcceptImplicit && isImplicit(binding));
		}
		// composite bindings don't support that kind of check.
		return fAcceptImplicit || !isImplicit(binding);	
	}

	private boolean isImplicit(IBinding binding) {
		if (binding instanceof ICPPMethod) { 
			return ((ICPPMethod) binding).isImplicit();
		}
		return false;
	}
}
