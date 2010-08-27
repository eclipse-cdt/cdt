/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPMethodInstance extends CompositeCPPFunctionInstance implements ICPPMethod {

	public CompositeCPPMethodInstance(ICompositesFactory cf, ICPPMethod rbinding) {
		super(cf, rbinding);
	}

	public boolean isDestructor() {
		return ((ICPPMethod)rbinding).isDestructor();
	}

	public boolean isImplicit() {
		return ((ICPPMethod)rbinding).isImplicit();
	}

	public boolean isVirtual() {
		return ((ICPPMethod)rbinding).isDestructor();
	}

	public ICPPClassType getClassOwner() {
		IIndexFragmentBinding rowner = (IIndexFragmentBinding) ((ICPPMethod)rbinding).getClassOwner();
		return (ICPPClassType) cf.getCompositeBinding(rowner);
	}

	public int getVisibility() {
		return ((ICPPMethod)rbinding).getVisibility();
	}

	public boolean isPureVirtual() {
		return ((ICPPMethod)rbinding).isPureVirtual();
	}
}
