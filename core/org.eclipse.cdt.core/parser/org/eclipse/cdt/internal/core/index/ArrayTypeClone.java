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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

public class ArrayTypeClone implements IIndexType, IArrayType, ITypeContainer {
	private final IArrayType delegate;
	private IType type;

	public ArrayTypeClone(IArrayType array) {
		this.delegate = array;
	}

	public boolean isSameType(IType type) {
		if (type instanceof ITypedef)
			return ((ITypedef) type).isSameType(this);

		if (!(type instanceof IArrayType)) 
			return false;

		IType type1= this.getType();
		if (type1 == null)
			return false;

		IArrayType rhs = (IArrayType) type;
		if (type1.isSameType(rhs.getType())) {
			IValue s1= getSize();
			IValue s2= rhs.getSize();
			if (s1 == s2)
				return true;
			if (s1 == null || s2 == null)
				return false;
			return CharArrayUtils.equals(s1.getSignature(), s2.getSignature());
		}
		return false;
	}

	public IValue getSize() {
		return delegate.getSize();
	}
	
	@Deprecated
	public IASTExpression getArraySizeExpression() throws DOMException {
		return delegate.getArraySizeExpression();
	}

	public IType getType() {
		if (type == null) {
			return delegate.getType();
		}
		return type;
	}

	public void setType(IType type) {
		this.type = type;
	}

	@Override
	public Object clone() {
		return new ArrayTypeClone(this);
	}
	
	@Override
	public String toString() {
		return delegate.toString();
	}
}
