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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexInternalTemplateParameter;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.core.runtime.CoreException;

public class CompositeCPPTemplateTypeParameter extends CompositeCPPBinding implements ICPPTemplateTypeParameter, IIndexType, IIndexInternalTemplateParameter {

	public CompositeCPPTemplateTypeParameter(ICompositesFactory cf,	ICPPTemplateTypeParameter binding) {
		super(cf, binding);
	}

	public IType getDefault() throws DOMException {
		IIndexType preresult= (IIndexType) ((ICPPTemplateTypeParameter)rbinding).getDefault();
		return cf.getCompositeType(preresult);
	}

	public boolean isSameType(IType type) {
		return ((IType)rbinding).isSameType(type);
	}

	public ICPPBinding getParameterOwner() throws CoreException {
		IIndexFragmentBinding preresult= (IIndexFragmentBinding) ((IIndexInternalTemplateParameter)rbinding).getParameterOwner();
		return (ICPPBinding) cf.getCompositeBinding(preresult);
	}
	
	public Object clone() {
		fail(); return null; 
	}
}
