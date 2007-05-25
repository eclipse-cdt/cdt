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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author Bryan Wilkinson
 */
public class ArrayTypeClone implements IIndexType, IArrayType, ITypeContainer {
	private final IArrayType delegate;
	private IType type = null;

	public ArrayTypeClone(IArrayType array) {
		this.delegate = array;
	}
	public boolean isSameType(IType type) {
		if( type instanceof ITypedef )
			return ((ITypedef)type).isSameType( this );

		if( !( type instanceof IArrayType )) 
			return false;

		try {
			IType type1= this.getType();
			if( type1 == null )
				return false;

			IArrayType rhs = (IArrayType) type;
			return type1.isSameType( rhs.getType() );
		} catch (DOMException e) {
		}
		return false;
	}
	public IASTExpression getArraySizeExpression() throws DOMException {
		return delegate.getArraySizeExpression();
	}
	public IType getType() throws DOMException {
		if (type == null) {
			return delegate.getType();
		}
		return type;
	}
	public void setType(IType type) {
		this.type = type;
	}
	public Object clone() {
		return new ArrayTypeClone(this);
	}
}
