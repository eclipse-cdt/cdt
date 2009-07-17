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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.ArrayTypeClone;

public class CompositeArrayType extends CompositeTypeContainer implements IArrayType {
	public CompositeArrayType(IArrayType arrayType, ICompositesFactory cf) {
		super((ITypeContainer) arrayType, cf);
	}

	@Override
	public Object clone() {
		return new ArrayTypeClone(this);
	}

	public IValue getSize() {
		return ((IArrayType) type).getSize();
	}

	@Deprecated
	public IASTExpression getArraySizeExpression() {
		return null;
	}
}
