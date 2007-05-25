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
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author Bryan Wilkinson
 */
public class PointerTypeClone implements IPointerType, ITypeContainer, IIndexType {
	protected final IPointerType delegate;
	private IType type = null;
	
	public PointerTypeClone(IPointerType pointer) {
		this.delegate = pointer;
	}
	public IType getType() throws DOMException {
		if (type == null) {
			return delegate.getType();
		}
		return type;
	}
	public boolean isConst() throws DOMException {
		return delegate.isConst();
	}
	public boolean isVolatile() throws DOMException {
		return delegate.isVolatile();
	}
	public boolean isSameType(IType type) {
		if( type instanceof ITypedef )
		    return ((ITypedef)type).isSameType( this );
		
		if( !( type instanceof IPointerType )) 
		    return false;
		
		IPointerType rhs = (IPointerType) type;
		try {
			if (isConst() == rhs.isConst() && isVolatile() == rhs.isVolatile()) {
				IType type1= getType();
				if (type1 != null) {
					return type1.isSameType(rhs.getType());
				}
			}
		} catch (DOMException e) {
		}
		return false;
	}
	public void setType(IType type) {
		this.type = type;
	}
	public Object clone() {
		return new PointerTypeClone(this);
	}
}
