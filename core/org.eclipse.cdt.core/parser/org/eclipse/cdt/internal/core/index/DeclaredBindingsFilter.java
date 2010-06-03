/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.index.composite.CompositeIndexBinding;
import org.eclipse.core.runtime.CoreException;

public class DeclaredBindingsFilter extends IndexFilter {
	final private int fLinkageID;
	final private boolean fAcceptImplicit;
	final private boolean fAllowInstances;

	public DeclaredBindingsFilter() {
		this(-1, false, true);
	}
	
	public DeclaredBindingsFilter(int linkageID, boolean acceptImplicit, boolean allowInstances) {
		fLinkageID= linkageID;
		fAcceptImplicit= acceptImplicit;
		fAllowInstances= allowInstances;
	}
	
	@Override
	public boolean acceptLinkage(ILinkage linkage) {
		return fLinkageID == -1 || fLinkageID == linkage.getLinkageID();
	}

	@Override
	public boolean acceptBinding(IBinding binding) throws CoreException {
		if (!fAllowInstances && binding instanceof ICPPTemplateInstance)
			return false;
		
		if (binding instanceof IIndexFragmentBinding) {
			return  ((IIndexFragmentBinding) binding).hasDeclaration()
					|| (fAcceptImplicit && isImplicit(binding));
		}
		// composite bindings don't support that kind of check.
		if (binding instanceof CompositeIndexBinding) {
			IIndexBinding raw= ((CompositeIndexBinding) binding).getRawBinding();
			if (raw instanceof IIndexFragmentBinding) {
				if (((IIndexFragmentBinding) raw).hasDeclaration()) {
					return true;
				}
			}
		}
		return fAcceptImplicit || !isImplicit(binding);	
	}

	private boolean isImplicit(IBinding binding) {
		if (binding instanceof ICPPSpecialization)
			return true;
		if (binding instanceof ICPPMethod) { 
			return ((ICPPMethod) binding).isImplicit();
		}
		return false;
	}
}
