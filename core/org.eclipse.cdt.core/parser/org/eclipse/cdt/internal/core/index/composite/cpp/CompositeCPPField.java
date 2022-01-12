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
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPField extends CompositeCPPVariable implements ICPPField {
	public CompositeCPPField(ICompositesFactory cf, ICPPField rbinding) {
		super(cf, rbinding);
	}

	@Override
	public ICPPClassType getClassOwner() {
		IIndexFragmentBinding rowner = (IIndexFragmentBinding) ((ICPPField) rbinding).getClassOwner();
		return (ICPPClassType) cf.getCompositeBinding(rowner);
	}

	@Override
	public int getVisibility() {
		return ((ICPPField) rbinding).getVisibility();
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		IBinding preresult = ((IField) rbinding).getCompositeTypeOwner();
		return (ICompositeType) cf.getCompositeBinding((IIndexFragmentBinding) preresult);
	}

	@Override
	public int getFieldPosition() {
		return ((ICPPField) rbinding).getFieldPosition();
	}
}
