/*******************************************************************************
 * Copyright (c) 2007, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

public class CPPReferenceTypeClone implements ICPPReferenceType, ITypeContainer, IIndexType {
	private final ICPPReferenceType delegate;
	private IType type;

	public CPPReferenceTypeClone(ICPPReferenceType reference) {
		this.delegate = reference;
	}

	public IType getType() {
		if (type == null) {
			return delegate.getType();
		}
		return type;
	}

	public boolean isSameType(IType type) {
		if (type instanceof ITypedef)
			return type.isSameType(this);

		if (!(type instanceof ICPPReferenceType)) 
			return false;

		ICPPReferenceType rhs = (ICPPReferenceType) type;
		IType type1= getType();
		if (type1 != null) {
			return type1.isSameType(rhs.getType());
		}
		return false;
	}

	public void setType(IType type) {
		this.type = type;
	}

	@Override
	public Object clone() {
		return new CPPReferenceTypeClone(this);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
