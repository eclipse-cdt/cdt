/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNestedClassType;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPNestedClassType extends CompositeCPPClassType implements ICPPNestedClassType {

	@Override
	public int getVisibility() {
		return ((ICPPField)rbinding).getVisibility();
	}

	@Override
	public ICPPClassType getClassOwner() {
		IIndexFragmentBinding rowner = (IIndexFragmentBinding) ((ICPPNestedClassType)rbinding).getClassOwner();
		return (ICPPClassType) cf.getCompositeBinding(rowner);
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public IType getType() throws DOMException {
		IType rtype = ((ICPPNestedClassType) rbinding).getType();
		return cf.getCompositeType(rtype);
	}

	public CompositeCPPNestedClassType(ICompositesFactory cf, ICPPNestedClassType rbinding) {
		super(cf, rbinding);
	}
}
