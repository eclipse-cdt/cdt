/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.PointerTypeClone;

public class CompositePointerType extends CompositeTypeContainer implements IPointerType {
	public CompositePointerType(IPointerType pointerType, ICompositesFactory cf) {
		super((ITypeContainer) pointerType, cf);
	}
	
	public boolean isConst() {
		return ((IPointerType) type).isConst();
	}

	public boolean isVolatile() {
		return ((IPointerType) type).isVolatile();
	}
	
	@Override
	public boolean isSameType(IType other) {
		return ((IPointerType) type).isSameType(other);
	}
	
	@Override
	public Object clone() {
		return new PointerTypeClone(this);
	}
}
