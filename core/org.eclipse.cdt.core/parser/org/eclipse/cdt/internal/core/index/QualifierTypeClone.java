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
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author Bryan Wilkinson
 */
public class QualifierTypeClone implements IQualifierType, ITypeContainer, IIndexType {
	private final IQualifierType delegate;
	private IType type = null;

	public QualifierTypeClone(IQualifierType qualifier) {
		this.delegate = qualifier;
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
			return type.isSameType( this );
		if( !( type instanceof IQualifierType ) ) 
			return false;

		IQualifierType pt = (IQualifierType) type;
		try {
			if( isConst() == pt.isConst() && isVolatile() == pt.isVolatile() ) {
				IType myType= getType();
				return myType != null && myType.isSameType( pt.getType() );
			}
		} catch (DOMException e) {
		}
		return false;
	}
	public void setType(IType type) {
		this.type = type;
	}
	public Object clone() {
		return new QualifierTypeClone(this);
	}
}

