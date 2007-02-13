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
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPEnumeration extends CompositeCPPBinding implements IEnumeration, IIndexType {
	public CompositeCPPEnumeration(ICompositesFactory cf, IEnumeration rbinding) {
		super(cf, (ICPPBinding) rbinding);
	}

	public IEnumerator[] getEnumerators() throws DOMException {
		IEnumerator[] result = ((IEnumeration)rbinding).getEnumerators();
		for(int i=0; i<result.length; i++)
			result[i] = (IEnumerator) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		return result;
	}

	public boolean isSameType(IType type) {
		return ((IEnumeration)rbinding).isSameType(type);
	}
	
	public Object clone() { fail(); return null; }
}
