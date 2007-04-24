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
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.index.IIndexType;

public class CompositeFunctionType extends CompositeType implements IFunctionType, IIndexType {

	public CompositeFunctionType(IFunctionType rtype, ICompositesFactory cf) {
		super(rtype, cf);
	}

	public IType[] getParameterTypes() throws DOMException {
		IType[] result = ((IFunctionType)type).getParameterTypes();
		for(int i=0; i<result.length; i++) {
			result[i] = cf.getCompositeType((IIndexType)result[i]);
		}
		return result;
	}

	public IType getReturnType() throws DOMException {
		return cf.getCompositeType((IIndexType)((IFunctionType)type).getReturnType());
	}

	public boolean isSameType(IType other) {
		return type.isSameType(other);
	}

}
