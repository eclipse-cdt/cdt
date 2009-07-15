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

import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

public class QualifierTypeClone implements IQualifierType, ITypeContainer, IIndexType {
	private final IQualifierType delegate;
	private IType type;

	public QualifierTypeClone(IQualifierType qualifier) {
		this.delegate = qualifier;
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
			return type.isSameType(this);
		if (!(type instanceof IQualifierType)) 
			return false;

		IQualifierType pt = (IQualifierType) type;
		if (isConst() == pt.isConst() && isVolatile() == pt.isVolatile()) {
			IType myType= getType();
			return myType != null && myType.isSameType(pt.getType());
		}
		return false;
	}

	public void setType(IType type) {
		this.type = type;
	}

	@Override
	public Object clone() {
		return new QualifierTypeClone(this);
	}
	
	@Override
	public String toString() {
		return delegate.toString();
	}
}

