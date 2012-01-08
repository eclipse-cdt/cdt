/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPMethod extends CompositeCPPFunction implements ICPPMethod {

	public CompositeCPPMethod(ICompositesFactory cf, ICPPFunction rbinding) {
		super(cf, rbinding);
	}

	@Override
	public boolean isDestructor() {
		return ((ICPPMethod)rbinding).isDestructor();
	}

	@Override
	public boolean isImplicit() {
		return ((ICPPMethod)rbinding).isImplicit();
	}

	@Override
	public boolean isExplicit() {
		return ((ICPPMethod)rbinding).isExplicit();
	}

	@Override
	public boolean isVirtual() {
		return ((ICPPMethod)rbinding).isVirtual();
	}

	@Override
	public ICPPClassType getClassOwner() {
		IIndexFragmentBinding rowner = (IIndexFragmentBinding) ((ICPPMethod)rbinding).getClassOwner();
		return (ICPPClassType) cf.getCompositeBinding(rowner);
	}

	@Override
	public int getVisibility() {
		return ((ICPPMethod)rbinding).getVisibility();
	}

	@Override
	public boolean isPureVirtual() {
		return ((ICPPMethod)rbinding).isPureVirtual();
	}
}
