/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;

public class CPPPointerToMemberTypeClone extends PointerTypeClone implements ICPPPointerToMemberType {
	public CPPPointerToMemberTypeClone(ICPPPointerToMemberType pointer) {
		super(pointer);
	}

	public IType getMemberOfClass() {
		return ((ICPPPointerToMemberType) delegate).getMemberOfClass();
	}

	@Override
	public Object clone() {
		return new CPPPointerToMemberTypeClone((ICPPPointerToMemberType) delegate);
	}
	
	@Override
	public boolean isSameType(IType o) {
		if (o instanceof ITypedef)
			return o.isSameType(this);

		if (!(o instanceof ICPPPointerToMemberType))
			return false;

		if (!super.isSameType(o))
			return false;

		ICPPPointerToMemberType pt = (ICPPPointerToMemberType) o;
		IType cls = pt.getMemberOfClass();
		if (cls != null)
			return cls.isSameType(getMemberOfClass());
		
		return false;
	}
}