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
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

public class CompositeQualifierType extends CompositeType implements IQualifierType, ITypeContainer {
	public CompositeQualifierType(IQualifierType qualifierType, ICompositesFactory cf) throws DOMException {
		super((ITypeContainer) qualifierType, cf);
	}

	public boolean isConst() throws DOMException {
		return ((IQualifierType)type).isConst();
	}

	public boolean isVolatile() throws DOMException {
		return ((IQualifierType)type).isVolatile();
	}

	public boolean isSameType(IType other) {		
		return ((IQualifierType)type).isSameType(other);
	}
}
