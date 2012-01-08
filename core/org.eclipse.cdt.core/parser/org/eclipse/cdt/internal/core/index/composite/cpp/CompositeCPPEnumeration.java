/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPEnumeration extends CompositeCPPBinding implements ICPPEnumeration, IIndexType {
	public CompositeCPPEnumeration(ICompositesFactory cf, ICPPEnumeration rbinding) {
		super(cf, rbinding);
	}

	@Override
	public IEnumerator[] getEnumerators() throws DOMException {
		IEnumerator[] result = ((IEnumeration)rbinding).getEnumerators();
		for (int i= 0; i < result.length; i++)
			result[i] = (IEnumerator) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		return result;
	}

	@Override
	public boolean isSameType(IType type) {
		return ((IEnumeration)rbinding).isSameType(type);
	}
	
	@Override
	public Object clone() { fail(); return null; }

	@Override
	public String toString() {
		return getName();
	}
	@Override
	public long getMinValue() {
		return ((IEnumeration)rbinding).getMinValue();
	}

	@Override
	public long getMaxValue() {
		return ((IEnumeration)rbinding).getMaxValue();
	}

	@Override
	public boolean isScoped() {
		return ((ICPPEnumeration)rbinding).isScoped();
	}

	@Override
	public IType getFixedType() {
		return ((ICPPEnumeration)rbinding).getFixedType();
	}

	@Override
	public ICPPScope asScope() {
		return new CompositeCPPEnumScope(cf, rbinding);
	}
}
