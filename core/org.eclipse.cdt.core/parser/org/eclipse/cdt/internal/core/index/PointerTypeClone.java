/*******************************************************************************
 * Copyright (c) 2007, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

public class PointerTypeClone implements IPointerType, ITypeContainer, IIndexType {
	protected final IPointerType delegate;
	private IType type;
	
	public PointerTypeClone(IPointerType pointer) {
		this.delegate = pointer;
	}

	public IType getType() {
		if (type == null) {
			return delegate.getType();
		}
		return type;
	}

	public boolean isConst() {
		return delegate.isConst();
	}

	public boolean isVolatile() {
		return delegate.isVolatile();
	}

	public boolean isSameType(IType type) {
		if (type instanceof ITypedef)
		    return ((ITypedef)type).isSameType(this);
		
		if (!(type instanceof IPointerType)) 
		    return false;
		
	    if (this instanceof ICPPPointerToMemberType != type instanceof ICPPPointerToMemberType) 
	        return false;

		IPointerType rhs = (IPointerType) type;
		if (isConst() == rhs.isConst() && isVolatile() == rhs.isVolatile()) {
			IType type1= getType();
			if (type1 != null) {
				return type1.isSameType(rhs.getType());
			}
		}
		return false;
	}

	public void setType(IType type) {
		this.type = type;
	}

	@Override
	public Object clone() {
		return new PointerTypeClone(this);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
