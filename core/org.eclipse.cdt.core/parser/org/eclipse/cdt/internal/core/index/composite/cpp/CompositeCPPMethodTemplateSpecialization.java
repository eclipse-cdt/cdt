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
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPMethodTemplateSpecialization extends CompositeCPPFunctionTemplateSpecialization
		implements ICPPMethod {
	public CompositeCPPMethodTemplateSpecialization(ICompositesFactory cf, ICPPFunction ft) {
		super(cf, ft);
	}

	@Override
	public boolean isDestructor() {
		return ((ICPPMethod) rbinding).isDestructor();
	}

	@Override
	public boolean isImplicit() {
		return ((ICPPMethod) rbinding).isImplicit();
	}

	@Override
	public boolean isExplicit() {
		return ((ICPPMethod) rbinding).isExplicit();
	}

	@Override
	public boolean isVirtual() {
		return ((ICPPMethod) rbinding).isVirtual();
	}

	@Override
	public ICPPClassType getClassOwner() {
		IIndexFragmentBinding rowner = (IIndexFragmentBinding) ((ICPPMethod) rbinding).getClassOwner();
		return (ICPPClassType) cf.getCompositeBinding(rowner);
	}

	@Override
	public int getVisibility() {
		return ((ICPPMethod) rbinding).getVisibility();
	}

	@Override
	public boolean isPureVirtual() {
		return ((ICPPMethod) rbinding).isPureVirtual();
	}

	@Override
	public boolean isOverride() {
		return ((ICPPMethod) rbinding).isOverride();
	}

	@Override
	public boolean isFinal() {
		return ((ICPPMethod) rbinding).isFinal();
	}
}
