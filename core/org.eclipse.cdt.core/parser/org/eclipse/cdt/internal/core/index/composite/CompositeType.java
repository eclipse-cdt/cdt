/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * Represents an index-contexting carrying type
 */
public abstract class CompositeType implements IType, IIndexType, ITypeContainer {
	protected final ITypeContainer type;
	protected final ICompositesFactory cf; 
	
	protected CompositeType(ITypeContainer rtype, ICompositesFactory cf) {
		this.type = rtype;
		this.cf = cf;
	}
	
	public boolean isSameType(IType other) {
		return type.isSameType(other);
	}
	
	public Object clone() {
		fail(); return null; 
	}
	
	public final void setType(IType type) {
		fail();
	}
	
	public final IType getType() throws DOMException {
		return cf.getCompositeType((IIndexType)type.getType());
	}
	
	protected void fail() {
		throw new CompositingNotImplementedError("Compositing feature (for IType) not implemented"); //$NON-NLS-1$
	}
}
