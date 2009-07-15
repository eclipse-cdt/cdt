/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.index.CPPPointerToMemberTypeClone;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.CompositePointerType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPPointerToMemberType extends CompositePointerType implements ICPPPointerToMemberType {
	
	CompositeCPPPointerToMemberType(ICompositesFactory cf, ICPPPointerToMemberType pointerToMemberType) {
		super(pointerToMemberType, cf);
	}
	
	public IType getMemberOfClass() {
		IIndexFragmentBinding rbinding = (IIndexFragmentBinding) ((ICPPPointerToMemberType) type).getMemberOfClass();
		return (IType) cf.getCompositeBinding(rbinding);
	}
	
	@Override
	public Object clone() {
		return new CPPPointerToMemberTypeClone(this);
	}
}
