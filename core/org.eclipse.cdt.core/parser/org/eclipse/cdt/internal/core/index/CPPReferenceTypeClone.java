/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author Bryan Wilkinson
 */
public class CPPReferenceTypeClone implements ICPPReferenceType, ITypeContainer, IIndexType {
	private final ICPPReferenceType delegate;
	private IType type = null;

	public CPPReferenceTypeClone(ICPPReferenceType reference) {
		this.delegate = reference;
	}
	public IType getType() throws DOMException {
		if (type == null) {
			return delegate.getType();
		}
		return type;
	}
	public boolean isSameType(IType type) {
		if( type instanceof ITypedef )
			return type.isSameType(this);

		if( !( type instanceof ICPPReferenceType )) 
			return false;

		ICPPReferenceType rhs = (ICPPReferenceType) type;
		try {
			IType type1= getType();
			if (type1 != null) {
				return type1.isSameType(rhs.getType());
			}
		} catch (DOMException e) {
		}
		return false;
	}
	public void setType(IType type) {
		this.type = type;
	}
	public Object clone() {
		return new CPPReferenceTypeClone(this);
	}
}
